#!/bin/bash

APP_JAR_NAME=facade-market
APP_JAR_PATH=./$APP_JAR_NAME.jar
MEM=1024

case $1 in
    start)
    nohup java -server -Xms${MEM}m -Xmx${MEM}m -XX:-UseGCOverheadLimit -XX:NewRatio=1 -XX:SurvivorRatio=8 -XX:+UseSerialGC -jar $APP_JAR_PATH --spring.profiles.active=prod  >/dev/null 2>&1 &
    proc_id=`ps -ef|grep -i $APP_JAR_NAME|grep -v "grep"|awk '{print $2}'`
    echo "application pid:"${proc_id}
    ;;
    stop)
    proc_id=`ps -ef|grep -i $APP_JAR_NAME|grep -v "grep"|awk '{print $2}'`
    if [[ -z  $proc_id ]];then
        echo "The task is not running !"
    else
        for id in ${proc_id[*]}
        do
            kill ${id}
            if [ $? -eq 0 ];then
                echo "task is killed ..."
            else
                echo "kill task failed "
            fi
        done
    fi
    ;;

    restart)
        sh $0 stop
        sleep 5
        if [ -e temp/$APP_JAR_NAME.jar ]
        then
            mv -f temp/$APP_JAR_NAME.jar .
            echo "temp/$APP_JAR_NAME.jar replaced"
        fi
        sh $0 start
    ;;

    *)
        echo "Application will restart..."
        sh $0 restart
    ;;
esac
