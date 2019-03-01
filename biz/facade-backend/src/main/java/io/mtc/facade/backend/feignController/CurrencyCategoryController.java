package io.mtc.facade.backend.feignController;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.backend.feign.ServiceCurrency;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 币种控制器
 *
 * @author Chinhin
 * 2018/6/16
 */
@RestController
@RequestMapping("/currencyCategory")
public class CurrencyCategoryController {

    @Resource
    private ServiceCurrency serviceCurrency;

    @PreAuthorize("hasAuthority('currency:select')")
    @GetMapping
    public String selectCategory(String name, @ModelAttribute PagingModel pageModel) {
        return serviceCurrency.selectCategory(name, CommonUtil.toJson(pageModel));
    }

    @PreAuthorize("hasAuthority('currency:insert')")
    @PutMapping
    public String insertCategory(String categoryJson) {
        return serviceCurrency.insertCategory(categoryJson);
    }

    @PreAuthorize("hasAuthority('currency:update')")
    @PostMapping
    public String updateCategory(String categoryJson) {
        return serviceCurrency.updateCategory(categoryJson);
    }

    @PreAuthorize("hasAuthority('currency:delete')")
    @DeleteMapping("/{id}")
    public String delCategory(@PathVariable Long id) {
        return serviceCurrency.delCategory(id);
    }

}
