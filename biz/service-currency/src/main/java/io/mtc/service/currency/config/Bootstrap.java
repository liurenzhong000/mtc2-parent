package io.mtc.service.currency.config;

import io.mtc.common.redis.util.RateCacheUtil;
import io.mtc.common.util.NumberUtil;
import io.mtc.service.currency.entity.Currency;
import io.mtc.service.currency.repository.CurrencyRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 系统启动初始化
 *
 * @author Chinhin
 * 2018/6/11
 */
@Component
public class Bootstrap {

    @Resource
    private CurrencyRepository currencyRepository;

    @Resource
    private RateCacheUtil rateCacheUtil;

    @PostConstruct
    public void initAdminData() {
        // 没有权限，则建一个所有权限的权限
        if (currencyRepository.count() == 0) {
            initCurrency("0", "ethereum",
                    "http://d.mtc.io/apk/eth.jpg", "ETH",
                    new BigDecimal("494.804"), "1000000000000000", 2);

            initCurrency("0x3ac6cb00f5a44712022a51fbace4c7497f56ee31", "M2C Mesh Network",
                    "http://d.mtc.io/apk/coin.jpg", "MESH",
                    new BigDecimal("0"), "20000000000000000", 1);

            initCurrency("0xb91318f35bdb262e9423bc7c7c2a3a93dd93c92c", "nuls",
                    "https://etherscan.io/token/images/nuls28.png", "NULS",
                    new BigDecimal("2.5681"), "5000000000000000000", 2);

            initCurrency("0xcb97e65f07da24d46bcdd078ebebd7c6e6e3d750", "bytom",
                    "https://etherscan.io/token/images/bytom_28.png", "BTM",
                    new BigDecimal("0.57913"), "5000000000000000000", 2);

            initCurrency("0x2cba12a076ba4d922aca5a6a814fc08701a2333c", "foglink",
                    "http://d.mtc.io/apk/foglink.png", "FNKOS",
                    new BigDecimal("0"), "5000000000000000000", 1);

            initCurrency("0xdfdc0d82d96f8fd40ca0cfb4a288955becec2088", "mesh-network",
                    "http://d.mtc.io/apk/mtc.png", "MTC",
                    new BigDecimal("0.0192388"), "50000000000000000000", 2);

            initCurrency("0x557307c5ede5d5d02950aa57682ccfa9719ca998", "LQT",
                    "http://d.mtc.io/apk/yl.jpg", "LQT",
                    new BigDecimal("0.03"), "8000000000000000000", 1);

            initCurrency("0x86Fa049857E0209aa7D9e616F7eb3b3B78ECfdb0", "eos",
                    "http://d.mtc.io/apk/eos.png", "EOS",
                    new BigDecimal("10.6519"), "100000000000000000", 2);

            initCurrency("0x0fed2aca55338d77438797bdf609252db92313ea", "BDB",
                    "http://logo.btcdo.com/btcdo_logo.png", "BDB",
                    new BigDecimal("1"), "50000000000000000000", 1);

            initCurrency("0x9a070232baB8FCB92e81DD7D14c9900602f20094", "BSQL",
                    "", "BSQL",
                    new BigDecimal("0.02"), "50000000000000000000", 1);

            initCurrency("0x9f549ebFD4974cD4eD4A1550D40394B44A7382AA", "lkn",
                    "https://res.linkcoin.pro//avatar/default.png?x-oss-process=image/circle,r_100", "LKN",
                    new BigDecimal("0.0121053"), "50000000000000000000", 2);

            initCurrency("0x20e94867794dba030ee287f1406e100d03c84cd3", "dew",
                    "https://etherscan.io/token/images/dewone_28.png", "DEW",
                    new BigDecimal("0.501763"), "50000000000000000000", 2);
        }
    }


    public void initCurrency(String address, String name, String image,
                                        String shortName, BigDecimal price, String fee,
                                        Integer sourceType) {
        Currency temp = new Currency();
        temp.setAddress(address);
        temp.setName(name);
        temp.setImage(image);
        temp.setShortName(shortName);
        temp.setPrice(price);
        temp.setCnyPrice(NumberUtil.multiply(price, rateCacheUtil.getUSD2CNY()));
        temp.setFee(fee);
        temp.setSourceType(sourceType);
        currencyRepository.save(temp);
    }

}
