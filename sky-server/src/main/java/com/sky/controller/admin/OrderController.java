package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Slf4j
@RequestMapping("/admin/order")
@Api(tags = "管理端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("搜索订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO pageQueryDTO){
        log.info("搜索订单:{}",pageQueryDTO);
        PageResult pageResult = orderService.serach(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 统计不同状态的订单数量
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("统计不同状态的订单数量")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id){
        log.info("查询订单详情,{}",id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param ordersDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirmOrders(@RequestBody OrdersConfirmDTO ordersDTO){
        log.info("接单,{}",ordersDTO);
        orderService.confirmOrders(ordersDTO);
        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒单,{}",ordersRejectionDTO);
        orderService.rejectionOrders(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersDTO){
        log.info("取消订单,{}",ordersDTO);
        orderService.cancelByAdmin(ordersDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result send(@PathVariable("id") Long id){
        log.info("派送订单,{}",id);
        orderService.send(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id){
        log.info("完成订单,{}",id);
        orderService.complete(id);
        return Result.success();
    }
}
