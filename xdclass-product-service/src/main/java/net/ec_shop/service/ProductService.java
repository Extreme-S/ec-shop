package net.ec_shop.service;

import java.util.Map;

public interface ProductService {

    /**
     * 分页查询商品列表
     *
     * @param page
     * @param size
     * @return
     */
    Map<String, Object> page(int page, int size);
}
