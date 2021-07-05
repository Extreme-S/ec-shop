package net.ec_shop.mapper;

import net.ec_shop.model.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author 不爱吃鱼的猫丶
 * @since 2021-07-05
 */
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 扣减存储
     *
     * @param couponId
     * @return
     */
    int reduceStock(@Param("couponId") long couponId);

}
