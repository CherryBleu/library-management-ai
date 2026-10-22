package com.example.library.infrastructure;
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

import java.sql.*;
import com.example.library.domain.DomainFactory;
import com.example.library.domain.repository.LibraryRepository;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Repository;
@Repository
public class JdbcLibraryRepository implements LibraryRepository {
 private final JdbcTemplate db;
 public JdbcLibraryRepository(JdbcTemplate db){this.db=db;}
 private <T> T one(String sql,RowMapper<T> mapper,Object... args){return db.query(sql,mapper,args).stream().findFirst().orElse(null);}
 private long insert(String sql,Object... args){
  KeyHolder keys=new GeneratedKeyHolder();
  db.update(c->{PreparedStatement s=c.prepareStatement(sql,new int[]{1});
   for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);return s;},keys);
  return Objects.requireNonNull(keys.getKey()).longValue();
 }
 private static LocalDate date(ResultSet r,String n)throws SQLException {return r.getObject(n,LocalDate.class);}
 public void lock(){db.queryForObject("SELECT id FROM circulation_lock WHERE id=1 FOR UPDATE",Integer.class);}
 private final RowMapper<Reader> readerMap=(r,n)->DomainFactory.reader(r.getLong("reader_id"),r.getString("name"),r.getString("department"),ReaderType.valueOf(r.getString("reader_type")),r.getString("status"));
 public Reader reader(long id){return one("SELECT * FROM readers WHERE reader_id=?",readerMap,id);}
 public long insertReader(String identityNo,String name,String department,ReaderType type,String contact){return insert("INSERT INTO readers(identity_no,name,department,reader_type,contact) VALUES(?,?,?,?,?)",identityNo,name,department,type.name(),contact);}
 private final RowMapper<BorrowCard> cardMap=(r,n)->new BorrowCard(r.getLong("card_id"),r.getString("card_no"),r.getLong("reader_id"),date(r,"expires_at"),r.getString("status"));
 public BorrowCard card(String no){return one("SELECT * FROM borrow_cards WHERE card_no=?",cardMap,no);}
 public BorrowCard card(long id){return one("SELECT * FROM borrow_cards WHERE card_id=?",cardMap,id);}
 public BorrowCard activeCard(long id){return one("SELECT * FROM borrow_cards WHERE active_reader_id=?",cardMap,id);}
 public long insertCard(Reader reader,LocalDate expiry,LocalDate today){
  int year=today.getYear();
  db.update("INSERT INTO card_sequences SELECT ?,1 WHERE NOT EXISTS(SELECT 1 FROM card_sequences WHERE yr=?)",year,year);
  int no=db.queryForObject("SELECT next_no FROM card_sequences WHERE yr=?",Integer.class,year);
  if(no>999999)throw new com.example.library.domain.DomainException("CARD_SEQUENCE_EXHAUSTED");
  db.update("UPDATE card_sequences SET next_no=next_no+1 WHERE yr=?",year);
  return insert("INSERT INTO borrow_cards(card_no,reader_id,holder_name,department,issued_at,expires_at,active_reader_id) VALUES(?,?,?,?,?,?,?)",
   "CARD"+year+String.format("%06d",no),reader.id(),reader.name(),reader.department(),today,expiry,reader.id());
 }
 public void deactivateCard(long id){db.update("UPDATE borrow_cards SET status='INACTIVE',active_reader_id=NULL,deleted_at=CURRENT_TIMESTAMP WHERE card_id=?",id);}
 private final RowMapper<BookTitle> titleMap=(r,n)->new BookTitle(r.getLong("title_id"),r.getString("title"),r.getString("authors"),r.getString("isbn_or_standard_no"),ItemType.valueOf(r.getString("item_type")),r.getString("status"));
 public BookTitle title(long id){return one("SELECT * FROM book_titles WHERE title_id=?",titleMap,id);}
 public List<BookTitle> titles(String key){String k="%"+key.toLowerCase(Locale.ROOT)+"%";return db.query("SELECT * FROM book_titles WHERE status='ACTIVE' AND (LOWER(title) LIKE ? OR LOWER(authors) LIKE ? OR LOWER(isbn_or_standard_no) LIKE ?) ORDER BY title_id",titleMap,k,k,k);}
 public long insertTitle(String title,String authors,String no,String publisher,ItemType type){return insert("INSERT INTO book_titles(title,authors,isbn_or_standard_no,publisher,language,item_type) VALUES(?,?,?,?,?,?)",title,authors,no,publisher,type.language(),type.name());}
 public void deactivateTitle(long id){db.update("UPDATE book_titles SET status='INACTIVE' WHERE title_id=?",id);}
 private final RowMapper<LibraryItem> itemMap=(r,n)->DomainFactory.item(r.getLong("item_id"),r.getLong("title_id"),r.getString("barcode"),r.getString("location"),r.getString("status"),"BORROWABLE".equals(r.getString("loan_eligibility")),ItemType.valueOf(r.getString("item_type")));
 public LibraryItem item(String b){return one("SELECT * FROM library_items WHERE barcode=?",itemMap,b);}
 public LibraryItem item(long id){return one("SELECT * FROM library_items WHERE item_id=?",itemMap,id);}
 public List<LibraryItem> items(long titleId){return db.query("SELECT * FROM library_items WHERE title_id=? ORDER BY item_id",itemMap,titleId);}
 public long insertItem(long title,String barcode,String location,boolean borrowable,ItemType type){return insert("INSERT INTO library_items(title_id,barcode,location,loan_eligibility,item_type,language) VALUES(?,?,?,?,?,?)",title,barcode,location,borrowable?"BORROWABLE":"REFERENCE_ONLY",type.name(),type.language());}
 public void itemStatus(long id,String status){db.update("UPDATE library_items SET status=? WHERE item_id=?",status,id);}
 private final RowMapper<Loan> loanMap=(r,n)->new Loan(r.getLong("loan_id"),r.getLong("reader_id"),r.getLong("item_id"),r.getLong("card_id"),date(r,"borrowed_at"),date(r,"due_at"),date(r,"returned_at"),r.getBigDecimal("daily_rate"),r.getBigDecimal("maximum_amount"),r.getLong("fine_rule_id"));
 public List<Loan> loans(long readerId){return db.query("SELECT * FROM loans WHERE reader_id=? ORDER BY loan_id",loanMap,readerId);}
 public Loan activeLoan(long itemId){return one("SELECT * FROM loans WHERE active_item_id=?",loanMap,itemId);}
 public long insertLoan(long readerId,long cardId,long itemId,long librarian,LocalDate today,BorrowPolicy p,FineRule f){
 return insert("INSERT INTO loans(reader_id,card_id,item_id,librarian_id,borrowed_at,due_at,policy_version,borrow_policy_id,fine_rule_id,daily_rate,maximum_amount,active_item_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
 readerId,cardId,itemId,librarian,today,today.plusDays(p.borrowDays()),"policy-"+p.id(),p.id(),f.id(),f.dailyRate(),f.maximumAmount(),itemId);
 }
 public void closeLoan(long id,LocalDate today,long librarian){db.update("UPDATE loans SET returned_at=?,return_librarian_id=?,status='RETURNED',active_item_id=NULL WHERE loan_id=?",today,librarian,id);}
 private final RowMapper<FineRecord> fineMap=(r,n)->new FineRecord(r.getLong("fine_id"),r.getLong("loan_id"),r.getLong("overdue_days"),r.getBigDecimal("amount"),r.getString("status"));
 public FineRecord fine(long id){return one("SELECT * FROM fine_records WHERE fine_id=?",fineMap,id);}
 public FineRecord fineByLoan(long id){return one("SELECT * FROM fine_records WHERE loan_id=?",fineMap,id);}
 public long insertFine(Loan loan,long days,BigDecimal amount){return insert("INSERT INTO fine_records(loan_id,overdue_days,amount,rule_version,status) VALUES(?,?,?,?,?)",loan.id(),days,amount,"rule-"+loan.fineRuleId(),amount.signum()==0?"WAIVED":"UNPAID");}
 public boolean unpaidFines(long readerId){return db.queryForObject("SELECT COUNT(*) FROM fine_records f JOIN loans l ON l.loan_id=f.loan_id WHERE l.reader_id=? AND f.status='UNPAID' AND f.amount>0",Integer.class,readerId)>0;}
 public void adjustFine(long id,BigDecimal amount,String reason,long admin){db.update("UPDATE fine_records SET amount=?,status=?,adjustment_reason=?,adjustment_admin_id=? WHERE fine_id=?",amount,amount.signum()==0?"WAIVED":"UNPAID",reason,admin,id);}
 private final RowMapper<Reservation> reservationMap=(r,n)->new Reservation(r.getLong("reservation_id"),r.getLong("reader_id"),r.getLong("title_id"),r.getLong("queue_no"),r.getString("status"),r.getTimestamp("created_at").toInstant(),r.getObject("item_id",Long.class),date(r,"hold_until"));
 public List<Reservation> reservations(){return db.query("SELECT * FROM reservations ORDER BY created_at,reservation_id",reservationMap);}
 public Reservation reservation(long id){return one("SELECT * FROM reservations WHERE reservation_id=?",reservationMap,id);}
 public long insertReservation(long readerId,long titleId,Instant now){
 long q=db.queryForObject("SELECT COALESCE(MAX(queue_no),0)+1 FROM reservations WHERE title_id=?",Long.class,titleId);
 return insert("INSERT INTO reservations(reader_id,title_id,queue_no,created_at,active_key) VALUES(?,?,?,?,?)",readerId,titleId,q,Timestamp.from(now),readerId+":"+titleId);
 }
 public void reservationStatus(long id,String status,Long itemId,LocalDate until){
 db.update("UPDATE reservations SET status=?,item_id=?,hold_until=?,active_key=CASE WHEN ? IN ('WAITING','READY') THEN active_key ELSE NULL END WHERE reservation_id=?",status,itemId,until,status,id);
 }
 public BorrowPolicy policy(ReaderType type){return one("SELECT * FROM borrow_policies WHERE reader_type=? AND effective_to IS NULL ORDER BY policy_id DESC",(r,n)->new BorrowPolicy(r.getLong("policy_id"),type,r.getInt("max_active_loans"),r.getInt("loan_period_days")),type.name());}
 public FineRule fineRule(ItemType type){return one("SELECT * FROM fine_rules WHERE item_type=? AND effective_to IS NULL ORDER BY rule_id DESC",(r,n)->new FineRule(r.getLong("rule_id"),type,r.getBigDecimal("daily_rate"),r.getBigDecimal("maximum_amount")),type.name());}
 public void updatePolicy(ReaderType type,int count,int days){
 db.update("UPDATE borrow_policies SET effective_to=CURRENT_TIMESTAMP,status='INACTIVE' WHERE reader_type=? AND effective_to IS NULL",type.name());
 insert("INSERT INTO borrow_policies(reader_type,max_active_loans,loan_period_days,version) VALUES(?,?,?,?)",type.name(),count,days,UUID.randomUUID().toString());
 }
 public void updateFineRule(ItemType type,BigDecimal rate,BigDecimal cap){
 db.update("UPDATE fine_rules SET effective_to=CURRENT_TIMESTAMP WHERE item_type=? AND effective_to IS NULL",type.name());
 insert("INSERT INTO fine_rules(item_type,daily_rate,maximum_amount,version) VALUES(?,?,?,?)",type.name(),rate,cap,UUID.randomUUID().toString());
 }
 private String table(boolean admin){return admin?"system_admins":"librarians";}
 private String idColumn(boolean admin){return admin?"admin_id":"librarian_id";}
 public boolean actorActive(Actor actor){
 if(actor.role()==Actor.Role.READER){Reader r=reader(actor.id());return r!=null && r.active();}
 boolean admin=actor.role()==Actor.Role.SYSTEM_ADMIN;
 return db.queryForObject("SELECT COUNT(*) FROM "+table(admin)+" WHERE "+idColumn(admin)+"=? AND status='ACTIVE'",Integer.class,actor.id())>0;
 }
 public long insertStaff(boolean admin,String name,String no){return insert("INSERT INTO "+table(admin)+"(name,employee_no) VALUES(?,?)",name,no);}
 public boolean staffExists(boolean admin,long id){return db.queryForObject("SELECT COUNT(*) FROM "+table(admin)+" WHERE "+idColumn(admin)+"=?",Integer.class,id)>0;}
 public int activeAdmins(){return db.queryForObject("SELECT COUNT(*) FROM system_admins WHERE status='ACTIVE'",Integer.class);}
 public void deactivateStaff(boolean admin,long id){db.update("UPDATE "+table(admin)+" SET status='INACTIVE' WHERE "+idColumn(admin)+"=?",id);}
 public void audit(Actor actor,String action,String target,String result){db.update("INSERT INTO audit_logs(actor_id,role,action,target,result) VALUES(?,?,?,?,?)",actor==null?null:actor.id(),actor==null?"ANONYMOUS":actor.role().name(),action,target,result);}
 public List<Map<String,Object>> audits(){return db.queryForList("SELECT * FROM audit_logs ORDER BY id");}
}
