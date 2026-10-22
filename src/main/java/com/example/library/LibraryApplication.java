package com.example.library;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class LibraryApplication {
 public static void main(String[] args) { SpringApplication.run(LibraryApplication.class,args); }
 @Bean public Clock clock() { return Clock.system(ZoneId.of("Asia/Shanghai")); }
}
