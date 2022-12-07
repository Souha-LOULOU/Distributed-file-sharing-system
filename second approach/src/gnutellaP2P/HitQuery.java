package gnutellaP2P;

import java.io.Serializable;
import java.util.ArrayList;

//class pour HitQuery : réponse des voisins
public class HitQuery implements Serializable {

	private static final long serialVersionUID = 1L;
	public ArrayList<PeerDetails> foundPeers = new ArrayList<PeerDetails>();
	public ArrayList<String> paths = new ArrayList<String>();
}
