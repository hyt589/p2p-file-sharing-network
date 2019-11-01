# P2P File Sharing Network

## How to run this program

First change directory to ~/p2p:  
```shell script
cd ~/p2p
```  
The run the following command to start the program:  
```shell script
java -cp classes p2p
```

All six peers needs to be started this way.

## Topology

eecslab-10~12 are connected to each other, same goes for eecslab-13~15; the two clusters are connected by a connection 
between 12 and 13

## Multiple Query Hits Returned to Intermediate Peer

This intermediate peer will only choose one query hit and pass it backward the query path to the original sender.