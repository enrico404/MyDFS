#!/bin/sh

echo "Ricorda di non chiudere questo terminale una volta inserita la password!"
echo ""
echo "Entra come myDfsUser (utente creato durante l'installazione)"
echo ""
export $(grep SOURCES_MyDFS /home/myDfsUser/.config/MyDFS/configMyDFS.txt)
su myDfsUser -c "cd $SOURCES_MyDFS/out; rmiregistry"

