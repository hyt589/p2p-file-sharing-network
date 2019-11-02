# P2P File Sharing Network

## How to run this program

First change directory to ~/p2p:  
```shell script
cd ~/p2p
```  
The run the following script to start the program:  
```shell script
. run.sh
```

All six peers needs to be started this way.

To get a file from the network, connect to the network first. After starting all six peers, at any one peer, type the 
following command:
```shell script
connect
get <filename>
```
Downloaded files will be in the `~/p2p/download/` directory.

## Note on run.sh

I included this shell script because I used the stream API from Java 8 in my program. I did a custom installation of jdk
13 and this script is used to set JAVA_HOME and PATH so the program can run properly.

## Topology

eecslab-10~12 are connected to each other, same goes for eecslab-13~15; the two clusters are connected by a connection 
between 12 and 13

## Multiple Query Hits Returned to Intermediate Peer

This intermediate peer will only choose one query hit and pass it backward the query path to the original sender.