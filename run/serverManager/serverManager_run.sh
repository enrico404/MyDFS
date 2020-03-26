#!/bin/sh

if [ $# -eq 0 ]
  then
    echo "È necessario inserire almeno l'ip di un Data Node!"
    echo "Formato: //slave_ip/server_name"
    exit
fi
echo "Entra come myDfsUser (utente creato durante l'installazione)"
su myDfsUser -c "cd /home/myDfsUser/MyDFS/out;
java -Djava.security.policy=./perm.policy Server/ServerManager $*"

