package com.example.library.presentation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import com.example.library.application.CirculationService;
import com.example.library.dto.*;
@RestController @RequestMapping("/api/circulation")
public class LibrarianController {
 private final CirculationService service;
 public LibrarianController(CirculationService service){this.service=service;}
 @PostMapping("/borrow") @ResponseStatus(HttpStatus.CREATED)
 public Views.LoanView borrow(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.BorrowBook req){return service.borrow(Requests.actor(role,id),req);}
}
