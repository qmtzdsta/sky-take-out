package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void insertBatch( List<OrderDetail> details );

    @Select("select * from order_detail where order_id = #{orderId};")
    List<OrderDetail> findByOrderId(Long orderId);
}
