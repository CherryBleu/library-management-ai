package com.example.library.domain.admin;
import com.example.library.domain.DomainException;
public record Actor(Role role,long id) {
 public enum Role { READER, LIBRARIAN, SYSTEM_ADMIN }
 public Actor { DomainException.require(id>0 && role!=null,"UNAUTHENTICATED"); }
}
