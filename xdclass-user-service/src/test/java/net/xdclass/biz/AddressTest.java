package net.xdclass.biz;

import lombok.extern.slf4j.Slf4j;
import net.ec_shop.UserApplication;
import net.ec_shop.model.AddressDO;
import net.ec_shop.service.AddressService;
import net.ec_shop.vo.AddressVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
@Slf4j
public class AddressTest {

    @Autowired
    private AddressService addressService;

    @Test
    public void testAddressDetail() {

        AddressVO addressVO = addressService.detail(1L);
        log.info(addressVO.toString());

        Assert.assertNotNull(addressVO);

    }


}
