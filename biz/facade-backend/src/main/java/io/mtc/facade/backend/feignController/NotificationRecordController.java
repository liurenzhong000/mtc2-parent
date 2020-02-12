package io.mtc.facade.backend.feignController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.backend.feign.ServiceNotification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 通知消息记录控制器
 *
 * @author Chinhin
 * 2018/7/13
 */
@RestController
@RequestMapping("/notifyRecord")
public class NotificationRecordController {

    @Resource
    private ServiceNotification serviceNotification;

    @PreAuthorize("hasAuthority('notifyRecord:select')")
    @GetMapping
    public String select(String address, Integer type, String txHash, @ModelAttribute PagingModel pageModel) {
        if (StringUtil.isBlank(pageModel.getSort())) {
            pageModel.setSort("createTime");
            pageModel.setOrder("DESC");
        }
        String resultList = serviceNotification.select(address, type, txHash, CommonUtil.toJson(pageModel));
        Map<String, Object> resultMap = CommonUtil.jsonToMap(resultList);
        Map result = (Map) resultMap.get("result");
        JSONArray objects = (JSONArray) result.get("list");
        for (Object temp : objects) {
            JSONObject jsonObject = (JSONObject) temp;
            Integer resultType = jsonObject.getInteger("type");
            String url = jsonObject.getString("url");
            String otherAddress = jsonObject.getString("otherAddress");
            Boolean isSender = jsonObject.getBoolean("isSender");

            String note = null;
            // 1:交易通知, 2:后台推送通知
            if (resultType == 1) {
                // 是否发币人
                if (isSender != null && isSender) {
                    note = "收款方：" + otherAddress;
                } else {
                    note = "发币方：" + otherAddress;
                }
            } else if (resultType == 2) {
                note = url;
            }
            jsonObject.put("note", note);
        }
        return resultList;
    }

    @PreAuthorize("hasAuthority('notifyRecord:delete')")
    @DeleteMapping("/{id}")
    public String del(@PathVariable Long id) {
        return serviceNotification.del(id);
    }

}
