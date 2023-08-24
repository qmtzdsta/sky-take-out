package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
//    @Scheduled(cron = "0/5 * * * * ?")
    public void processTimeout(){
        log.info("处理超时订单，{}", LocalDateTime.now());

//        找出需要处理的订单
        List<Orders> ordersList =  orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,LocalDateTime.now().plusMinutes(-15));
//        修改订单的状态，写明订单取消元婴
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时，自动取消");
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
//    @Scheduled(cron = "0/1 * * * * ?")
    public void processDeliveryOrders(){
        log.info("处理派送超时的订单，{}",LocalDateTime.now());

        List<Orders> ordersList =  orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now().plusHours(-2));
//        修改订单的状态，写明订单取消元婴
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
