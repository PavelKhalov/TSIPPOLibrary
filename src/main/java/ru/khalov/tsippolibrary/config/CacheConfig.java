package ru.khalov.tsippolibrary.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

/**
 * Redis кэш для карточек книг (ТЗ 3.1.6, 3.6.2, ВИ-6).
 * При недоступности Redis — fallback на БД (ТЗ 3.3.4): CacheErrorHandler гасит исключения Redis.
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJacksonJsonRedisSerializer(new ObjectMapper())));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

    // ТЗ 3.3.4: при недоступности Redis система продолжает работу через PostgreSQL
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Redis get failed (cache={}, key={}), fallback to DB: {}", cache.getName(), key, ex.toString());
            }
            @Override public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log.warn("Redis put failed (cache={}, key={}): {}", cache.getName(), key, ex.toString());
            }
            @Override public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Redis evict failed (cache={}, key={}): {}", cache.getName(), key, ex.toString());
            }
            @Override public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log.warn("Redis clear failed (cache={}): {}", cache.getName(), ex.toString());
            }
        };
    }
}
