package net.ec_shop.service;

import net.ec_shop.request.CartItemRequest;
import net.ec_shop.vo.CartVO;

public interface CartService {

    /**
     * 添加是商品到购物车
     *
     * @param cartItemRequest
     */
    void addToCart(CartItemRequest cartItemRequest);

    /**
     * 清空购物车
     */
    void clear();

    /**
     * 查看我的购物车
     *
     * @return
     */
    CartVO getMyCart();

}
