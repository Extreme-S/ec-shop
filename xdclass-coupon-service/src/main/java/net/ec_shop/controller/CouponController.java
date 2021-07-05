package net.ec_shop.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.ec_shop.enums.CouponCategoryEnum;
import net.ec_shop.service.CouponService;
import net.ec_shop.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 二当家小D
 * @since 2021-02-07
 */
@Api("优惠券模块")
@RestController
@RequestMapping("/api/coupon/v1")
public class CouponController {


    @Autowired
    private CouponService couponService;


    @ApiOperation("分页查询优惠券")
    @GetMapping("page_coupon")
    public JsonData pageCouponList(
            @ApiParam(value = "当前页") @RequestParam(value = "page", defaultValue = "1") int page,
            @ApiParam(value = "每页显示多少条") @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        Map<String, Object> pageMap = couponService.pageCouponActivity(page, size);
        return JsonData.buildSuccess(pageMap);
    }

    /**
     * 领取优惠券
     *
     * @param couponId
     * @return
     */
    @ApiOperation("领取优惠券")
    @GetMapping("/add/promotion/{coupon_id}")
    public JsonData addPromotionCoupon(
            @ApiParam(value = "优惠券id", required = true) @PathVariable("coupon_id") long couponId) {
        JsonData jsonData = couponService.addCoupon(couponId, CouponCategoryEnum.PROMOTION);

        return jsonData;
    }

}

