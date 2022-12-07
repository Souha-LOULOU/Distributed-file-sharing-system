package gnutellaP2P;
import java.io.Serializable;

// classe pour les détails du peer
public class PeerDetails implements Serializable {

private static final long serialVersionUID = 1L;
public int peerId;
public String hostIp;
public int port;
}
