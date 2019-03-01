#!/usr/bin/env bash
readonly geth_pid=`ps -ef | grep 'geth --rpc' | grep -v 'grep' | awk '{print $2}'`
if [ -n "$geth_pid" ]
then
    echo "kill $geth_pid"
    kill -9 $geth_pid
    sleep 3
    echo "restart..."
fi
# 节点1
nohup geth --rpc --rpcaddr "172.21.131.72" --rpcport 7000 --rpcapi "eth,web3,personal" -rpccorsdomain "*" --datadir "/alidata1/mtc" --cache 1024 --syncmode "fast" --maxpeers 100 > /dev/null 2>&1 &
# 节点2
#nohup geth --rpc --rpcaddr "172.21.131.95" --rpcport 7000 --rpcapi "eth,web3,personal" -rpccorsdomain "*" --datadir "/alidata1/mtc" --cache 1024 --syncmode "fast" --maxpeers 100 > /dev/null 2>&1 &
# 节点3
#nohup geth --rpc --rpcaddr  "172.21.50.90" --rpcport 7000 --rpcapi "eth,web3,personal" -rpccorsdomain "*" --datadir "/alidata1/mtc" --cache 1024 --syncmode "fast" --maxpeers 100 > /dev/null 2>&1 &
# 节点4
#nohup geth --rpc --rpcaddr  "172.21.50.91" --rpcport 7000 --rpcapi "eth,web3,personal" -rpccorsdomain "*" --datadir "/alidata1/mtc" --cache 2048 --syncmode "fast" --maxpeers 100 > /dev/null 2>&1 &
# 节点5
#nohup geth --rpc --rpcaddr  "172.21.50.92" --rpcport 7000 --rpcapi "eth,web3,personal" -rpccorsdomain "*" --datadir "/alidata1/mtc" --cache 1024 --syncmode "fast" --maxpeers 100 > /dev/null 2>&1 &