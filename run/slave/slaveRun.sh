#!/bin/sh

su myDfsUser -c "cd /home/myDfsUser/MyDFS/src;
java -Djava.security.policy=./perm.policy Server/ServerClass"

