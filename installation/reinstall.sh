#!/bin/sh

sudo rm -rf $SOURCES_MyDFS
sudo cp -r ../../MyDFS $SOURCES_MyDFS
sudo chown -R myDfsUser:myDfsUser $SOURCES_MyDFS
echo "Reinstallazione completata con successso!"
exit
