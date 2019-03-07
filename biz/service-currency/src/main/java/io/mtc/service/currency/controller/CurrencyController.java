package io.mtc.service.currency.controller;

import io.mtc.common.constants.Constants;
import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.redis.util.RateCacheUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.NumberUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.service.currency.entity.Currency;
import io.mtc.service.currency.repository.CurrencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 币种控制器
 *
 * @author Chinhin
 * 2018/6/15
 */
@Slf4j
@RequestMapping("/currency")
@RestController
public class CurrencyController {

    @Resource
    private CurrencyRepository currencyRepository;

    @Resource
    private RateCacheUtil rateCacheUtil;

    @PutMapping
    @Transactional
    public String insert(String currencyJson) {
        Currency currency = CommonUtil.fromJson(currencyJson, Currency.class);
        currency.setCnyPrice(NumberUtil.multiply(currency.getPrice(), rateCacheUtil.getUSD2CNY()));
        currency.setAddress(currency.getAddress().toLowerCase());

        if (currencyRepository.findByAddress(currency.getAddress()) != null) {
            return ResultUtil.error(MTCError.CURRENCY_ADDRESS_EXIST);
        }

        Currency ether = null;
        if (StringUtil.isEmpty(currency.getImage())) {
            ether = currencyRepository.findByAddress(Constants.ETH_ADDRESS);
            currency.setImage(ether.getImage());
        }
        if (currency.getSourceType() == null) {
            currency.setSourceType(1);
        }
        if (currency.getFee() == null) {
            if (ether == null) {
                ether = currencyRepository.findByAddress(Constants.ETH_ADDRESS);
            }
            currency.setFee(ether.getFee());
        }

        currencyRepository.save(currency);
        return ResultUtil.success(currency);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public String del(@PathVariable Long id) {
        currencyRepository.deleteById(id);
        return ResultUtil.success("删除成功");
    }

    @Transactional
    @PostMapping
    public String update(String currencyJson) {
        Currency currency = CommonUtil.fromJson(currencyJson, Currency.class);
        currency.setAddress(currency.getAddress().toLowerCase());
        Currency dbCurrency = currencyRepository.findById(currency.getId()).get();
        Boolean isDefaultVisible = dbCurrency.getIsDefaultVisible();
        Boolean redPacketEnabled = dbCurrency.getRedPacketEnabled();
        Boolean hostEnabled = dbCurrency.getHostEnabled();
        BeanUtils.copyProperties(currency, dbCurrency);
        dbCurrency.setCnyPrice(NumberUtil.multiply(dbCurrency.getPrice(), rateCacheUtil.getUSD2CNY()));
        dbCurrency.setIsDefaultVisible(isDefaultVisible);
        dbCurrency.setRedPacketEnabled(redPacketEnabled);
        dbCurrency.setHostEnabled(hostEnabled);
        currencyRepository.save(dbCurrency);
        return ResultUtil.success(dbCurrency);
    }

    /**
     * 改变币种状态
     * @param id 币种id
     * @param type 1:默认可见，2:有效, 3:发红包, 4:托管
     * @return 更新后的币种json字符串
     */
    @Transactional
    @PostMapping("/changeStat/{id}")
    public String changeStat(@PathVariable Long id, int type) {
        Currency currency = currencyRepository.findById(id).get();
        if (type == 1) {
            currency.setIsDefaultVisible(!currency.getIsDefaultVisible());
        } else if (type == 3) {
            currency.setRedPacketEnabled(!currency.getRedPacketEnabled());
        } else if (type == 4) {
            currency.setHostEnabled(!currency.getHostEnabled());
        } else {
            currency.setIsEnabled(!currency.getIsEnabled());
        }
        currencyRepository.save(currency);
        return ResultUtil.success(currency);
    }

    @Transactional(readOnly = true)
    @GetMapping
    public String select(String name, Boolean isEnable, Integer baseType, String pageModelStr) {
        PagingModel pageModel;
        if (StringUtil.isBlank(pageModelStr)) {
            pageModel = new PagingModel();
        } else {
            pageModel = CommonUtil.fromJson(pageModelStr, PagingModel.class);
        }
        Specification<Currency> specification = (Specification<Currency>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (StringUtil.isNotBlank(name)) {
                list.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (baseType != null) {
                list.add(criteriaBuilder.equal(root.get("baseType"), baseType));
            }
            if (isEnable != null) {
                list.add(criteriaBuilder.equal(root.get("isEnabled"), isEnable));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        return PagingResultUtil.list(currencyRepository.findAll(specification, pageModel.make()));
    }

    /**
     * 提供给app端显示的
     */
    @Transactional(readOnly = true)
    @GetMapping("/appList")
    public String appList() {
        Iterable<Currency> appList = currencyRepository.findAllByIsEnabled(true, Sort.by(Sort.Direction.ASC, "id"));
        return ResultUtil.success(appList);
    }

    /**
     * 支持红包的币种
     */
    @Transactional(readOnly = true)
    @GetMapping("/redPacketEnableCurrency")
    public String redPacketEnableCurrency() {
        List<Currency> packetEnabled = currencyRepository.findAllByRedPacketEnabled(true);
        return ResultUtil.success(packetEnabled);
    }

    /**
     * 支持托管的币种
     */
    @Transactional(readOnly = true)
    @GetMapping("/hostEnableCurrency")
    public String hostEnableCurrency() {
        List<Currency> hostEnabled = currencyRepository.findAllByHostEnabled(true);
        return ResultUtil.success(hostEnabled);
    }

    /**
     * 获取eth的价格(美元)
     * @return eth的价格(美元)
     */
    @Transactional(readOnly = true)
    @GetMapping("/getEthPrice")
    public BigDecimal getEthPrice() {
        // 获取以太坊
        Currency eth = currencyRepository.findByAddress("0");
        return eth.getPrice();
    }

    /**
     * 根据ether的数量 换成 currency的数量
     * @param currencyAddress 代币地址
     * @param etherNumber ether数量
     * @return 结果
     */
    @Transactional(readOnly = true)
    @GetMapping("/ether2currency")
    public BigInteger ether2currency(String currencyAddress, BigInteger etherNumber) {
        // 代币
        Currency token = currencyRepository.findByAddress(currencyAddress);
        // 代币价格
        BigDecimal tokenPrice = token.getPrice();
        if (token.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return new BigInteger(token.getFee());
        }
        // 以太坊
        Currency eth = currencyRepository.findByAddress("0");
        // 以太坊价格
        BigDecimal etherPrice = eth.getPrice();
        return calcTokenNum(etherNumber, etherPrice, tokenPrice);
    }

    /**
     * 获取对应币种的提现手续费
     * @param currencyAddress 币种地址
     * @return 提现手续费
     */
    @Transactional(readOnly = true)
    @GetMapping("/getWithdrawFee")
    public BigInteger getWithdrawFee(String currencyAddress){
        Currency token = currencyRepository.findByAddress(currencyAddress);
        BigInteger fee = new BigInteger(token.getFee());
        return fee;
    }

    private static BigInteger calcTokenNum(BigInteger etherNumber, BigDecimal etherPrice, BigDecimal tokenPrice) {
        BigInteger tempTokenNum = new BigDecimal(etherNumber).multiply(etherPrice).divide(tokenPrice, RoundingMode.UP).toBigInteger();
        BigDecimal tokenNum = new BigDecimal(tempTokenNum).divide(new BigDecimal(Constants.CURRENCY_UNIT), RoundingMode.UP);
        return tokenNum.multiply(new BigDecimal(Constants.CURRENCY_UNIT)).toBigInteger();
    }

}
