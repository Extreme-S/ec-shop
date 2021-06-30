package net.xdclass.biz;

import lombok.extern.slf4j.Slf4j;
import net.ec_shop.UserApplication;
import net.ec_shop.service.MailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
@Slf4j
public class MailTest {

    @Autowired
    private MailService mailService;


    @Test
    public void testSendMail() {

        mailService.sendMail("1716224950@qq.com", "微服务项目发送邮件功能测试", "哈哈，这个就是内容，https://github.com/Extreme-S");
    }

}
