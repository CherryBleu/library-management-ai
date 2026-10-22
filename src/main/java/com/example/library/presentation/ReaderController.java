package com.example.library.presentation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.Map;
import com.example.library.application.ReaderService;
import com.example.library.application.ReservationService;
import com.example.library.dto.*;
@RestController @RequestMapping("/api")
public class ReaderController {
 private final ReaderService readers;
 private final ReservationService reservations;
 public ReaderController(ReaderService readers,ReservationService reservations){this.readers=readers;this.reservations=reservations;}
 @PostMapping("/reservations") @ResponseStatus(HttpStatus.CREATED)
 public Views.ReservationView reserve(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@Valid @RequestBody Requests.ReserveBook req){return reservations.reserve(Requests.actor(role,id),req);}
 @DeleteMapping("/reservations/{target}")
 public Map<String,String> cancel(@RequestHeader(value="X-Role",required=false) String role,@RequestHeader(value="X-Actor-Id",required=false) Long id,@PathVariable long target){reservations.cancel(Requests.actor(role,id),target);return Map.of("status","CANCELLED");}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","UP");}
 @PostMapping("/readers") @ResponseStatus(HttpStatus.CREATED)
 public Views.ReaderView register(@Valid @RequestBody Requests.RegisterReader req){return readers.register(req);}
}
