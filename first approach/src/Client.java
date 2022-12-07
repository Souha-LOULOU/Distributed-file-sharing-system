import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client extends Thread {

    int connectionPort;
    int peerToConnect;
    String fileToDownload;
    Socket socket=null;
    int[] peersArray;
    
    MessageSpecs message=new MessageSpecs();
    String msgID;
    int senderPeerID;
    int TTL;
    
    public Client(int connectionPort, int peerToConnect, String fileToDownload, String msgID, int senderPeerID, int TTL) {
        this.connectionPort=connectionPort;
		this.peerToConnect=peerToConnect;
		this.fileToDownload=fileToDownload;
		this.msgID=msgID;
		this.senderPeerID=senderPeerID;
		this.TTL=TTL;
    }
    public void run(){
        try{
            socket = new Socket("localhost",connectionPort); //etablissement d'une connection avec le client
            //les flux d'entree et de sortie d'objets
            OutputStream os=socket.getOutputStream();
			ObjectOutputStream objOS=new ObjectOutputStream(os);
			InputStream is=socket.getInputStream();
			ObjectInputStream objIS=new ObjectInputStream(is);

            //construction d'un message
            message.fileName = fileToDownload;
            message.msgID = msgID;
            message.senderPeerID = senderPeerID;
            message.TTL = TTL;
            //envoi d'un message dans l'object output stream
            objOS.writeObject(message);
            
            

        }
        catch(IOException io)
        {
            io.printStackTrace();
        }
        
       
    }
    public int[] getarray()
    {
        return peersArray;
    }
}
