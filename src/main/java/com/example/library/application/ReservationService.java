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
 @Transactional public Views.ReservationView reserve(Actor actor,Requests.ReserveBook req){
 repo.lock();auth.check(actor,READER);expireAndPromote();LocalDate today=LocalDate.now(clock);
 require(eligible(actor.id(),today),"INVALID_CARD");
 var title=repo.title(req.titleId());require(title!=null&&title.active(),"TITLE_NOT_FOUND");
 var active=repo.reservations().stream().filter(r->r.readerId()==actor.id()&&r.active()).toList();
 require(active.stream().noneMatch(r->r.titleId()==req.titleId()),"DUPLICATE_RESERVATION");
 require(active.size()<5,"RESERVATION_LIMIT");
 var items=repo.items(title.id());
 require(items.stream().noneMatch(i->i.available()),"ITEM_AVAILABLE");
 require(items.stream().anyMatch(i->i.borrowable()&&!"INACTIVE".equals(i.status())&&!"LOST".equals(i.status())),"ITEM_UNAVAILABLE");
 long id=repo.insertReservation(actor.id(),title.id(),clock.instant());
 repo.audit(actor,"RESERVE","reservation:"+id,"SUCCESS");return Views.reservation(repo.reservation(id));
 }
 @Transactional public void cancel(Actor actor,long id){
 repo.lock();auth.check(actor,READER,SYSTEM_ADMIN);var r=repo.reservation(id);require(r!=null,"RESERVATION_NOT_FOUND");
 auth.owner(actor,r.readerId());require(r.active(),"RESERVATION_CLOSED");
 if("READY".equals(r.status())&&r.itemId()!=null)repo.itemStatus(r.itemId(),"AVAILABLE");
 repo.reservationStatus(id,"CANCELLED",r.itemId(),r.holdUntil());expireAndPromote();
 repo.audit(actor,"CANCEL_RESERVATION","reservation:"+id,"SUCCESS");
 }
 @Transactional public void expire(Actor actor){repo.lock();auth.check(actor,SYSTEM_ADMIN);expireAndPromote();repo.audit(actor,"EXPIRE_RESERVATIONS","queue","SUCCESS");}
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
