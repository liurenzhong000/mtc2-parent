package io.mtc.service.currency.controller;

import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.dto.CreateCurrencyDTO;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.service.currency.entity.Category;
import io.mtc.service.currency.entity.CreateCurrency;
import io.mtc.service.currency.repository.CategoryRepository;
import io.mtc.service.currency.repository.CreateCurrencyRepository;
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
 * 发币控制器
 *
 * @author Chinhin
 * 2018/8/15
 */
@Slf4j
@RequestMapping("/createCurrency")
@RestController
public class CreateCurrencyController {

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private CreateCurrencyRepository createCurrencyRepository;

    @PutMapping
    @Transactional
    public String insert(@RequestBody CreateCurrencyDTO createCurrencyDTO) {
        CreateCurrency createCurrency = new CreateCurrency();
        BeanUtils.copyProperties(createCurrencyDTO, createCurrency);

        Category category = categoryRepository.findById(createCurrencyDTO.getCategoryId()).orElse(null);
        if (category == null) {
            return ResultUtil.error(MTCError.PARAMETER_INVALID);
        }
        createCurrency.setCategory(category);
        createCurrencyRepository.save(createCurrency);
        return ResultUtil.success(createCurrency.getId());
    }

    @Transactional(readOnly = true)
    @GetMapping
    public String select(Long uid, Long categoryId, @ModelAttribute PagingModel pageModel) {
        // 设置默认排序
        if (StringUtil.isBlank(pageModel.getSort())) {
            pageModel.setOrder("ASC");
            pageModel.setSort("id");
        }
        Specification<CreateCurrency> specification = (Specification<CreateCurrency>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (uid != null) {
                list.add(criteriaBuilder.equal(root.get("uid"), uid));
            }
            if (categoryId != null) {
                list.add(criteriaBuilder.equal(root.get("category"), categoryId));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(createCurrencyRepository.findAll(specification, pageModel.make()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/backendQuery")
    public String select4backend(String symbol, String ownerAddress, Integer status, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<CreateCurrency> specification = (Specification<CreateCurrency>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(symbol)) {
                list.add(criteriaBuilder.like(root.get("symbol"), "%" + symbol + "%"));
            }
            if (StringUtil.isNotBlank(ownerAddress)) {
                list.add(criteriaBuilder.like(root.get("ownerAddress"), "%" + ownerAddress + "%"));
            }
            if (status != null) {
                list.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(createCurrencyRepository.findAll(specification, pageModel.make()));
    }

}