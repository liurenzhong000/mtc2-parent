package io.mtc.facade.user.controller;


import io.mtc.common.dto.CreateCurrencyDTO;
import io.mtc.facade.user.service.CreateCurrencyService;
import io.swagger.annotations.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 发币
 *
 * @author Chinhin
 * 2018/7/25
 */
@Api(description="发币相关", tags = {"发币"})
@Transactional(readOnly = true)
@RequestMapping("/createCurrency")
@RestController
public class CreateCurrencyController {

    @Resource
    private CreateCurrencyService createCurrencyService;

    @ApiOperation(value="获取所有分类", tags = {"不需要token"})
    @GetMapping("/categories")
    public Object allCategory() {
        return createCurrencyService.allCategory();
    }

    @ApiOperation(value="获取创建合约手续费", tags = {"需要token"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "<strong>说明：</strong>\n" +
                    "amount: 需要多少个mtc币（单位是wei）\n" +
                    "money: 手续费价值多少美元\n" +
                    "moneyCny：手续费价值多少人民币")
    })
    @GetMapping("/createContractFee")
    public Object createContractFee() {
        return createCurrencyService.createContractFee();
    }

    @ApiOperation(value="发币", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long")
    })
    @PostMapping("/createContract")
    @Transactional
    public Object createContract(@RequestHeader Long uid, @ModelAttribute  CreateCurrencyDTO createCurrencyDTO) {
        return createCurrencyService.createCurrency(uid, createCurrencyDTO);
    }

    @ApiOperation(value="获取发币记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "用户ID", dataType = "Long"),
            @ApiImplicitParam(name = "categoryId", value = "分类ID", dataType = "Long"),
            @ApiImplicitParam(name = "pageNumber", value = "第几页", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条", dataType = "int"),
            @ApiImplicitParam(name = "order", value = "排序，取'ASC'或'DESC'", dataType = "String"),
            @ApiImplicitParam(name = "sort", value = "排序字段，默认id升序", dataType = "String"),
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "<strong>重要字段说明：</strong>\n" +
                "image: 图片\n" +
                "symbol: 简称\n" +
                "website: 网址\n" +
                "description: 描述\n" +
                "supply: 发币数量(不是wei)\n" +
                "tokenAddress: 合约地址\n" +
                "successTime: 成功时间\n" +
                "name: 名字\n" +
                "ownerAddress: 获得币的钱包地址\n" +
                "txHash: 创建合约的交易hash\n" +
                "category: {\n" +
                "　name:分类名\n" +
                "　id:分类id\n" +
                "}\n" +
                "status: 状态 1排队中，2创建中，3创建成功，4创建失败")
    })
    @GetMapping("/list")
    public Object list(Long uid, Long categoryId, Integer pageNumber, Integer pageSize,
                       String order, String sort) {
        return createCurrencyService.list(uid, categoryId, pageNumber, pageSize, order, sort);
    }

}
