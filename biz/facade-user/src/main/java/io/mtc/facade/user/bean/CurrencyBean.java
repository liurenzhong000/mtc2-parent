package io.mtc.facade.user.bean;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @Auther: hyp
 * @Date: 2019/3/8 15:39
 * @Description: Currency类对应传输
 */
@Setter
@Getter
public class CurrencyBean {
    /**地址*/
    private String address;

    /**名称*/
    private String name;

    /**图片地址*/
    private String image;

    /**简称*/
    private String shortName;

    /**市场价（美元）*/
    private BigDecimal price;

    /**市场价（人民币）*/
    private BigDecimal cnyPrice;

    /**提现手续费*/
    private String fee;

    /**超过多少要进行汇总*/
    private String outQtyToMainAddress;

    /**来源类型 1:本地，2:block.cc，3:交易所*/
    private Integer sourceType;

    /**来源Key*/
    private String sourceSystemId;

    /**基链类型 1:eth, 2:bch, 3:eos，4:btc*/
    private Integer baseType = 1;

    /**是否默认显示*/
    private Boolean isDefaultVisible = false;

    /**是否有效*/
    private Boolean isEnabled = true;

    /**是否支持发红包*/
    private Boolean redPacketEnabled = false;

    /**是否支持托管*/
    private Boolean hostEnabled = false;

    /**备注说明*/
    private String note;

    /**涨跌(小时)*/
    private BigDecimal changeHourly = BigDecimal.ZERO;
}
