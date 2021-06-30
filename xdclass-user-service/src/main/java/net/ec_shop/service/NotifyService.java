package net.ec_shop.service;

import net.ec_shop.enums.SendCodeEnum;
import net.ec_shop.util.JsonData;
public interface NotifyService {

    JsonData sendCode(SendCodeEnum sendCodeEnum, String to );

}
