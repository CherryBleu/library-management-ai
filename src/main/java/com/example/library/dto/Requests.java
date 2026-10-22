package com.example.library.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import com.example.library.domain.reader.ReaderType;
import com.example.library.domain.catalog.ItemType;
import com.example.library.domain.admin.Actor;
import com.example.library.domain.DomainException;
public final class Requests {
 private Requests(){}
 public static Actor actor(String role,Long id){
 if(role==null||id==null)throw new DomainException("UNAUTHENTICATED");
 try{return new Actor(Actor.Role.valueOf(role),id);}catch(IllegalArgumentException e){throw new DomainException("UNAUTHENTICATED");}
 }
 public record RegisterReader(@NotBlank @Size(max=80) String identityNo,@NotBlank @Size(max=100) String name,@NotBlank @Size(max=150) String department,@NotNull ReaderType readerType,@Size(max=100) String contact){}
 public record IssueCard(@Positive long readerId,@NotNull LocalDate expiresAt){}
 public record AddTitle(@NotBlank @Size(max=300) String title,@NotBlank @Size(max=500) String authors,@Size(max=50) String standardNo,@Size(max=200) String publisher,@NotNull ItemType itemType){}
 public record AddItem(@Positive long titleId,@NotBlank @Size(max=80) String barcode,@NotBlank @Size(max=100) String location,boolean borrowable){}
 public record BorrowBook(@NotBlank @Size(max=20) String cardNo,@NotBlank @Size(max=80) String barcode){}
 public record ReturnBook(@NotBlank @Size(max=80) String barcode){}
 public record ReserveBook(@Positive long titleId){}
 public record AddStaff(@NotBlank @Size(max=100) String name,@NotBlank @Size(max=50) String employeeNo){}
 public record UpdatePolicy(@Min(1) @Max(1000) int maxBorrowCount,@Min(1) @Max(3650) int borrowDays){}
 public record UpdateFineRule(@NotNull @DecimalMin("0") @Digits(integer=8,fraction=2) BigDecimal dailyRate,@NotNull @DecimalMin("0") @Digits(integer=8,fraction=2) BigDecimal maximumAmount){}
 public record AdjustFine(@NotNull @DecimalMin("0") @Digits(integer=8,fraction=2) BigDecimal amount,@NotBlank @Size(max=500) String reason){}
}
