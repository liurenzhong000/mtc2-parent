## 项目模块结构说明 ##

模块 | 说明 | 端口
---|---|---
sdk | 与业务结合不紧密的 |
　∟ common | 不依赖于parent的项目，为所有模块提供工具类、常量、model及共通bootstrap.yml配置 `务必调用`
　∟ common-data | 依赖于data-jpa的分页组件
　∟ common-http | http请求封装的共通
　∟ common-jpa | 为需要jpa的模块，提供共通的jpa支持
　∟ common-mongo | mongodb共通支持
　∟ common-mq | 阿里云Mq
　∟ common-oss | 阿里云OSS
　∟ common-quartz | job cluster共通模块
　∟ common-redis | 为需要reids的模块，提供共通的reids支持
　∟ common-web3j | web3j库及工具类
　∟ server-register | 注册中心    | 8761
　∟ server-config | 配置中心    | 8888
　∟ server-monitor | 配置中心    | 8880
　∟ server-zuul | 网关    | 80(正式)、8080(测试)
biz | |
　∟ facade-backend | 后台管理：管理员、权限、角色 | 9000
　∟ facade-api | App接口 | 9001
　∟ facade-bitcoin | BCH与BTC的钱包接口 | 8210
　∟ facade-market | 行情App接口 | 9003
　∟ service-currency | 币种    | 8085
　∟ service-endpoint-eth | 与节点交互的模块,只做节点代理。除了交易请求时，会存一次交易记录 | 8090
　∟ service-notification | 通知推送消息相关    | 8105
　∟ service-trans-eth | eth交易记录 | 8100
　∟ service-user | 托管用户 | 8110


## 配置文件说明 ##
> server-register 配置了:
1. 开通的服务注册地址
---
> server-config 配置了:
1. 连接的服务注册地址
2. 开通配置中心
---
> 共通 配置了:
1. 连接的服务注册地址
2. 连接配置中心
---
> 其他 需要配置:
1. 在application.yml里配置服务名
2. 在配置中心配置自己的其他配置