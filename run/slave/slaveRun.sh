#!/bin/sh

echo "Entra come myDfsUser (utente creato durante l'installazione)"
export $(grep SOURCES_MyDFS /home/myDfsUser/.config/MyDFS/configMyDFS.txt)
su myDfsUser -c "cd $SOURCES_MyDFS/out;
java -Djava.security.policy=./perm.policy Server/ServerClassMain"

