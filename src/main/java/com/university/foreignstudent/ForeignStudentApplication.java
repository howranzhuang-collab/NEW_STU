package com.university.foreignstudent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.university.foreignstudent.mapper")
public class ForeignStudentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForeignStudentApplication.class, args);
    }
}

