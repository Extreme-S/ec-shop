package net.ec_shop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
@MapperScan("net.ec_shop.mapper")
public class CouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponApplication.class, args);
    }

}
