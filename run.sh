#!/usr/bin/env bash

LOG_DIR=/tmp

if [[ -z $WAIT_PERIOD ]]; then
    WAIT_PERIOD=0
fi

params="-Dsun.net.inetaddr.ttl=300 \
-Djava.awt.headless=true \
-Dfile.encoding=UTF-8 \
-Dspring.profiles.active=$ML_ENVIRONMENT \
-Dml.log.dir=${LOG_DIR} \
-XX:+UseG1GC \
-XX:InitialRAMPercentage=40 -XX:MaxRAMPercentage=90 \
-XX:-HeapDumpOnOutOfMemoryError \
-XX:ErrorFile=${LOG_DIR}/hs_err_pid_%p.log"
echo "Start with database connection ${DB_HOST}:${DB_PORT}"
echo "start java process after ${WAIT_PERIOD} secs wait time..."
sleep ${WAIT_PERIOD}
exec java $params -jar ./ml-bouncing-racoon.jar
