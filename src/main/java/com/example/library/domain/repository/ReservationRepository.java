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
public interface ReservationRepository {
 List<Reservation> reservations(); Reservation reservation(long id); long insertReservation(long readerId,long titleId,Instant now); void reservationStatus(long id,String status,Long itemId,LocalDate until);
}
