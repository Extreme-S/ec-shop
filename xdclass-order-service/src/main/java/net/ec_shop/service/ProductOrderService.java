package net.ec_shop.service;

import net.ec_shop.model.OrderMessage;
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

    /**
     * 查询订单状态
     *
     * @param outTradeNo
     * @return
     */
    String queryProductOrderState(String outTradeNo);

    /**
     * 队列监听，定时关单
     *
     * @param orderMessage
     * @return
     */
    boolean closeProductOrder(OrderMessage orderMessage);
}
