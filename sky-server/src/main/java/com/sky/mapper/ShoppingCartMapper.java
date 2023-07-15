package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 根据条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 修改商品数量
     * @param shoppingCart1
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void update(ShoppingCart shoppingCart1);

    void insert(ShoppingCart shoppingCart);

    void delete(ShoppingCart shoppingCart);
}
