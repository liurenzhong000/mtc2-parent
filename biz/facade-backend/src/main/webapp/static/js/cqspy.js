/**
 * 弹窗相关集成
 * 依赖于jquery、layer.js、sweetalert
 */
let s = {
    /**
     * 获取根路径
     * @param appendPath 需要拼接的url,不传则获取根路径
     * @returns string
     */
    rootPath : function(appendPath){
        if (typeof(appendPath) !== 'undefined'){
            return (webRoot + appendPath).toString();
        }
        return webRoot;
    },
    limitLength: function(str, limit) {
        if (!str) {
            return '';
        }
        let text = str;
        if (text.length > limit) {
            text = text.substring(0, limit)+"...";
        }
        return text;
    },
    copy : function(obj){
        let newobj = {};
        for ( let attr in obj) {
            newobj[attr] = obj[attr];
        }
        return newobj;
    },
    deepCopy : function(obj) {
        if(typeof obj !== 'object'){
            return obj;
        }
        let newobj = {};
        for (let attr in obj) {
            newobj[attr] = s.deepCopy(obj[attr]);
        }
        return newobj;
    },
    log : function(obj, tag) {
        if (typeof(tag) !== 'undefined') {
            console.log(new Date() + " : " + tag + " : " + s.json2str(obj));
        } else {
            console.log(s.json2str(obj));
        }
    },
    /**
     * obj转string，便于打日志
     * @param obj 要转的对象
     * @returns {string} string
     */
    json2str: function(obj) {
        return JSON.stringify(obj);
    },
    str2json: function(str) {
        return JSON.parse(str);
    },
    /**
     * 延迟执行某个方法
     * @param time 时间,毫秒
     * @param callback 回调函数
     */
    delay : function (time, callback) {
        setTimeout(function(){
            callback();
        }, time);
    },
    /**
     * 打开加载
     * @returns layer对象
     */
    load : function(){
        return layer.load();
    },
    /**
     * 清除弹窗
     * @param layerId layer对象
     */
    clear : function(layerId){
        layer.close(layerId);
    },
    /**
     * 打开一个链接并加载到主框架中
     * @param title 标题
     * @param url 请求地址
     */
    open : function(title, url){
        layer.open({
            type: 2,
            title: title,
            shadeClose: true,
            shade: false,
            maxmin: true, //开启最大化最小化按钮
            area: ['90%', '90%'],
            content: url
        });
    },
    /**
     * 打开修改或新增页面
     * @param title 页面标题
     * @param url 地址
     * @param callBack 确认执行后的回调
     * @param data 更新页面需要的传参
     * @returns {*}
     */
    openAdd : function (title, url, callBack, data) {
        return layer.open({
            type: 2,
            area: ['90%', '90%'],
            title: title,
            shadeClose: false,
            shade: null,
            anim: 5,
            maxmin: true, //开启最大化最小化按钮
            content: url ,
            btn: ['确定', '关闭'],
            success: function(layero, index) {
                // 更新页面，按对象名字赋值到页面上
                if (typeof(data) !== 'undefined') {
                    let body = layer.getChildFrame('body', index);
                    let obj = s.str2json(data);
                    for (let temp in obj) {
                        let el = body.find('#' + temp);
                        // 如果有对应id的值，就不会通过name去找对应的值
                        if (el[0]) {
                            el.val(obj[temp]);
                            continue;
                        }
                        el = body.find('.' + temp);
                        if (el[0]) {
                            if (s.existAttr(el, 'neetHtml')) {
                                el.html(obj[temp]);
                                continue;
                            }
                        }
                        el = body.find("input[name="+ temp +"]");
                        if (el[0]) {
                            el.val(obj[temp]);
                            continue;
                        }
                        el = body.find("textarea[name="+ temp +"]");
                        if (el[0]) {
                            el.val(obj[temp]);
                            continue;
                        }
                        el = body.find("select[name="+ temp +"]");
                        if (el[0]) {
                            el.val(obj[temp]);

                        }
                    }
                }
            },
            yes: function(index, layero){
                let iframeWin = window[layero.find('iframe')[0]['name']];
                // 调用form页面的submit方法, 传入一个提交完成后的回调方法
                iframeWin.submit(function (result) {
                    layer.close(index);
                    if (typeof (callBack) === 'function'){
                        callBack(result);
                    }
                });
            }
        });
    },
    /**
     * 判断某个标签是否存在某个属性
     * @param el 标签
     * @param attrName 属性名
     * @returns {boolean} true表示存在
     */
    existAttr: function(el, attrName) {
        let temp = el.attr(attrName);
        return typeof temp !== 'undefined';
    },
    /**
     * 警告信息并执行
     * @param detail 内容(非必须)
     * @param callback 回调函数
     * @param params 一个参数,格式不限
     */
    alert : function (detail, callback, params) {
        layer.alert(detail, {
            icon: 0,
            title: '确认',
            btn: ['确定', '取消'],
            move: false,
            closeBtn: 0,
            resize: false
        }, function(index){
            if (params) {
                callback(params);
            } else {
                callback();
            }
            s.clear(index);
        });
    },
    /**
     * 展示成功的消息
     * @param content 内容
     * @param callback 回调方法
     * @param params 回调参数
     */
    alertSuccess : function (content, callback, params) {
        layer.alert(content, {icon: 1, title: '成功', btn: ['好的'], move: false, closeBtn: 0, resize: false}, function(index){
            if (typeof (callback) === 'function') {
                callback(params);
            }
            s.clear(index);
        });
    },
    /**
     * 展示错误的消息
     * @param content 内容(非必须)
     */
    alertError : function (content) {
        layer.alert(content, {icon: 2, title: '错误', btn: ['知道了'], move: false, closeBtn: 0, resize: false});
    },
    /**
     * 弹出Html
     * @param title 标题
     * @param content 内容Html
     * @param params 参数
     * @param confirmCallback 点击确定的回调
     * @param cancelCallback 点击取消的回调
     */
    alertHtml : function (title, content, params, confirmCallback, cancelCallback) {
        return layer.open({
            type: 1,
            title: title,
            shadeClose: false,
            shade: [0.5, '#000'],
            content: '<div style="padding: 20px; min-width: 320px; font-size: 14px; line-height: 24px;">' + content + '</div>' ,
            btn: ['确定', '取消'],
            closeBtn: 0,
            move: true,
            resize: false,
            yes: function(index, layero){ // 确定按钮的回调
                if (typeof (confirmCallback) === 'function') {
                    confirmCallback(params);
                } else {
                    s.clear(index);
                }
            },
            btn2: function (index, layero) { // 取消按钮的回调
                if (typeof (cancelCallback) === 'function') {
                    cancelCallback(params);
                } else {
                    s.clear(index);
                }
            }
        });
    },
    /**
     * 获取请求地址的html
     * @param url 请求地址
     * @param callback 回调函数,会传入获得的html
     */
    ajaxHtml : function(url, callback){
        $.ajax({
            url: url,
            type:'post',
            dataType:'html',
            data: {},
            error: function(){
                s.alertError("遇到错误");
            },
            success:function(data){
                if (typeof(callback) === 'function') {
                    callback(data);
                }
            }
        });
    },
    /**
     * 通过ajax获得数据
     * @param url 请求的actionUrl,不需要全路径
     * @param method 请求方法 POST、GET、PUT、DELETE
     * @param jsonParam 参数对象 {name : 'value', name2 : 'value2'}
     * @param callBack 成功回调函数
     * @param errorCallback 错误回调函数
     * @param closeLoad 是否有加载层
     */
    ajax : function(url, method, jsonParam, callBack, errorCallback, closeLoad){
        let load = null;
        if (typeof(closeLoad) === 'undefined'){
            load = s.load();
        }
        $.ajax({
            type : method,
            url : url,
            dataType : "json",
            data : jsonParam,
            async: true,
            success:function(result){
                if (typeof(callBack) === 'function') {
                    if (result.status === 200) {
                        s.clear(load);
                        callBack(result.result);
                    } else {
                        if (typeof(errorCallback) === 'function') {
                            errorCallback(result.error);
                        } else {
                            s.alertError(result.error);
                            s.clear(load);
                        }
                    }
                }
            },
            complete:function(){
                if (typeof(closeLoad) === 'undefined'){
                    s.clear(load);
                }
            },
            error:function(xhr, textStatus, errorThrown) {
                let status = xhr.status; // http status
                let msg = xhr.responseText;
                if (status === 401) {
                    s.alertError("会话过期，请刷新浏览器");
                    return;
                }
                if (typeof(errorCallback) === 'function') {
                    errorCallback(JSON.parse(msg).error);
                } else {
                    s.alertError(JSON.parse(msg).error);
                }
            }
        });
    },
    /**
     * 保存数据到浏览器
     * @param key 键
     * @param value 值
     */
    set : function (key, value) {
        if (s.storeEnable()) {
            localStorage.setItem(key, value);
        }
    },
    /**
     * 获取浏览器数据
     * @param key 键
     */
    get : function (key) {
        if (s.storeEnable()) {
            return localStorage.getItem(key);
        }
    },
    /**
     * 判断浏览器是否支持html5本地存储
     * @returns {boolean}
     */
    storeEnable : function () {
        return (('localStorage' in window) && window['localStorage'] !== null)
    },
    tipError : function (selector, content) {
        layer.tips(content, selector, {
            tips: [1, '#F00'],
            time: 2000
        });
    },
    /**
     * 获取url中的参数
     * @param href URL连接字符串
     * @param para 要从连接中获取的参数名
     */
    getUrlParam: function (href, para) {
        let reg = new RegExp("(^|&)" + para + "=([^&]*)(&|$)");
        href = href.split('?')[1];
        let r = href.match(reg);
        if (r != null) {
            return r[2];
        }
        return null;
    },
    /**
     * 初始化所有ajax上传图片
     */
    initAjaxUploadPic : function() {
        $('.ajax_upload_pic').each(function () {
            s.initAjaxUploadPicWithDom(this);
        });
    },
    /**
     * 按标签初始化单个ajax上传图片
     * @param dom 元素
     */
    initAjaxUploadPicWithDom : function(dom) {
        if ($.trim($(dom).html())) {
            return;
        }
        var hiddenName = $(dom).attr('data-hiddenName');
        var fileName = $(dom).attr('data-fileName');
        var url = $(dom).attr('data-url');
        var value = $(dom).attr('data-value');
        var successCallback = $(dom).attr('data-successCallback');
        var html = '<input type="hidden" name="' + hiddenName + '">';
        if (value) {
            html = '<input type="hidden" name="' + hiddenName + '" value="' + value + '">';
        }
        if (successCallback) {
            html += '<input type="file" name="' + fileName + '" class="ajax_btn_pic" onchange="s.ajaxUploadPic(this, \'' + hiddenName + '\', \'' + fileName + '\', \'' + url + '\', ' + successCallback + ')">';
        } else {
            html += '<input type="file" name="' + fileName + '" class="ajax_btn_pic" onchange="s.ajaxUploadPic(this, \'' + hiddenName + '\', \'' + fileName + '\', \'' + url + '\')">';
        }
        html += '<div class="ajax_upload_pic_progress"></div>';
        html += '<a href="javascript:;" title="移除此图片" class="ajax_upload_close_btn" onclick="s.ajaxUploadPicRemove(this, \'' + hiddenName + '\', \'' + fileName + '\')"><i class="fa fa-trash"></i></a>';
        $(dom).html(html);
        if (value) {
            $(dom).find('input[name=' + fileName + ']').css('background-image', 'url(' + value + ')');
            $(dom).addClass('hasPic');
        }
    },
    ajaxUploadPicRemove : function (dom, hiddenName, fileName) {
        $(dom).parent().removeClass('hasPic');
        $(dom).siblings('input[name=' + hiddenName + ']').val('');
        $(dom).siblings('.ajax_btn_pic').css('background-image', 'url('+s.rootPath("images/common/pic_add.png")+')');
    },
    /**
     * ajax上传图片
     * @param fileInput file域input标签
     * @param hiddenName 隐藏域name
     * @param fileName file域name
     * @param url 上传图片地址，文件参数名为fileId的值
     * @param successCallback 上传成功后的回调方法
     * example:
     * <div class="ajax_upload_pic" data-hiddenName="pic" data-fileName="picFile" data-url="item/uploadPic"></div>
     * example(带value和callback):
     * <div class="ajax_upload_pic" data-hiddenName="pic" data-fileName="picFile" data-url="item/uploadPic" data-successCallback="success" data-value="${params.pic}"></div>
     */
    ajaxUploadPic : function(fileInput, hiddenName, fileName, url, successCallback) {
        if (!$(fileInput)[0].files[0]) {
            return;
        }
        var formData = new FormData();
        formData.append(fileName, $(fileInput)[0].files[0]);
        $.ajax({
            url: s.rootPath(url),
            type: 'POST',
            xhr: function() {
                var myXhr = $.ajaxSettings.xhr();
                if(myXhr.upload){ // check if upload property exists
                    myXhr.upload.addEventListener('progress',function(e) {
                        if(e.lengthComputable){
                            var percent = e.loaded / e.total * 100;
                            $(fileInput).next('.ajax_upload_pic_progress').css('height', percent + '%');
                        }
                    }, false); // for handling the progress of the upload
                }
                return myXhr;
            },
            //Ajax事件
            beforeSend: function() {
            },
            success: function(data) {
                var isUpdate = false;
                if ($(fileInput).parent().find('input[name=' + hiddenName + ']').val()) {
                    isUpdate = true;
                }
                $(fileInput).parent().addClass('hasPic');
                $(fileInput).parent().find('input[name=' + hiddenName + ']').val(data.result);
                $(fileInput).css('background-image', 'url(' + data.result + ')');
                //s.clear();
                $(fileInput).next('.ajax_upload_pic_progress').css('height', '0');
                if (successCallback) {
                    successCallback(isUpdate, data.result);
                }
            },
            error: function(err) {
                s.log(err);
                $(fileInput).val('');
                $(fileInput).next('.ajax_upload_pic_progress').css('height', '0');
                s.alertError('上传错误');
            },
            // Form数据
            data: formData,
            // Options to tell JQuery not to process data or worry about content-type
            cache: false,
            contentType: false,
            processData: false
        });
    },
};
function reloadPage() {
    window.location.href=window.location.href;
}
function dateFormat(input) {
    let d = new Date(input);
    let year = d.getFullYear();
    let month = d.getMonth() + 1;
    let day = d.getDate() < 10 ? '0' + d.getDate() : '' + d.getDate();
    let hour = d.getHours();
    let minutes = d.getMinutes();
    let seconds = d.getSeconds();
    return year + '-' + month + '-' + day + ' ' + hour + ':' + minutes + ':' + seconds;
}
$.fn.serializeObject = function() {
    let o = {};
    let a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name]) {
            if (!o[this.name].push) {
                o[this.name] = [ o[this.name] ];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

function wei2etherNumber(arg1) {
    if (!arg1) {
        return '';
    }
    return new BigNumber(arg1).dividedBy(1000000000000000000).toString();
}

function accDiv(arg1, arg2) {
    let t1 = 0, t2 = 0, r1, r2;
    try {
        t1 = (1 * arg1).toString().split(".")[1].length;
    } catch (e) {
    }
    try {
        t2 = (1 * arg2).toString().split(".")[1].length;
    } catch (e) {
    }
    with (Math) {
        r1 = Number((1 * arg1).toString().replace(".", ""));
        r2 = Number((1 * arg2).toString().replace(".", ""));
        return (r1 / r2) * pow(10, t2 - t1);
    }
}

//说明：javascript的加法结果会有误差，在两个浮点数相加的时候会比较明显。这个函数返回较为精确的加法结果。
//调用：accAdd(arg1,arg2)
//返回值：arg1加上arg2的精确结果
function accAdd(arg1, arg2) {
    var r1, r2, m, n;
    try { r1 = arg1.toString().split(".")[1].length } catch (e) { r1 = 0 }
    try { r2 = arg2.toString().split(".")[1].length } catch (e) { r2 = 0 }
    m = Math.pow(10, Math.max(r1, r2));
    n = (r1 >= r2) ? r1 : r2;
    return parseFloat(((arg1 * m + arg2 * m) / m).toFixed(n));
}