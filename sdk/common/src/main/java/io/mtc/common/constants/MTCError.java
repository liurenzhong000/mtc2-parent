package io.mtc.common.constants;

/**
 * json错误
 *
 * @author Chinhin
 * 2018/6/22
 */
public enum  MTCError {

    SEND_CODE_WAIT(99, "请30秒后再试"),
    PARAMETER_INVALID(100, "参数错误"),
    VALID_CODE_INVALID(101, "无效的验证码"),
    PHONE_REGISTERED(102, "手机号已注册"),
    USER_NOT_EXIST(103, "用户不存在"),
    FUND_PASSWORD_EXIST(104, "已设置过资金密码"),
    VALID_USER_FAILURE(105, "验证身份信息失败"),
    OLD_PWD_ERROR(106, "旧密码错误"),
    UPLOAD_ERROR(107, "上传错误"),
    FUND_PASSWORD_ERROR(108, "资金密码错误"),
    SEND_VERIFY_ERROR(109, "发送验证码失败"),
    SEND_VERIFY_OVER_LIMIT(110, "发送验证码失败，超过每日上限次数"),
    DEPOSIT_DEST_ERROR(111, "充值收款地址错误"),
    EMAIL_REGISTERED(112, "该邮箱已注册"),
    CAN_WITHDRAW_TIME_NOT_REACH(113, "重置密码后，需要24小时候才能提现或转账"),
    BALANCE_NOT_ENOUGH(114, "余额不足"),
    CURRENCY_NOT_ENABLE_ENVELOPE(115, "当前代币不支持红包"),
    ENVELOPE_UPDATE_NO_AUTH(116, "当前用户不拥有此红包"),
    ENVELOPE_ENDED(117, "红包已结束"),
    KEYSTORE_INVALID(118, "Keystore不存在"),
    ENVELOPE_END_WAIT(119, "有正在处理的交易，请稍后重试"),
    ENVELOPE_GRABBED(120, "已抢过该红包"),
    BILL_UPDATE_NO_AUTH(121, "当前用户不拥有此账单"),
    FUND_PASSWORD_NOT_EXIST(122, "请先设置资金密码"),
    RED_ENVELOP_AMOUNT_TOO_LOW(123, "发送的红包金额太小"),
    USERNAME_OR_PWD_ERROR(124, "用户名或密码错误"),
    CONTRACT_ADDRESS_WRONG(125, "请输入正确的合约地址"),
    INVITER_USER_NOT_EXIST(126, "邀请人不存在"),
    INVITER_USER_CANT_BE_SELF(127, "邀请人不能为自己"),
    TRANSFER_TARGET_USER_CANT_BE_SELF(128, "收款方不能为自己"),

    REQUEST_ENDPOINT_ERROR(300, "请求节点错误"),
    CURRENCY_ADDRESS_EXIST(301, "平台有相同代币地址"),
    CURRENCY_NOT_EXIST(302, "代币不存在"),
    SYSTEM_BUSY(400, "系统繁忙，请稍后再试"),
    NO_AUTH(401, "没有权限"),

    USER_WHEEL_NUM_NOT_ENOUTH(501, "用户抽转盘次数不足"),
    REQUEST_WEB3J_ERROR(600, "此方法请不要用web3j来获取，有提供接口"),
    UTXO_INPUT_NOT_EXIST(1001, "输入UTXO不存在");

    private final Integer key;
    private final String value;

    MTCError(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
