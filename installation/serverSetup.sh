#!/bin/sh

adduser --force-badname myDfsUser
usermod -aG sudo myDfsUser
sudo cp -r ../../MyDFS /home/myDfsUser/
sudo chown -R myDfsUser:myDfsUser /home/myDfsUser/MyDFS
su myDfsUser -c 'mkdir -p /home/myDfsUser/shDir'
exit
