#!/bin/sh

cd ../../out/

if [ $# -eq 0 ]
  then
    echo "È necessario inserire l'ip del serverManager!"
    exit
fi
java Client.ClientClass //$1/ServerManager
