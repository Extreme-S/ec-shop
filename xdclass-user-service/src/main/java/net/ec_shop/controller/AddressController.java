package net.ec_shop.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.ec_shop.enums.BizCodeEnum;
import net.ec_shop.model.AddressDO;
import net.ec_shop.request.AddressAddReqeust;
import net.ec_shop.service.AddressService;
import net.ec_shop.util.JsonData;
import net.ec_shop.vo.AddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 电商-公司收发货地址表 前端控制器
 * </p>
 *
 * @author 不爱吃鱼的猫丶
 * @since 2021-06-26
 */
@Api(tags = "收货地址模块")
@RestController
@RequestMapping("/api/address/v1/")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @ApiOperation("新增收货地址")
    @PostMapping("add")
    public JsonData add(@ApiParam("地址对象") @RequestBody AddressAddReqeust addressAddReqeust) {

        addressService.add(addressAddReqeust);

        return JsonData.buildSuccess();
    }

    @ApiOperation("根据id查找地址详情")
    @GetMapping("/find/{address_id}")
    public JsonData detail(
            @ApiParam(value = "地址id", required = true) @PathVariable("address_id")
                    long addressId) {

        AddressVO addressVO = addressService.detail(addressId);
//        int i = 1 / 0;
//        if (addressId == 1) {
//            throw new BizException(-1, "测试自定义异常");
//        }
        return addressVO == null ? JsonData.buildResult(BizCodeEnum.ADDRESS_NO_EXITS) : JsonData.buildSuccess(addressVO);
    }

    /**
     * 删除指定收货地址
     *
     * @param addressId
     * @return
     */
    @ApiOperation("删除指定收货地址")
    @DeleteMapping("/del/{address_id}")
    public JsonData del(
            @ApiParam(value = "地址id", required = true)
            @PathVariable("address_id") int addressId) {

        int rows = addressService.del(addressId);

        return rows == 1 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.ADDRESS_DEL_FAIL);
    }


    /**
     * 查询用户的全部收货地址
     *
     * @return
     */
    @ApiOperation("查询用户的全部收货地址")
    @GetMapping("/list")
    public JsonData findUserAllAddress() {

        List<AddressVO> list = addressService.listUserAllAddress();

        return JsonData.buildSuccess(list);
    }

}
