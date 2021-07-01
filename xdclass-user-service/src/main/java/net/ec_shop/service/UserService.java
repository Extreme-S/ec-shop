package net.ec_shop.service;

import net.ec_shop.request.UserRegisterRequest;
import net.ec_shop.util.JsonData;

public interface UserService {

    /**
     * 用户注册
     *
     * @param registerRequest
     * @return
     */
    JsonData register(UserRegisterRequest registerRequest);

}
