package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 保存菜品和相应的口味
     * @param dto
     */
    public void saveWithFlavor(DishDTO dto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto,dish);
//        菜品保存
        dishMapper.insert(dish);
//        获取id
        Long id = dish.getId();
//        菜品风味的保存
        List<DishFlavor> flavors = dto.getFlavors();
        flavors.forEach(dishFlavor -> dishFlavor.setDishId(id));
        if (flavors.size()>0 && flavors!=null) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据id查找菜品
     * @param id
     * @return
     */
    public Dish findById(Long id){
        Dish dish = dishMapper.findById(id);
        return dish;
    }

    /**
     * 根据分类id查询菜品
     * @param id
     * @return
     */
    public Dish findByCategoryId(Long id){
        Dish dish = dishMapper.findByCategoryId(id);
        return dish;
    }

    /**
     * 菜品的分页查询
     * @param dto
     * @return
     */
    public PageResult page(DishPageQueryDTO dto){
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dto);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
