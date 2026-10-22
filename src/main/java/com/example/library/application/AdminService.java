package com.example.library.application;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.admin.Actor;
import com.example.library.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.library.domain.DomainException.require;
import static com.example.library.domain.admin.Actor.Role.*;

import java.util.*;
import com.example.library.domain.reader.ReaderType;
import com.example.library.domain.catalog.ItemType;
@Service
public class AdminService {
 private final LibraryRepository repo;private final AuthorizationService auth;
 public AdminService(LibraryRepository repo,AuthorizationService auth){this.repo=repo;this.auth=auth;}
 @Transactional public long addStaff(Actor actor,boolean admin,Requests.AddStaff req){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);long id=repo.insertStaff(admin,req.name(),req.employeeNo());repo.audit(actor,"ADD_STAFF",(admin?"admin:":"librarian:")+id,"SUCCESS");return id;
 }
 @Transactional public void removeStaff(Actor actor,boolean admin,long id){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);require(repo.staffExists(admin,id),"STAFF_NOT_FOUND");
 if(admin&&repo.actorActive(new Actor(SYSTEM_ADMIN,id)))require(repo.activeAdmins()>1,"LAST_ADMIN");
 repo.deactivateStaff(admin,id);repo.audit(actor,"REMOVE_STAFF",(admin?"admin:":"librarian:")+id,"SUCCESS");
 }
 @Transactional public void policy(Actor actor,ReaderType type,Requests.UpdatePolicy req){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);require(req.maxBorrowCount()>0&&req.maxBorrowCount()<=1000&&req.borrowDays()>0&&req.borrowDays()<=3650,"INVALID_INPUT");
 repo.updatePolicy(type,req.maxBorrowCount(),req.borrowDays());repo.audit(actor,"UPDATE_POLICY",type.name(),"SUCCESS");
 }
 @Transactional public void fineRule(Actor actor,ItemType type,Requests.UpdateFineRule req){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);require(req.dailyRate().signum()>=0&&req.maximumAmount().signum()>=0,"INVALID_INPUT");
 repo.updateFineRule(type,req.dailyRate(),req.maximumAmount());repo.audit(actor,"UPDATE_FINE_RULE",type.name(),"SUCCESS");
 }
 @Transactional public void adjustFine(Actor actor,long id,Requests.AdjustFine req){
 repo.lock();auth.check(actor,SYSTEM_ADMIN);var f=repo.fine(id);require(f!=null,"FINE_NOT_FOUND");
 require(req.amount()!=null&&req.amount().signum()>=0&&req.amount().compareTo(f.amount())<=0&&req.reason()!=null&&!req.reason().isBlank(),"INVALID_INPUT");
 repo.adjustFine(id,req.amount(),req.reason(),actor.id());repo.audit(actor,"ADJUST_FINE","fine:"+id,"SUCCESS");
 }
 @Transactional(readOnly=true) public Map<String,Object> rules(Actor actor){
 auth.check(actor,SYSTEM_ADMIN);return Map.of("borrowPolicies",Arrays.stream(ReaderType.values()).map(repo::policy).toList(),"fineRules",Arrays.stream(ItemType.values()).map(repo::fineRule).toList());
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> audit(Actor actor){auth.check(actor,SYSTEM_ADMIN);return repo.audits();}
}
