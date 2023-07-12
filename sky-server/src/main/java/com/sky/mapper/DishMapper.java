package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.Autofill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Autofill(OperationType.INSERT)
    void insert(Dish dish);

    @Select("select * from dish where id = #{id}")
    Dish findById(Long id);

    @Select("select * from dish where category_id = #{id};")
    Dish findByCategoryId(Long id);

    Page<DishVO> pageQuery(DishPageQueryDTO dto);

    void update(Dish dish);

    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);
}
