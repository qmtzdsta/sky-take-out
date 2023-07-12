package com.sky.mapper;

import com.sky.annotation.Autofill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    @Delete("delete from dish_flavor where dish_id =#{id} ")
    void deleteBydishId(Long id);

    @Select("select * from dish_flavor where dish_id = #{dishId} ;")
    List<DishFlavor> findByDishId(Long dishId);

}
