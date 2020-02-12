package io.mtc.facade.user.service;

import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.dto.EthTransObj;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.constants.BillStatus;
import io.mtc.facade.user.entity.Bill;
import io.mtc.facade.user.repository.BillRepository;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API的service层
 *
 * @author Chinhin
 * 2018/8/6
 */
@Service
@Transactional
public class ApiService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private BillRepository billRepository;

    @Autowired
    private DepositWithdrawService depositWithdrawService;

    public String selectBill(Long uid, String currencyAddress, Integer type, Integer status, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }

        List<Map<String, Object>> list = getList(uid, currencyAddress, type, status, pageModelStr);
        long total = getCount(uid, currencyAddress, type, status);

        long totalPages = total / pageModel.getPageSize();
        if (total % pageModel.getPageSize() != 0) {
            totalPages ++;
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("pageNumber", pageModel.getPageNumber());
        pageInfo.put("pageSize", pageModel.getPageSize());
        pageInfo.put("totalElements", total);
        pageInfo.put("totalPages", totalPages);
        Map<String, Object> data = new HashMap<>();
        data.put("page", pageInfo);
        data.put("list", list);
        result.put("status", 200);
        result.put("timestamp", new Date().getTime());
        result.put("result", data);
        return CommonUtil.toJson(result);
    }

    public String updateBillStatus(Long id, Integer status){
        if (status != BillStatus.AUDIT_FAILURE.getKey() && status != BillStatus.PENDING.getKey()) {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        Bill bill = billRepository.findById(id).orElse(null);
        if (bill == null) return ResultUtil.error(MTCError.PARAMETER_INVALID);
        if (bill.getStatus() == BillStatus.WAIT_AUDIT) {
            bill.setStatus(BillStatus.getByKey(status));
        } else {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        billRepository.save(bill);
        if (status == BillStatus.AUDIT_FAILURE.getKey()) {
            EthTransObj transInfo = new EthTransObj();
            transInfo.setTxId(bill.getId());
            transInfo.setStatus(2);
            depositWithdrawService.completeWithdraw(transInfo);
        }
        return ResultUtil.success();
    }

    private Long getCount(Long uid, String currencyAddress, Integer type, Integer status) {
        String sql = "SELECT COUNT(t1.id) ";
        sql += getSqlBody(uid, currencyAddress, type, status);
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private List<Map<String, Object>> getList(Long uid, String currencyAddress, Integer type, Integer status, String pageModelStr) {
        String sql = "SELECT  t1.*, t2.user_id ";
        sql += getSqlBody(uid, currencyAddress, type, status);
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        if (StringUtil.isNotBlank(pageModel.getSort())) {
            sql += " ORDER BY t1." + pageModel.getSort() + " " + pageModel.getOrder();
        }
        sql += " LIMIT " + pageModel.getPageNumber() * pageModel.getPageSize() + ", " + pageModel.getPageSize();
        return jdbcTemplate.queryForList(sql);
    }

    private String getSqlBody(Long uid, String currencyAddress, Integer type, Integer status) {
        String sql =
                        "FROM " +
                        "  bill t1 " +
                        "  INNER JOIN user_balance t2 " +
                        "    ON t2.id = t1.balance_id ";
        if (uid != null) {
            sql += "    AND t2.user_id = " + uid;
        }
        if (StringUtil.isNotBlank(currencyAddress)) {
            sql += "    AND t2.currency_address = '" + currencyAddress + "' ";
            sql += " WHERE 1 = 1";
        }
        if (type != null) {
            sql += "  AND t1.type = " + type;
        }
        if (status != null) {
            sql += "  AND t1.status = " + status;
        }
        return sql;
    }

}
