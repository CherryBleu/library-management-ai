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
public class QueryService {
 private final LibraryRepository repo;private final AuthorizationService auth;private final Clock clock;
 public QueryService(LibraryRepository repo,AuthorizationService auth,Clock clock){this.repo=repo;this.auth=auth;this.clock=clock;}
 @Transactional(readOnly=true) public List<Views.TitleView> books(Actor actor,String keyword){
 auth.check(actor,READER,LIBRARIAN,SYSTEM_ADMIN);String key=keyword==null?"":keyword.trim();require(key.length()<=100,"INVALID_INPUT");
 return repo.titles(key).stream().map(t->Views.title(t,repo.items(t.id()))).toList();
 }
 @Transactional(readOnly=true) public List<Views.LoanView> loans(Actor actor,long readerId){
 auth.check(actor,READER,LIBRARIAN,SYSTEM_ADMIN);auth.owner(actor,readerId);require(repo.reader(readerId)!=null,"READER_NOT_FOUND");
 return repo.loans(readerId).stream().map(l->Views.loan(l,LocalDate.now(clock))).toList();
 }
}
