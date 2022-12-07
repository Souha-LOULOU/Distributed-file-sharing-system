### ~[ Project : File Sharing System V2.0 ] ~ ###
_________________________________________________________

### 2nd Approach : Gnutella Peer-2-Peer Network using RMI####
_________________________________________________________
## Table of Content:
- Introduction
- Pre-requirements
- Running
- Code explanation
- Further explanations
_________________________________________________________
### Introduction :
This project is a file sharing system (Version 2.0) between peers, that implements the
peer-2-peer structure according to a particular topology (static and not dynamic, predefined in 
a configuration file). The approach used here is the Gnutella approach but this time using RMI registry. 
Where each peer is at the same time a client and a server. Listens to the requests in the
network and can send at any moment a request to search and download a certain file. The execution is
fully explained in the joint PDF.
This project is developed by :
	- Loulou Souha
	- Ben Jemaa Mouhib
This application is destined to run on remote machines but we can run it also locally.
_________________________________________________________

### Pre-requirements : 
- You need to install the latest version of java in your machine.
_________________________________________________________

### Running
To run the code, open as many CMDs as the number of peers that you want. For a test topology, we chose
9 peers so you have to open 9 CMDs into the folder where you installed the source code.
To run the code you have to type the command "java gnutellaP2P/Peer" which will run the Peer class.
For further details, please refer to the joint PDF of the execution where everything is fully explained with details.

If you want to run this applcation between distant PCs, define your own topology by specifying the particular IP address for
each machine in the config file "config.properties" , then replace all "localhost" instantiations in the code with the respective IP address.

_________________________________________________________
### Code explanation
The code consists of 5 classes and 1 interface: 

	*- PeerInterface.java:  interface :
		contains the abstract remote methods: 
		- obtain(filename) 
		- query(fromPeerId,msgId,fielname)

	*-PeerDetails: class: contains all the details of the peer

	*- HitQuery.java: class: 
		This class is used to handle the details of the result of every query. 
		It contains properties of the peer details of the requested file and the path through which search performed.

	*-NeighborConnectionThread: class and a thread
		creates HitQuery objects for earch each connection with neighbors (thread)
		calls the RMI lookup to establich connection with remote peers.

	*- PeerInterfaceRemote.java: class :
		implements PeerInterface and defines the remote methods

		-query(fromPeerId,msgId,fielname): This remote method takes the filename, message id as arguments. 
		It checks whether the peer already processed that msgId or not. If it is already processed, 
		it will skip the process and return to the called client. 
		If message id is not there with the peer then it will perform the search locally. 
		It will establish the connection with its neighbor peers and will get the results. 
		All these results will be sent back to the called client. 

		-Obtain(filename):This remote method takes the file name as an argument. 
		It will send the file contents as bytes and client will create a new file with these bytes of data.

	*- Peer.java: main class:
		- Enter the directory path that is going to be shared with other peers
		Prompts for file name to search 
		- Get the neighbor peers from the config file and get the result from those peers.
		- Display the list of peers containing the requested file. 
		- Prompt to enter the peer id from where the file should be downloaded 
		-Current peer run as a sever (RMI rebind)
		- Establish connection with peer and download the file.
	



_________________________________________________________
### Further Explanations
For further explanations please refer the PDF "report.PDF" where everything is explained fully in details. If you have any questions
you can send us a mail.