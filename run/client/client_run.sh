#!/bin/sh

cd ../../out/

if [ $# -eq 0 ]
  then
    echo "È necessario inserire almeno l'ip di un serverManager!"
    exit
fi

STR=""
TMP=""
for ip in $@; do
    TMP=//$ip/ServerManager
    STR=$STR" "$TMP
done

java Client.ClientClassMain $STR
