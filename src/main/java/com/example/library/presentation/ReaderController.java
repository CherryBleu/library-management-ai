package com.example.library.presentation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.Map;
import com.example.library.application.ReaderService;
import com.example.library.dto.*;
@RestController @RequestMapping("/api")
public class ReaderController {
 private final ReaderService readers;
 public ReaderController(ReaderService readers){this.readers=readers;}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","UP");}
 @PostMapping("/readers") @ResponseStatus(HttpStatus.CREATED)
 public Views.ReaderView register(@Valid @RequestBody Requests.RegisterReader req){return readers.register(req);}
}
