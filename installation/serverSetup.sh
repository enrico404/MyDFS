#!/bin/sh

#check if the user already exists
res=$(grep -c '^myDfsUser:' /etc/passwd)

if [ $res -eq 1 ]
  then
    echo "Il software è già stato installato!"
    exit
fi

sudo adduser --force-badname myDfsUser
sudo usermod -aG sudo,disk myDfsUser

sudo mkdir -p /home/myDfsUser/.config
sudo mkdir -p /home/myDfsUser/.config/MyDFS
sudo chown -R $USER:$USER /home/myDfsUser/.config/MyDFS/

echo "PORT_SMSEC=6660" > /home/myDfsUser/.config/MyDFS/configMyDFS.txt
{
  echo "PORT_TRINT=6668"
  echo "PORT_CL1=6668"
  echo "PORT_CL2=6669"
} >> /home/myDfsUser/.config/MyDFS/configMyDFS.txt

if [ $# -eq 1 ]
  then
      sudo cp -r ../../MyDFS $1
      sudo chown -R myDfsUser:myDfsUser $1/MyDFS
      echo "SOURCES_MyDFS=$1/MyDFS" >> /home/myDfsUser/.config/MyDFS/configMyDFS.txt
      echo "Setting del nuovo utente 'myDfsUser'...."
      sudo mkdir -p $1/shDir
      sudo chown -R myDfsUser:myDfsUser $1/shDir
      echo "SHDIR_MyDFS=$1/shDir" >> /home/myDfsUser/.config/MyDFS/configMyDFS.txt
      sudo chown -R myDfsUser:myDfsUser /home/myDfsUser/.config/MyDFS/
      echo "Installazione completata con successo!"
      exit
fi
#default dir
sudo cp -r ../../MyDFS /home/myDfsUser/
sudo chown -R myDfsUser:myDfsUser /home/myDfsUser/MyDFS
echo "SOURCES_MyDFS=/home/myDfsUser/MyDFS" >> /home/myDfsUser/.config/MyDFS/configMyDFS.txt
echo "Setting del nuovo utente 'myDfsUser'...."
sudo mkdir -p /home/myDfsUser/shDir
sudo chown -R myDfsUser:myDfsUser /home/myDfsUser/shDir
echo "SHDIR_MyDFS=/home/myDfsUser/shDir" >> /home/myDfsUser/.config/MyDFS/configMyDFS.txt
sudo chown -R myDfsUser:myDfsUser /home/myDfsUser/.config/MyDFS/
echo "Installazione completata con successo!"
exit
