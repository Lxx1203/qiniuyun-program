package top.niumacoder.userservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("top.niumacoder.userservice.dao.mapper")
public class Main {
    public static void main(String[] args) {

    }
}
