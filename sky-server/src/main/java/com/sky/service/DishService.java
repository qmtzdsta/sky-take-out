package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService {

    /**
     * 保存菜品和相应的口味
     * @param dto
     */
    void saveWithFlavor(DishDTO dto);

    /**
     * 根据id查找菜品
     * @param id
     * @return
     */
    DishVO findById(Long id);

    /**
     * 根据分类id查询菜品
     * @param id
     * @return
     */
    List<Dish> findByCategoryId(Long categoryId);

    /**
     * 菜品的分页查询
     * @param dto
     * @return
     */
    PageResult page(DishPageQueryDTO dto);

    void dishStartAndStop(Long id, Integer status);

    void deleteBatch(List<Long> ids);

    void update(DishVO dishVO);
}
