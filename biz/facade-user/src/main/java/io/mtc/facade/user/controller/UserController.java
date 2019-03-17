package io.mtc.facade.user.controller;

import io.mtc.common.constants.Constants;
import io.mtc.common.constants.MTCError;
import io.mtc.common.oss.util.FileUtil;
import io.mtc.common.redis.constants.RedisKeys;
import io.mtc.common.redis.util.RedisUtil;
import io.mtc.common.util.CodecUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.repository.UserRepository;
import io.mtc.facade.user.service.SendCodeService;
import io.mtc.facade.user.service.UserService;
import io.mtc.facade.user.util.RongyunUtil;
import io.mtc.facade.user.util.UserDefaultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理相关
 *
 * @author Chinhin
 * 2018/7/23
 */
@Api(description="用户资料相关", tags = {"用户"})
@Transactional(readOnly = true)
@RequestMapping("/user")
@RestController
@Slf4j
public class UserController {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SendCodeService sendCodeService;

    @Resource
    private UserService userService;

    @ApiOperation(value="发送验证码", notes="target需要输入的时候用此接口", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="target", value = "目标，邮箱或手机号,手机号需要加上国际区号 如 8613900000000", required = true, dataType = "String"),
            @ApiImplicitParam(name="isPhone", value = "true表示发送手机，false表示发送邮箱", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="langCode", value = "语言编号(1:英文, 2:中文, 3:韩文)", required = true, dataType = "int")
    })
    @Transactional
    @GetMapping("/sendCode")
    public Object sendCode(String target, Boolean isPhone, int langCode) {
        log.info("sendCode --> target={}, isPhone={}", target, isPhone);
        if (isPhone) {
            return sendCodeService.sendCode(target, langCode);
        } else {
            return sendCodeService.emailSendCode(target, langCode);
        }
    }

    @ApiOperation(value="发送验证码", notes="target需要输入的时候用此接口", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="target", value = "目标，邮箱或手机号,手机号需要加上国际区号 如 8613900000000", required = true, dataType = "String"),
            @ApiImplicitParam(name="isPhone", value = "true表示发送手机，false表示发送邮箱", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="langCode", value = "语言编号(1:英文, 2:中文, 3:韩文)", required = true, dataType = "int")
    })
    @Transactional
    @GetMapping("/sendCode2/{target}")
    public Object sendCode2(@PathVariable String target, Boolean isPhone, int langCode) {
        log.info("sendCode --> target={}, isPhone={}", target, isPhone);
        if (isPhone) {
            return sendCodeService.sendCode(target, langCode);
        } else {
            return sendCodeService.emailSendCode(target, langCode);
        }
    }

    @ApiOperation(value="验证验证码是否正确", notes="target需要输入的时候用此接口", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="target", value = "目标，邮箱或手机号", required = true, dataType = "String"),
            @ApiImplicitParam(name="isPhone", value = "true表示发送手机，false表示发送邮箱", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="code", value = "验证码", required = true, dataType = "String")
    })
    @GetMapping("/validCode")
    public Object validCode(String target, String code, Boolean isPhone) {
        // 验证码check成功
        if (isPhone && !sendCodeService.check(target, code)
                || !isPhone && !sendCodeService.emailCheck(target, code)) {
            return ResultUtil.errorObj(MTCError.VALID_CODE_INVALID);
        } else {
            return ResultUtil.successObj();
        }
    }

    @ApiOperation(value="验证手机号或邮箱是否注册过", notes="注册个返回true，没注册过返回false", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="target", value = "目标，邮箱或手机号", required = true, dataType = "String"),
            @ApiImplicitParam(name="isPhone", value = "true表示发送手机，false表示发送邮箱", required = true, dataType = "Boolean")
    })
    @GetMapping("/exist")
    public Object exist(String target, Boolean isPhone) {
        if (isPhone) {
            User byPhone = userRepository.findByPhone(target);
            return ResultUtil.successObj(byPhone != null);
        } else {
            User byEmail = userRepository.findByEmail(target);
            return ResultUtil.successObj(byEmail != null);
        }
    }

    @ApiOperation(value="注册", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="target", value = "目标，邮箱或手机号", required = true, dataType = "String"),
            @ApiImplicitParam(name="loginPassword", value = "登录密码(前端加密)", required = true, dataType = "String"),
            @ApiImplicitParam(name="isPhone", value = "true表示发送手机，false表示发送邮箱", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="code", value = "验证码", required = true, dataType = "String"),
            @ApiImplicitParam(name="promoter", value = "推广人(手机号/邮箱), 没有请传null或空串", dataType = "String"),
    })
    @Transactional
    @PutMapping("/register")
    public Object register(String target, String loginPassword, String code, Boolean isPhone, String promoter) {
        log.info("register --> target={}, code={}, isPhone={}, promoter={}", target, code, isPhone, promoter);
        // 验证码check
        if (isPhone && !sendCodeService.checkAndDel(target, code)
                || !isPhone && !sendCodeService.emailCheckAndDel(target, code)) {
            return ResultUtil.errorObj(MTCError.VALID_CODE_INVALID);
        }
        // 去重check
        User user;
        if (isPhone) {
            user = userRepository.findByPhone(target);
            if (user != null) {
                return ResultUtil.errorObj(MTCError.PHONE_REGISTERED);
            }
        } else {
            user = userRepository.findByEmail(target);
            if (user != null) {
                return ResultUtil.errorObj(MTCError.EMAIL_REGISTERED);
            }
        }
        user = new User();
        if (isPhone) {
            user.setPhone(target);
        } else {
            user.setEmail(target);
        }
        if (StringUtil.isNotEmpty(promoter)) {
            // 推广人
            User promoterUser;
            // 邮箱登录
            if (StringUtil.checkEmail(promoter)) {
                promoterUser = userRepository.findByEmail(promoter);
            } else {// 手机号登录
                promoterUser = userRepository.findByPhone(promoter);
                if (promoterUser == null) {
                    promoter = "86" + promoter;
                    promoterUser = userRepository.findByPhone(promoter);
                }
            }
            if (promoterUser == null) {
                return ResultUtil.errorObj(MTCError.INVITER_USER_NOT_EXIST);
            }
            user.setPromoter(promoter);
        } else {
//            user.setPromoter(UserDefaultUtil.defaultPromoter()); //去掉默认推荐（20190304）
            return ResultUtil.errorObj(MTCError.INVITER_USER_NOT_EXIST);
        }
        user.setNick(UserDefaultUtil.getNick());
        user.setPhoto(UserDefaultUtil.getRandomHead());
        user.setLoginPassword(CodecUtil.digestStrSHA1(loginPassword));
        userRepository.save(user);
        return ResultUtil.successObj();
    }

    @ApiOperation(value="找回并重置登录密码", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="target", value = "目标，邮箱或手机号", required = true, dataType = "String"),
            @ApiImplicitParam(name="pwd", value = "新密码", required = true, dataType = "String"),
            @ApiImplicitParam(name="isValidByPhone", value = "是否用手机号找回,false表示用邮箱", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="validCode", value = "验证码", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping("/findLoginPwd")
    public Object findLoginPwd(String target, String pwd, Boolean isValidByPhone, String validCode) {
        // 验证码验证 失败
        if (!(isValidByPhone && sendCodeService.checkAndDel(target, validCode))
                && !(!isValidByPhone && sendCodeService.emailCheckAndDel(target, validCode))) {
            return ResultUtil.errorObj(MTCError.VALID_CODE_INVALID);
        }
        User user;
        if (isValidByPhone) {
            user = userRepository.findByPhone(target);
        } else {
            user = userRepository.findByEmail(target);
        }
        if (user == null) {
            return ResultUtil.errorObj(MTCError.USER_NOT_EXIST);
        }
        user.setLoginPassword(CodecUtil.digestStrSHA1(pwd));
        user.setCanWithdrawTime(DateUtil.plusSeconds(new Date(), 60 * 60 * 24));
        userRepository.save(user);
        return ResultUtil.successObj();
    }

    @ApiOperation(value="登录", notes = "会返回user info", tags = {"不需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="target", value = "目标，邮箱或手机号", required = true, dataType = "String"),
            @ApiImplicitParam(name="loginPassword", value = "登录密码(前端加密)", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping("/login")
    public Object login(String target, String loginPassword) {
        User user;
        // 邮箱登录
        if (StringUtil.checkEmail(target)) {
            user = userRepository.findByEmailAndLoginPassword(target, CodecUtil.digestStrSHA1(loginPassword));
        // 手机号登录
        } else {
            user = userRepository.findByPhoneAndLoginPassword(target, CodecUtil.digestStrSHA1(loginPassword));
        }
        if (user == null) {
            return ResultUtil.errorObj(MTCError.USERNAME_OR_PWD_ERROR);
        }
        user.setToken(StringUtil.getRandomString(16));
        redisUtil.set(RedisKeys.USER_TOKEN(user.getId().toString()), user.getToken());
        userRepository.save(user);

        return ResultUtil.successObj(userService.userInfo(user));
    }

    @ApiOperation(value="获取用户信息", tags = {"需要token"}, notes = "<b>user info:</b> \n" +
            "    nick: 昵称,\n" +
            "    canWithdrawTime: 可以提现的时间(重置密码后，要24小时候才可以提现) 0表示不限,\n" +
            "    uid: 用户id,\n" +
            "    phone: 手机号加密（未绑定返回空串）,\n" +
            "    hasFundPassword: 是否有资金密码 boolean,\n" +
            "    photo: 头像,\n" +
            "    email: 邮件地址加密（未绑定返回空串）,\n" +
            "    token: token,\n" +
            "    keystores: [\n" +
            "      {\n" +
            "        　currencyType: 代币基链类型,\n" +
            "        　walletName: 钱包名称,\n" +
            "        　walletAddress: 钱包地址\n" +
            "      }...]")
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
    })
    @GetMapping("/userInfo")
    public Object userInfo(@RequestHeader Long uid) {
        User user = userRepository.findById(uid).get();
        return ResultUtil.successObj(userService.userInfo(user));
    }

    @ApiOperation(value="更新资金密码", notes = "会返回user info", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="fundPassword", value = "新资金密码(前端加密)", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping("/updateFundPassword")
    public Object updateFundPassword(@RequestHeader Long uid, String fundPassword) {
        if (StringUtil.isBlank(fundPassword)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        User user = userRepository.findById(uid).get();
        if (StringUtil.isNotBlank(user.getFundPassword())) {
            return ResultUtil.errorObj(MTCError.FUND_PASSWORD_EXIST);
        }
        user.setFundPassword(CodecUtil.digestStrSHA1(fundPassword));
        userRepository.save(user);
        return ResultUtil.successObj(userService.userInfo(user));
    }

    @ApiOperation(value="更新昵称", notes = "会返回user info", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="nick", value = "昵称", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping("/updateNick")
    public Object updateNick(@RequestHeader Long uid, String nick) {
        if (StringUtil.isBlank(nick)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        User user = userRepository.findById(uid).get();
        user.setNick(nick);
        userRepository.save(user);
        return ResultUtil.successObj(userService.userInfo(user));
    }

    @ApiOperation(value="更新头像", notes = "会返回user info", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="photo", value = "照片链接全路径", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping("/updatePhoto")
    public Object updatePhoto(@RequestHeader Long uid, String photo) {
        if (StringUtil.isBlank(photo)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        User user = userRepository.findById(uid).get();
        user.setPhoto(photo);
        userRepository.save(user);
        return ResultUtil.successObj(userService.userInfo(user));
    }


    @ApiOperation(value="发送验证码 (对已经绑定的邮箱或手机进行验证使用)", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="langCode", value = "语言编号(1:英文, 2:中文, 3:韩文)", required = true, dataType = "int"),
            @ApiImplicitParam(name="isPhone", value = "true表示发送手机，false表示发送邮箱", required = true, dataType = "Boolean")
    })
    @Transactional
    @GetMapping("/sendUserCode")
    public Object sendUserCode(@RequestHeader Long uid, Boolean isPhone, int langCode) {
        User user = userRepository.findById(uid).get();
        if (isPhone) {
            return sendCodeService.sendCode(user.getPhone(), langCode);
        } else {
            return sendCodeService.emailSendCode(user.getEmail(), langCode);
        }
    }

    @ApiOperation(value="验证验证码 (对已经绑定的邮箱或手机进行验证使用)", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="code", value = "验证码", required = true, dataType = "String"),
            @ApiImplicitParam(name="isPhone", value = "true表示发送手机，false表示发送邮箱", required = true, dataType = "Boolean")
    })
    @Transactional
    @GetMapping("/validUserCode")
    public Object validUserCode(@RequestHeader Long uid, String code, Boolean isPhone) {
        User user = userRepository.findById(uid).get();
        // 验证码check成功
        if (isPhone && !sendCodeService.check(user.getPhone(), code)
                || !isPhone && !sendCodeService.emailCheck(user.getEmail(), code)) {
            return ResultUtil.errorObj(MTCError.VALID_CODE_INVALID);
        } else {
            return ResultUtil.successObj();
        }
    }

    @ApiOperation(value="绑定", notes = "成功会返回user info，失败会返回身份验证错误或绑定验证码验证错误，流程举例说明, 在绑定手机页面:：\n" +
            "1.需要先验证邮箱，则调用 sendUserCode?isPhone=false\n" +
            "2.点下一步的时候，调用 validCode?isPhone=false&code=c1\n" +
            "3.然后发送手机验证码，调用 sendCode?isPhone=true, 得到用户填写的手机验证码c2\n" +
            "4.最后调用 bind?isPhone=true&validCode=c1&target=phoneNum&bindCode=c2\n" +
            "5.如果手机号验证码错误，会返回101错误，如果邮箱验证码错误会返回105错误", tags = {"需要token"}
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="validCode", value = "验证的code", required = true, dataType = "String"),
            @ApiImplicitParam(name="bindCode", value = "需要绑定的code", required = true, dataType = "String"),
            @ApiImplicitParam(name="target", value = "需要绑定的邮箱或phone", required = true, dataType = "String"),
            @ApiImplicitParam(name="isPhone", value = "true表示发送手机，false表示发送邮箱", required = true, dataType = "Boolean")
    })
    @Transactional
    @PostMapping("/bind")
    public Object bind(@RequestHeader Long uid, Boolean isPhone, String validCode, String target, String bindCode) {
        User user = userRepository.findById(uid).get();
        // 绑定手机
        if (isPhone) {
            // 判断用户是否已被绑定
            User existUser = userRepository.findByPhone(target);
            if (existUser != null) {
                return ResultUtil.errorObj(MTCError.PHONE_REGISTERED);
            }
            // 验证手机验证码 正确
            if (sendCodeService.checkAndDel(target, bindCode)) {
                // 邮箱验证 正确
                if (sendCodeService.emailCheckAndDel(user.getEmail(), validCode)) {
                    user.setPhone(target);
                    userRepository.save(user);
                    return ResultUtil.successObj(userService.userInfo(user));
                }
                return ResultUtil.errorObj(MTCError.VALID_USER_FAILURE);
            }
        } else {
            // 判断用户是否已被绑定
            User existUser = userRepository.findByEmail(target);
            if (existUser != null) {
                return ResultUtil.errorObj(MTCError.EMAIL_REGISTERED);
            }
            // 邮箱验证 正确
            if (sendCodeService.emailCheckAndDel(target, bindCode)) {
                // 手机验证 正确
                if (sendCodeService.checkAndDel(user.getPhone(), validCode)) {
                    user.setEmail(target);
                    userRepository.save(user);
                    return ResultUtil.successObj(userService.userInfo(user));
                }
                return ResultUtil.errorObj(MTCError.VALID_USER_FAILURE);
            }
        }
        return ResultUtil.errorObj(MTCError.VALID_CODE_INVALID);
    }

    @ApiOperation(value="修改密码", notes = "会返回user info", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="isReset", value = "true表示重置，false表示修改(要传oldPwd)", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="isLoginPwd", value = "true表示登录密码，false表示资金密码", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="oldPwd", value = "旧密码，当isRest为false时需要", required = true, dataType = "String"),
            @ApiImplicitParam(name="pwd", value = "新密码", required = true, dataType = "String"),
            @ApiImplicitParam(name="isValidByPhone", value = "true表示用手机号验证的，false表示用邮箱验证的", required = true, dataType = "Boolean"),
            @ApiImplicitParam(name="validCode", value = "验证码", required = true, dataType = "String")
    })
    @Transactional
    @PostMapping("/updatePwd")
    public Object updatePwd(@RequestHeader Long uid, Boolean isReset, Boolean isLoginPwd, String oldPwd, String pwd, Boolean isValidByPhone, String validCode) {
        User user = userRepository.findById(uid).get();
        // 非重置情况下，对旧密码验证
        if (isReset == null || !isReset) {
            if (isLoginPwd) {
                if (!user.getLoginPassword().equals(CodecUtil.digestStrSHA1(oldPwd))) {
                    return ResultUtil.errorObj(MTCError.OLD_PWD_ERROR);
                }
            } else {
                if (!user.getFundPassword().equals(CodecUtil.digestStrSHA1(oldPwd))) {
                    return ResultUtil.errorObj(MTCError.OLD_PWD_ERROR);
                }
            }
        }
        // 身份验证 失败
        if (!(isValidByPhone && sendCodeService.checkAndDel(user.getPhone(), validCode))
                && !(!isValidByPhone && sendCodeService.emailCheckAndDel(user.getEmail(), validCode))) {
            return ResultUtil.errorObj(MTCError.VALID_USER_FAILURE);
        }

        if (isLoginPwd) {
            user.setLoginPassword(CodecUtil.digestStrSHA1(pwd));
        } else {
            user.setFundPassword(CodecUtil.digestStrSHA1(pwd));
        }
        // 重置密码，24小时不能提现
        if (isReset != null && isReset) {
            user.setCanWithdrawTime(DateUtil.plusSeconds(new Date(), 60 * 60 * 24));
        }
        return ResultUtil.successObj(userService.userInfo(user));
    }

    @ApiOperation(value="上传文件", notes = "成功会返回文件全路径", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="file", value = "文件的参数名", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name="folderType", value = "文件夹类型，1表示photo(默认), 2申请代币的图片", dataType = "Integer")
    })
    @Transactional
    @PostMapping("/upload")
    public Object upload(@RequestParam("file") MultipartFile file, Integer folderType) {
        InputStream is;
        String fileName;
        String filePath;
        String folder;
        if (folderType == null) {
            folderType = 1;
        }
        switch (folderType) {
            case 1:
                folder = "photo";
                break;
            case 2:
                folder = "applyCurrency";
                break;
            default:
                folder = "photo";
                break;
        }
        try {
            if (file == null) {
                throw new IOException("文件未获取到");
            }
            // 取得文件的原始文件名称
            fileName = file.getOriginalFilename();

            if(StringUtil.isNotBlank(fileName)){
                is = file.getInputStream();
                fileName = FileUtil.reName(fileName);
                filePath = FileUtil.saveFile(fileName, is, folder, Constants.EMPTY);
            } else {
                throw new IOException("文件名为空");
            }
            return ResultUtil.successObj(Constants.ALI_OSS_URI + filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.errorObj(MTCError.UPLOAD_ERROR);
        }
    }

    @ApiOperation(value="刷新融云的token，调用后在userInfo里面也可以看到", notes = "用户注册后默认是没有token的", tags = {"需要token"}
    )
    @ApiImplicitParams({
        @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
    })
    @Transactional
    @PostMapping("/rongyunToken")
    public Object rongyunToken(@RequestHeader Long uid) throws Exception {
        User user = userRepository.findById(uid).get();
        String token = RongyunUtil.getToken(user);
        user.setRongyunToken(token);
        userRepository.save(user);
        return ResultUtil.successObj(token);
    }

    @ApiOperation(value="检索用户", notes = "通过手机号或邮箱查询用户的uid", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="target", value = "目标，邮箱或手机号", required = true, dataType = "String")
    })
    @GetMapping("/findUser")
    public Object findUser(Long uid, String target) {
        User user = userService.findUser(target);
        if (user == null) {
            return ResultUtil.errorObj(MTCError.USER_NOT_EXIST);
        }
        return ResultUtil.successObj(user.getId());
    }

    @ApiOperation(value="获取用户信息", notes = "通过手机号或邮箱查询用户的uid", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="userId", value = "对应用户id", required = true, dataType = "Long"),
    })
    @GetMapping("/otherUserInfo")
    public Object otherUserInfo(Long uid, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResultUtil.errorObj(MTCError.USER_NOT_EXIST);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("nick", user.getNick());
        result.put("photo", user.getPhoto());
        result.put("id", user.getId());
        return ResultUtil.successObj(result);
    }

}
