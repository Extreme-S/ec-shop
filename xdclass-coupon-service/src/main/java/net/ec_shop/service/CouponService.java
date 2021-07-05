package net.ec_shop.service;

import net.ec_shop.enums.CouponCategoryEnum;
import net.ec_shop.util.JsonData;

import java.util.Map;

public interface CouponService {

    /**
     * 分页查询优惠券
     *
     * @param page
     * @param size
     * @return
     */
    Map<String, Object> pageCouponActivity(int page, int size);

    /**
     * 领取优惠券接口
     *
     * @param couponId
     * @param category
     * @return
     */
    JsonData addCoupon(long couponId, CouponCategoryEnum category);
}
