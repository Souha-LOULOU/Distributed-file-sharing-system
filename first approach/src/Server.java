import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

public class Server extends Thread{
    int peerID;
    String directory;
    int port;
    ServerSocket serverSocket = null;
    Socket socket = null;
    String msg;
    static ArrayList<String> msgList;
    static boolean duplicateStatus = false;
    static int[] peersHaveTheFile = new int[10];
    public Server(int port, String directory, int peerID) {
        this.port = port;
        this.directory = directory;
        this.peerID = peerID;
        msgList=new ArrayList<String>();
    }

    public void run(){
        //Création d'un socket de serveur
        try{
            serverSocket = new ServerSocket(port);
        }
        catch(IOException io){
            io.printStackTrace();
        }
        //Accepter la création d'un socket serveur pour chaque demande
        while(true){
            try{
                socket = serverSocket.accept();
                System.out.println("Connected to client at "+socket.getRemoteSocketAddress()+" with peer "+peerID);
                //Initialiser la classe Download pour écouter les telechargements
				new Download(socket,directory,peerID,msgList).start();
            }
            catch(IOException io)
			{
				io.printStackTrace();
			}
        }
    }

//classe qui sera utilisée dans la recherche des fichiers dans les repertoires concernés
    class MyFilenameFilter implements FilenameFilter {
    
        String initials;
        
        // constructor to initialize object
        public MyFilenameFilter(String initials)
        {
            this.initials = initials;
        }
        
        // overriding the accept method of FilenameFilter
        // interface
        public boolean accept(File dir, String name)
        {
            return name.startsWith(initials); //recuperer le fichier qui a ses critères
        }
    }
    
    
    class Download extends Thread{
        // la classe qui va s'occuper de telechargement du point de vue ClientasAserver
        Socket socket;
        String directory;
        int peerID;
        MessageSpecs message = new MessageSpecs();
        String msg;
        String fileToDownload_Name;
        ArrayList<Thread> thread=new ArrayList<Thread>();
        
	    int countofpeers=0;
        
        ArrayList<String> msgList; 

