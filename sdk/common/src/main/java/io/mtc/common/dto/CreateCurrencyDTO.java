package io.mtc.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

/**
 * 发币
 *
 * @author Chinhin
 * 2018/8/15
 */
@ApiModel
@Getter @Setter
public class CreateCurrencyDTO {

    @ApiModelProperty("代币名字")
    private String name;

    @ApiModelProperty("代币简称")
    private String symbol;

    @ApiModelProperty(value = "代币图片", allowEmptyValue = true)
    private String image;

    @ApiModelProperty("发行数量 单位<strong>!不!</strong>是wei")
    private BigInteger supply;

    @ApiModelProperty("官方网站")
    private String website;

    @ApiModelProperty("代币说明")
    private String description;

    @ApiModelProperty("发行代币后，收款地址")
    private String ownerAddress;

    @ApiModelProperty("分类id")
    private Long categoryId;

    @ApiModelProperty(hidden = true)
    private Long uid;

}