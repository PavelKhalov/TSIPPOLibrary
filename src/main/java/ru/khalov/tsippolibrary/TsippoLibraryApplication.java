package ru.khalov.tsippolibrary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching   // Redis кэш (ТЗ 3.1.6)
@EnableScheduling // плановая очистка кэша
public class TsippoLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TsippoLibraryApplication.class, args);
    }
}
