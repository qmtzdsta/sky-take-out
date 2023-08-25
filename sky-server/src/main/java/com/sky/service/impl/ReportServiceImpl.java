package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 销量统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO turnoverReport(LocalDate begin, LocalDate end) {
        List<LocalDate> localDateArrayList = new ArrayList<>();

        localDateArrayList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            localDateArrayList.add(begin);
        }

        List<Double> doubles = new ArrayList<>();
        for (LocalDate localDate : localDateArrayList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            HashMap map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double profit = orderMapper.sumBymap(map);
            doubles.add(profit == null ? 0 : profit);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(localDateArrayList,","))
                .turnoverList(StringUtils.join(doubles,","))
                .build();
    }

    /**
     * 日活统计
     * @param begin
     * @param end
     * @return
     */

    @Override
    public UserReportVO userReport(LocalDate begin, LocalDate end) {

        List<LocalDate> localDateArrayList = new ArrayList<>();

        localDateArrayList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            localDateArrayList.add(begin);
        }

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate localDate : localDateArrayList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            HashMap map = new HashMap<>();
            map.put("end",endTime);
            Integer totalUser = userMapper.countBymap(map);
            totalUserList.add(totalUser == null ?0:totalUser);
            map.put("begin",beginTime);
            Integer newUser = userMapper.countBymap(map);
            newUserList.add(newUser == null ? 0:newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(localDateArrayList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderReport(LocalDate begin, LocalDate end) {

        List<LocalDate> localDateArrayList = new ArrayList<>();

        localDateArrayList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            localDateArrayList.add(begin);
        }

        List<Integer> totalOrdersList = new ArrayList<>();
        List<Integer> validOrdersList = new ArrayList<>();
        Integer allTotalOrders = 0;
        Integer allValidOrders = 0;
        Double orderCompletionRate = null;
        for (LocalDate localDate : localDateArrayList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            int totalOrders = getOrderCount(beginTime, endTime, null);
            allTotalOrders += totalOrders;
            totalOrdersList.add(totalOrders);

            int validOrders = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            allValidOrders += validOrders;
            validOrdersList.add(validOrders);

            orderCompletionRate = 0.0;
            if (allTotalOrders != 0) {
                orderCompletionRate = allValidOrders.doubleValue() / allTotalOrders;
            }
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(localDateArrayList, ","))
                .orderCountList(StringUtils.join(totalOrdersList, ","))
                .validOrderCountList(StringUtils.join(validOrdersList, ","))
                .totalOrderCount(allTotalOrders)
                .validOrderCount(allValidOrders)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 获取指定条件的订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        HashMap map = new HashMap<>();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status", status);
        Integer count = orderMapper.countByMap(map);
        return count;
    }

    @Override
    public SalesTop10ReportVO top10Report(LocalDate begin, LocalDate end) {
//        PageHelper.startPage(0,10);

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        HashMap map = new HashMap<>();
        map.put("begin",beginTime);
        map.put("end",endTime);
        List<GoodsSalesDTO> orders = orderMapper.top10(map);

        List<String> nameList = new ArrayList<>();
        ArrayList<Integer> numberList = new ArrayList<>();
        for (GoodsSalesDTO order : orders) {
            nameList.add(order.getName());
            numberList.add(order.getNumber());
        }

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }
}
