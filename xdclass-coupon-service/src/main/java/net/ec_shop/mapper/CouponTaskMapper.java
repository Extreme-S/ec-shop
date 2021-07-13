package net.ec_shop.mapper;

import net.ec_shop.model.CouponTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper 接口
 *
 * @author 不爱吃鱼的猫丶
 * @since 2021-07-13
 */
public interface CouponTaskMapper extends BaseMapper<CouponTaskDO> {

    /**
     * 批量插入
     *
     * @param couponTaskDOList
     * @return
     */
    int insertBatch(@Param("couponTaskList") List<CouponTaskDO> couponTaskDOList);
}
