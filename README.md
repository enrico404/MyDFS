# Introduzione

MyDFS è un filesystem distribuito scritto in Java, la cui architettura è ispirata a quella di Hadoop nella sua prima versione. Il sistema ha quindi una grandissima scalabilità orrizzontale ed è fault tollerance.

# Architettura

...


# Istruzioni per l'uso

## Installazione:

1) nella cartella "installation" eseguire "sudo ./serverSetup.sh"

Questa procedura va ripetuta per ogni macchina che si vuole inserire nel file-system distribuito



## RUN


### Data Nodes

In tutti i nodi dedicati allo storage dei dati (Data nodes):

	1) apri un terminale nella directory "run/slave/"
	2) eseguire ./slaveInit.sh 
	3) apri un altro terminale nella directory "run/slave"
	4) eseguire ./slaveRun.sh




Per il nodo dedicato alla gestione dei vari Data nodes è necessario eseguire i seguenti comandi (è possibile anche definire come ServerManager un Data Node):

### ServerManager:

NB: se il nodo è già avviato come "Data node" saltare i passi 1 e 2


	1) apri un terminale nella directory "run/serverManager"
	2) eseguire ./serverManager_init.sh
	3) apri un altro terminale nella directory "run/serverManager"
	4) eseguire ./serverManager_run.sh <slave_ip>...

slave_ip deve essere nel formato:  //ip_slave/server_name

NB: i nomi dei server devono essere differenti l'uno dall'altro, un uso scorretto potrebbe compromettere il funzionamento del software



### Client

Ora un qualsiasi host connesso alla stessa rete locale può diventare un potenziale client per il cluster:

	1) eseguire ./client_run.sh <ip_serverManager> 
	
