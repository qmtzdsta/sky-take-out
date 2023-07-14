package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.Autofill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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
     * 根据id查找菜品和对应的口味
     * @param id
     * @return
     */
    public DishVO findById(Long id){
        Dish dish = dishMapper.findById(id);
        List<DishFlavor> dishFlavors = dishFlavorMapper.findByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> findByCategoryId(Long categoryId){
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        List<Dish> dishes = dishMapper.list(dish);
        return dishes;
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

    public void dishStartAndStop(Long id, Integer status){
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);

        dishMapper.update(dish);
    }

    @Transactional
    public void deleteBatch(List<Long> ids){
//        是否为禁售状态
        for (Long id : ids) {
            Dish byId = dishMapper.findById(id);
            int status = byId.getStatus();
            if (status == StatusConstant.ENABLE) {
//                菜品属于起售状态无法删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
//        是否有关联的套餐
        for (Long id : ids) {
            List<Long> setmealDishIdByDishId = setmealDishMapper.getSetmealDishIdByDishId(id);
            if (setmealDishIdByDishId!=null&&setmealDishIdByDishId.size()>0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }
        for (Long id : ids) {
//        删除菜品
            dishMapper.deleteById(id);
//        关联菜品的口味的删除
            dishFlavorMapper.deleteBydishId(id);
        }

    }

    /**
     * 修改菜品信息
     * @param dishVO
     */

    public void update(DishVO dishVO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishVO,dish);
//        修改菜品
        dishMapper.update(dish);
//        修改风味 先删除后插入
        dishFlavorMapper.deleteBydishId(dish.getId());
        List<DishFlavor> flavors = dishVO.getFlavors();
        flavors.forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
        if (flavors.size()>0 && flavors!=null) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.findByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
