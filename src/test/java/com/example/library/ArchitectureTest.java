package com.example.library;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;
class ArchitectureTest {
 @Test void TC035_dependencyDirections()throws Exception{
 var base=Path.of("src/main/java/com/example/library");
 try(var files=Files.walk(base.resolve("presentation"))){for(var p:files.filter(x->x.toString().endsWith(".java")).toList()){
 var s=Files.readString(p);assertThat(s).doesNotContain("domain.repository","infrastructure.","JdbcTemplate","@Transactional");
 }}
 try(var files=Files.walk(base.resolve("domain"))){for(var p:files.filter(x->x.toString().endsWith(".java")).toList()){
 var s=Files.readString(p);assertThat(s).doesNotContain("org.springframework","jakarta.persistence","com.example.library.application","com.example.library.dto");
 }}
 }
}

