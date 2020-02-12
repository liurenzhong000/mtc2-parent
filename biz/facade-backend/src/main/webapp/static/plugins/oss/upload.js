
// let accessid= 'LTAI6Nz6H3G1BgkD';
// let accesskey= 'N3OAqHxZb1MGMclZaGLjrCneb4zHtT';
// let host = 'https://mtc-files.oss-ap-southeast-1.aliyuncs.com';
// let displayhost = 'https://files.m2c.work';

let accessid = 'LTAIQDLWQ1yGUcrS';
let accesskey = 'mqUFcmwTpuhchzrsiUlpHK1dXu2SBY';
let host = 'https://zcd-wallet.oss-ap-southeast-1.aliyuncs.com';
let displayhost = 'https://zcd-wallet.oss-ap-southeast-1.aliyuncs.com';

let g_dirname = 'image/currency';
let g_object_name = '';
let now = timestamp = Date.parse(new Date()) / 1000;

let policyText = {
    "expiration": "2020-01-01T12:00:00.000Z", //设置该Policy的失效时间，超过这个失效时间之后，就没有办法通过这个policy上传文件了
    "conditions": [
        ["content-length-range", 0, 1048576000] // 设置上传文件的大小限制
    ]
};

let policyBase64 = Base64.encode(JSON.stringify(policyText));
let bytes = Crypto.HMAC(Crypto.SHA1, policyBase64, accesskey, { asBytes: true });
let signature = Crypto.util.bytesToBase64(bytes);

function random_string(len) {
    len = len || 32;
    let chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';
    let maxPos = chars.length;
    let pwd = '';
    for (let i = 0; i < len; i++) {
        pwd += chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return pwd;
}

function get_suffix(filename) {
    let pos = filename.lastIndexOf('.');
    let suffix = '';
    if (pos !== -1) {
        suffix = filename.substring(pos)
    }
    return suffix;
}

function set_upload_param(up, filename, ret)  {
    g_object_name = g_dirname;
    if (filename !== '') {
        let suffix = get_suffix(filename);
        g_object_name = g_dirname + "/" + random_string(10) + suffix;
    }
    let new_multipart_params = {
        'key' : g_object_name,
        'policy': policyBase64,
        'OSSAccessKeyId': accessid,
        'success_action_status' : '200', //让服务端返回200,不然，默认会返回204
        'signature': signature,
    };

    up.setOption({
        'url': host,
        'multipart_params': new_multipart_params
    });

    up.start();
}

let uploader = new plupload.Uploader({
    runtimes : 'html5,flash,silverlight,html4',
    browse_button : 'selectfiles',
    //multi_selection: false,
    container: document.getElementById('container'),
    flash_swf_url : 'lib/plupload-2.1.2/js/Moxie.swf',
    silverlight_xap_url : 'lib/plupload-2.1.2/js/Moxie.xap',
    url : 'http://oss.aliyuncs.com',
    callback : undefined,

    init: {
        PostInit: function() {
            document.getElementById('ossfile').innerHTML = '';
            document.getElementById('postfiles').onclick = function() {
                set_upload_param(uploader, '', false);
                return false;
            };
        },

        FilesAdded: function(up, files) {
            plupload.each(files, function(file) {
                document.getElementById('ossfile').innerHTML +=
                    '<div id="' + file.id + '">'
                    +'  <div class="progress" style="margin-bottom: 10px; background-color: #f5f5f5; border-radius: 4px; box-shadow: inset 0 1px 2px rgba(0,0,0,.1);"><div class="progress-bar" style="width: 0"></div></div>'
                    +'</div>';
            });
        },

        BeforeUpload: function(up, file) {
            set_upload_param(up, file.name, true);
        },

        UploadProgress: function(up, file) {
            let d = document.getElementById(file.id);
            // d.getElementsByTagName('b')[0].innerHTML = '<span>' + file.percent + "%</span>";
            let prog = d.getElementsByTagName('div')[0];
            let progBar = prog.getElementsByTagName('div')[0];
            progBar.style.width= file.percent+'%';
            progBar.setAttribute('aria-valuenow', file.percent);
        },

        FileUploaded: function(up, file, info) {
            if (info.status === 200) {
                if (typeof uploader.callback !== 'undefined') {
                    uploader.callback(displayhost + "/" + g_object_name);
                } else {
                    document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = 'upload to oss success, object name:' + file.name;
                }
            } else {
                document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = info.response;
            }
        },

        Error: function(up, err) {
            document.getElementById('console').appendChild(document.createTextNode("\nError xml:" + err.response));
        }
    }
});

uploader.init();