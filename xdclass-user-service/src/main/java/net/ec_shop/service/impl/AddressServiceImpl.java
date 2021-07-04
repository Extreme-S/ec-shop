package net.ec_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;
import net.ec_shop.enums.AddressStatusEnum;
import net.ec_shop.interceptor.LoginInterceptor;
import net.ec_shop.mapper.AddressMapper;
import net.ec_shop.model.AddressDO;
import net.ec_shop.model.LoginUser;
import net.ec_shop.request.AddressAddReqeust;
import net.ec_shop.service.AddressService;
import net.ec_shop.vo.AddressVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public AddressDO detail(Long id) {

        AddressDO addressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>().eq("id", id));

        return addressDO;
    }

    /**
     * 新增收货地址
     *
     * @param addressAddReqeust
     */
    @Override
    public void add(AddressAddReqeust addressAddReqeust) {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        AddressDO addressDO = new AddressDO();
        addressDO.setCreateTime(new Date());
        addressDO.setUserId(loginUser.getId());

        BeanUtils.copyProperties(addressAddReqeust, addressDO);


        //是否有默认收货地址
        if (addressDO.getDefaultStatus() == AddressStatusEnum.DEFAULT_STATUS.getStatus()) {
            //查找数据库是否有默认地址
            AddressDO defaultAddressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>()
                    .eq("user_id", loginUser.getId())
                    .eq("default_status", AddressStatusEnum.DEFAULT_STATUS.getStatus()));

            if (defaultAddressDO != null) {
                //修改为非默认收货地址
                defaultAddressDO.setDefaultStatus(AddressStatusEnum.COMMON_STATUS.getStatus());
                addressMapper.update(defaultAddressDO, new QueryWrapper<AddressDO>().eq("id", defaultAddressDO.getId()));
            }
        }

        int rows = addressMapper.insert(addressDO);

        log.info("新增收货地址:rows={},data={}", rows, addressDO);
    }

    /**
     * 根据id删除地址
     *
     * @param addressId
     * @return
     */
    @Override
    public int del(int addressId) {
        int rows = addressMapper.delete(new QueryWrapper<AddressDO>().eq("id", addressId));
        return rows;
    }


    /**
     * 查找用全部收货地址
     *
     * @return
     */
    @Override
    public List<AddressVO> listUserAllAddress() {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        List<AddressDO> list = addressMapper.selectList(new QueryWrapper<AddressDO>().eq("user_id", loginUser.getId()));

        List<AddressVO> addressVOList = list.stream().map(obj -> {
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(obj, addressVO);
            return addressVO;
        }).collect(Collectors.toList());

        return addressVOList;

    }
}
