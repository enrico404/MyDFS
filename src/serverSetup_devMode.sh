#!/bin/sh


#creo config file
mkdir -p $HOME/.config/MyDFS

echo "PORT_SMSEC=6660" > $HOME/.config/MyDFS/configMyDFS.txt
{
  echo "PORT_TRINT=6668"
  echo "PORT_CL1=6668"
  echo "PORT_CL2=6669"
} >> $HOME/.config/MyDFS/configMyDFS.txt

if [ $# -eq 1 ]
  then
      echo "SOURCES_MyDFS=$1/MyDFS" >> $HOME/.config/MyDFS/configMyDFS.txt
      echo "Setting dell'utente...."
      mkdir -p $1/shDir
      echo "SHDIR_MyDFS=$1/shDir" >> $HOME/.config/MyDFS/configMyDFS.txt
      echo "Installazione completata con successo!"
      exit
fi
#default dir
echo "SOURCES_MyDFS=$HOME/MyDFS" >> $HOME/.config/MyDFS/configMyDFS.txt
mkdir -p $HOME/shDir
echo "SHDIR_MyDFS=$HOME/shDir" >> $HOME/.config/MyDFS/configMyDFS.txt
echo "Installazione completata con successo!"
exit
