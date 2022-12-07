package gnutellaP2P;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Peer {
	public static void main(String args[]) {

		Peer peerInstance = new Peer();
		peerInstance.peerOperations(args);
	}
	// Methode qui gère les opérations qui peuvent pêtre réalisées par le peer
		//
	public void peerOperations(String args[]) {
		
		String sharedDir;
		ArrayList<String> localFiles = new ArrayList<String>();
		List<Thread> threadInstancesList = new ArrayList<Thread>();
		int port;
		int peerid;
		int searchCounter = 0;
		int choice;
		Boolean bExit = false;
		ArrayList<NeighborPeers> neighborPeers = new ArrayList<NeighborPeers>();
		String searchFileName;
		ArrayList<PeerDetails> searchResult_Peers = new ArrayList<PeerDetails>();
		ArrayList<NeighborConnectionThread> neighborConnThreadList = new ArrayList<NeighborConnectionThread>();
		//
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			//
			// entrer ID du peer
			System.out.println("Enter the peerid");
			peerid = Integer.parseInt(br.readLine());
			//entrer le port
			System.out.println("Enter the port");
			port = Integer.parseInt(br.readLine());
			System.out.println("Session for peer id: " + peerid + " started...");
			//
			// entrer le répertoire partagé du peer
			System.out.println("Enter the shared directory");
			sharedDir = br.readLine();
			
			//Appel d'une fonction getLocalFiles:
			// Lire les fichiers contenus dans le repertoire partagé
			getLocalFiles(sharedDir, localFiles);
			//Appel d'une fonction runPeerAsServer:
			// Le peer se comporte comme un serveur qui posséde le fichier désiré
			runPeerAsServer(peerid, port, sharedDir, localFiles);

			// Affichage: Menu utilisateur
			while (true) {
				System.out.println("************ Main Menu ***************");
				System.out.println("1. Search File");
				System.out.println("2. Exit");
				System.out.println("**************************************");
				System.out.println("Select your choice");
				choice = Integer.parseInt(br.readLine());
				switch (choice) {
				case 1:
					// l'option de recherche d'un fichier
					//dans ce cas le peer se comporte comme un client
					// netoyage de l'historique des recherches
					neighborPeers.clear();
					threadInstancesList.clear();
					neighborConnThreadList.clear();
					searchResult_Peers.clear();
					//entrer le fichier à chercher
					System.out.println("Enter file name to search:");
					searchFileName = br.readLine();
					
					//Appel de la fonction: getNeighborPeers
					// récupérer des voisins du peer dans la liste neighborPeers
					getNeighborPeers(neighborPeers, peerid);
					//
					// Générer un identifient unique de message
					++searchCounter;
					String msgId = "Peer1.Search" + searchCounter;
					System.out.println("Message id for search: " + msgId);
					//
					// boucle pour effectuer la recherche dans les voisins
					for (int i = 0; i < neighborPeers.size(); i++) {
						System.out.println("Sending request to " + neighborPeers.get(i).peerId + " "
								+ neighborPeers.get(i).portno);
						//instanciation d'un nouveau objet de la classe NeighborConnectionThread
						NeighborConnectionThread connectionThread = new NeighborConnectionThread(
								neighborPeers.get(i).ip, neighborPeers.get(i).portno, searchFileName, msgId, peerid,
								neighborPeers.get(i).peerId);
						// thread pour chaque connexion du voisin
						Thread threadInstance = new Thread(connectionThread);
						threadInstance.start();
						//
						// Enregistrer les threads des connexions dans la liste threadInstancesList
						// Enregistrer les objets des connexions dans la liste neighborConnThreadList
						threadInstancesList.add(threadInstance);
						neighborConnThreadList.add(connectionThread);

					}
					//
					// attendre jusqu'à la terminaison du thread fils
					for (int i = 0; i < threadInstancesList.size(); i++)
						((Thread) threadInstancesList.get(i)).join();
					//
					// Recevoir hitQuery (acquittement/réponse) de la part de tous les voisins
					System.out.println("*** Search Paths ***");
					for (int i = 0; i < neighborConnThreadList.size(); i++) {
						HitQuery hitQueryResult = (HitQuery) neighborConnThreadList.get(i).getValue();
						if (hitQueryResult.foundPeers.size() > 0) {
							//
							// Stocker les résultats de la recherche dans la liste searchResult_Peers
							searchResult_Peers.addAll(hitQueryResult.foundPeers);
						}
						//
						// Affichages des routes de recherches utilisées
						for (int count = 0; count < hitQueryResult.paths.size(); count++) {
							String path = peerid + hitQueryResult.paths.get(count);
							System.out.println("Search Path: " + path);
						}

					}
					System.out.println("*******************");
					
					if (searchResult_Peers.size() == 0) {
						System.out.println(searchFileName+" File not found in the network");
					} else {
						System.out.println(searchFileName+" File found in the network at below peers");
					}
					// Affichage des résultat de recherches
					for (int i = 0; i < searchResult_Peers.size(); i++) {
						System.out.println("--Found at Peer" + searchResult_Peers.get(i).peerId
								+ " , running on 127.0.0.1:" + searchResult_Peers.get(i).port);

					}
					// Si fichier trouvé dans plusieurs voisins 
					//Appel de la fonction selectPeerToDownload qui permettra de selectionner le peer 
					//duquel on téléchargera le fichier
					if (searchResult_Peers.size()>0){
					selectPeerToDownload(br, searchResult_Peers, searchFileName, sharedDir);
					}
					break;
				default:
					bExit = true;
				}
				if (bExit) {
					// quitter la session client
					System.exit(1);
					break;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void getLocalFiles(String sharedDir, ArrayList<String> localFiles) {
		
		File directoryObj = new File(sharedDir);
		File newfind;
		String filename;
		String[] filesList = directoryObj.list();
		for (int i = 0; i < filesList.length; i++) {
			newfind = new File(filesList[i]);
			filename = newfind.getName();
			// stocker les fichiers locaux dans une liste
			localFiles.add(filename);

		}

	}

	public void runPeerAsServer(int peerId, int port, String sharedDir, ArrayList<String> localFiles) {
		
		try {
			LocateRegistry.createRegistry(port);
			PeerInterface stub = new PeerInterfaceRemote(sharedDir, peerId, port, localFiles);
			Naming.rebind("rmi://localhost:" + port + "/peerServer", stub);
			System.out.println("Peer " + peerId + " acting as server on 127.0.0.1:" + port);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void getNeighborPeers(ArrayList<NeighborPeers> neighborPeers, int peerId) {
		
		String property = null;
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("config.properties");

			// charger le fichier de propriété de la configuration
			prop.load(input);
			property = "peerid." + peerId + ".neighbors";
			
			String[] strNeighbors = prop.getProperty(property).split(",");
			for (int i = 0; i < strNeighbors.length; i++) {
				NeighborPeers tempPeer = new NeighborPeers();
				tempPeer.peerId = strNeighbors[i];
				tempPeer.ip = prop.getProperty(strNeighbors[i] + ".ip");
				tempPeer.portno = Integer.parseInt(prop.getProperty(strNeighbors[i] + ".port"));
				neighborPeers.add(tempPeer);
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

	public void selectPeerToDownload(BufferedReader br, ArrayList<PeerDetails> searchResult_Peers, String fileName,
			String Path) {
		
		int choice;
		int peerId;
		try {
			System.out.println("***Download Menu***");
			System.out.println("1.Download file");
			System.out.println("2.Exit");
			System.out.println("*******************");
			
			System.out.println("Select operaion");
			choice = Integer.parseInt(br.readLine());
			switch (choice) {
			case 1:
				System.out.println("Enter peer id to connect and download the file");
				peerId = Integer.parseInt(br.readLine());
				download(searchResult_Peers, peerId, fileName, Path);
				break;

			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void download(ArrayList<PeerDetails> searchResult_Peers, int peerId, String fileName, String Path)
			throws IOException {
		
		int totalPeers, iCount = 0;
		int port = 0;
		String Host = null;
		totalPeers = searchResult_Peers.size();
		while (iCount < totalPeers) {
			if (peerId == searchResult_Peers.get(iCount).peerId) {
				port = searchResult_Peers.get(iCount).port;
				Host = searchResult_Peers.get(iCount).hostIp;
				break;
			}
			iCount++;
		}

		System.out.println("Downloading from " + Host + ":" + port);
		//
		// objet pour serveur peer pour le téléchargement du fichier
		PeerInterface PeerServer = null;
		try {
			PeerServer = (PeerInterface) Naming.lookup("rmi://localhost:" + port + "/peerServer");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Appel de la méthode obtain pour recupérer le fichier
		byte[] fileData = null;
		try {
			fileData = PeerServer.obtain(fileName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		// créer un nouveau fichier pour le peer courant et y stocker les données du fichier téléchargé
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(Path + "//" + fileName));
		try {
			output.write(fileData, 0, fileData.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output.flush();
		output.close();
		System.out.println("\"" + fileName + "\" downloaded to path: " + Path);

	}

}
