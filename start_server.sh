#!/bin/sh

mvn -o exec:java \
-Dexec.mainClass="org.sherlok.SherlokServer" \
-Dexec.classpathScope=runtime \
-Dexec.args="$1 $2 $3 $4 $5 $6"
#-Dexec.args="$1 $2 $3 -Xms3G -Xmx6G -server"
