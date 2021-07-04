package net.ec_shop.service;

import net.ec_shop.model.AddressDO;
import net.ec_shop.request.AddressAddReqeust;
import net.ec_shop.vo.AddressVO;

import java.util.List;


public interface AddressService {

    /**
     * 查找指定地址详情
     *
     * @param id
     * @return
     */
    AddressDO detail(Long id);

    /**
     * 新增收货地址
     *
     * @param addressAddReqeust
     */
    void add(AddressAddReqeust addressAddReqeust);

    /**
     * 根据id删除地址
     *
     * @param addressId
     * @return
     */
    int del(int addressId);

    /**
     * 查找用户全部收货地址
     *
     * @return
     */
    List<AddressVO> listUserAllAddress();
}
