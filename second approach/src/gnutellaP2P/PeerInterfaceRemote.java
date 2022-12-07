package gnutellaP2P;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class NeighborPeers
{
	String peerId;
	int portno;
	String ip;
}
public class PeerInterfaceRemote extends UnicastRemoteObject implements PeerInterface {
	private static final long serialVersionUID = 1L;
	String sharedDirectory;
	ArrayList<String> localFiles=new ArrayList<String>();
	ArrayList<String> processdMsgIds;
	int peerId;
	int currentPeerPort;

	PeerInterfaceRemote(String sharedDir,int peerId,int currentPeerPort,ArrayList<String> localFiles) throws RemoteException {
		super();
		this.sharedDirectory = sharedDir;
		this.peerId=peerId;
		this.localFiles=localFiles;
		this.currentPeerPort=currentPeerPort;
		processdMsgIds=new ArrayList<String>();
	}
	public synchronized byte[] obtain(String filename) throws RemoteException {
	
		byte[] fileBytes = null;
		String fullFileName = sharedDirectory + "/" + filename;
		try {
			File myFile = new File(fullFileName);
			fileBytes = new byte[(int) myFile.length()];
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(fullFileName));
		
			input.read(fileBytes, 0, fileBytes.length);
			input.close();
			
			return fileBytes;
		} catch (Exception e) {

		}
		return fileBytes;

	}

	public HitQuery query(int fromPeerId,String msgId,String filename) throws RemoteException {
		
		ArrayList<NeighborPeers> neighborPeers=new ArrayList<NeighborPeers>();
		ArrayList<PeerDetails> findAt = new ArrayList<PeerDetails>();
		ArrayList<String> pathTrace=new ArrayList<String>();
		HitQuery hitqueryResult=new HitQuery();
		Boolean bDuplicate=false;
		ArrayList<NeighborConnectionThread> peerThreadsList = new ArrayList<NeighborConnectionThread>();
		synchronized(this){

		if (this.processdMsgIds.contains(msgId)){
			System.out.println("Incoming Request to peer "+peerId+": From - "+fromPeerId+" Duplicate Request - Already searched in this peer- with message id - " + String.valueOf(msgId));
			bDuplicate=true;	
		}
					    
				
		else{
			
			this.processdMsgIds.add(msgId);

		}
		} 
		if(bDuplicate==false){
			System.out.println("Incoming Request to peer "+peerId+": From - "+fromPeerId+" Search locally and send request to neighbours for msg id- " + String.valueOf(msgId));
			
		List<Thread> threads = new ArrayList<Thread>();
		
		if(searchInCurrentPeer(localFiles,filename)==true){
			System.out.println("Local Search: File Found in the current peer");
			PeerDetails temp=new PeerDetails();
			temp.hostIp="localhost";
			temp.peerId=peerId;
			temp.port=currentPeerPort;
			findAt.add(temp);
		}
			else{
				
				System.out.println("Local Search: File not found in the current peer");
				
		}
		
		getNeighborPeers(neighborPeers,peerId);
		if (neighborPeers.size()==0){
			pathTrace.add(Integer.toString(peerId));
		}
		
		for(int i=0;i<neighborPeers.size();i++){
			String currentPeer="peerid."+fromPeerId;
			if (neighborPeers.get(i).peerId.equals(currentPeer)){
				
			continue;
			}
			System.out.println("Outgoing Request from peer "+peerId+": Sending request to "+neighborPeers.get(i).peerId + " "+ neighborPeers.get(i).portno);
			NeighborConnectionThread ths = new NeighborConnectionThread(neighborPeers.get(i).ip, neighborPeers.get(i).portno,filename,msgId,peerId,neighborPeers.get(i).peerId);  
			Thread ts = new Thread(ths);
			
			ts.start();
			
			threads.add(ts);
			peerThreadsList.add(ths);
	
		}
		for (int i = 0; i < threads.size(); i++)
			try {
				
				((Thread) threads.get(i)).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		for (int i = 0; i < threads.size(); i++)
			try {
				((Thread) threads.get(i)).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		for (int i = 0; i < peerThreadsList.size(); i++){
		
			HitQuery temp = new HitQuery();
			
			temp=  (HitQuery) peerThreadsList.get(i).getValue();
			   if(temp.foundPeers.size()>0){
			
			  findAt.addAll(temp.foundPeers);
			   }
			   for (int count=0;count<temp.paths.size();count++){
				   String path=peerId+temp.paths.get(count);
							   pathTrace.add(path);
			   }
			   }
		if (pathTrace.size()==0)
		{
			pathTrace.add(Integer.toString(peerId));
		}
			
		 System.out.println("HitQuery: Send following result back to "+fromPeerId);
		 for (int i = 0; i < findAt.size(); i++){
			 System.out.println("--Found at Peer"+findAt.get(i).peerId+" on localhost:"+findAt.get(i).port);
			  
			}
		 hitqueryResult.foundPeers.addAll(findAt);
			hitqueryResult.paths.addAll(pathTrace);
			
		}
		
		return hitqueryResult;


	}

	private Boolean searchInCurrentPeer(ArrayList<String> localFiles2, String filename) {
		
		 int index = localFiles.indexOf(filename);
		    if (index == -1)
		      return false;
		    else
		      return true;
	}

	private void getNeighborPeers(ArrayList<NeighborPeers> neighborPeers, int peerId) {
		
		String property=null;
	
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("config.properties");

			
			prop.load(input);
			property="peerid."+peerId+".neighbors";
			String value=prop.getProperty(property);
			if(value!=null){
			String[] strNeighbors=value.split(",");
			
			for(int i=0;i<strNeighbors.length;i++){
				NeighborPeers tempPeer=new NeighborPeers();

				tempPeer.peerId=strNeighbors[i];
				tempPeer.ip=prop.getProperty(strNeighbors[i]+".ip");
				tempPeer.portno=Integer.parseInt(prop.getProperty(strNeighbors[i]+".port"));
				neighborPeers.add(tempPeer);
			}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
	}

}
