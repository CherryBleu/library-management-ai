package com.example.library.presentation;
import com.example.library.domain.DomainException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(DomainException.class) public ResponseEntity<?> domain(DomainException e){
 int status=e.code().equals("UNAUTHENTICATED")?401:e.code().equals("FORBIDDEN")?403:e.code().endsWith("_NOT_FOUND")?404:e.code().equals("INVALID_INPUT")?400:409;
 return ResponseEntity.status(status).body(Map.of("code",e.code(),"message",e.getMessage()));
 }
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> conflict(Exception e){return ResponseEntity.status(409).body(Map.of("code","CONFLICT","message","Unique or relational constraint violation"));}
 @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,ConstraintViolationException.class,IllegalArgumentException.class})
 public ResponseEntity<?> invalid(Exception e){return ResponseEntity.badRequest().body(Map.of("code","INVALID_INPUT","message","Invalid request fields"));}
 @ExceptionHandler(Exception.class) public ResponseEntity<?> unexpected(Exception e){
 org.slf4j.LoggerFactory.getLogger(ApiErrors.class).error("Request failed",e);
 return ResponseEntity.internalServerError().body(Map.of("code","INTERNAL_ERROR","message","Request failed"));
 }
}
