package net.ec_shop.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import net.ec_shop.enums.BizCodeEnum;
import net.ec_shop.enums.ClientType;
import net.ec_shop.enums.ProductOrderPayTypeEnum;
import net.ec_shop.request.ConfirmOrderRequest;
import net.ec_shop.service.ProductOrderService;
import net.ec_shop.util.JsonData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 不爱吃鱼的猫丶
 * @since 2021-07-12
 */
@Api("订单模块")
@RestController
@RequestMapping("/api/order/v1")
@Slf4j
public class ProductOrderController {

    @Autowired
    private ProductOrderService orderService;

    /**
     * 提交订单
     *
     * @param orderRequest
     * @param response
     */
    @ApiOperation("提交订单")
    @PostMapping("confirm")
    public void confirmOrder(@ApiParam("订单对象") @RequestBody ConfirmOrderRequest orderRequest, HttpServletResponse response) {
        JsonData jsonData = orderService.confirmOrder(orderRequest);
        if (jsonData.getCode() == 0) {
            //获取端类型
            String client = orderRequest.getClientType();
            //获取支付方式
            String payType = orderRequest.getPayType();
            //如果是支付宝网页支付，都是跳转网页，APP除外
            if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.ALIPAY.name())) {
                log.info("创建支付宝订单成功:{}", orderRequest.toString());
                if (client.equalsIgnoreCase(ClientType.H5.name())) {
                    //网页端支付
                    writeData(response, jsonData);
                } else if (client.equalsIgnoreCase(ClientType.APP.name())) {
                    //APP SDK支付  TODO
                }
            } else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.WECHAT.name())) {
                //微信支付 TODO
            }
        } else {

            log.error("创建订单失败{}", jsonData.toString());

        }
    }

    /**
     * 查询订单状态
     * 此接口没有登录拦截，可以增加一个秘钥进行rpc通信
     *
     * @param outTradeNo
     * @return
     */
    @ApiOperation("查询订单状态")
    @GetMapping("query_state")
    public JsonData queryProductOrderState(@ApiParam("订单号") @RequestParam("out_trade_no") String outTradeNo) {
        String state = orderService.queryProductOrderState(outTradeNo);
        return StringUtils.isBlank(state) ? JsonData.buildResult(BizCodeEnum.ORDER_CONFIRM_NOT_EXIST) : JsonData.buildSuccess(state);
    }

    private void writeData(HttpServletResponse response, JsonData jsonData) {
        try {
            response.setContentType("text/html;charset=UTF8");
            response.getWriter().write(jsonData.getData().toString());
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            log.error("写出Html异常：{}", e);
        }
    }
}

