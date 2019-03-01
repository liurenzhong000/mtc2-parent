// const baseUrl = "https://app.mtc.io";
const baseUrl = "http://47.74.210.94:8180";
// const baseUrl = "http://localhost:8080";
const webRoot = baseUrl + "/backend";
const userWebRoot = baseUrl + "/user";

// 标准的部署方案
const standardDeploy = [
    { name : 'SERVICE-ENDPOINT-ETH', total : 4},
    { name : 'FACADE-API', total : 2},
    { name : 'FACADE-MARKET', total : 2},
    { name : 'FACADE-BACKEND', total : 2},
    { name : 'FACADE-USER', total : 2},
    { name : 'SERVER-CONFIG', total : 1},
    { name : 'SERVER-REGISTER', total : 2},
    { name : 'SERVER-ZUUL', total : 2},
    { name : 'SERVICE-CURRENCY', total : 2},
    { name : 'SERVICE-NOTIFICATION', total : 2},
    { name : 'SERVICE-TRANS-ETH', total : 2},
];

function hasAuth(key) {
    let auths = s.get("auths");
    let authMap = JSON.parse(auths);
    return authMap[key];
}

function compareInstants(instanceInfo) {
    let instances = new Array(0);
    $.each(standardDeploy, function (index, obj) {
        let tempName = obj.name;
        let upNum = 0;
        let downNum = 0;

        for (let i = 0; i < instanceInfo.length; i++) {
            let remoteTemp = instanceInfo[i];
            if (remoteTemp.name === tempName) {
                upNum = remoteTemp.upNum;
                downNum = remoteTemp.downNum;
                break;
            }
        }
        let temp = { name : tempName, total : obj.total, downNum : downNum, upNum : upNum};
        instances.push(temp);
    });
    return instances;
}