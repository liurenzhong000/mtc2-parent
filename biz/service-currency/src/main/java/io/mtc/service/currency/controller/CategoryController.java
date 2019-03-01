package io.mtc.service.currency.controller;

import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.service.currency.entity.Category;
import io.mtc.service.currency.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类控制器
 *
 * @author Chinhin
 * 2018/8/15
 */
@Slf4j
@RequestMapping("/category")
@RestController
public class CategoryController {

    @Resource
    private CategoryRepository categoryRepository;

    @PutMapping
    @Transactional
    public String insert(String categoryJson) {
        Category category = CommonUtil.fromJson(categoryJson, Category.class);
        if (StringUtil.isEmpty(category.getName())) {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        Category dbCategory = categoryRepository.findByName(category.getName());
        if (dbCategory != null) {
            return ResultUtil.error("该分类已存在");
        }
        categoryRepository.save(category);
        return ResultUtil.success(category);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public String del(@PathVariable Long id) {
        Category category = categoryRepository.findById(id).get();
        if (category.getCurrencies().size() > 0) {
            return ResultUtil.error("请先删除该分类下的代币");
        }
        categoryRepository.deleteById(id);
        return ResultUtil.success("删除成功");
    }

    @Transactional
    @PostMapping
    public String update(String categoryJson) {
        Category category = CommonUtil.fromJson(categoryJson, Category.class);
        Category dbCategory = categoryRepository.findById(category.getId()).get();
        BeanUtils.copyProperties(category, dbCategory);
        categoryRepository.save(dbCategory);
        return ResultUtil.success(dbCategory);
    }

    @Transactional(readOnly = true)
    @GetMapping
    public String select(String name, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<Category> specification = (Specification<Category>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(name)) {
                list.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(categoryRepository.findAll(specification, pageModel.make()));
    }

    /**
     * 获取所有的分类
     * @return 所有分类集合
     */
    @Transactional(readOnly = true)
    @GetMapping("/all")
    public String all() {
        List<Category> result = new ArrayList<>();
        categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).forEach(result::add);
        return ResultUtil.success(result);
    }

}
