package com.example.library.presentation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.Map;
import com.example.library.application.CardService;
import com.example.library.dto.*;
@RestController @RequestMapping("/api/admin")
public class AdminController {
 private final CardService cards;
 public AdminController(CardService cards){this.cards=cards;}
 @PostMapping("/borrow-cards") @ResponseStatus(HttpStatus.CREATED)
 public Views.CardView issue(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.IssueCard req){return cards.issue(Requests.actor(role,id),req);}
 @DeleteMapping("/borrow-cards/{cardId}")
 public Map<String,String> deactivate(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long cardId){cards.deactivate(Requests.actor(role,id),cardId);return Map.of("status","INACTIVE");}
}
