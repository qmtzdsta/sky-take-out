package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.AddressProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Slf4j
public class AddressUtil {
    private static String ak;
    private final static String geocodingUrl = "https://api.map.baidu.com/geocoding/v3";
    private final static String drivingUrl = "https://api.map.baidu.com/directionlite/v1/driving";

    public AddressUtil(String ak){
        this.ak = ak;
    }
//    获取地方的经纬度地址
    public String geocoding(String address){
        Map<String, String> map = new HashMap<>();
        map.put("address",address);
        map.put("output","json");
        map.put("ak",ak);
        String s = HttpClientUtil.doGet(geocodingUrl, map);

        JSONObject jsonObject = JSON.parseObject(s);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException(address+"地址解析失败");
        }

        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        String adminAddress = lat + "," + lng;
        return adminAddress;
    }

//    配送路线规划
    public void driving(String origin,String target){
        HashMap<String, String> map = new HashMap<>();
        map.put("origin",origin);
        map.put("destination",target);
        map.put("steps_info","0");
        map.put("ak",ak);

        String s = HttpClientUtil.doGet(drivingUrl, map);
        JSONObject jsonObject = JSON.parseObject(s);if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("驾车规划解析失败");
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }


}
