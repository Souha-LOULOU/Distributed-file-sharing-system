package gnutellaP2P;
import java.rmi.*;
//
// Interface pour les méthodes remote
public interface PeerInterface extends Remote{
	public byte[] obtain(String filename)throws RemoteException;
	public HitQuery query(int fromPeerId,String msgId,String fileName)throws RemoteException;
}
