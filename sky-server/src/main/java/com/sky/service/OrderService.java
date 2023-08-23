package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 历史订单查询
     * @return
     */
    PageResult pageQueryHistory(int page, int pageSize, Integer status);

    /**
     * 订单详情查询
     * @param id
     * @return
     */
    OrderVO details(Long id);

    /**
     * 用户取消订单
     * @param id
     * @throws Exception
     */
    void cancel(Long id) throws Exception;

    /**
     * 再来一单
     * @param id
     */
    void repeat(Long id);

    /**
     * 订单搜索
     * @param pageQueryDTO
     * @return
     */
    PageResult serach(OrdersPageQueryDTO pageQueryDTO);

    /**
     * 统计不同状态的订单数量
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 接单
     * @param ordersDTO
     */
    void confirmOrders(OrdersConfirmDTO ordersDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejectionOrders(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 商户取消订单
     * @param ordersDTO
     */
    void cancelByAdmin(OrdersCancelDTO ordersDTO);

    /**
     * 配送订单
     * @param id
     */
    void send(Long id);

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);
}
