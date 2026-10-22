package com.example.library.presentation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.Map;
import com.example.library.application.CardService;
import com.example.library.application.CatalogService;
import com.example.library.dto.*;
@RestController @RequestMapping("/api/admin")
public class AdminController {
 private final CardService cards;
 private final CatalogService catalog;
 public AdminController(CardService cards,CatalogService catalog){this.cards=cards;this.catalog=catalog;}
 @PostMapping("/book-titles") @ResponseStatus(HttpStatus.CREATED)
 public Views.TitleView title(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.AddTitle req){return catalog.addTitle(Requests.actor(role,id),req);}
 @PostMapping("/library-items") @ResponseStatus(HttpStatus.CREATED)
 public Views.ItemView item(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.AddItem req){return catalog.addItem(Requests.actor(role,id),req);}
 @DeleteMapping("/book-titles/{target}")
 public Map<String,String> removeTitle(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long target){catalog.removeTitle(Requests.actor(role,id),target);return Map.of("status","INACTIVE");}
 @DeleteMapping("/library-items/{target}")
 public Map<String,String> removeItem(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long target){catalog.removeItem(Requests.actor(role,id),target);return Map.of("status","INACTIVE");}
 @PostMapping("/borrow-cards") @ResponseStatus(HttpStatus.CREATED)
 public Views.CardView issue(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.IssueCard req){return cards.issue(Requests.actor(role,id),req);}
 @DeleteMapping("/borrow-cards/{cardId}")
 public Map<String,String> deactivate(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long cardId){cards.deactivate(Requests.actor(role,id),cardId);return Map.of("status","INACTIVE");}
}
