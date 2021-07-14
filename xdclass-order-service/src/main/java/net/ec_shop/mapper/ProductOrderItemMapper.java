package net.ec_shop.mapper;

import net.ec_shop.model.ProductOrderItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper 接口
 *
 * @author 不爱吃鱼的猫丶
 * @since 2021-07-12
 */
public interface ProductOrderItemMapper extends BaseMapper<ProductOrderItemDO> {
    /**
     * 批量插入
     *
     * @param list
     */
    void insertBatch(@Param("orderItemList") List<ProductOrderItemDO> list);
}
