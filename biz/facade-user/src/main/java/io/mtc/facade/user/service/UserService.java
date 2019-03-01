package io.mtc.facade.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserKeystore;
import io.mtc.facade.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * user的service
 *
 * @author Chinhin
 * 2018/7/24
 */
@Service
public class UserService {

    @Resource
    private UserRepository  userRepository;

    /**
     * 转账后，调用此接口，可以增加一次抽奖机会
     *
     * 抽奖机会：
     * 1. 每日0点job会重置为1，相当于每日赠送一次
     * 2. 每天分享可以获得1次
     * 3. 每日转账可以获得1次
     * 每日最多可以抽5次，也就是分享+转账最多可以得到4次
     *
     * @param user 转账的用户
     * @param isTransfer true表示转账后，false表示分享后
     */
    @Transactional
    public void addWheelNum(User user, Boolean isTransfer) {
        Integer shareNum = user.getTodayGetWheelNumByShare();
        Integer transferNum = user.getTodayGetWheelNumByTransfer();
        // 今日已经获得了5次
        if ((shareNum + transferNum) == 4) {
            return;
        }
        if (isTransfer) {
            user.setTodayGetWheelNumByTransfer(user.getTodayGetWheelNumByTransfer() + 1);
        } else {
            if (user.getTodayGetWheelNumByShare() > 0) {
                return;
            }
            user.setTodayGetWheelNumByShare(user.getTodayGetWheelNumByShare() + 1);
        }
        user.setWheelNum(user.getWheelNum() + 1);
        userRepository.save(user);
    }

    @Transactional
    public void resetUserWheelNum() {
        userRepository.resetUserWheelNum();
    }

    public Map<String, Object> userInfo(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", user.getId());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("hasFundPassword", StringUtil.isNotBlank(user.getFundPassword()));
        result.put("photo", user.getPhoto());
        result.put("nick", user.getNick());
        result.put("rongyunToken", user.getRongyunToken());
        if (user.getCanWithdrawTime() == null) {
            result.put("canWithdrawTime", 0);
        } else {
            result.put("canWithdrawTime", user.getCanWithdrawTime().getTime());
        }
        result.put("token", user.getToken());
        List<UserKeystore> keystores = user.getKeystores();
        JSONArray jsonArray = JSON.parseArray(CommonUtil.toJson(keystores));
        for (Object temp : jsonArray) {
            JSONObject tempObj = (JSONObject) temp;
            tempObj.remove("id");
            tempObj.remove("createTime");
            tempObj.remove("updateTime");
        }
        result.put("keystores", jsonArray);
        return result;
    }

    public User findUser(String target) {
        User user;
        // 邮箱登录
        if (StringUtil.checkEmail(target)) {
            user = userRepository.findByEmail(target);
            // 手机号登录
        } else {
            user = userRepository.findByPhone(target);
        }
        return user;
    }

}
