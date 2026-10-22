package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.library.domain.repository.LibraryRepository;
import com.example.library.domain.reader.ReaderType;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:repository;DB_CLOSE_DELAY=-1")
@Transactional
class RepositoryTest {
 @Autowired LibraryRepository repo;
 @Autowired JdbcTemplate db;
 @Test void foreignKeysAndUniqueIdentities(){
 repo.lock();
 long id=repo.insertReader("unique","A","CS",ReaderType.UNDERGRADUATE,null);
 assertThat(repo.reader(id).name()).isEqualTo("A");
 assertThatThrownBy(()->repo.insertReader("unique","B","CS",ReaderType.TEACHER,null)).isInstanceOf(DataIntegrityViolationException.class);
 assertThatThrownBy(()->db.update("INSERT INTO library_items(title_id,barcode,item_type,language,loan_eligibility,location) VALUES(999,'bad','THESIS','ZH','BORROWABLE','A')")).isInstanceOf(DataIntegrityViolationException.class);
 }
}
