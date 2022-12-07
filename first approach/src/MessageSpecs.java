import java.io.Serializable;
//cette classe va definir la structure des messages echangees dans le reseau

public class MessageSpecs implements Serializable {
    String msgID; //l'id unique des messages
    int senderPeerID; //l'ID du peer qui a envoyé le message
    String fileName; //le nom de fichier à telecharger
    int TTL; //la durée de vie d'un message
}
