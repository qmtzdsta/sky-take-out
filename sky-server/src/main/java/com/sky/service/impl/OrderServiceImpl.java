package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.AddressUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private OrderService orderService;
    @Value("${sky.shop.address}")
    private String address;
    @Autowired
    private AddressUtil addressUtil;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
//        1 业务异常检查，地址簿为空，购物车为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook.getId()==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        checkoutoutofrange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list==null&& list.size()==0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
//        2 向订单表里面插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setUserId(BaseContext.getCurrentId());
        orderMapper.insert(orders);
//        3 向订单详细表里面插入n条数据
        List<OrderDetail> details = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            details.add(orderDetail);
        }
//        log.info("插入的订单业务表的n条信息{}",details);
        orderDetailMapper.insertBatch(details);
//        4 清空购物车
        shoppingCartMapper.delete(shoppingCart);
//        5 封装vo对象
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setOrderTime(LocalDateTime.now());
        orderSubmitVO.setId(orders.getId());
        orderSubmitVO.setOrderAmount(orders.getAmount());
        orderSubmitVO.setOrderNumber(orders.getNumber());
        return orderSubmitVO;
    }


//  判断是否距离大于5000m
    private void checkoutoutofrange(String userAddress) {
        String origin = addressUtil.geocoding(address);
        String target = addressUtil.geocoding(userAddress);
        addressUtil.driving(origin,target);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.findById(userId);

       /* //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
*/
        OrderPaymentVO vo = new OrderPaymentVO();
        vo.setNonceStr(RandomStringUtils.random(5));
        vo.setTimeStamp(String.valueOf(System.currentTimeMillis()));

//      修改底层数据库直接代替
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return vo;
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repeat(Long id) {
        Long userId = BaseContext.getCurrentId();

        List<OrderDetail> orderDetailList = orderDetailMapper.findByOrderId(id);

        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x->{
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x,shoppingCart,"id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) throws Exception {
//        获取订单状态
        Orders orderDb = orderMapper.findById(id);
//        校验订单是否存在
        if (orderDb == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (orderDb.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderDb.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (orderDb.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            /*weChatPayUtil.refund(
                    order.getNumber(), //商户订单号
                    order.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额*/

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 订单详情查询
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
//        查询对应的订单
        Orders order = orderMapper.findById(id);
//        根据订单查询对应详情
        List<OrderDetail> details = orderDetailMapper.findByOrderId(order.getId());
//        封装为vo对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(details);

        return orderVO;
    }

    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQueryHistory(int page, int pageSize, Integer status) {
        PageHelper.startPage(page,pageSize);
//        查询订单
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        Page<Orders> orders = orderMapper.page(ordersPageQueryDTO);

        List<OrderVO> orderVOS = new ArrayList<>();
//        查询订单详情
        if(orders!=null&&orders.getTotal()>0){
            for (Orders order : orders) {
                List<OrderDetail> orderDetail = orderDetailMapper.findByOrderId(order.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDetailList(orderDetail);
                orderVOS.add(orderVO);
            }
        }
        return new PageResult(orderVOS.size(),orderVOS);
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 搜索查询订单
     * @param pageQueryDTO
     * @return
     */

    @Override
    public PageResult serach(OrdersPageQueryDTO pageQueryDTO) {

        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.page(pageQueryDTO);

        List<OrderVO> orderVOList = changeByOrders(page);

        PageResult pageResult = new PageResult(orderVOList.size(), orderVOList);
        return pageResult;
    }

    /**
     * 将orders对象转换为orderVO对像
     * @param page
     * @return
     */
    private List<OrderVO> changeByOrders(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        if(!CollectionUtils.isEmpty(page)){
            for (Orders orders : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                String orderDishes = getOrderDishes(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }

        return orderVOList;
    }

    /**
     * 将订单的所有菜品名称合并为一个字符串
     * @param orders
     * @return
     */
    private String getOrderDishes(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.findByOrderId(orders.getId());

        List<String> orderDishesList = orderDetailList.stream().map(x->{
            String item = x.getName()+"*"+x.getNumber();
            return item;
        }).collect(Collectors.toList());

        return String.join(";",orderDishesList);
    }

    /**
     * 统计订单状态的数量
     * @return
     */

    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersDTO
     */
    @Override
    public void confirmOrders(OrdersConfirmDTO ordersDTO) {
        Orders orders = new Orders();
        orders.setId(ordersDTO.getId());
        orders.setStatus(Orders.CONFIRMED);
        log.info("{}",orders);
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejectionOrders(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.findById(ordersRejectionDTO.getId());

        if(orders==null||orders.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        int payStatus = orders.getPayStatus();
        if(payStatus == Orders.PAID){
//            代替给用户退款的操作
            log.info("用户已经退款");
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersDTO) {
        Orders orders = orderMapper.findById(ordersDTO.getId());

        if(orders==null||orders.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        int payStatus = orders.getPayStatus();
        if(payStatus == Orders.PAID){
//            代替给用户退款的操作
            log.info("用户已经退款");
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 开始派送订单
     * @param id
     */
    @Override
    public void send(Long id) {
        Orders orders = orderMapper.findById(id);
        if(orders == null || orders.getStatus() != Orders.CONFIRMED){
            throw  new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.findById(id);
        if(orders == null || orders.getStatus() != Orders.DELIVERY_IN_PROGRESS){
            throw  new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }
}
