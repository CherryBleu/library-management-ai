package com.example.library.application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.dto.*;
@Service
public class ReaderService {
 private final LibraryRepository repo;
 public ReaderService(LibraryRepository repo){this.repo=repo;}
 @Transactional public Views.ReaderView register(Requests.RegisterReader req){
 repo.lock();long id=repo.insertReader(req.identityNo().trim(),req.name().trim(),req.department().trim(),req.readerType(),req.contact());
 repo.audit(null,"REGISTER","reader:"+id,"SUCCESS");return Views.reader(repo.reader(id));
 }
}
