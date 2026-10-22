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
public interface AdminRepository {
 boolean actorActive(Actor actor); long insertStaff(boolean admin,String name,String employeeNo); boolean staffExists(boolean admin,long id); int activeAdmins(); void deactivateStaff(boolean admin,long id); void audit(Actor actor,String action,String target,String result); List<Map<String,Object>> audits();
}
