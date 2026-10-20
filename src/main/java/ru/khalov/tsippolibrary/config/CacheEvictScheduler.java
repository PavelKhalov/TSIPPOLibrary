package ru.khalov.tsippolibrary.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Плановое задание — полная очистка кэша каждую ночь в 00:00 (ТЗ 3.1.6).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictScheduler {

    private final CacheManager cacheManager;

    @Scheduled(cron = "${library.cache.evict-cron:0 0 0 * * *}")
    public void evictAll() {
        log.info("Scheduled cache eviction started");
        try {
            cacheManager.getCacheNames().forEach(name -> {
                try {
                    var cache = cacheManager.getCache(name);
                    if (cache != null) {
                        cache.clear();
                        log.info("Cache '{}' cleared", name);
                    }
                } catch (Exception e) {
                    // ТЗ 3.3.4: Redis может быть недоступен — не падать
                    log.warn("Failed to clear cache '{}': {}", name, e.toString());
                }
            });
        } catch (Exception e) {
            log.warn("Scheduled eviction failed: {}", e.toString());
        }
    }
}
