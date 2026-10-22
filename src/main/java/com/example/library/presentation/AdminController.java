package com.example.library.presentation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.Map;
import com.example.library.application.CardService;
import com.example.library.application.CatalogService;
import com.example.library.application.ReservationService;
import com.example.library.application.AdminService;
import com.example.library.dto.*;
@RestController @RequestMapping("/api/admin")
public class AdminController {
 private final CardService cards;
 private final CatalogService catalog;
 private final ReservationService reservations;
 private final AdminService administration;
 public AdminController(CardService cards,CatalogService catalog,ReservationService reservations,AdminService administration){this.cards=cards;this.catalog=catalog;this.reservations=reservations;this.administration=administration;}
 @PostMapping("/librarians") @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Long> addLibrarian(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.AddStaff req){return Map.of("id",administration.addStaff(Requests.actor(role,id),false,req));}
 @DeleteMapping("/librarians/{target}")
 public Map<String,String> removeLibrarian(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long target){administration.removeStaff(Requests.actor(role,id),false,target);return Map.of("status","INACTIVE");}
 @PostMapping("/system-admins") @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Long> addSystemAdmin(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.AddStaff req){return Map.of("id",administration.addStaff(Requests.actor(role,id),true,req));}
 @DeleteMapping("/system-admins/{target}")
 public Map<String,String> removeSystemAdmin(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long target){administration.removeStaff(Requests.actor(role,id),true,target);return Map.of("status","INACTIVE");}
 @PutMapping("/borrow-policies/{type}")
 public Map<String,String> policy(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable com.example.library.domain.reader.ReaderType type,@Valid @RequestBody Requests.UpdatePolicy req){administration.policy(Requests.actor(role,id),type,req);return Map.of("status","OK");}
 @PutMapping("/fine-rules/{type}")
 public Map<String,String> fineRule(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable com.example.library.domain.catalog.ItemType type,@Valid @RequestBody Requests.UpdateFineRule req){administration.fineRule(Requests.actor(role,id),type,req);return Map.of("status","OK");}
 @PatchMapping("/fines/{target}")
 public Map<String,String> adjust(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long target,@Valid @RequestBody Requests.AdjustFine req){administration.adjustFine(Requests.actor(role,id),target,req);return Map.of("status","OK");}
 @GetMapping("/rules")
 public Map<String,Object> rules(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id){return administration.rules(Requests.actor(role,id));}
 @GetMapping("/audit")
 public java.util.List<Map<String,Object>> audit(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id){return administration.audit(Requests.actor(role,id));}
 @PostMapping("/reservations/expire")
 public Map<String,String> expire(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id){reservations.expire(Requests.actor(role,id));return Map.of("status","OK");}
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
