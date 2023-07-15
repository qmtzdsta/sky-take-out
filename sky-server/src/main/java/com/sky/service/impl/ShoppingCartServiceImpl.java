package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
//        如果此时购物车里面的菜品只有一份了，就删除，大于等于二减一
        ShoppingCart shoppingCart = getShoppingCart();
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());
//        先获取再判断
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        ShoppingCart shoppingCart1 = list.get(0);
        if(shoppingCart1.getNumber()>1){
//            大于一份
            shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
            shoppingCartMapper.update(shoppingCart1);
        } else {
//            只有一份
            shoppingCartMapper.delete(shoppingCart);
        }



    }

    @Override
    public void clean() {
        ShoppingCart shoppingCart = getShoppingCart();
        shoppingCartMapper.delete(shoppingCart);
    }

    @Override
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = getShoppingCart();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;

    }

    private ShoppingCart getShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCart;
    }

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
//        判断是否已经存在，如果已经存在，就只加一
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list!=null && list.size()>0) {
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCartMapper.update(shoppingCart1);

        }else {
//        不存在插入
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
//              传递的参数为菜品
                Dish byId = dishMapper.findById(dishId);
                shoppingCart.setName(byId.getName());
                shoppingCart.setImage(byId.getImage());
                shoppingCart.setAmount(byId.getPrice());

            }else{
//              传递的参数为套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal byId = setmealMapper.findById(setmealId);
                shoppingCart.setName(byId.getName());
                shoppingCart.setImage(byId.getImage());
                shoppingCart.setAmount(byId.getPrice());

            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }


    }
}
