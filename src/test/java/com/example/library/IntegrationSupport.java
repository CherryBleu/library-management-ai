package com.example.library;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import com.example.library.application.*;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.admin.Actor;
import com.example.library.domain.reader.ReaderType;
import com.example.library.domain.catalog.ItemType;
import com.example.library.dto.*;
import java.time.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:integration;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000","library.scheduling.enabled=false","logging.level.root=WARN","debug=false"})
@AutoConfigureMockMvc
@Import(IntegrationSupport.TimeConfig.class)
abstract class IntegrationSupport {
 @TestConfiguration static class TimeConfig {
 @Bean @Primary Clock testClock(){return Clock.fixed(Instant.parse("2026-10-22T04:00:00Z"),ZoneId.of("Asia/Shanghai"));}
 }
 @Autowired JdbcTemplate db; @Autowired LibraryRepository repo; @Autowired ReaderService readers; @Autowired CardService cards;
 @Autowired MockMvc mvc; @Autowired PlatformTransactionManager txManager;
 final Actor admin=new Actor(Actor.Role.SYSTEM_ADMIN,1), librarian=new Actor(Actor.Role.LIBRARIAN,1);
 final LocalDate today=LocalDate.of(2026,10,22);
 @BeforeEach void reset(){
 db.execute("DROP ALL OBJECTS");
 new ResourceDatabasePopulator(new ClassPathResource("schema.sql"),new ClassPathResource("data.sql")).execute(db.getDataSource());
 }
 long reader(String identity){return readers.register(new Requests.RegisterReader(identity,identity,"CS",ReaderType.UNDERGRADUATE,null)).id();}
 Views.CardView card(long id){return cards.issue(admin,new Requests.IssueCard(id,today.plusYears(1)));}
 long title(String name){return new TransactionTemplate(txManager).execute(s->repo.insertTitle(name,"Author",name,null,ItemType.CHINESE_BOOK));}
 long item(long title,String barcode){return new TransactionTemplate(txManager).execute(s->repo.insertItem(title,barcode,"A1",true,ItemType.CHINESE_BOOK));}
}

