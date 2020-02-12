package io.mtc.facade.api.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RateCacheUtil;
import io.mtc.common.redis.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hooger on 2018/7/30.
 */
@Slf4j
@Component
public class CacheUtil {

    @Resource
    private RateCacheUtil rateCacheUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${api.blockcc.url}")
    private String blockccurl;

    @Value("${api.btcsql.url}")
    private String btcsqlurl;

    @Value("${api.btcsql.secretkey}")
    private String btcsqlsecretkey;


    /**
     * 美元兑人民币汇率
     * @return 汇率
     */
    public BigDecimal getUSD2CNY() {
        return rateCacheUtil.getUSD2CNY();
    }

//    public List<String> searchMarket(String keyword){
//        List<String> marketlist = new ArrayList<>();
//        JSONArray data = marketlist();
//        if(null != data){
//            for(int i=0;i<data.size();i++){
//                JSONObject jsonObj = data.getJSONObject(i);
//               if(jsonObj.getString("name").toLowerCase().contains(keyword.toLowerCase()) ||
//                       jsonObj.getString("display_name").toLowerCase().contains(keyword.toLowerCase()) ){
//                   marketlist.add(jsonObj.getString("name"));
//               }
//            }
//        }
//        return marketlist;
//
//    }
//
//    private JSONArray marketlist() {
//        Object o = redisUtil.get(RedisKeys.QUOTATION_MARKET);
//        JSONArray data = null;
//        if(null != o){
//            data = (JSONArray)o;
//        }else{
//            String url = blockccurl +"/markets";
//            String apiresponse = HttpUtil.get(url);
//            JSONObject respobj = JSON.parseObject(apiresponse);
//            data = respobj.getJSONArray("data");
//            redisUtil.set(RedisKeys.QUOTATION_MARKET, data);
//        }
//        return data;
//    }

    public JSONArray refreshSymbolList() {
        String url = blockccurl +"/symbols";
        String apiresponse = HttpUtil.get(url);
        JSONObject respobj = JSON.parseObject(apiresponse);
        if (respobj == null) {
            log.error("刷新symbol list失败");
            return null;
        }
        JSONArray data = respobj.getJSONArray("data");
        redisUtil.set(RedisKeys.QUOTATION_SYMBOL, data);
        return data;
    }

    private JSONArray symbollist() {
        JSONArray data = redisUtil.get(RedisKeys.QUOTATION_SYMBOL, JSONArray.class);
        if(data == null){
            data = refreshSymbolList();
        }
        return data;
    }

    public List<String> searchSymbol(String keyword){
        List<String> symbollist = new ArrayList<>();
        JSONArray data = symbollist();
        if(null != data){
            for(int i=0;i<data.size();i++){
                JSONObject jsonObj = data.getJSONObject(i);
                if(jsonObj.getString("name").toLowerCase().contains(keyword.toLowerCase()) ||
                        jsonObj.getString("symbol").toLowerCase().contains(keyword.toLowerCase()) ||
                        jsonObj.getJSONArray("alias").contains(keyword)){
                    symbollist.add(jsonObj.getString("name"));
                }
            }
        }
        return symbollist;

    }


    public JSONArray guidlist() {
        Object o = redisUtil.get(RedisKeys.QUOTATION_GUID);
        JSONArray data = null;
        if(null != o){
            data = (JSONArray)o;
        }else{
            long startTime = System.currentTimeMillis();
            String url = btcsqlurl+"/api/web/brief/token-list?secret_key="+btcsqlsecretkey;
            log.info(String.format("开始请求btcsql API,url=[%s]", url));
            String apiresponse = HttpUtil.get(url);
            log.info(String.format("结束请求btcsql API,url=[%s],耗时=[%s]ms,resp=[%s]",url,(System.currentTimeMillis()-startTime), apiresponse));
            JSONObject respobj = JSON.parseObject(apiresponse);
            data = respobj.getJSONObject("data").getJSONArray("list");

            redisUtil.set(RedisKeys.QUOTATION_GUID, data,60*60*24);
        }
        return data;
    }

    public String getGuid(String name){
        JSONArray symbollist = symbollist();
        JSONObject symbolobj = null;
        for(int i=0;i<symbollist.size();i++){
            JSONObject obj = symbollist.getJSONObject(i);
            if(obj.getString("name").toLowerCase().equals(name.toLowerCase())){
                symbolobj = obj;
                break;
            }
        }

        JSONArray guidlist = guidlist();

        String guid = null;
        for(int i=0;i<guidlist.size();i++){
            JSONObject obj = guidlist.getJSONObject(i);
            if(obj.getString("guid").toLowerCase().equals(name.toLowerCase())){
                return obj.getString("guid");
            }else if(obj.getString("symbol").toLowerCase().equals(symbolobj.getString("symbol").toLowerCase())){

                if(null != guid){
                    return null;
                }
                guid = obj.getString("guid");
            }
        }
        return guid;
    }

}
