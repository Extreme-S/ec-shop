package net.ec_shop.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.ec_shop.request.ConfirmOrderRequest;
import net.ec_shop.service.ProductOrderService;
import net.ec_shop.util.JsonData;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ProductOrderServiceImpl implements ProductOrderService {


    @Override
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {
        return null;
    }
}
