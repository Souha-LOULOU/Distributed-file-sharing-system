### ~[ Project : File Sharing System V1.0 ] ~ ###
_________________________________________________________

### 1st Approach : Gnutella Peer-2-Peer Network using sockets####
_________________________________________________________
## Table of Content:
- Introduction
- Pre-requirements
- Running
- Code explanation
- Further explanations
_________________________________________________________
### Introduction :
This project is a file sharing system (Version 1.0) between peers, that implements the
peer-2-peer structure according to a particular topology (static and not dynamic, predefined in 
a configuration file). The approach used here is the Gnutella approach by implementing the socket
mechanism. Where each peer is at the same time a client and a server ; Listens to the requests in the
network and can send at any moment a request to search and download a certain file. The execution is
fully explained in the joint PDF.
This project is developed by :
	- Loulou Souha
	- Ben Jemaa Mouhib
You have to know that this application is to be run locally on your machine.
_________________________________________________________

### Pre-requirements : 
- You need to install the latest version of java in your machine.
- This solution uses the Socket class in java, you might be request to 
give permission for the application to run if you have some firewall rules 
that block the connection. Please grant this permission, our application is
100 % safe.

_________________________________________________________

### Running
To run the code, open as many CMDs as the number of peers that you want. For a test topology, we chose
3 peers so you have to open 3 CMDs into the folder where you installed the source code.
To run the code you have to type the command "java Main" which will run the main class "Main.java"
For further details, please refer to the joint PDF of the execution where everything is fully explained with details.

_________________________________________________________
### Code explanation
The code consists of 5 classes. 
	*- The Main class that instantiates the peer as a server and launches a download socket that
listens if there's a request. 
	*- The Client class that instantiates the peer as a client by starting a socket with the desired port (on localhost).
	*- The Server class that instantiates the peer as a server and performes the local search, the download as a server and the 
reception of a file as a Server
	*- The DownloadFromServer class that instantiates a peer as listening for a download and receiving a file from a server.
	*- The MessageSpecs class that defines the structure of the messages sent through the network

_________________________________________________________
### Further Explanations
For further explanations please refer the PDF "report.PDF" where everything is explained fully in details. If you have any questions
you can send us a mail.