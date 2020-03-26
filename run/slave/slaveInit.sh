#!/bin/sh

echo "Ricorda di non chiudere questo terminale una volta inserita la password!"
echo ""
echo "Entra come myDfsUser (utente creato durante l'installazione)"
echo ""
su myDfsUser -c "cd /home/myDfsUser/MyDFS/out; rmiregistry"

