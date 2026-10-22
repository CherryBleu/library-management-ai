package com.example.library.application;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Component
@ConditionalOnProperty(name="library.scheduling.enabled",havingValue="true",matchIfMissing=true)
public class ReservationScheduler {
 private final ReservationService service;
 public ReservationScheduler(ReservationService service){this.service=service;}
 @Scheduled(fixedDelay=60000,initialDelay=60000) public void expire(){service.expireAndPromote();}
}
