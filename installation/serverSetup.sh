#!/bin/sh

sudo adduser --force-badname myDfsUser
sudo usermod -aG sudo myDfsUser
sudo cp -r ../../MyDFS /home/myDfsUser/
sudo chown -R myDfsUser:myDfsUser /home/myDfsUser/MyDFS
su myDfsUser -c 'mkdir -p /home/myDfsUser/shDir'
exit
