package com.example.library.domain.repository;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import com.example.library.domain.reader.*;
import com.example.library.domain.card.*;
import com.example.library.domain.catalog.*;
import com.example.library.domain.circulation.*;
import com.example.library.domain.reservation.*;
import com.example.library.domain.fine.*;
import com.example.library.domain.admin.Actor;
public interface BorrowCardRepository {
 BorrowCard card(String no); BorrowCard card(long id); BorrowCard activeCard(long readerId); long insertCard(Reader reader,LocalDate expiresAt,LocalDate today); void deactivateCard(long id);
}
