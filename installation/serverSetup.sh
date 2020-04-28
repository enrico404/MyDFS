#!/bin/sh

sudo adduser --force-badname myDfsUser
sudo usermod -aG sudo,disk myDfsUser

su myDfsUser -c 'mkdir -p /home/myDfsUser/.config'
su myDfsUser -c 'mkdir -p /home/myDfsUser/.config/MyDFS'

if [ $# -eq 1 ]
  then
      sudo cp -r ../../MyDFS $1
      sudo chown -R myDfsUser:myDfsUser $1/MyDFS
      echo "export SOURCES_MyDFS=$1/MyDFS" >> $HOME/.bashrc
      echo "Setting del nuovo utente 'myDfsUser'...."
      mkdir -p $1/shDir
      sudo chown -R myDfsUser:myDfsUser $1/shDir
      echo "export SHDIR_MyDFS=$1/shDir" >> /home/enrico404/.bashrc
      echo "Installazione completata con successo!"
      exit
fi
#default dir
sudo cp -r ../../MyDFS /home/myDfsUser/
sudo chown -R myDfsUser:myDfsUser /home/myDfsUser/MyDFS
echo "export SOURCES_MyDFS=/home/myDfsUser/MyDFS" >> $HOME/.bashrc
echo "Setting del nuovo utente 'myDfsUser'...."
su myDfsUser -c 'mkdir -p /home/myDfsUser/shDir'
echo "export SHDIR_MyDFS=/home/myDfsUser/shDir" >> $HOME/.bashrc
echo "Installazione completata con successo!"
exit
