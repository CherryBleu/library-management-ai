package com.example.library.application;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.admin.Actor;
import com.example.library.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.library.domain.DomainException.require;
import static com.example.library.domain.admin.Actor.Role.*;

import java.time.*;
import java.util.*;
@Service
public class ReservationService {
 private final LibraryRepository repo;private final AuthorizationService auth;private final Clock clock;
 public ReservationService(LibraryRepository repo,AuthorizationService auth,Clock clock){this.repo=repo;this.auth=auth;this.clock=clock;}
 private boolean eligible(long readerId,LocalDate today){var r=repo.reader(readerId);var c=repo.activeCard(readerId);return r!=null&&r.active()&&c!=null&&c.valid(today);}
 @Transactional public void expireAndPromote(){
 repo.lock();LocalDate today=LocalDate.now(clock);
 for(var r:repo.reservations()){
 if(r.active()&&(!eligible(r.readerId(),today)||r.expired(today))){
  if(r.itemId()!=null&&"READY".equals(r.status()))repo.itemStatus(r.itemId(),"AVAILABLE");
  repo.reservationStatus(r.id(),"EXPIRED",r.itemId(),r.holdUntil());
 }
 }
 Set<Long> titles=new LinkedHashSet<>();repo.reservations().stream().filter(r->"WAITING".equals(r.status())).forEach(r->titles.add(r.titleId()));
 for(long titleId:titles){
 var title=repo.title(titleId);if(title==null||!title.active())continue;
 var waiting=repo.reservations().stream().filter(r->r.titleId()==titleId&&"WAITING".equals(r.status())).iterator();
 for(var item:repo.items(titleId)){
 if(!waiting.hasNext())break;
 if(item.available()){var first=waiting.next();repo.itemStatus(item.id(),"RESERVED");repo.reservationStatus(first.id(),"READY",item.id(),today.plusDays(3));}
 }
 }
 }
}
