package com.example.library;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class ApiTest extends IntegrationSupport {
 @Test void TC032_missingAndInactiveIdentity()throws Exception{
 mvc.perform(get("/api/books")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
 db.update("UPDATE librarians SET status='INACTIVE' WHERE librarian_id=1");
 mvc.perform(get("/api/books").header("X-Role","LIBRARIAN").header("X-Actor-Id","1")).andExpect(status().isForbidden());
 }
 @Test void TC026_validation()throws Exception{
 mvc.perform(post("/api/readers").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
 mvc.perform(post("/api/readers").contentType(MediaType.APPLICATION_JSON).content("{\"identityNo\":\"S\",\"name\":\"N\",\"department\":\"D\",\"readerType\":\"INVALID\"}")).andExpect(status().isBadRequest());
 mvc.perform(put("/api/admin/fine-rules/THESIS").header("X-Role","SYSTEM_ADMIN").header("X-Actor-Id",1).contentType(MediaType.APPLICATION_JSON).content("{\"dailyRate\":-1,\"maximumAmount\":100}")).andExpect(status().isBadRequest());
 }
 @Test void httpFlow()throws Exception{
 mvc.perform(post("/api/readers").contentType(MediaType.APPLICATION_JSON).content("{\"identityNo\":\"S1\",\"name\":\"Student\",\"department\":\"CS\",\"readerType\":\"UNDERGRADUATE\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
 mvc.perform(post("/api/admin/borrow-cards").header("X-Role","SYSTEM_ADMIN").header("X-Actor-Id",1).contentType(MediaType.APPLICATION_JSON).content("{\"readerId\":1,\"expiresAt\":\"2030-01-01\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.cardNo").value("CARD2026000001"));
 mvc.perform(post("/api/admin/book-titles").header("X-Role","SYSTEM_ADMIN").header("X-Actor-Id",1).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Book\",\"authors\":\"Author\",\"itemType\":\"CHINESE_BOOK\"}")).andExpect(status().isCreated());
 mvc.perform(post("/api/admin/library-items").header("X-Role","SYSTEM_ADMIN").header("X-Actor-Id",1).contentType(MediaType.APPLICATION_JSON).content("{\"titleId\":1,\"barcode\":\"B1\",\"location\":\"A\",\"borrowable\":true}")).andExpect(status().isCreated());
 mvc.perform(post("/api/circulation/borrow").header("X-Role","LIBRARIAN").header("X-Actor-Id",1).contentType(MediaType.APPLICATION_JSON).content("{\"cardNo\":\"CARD2026000001\",\"barcode\":\"B1\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.dueAt").value("2026-11-21"));
 mvc.perform(get("/api/readers/1/loans").header("X-Role","READER").header("X-Actor-Id",1)).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("BORROWED"));
 mvc.perform(post("/api/circulation/return").header("X-Role","LIBRARIAN").header("X-Actor-Id",1).contentType(MediaType.APPLICATION_JSON).content("{\"barcode\":\"B1\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.loan.status").value("RETURNED")).andExpect(jsonPath("$.fine.amount").value(0));
 }
 @Test void forbiddenAndDuplicateHttp()throws Exception{
 long r=reader("S1");
 mvc.perform(post("/api/admin/borrow-cards").header("X-Role","READER").header("X-Actor-Id",r).contentType(MediaType.APPLICATION_JSON).content("{\"readerId\":1,\"expiresAt\":\"2030-01-01\"}")).andExpect(status().isForbidden());
 mvc.perform(post("/api/readers").contentType(MediaType.APPLICATION_JSON).content("{\"identityNo\":\"S1\",\"name\":\"Student\",\"department\":\"CS\",\"readerType\":\"UNDERGRADUATE\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
 }
}

