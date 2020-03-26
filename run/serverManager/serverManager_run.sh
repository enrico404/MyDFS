#!/bin/sh

if [ $# -eq 0 ]
  then
    echo "È necessario inserire almeno l'ip di un Data Node!"
    exit
fi
echo "Entra come myDfsUser (utente creato durante l'installazione)"
su myDfsUser -c "cd /home/myDfsUser/MyDFS/out;
java -Djava.security.policy=./perm.policy Server/ServerManager $*"

