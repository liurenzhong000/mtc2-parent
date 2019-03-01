package io.mtc.facade.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netflix.ribbon.proxy.annotation.Http;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.util.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hooger on 2018/7/26.
 */
@Api(value = "元数据API", description = "元数据为基础数据，一般作为请求行情数据的参数")
@Slf4j
@RestController
@RequestMapping("api")
public class MarketBasedataController {

    @Value("${api.blockcc.url}")
    private String apiurl;

    @ApiOperation(value = "获取元数据列表")
    @ApiImplicitParam(name = "element", value = "元数据名称:\nmarkets(获取所有支持的交易所列表),\nsymbols(获取所有支持的币种列表),\ncurrencies(获取所有支持的基础货币列表)\n,market_pairs(获取所有支持的交易所以及支持的交易对列表)", required = true, dataType = "String")
    @GetMapping("/base/{element}")
    public String elementlist(@PathVariable String element){
        String url = apiurl+"/"+element;
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONArray data = respobj.getJSONArray("data");
            return ResultUtil.success(data);
        }
    }

    @ApiOperation("获取指定交易所信息")
    @ApiImplicitParam(name = "name", value = "交易所名称(ID)", required = true, dataType = "String")
    @GetMapping(value = "/market/{name}")
    public String marketname(@PathVariable String name){
        String url = apiurl+"/market/"+name;
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONObject data = respobj.getJSONObject("data");
            return ResultUtil.success(data);
        }
    }
}
