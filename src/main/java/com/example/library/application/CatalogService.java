package com.example.library.application;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.admin.Actor;
import com.example.library.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.library.domain.DomainException.require;
import static com.example.library.domain.admin.Actor.Role.*;

import java.util.List;
@Service
public class CatalogService {
 private final LibraryRepository repo; private final AuthorizationService auth;
 public CatalogService(LibraryRepository repo,AuthorizationService auth){this.repo=repo;this.auth=auth;}
 @Transactional public Views.TitleView addTitle(Actor actor,Requests.AddTitle req){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);
 long id=repo.insertTitle(req.title().trim(),req.authors().trim(),req.standardNo()==null||req.standardNo().isBlank()?null:req.standardNo().trim(),req.publisher(),req.itemType());
 repo.audit(actor,"ADD_TITLE","title:"+id,"SUCCESS");return Views.title(repo.title(id),List.of());
 }
 @Transactional public Views.ItemView addItem(Actor actor,Requests.AddItem req){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);var t=repo.title(req.titleId());require(t!=null&&t.active(),"TITLE_NOT_FOUND");
 long id=repo.insertItem(t.id(),req.barcode().trim(),req.location().trim(),req.borrowable(),t.itemType());repo.audit(actor,"ADD_ITEM","item:"+id,"SUCCESS");return Views.item(repo.item(id));
 }
 @Transactional public void removeItem(Actor actor,long id){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);var item=repo.item(id);require(item!=null,"ITEM_NOT_FOUND");
 require(!"ON_LOAN".equals(item.status())&&!"RESERVED".equals(item.status()),"ACTIVE_ASSOCIATION");
 repo.itemStatus(id,"INACTIVE");repo.audit(actor,"REMOVE_ITEM","item:"+id,"SUCCESS");
 }
 @Transactional public void removeTitle(Actor actor,long id){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);require(repo.title(id)!=null,"TITLE_NOT_FOUND");
 require(repo.items(id).stream().allMatch(i->"INACTIVE".equals(i.status()))&&repo.reservations().stream().noneMatch(r->r.titleId()==id&&r.active()),"ACTIVE_ASSOCIATION");
 repo.deactivateTitle(id);repo.audit(actor,"REMOVE_TITLE","title:"+id,"SUCCESS");
 }
}
