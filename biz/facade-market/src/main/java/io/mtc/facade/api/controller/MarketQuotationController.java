package io.mtc.facade.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.http.util.HttpUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.facade.api.annotations.SystemLog;
import io.mtc.facade.api.common.CacheUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by hooger on 2018/7/26.
 */
@Api(value = "行情数据API", description = "行情数据")
@Slf4j
@RestController
@RequestMapping("api")
public class MarketQuotationController {


    private static final String default_market = "bibox";//"bitmex,okex,binance,huobipro,lhang,hitbtc,zb,upbit,bibox,jex,bitfinex,bithumb,digifinex,quoine,bit-z,coinbase-pro,coinw,kraken,bitstamp,zaif";
    private static  final String default_symbol_name = "ethereum";


    @Value("${api.blockcc.url}")
    private String blockccurl;

    @Value("${api.btcsql.url}")
    private String btcsqlurl;

    @Value("${api.btcsql.secretkey}")
    private String btcsqlsecretkey;
    @Autowired
    private CacheUtil cacheUtil;


    @ApiOperation(value = "获取汇率")
    @GetMapping("/rate")
    public String exchangerate(@RequestParam(required = false) @ApiParam(value = "基础兑换货币，默认USD",required = false) String base,
                               @RequestParam(required = false) @ApiParam(value = "目标兑换货币，可传多个币种，逗号分割，默认返回全部Currency的汇率", required = false) String symbols,
                               @RequestParam(required = false) @ApiParam(value = "Enum('array','object'), 默认object, 决定返回参数中的rates的类型", required = false) String format){
        String url = blockccurl +"/exchange_rate";
        String querystring = "";
        try {
            if (StringUtils.isNotEmpty(base)) {
                querystring += "&base=" + URLEncoder.encode(base, "utf-8");
            }
            if (StringUtils.isNotEmpty(symbols)) {
                querystring += "&symbols=" + URLEncoder.encode(symbols, "utf-8");
            }
            if (StringUtils.isNotEmpty(format)) {
                querystring += "&format=" + URLEncoder.encode(format, "utf-8");
            }
        }catch (Exception e){
            log.error("",e );
        }
        if(querystring.length()>0){
            url+="?"+querystring.substring(1);
        }
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求block API,耗时=[%s]ms,resp=[%s]",(System.currentTimeMillis()-startTime), "不打印"));

        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONObject data = respobj.getJSONObject("data");
            return ResultUtil.success(data);
        }
    }

    @ApiOperation(value = "获取币种价格", notes = "两种参数必须存在一种, 优先级 symbol_name > symbol， 按照交易量大小降序返回")
    @GetMapping("/price")
    public String price(@RequestParam(required = false) @ApiParam(value = "币种名称,可传多个币种,逗号分割",required = false) String symbol_name,
                               @RequestParam(required = false) @ApiParam(value = "币种符号,可传多个币种,逗号分割。一个符号可能对应多个币种", required = false) String symbol){
        JSONObject respobj = getPrice(symbol_name, symbol);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONArray data = respobj.getJSONArray("data");
            return ResultUtil.success(data);
        }
    }

    private JSONObject getPrice(@RequestParam(required = false) @ApiParam(value = "币种名称,可传多个币种,逗号分割", required = false) String symbol_name, @RequestParam(required = false) @ApiParam(value = "币种符号,可传多个币种,逗号分割。一个符号可能对应多个币种", required = false) String symbol) {
        String url = blockccurl +"/price";
        String querystring = "";
        try {
            if (StringUtils.isNotEmpty(symbol_name)) {
                querystring += "&symbol_name=" + URLEncoder.encode(symbol_name, "utf-8");
            }
            if (StringUtils.isNotEmpty(symbol)) {
                querystring += "&symbols=" + URLEncoder.encode(symbol, "utf-8");
            }

        }catch (Exception e){
            log.error("",e );
        }
        if(querystring.length()>0){
            url+="?"+querystring.substring(1);
        }
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求block API,url=[%s],耗时=[%s]ms,resp=[%s]", url,(System.currentTimeMillis()-startTime), "不打印"));
        return JSON.parseObject(apiresponse);
    }


    @SystemLog
    @ApiOperation(value = "获取币种历史价格", notes = "symbol_name symbol 两种参数必须存在一种, 优先级 symbol_name > symbo")
    @GetMapping("/price/history")
    public String pricehistory(@RequestParam(required = false) @ApiParam(value = "币种名称,可传多个币种,逗号分割",required = false) String symbol_name,
                               @RequestParam(required = false) @ApiParam(value = "币种符号,可传多个币种,逗号分割。一个符号可能对应多个币种", required = false) String symbol,
                               @RequestParam(required = false) @ApiParam(value = "返回数据量， 默认1000，最大返回2000条数据", required = false) String limit){
        String url = blockccurl +"/price/history";
        String querystring = "";
        try {
            if (StringUtils.isNotEmpty(symbol_name)) {
                querystring += "&symbol_name=" + URLEncoder.encode(symbol_name, "utf-8");
            }
            if (StringUtils.isNotEmpty(symbol)) {
                querystring += "&symbols=" + URLEncoder.encode(symbol, "utf-8");
            }
            if (StringUtils.isNotEmpty(limit)) {
                querystring += "&limit=" + URLEncoder.encode(limit, "utf-8");
            }

        }catch (Exception e){
            log.error("",e );
        }
        if(querystring.length()>0){
            url+="?"+querystring.substring(1);
        }
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求block API,耗时=[%s]ms,resp=[%s]",(System.currentTimeMillis()-startTime), "不打印"));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONArray data = respobj.getJSONArray("data");
            return ResultUtil.success(data);
        }
    }


    @ApiOperation(value = "获取交易对Ticker")
    @GetMapping("/ticker")
    public String ticker(@RequestParam @ApiParam(value = "交易所名称",required = true) String market,
                               @RequestParam @ApiParam(value = "币种符号,可传多个币种,逗号分割。一个符号可能对应多个币种", required = true) String symbol_pair){
        String url = blockccurl +"/ticker";
        String querystring = "";
        try {
            if (StringUtils.isNotEmpty(market)) {
                querystring += "&market=" + URLEncoder.encode(market, "utf-8");
            }
            if (StringUtils.isNotEmpty(symbol_pair)) {
                querystring += "&symbol_pair=" + URLEncoder.encode(symbol_pair, "utf-8");
            }

        }catch (Exception e){
            log.error("",e );
        }
        if(querystring.length()>0){
            url+="?"+querystring.substring(1);
        }
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求block API,耗时=[%s]ms,resp=[%s]",(System.currentTimeMillis()-startTime), "不打印"));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONObject data = respobj.getJSONObject("data");
           return ResultUtil.success(data);
        }
    }

    @SystemLog
    @ApiOperation(value = "批量获取交易对Tickers")
    @GetMapping("/tickers")
    public String tickers(@RequestParam(required = false) @ApiParam(value = "交易所名称，可传多个，逗号分割",required = false) String market,
                         @RequestParam(required = false) @ApiParam(value = "币种符号，可传多个，逗号分割", required = false) String symbol,
                          @RequestParam(required = false) @ApiParam(value = "币种名称，可传多个，逗号分割", required = false) String symbol_name,
                          @RequestParam(required = false) @ApiParam(value = "基础货币，可传多个，逗号分割", required = false) String currency,
                          @RequestParam(required = false) @ApiParam(value = "交易所-交易对，可传多个，逗号分割", required = false) String market_pair,
                          @RequestParam(required = false) @ApiParam(value = "当前页数，默认 1, (>=1)", required = false) String page,
                          @RequestParam(required = false) @ApiParam(value = "每页数据量，默认 20 (>=1)", required = false) String size	){
        JSONObject respobj = getTickers(market, symbol, symbol_name, currency, market_pair, page, size);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONObject data = respobj.getJSONObject("data");
            int pageint = data.getIntValue("page");
            int sizeint = data.getIntValue("size");
            int totalpage = data.getInteger("total_page");
            int totalcount = data.getIntValue("total_count");
            JSONArray list = data.getJSONArray("list");

            BigDecimal rate = cacheUtil.getUSD2CNY();
            for(int i=list.size()-1;i>=0;i--){
                JSONObject jsonObj = list.getJSONObject(i);
                double usdrate = jsonObj.getDoubleValue("usd_rate");
                if (usdrate<=0){
                    list.remove(i);
                    continue;
                }else{
                    jsonObj.put("cny_rate",rate);
                }
            }


            PagingModel pagingModel = new PagingModel();
            pagingModel.setPageNumber(pageint-1);
            pagingModel.setPageSize(sizeint);
            PageImpl pagemap = new PageImpl(list,pagingModel.make(), totalcount);
            return PagingResultUtil.list(pagemap);
        }
    }

    private JSONObject getTickers(@RequestParam(required = false) @ApiParam(value = "交易所名称，可传多个，逗号分割", required = false) String market, @RequestParam(required = false) @ApiParam(value = "币种符号，可传多个，逗号分割", required = false) String symbol, @RequestParam(required = false) @ApiParam(value = "币种名称，可传多个，逗号分割", required = false) String symbol_name, @RequestParam(required = false) @ApiParam(value = "基础货币，可传多个，逗号分割", required = false) String currency, @RequestParam(required = false) @ApiParam(value = "交易所-交易对，可传多个，逗号分割", required = false) String market_pair, @RequestParam(required = false) @ApiParam(value = "当前页数，默认 1, (>=1)", required = false) String page, @RequestParam(required = false) @ApiParam(value = "每页数据量，默认 20 (>=1)", required = false) String size) {
        String url = blockccurl +"/tickers";
        String querystring = "";
        try {
            if (StringUtils.isNotEmpty(market)) {
                querystring += "&market=" + URLEncoder.encode(market, "utf-8");
            }
            if (StringUtils.isNotEmpty(symbol)) {
                querystring += "&symbol=" + URLEncoder.encode(symbol, "utf-8");
            }

            if (StringUtils.isNotEmpty(symbol_name)) {
                querystring += "&symbol_name=" + URLEncoder.encode(symbol_name, "utf-8");
            }

            if (StringUtils.isNotEmpty(currency)) {
                querystring += "&currency=" + URLEncoder.encode(currency, "utf-8");
            }

            if (StringUtils.isNotEmpty(market_pair)) {
                querystring += "&market_pair=" + URLEncoder.encode(market_pair, "utf-8");
            }

            if (StringUtils.isNotEmpty(page)) {
                querystring += "&page=" + URLEncoder.encode(page, "utf-8");
            }

            if (StringUtils.isNotEmpty(size	)) {
                querystring += "&size=" + URLEncoder.encode(size	, "utf-8");
            }



        }catch (Exception e){
            log.error("",e );
        }
        if(querystring.length()>0){
            url+="?"+querystring.substring(1);
        }
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求block API,耗时=[%s]ms,resp=[%s]",(System.currentTimeMillis()-startTime), "不打印"));
        return JSON.parseObject(apiresponse);
    }


    @ApiOperation(value = "获取交易对深度")
    @GetMapping("/depth")
    public String depth(@RequestParam @ApiParam(value = "交易所名称",required = true) String market,
                          @RequestParam @ApiParam(value = "交易对", required = true) String symbol_pair,
                          @RequestParam(required = false) @ApiParam(value = "深度档位，默认25", required = false) String limit){
        String url = blockccurl +"/depth";
        String querystring = "";
        try {
            if (StringUtils.isNotEmpty(market)) {
                querystring += "&market=" + URLEncoder.encode(market, "utf-8");
            }
            if (StringUtils.isNotEmpty(symbol_pair)) {
                querystring += "&symbol_pair=" + URLEncoder.encode(symbol_pair, "utf-8");
            }

            if (StringUtils.isNotEmpty(limit)) {
                querystring += "&limit=" + URLEncoder.encode(limit, "utf-8");
            }

        }catch (Exception e){
            log.error("",e );
        }
        if(querystring.length()>0){
            url+="?"+querystring.substring(1);
        }
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求block API,耗时=[%s]ms,resp=[%s]",(System.currentTimeMillis()-startTime), "不打印"));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONObject data = respobj.getJSONObject("data");
            return ResultUtil.success(data);
        }
    }



    @ApiOperation(value = "获取交易对成交记录")
    @GetMapping("/trade")
    public String trade(@RequestParam @ApiParam(value = "交易所名称",required = true) String market,
                        @RequestParam @ApiParam(value = "交易对", required = true) String symbol_pair,
                        @RequestParam(required = false) @ApiParam(value = "返回数据量，默认50", required = false) String limit){
        String url = blockccurl +"/trade";
        String querystring = "";
        try {
            if (StringUtils.isNotEmpty(market)) {
                querystring += "&market=" + URLEncoder.encode(market, "utf-8");
            }
            if (StringUtils.isNotEmpty(symbol_pair)) {
                querystring += "&symbol_pair=" + URLEncoder.encode(symbol_pair, "utf-8");
            }

            if (StringUtils.isNotEmpty(limit)) {
                querystring += "&limit=" + URLEncoder.encode(limit, "utf-8");
            }

        }catch (Exception e){
            log.error("",e );
        }
        if(querystring.length()>0){
            url+="?"+querystring.substring(1);
        }
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求block API,耗时=[%s]ms,resp=[%s]",(System.currentTimeMillis()-startTime), "不打印"));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONArray data = respobj.getJSONArray("data");
            return ResultUtil.success(data);
        }
    }


    @ApiOperation(value = "获取交易对K线数据(OHLCV)")
    @GetMapping("/kline")
    public String kline(@RequestParam @ApiParam(value = "交易所名称",required = true) String market,
                        @RequestParam @ApiParam(value = "交易对", required = true) String symbol_pair,
                        @RequestParam(required = false) @ApiParam(value = "返回数据量,默认1000", required = false) String limit,
                        @RequestParam(required = false) @ApiParam(value = "K线类型ENUM[5m,15m,30m,1h,6h,1d],默认5m",required = false) String type){
        String url = blockccurl +"/kline";
        String querystring = "";
        try {
            if (StringUtils.isNotEmpty(market)) {
                querystring += "&market=" + URLEncoder.encode(market, "utf-8");
            }
            if (StringUtils.isNotEmpty(symbol_pair)) {
                querystring += "&symbol_pair=" + URLEncoder.encode(symbol_pair, "utf-8");
            }

            if (StringUtils.isNotEmpty(limit)) {
                querystring += "&limit=" + URLEncoder.encode(limit, "utf-8");
            }

            if (StringUtils.isNotEmpty(type)) {
                querystring += "&type=" + URLEncoder.encode(type, "utf-8");
            }

        }catch (Exception e){
            log.error("",e );
        }
        if(querystring.length()>0){
            url+="?"+querystring.substring(1);
        }
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求block API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求block API,耗时=[%s]ms,resp=[%s]",(System.currentTimeMillis()-startTime), "不打印"));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(0 != code){
            return ResultUtil.error(respobj.getString("message"), code);
        }else{
            JSONArray data = respobj.getJSONArray("data");
            return ResultUtil.success(data);
        }
    }


    @SystemLog
    @ApiOperation(value = "行情主页")
    @ApiImplicitParam(name = "jsonObject", value = "json参数:      {\n" +
            "        \"keyword\": \"MTC\",\n" +
            "        \"favorite\": [\"mesh-network\",\"bitcoin\"]\n" +
            "      }", required = false)
    @PostMapping("/quotation")
    public String quotation(@RequestBody(required = false) JSONObject jsonObject){
        String symbolnames = "";
        JSONArray favoritelist = jsonObject.getJSONArray("favorite");
        String keyword = jsonObject.getString("keyword");
        Map<String,Integer> sortmap = new HashMap<>();
        if(null != favoritelist && !favoritelist.isEmpty()){
            Set<String> symbollist = new HashSet<>();
            for(int i=0;i<favoritelist.size();i++){
                String symbolname = favoritelist.getString(i);
               symbollist.add(symbolname);
               sortmap.put(symbolname, i);
            }
            symbolnames = StringUtils.join(symbollist, ",");
        }else if(StringUtils.isNotEmpty(keyword)){

            keyword = keyword.trim().replaceAll("  "," ");
            String[]keyarray = keyword.split(" ");
            for(String key : keyarray){
                String targetsymbol = StringUtils.join(cacheUtil.searchSymbol(key), ",");
                if(StringUtils.isNotEmpty(targetsymbol)){
                    symbolnames+=targetsymbol+",";
                }
            }

            if(symbolnames.length()>0){
                symbolnames= symbolnames.substring(0,symbolnames.length()-1);
            }

        }else{

            symbolnames = default_symbol_name;
        }
        if(((null != favoritelist && !favoritelist.isEmpty())||StringUtils.isNotEmpty(keyword))&& StringUtils.isEmpty(symbolnames)){
           return ResultUtil.success(new ArrayList<>());
        }else{
            JSONObject respobj = getPrice(symbolnames,null);

            int code = respobj.getIntValue("code");
            if(0 != code){
                return ResultUtil.error(respobj.getString("message"), code);
            }else {
                JSONArray data = respobj.getJSONArray("data");


                BigDecimal rate = cacheUtil.getUSD2CNY();
                for (int i = data.size() - 1; i >= 0; i--) {
                    JSONObject jsonObj = data.getJSONObject(i);
                        jsonObj.put("cny_rate", rate);
                        jsonObj.put("logo","https://blockchains.oss-cn-shanghai.aliyuncs.com/static/coinInfo/"+jsonObj.getString("name").toLowerCase()+".png");
                }

                if(sortmap.size()>0) {
                    Collections.sort(data, new Comparator() {

                        @Override
                        public int compare(Object o1, Object o2) {
                            JSONObject json1 = (JSONObject) o1;
                            JSONObject json2 = (JSONObject) o2;

                            Integer sort1 = sortmap.get(json1.getString("name"));
                            Integer sort2 = sortmap.get(json2.getString("name"));
                            if(null == sort1)
                                return -1;
                            if(null == sort2)
                                return 1;
                            return sort1.compareTo(sort2);
                        }
                    });
                }



                return ResultUtil.success(data);
            }
        }

    }



    @ApiOperation(value = "币种概况",notes = "{\n" +
            "\t\"result\": {\n" +
            "\t\t\"ftime\": \"2018-03-01 \",\n" +
            "\t\t\"amount\": \"10亿\",\n" +
            "\t\t\"website\": \"http://www.mtc.io\",\n" +
            "\t\t\"capital\": \"\",\n" +
            "\t\t\"wallet\": \"\",\n" +
            "\t\t\"line\": \"\",\n" +
            "\t\t\"mctime\": \"\",\n" +
            "\t\t\"cn\": \"\",\n" +
            "\t\t\"type\": \"代币\",\n" +
            "\t\t\"allot\": \"\",\n" +
            "\t\t\"tintro\": \"\",\n" +
            "\t\t\"intro\": \"MTC Mesh Network 是一种将物联网机器与机器互相通信的Mesh网络；它是一种通过机器与机器自身通信模块Bluetooth-LE/Wi-fi等互相数据广播来通信的去中心化网络协议；在整个Mesh网络中，每一个节点可能是手机、冰箱、汽车、收音机、机器人等。有了MTC Mesh 网络，就能让所有区块链项目和物联网设备在无网情况下实现快速、便捷的价值传递。它能支持所有的区块链项目，比如Bitcoin，Etherum,EOS,Qtum,Achain等。MTC 能解决大量交易导致的网络堵塞，也能减少物联网网络建设的成本。\",\n" +
            "\t\t\"guid\": \"MTC Mesh Network\",\n" +
            "\t\t\"gintro\": \"\",\n" +
            "\t\t\"ratio\": \" \"\n" +
            "\t},\n" +
            "\t\"status\": 200,\n" +
            "\t\"timestamp\": 1534929691400\n" +
            "}")
    @GetMapping("/brief")
    public String tokenbrief(@ApiParam(value = "币种全称", required = true) @RequestParam String name) throws  Exception{

        String guid = cacheUtil.getGuid(name);

        if(null == guid){
            return ResultUtil.error("无此币种概况信息");
        }
        String url = btcsqlurl+"/api/web/brief/token-brief?secret_key="+btcsqlsecretkey+"&guid="+URLEncoder.encode(guid, "utf-8").replaceAll("\\+","%20");
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求btcsql API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求btcsql API,url=[%s],耗时=[%s]ms,resp=[%s]",url,(System.currentTimeMillis()-startTime), apiresponse));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(200 != code){
            return ResultUtil.error(respobj.getString("msg"), code);
        }else{
            JSONObject data = respobj.getJSONObject("data").getJSONObject("data");
            String intro = StringUtils.replaceAll(data.getString("intro"), "\n", "");
            data.put("intro", intro);

            return ResultUtil.success(data);
        }


    }

    @ApiOperation(value = "币种信号列表",notes = "{\n" +
            "\t\"result\": [{\n" +
            "\t\t\"template\": [\"转出地址：0x7d9d60f2…6ff7【持币地址第58名】 \", \"转入地址：0xe28d5a15…6e12 \", \"Token数量：1558095.5901  \", \"金额：约94547.56元 \", \"24H行情：当前均价0.06068148708元，涨跌幅3.3%\"],\n" +
            "\t\t\"symbol\": \"MTC\",\n" +
            "\t\t\"md\": \"\",\n" +
            "\t\t\"token_name\": \"MTC Mesh Network\",\n" +
            "\t\t\"ctime\": \"11:51\",\n" +
            "\t\t\"signal_color\": \"0x436EEE\",\n" +
            "\t\t\"tokens\": \"\",\n" +
            "\t\t\"exchange\": \"\",\n" +
            "\t\t\"details\": \"9.45万\",\n" +
            "\t\t\"id\": \"2602810\",\n" +
            "\t\t\"signal\": \"链上大额转账\"\n" +
            "\t}],\n" +
            "\t\"status\": 200,\n" +
            "\t\"timestamp\": 1534929420081\n" +
            "}")
    @GetMapping("/signal")
    public String signallist(@ApiParam(value = "币种全称", required = true) @RequestParam String name){
        String guid = cacheUtil.getGuid(name);

        if(null == guid){
            return ResultUtil.error("无此币种信号信息");
        }

        String url = btcsqlurl+"/api/web/msg/signal-list?secret_key="+btcsqlsecretkey+"&guid="+guid;
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求btcsql API,url=[%s]", url));
        String apiresponse = HttpUtil.post(url, new HashMap<String,String>(){
            {
                put("secret_key", btcsqlsecretkey);
                put("guid", guid);
            }
        });
        log.info(String.format("结束请求btcsql API,url=[%s],耗时=[%s]ms,resp=[%s]",url,(System.currentTimeMillis()-startTime), apiresponse));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if(200 != code){
            return ResultUtil.error(respobj.getString("msg"), code);
        }else{
            JSONArray data = respobj.getJSONObject("data").getJSONArray("data_list");
            return ResultUtil.success(data);
        }
    }

    @ApiOperation(value = "筹码数据")
    @GetMapping("/chips")
    public Object chips(@ApiParam(value = "币种全称", required = true) @RequestParam String name) {
        String guid = cacheUtil.getGuid(name);
        if (null == guid) {
            return ResultUtil.error("无此币种信号信息");
        }
        String url = btcsqlurl+"/api/web/digiccy/token-info?secret_key="+btcsqlsecretkey+"&guid="+guid;
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求btcsql API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求btcsql API,url=[%s],耗时=[%s]ms,resp=[%s]",url,(System.currentTimeMillis()-startTime), apiresponse));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if (code != 200) {
            return ResultUtil.errorObj(respobj.getString("msg"), code);
        } else {
            JSONObject data = respobj.getJSONObject("data");
            return ResultUtil.successObj(data);
        }
    }

    @ApiOperation(value = "币种折线图数据")
    @GetMapping("/digiccy7d")
    public Object digiccy7d(@ApiParam(value = "币种全称", required = true) @RequestParam String name) {
        String guid = cacheUtil.getGuid(name);
        if (null == guid) {
            return ResultUtil.error("无此币种信号信息");
        }
        String url = btcsqlurl+"/api/web/digiccy/get-digiccy7d-data-ver?secret_key="+btcsqlsecretkey+"&guid="+guid;
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求btcsql API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求btcsql API,url=[%s],耗时=[%s]ms,resp=[%s]", url,
                (System.currentTimeMillis() - startTime), apiresponse));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if (code != 200) {
            return ResultUtil.errorObj(respobj.getString("msg"), code);
        } else {
            JSONObject data = respobj.getJSONObject("data");
            return ResultUtil.successObj(data);
        }
    }

    @ApiOperation(value = "24小时数据")
    @GetMapping("/digiccy24h")
    public Object digiccy24h(@ApiParam(value = "币种全称", required = true) @RequestParam String name) {
        String guid = cacheUtil.getGuid(name);
        if (null == guid) {
            return ResultUtil.error("无此币种信号信息");
        }
        String url = btcsqlurl+"/api/web/digiccy/get-part-digiccy24h-data?secret_key="+btcsqlsecretkey+"&guid="+guid;
        long startTime = System.currentTimeMillis();
        log.info(String.format("开始请求btcsql API,url=[%s]", url));
        String apiresponse = HttpUtil.get(url);
        log.info(String.format("结束请求btcsql API,url=[%s],耗时=[%s]ms,resp=[%s]", url,
                (System.currentTimeMillis() - startTime), apiresponse));
        JSONObject respobj = JSON.parseObject(apiresponse);

        int code = respobj.getIntValue("code");
        if (code != 200) {
            return ResultUtil.errorObj(respobj.getString("msg"), code);
        } else {
            JSONObject data = respobj.getJSONObject("data");
            return ResultUtil.successObj(data);
        }
    }

}
