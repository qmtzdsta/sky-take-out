package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import com.sun.org.apache.regexp.internal.RE;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关的接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 保存菜品
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation("保存菜品")
    public Result saveWithFlavor(@RequestBody DishDTO dto){
        log.info("保存菜品，{}",dto);
        dishService.saveWithFlavor(dto);
        String key = "dish_" + dto.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 根据id查找菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> findById(@PathVariable Long id ){
        log.info("根据id查询菜品,{}",id);
        DishVO dishVO = dishService.findById(id);
        return Result.success(dishVO);
    }

    /**
     * 根据分类id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> findByCategoryId(Long categoryId){
        log.info("根据分类id查询菜品,{}",categoryId);
        List<Dish> dishes = dishService.findByCategoryId(categoryId);
        return Result.success(dishes);
    }

    /**
     * 菜品的分页查询
     * @param dto
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品的分页查询")
    public Result<PageResult> page(DishPageQueryDTO dto){
        log.info("菜品的分页查询,参数{}",dto);
        PageResult page = dishService.page(dto);
        return Result.success(page);
    }

    /**
     * 菜品的起售和停售
     * @param status
     * @param id
     * @return
     */

    @PostMapping("/status/{status}")
    @ApiOperation("菜品的起售和停售")
    public Result dishStartAndStop(@PathVariable Integer status, Long id){
        log.info("菜品的起售和停售,参数{}，{}",status,id);
        dishService.dishStartAndStop(id,status);
        cleanCache("dish_*");
        return Result.success();
    }


    /**
     * 菜品的删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品的删除")
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("菜品的删除，参数{}",ids);
        dishService.deleteBatch(ids);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 修改菜品
     * @param dishVO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishVO dishVO){
        log.info("修改菜品，参数{}",dishVO);
        dishService.update(dishVO);
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 清除redis之中的缓存
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        Long delete = redisTemplate.delete(keys);
        log.info("redis返回的数据{}",delete);
    }
}
