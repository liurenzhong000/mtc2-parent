package io.mtc.facade.backend.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.backend.entity.CustomPage;
import io.mtc.facade.backend.repository.CustomPageRepostitory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义页面
 *
 * @author Chinhin
 * 2018/6/20
 */
@RestController
@RequestMapping("/custom")
public class CustomPageController {

    @Resource
    private CustomPageRepostitory customPageRepostitory;

    @PreAuthorize("hasAuthority('custom:insert')")
    @PutMapping
    @Transactional
    public String insert(String title, String content, String linkTag) {
        CustomPage customPage = new CustomPage();
        customPage.setTitle(title);
        customPage.setContent(content);
        customPage.setLinkTag(linkTag);
        customPageRepostitory.save(customPage);
        return ResultUtil.success(customPage);
    }

    @PreAuthorize("hasAuthority('custom:delete')")
    @Transactional
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        customPageRepostitory.deleteById(id);
        return ResultUtil.success("删除成功");
    }

    @PreAuthorize("hasAuthority('custom:update')")
    @Transactional
    @PostMapping
    public String update(Long id, String title, String content, String linkTag) {
        CustomPage customPage = customPageRepostitory.findById(id).get();
        customPage.setTitle(title);
        customPage.setContent(content);
        customPage.setLinkTag(linkTag);
        customPageRepostitory.save(customPage);
        return ResultUtil.success(customPage);
    }

    @PreAuthorize("hasAuthority('custom:select')")
    @Transactional(readOnly = true)
    @GetMapping
    public String select(String title, String linkTag, @ModelAttribute PagingModel pageModel) {
        Specification<CustomPage> specification = (Specification<CustomPage>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(title)) {
                list.add(criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }
            if (StringUtil.isNotBlank(linkTag)) {
                list.add(criteriaBuilder.like(root.get("linkTag"), "%" + linkTag + "%"));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(customPageRepostitory.findAll(specification, pageModel.make()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/detail/{linkTag}")
    public String detail(@PathVariable String linkTag) {
        CustomPage customPage = customPageRepostitory.findByLinkTag(linkTag);
        return ResultUtil.success(customPage);
    }

}
