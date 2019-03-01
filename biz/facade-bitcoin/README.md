# BTCノードを立ち上げほうほう #
# 一、安装环境 #
1. 安装依赖包`apt-get install python-software-properties`
2. `curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -`
3. 安装nodejs `apt-get install nodejs`
4. 管理软件源 `npm install -g nrm`，设置为淘宝的 `nrm use taobao`

# 二、安装软件 #
```
apt-get install libzmq3-dev
npm install -g --unsafe-perm=true bitcore

bitcore create mynode
cd mynode
# API库，必须安装
bitcore install insight-api
# web界面，如果不需要可不安装
bitcore install insight-ui
# 启动后，会从0开始同步区块
bitcore start
```


# CentOS环境 #
1. 安装[nodejs](https://blog.csdn.net/abcdefg2343/article/details/81355002)
2. 安装bitcore-core
```
yum install gcc-c++
```

# BCHノードを立ち上げほうほう #
## 環境 ##
今回はEC2(t2.medium 500GB SSD Ubuntu 16.04)上で動かします

https://github.com/bitprim/bitcoin-abc

## フルノードのセットアップ ##
Insight APIを立ち上げるにはフルノードを立ち上げておく必要があります

本家bitcoin-abcのフルノードにはいくつかのinsight-api用のメソッドが無いため、それらが追加されたものを利用します

手順は以下：
1. `$ sudo apt update`
2. `$ sudo apt upgrade`
3. `git clone -b 0.18.2-bitcore https://github.com/bitprim/bitcoin-abc`
4. `cd bitcoin-abc`
5. `$ cd bitprim-bitcoin-abc-4e118c1`
6. ` $ sudo apt-get install build-essential libtool autotools-dev automake pkg-config libssl-dev libevent-dev bsdmainutils libboost-system-dev libboost-filesystem-dev libboost-chrono-dev libboost-program-options-dev libboost-test-dev libboost-thread-dev libzmq3-dev libminiupnpc-dev`
8. `$ ./autogen.sh`
9. `$ ./configure --disable-wallet --without-gui`
10. `$ make -j`
11. `$ make install`

## ~/.bitcoin/bitcoin.conf ##
フルノードの設定ファイルを以下のように記述します

各項目にindexを貼るようにすることでutxo等を算出するクエリが速く実行できるようになります

今回RPCは利用しませんが、 rpcuser と rpcpassword は任意のものに変更しておいてください
```
server=1
txindex=1
addressindex=1
timestampindex=1
spentindex=1
zmqpubrawtx=tcp://127.0.0.1:28332
zmqpubhashblock=tcp://127.0.0.1:28332
rpcuser=satoshi
rpcpassword=satoshi
usecashaddr=0
```
> `usecashaddr`はいちばん肝心です，俺は `bchfullnode/data/bitcoin.conf`のファイルを修正した，なりよりbitcoreはこのファイルを使っている

## Insight APIのセットアップ ##
bitcoreをインストールすることでInsight APIを利用することができるようになります

手順としては、まず推奨されているNodeのバージョン4をnvmでインストールします
その後、npmでbitcore及びbitcore-nodeをインストールしていきます

1. `$ curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.11/install.sh | bash`
2. `$ source ~/.bashrc`
3. `$ nvm install 4`
4. `$ nvm use 4`
5. `$ sudo apt install python`
6. `$ npm install -g bitcore bitcore-node`

## bitcore ノード立ち上げ ##
まず、bitcoreのワーキングディレクトリを作ります

1. `$ bitcore create bchfullnode`
2. `$ cd bchfullnode`
3. `$ bitcore install insight-api`

## bchfullnode/bitcore-node.json ##
bitcoreを立ち上げる前に、bitcoreの設定をしましょう
以下を参考にしてください

ここで一番重要なのがexecの項目で、ビルドしたbitprim/bitcoin-abcのフルノードの実行ファイルを指定する必要があります

```
{
 "network": "livenet",
 "port": 3001,
 "services": [
   "bitcoind",
   "insight-api",
   "web"
 ],
 "servicesConfig": {
   "insight-api": {
     "disableRateLimiter": true,
     "routePrefix": "api/bch"
   },
   "bitcoind": {
     "spawn": {
       "datadir": "/home/ubuntu/.bitcoin",
       "exec": "/usr/local/bin/bitcoind"
     }
   }
 }
}
```
設定が終わったら、bitcore start でInsight API及びフルノードを立ち上げます

初回はメインチェーンの同期に数日かかってしまいますが、根気よく待ちましょう💪*4
