#!/bin/bash

if [ -z "$HADOOP_HOME" ];then
  HADOOP_HOME=/usr/lib/hadoop
fi
BASEDIR=$(dirname $0)
$HADOOP_HOME/bin/hadoop fs -put $BASEDIR/docs $1/data
