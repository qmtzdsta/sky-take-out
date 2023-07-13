package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    SetmealVO findById(Long id);

    void saveWithDish(SetmealDTO setmealDTO);


    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    void setmealStartAndStop(Setmeal setmeal);

    void update(SetmealDTO setmealDTO);

    void deleteSetmealBatch(List<Long> ids);
}
