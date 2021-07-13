package net.ec_shop.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.ec_shop.enums.BizCodeEnum;
import net.ec_shop.request.LockCouponRecordRequest;
import net.ec_shop.service.CouponRecordService;
import net.ec_shop.util.JsonData;
import net.ec_shop.vo.CouponRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 不爱吃鱼的猫丶
 * @since 2021-07-05
 */
@RestController
@RequestMapping("/api/coupon_record/v1")
public class CouponRecordController {

    @Autowired
    private CouponRecordService couponRecordService;


    @ApiOperation("分页查询个人优惠券")
    @GetMapping("page")
    public JsonData page(@ApiParam(value = "当前页") @RequestParam(value = "page", defaultValue = "1") int page,
                         @ApiParam(value = "每页显示多少条") @RequestParam(value = "size", defaultValue = "10") int size) {

        Map<String, Object> pageResult = couponRecordService.page(page, size);
        return JsonData.buildSuccess(pageResult);
    }

    @ApiOperation("查询优惠券记录详情")
    @GetMapping("detail/{record_id}")
    public JsonData getCouponRecordDetail(@ApiParam(value = "记录id") @PathVariable("record_id") long recordId) {

        CouponRecordVO couponRecordVO = couponRecordService.findById(recordId);
        return couponRecordVO == null ? JsonData.buildResult(BizCodeEnum.COUPON_NO_EXITS) : JsonData.buildSuccess(couponRecordVO);
    }

    @ApiOperation("rpc-锁定，优惠券记录")
    @PostMapping("lock_records")
    public JsonData lockCouponRecords(@ApiParam("锁定优惠券请求对象") @RequestBody LockCouponRecordRequest recordRequest) {
        JsonData jsonData = couponRecordService.lockCouponRecords(recordRequest);

        return jsonData;

    }

}

