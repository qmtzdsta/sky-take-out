package com.sky.mapper;

import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param dishId
     * @return
     */
    @Select("select setmeal_id from setmeal_dish where dish_id = #{dishId};")
    List<Long> getSetmealDishIdByDishId(Long dishId);

    /**
     * 查询套餐之中的菜品信息
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId} ")
    List<SetmealDish> findBySetmealId(Long setmealId);

    /**
     * 插入对应的套餐关系表
     * @param setmealDishes
     */
    void insertBatch(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    void update(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    @Delete(" delete from setmeal_dish where setmeal_id = #{setmealId} ")
    void deleteBySetmealId(Long setmealId);
}
