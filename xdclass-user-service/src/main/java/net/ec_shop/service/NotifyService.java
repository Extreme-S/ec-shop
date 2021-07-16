package net.ec_shop.service;

import net.ec_shop.enums.SendCodeEnum;
import net.ec_shop.util.JsonData;

public interface NotifyService {

    /**
     * 发送邮件验证码
     *
     * @param sendCodeEnum
     * @param to
     * @return
     */
    JsonData sendCode(SendCodeEnum sendCodeEnum, String to);


    /**
     * 判断验证码是否一样
     *
     * @param sendCodeEnum
     * @param to
     * @param code
     * @return
     */
    boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code);

}
