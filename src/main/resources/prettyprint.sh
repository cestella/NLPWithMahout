#!/bin/bash
if [ $# -lt 1 ];then
  NUM_LINES=5
else
  NUM_LINES=$1
fi

  grep "^{"\
| sed 's/^{//g'\
| sed 's/}$/\n/g'\
| sed 's;:;\n;g'\
| awk -F, '{if(NF > 1) {print $1; REST=$2;for (i=3;i <= NF;i++) {REST=REST","$i};print REST} else print $0 }'\
| awk '!(NR%2){print p"\t"$0}{p=$0}'\
| sort -k 2 -g -r | head -n $NUM_LINES

