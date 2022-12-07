import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class Main{
    public static void main(String[] args)  {
        Scanner scan = new Scanner(System.in);
		String serverName = "localhost";
        String PeerFilesdirectory; //repertoire contenant les fichiers à partager avec le réseau
        int ports;
        int serverPorts;
        int nbMsgCount = 0; // pour l'utiliser au msgID
        String msgID;
        int TTL; //Durée de vie d'un message
        ArrayList<Thread> thread=new ArrayList<Thread>();				
		ArrayList<Client> peers=new ArrayList<Client>();		//Pour stocker tous les threads clients
        try{
            System.out.println("Enter the peerID : ");
            int peerID=scan.nextInt(); //lecture de l'ID du peer
            scan.nextLine();
            System.out.print("___________\n\n");
            System.out.println("Enter the shared directory : "); 		//Répertoire local du Peer
            PeerFilesdirectory=scan.nextLine(); //lecture du répertoire du fichier
            System.out.print("___________\n\n");
            System.out.println("Peer "+peerID+" joined the network with shared directory "+ PeerFilesdirectory);
            Properties prop = new Properties(); //pour lire les propriétés de la topologie à partir de toplogy.txt
            String propFileName = "topology.txt";
            InputStream is = new FileInputStream(propFileName);
            prop.load(is); //charger le contenu de topology.txt dans une structure Propriétés

            ports=Integer.parseInt(prop.getProperty("peer"+peerID+".port")); //recuperer le parametre peer"peerID".port qui existe dans le fichier topology.txt
            serverPorts=Integer.parseInt(prop.getProperty("peer"+peerID+".serverport")); //recuperer le parametre peer"peerID".serverport qui existe dans le fichier topology.txt
            System.out.print("___________\n\n");
            System.out.println("ports : "+ports+" || serverPorts : "+serverPorts); 


            //Initialisation de la classe DownloadFromServer pour l'écoute de téléchargements
            DownloadFromServer download = new DownloadFromServer(serverPorts, PeerFilesdirectory);
            download.start();

            //Initialisation de la classe Server pour définir le peer en tant que serveur qui acceptera les demandes de téléchargements/recherches
            Server server = new Server(ports, PeerFilesdirectory, peerID);
            server.start();


            System.out.print("___________\n\n");
            System.out.println("\nEnter\n\t1 To download a file\n");
			int ch=scan.nextInt(); //lecture du caractère "1" pour confirmer le téléchargement du fichier

            // variable pour savoir s'il y a un file à downloader
            boolean fileToDownload_exist = false;

            while(true){
                scan.nextLine();
                if(ch==1)
                {
                        System.out.println("Enter the file to be downloaded : ");
                        fileToDownload_exist = true; //un fichier à télécharger existe

                        //PeerFilesdirectory => le répertoire où nous allons envoyer le fichier
                        String currentDir = System.getProperty("user.dir"); // Pour trouver le répertoire où le code est installé
                        Path path = Paths.get(currentDir+"\\PeerFilesdirectory.txt"); //récupérer le Path du fichier où on va écrire le répertoire
                        // de celui qui a fait le telechargement

                        try {
                            Files.writeString(path, PeerFilesdirectory,StandardCharsets.UTF_8); //ecrire le reperoite où on va telecharger dans un fichier
                        }
                        catch (IOException ex) {
                            // si le chemin de fichier PeerFilesdirectory.txt est invalide , message d'erreur:
                            System.out.print("Invalid Path");
                        }
                        
                        break;//sortir du boucle dès qu'on a une entrée "1"
                }
                else
                {
                    System.out.println("Error ! Please enter a correct variable.");
                    ch=scan.nextInt();
                }
            }


            //s'il existe un file à downloader, cette partie du code sera executée:
            if (fileToDownload_exist){
                //long startTime = System.currentTimeMillis();

                //recuperer le fichier à downloader
                String fileToDownload_Name=scan.nextLine();
                System.out.println("The file to be downloaded is :  "+fileToDownload_Name);
                
                System.out.println("~~[[ Start time : "+System.currentTimeMillis());
                ++nbMsgCount;
                msgID= peerID +"."+nbMsgCount;

                //Création d'un thread client pour chaque pair voisin
                String[] neighbours=prop.getProperty("peer"+peerID+".next").split(","); 
                TTL = neighbours.length; //ttl selon la longueur de la liste des voisins
                
                
                for(int i=0;i<neighbours.length;i++){
                    int connectionPort = Integer.parseInt(prop.getProperty("peer"+neighbours[i]+".port")); //port de connection pour chaque voisin Peer
                    int neighbourPeer = Integer.parseInt(neighbours[i]); //recuperation des voisins peers
                     
                    System.out.println("connection port : "+connectionPort+" || neighbourPeer : "+neighbourPeer);
                    //Initialisation d'un client voisin selon les données dans topology.txt
                    Client client = new Client(connectionPort, neighbourPeer, fileToDownload_Name, msgID, peerID, TTL);
                    //execution du client voisin dans un nouveau thread
                    Thread t=new Thread(client);
				    t.start();
					thread.add(t);
			    	peers.add(client); //ajout du client dans la liste des clients
                    
                    }

                }
                
                for(int i=0;i<thread.size();i++){

				    try {
						//Attendez que tous les threads clients aient fini de s'exécuter
						((Thread) thread.get(i)).join();
                        
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
        }
            catch(IOException io){
            io.printStackTrace();
        }
    }

    
}