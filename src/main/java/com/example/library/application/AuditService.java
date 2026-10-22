package com.example.library.application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.admin.Actor;
@Service
public class AuditService {
 private final LibraryRepository repo;
 public AuditService(LibraryRepository repo){this.repo=repo;}
 @Transactional(propagation=Propagation.REQUIRES_NEW)
 public void denied(Actor actor,String code){repo.audit(actor,"AUTHORIZATION","request",code);}
}
