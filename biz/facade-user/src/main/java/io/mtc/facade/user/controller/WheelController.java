package io.mtc.facade.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.wheel.WheelPrize;
import io.mtc.facade.user.entity.wheel.WheelRecord;
import io.mtc.facade.user.repository.UserRepository;
import io.mtc.facade.user.repository.WheelPrizeRepository;
import io.mtc.facade.user.repository.WheelRecordRepository;
import io.mtc.facade.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 转盘控制器
 *
 * @author Chinhin
 * 2018/12/24
 */
@Api(description="转盘", tags = {"转盘"})
@RestController
@RequestMapping("/wheel")
public class WheelController {

    @Resource
    private WheelPrizeRepository wheelPrizeRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private WheelRecordRepository wheelRecordRepository;

    @Resource
    private UserService userService;

    @ApiOperation(value="抽奖", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
    })
    @Transactional
    @PostMapping
    public Object wheel(@RequestHeader Long uid) {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return ResultUtil.error(MTCError.USER_NOT_EXIST);
        }
        Integer wheelNum = user.getWheelNum();
        if (wheelNum < 1) {
            return ResultUtil.error(MTCError.USER_WHEEL_NUM_NOT_ENOUTH);
        }
        user.setWheelNum(user.getWheelNum() - 1);
        userRepository.save(user);

        Random r = new Random();
        BigDecimal catchRate = new BigDecimal(r.nextDouble());

        BigDecimal exceptRate = BigDecimal.ZERO;
        ArrayList<WheelPrize> prizes = Lists.newArrayList(wheelPrizeRepository.findAll());

        WheelPrize winPrize = null;
        // 最后一个不计算
        for (int i = 0; i < 7; i ++) {
            if (prizes.get(i).getStock() <= 0 && prizes.get(i).getType() != 1) {
                continue;
            }
            exceptRate = exceptRate.add(prizes.get(i).getRate());
            if (catchRate.compareTo(exceptRate) < 0) {
                winPrize = prizes.get(i);
                break;
            }
        }
        if (winPrize == null) {
            winPrize = prizes.get(7);
        }
        // 不是未中奖
        if (winPrize.getType() != 1) {
            winPrize.setStock(winPrize.getStock() - 1);
            wheelPrizeRepository.save(winPrize);

            WheelRecord wheelRecord = new WheelRecord();
            wheelRecord.setName(winPrize.getName());
            wheelRecord.setPic(winPrize.getPic());
            wheelRecord.setUser(user);
            wheelRecordRepository.save(wheelRecord);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("type", winPrize.getType());
        result.put("name", winPrize.getName());
        result.put("pic", winPrize.getPic());
        return ResultUtil.success(result);
    }

    /**
     * 初始化获得奖品一览及用户的抽奖次数
     */
    @ApiOperation(value="初始化抽奖信息", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
    })
    @GetMapping
    public Object init(@RequestHeader Long uid) {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return ResultUtil.error(MTCError.USER_NOT_EXIST);
        }

        List<Map<String, Object>> prizes = new ArrayList<>();
        for (WheelPrize wheelPrize : wheelPrizeRepository.findAll()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("name", wheelPrize.getName());
            temp.put("pic", wheelPrize.getPic());
            prizes.add(temp);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("wheelNum", user.getWheelNum());
        result.put("prizes", prizes);
        return ResultUtil.successObj(result);
    }

    @ApiOperation(value="分享后调用此接口，可以增加一次抽奖机会（每天只会增加一次，多的时候不报错）",
            notes = "分享步骤:<br/>1, h5端点击分享的时候，通过js回调原生；<br/>2，原生分享跳往到其他app之前，调用此接口",
            tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
    })
    @PostMapping("/share")
    public Object shareWheel(@RequestHeader Long uid) {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return ResultUtil.error(MTCError.USER_NOT_EXIST);
        }
        userService.addWheelNum(user, false);
        return ResultUtil.successObj();
    }

    @ApiOperation(value="获取中奖记录", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认交易时间'times'", dataType = "String")
    })
    @GetMapping("/record")
    public Object record(@RequestHeader Long uid, @ModelAttribute PagingModel pageModel) {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return ResultUtil.error(MTCError.USER_NOT_EXIST);
        }
        // 设置默认排序
        if (StringUtil.isBlank(pageModel.getSort())) {
            pageModel.setOrder("DESC");
            pageModel.setSort("createTime");
        }
        Page<WheelRecord> allByUser = wheelRecordRepository.findAllByUser(user, pageModel.make());
        JSONObject object = JSON.parseObject(PagingResultUtil.list(allByUser));
        JSONObject resultJson = object.getJSONObject("result");
        JSONArray jsonArray = resultJson.getJSONArray("list");
        for (Object temp : jsonArray) {
            JSONObject tempObj = (JSONObject) temp;
            tempObj.remove("user");
            tempObj.remove("updateTime");
        }
        return object;
    }

}
