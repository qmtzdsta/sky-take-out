package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关的接口")
public class DishController {

    @Autowired
    private DishService dishService;

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
    public Result<Dish> findByCategoryId(Long id){
        log.info("根据分类id查询菜品,{}",id);
        Dish dish = dishService.findByCategoryId(id);
        return Result.success(dish);
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
        return Result.success();
    }


    /**
     * 菜品的删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品的删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品的删除，参数{}",ids);
        dishService.deleteBatch(ids);
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
        return Result.success();
    }
}
