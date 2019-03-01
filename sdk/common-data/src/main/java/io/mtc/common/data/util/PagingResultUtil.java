package io.mtc.common.data.util;

import io.mtc.common.util.CommonUtil;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 依赖于jpa的翻页结果
 *
 * @author Chinhin
 * 2018/6/16
 */
public class PagingResultUtil {

    /**
     * 翻页列表信息
     * @param obj 翻页的对象
     * @return 结果字符串
     */
    public static <T> String list(Page<T> obj){
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("pageNumber", obj.getPageable().getPageNumber());
        pageInfo.put("pageSize", obj.getPageable().getPageSize());
        pageInfo.put("totalElements", obj.getTotalElements());
        pageInfo.put("totalPages", obj.getTotalPages());
        Map<String, Object> data = new HashMap<>();
        data.put("page", pageInfo);
        data.put("list", obj.getContent());
        result.put("status", 200);
        result.put("timestamp", new Date().getTime());
        result.put("result", data);
        return CommonUtil.toJson(result);
    }

}
