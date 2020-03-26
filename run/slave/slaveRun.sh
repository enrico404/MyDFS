#!/bin/sh

echo "Entra come myDfsUser (utente creato durante l'installazione)"
su myDfsUser -c "cd /home/myDfsUser/MyDFS/out;
java -Djava.security.policy=./perm.policy Server/ServerClass"

