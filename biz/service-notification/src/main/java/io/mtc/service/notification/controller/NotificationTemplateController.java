package io.mtc.service.notification.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.service.notification.entity.NotificationTemplet;
import io.mtc.service.notification.repository.NotificationTempletRepository;
import io.mtc.service.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * 推送模板控制器
 *
 * @author Chinhin
 * 2018/7/16
 */
@Slf4j
@RequestMapping("/notificationTemplate")
@RestController
public class NotificationTemplateController {

    @Resource
    private NotificationTempletRepository notificationTempletRepository;

    @Resource
    private NotificationService notificationService;

    @PutMapping
    @Transactional
    public String insert(String templateJson) {
        NotificationTemplet template = CommonUtil.fromJson(templateJson, NotificationTemplet.class);
        notificationTempletRepository.save(template);
        return ResultUtil.success(template);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public String del(@PathVariable Long id) {
        notificationTempletRepository.deleteById(id);
        return ResultUtil.success();
    }

    @Transactional
    @PostMapping
    public String update(String templateJson) {
        NotificationTemplet template = CommonUtil.fromJson(templateJson, NotificationTemplet.class);
        NotificationTemplet dbTemplate = notificationTempletRepository.findById(template.getId()).get();
        BeanUtils.copyProperties(template, dbTemplate);
        notificationTempletRepository.save(template);
        return ResultUtil.success(template);
    }

    @Transactional(readOnly = true)
    @GetMapping
    public String select(String title, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<NotificationTemplet> specification = (Specification<NotificationTemplet>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(title)) {
                list.add(criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(notificationTempletRepository.findAll(specification, pageModel.make()));
    }

    @Transactional
    @PostMapping("/push/{id}")
    public String push(@PathVariable Long id) {
        notificationService.push(id);
        return ResultUtil.success();
    }

}
