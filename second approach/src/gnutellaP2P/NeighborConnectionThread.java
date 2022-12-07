package gnutellaP2P;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

//classe pour la gestion de la connexion entre les voisins
public class NeighborConnectionThread extends Thread{
		
int port;
String ip;
String fileName;
String msgId;
int fromPeerId;
int fromPeerPort;
ArrayList<PeerDetails> filesFoundat = new ArrayList<PeerDetails>();
HitQuery hitQueryResult = new HitQuery();
String toPeerId;

NeighborConnectionThread(String ip,int port, String fileName, String msgId,int fromPeerId,String toPeerId){
	this.ip=ip;
	this.port=port;
	this.fromPeerId=fromPeerId;
	this.fileName=fileName;;
	this.msgId=msgId;
	this.toPeerId=toPeerId;
}
	@Override
	public void run() {
		PeerInterface peer=null;
			 try {
				 // Etablir la connexion avec le voisin
				peer=(PeerInterface) Naming.lookup("rmi://"+ip+":"+port+"/peerServer");
				// Appel de la méthode remote query
				hitQueryResult=peer.query(fromPeerId,msgId, fileName);			
			 } catch (MalformedURLException | RemoteException | NotBoundException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				 System.out.println("Unable to connect to " + toPeerId +" : "+e.getMessage());
			}
		
	}
	public HitQuery getValue(){
		// retourner le résultat de HitQuery
		return hitQueryResult;
	}

}
