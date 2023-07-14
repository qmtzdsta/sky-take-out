package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    /**
     * 微信用户登录
     * @return
     */
    public User WxLogin(UserLoginDTO userLoginDTO) {
//        获取openID
        String openid = getString(userLoginDTO);

        if (openid == null) {
//            如果openid为空，则证明登录失败
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

//        查询Openid是否在自己的用户表里面存在，不存在就注册一个
        User user = userMapper.findByOpenId(openid);
        if (user == null) {
            user = new User();
            user.setCreateTime(LocalDateTime.now());
            user.setOpenid(openid);
            userMapper.insert(user);
        }
        return user;
    }

    private String getString(UserLoginDTO userLoginDTO) {
        Map<String, String> param = new HashMap<>();
        param.put("appid",weChatProperties.getAppid());
        param.put("secret",weChatProperties.getSecret());
        param.put("js_code", userLoginDTO.getCode());
        param.put("grant_type","authorization_code");
        String s = HttpClientUtil.doGet(WX_LOGIN, param);

        JSONObject jsonObject = JSON.parseObject(s);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
