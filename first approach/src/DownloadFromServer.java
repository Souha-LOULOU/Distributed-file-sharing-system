import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadFromServer extends Thread{
    //cette classe servira pour le telechargement depuis le serveur
    int nbPort;
    String directory;
    ServerSocket serverSocket;
    Socket socket;
    DownloadFromServer(int nbPort, String directory){
        this.nbPort = nbPort;
        this.directory = directory;
    }

    public void run(){
        try{
            serverSocket = new ServerSocket(nbPort); //creation d'un socket serveur
        }
        catch(IOException io){
            io.printStackTrace();
        }

        try{
            socket = serverSocket.accept();
        }
        catch(IOException io){
            io.printStackTrace();
        }
        //initialisation de la classe Telechargement pour lancer un nouveau telechargement de ce niveau
        new Telechargement(socket, nbPort, directory).start();
    }
    
}

class Telechargement extends Thread{
    //cette classe est où se déroule le telechargement au niveau d'un Peer qui se comporte comme un serveur
    int nbPort;
    Socket socket;
    String directory;
    String fileName;
    Telechargement(Socket socket, int nbPort, String directory){
        this.socket = socket;
        this.nbPort = nbPort;
        this.directory = directory;
    }

    public void run(){
        try{
            // les flux d'entrée et sortie d'objets
            InputStream is=socket.getInputStream();		    //Connecter le Client jouant le role de serveur au fichier Client demandeur
			ObjectInputStream ois=new ObjectInputStream(is);
			OutputStream os=socket.getOutputStream();
			ObjectOutputStream oos=new ObjectOutputStream(os);

            fileName=(String)ois.readObject();					//Nom du fichier à télécharger

            //while(true)
			//	{
					File myFile = new File(directory+"//"+fileName);
		            long length = myFile.length();
                    System.out.println("File length: "+length+ " File name : "+fileName);
                    
                    //******* */
                    String currentDir = System.getProperty("user.dir"); // Pour trouver le répertoire où le code est installé
                    
                    Path fileName= Path.of(currentDir+"\\PeerFilesdirectory.txt"); //trouver le nom de fichier concerné pour le telechargement
        
                    // la methode Files.readString() sera utilisee pour la lecture du fichier
                    String PeersFilesDirectory = Files.readString(fileName);
                    
                    directory = PeersFilesDirectory;
		            
                    byte [] mybytearray = new byte[(int)length];		//Longueur du fichier d'envoi du fichier à télécharger au client
		            oos.writeObject((int)myFile.length());
		            oos.flush();
                    
                    if (myFile.isFile() && length>0){
                        FileInputStream fileInSt=new FileInputStream(myFile);
                        BufferedInputStream objBufInStream = new BufferedInputStream(fileInSt); 
                        
                        //Transfert du contenu du fichier sous forme de flux d'octets		                
                        System.out.println("- Sending file of " +mybytearray.length+ " bytes");
                        objBufInStream.read(mybytearray,0,mybytearray.length);
                    
                        oos.write(mybytearray,0,mybytearray.length);
                        oos.flush(); 
                    }else{
                        System.out.println("Error ! Fichier introuvable");
                    }
                //}
        }catch(Exception e){
			e.printStackTrace();
		}
    }
}
