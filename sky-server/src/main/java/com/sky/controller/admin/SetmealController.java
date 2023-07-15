package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.Cacheable;

import java.util.List;

@RestController("adminSetmealController")
@Slf4j
@Api(tags = "套餐相关接口")
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;


    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> findById(@PathVariable Long id){
        log.info("根据id查询套餐",id);
        SetmealVO setmealVO = setmealService.findById(id);
        return Result.success(setmealVO);
    }

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmeal",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询,{}",setmealPageQueryDTO);
        PageResult page = setmealService.page(setmealPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 套餐的停售和起售
     * @param status
     * @param id
     * @return
     */

    @PostMapping("/status/{status}")
    @ApiOperation("套餐的停售和起售")
    @CacheEvict(cacheNames = "setmeal",allEntries = true)
    public Result setmealStartAndStop(@PathVariable Integer status,Long id){
        log.info("套餐{}",status == 1 ? "起售":"停售");
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);
        setmealService.setmealStartAndStop(setmeal);
        return Result.success();
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmeal",allEntries = true)
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐,{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmeal",allEntries = true)
    public Result deleteSetmealBatch(@RequestParam List<Long> ids){
        log.info("批量删除套餐，{}",ids);
        setmealService.deleteSetmealBatch(ids);
        return Result.success();
    }

}
