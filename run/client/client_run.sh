#!/bin/sh

cd ../../out/

if [ $# -eq 0 ]
  then
    echo "È necessario inserire l'ip del serverManager!"
    exit
fi

STR=""
for (( i=1; i<=$#; i++ ))
do
  STR+="//$i/ServerManager "
done

java Client.ClientClass $STR