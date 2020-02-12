package io.mtc.facade.user.controller;


import io.mtc.common.constants.MTCError;
import io.mtc.common.util.CodecUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.entity.UserKeystore;
import io.mtc.facade.user.repository.UserKeystoreRepository;
import io.mtc.facade.user.repository.UserRepository;
import io.mtc.facade.user.service.SendCodeService;
import io.mtc.facade.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * keystore控制器
 *
 * @author Chinhin
 * 2018/7/25
 */
@Api(description="keystore相关", tags = {"keystore"})
@Transactional(readOnly = true)
@RequestMapping("/keystore")
@RestController
public class KeystoreController {

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserKeystoreRepository userKeystoreRepository;

    @Resource
    private UserService userService;

    @Resource
    private SendCodeService sendCodeService;

    @ApiOperation(value="增加托管账户", notes = "会返回user info", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="currencyType", value = "代币基链类型", required = true, dataType = "int"),
            @ApiImplicitParam(name="keystore", value = "keystore", required = true, dataType = "String"),
            @ApiImplicitParam(name="walletAddress", value = "钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="walletName", value = "钱包名称", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping
    public Object add(@RequestHeader Long uid, int currencyType, String walletAddress, String walletName, String keystore) {
        User user = userRepository.findById(uid).get();

        UserKeystore userKeystore = userKeystoreRepository.findByWalletAddressAndUser(walletAddress, user);
        if (userKeystore == null) {
            userKeystore = new UserKeystore();
        }
        userKeystore.setCurrencyType(currencyType);
        userKeystore.setWalletAddress(walletAddress);
        userKeystore.setWalletName(walletName);
        userKeystore.setKeystore(keystore);
        userKeystore.setUser(user);
        userKeystoreRepository.save(userKeystore);
        return ResultUtil.successObj(userService.userInfo(user));
    }

    @ApiOperation(value="删除托管账户", notes = "会返回user info", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="walletAddress", value = "钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="fundPassword", value = "资金密码", required = true, dataType = "String"),
            @ApiImplicitParam(name="isValidByPhone", value = "true表示用手机号验证的，false表示用邮箱验证的", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="validCode", value = "验证码", required = true, dataType = "String")
    })
    @Transactional
    @DeleteMapping
    public Object del(@RequestHeader Long uid, String walletAddress, String fundPassword, Boolean isValidByPhone, String validCode) {
        User user = userRepository.findById(uid).get();

        // 资金密码验证
        if (!CodecUtil.digestStrSHA1(fundPassword).equals(user.getFundPassword())) {
            return ResultUtil.errorObj(MTCError.FUND_PASSWORD_ERROR);
        }

        // 身份验证 失败
        if (!(isValidByPhone && sendCodeService.checkAndDel(user.getPhone(), validCode))
                && !(!isValidByPhone && sendCodeService.emailCheckAndDel(user.getEmail(), validCode))) {
            return ResultUtil.errorObj(MTCError.VALID_USER_FAILURE);
        }

        UserKeystore userKeystore = userKeystoreRepository.findByWalletAddressAndUser(walletAddress, user);
        if (userKeystore == null) {
            return ResultUtil.errorObj(MTCError.KEYSTORE_INVALID);
        }
        List<UserKeystore> keystores = user.getKeystores();
        for (UserKeystore temp : keystores) {
            if (temp.getWalletAddress().equals(userKeystore.getWalletAddress())) {
                keystores.remove(temp);
                break;
            }
        }
        userRepository.save(user);
        userKeystoreRepository.delete(userKeystore);
        return ResultUtil.successObj(userService.userInfo(user));
    }

    @ApiOperation(value="获取keystore", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="walletAddress", value = "钱包地址", required = true, dataType = "String"),
            @ApiImplicitParam(name="fundPassword", value = "资金密码", required = true, dataType = "String"),
            @ApiImplicitParam(name="isValidByPhone", value = "true表示用手机号验证的，false表示用邮箱验证的", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="validCode", value = "验证码", required = true, dataType = "String")
    })
    @Transactional
    @GetMapping
    public Object restore(@RequestHeader Long uid, String walletAddress, String fundPassword, Boolean isValidByPhone, String validCode) {
        User user = userRepository.findById(uid).get();
        // 资金密码验证
        if (!CodecUtil.digestStrSHA1(fundPassword).equals(user.getFundPassword())) {
            return ResultUtil.errorObj(MTCError.FUND_PASSWORD_ERROR);
        }
        // 身份验证 失败
        if (!(isValidByPhone && sendCodeService.checkAndDel(user.getPhone(), validCode))
                && !(!isValidByPhone && sendCodeService.emailCheckAndDel(user.getEmail(), validCode))) {
            return ResultUtil.errorObj(MTCError.VALID_USER_FAILURE);
        }
        UserKeystore userKeystore = userKeystoreRepository.findByWalletAddressAndUser(walletAddress, user);
        return ResultUtil.successObj(userKeystore.getKeystore());
    }

}
