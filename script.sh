#!/bin/bash
SERVICE="/servers/demo-0.0.1-SNAPSHOT.jar"
if pgrep -f demo-0.0.1-SNAPSHOT.jar > /dev/null
then
    pkill -9 -f demo-0.0.1-SNAPSHOT.jar
else
    echo "$SERVICE stopped"
fi
echo "hello123"
