package com.example.library.application;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.admin.Actor;
import com.example.library.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.library.domain.DomainException.require;
import static com.example.library.domain.admin.Actor.Role.*;

import java.time.*;
import com.example.library.domain.circulation.*;
import com.example.library.domain.fine.*;
@Service
public class CirculationService {
 private final LibraryRepository repo;private final AuthorizationService auth;private final Clock clock;
 public CirculationService(LibraryRepository repo,AuthorizationService auth,Clock clock){this.repo=repo;this.auth=auth;this.clock=clock;}
 @Transactional public Views.LoanView borrow(Actor actor,Requests.BorrowBook req){
 repo.lock();auth.check(actor,LIBRARIAN);LocalDate today=LocalDate.now(clock);
 var card=repo.card(req.cardNo());require(card!=null&&card.valid(today),"INVALID_CARD");
 var reader=repo.reader(card.readerId());require(reader!=null&&reader.active(),"INVALID_CARD");
 var policies=new BorrowPolicyService(repo);policies.canBorrow(reader,repo.loans(reader.id()),today);
 var item=repo.item(req.barcode());require(item!=null,"ITEM_NOT_FOUND");
 var title=repo.title(item.titleId());require(title!=null&&title.active(),"ITEM_UNAVAILABLE");
 var hold=repo.reservations().stream().filter(r->"READY".equals(r.status())&&r.itemId()!=null&&r.itemId()==item.id()).findFirst().orElse(null);
 boolean ownHold="RESERVED".equals(item.status())&&hold!=null&&hold.readerId()==reader.id()&&!hold.expired(today);
 require(item.borrowable()&&(item.available()||ownHold),"ITEM_UNAVAILABLE");
 var rule=new FineRuleService(repo).rule(item.itemType());
 long id=repo.insertLoan(reader.id(),card.id(),item.id(),actor.id(),today,policies.policy(reader.readerType()),rule);
 repo.itemStatus(item.id(),"ON_LOAN");
 if(ownHold)repo.reservationStatus(hold.id(),"FULFILLED",item.id(),hold.holdUntil());
 repo.audit(actor,"BORROW","loan:"+id,"SUCCESS");
 return Views.loan(repo.activeLoan(item.id()),today);
 }
}
