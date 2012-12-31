#!/bin/bash
JAVA="java"
JAVA_OPTS="-Xms512M -Xmx2048M -Xss1M -XX:MaxPermSize=512M"
SBT="sbt-launch.jar"

$JAVA $JAVA_OPTS -jar $SBT "$@"
