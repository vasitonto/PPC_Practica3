package servidor;

import java.io.IOException; 
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Server extends Thread{
	
	//TODO hacer el sistema de forma que se puedan lanzar varios servidores
	// es decir, que el n� del puerto vaya cambiando
	private static final int BCSocketPort = 4445; // puerto desde el cual se enviar�n los mensajes bcast
//	private static final int CtrlSocketPort = 4446;
	private static final int BCPORT = 4999; // puerto en el que escucha el clienteBroker
	private static final String grupoMulticast = "224.48.75.1";
	private InetSocketAddress BCADDR;
	private MulticastSocket BCSocket;
	private DatagramSocket CTRLSocket;
	//	este array contiene la siguiente informaci�n
	// 	posicion 0: id del servidor
	// posicion 1: tipo del servidor [0: agua, 1: viento, 2: precipitaciones]
	// posicion 2: formato de mensajes [0: xml, 1: json]
	// posicion 3: 1/0 si est� a 0 envia mensajes, si est� a 0 no
	// posicion 4: intervalo de tiempo en ms durante el cual espera entre mensajes
	private  AtomicIntegerArray datos;
	
	public Server(int id, int tipo, int escucha){
		this.datos = new AtomicIntegerArray(5);
	    this.datos.set(0, id);
	    this.datos.set(1, tipo);
	    this.datos.set(2, 0);
	    this.datos.set(3, 0);
	    this.datos.set(4, 3000);
		int puerto1 = BCSocketPort;
//		int puerto2 = CtrlSocketPort;
		while(true) {
			try {
				// BCADDR ser� la direcci�n a la que se enviar�n los paquetes de broadcast
				this.BCADDR = new InetSocketAddress(InetAddress.getByName(grupoMulticast), BCPORT);
				this.BCSocket = new MulticastSocket(puerto1);
				this.BCSocket.setReuseAddress(true);
				break;
			} catch (IOException e1) {
				puerto1++;
			}
		}
		while(true) {
			try {
				// para el socket de control tambi�n necesitamos un puerto nuevo, as� que 
				// le tendremos que hacer el tratamiento de errores por separado
				this.CTRLSocket = new DatagramSocket(escucha); 
//				this.CTRLSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), puerto2));
				break;		
			} catch (IOException e) {
//				puerto2++;
			}	
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		Server server = new Server(1, 0, 4446);
		server.BCSocket.joinGroup(server.BCADDR, NetworkInterface.getByName(grupoMulticast));
		ServerQueryResponder responder = new ServerQueryResponder(server.CTRLSocket, server.datos);
		ServerBroadcaster broadcaster = new ServerBroadcaster(server.BCADDR, server.BCSocket, server.datos);
		responder.start();
		broadcaster.start();
		
		Server server2 = new Server(2, 1, 4447);
		server2.BCSocket.joinGroup(server2.BCADDR, NetworkInterface.getByName(grupoMulticast));
		ServerQueryResponder responder2 = new ServerQueryResponder(server2.CTRLSocket, server2.datos);
		ServerBroadcaster broadcaster2 = new ServerBroadcaster(server2.BCADDR, server2.BCSocket, server2.datos);
		responder2.start();
		broadcaster2.start();
		
		Server server3 = new Server(3, 2, 4448);
		server3.BCSocket.joinGroup(server3.BCADDR, NetworkInterface.getByName(grupoMulticast));
		ServerQueryResponder responder3 = new ServerQueryResponder(server3.CTRLSocket, server3.datos);
		ServerBroadcaster broadcaster3 = new ServerBroadcaster(server3.BCADDR, server3.BCSocket, server3.datos);
		responder3.start();
		broadcaster3.start();
	}
}

