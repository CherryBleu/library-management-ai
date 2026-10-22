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
public interface ReaderRepository {
 Reader reader(long id); long insertReader(String identityNo,String name,String department,ReaderType type,String contact);
}
