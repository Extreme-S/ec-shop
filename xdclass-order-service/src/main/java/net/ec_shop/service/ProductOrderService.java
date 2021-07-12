package net.ec_shop.service;

import net.ec_shop.request.ConfirmOrderRequest;
import net.ec_shop.util.JsonData;


public interface ProductOrderService {

    /**
     * 创建订单
     *
     * @param orderRequest
     * @return
     */
    JsonData confirmOrder(ConfirmOrderRequest orderRequest);
}
