/**
 * 退出
 */
function logout() {
    s.alert("确定退出", function () {
        s.ajax(s.rootPath("/logout"), "GET", null, function (result) {
            location.href = webRoot + "/login";
        });
    });
}

initMenu();
function initMenu() {

    $('#username').empty().append(s.get("username"));
    $('#roleName').empty().append(s.get("roleName"));

    s.ajax(s.rootPath("/menu"), "GET", null, function (list) {
        let html = '';
        $.each(list, function (i, o) {
            html += '<li>';
            if (o.hasSub) {
                html += '<a href="javascript:;">';
            } else {
                html += '<a class="J_menuItem" href="'+ o.href +'">';
            }
            html += '        <i class="fa '+ o.icon +'"></i>';
            html += '        <span class="nav-label">'+ o.name +'</span>';
            if (o.hasSub) {
                html += '    <span class="fa arrow"></span>';
            }
            html += '    </a>';
            if (o.hasSub) {
                html += '<ul class="nav nav-second-level">';
                $.each(o.subMenus, function (j, b) {
                    html += '<li><a class="J_menuItem" href="'+b.href+'">'+b.name+'</a></li>';
                });
                html += '</ul>';
            }
            html += '</li>';
        });

        $('#side-menu').append(html).metisMenu();
        $.getScript('js/contabs.min.js');
    });
}

/**
 * 修改管理员密码
 */
function updatePwd(){
    let html = "";
    html += '<input type="password" class="form-control" id="oldPwd" placeholder="请输入旧密码...">';
    html += '<input type="password" class="form-control m-t-sm" id="newPwd" placeholder="请输入新密码...">';
    html += '<input type="password" class="form-control m-t-sm" id="confirmPwd" placeholder="请再次输入新密码...">';
    let temp = s.alertHtml("修改密码", html, '', function () {
        let oldPwd = $('#oldPwd').val();
        if (!oldPwd || oldPwd === '') {
            s.tipError("#oldPwd", "请输入旧密码");
            return;
        }
        let newPwd = $('#newPwd').val();
        if (!newPwd || newPwd === '') {
            s.tipError("#newPwd", "请输入新密码");
            return;
        }
        let confirmPwd = $('#confirmPwd').val();
        if (newPwd !== confirmPwd) {
            s.tipError("#confirmPwd", "新密码与确认密码需要一致");
            return;
        }
        s.ajax(s.rootPath("/updatePwd"), "POST", {oldPwd : oldPwd, newPwd : newPwd}, function (result) {
            s.alertSuccess(result);
            layer.close(temp);
        }, function (result) {
            s.alertError(result);
        });
    });
}
/**
 * 修改管理员信息
 */
function updateInfo() {
    s.ajax(s.rootPath("/adminInfo"), "GET", null, function (result) {
        openUpdateInfo(result);
    });
}

/**
 * 修改管理员密码
 */
function openUpdateInfo(dto){
    if (!dto.phone) {
        dto.phone = '';
    }
    if (!dto.email) {
        dto.email = '';
    }
    let html = "";
    html += '<h3>用户名: ' + dto.username + '</h3>';
    html += '<input type="text" class="form-control" id="phone" value="'+dto.phone+'" placeholder="请输入手机号...">';
    html += '<input type="text" class="form-control m-t-sm" id="email" value="'+ dto.email +'" placeholder="请输入邮件...">';
    let temp = s.alertHtml("修改信息", html, '', function () {
        s.ajax(s.rootPath("/updateInfo"), "POST", {phone : $('#phone').val(), email : $('#email').val()}, function (result) {
            s.alertSuccess("修改信息成功");
            layer.close(temp);
        }, function (result) {
            s.alertError(result);
        });
    });
}