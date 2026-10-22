package com.example.library.presentation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.Map;
import com.example.library.application.ReaderService;
import com.example.library.application.ReservationService;
import com.example.library.application.QueryService;
import com.example.library.dto.*;
@RestController @RequestMapping("/api")
public class ReaderController {
 private final ReaderService readers;
 private final ReservationService reservations;
 private final QueryService queries;
 public ReaderController(ReaderService readers,ReservationService reservations,QueryService queries){this.readers=readers;this.reservations=reservations;this.queries=queries;}
 @GetMapping("/books")
 public java.util.List<Views.TitleView> books(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@RequestParam(required=false) String keyword){return queries.books(Requests.actor(role,id),keyword);}
 @GetMapping("/readers/{target}/loans")
 public java.util.List<Views.LoanView> loans(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long target){return queries.loans(Requests.actor(role,id),target);}
 @PostMapping("/reservations") @ResponseStatus(HttpStatus.CREATED)
 public Views.ReservationView reserve(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.ReserveBook req){return reservations.reserve(Requests.actor(role,id),req);}
 @DeleteMapping("/reservations/{target}")
 public Map<String,String> cancel(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long target){reservations.cancel(Requests.actor(role,id),target);return Map.of("status","CANCELLED");}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","UP");}
 @PostMapping("/readers") @ResponseStatus(HttpStatus.CREATED)
 public Views.ReaderView register(@Valid @RequestBody Requests.RegisterReader req){return readers.register(req);}
}
