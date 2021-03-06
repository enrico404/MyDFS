#!/bin/sh

if [ $# -eq 0 ]
  then
    echo "È necessario inserire almeno l'ip di un Data Node!"
    echo "Formato: //slave_ip/server_name"
    exit
fi
echo "Entra come myDfsUser (utente creato durante l'installazione)"
export $(grep SOURCES_MyDFS /home/myDfsUser/.config/MyDFS/configMyDFS.txt)
su myDfsUser -c "cd $SOURCES_MyDFS/out;
java -Djava.security.policy=./perm.policy Server/ServerManagerMain $*"

