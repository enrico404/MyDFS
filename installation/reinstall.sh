#!/bin/sh

sudo cp -r ../../MyDFS /home/myDfsUser/
sudo chown -R myDfsUser:myDfsUser /home/myDfsUser/MyDFS
echo "Reinstallazione completata con successso!"
exit
