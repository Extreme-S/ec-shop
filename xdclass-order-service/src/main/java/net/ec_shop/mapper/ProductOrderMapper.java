package net.ec_shop.mapper;

import net.ec_shop.model.ProductOrderDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author 不爱吃鱼的猫丶
 * @since 2021-07-12
 */
public interface ProductOrderMapper extends BaseMapper<ProductOrderDO> {

    /**
     * 更新订单状态
     *
     * @param outTradeNo
     * @param newState
     * @param oldState
     */
    void updateOrderPayState(@Param("outTradeNo") String outTradeNo, @Param("newState") String newState, @Param("oldState") String oldState);

}
