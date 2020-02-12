package io.mtc.facade.user.apiController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.constants.BillTypeConverter;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.wheel.WheelPrize;
import io.mtc.facade.user.entity.wheel.WheelRecord;
import io.mtc.facade.user.entity.yukuang.YukuangTrade;
import io.mtc.facade.user.repository.WheelPrizeRepository;
import io.mtc.facade.user.repository.WheelRecordRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 转盘相关的
 */
@ApiIgnore
@Transactional(readOnly = true)
@RequestMapping("/wheelApi")
@RestController
public class WheelApiController {

    @Resource
    private WheelRecordRepository wheelRecordRepository;

    @Resource
    private WheelPrizeRepository wheelPrizeRepository;

    @Transactional
    @PostMapping("/record")
    public String updateStatus(Long recordId, Integer status) {
        WheelRecord wheelRecord = wheelRecordRepository.findById(recordId).get();
        wheelRecord.setStatus(status);
        wheelRecordRepository.save(wheelRecord);
        return ResultUtil.success();
    }

    /**
     * 中奖一览
     */
    @GetMapping("/record")
    public String record(String prizeName, String startTime, String endTime, Integer status,
                         Integer selectUserType, String userInfo, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<WheelRecord> specification = (Specification<WheelRecord>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotEmpty(startTime)) {
                list.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"),
                        DateUtil.parseDate(startTime, "yyyy-MM-dd HH:mm:ss")));
            }
            if (status != null && status != 0) {
                list.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (StringUtil.isNotEmpty(endTime)) {
                list.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"),
                        DateUtil.parseDate(endTime, "yyyy-MM-dd HH:mm:ss")));
            }
            if (StringUtil.isNotEmpty(prizeName)) {
                list.add(criteriaBuilder.like(root.get("name"), "%" + prizeName + "%"));
            }
            if (selectUserType != null && selectUserType != 0 && StringUtil.isNotEmpty(userInfo)) {
                Join<WheelRecord, User> userJoin = root.join("user", JoinType.LEFT);
                Predicate userJoinPredicate;
                if (selectUserType == 1) {
                    userJoinPredicate = criteriaBuilder.equal(userJoin.get("phone"), userInfo);
                } else if (selectUserType == 2) {
                    userJoinPredicate = criteriaBuilder.like(userJoin.get("email"), "%" + userInfo + "%");
                } else {
                    userJoinPredicate = criteriaBuilder.equal(userJoin.get("id"), Integer.parseInt(userInfo));
                }
                list.add(userJoinPredicate);
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(wheelRecordRepository.findAll(specification, pageModel.make()));
    }

    /**
     * 获得奖品一览
     */
    @GetMapping("/prize")
    public String prizes() {
        Iterable<WheelPrize> all = wheelPrizeRepository.findAll();
        List<WheelPrize> resultList = new ArrayList<>();
        for (WheelPrize wheelPrize : all) {
            resultList.add(wheelPrize);
        }
        return ResultUtil.success(resultList);
    }

    /**
     * 更新奖品
     *
     * @param json [{name : '名字', type : '类型', pic : '图片', stock: '库存(int)', rate: '概率(double)'}]
     *             概率之和必须为1，必须是8个元素
     */
    @Transactional
    @PostMapping("/prize")
    public String updatePrize(String json) {
        JSONArray data = JSONArray.parseArray(json);
        if (data.size() != 8) {
            return ResultUtil.error("转盘奖品必须为8个");
        }

        Iterable<WheelPrize> all = wheelPrizeRepository.findAll();
        List<WheelPrize> updatePrizes = new ArrayList<>();

        BigDecimal rateTotal = BigDecimal.ZERO;
        int index = 0;
        for (WheelPrize tempPrize : all) {
            JSONObject temp = data.getJSONObject(index);

            tempPrize.setType(temp.getIntValue("type"));
            tempPrize.setName(temp.getString("name"));
            tempPrize.setPic(temp.getString("pic"));
            tempPrize.setStock(temp.getIntValue("stock"));
            tempPrize.setRate(temp.getBigDecimal("rate"));
            updatePrizes.add(tempPrize);

            rateTotal = rateTotal.add(tempPrize.getRate());
            index ++;
        }

        // 概率之和不为1
        if (rateTotal.compareTo(BigDecimal.ONE) != 0) {
            return ResultUtil.error("概率之和必须为1");
        }
        wheelPrizeRepository.saveAll(updatePrizes);
        return ResultUtil.success();
    }

}