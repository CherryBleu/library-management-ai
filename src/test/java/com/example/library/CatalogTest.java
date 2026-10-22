package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.library.application.CatalogService;
import com.example.library.dto.Requests;
import com.example.library.domain.catalog.ItemType;
import com.example.library.domain.admin.Actor;
import static org.assertj.core.api.Assertions.*;
class CatalogTest extends IntegrationSupport {
 @Autowired CatalogService catalog;
 @Test void addAndDuplicateBarcode(){
 var title=catalog.addTitle(admin,new Requests.AddTitle("Book","Author","ISBN1",null,ItemType.CHINESE_BOOK));
 var req=new Requests.AddItem(title.id(),"B1","A1",true);
 assertThat(catalog.addItem(admin,req).status()).isEqualTo("AVAILABLE");
 assertThatThrownBy(()->catalog.addItem(admin,req)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
 }
 @Test void readerCannotManage(){long id=reader("S1");assertThatThrownBy(()->catalog.addTitle(new Actor(Actor.Role.READER,id),new Requests.AddTitle("B","A",null,null,ItemType.THESIS))).hasMessage("FORBIDDEN");}
 @Test void missingTitle(){assertThatThrownBy(()->catalog.addItem(admin,new Requests.AddItem(999,"B1","A1",true))).hasMessage("TITLE_NOT_FOUND");}
 @Test void deletionProtectsCopies(){long t=title("B");long i=item(t,"B1");assertThatThrownBy(()->catalog.removeTitle(admin,t)).hasMessage("ACTIVE_ASSOCIATION");catalog.removeItem(admin,i);catalog.removeTitle(admin,t);assertThat(repo.title(t).status()).isEqualTo("INACTIVE");}
}
