package net.ec_shop.service;


import net.ec_shop.model.CouponRecordMessage;
import net.ec_shop.request.LockCouponRecordRequest;
import net.ec_shop.util.JsonData;
import net.ec_shop.vo.CouponRecordVO;

import java.util.Map;

public interface CouponRecordService {

    /**
     * 分页查询领劵记录
     *
     * @param page
     * @param size
     * @return
     */
    Map<String, Object> page(int page, int size);

    /**
     * 根据id查询详情
     *
     * @param recordId
     * @return
     */
    CouponRecordVO findById(long recordId);

    /**
     * 锁定优惠券
     *
     * @param recordRequest
     * @return
     */
    JsonData lockCouponRecords(LockCouponRecordRequest recordRequest);

    /**
     * 释放优惠券记录
     *
     * @param recordMessage
     * @return
     */
    boolean releaseCouponRecord(CouponRecordMessage recordMessage);
}
