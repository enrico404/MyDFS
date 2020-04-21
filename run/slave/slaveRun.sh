#!/bin/sh

echo "Entra come myDfsUser (utente creato durante l'installazione)"
su myDfsUser -c "cd $SOURCES_MyDFS/out;
java -Djava.security.policy=./perm.policy Server/ServerClass $SHDIR_MyDFS"

