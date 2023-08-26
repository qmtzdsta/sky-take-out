package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;
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

    @Override
    public void export(HttpServletResponse response) {

        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook excel = null;
        try {
            excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet1 = excel.getSheet("Sheet1");

//        设置时间
            sheet1.getRow(1).getCell(1).setCellValue("时间："+begin+"到"+end);
//        概览数据
            XSSFRow row3 = sheet1.getRow(3);
            row3.getCell(2).setCellValue(businessData.getTurnover());
            row3.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row3.getCell(6).setCellValue(businessData.getNewUsers());

            XSSFRow row4 = sheet1.getRow(4);
            row4.getCell(2).setCellValue(businessData.getValidOrderCount());
            row4.getCell(4).setCellValue(businessData.getUnitPrice());

//        明细数据

            for (int i = 0; i <30 ; i++) {
                LocalDate localDate = end.minusDays(i);
                BusinessDataVO everyBusinessData =
                        workspaceService.getBusinessData(LocalDateTime.of(localDate, LocalTime.MIN), LocalDateTime.of(localDate, LocalTime.MAX));
                XSSFRow row = sheet1.getRow(i + 7);
                row.getCell(1).setCellValue(localDate.toString());
                row.getCell(2).setCellValue(everyBusinessData.getTurnover());
                row.getCell(3).setCellValue(everyBusinessData.getValidOrderCount());
                row.getCell(4).setCellValue(everyBusinessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(everyBusinessData.getUnitPrice());
                row.getCell(6).setCellValue(everyBusinessData.getNewUsers());
            }

            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            excel.close();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
