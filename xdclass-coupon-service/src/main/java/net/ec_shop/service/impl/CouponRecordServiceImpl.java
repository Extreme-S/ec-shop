package net.ec_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.ec_shop.config.RabbitMQConfig;
import net.ec_shop.enums.BizCodeEnum;
import net.ec_shop.enums.CouponStateEnum;
import net.ec_shop.enums.StockTaskStateEnum;
import net.ec_shop.exception.BizException;
import net.ec_shop.mapper.CouponTaskMapper;
import net.ec_shop.model.CouponRecordMessage;
import net.ec_shop.model.CouponTaskDO;
import net.ec_shop.request.LockCouponRecordRequest;
import net.ec_shop.service.CouponRecordService;
import net.ec_shop.interceptor.LoginInterceptor;
import net.ec_shop.mapper.CouponRecordMapper;
import net.ec_shop.model.CouponRecordDO;
import net.ec_shop.model.LoginUser;
import net.ec_shop.service.CouponRecordService;
import net.ec_shop.util.JsonData;
import net.ec_shop.vo.CouponRecordVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class CouponRecordServiceImpl implements CouponRecordService {

    @Autowired
    private CouponRecordMapper couponRecordMapper;

    @Autowired
    private CouponTaskMapper couponTaskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;


    @Override
    public Map<String, Object> page(int page, int size) {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        //封装分页信息
        Page<CouponRecordDO> pageInfo = new Page<>(page, size);

        IPage<CouponRecordDO> recordDOIPage = couponRecordMapper.selectPage(pageInfo, new QueryWrapper<CouponRecordDO>()
                .eq("user_id", loginUser.getId())
                .orderByDesc("create_time"));

        Map<String, Object> pageMap = new HashMap<>(3);

        pageMap.put("total_record", recordDOIPage.getTotal());
        pageMap.put("total_page", recordDOIPage.getPages());
        pageMap.put("current_data", recordDOIPage.getRecords().stream().map(obj -> beanProcess(obj)).collect(Collectors.toList()));

        return pageMap;
    }

    @Override
    public CouponRecordVO findById(long recordId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        CouponRecordDO couponRecordDO = couponRecordMapper.selectOne(new QueryWrapper<CouponRecordDO>()
                .eq("id", recordId) //防止水平越权攻击
                .eq("user_id", loginUser.getId()));

        if (couponRecordDO == null) {
            return null;
        }
        return beanProcess(couponRecordDO);
    }

    /**
     * 锁定优惠券
     * 1）锁定优惠券记录
     * 2）task表插入记录
     * 3）发送延迟消息
     *
     * @param recordRequest
     * @return
     */
    @Override
    public JsonData lockCouponRecords(LockCouponRecordRequest recordRequest) {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        String orderOutTradeNo = recordRequest.getOrderOutTradeNo();
        List<Long> lockCouponRecordIds = recordRequest.getLockCouponRecordIds();

        int updateRows = couponRecordMapper.lockUseStateBatch(loginUser.getId(), CouponStateEnum.USED.name(), lockCouponRecordIds);

        List<CouponTaskDO> couponTaskDOList = lockCouponRecordIds.stream().map(obj -> {
            CouponTaskDO couponTaskDO = new CouponTaskDO();
            couponTaskDO.setCreateTime(new Date());
            couponTaskDO.setOutTradeNo(orderOutTradeNo);
            couponTaskDO.setCouponRecordId(obj);
            couponTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
            return couponTaskDO;
        }).collect(Collectors.toList());

        int insertRows = couponTaskMapper.insertBatch(couponTaskDOList);

        log.info("优惠券记录锁定updateRows={}", updateRows);
        log.info("新增优惠券记录task insertRows={}", insertRows);

        if (lockCouponRecordIds.size() == insertRows && insertRows == updateRows) {
            //发送延迟消息

            for (CouponTaskDO couponTaskDO : couponTaskDOList) {
                CouponRecordMessage couponRecordMessage = new CouponRecordMessage();
                couponRecordMessage.setOutTradeNo(orderOutTradeNo);
                couponRecordMessage.setTaskId(couponTaskDO.getId());

                rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getCouponReleaseDelayRoutingKey(), couponRecordMessage);
                log.info("优惠券锁定消息发送成功:{}", couponRecordMessage.toString());
            }
            return JsonData.buildSuccess();
        } else {
            throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
        }
    }

    private CouponRecordVO beanProcess(CouponRecordDO couponRecordDO) {
        CouponRecordVO couponRecordVO = new CouponRecordVO();
        BeanUtils.copyProperties(couponRecordDO, couponRecordVO);
        return couponRecordVO;
    }
}
