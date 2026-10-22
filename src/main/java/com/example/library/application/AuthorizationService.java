package com.example.library.application;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.admin.Actor;
import com.example.library.domain.DomainException;
@Service
public class AuthorizationService {
 private final LibraryRepository repo;private final AuditService audit;
 public AuthorizationService(LibraryRepository repo,AuditService audit){this.repo=repo;this.audit=audit;}
 public void check(Actor actor,Actor.Role... roles){
 String error=actor==null?"UNAUTHENTICATED":!Arrays.asList(roles).contains(actor.role())?"FORBIDDEN":!repo.actorActive(actor)?
 (actor.role()==Actor.Role.READER&&repo.reader(actor.id())==null?"READER_NOT_FOUND":"FORBIDDEN"):null;
 if(error!=null){audit.denied(actor,error);throw new DomainException(error);}
 }
 public void owner(Actor actor,long id){if(actor.role()==Actor.Role.READER&&actor.id()!=id){audit.denied(actor,"FORBIDDEN");throw new DomainException("FORBIDDEN");}}
}
