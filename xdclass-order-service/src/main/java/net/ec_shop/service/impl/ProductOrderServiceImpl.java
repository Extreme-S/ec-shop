package net.ec_shop.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.ec_shop.config.RabbitMQConfig;
import net.ec_shop.enums.*;
import net.ec_shop.exception.BizException;
import net.ec_shop.feign.CouponFeignSerivce;
import net.ec_shop.feign.ProductFeignService;
import net.ec_shop.feign.UserFeignService;
import net.ec_shop.interceptor.LoginInterceptor;
import net.ec_shop.mapper.ProductOrderItemMapper;
import net.ec_shop.mapper.ProductOrderMapper;
import net.ec_shop.model.LoginUser;
import net.ec_shop.model.OrderMessage;
import net.ec_shop.model.ProductOrderDO;
import net.ec_shop.model.ProductOrderItemDO;
import net.ec_shop.request.ConfirmOrderRequest;
import net.ec_shop.request.LockCouponRecordRequest;
import net.ec_shop.request.LockProductRequest;
import net.ec_shop.request.OrderItemRequest;
import net.ec_shop.service.ProductOrderService;
import net.ec_shop.util.CommonUtil;
import net.ec_shop.util.JsonData;
import net.ec_shop.vo.CouponRecordVO;
import net.ec_shop.vo.OrderItemVO;
import net.ec_shop.vo.ProductOrderAddressVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ProductOrderServiceImpl implements ProductOrderService {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private UserFeignService userFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private CouponFeignSerivce couponFeignSerivce;

    @Autowired
    private ProductOrderItemMapper orderItemMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    /**
     * ??????????????????
     * * ????????????
     * * ???????????????-??????????????????
     * * ???????????????-??????????????????????????????
     * * ????????????
     * * ??????????????????-???????????????
     * * ????????????
     * * ???????????????
     * * ??????????????????
     * * ??????????????????
     * * ?????????????????????
     * * ??????????????????-??????????????????
     * * ??????????????????-??????????????????
     *
     * @param orderRequest
     * @return
     */
    @Override
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);
        //????????????????????????
        ProductOrderAddressVO addressVO = this.getUserAddress(orderRequest.getAddressId());
        log.info("??????????????????:{}", addressVO);
        //??????????????????????????????????????????
        List<Long> productIdList = orderRequest.getProductIdList();
        JsonData cartItemDate = productFeignService.confirmOrderCartItem(productIdList);
        List<OrderItemVO> orderItemList = cartItemDate.getData(new TypeReference<>() {
        });
        log.info("???????????????:{}", orderItemList);
        if (orderItemList == null) {
            //????????????????????????
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_CART_ITEM_NOT_EXIST);
        }

        //????????????????????????????????????
        this.checkPrice(orderItemList, orderRequest);

        //???????????????
        this.lockCouponRecords(orderRequest, orderOutTradeNo);

        //????????????
        this.lockProductStocks(orderItemList, orderOutTradeNo);

        //????????????
        ProductOrderDO productOrderDO = this.saveProductOrder(orderRequest, loginUser, orderOutTradeNo, addressVO);

        //???????????????
        this.saveProductOrderItems(orderOutTradeNo, productOrderDO.getId(), orderItemList);

        //??????????????????????????????????????? TODO
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOutTradeNo(orderOutTradeNo);
        rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getOrderCloseDelayRoutingKey(), orderMessage);

        //????????????  TODO


        return null;
    }

    /**
     * ????????????
     *
     * @param orderRequest
     * @param loginUser
     * @param orderOutTradeNo
     * @param addressVO
     */
    private ProductOrderDO saveProductOrder(ConfirmOrderRequest orderRequest, LoginUser loginUser, String orderOutTradeNo, ProductOrderAddressVO addressVO) {

        ProductOrderDO productOrderDO = new ProductOrderDO();
        productOrderDO.setUserId(loginUser.getId());
        productOrderDO.setHeadImg(loginUser.getHeadImg());
        productOrderDO.setNickname(loginUser.getName());

        productOrderDO.setOutTradeNo(orderOutTradeNo);
        productOrderDO.setCreateTime(new Date());
        productOrderDO.setDel(0);
        productOrderDO.setOrderType(ProductOrderTypeEnum.DAILY.name());

        //?????????????????????
        productOrderDO.setPayAmount(orderRequest.getRealPayAmount());

        //????????????????????????????????????
        productOrderDO.setTotalAmount(orderRequest.getTotalAmount());
        productOrderDO.setState(ProductOrderStateEnum.NEW.name());
        ProductOrderTypeEnum.valueOf(orderRequest.getPayType()).name();
        productOrderDO.setPayType(ProductOrderPayTypeEnum.valueOf(orderRequest.getPayType()).name());

        productOrderDO.setReceiverAddress(JSON.toJSONString(addressVO));

        productOrderMapper.insert(productOrderDO);

        return productOrderDO;
    }

    /**
     * ???????????????
     *
     * @param orderOutTradeNo
     * @param orderId
     * @param orderItemList
     */
    private void saveProductOrderItems(String orderOutTradeNo, Long orderId, List<OrderItemVO> orderItemList) {
        List<ProductOrderItemDO> list = orderItemList.stream().map(
                obj -> {
                    ProductOrderItemDO itemDO = new ProductOrderItemDO();
                    itemDO.setBuyNum(obj.getBuyNum());
                    itemDO.setProductId(obj.getProductId());
                    itemDO.setProductImg(obj.getProductImg());
                    itemDO.setProductName(obj.getProductTitle());
                    itemDO.setOutTradeNo(orderOutTradeNo);
                    itemDO.setCreateTime(new Date());
                    //??????
                    itemDO.setAmount(obj.getAmount());
                    //??????
                    itemDO.setTotalAmount(obj.getTotalAmount());
                    itemDO.setProductOrderId(orderId);
                    return itemDO;
                }
        ).collect(Collectors.toList());
        orderItemMapper.insertBatch(list);
    }

    /**
     * ??????????????????
     *
     * @param orderItemList
     * @param orderOutTradeNo
     */
    private void lockProductStocks(List<OrderItemVO> orderItemList, String orderOutTradeNo) {
        List<OrderItemRequest> itemRequestList = orderItemList.stream().map(obj -> {
            OrderItemRequest request = new OrderItemRequest();
            request.setBuyNum(obj.getBuyNum());
            request.setProductId(obj.getProductId());
            return request;
        }).collect(Collectors.toList());

        LockProductRequest lockProductRequest = new LockProductRequest();
        lockProductRequest.setOrderOutTradeNo(orderOutTradeNo);
        lockProductRequest.setOrderItemList(itemRequestList);

        JsonData jsonData = productFeignService.lockProductStock(lockProductRequest);
        if (jsonData.getCode() != 0) {
            log.error("???????????????????????????{}", lockProductRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
        }
    }

    /**
     * ???????????????
     *
     * @param orderRequest
     * @param orderOutTradeNo
     */
    private void lockCouponRecords(ConfirmOrderRequest orderRequest, String orderOutTradeNo) {
        List<Long> lockCouponRecordIds = new ArrayList<>();
        if (orderRequest.getCouponRecordId() > 0) {
            lockCouponRecordIds.add(orderRequest.getCouponRecordId());

            LockCouponRecordRequest lockCouponRecordRequest = new LockCouponRecordRequest();
            lockCouponRecordRequest.setOrderOutTradeNo(orderOutTradeNo);
            lockCouponRecordRequest.setLockCouponRecordIds(lockCouponRecordIds);
            //???????????????????????????
            JsonData jsonData = couponFeignSerivce.lockCouponRecords(lockCouponRecordRequest);
            if (jsonData.getCode() != 0) {
                throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
            }
        }
    }

    /**
     * ????????????
     * 1??????????????????????????????
     * 2) ???????????????(????????????????????????????????????)???????????????????????????????????? ?????? ???????????????
     *
     * @param orderItemList
     * @param orderRequest
     */
    private void checkPrice(List<OrderItemVO> orderItemList, ConfirmOrderRequest orderRequest) {
        //?????????????????????
        BigDecimal realPayAmount = new BigDecimal("0");
        if (orderItemList != null) {
            for (OrderItemVO orderItemVO : orderItemList) {
                BigDecimal itemRealPayAmount = orderItemVO.getTotalAmount();
                realPayAmount = realPayAmount.add(itemRealPayAmount);
            }
        }
        //??????????????????????????????????????????
        CouponRecordVO couponRecordVO = getCartCouponRecord(orderRequest.getCouponRecordId());
        //?????????????????????????????????????????????????????????
        if (couponRecordVO != null) {
            //????????????????????????
            if (realPayAmount.compareTo(couponRecordVO.getConditionPrice()) < 0) {
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
            }
            if (couponRecordVO.getPrice().compareTo(realPayAmount) > 0) {
                realPayAmount = BigDecimal.ZERO;
            } else {
                realPayAmount = realPayAmount.subtract(couponRecordVO.getPrice());
            }
        }
        if (realPayAmount.compareTo(orderRequest.getRealPayAmount()) != 0) {
            log.error("?????????????????????{}", orderRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
    }

    /**
     * ???????????????
     *
     * @param couponRecordId
     * @return
     */
    private CouponRecordVO getCartCouponRecord(Long couponRecordId) {
        if (couponRecordId == null || couponRecordId < 0) {
            return null;
        }
        JsonData couponData = couponFeignSerivce.findUserCouponRecordById(couponRecordId);
        if (couponData.getCode() != 0) {
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
        }
        if (couponData.getCode() == 0) {
            CouponRecordVO couponRecordVO = couponData.getData(new TypeReference<>() {
            });
            if (!couponAvailable(couponRecordVO)) {
                log.error("?????????????????????");
                throw new BizException(BizCodeEnum.COUPON_UNAVAILABLE);
            }
            return couponRecordVO;
        }
        return null;
    }

    /**
     * ???????????????????????????
     *
     * @param couponRecordVO
     * @return
     */
    private boolean couponAvailable(CouponRecordVO couponRecordVO) {
        if (couponRecordVO.getUseState().equalsIgnoreCase(CouponStateEnum.NEW.name())) {
            long currentTimestamp = CommonUtil.getCurrentTimestamp();
            long end = couponRecordVO.getEndTime().getTime();
            long start = couponRecordVO.getStartTime().getTime();
//            if (currentTimestamp >= start && currentTimestamp <= end) {
//                return true;
//            }
            return currentTimestamp >= start && currentTimestamp <= end;
        }
        return false;
    }

    /**
     * ????????????id????????????????????????
     *
     * @param addressId
     * @return
     */
    private ProductOrderAddressVO getUserAddress(long addressId) {
        JsonData addressData = userFeignService.detail(addressId);
        if (addressData.getCode() != 0) {
            log.error("????????????????????????,msg:{}", addressData);
            throw new BizException(BizCodeEnum.ADDRESS_NO_EXITS);
        }
        ProductOrderAddressVO addressVO = addressData.getData(new TypeReference<>() {
        });
        return addressVO;
    }


    /**
     * ??????????????????
     *
     * @param outTradeNo
     * @return
     */
    @Override
    public String queryProductOrderState(String outTradeNo) {
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>().eq("out_trade_no", outTradeNo));
        if (productOrderDO == null) {
            return "";
        } else {
            return productOrderDO.getState();
        }
    }

    /**
     * ????????????
     *
     * @param orderMessage
     * @return
     */
    @Override
    public boolean closeProductOrder(OrderMessage orderMessage) {
        ProductOrderDO productOrderDO = productOrderMapper
                .selectOne(new QueryWrapper<ProductOrderDO>().eq("out_trade_no", orderMessage.getOutTradeNo()));
        //???????????????
        if (productOrderDO == null) {
            log.warn("????????????????????????????????????:{}", orderMessage);
            return true;
        }
        //????????????
        if (productOrderDO.getState().equalsIgnoreCase(ProductOrderStateEnum.PAY.name())) {
            log.info("??????????????????,??????????????????:{}", orderMessage);
            return true;
        }
        //???????????????????????????????????????????????????  TODO

        String payResult = "";

        //??????????????????????????????????????????????????????
        if (StringUtils.isBlank(payResult)) {
            productOrderMapper.updateOrderPayState(productOrderDO.getOutTradeNo(), ProductOrderStateEnum.CANCEL.name(), ProductOrderStateEnum.NEW.name());
            log.info("??????????????????????????????????????????????????????:{}", orderMessage);
            return true;
        } else {
            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            log.warn("?????????????????????????????????????????????UI????????????????????????????????????????????????????????????????????????:{}", orderMessage);
            productOrderMapper.updateOrderPayState(productOrderDO.getOutTradeNo(), ProductOrderStateEnum.PAY.name(), ProductOrderStateEnum.NEW.name());
            return true;
        }
    }
}
