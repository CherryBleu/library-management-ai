package com.example.library.application;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.admin.Actor;
import com.example.library.domain.card.BorrowCard;
import com.example.library.dto.*;
import static com.example.library.domain.DomainException.require;
import static com.example.library.domain.admin.Actor.Role.SYSTEM_ADMIN;
@Service
public class CardService {
 private final LibraryRepository repo;private final AuthorizationService auth;private final Clock clock;
 public CardService(LibraryRepository repo,AuthorizationService auth,Clock clock){this.repo=repo;this.auth=auth;this.clock=clock;}
 @Transactional public Views.CardView issue(Actor actor,Requests.IssueCard req){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);var r=repo.reader(req.readerId());require(r!=null,"READER_NOT_FOUND");require(r.active(),"INVALID_CARD");
 LocalDate today=LocalDate.now(clock);require(req.expiresAt()!=null&&!req.expiresAt().isBefore(today),"INVALID_INPUT");
 BorrowCard old=repo.activeCard(r.id());
 require(old==null||!old.valid(today),"DUPLICATE_CARD");
 if(old!=null)repo.deactivateCard(old.id());
 long id=repo.insertCard(r,req.expiresAt(),today);repo.audit(actor,"ISSUE_CARD","card:"+id,"SUCCESS");return Views.card(repo.card(id));
 }
 @Transactional public void deactivate(Actor actor,long id){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);var card=repo.card(id);require(card!=null,"CARD_NOT_FOUND");
 require(repo.loans(card.readerId()).stream().noneMatch(l->l.active())&&!repo.unpaidFines(card.readerId())&&repo.reservations().stream().noneMatch(r->r.readerId()==card.readerId()&&r.active()),"ACTIVE_ASSOCIATION");
 repo.deactivateCard(id);repo.audit(actor,"DEACTIVATE_CARD","card:"+id,"SUCCESS");
 }
}
