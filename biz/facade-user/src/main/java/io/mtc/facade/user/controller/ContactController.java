package io.mtc.facade.user.controller;


import com.alibaba.fastjson.JSONObject;
import io.mtc.common.constants.MTCError;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.user.entity.Contact;
import io.mtc.facade.user.entity.User;
import io.mtc.facade.user.repository.ContactRepository;
import io.mtc.facade.user.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * EOS相关接口
 *
 * @author Chinhin
 * 2018/9/11
 */
@Api(description="联系人", tags = {"联系人"})
@Transactional(readOnly = true)
@RequestMapping("/contact")
@RestController
public class ContactController {

    @Resource
    private UserRepository userRepository;

    @Resource
    private ContactRepository contactRepository;

    @ApiOperation(value="创建联系人", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="photo", value = "头像", dataType = "String"),
            @ApiImplicitParam(name="name", value = "姓名", required = true, dataType = "String"),
            @ApiImplicitParam(name="hostName", value = "托管账户", dataType = "String"),
            @ApiImplicitParam(name="ethAddress", value = "eth钱包地址", dataType = "String"),
            @ApiImplicitParam(name="eosAddress", value = "eos钱包地址", dataType = "String")

    })
    @Transactional
    @PutMapping
    public Object insert(@RequestHeader Long uid, String photo, String name, String hostName,
                                      String ethAddress, String eosAddress) {
        if (StringUtil.isEmpty(name)) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return ResultUtil.errorObj(MTCError.USER_NOT_EXIST);
        }
        if (StringUtil.isNotBlank(name) && name.length() > 200) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        if (StringUtil.isNotBlank(hostName) && hostName.length() > 200) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setPhoto(photo);
        contact.setName(name);
        contact.setHostName(hostName);
        contact.setEthAddress(ethAddress);
        contact.setEosAddress(eosAddress);
        contactRepository.save(contact);
        return ResultUtil.successObj(JSONObject.parse(CommonUtil.toJson(contact)));
    }

    @ApiOperation(value="更新联系人", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="id", value = "联系人id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="photo", value = "头像", dataType = "String"),
            @ApiImplicitParam(name="name", value = "姓名", required = true, dataType = "String"),
            @ApiImplicitParam(name="hostName", value = "托管账户", dataType = "String"),
            @ApiImplicitParam(name="ethAddress", value = "eth钱包地址", dataType = "String"),
            @ApiImplicitParam(name="eosAddress", value = "eos钱包地址", dataType = "String")

    })
    @Transactional
    @PostMapping
    public Object update(@RequestHeader Long uid, Long id, String photo, String name, String hostName,
                         String ethAddress, String eosAddress) {
        Contact contact = contactRepository.findById(id).orElse(null);
        if (contact == null) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 联系人不属于该用户
        if (!contact.getUser().getId().equals(uid)) {
            return ResultUtil.errorObj(MTCError.NO_AUTH);
        }
        if (StringUtil.isNotBlank(name) && name.length() > 200) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        if (StringUtil.isNotBlank(hostName) && hostName.length() > 200) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        contact.setPhoto(photo);
        contact.setName(name);
        contact.setHostName(hostName);
        contact.setEthAddress(ethAddress);
        contact.setEosAddress(eosAddress);
        contactRepository.save(contact);

        return ResultUtil.successObj(JSONObject.parse(CommonUtil.toJson(contact)));
    }

    @ApiOperation(value="删除联系人", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name="id", value = "联系人id", required = true, dataType = "Long")
    })
    @Transactional
    @DeleteMapping("/{id}")
    public Object delete(@RequestHeader Long uid, @PathVariable Long id) {
        Contact contact = contactRepository.findById(id).orElse(null);
        if (contact == null) {
            return ResultUtil.errorObj(MTCError.PARAMETER_INVALID);
        }
        // 联系人不属于该用户
        if (!contact.getUser().getId().equals(uid)) {
            return ResultUtil.errorObj(MTCError.NO_AUTH);
        }
        contactRepository.deleteById(id);
        return ResultUtil.successObj();
    }

    @ApiOperation(value="获取联系人一览", tags = {"需要token"})
    @ApiImplicitParams({
            @ApiImplicitParam(name="uid", value = "用户id", required = true, dataType = "Long")
    })
    @GetMapping
    public Object list(@RequestHeader Long uid) {
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            return ResultUtil.errorObj(MTCError.USER_NOT_EXIST);
        }
        List<Contact> list = contactRepository.findAllByUserOrderByNameAsc(user);
        return ResultUtil.successObj(JSONObject.parse(CommonUtil.toJson(list)));
    }

}
