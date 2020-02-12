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
@RequestMapping("/currency")
public class CurrencyController {

    @Resource
    private ServiceCurrency serviceCurrency;

    @PreAuthorize("hasAuthority('currency:select')")
    @GetMapping
    public String select(String name, Boolean isEnable, Integer baseType, @ModelAttribute PagingModel pageModel) {
        return serviceCurrency.select(name, isEnable, baseType, CommonUtil.toJson(pageModel));
    }

    @PreAuthorize("hasAuthority('currency:insert')")
    @PutMapping
    public String insert(String currencyJson) {
        return serviceCurrency.insert(currencyJson);
    }

    @PreAuthorize("hasAuthority('currency:update')")
    @PostMapping
    public String update(String currencyJson) {
        return serviceCurrency.update(currencyJson);
    }

    /**
     * 改变币种状态
     * @param id 币种id
     * @param type 1:默认可见，2:有效，3：红包支持, 4:托管
     * @return 更新后的币种json字符串
     */
    @PreAuthorize("hasAuthority('currency:update')")
    @PostMapping("/changeStat/{id}")
    public String changeStat(@PathVariable Long id, int type) {
        return serviceCurrency.updateStat(id, type);
    }

    @PreAuthorize("hasAuthority('currency:delete')")
    @DeleteMapping("/{id}")
    public String del(@PathVariable Long id) {
        return serviceCurrency.del(id);
    }

    @PreAuthorize("hasAuthority('currency:select')")
    @GetMapping("/created")
    public String created(String symbol, String ownerAddress, Integer status, @ModelAttribute PagingModel pageModel) {
        return serviceCurrency.backendQueryCreated(symbol, ownerAddress, status, CommonUtil.toJson(pageModel));
    }

}