        public Download(Socket socket, String directory, int peerID, ArrayList<String> msgList){
            this.socket = socket;
            this.directory = directory;
            this.peerID = peerID;
            this.msgList = msgList;
        }
        //methode du rechehrche du fichier
        public boolean SearchFile(String searchedFile, String directory){
            boolean foundFile = false;
            String fileName = "";
            // Créer un objet de la classe File
            // Remplacez le chemin du fichier par le chemin du répertoire
            File directoryOfSearch = new File(directory);
             // Créer un objet de la classe MyFilenameFilter
            // Constructeur avec le nom du fichier recherché
            MyFilenameFilter filter= new MyFilenameFilter(searchedFile);
            // stocker tous les noms avec le même nom
            // avec ou sans extension
            String[] flist = directoryOfSearch.list(filter);
            //Tableau vide
            if (flist == null) {
                System.out.println("Empty directory or directory does not exists.");
            }
            else {
    
                // Imprimer tous les fichiers avec le même nom dans le répertoire
                // comme fourni dans l'objet de la classe MyFilenameFilter
                for (int i = 0; i < flist.length; i++) {
                    if (i==0){
                        fileName = flist[0];
                        if (fileName != ""){
                            System.out.println("File : "+flist[i]+" found");
                            foundFile = true;
                        }
                        break;
                    }
                }
            }
        return foundFile;
    }
        
        
        public void run(){
            try{
                System.out.println("- Server thread for peer"+peerID);
                //les flux entrées/sorties d'objets
                InputStream is = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                boolean peerduplicate;

                message = (MessageSpecs)ois.readObject(); //recuperation du message envoyé 

                System.out.println("- Got request from "+message.senderPeerID);
                fileToDownload_Name = message.fileName; //le fichier à telecharger

                peerduplicate = this.msgList.contains(message.msgID); //verifier si l'ID du message existe dans la liste des IDs de messages
                if (peerduplicate==false){
                    //si l'ID indique que le message n'est pas dupliqué, l'ajouter dans la liste des IDs de messages
                    this.msgList.add(message.msgID);
                }
                else{
                    //afficher un message d'erreur s'il y a un message dupliqué qui circule dans le réseau
                    System.out.println("Error : duplicate msg !");
                    Server.duplicateStatus = true;
                    
                }
                //si le message n'est pas dupliqué on fait ce traitement:
                if (!peerduplicate){
                    boolean foundFile = SearchFile(fileToDownload_Name, directory); //rechercher le fichier à telecharger dans le repertoire donné
                    if (foundFile == true){
                        Server.peersHaveTheFile[countofpeers++]=peerID; //les peers qui ont le fichier
                        
                        System.out.println("Peers that have the file : "+Server.peersHaveTheFile[0]);
                        countofpeers++; //incrementer le nombre de peers qui ont le fichier
                    }
                    else{
                        System.out.println("File not found");
                    }
                    Properties prop = new Properties();
                    String fileName = "topology.txt"; //recuperer le fichier "topology.txt" en tant qu'un object de la classe Properties
                    is = new FileInputStream(fileName);
                    prop.load(is);
                    //prendre la valeur du voisin peer
                    String temp=prop.getProperty("peer"+peerID+".next");
                    
                    //si le prochain peer n'est pas null et si la valeur du TTL n'est pas encore nulle on fait ce traitement:
                    if(temp!=null && message.TTL > 0){
                        //sauvegarder la liste des voisins peers dans la liste de chaines neighbors
                        String[] neighbours=temp.split(",");
                        //parcourir la liste des voisins
                        for(int i=0;i<neighbours.length;i++)
                        {   
                            if(message.senderPeerID==Integer.parseInt(neighbours[i]))	
                            //créer un thread client pour tous les pairs voisins (meme principe que dans la classe Main)
                            {
                                continue;
                            }
                            int connectionPort=Integer.parseInt(prop.getProperty("peer"+neighbours[i]+".port"));
                            int neighbourPeer=Integer.parseInt(neighbours[i]);
                            
                            System.out.println("- Sending to "+neighbourPeer);
                            Client c=new Client(connectionPort,neighbourPeer,fileToDownload_Name,message.msgID,peerID,message.TTL--);
                            Thread t=new Thread(c);
                            t.start();
                            thread.add(t);
                            
                        }
                    }
                
                    for(int i=0;i<thread.size();i++){
                            //attendre la fin d'execution des threads
                            ((Thread) thread.get(i)).join();
                            
                            
                    }

                    int peerswithfiles= Server.peersHaveTheFile[0];
                    int peerfromdownload = peerswithfiles; //le peer qui a le fichier
                    
                    //si le peer qui a le fichier n'est pas null, on fait ce traitement:
                    if (peerfromdownload != 0){
                        //Initialisation de la classe "ClientasServer" pour faire l'envoi du fichier vers le repertoire voulu
                        int porttodownload=Integer.parseInt(prop.getProperty("peer"+peerfromdownload+".serverport"));
                        ClientasServer(peerfromdownload,porttodownload,fileToDownload_Name,directory);

                        String currentDir = System.getProperty("user.dir"); // Pour trouver le répertoire où le code est installé

                        Path filePEERname= Path.of(currentDir+"\\PeerFilesdirectory.txt"); //trouver le nom de fichier concerné pour le telechargement
                
                        String PeersFilesDirectory = Files.readString(filePEERname);

                        System.out.println("File: "+fileToDownload_Name+" downloaded from Peer "+peerfromdownload+" to Peer "+PeersFilesDirectory);
                    }else{
                        System.out.println("Error! No peer available");
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        //la classe ClientasServer qui s'occupera du telechargement au niveau des noeuds Clients
        public void ClientasServer(int ClientPeerID,int ServerPortNb,String filename,String sharedDir)
	{																													
		try{
            
			Socket clientasserversocket=new Socket("localhost",ServerPortNb); //creation du socket
			// Les flux d'objets entrée et sortie
            ObjectOutputStream ooos=new ObjectOutputStream(clientasserversocket.getOutputStream());
			ooos.flush();
			ObjectInputStream oois=new ObjectInputStream(clientasserversocket.getInputStream());
			ooos.writeObject(filename);
            //lecture du longueur fichier en tant qu'une variable de bytes
			int readbytes=(int)oois.readObject();
			System.out.println("bytes transferred: "+readbytes);
			
            //lecture du fichier en tant qu'une variable de bytes
            byte[] b=new byte[readbytes];
			oois.readFully(b);
            
            
            String currentDir = System.getProperty("user.dir"); // Pour trouver le répertoire où le code est installé

            Path fileName= Path.of(currentDir+"\\PeerFilesdirectory.txt"); //trouver le nom de fichier concerné pour le telechargement
        
 
            String PeersFilesDirectory = Files.readString(fileName);// Pour lire le fichier PeerFilesdirectory.txt contenant le nom du repertoire où on sauvegardera le telechargement
            sharedDir = PeersFilesDirectory;

            
            //fichier à recevoir
			OutputStream fileos=new FileOutputStream(sharedDir+"//"+filename);
			BufferedOutputStream bos=new BufferedOutputStream(fileos);
			bos.write(b, 0,(int) readbytes); //envoi du fichier octet par octet
            
            System.out.println("~~[[ End time : "+System.currentTimeMillis());
	        System.out.println(filename+" file has been downloaded to your directory "+sharedDir);
            
            bos.flush();
            
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
    }
    
}
