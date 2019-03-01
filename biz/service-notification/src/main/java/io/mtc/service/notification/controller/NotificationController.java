package io.mtc.service.notification.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.service.notification.entity.Notification;
import io.mtc.service.notification.repository.NotificationRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * 推送消息记录控制器
 *
 * @author Chinhin
 * 2018/7/13
 */
@RequestMapping("/notification")
@RestController
public class NotificationController {

    @Resource
    private NotificationRepository notificationRepository;

    @DeleteMapping("/{id}")
    @Transactional
    public String del(@PathVariable Long id) {
        notificationRepository.deleteById(id);
        return ResultUtil.success();
    }

    @Transactional(readOnly = true)
    @GetMapping("/history")
    public String history(String address, Integer type, Integer pageNumber, Integer pageSize,
                         String order, String sort) {
        PagingModel pageModel = new PagingModel();
        pageModel.setOrder(order);
        pageModel.setSort(sort);
        pageModel.setPageNumber(pageNumber);
        pageModel.setPageSize(pageSize);
        return select(address, type, null, CommonUtil.toJson(pageModel));
    }

    @Transactional(readOnly = true)
    @GetMapping
    public String select(String address, Integer type, String txHash, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<Notification> specification = (Specification<Notification>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(address)) {
                list.add(criteriaBuilder.like(root.get("address"), "%" + address + "%"));
            }
            if (StringUtil.isNotBlank(txHash)) {
                list.add(criteriaBuilder.like(root.get("txHash"), "%" + address + "%"));
            }
            if (type != null) {
                list.add(criteriaBuilder.equal(root.get("type"), type));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(notificationRepository.findAll(specification, pageModel.make()));
    }

}