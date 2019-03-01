$(function () {
    let url = window.location.href;
    let start = url.lastIndexOf('/') + 1;
    let tag;

    let end = url.indexOf('?');
    if (end === -1) {
        tag = url.substr(start);
    } else {
        tag = url.substr(start, end - start);
    }

    if (tag !== 'register' && tag !== '') {
        $('#promoter').val(tag).attr("disabled", true);
    }

});

// 注册方式 1表示手机，2表示邮箱
let registerIsByPhone = true;
/**
 * 切换注册类型
 * @param isPhone 切换到手机？
 */
function changeType(isPhone) {
    registerIsByPhone = isPhone;
    if (registerIsByPhone) {
        $('#by-phone').addClass('focus');
        $('#by-mail').removeClass('focus');
        $('.just-phone').show();
        $('.just-mail').hide();
    } else {
        $('#by-phone').removeClass('focus');
        $('#by-mail').addClass('focus');
        $('.just-phone').hide();
        $('.just-mail').show();
    }
}

/**
 * 获取验证码
 */
function getCode() {
    if ($('.get-code').hasClass('counting')) {
        return;
    }
    let target;
    if (registerIsByPhone) {
        target = $('#phone').val();
        if (!target) {
            alert('请输入手机号');
            return;
        }
        target = '86' + target;
    } else {
        target = $('#email').val();
        if (!target) {
            alert('请输入邮箱');
            return;
        }
    }
    let param = {
        target: target,
        isPhone: registerIsByPhone,
        langCode: 2
    };
    s.ajax(userWebRoot + '/user/sendCode', 'GET', param, function (result) {
        // 发送成功
        if (result === "OK") {
            countDown();
        }
    });
}

/**
 * 开始倒计时
 */
function countDown() {
    $('.get-code').addClass('counting');
    counting(61);
}
function counting(second) {
    second --;
    if (second < 1) {
        $('.get-code').empty().append('获取验证码').removeClass('counting');
        return;
    } else {
        $('.get-code').empty().append(second + 's');
    }
    s.delay(1000, function () {
        counting(second);
    });
}

/**
 * 执行注册
 */
function doRegister() {
    let target;
    if (registerIsByPhone) {
        target = $('#phone').val();
        if (!target) {
            alert('请输入手机号');
            return;
        }
        target = '86' + target;
    } else {
        target = $('#email').val();
        if (!target) {
            alert('请输入邮箱');
            return;
        }
    }
    // 验证码
    let code = $('#code').val();
    if (!code || code.length !== 6) {
        alert('请输入正确的验证码');
        return;
    }

    let pwd = $('#pwd').val();
    if (pwd.length === 0) {
        alert('请输入请登录密码');
        return;
    }
    if (pwd.length < 8 || pwd.length > 20) {
        alert('登录密码需在8~20位之间');
        return;
    }
    let pwdRe = $('#pwd-re').val();
    if (pwdRe.length === 0) {
        alert('请再次输入密码');
        return;
    }
    if (pwd !== pwdRe) {
        alert('两次输入密码不一致，请确认');
        return;
    }

    // 推荐人
    let promoter = $('#promoter').val();
    if (!promoter) {
        promoter = '';
    }

    let param = {
        target : target,
        isPhone : registerIsByPhone,
        promoter : promoter,
        code : code,
        loginPassword : pwd
    };

    s.ajax(userWebRoot + '/user/register', 'PUT', param, function () {
        s.alertSuccess('注册成功', function () {
            window.location.href = 'http://d.allinpay.io';
        });
    }, function (error) {
        alert(error);
    });
}