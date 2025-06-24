package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ServerQueryResponder extends Thread {
	private DatagramSocket socket;
// 	posicion 0: id del servidor
	// posicion 1: tipo del servidor [0: agua, 1: viento, 2: precipitaciones]
	// posicion 2: formato de mensajes [0: xml, 1: json]
	// posicion 3: 1/0 si est� a 0 envia mensajes, si est� a 0 no
	// posicion 4: intervalo de tiempo en ms durante el cual espera entre mensajes
	private AtomicIntegerArray datos;
	
	
	public ServerQueryResponder(DatagramSocket socket, AtomicIntegerArray datos) {
		this.socket = socket;
		this.datos = datos;
	}
	
	public void run() {
		while(true) {
			try {
				byte[] buf = new byte[256];
				DatagramPacket query = new DatagramPacket(buf, buf.length);
				socket.receive(query);
				String querystr = new String(query.getData(), 0, query.getLength());
				SocketAddress returnAddr = query.getSocketAddress();
				System.out.println("Recibido: " + querystr);
				procesaSol(ServerParser.parseaCtrl(querystr));
				String ackString = new String("Server "+ String.valueOf(datos.get(0)) + ": Se ha recibido el mensaje de control");
				byte[] buf2 = new byte[256];
				buf2 = ackString.getBytes();
				DatagramPacket ack = new DatagramPacket(buf2, buf2.length, returnAddr);
				socket.send(ack);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void procesaSol(Solicitud sol) {
		String tipoSol = sol.getTipo();
		switch(tipoSol) {
		case "formato":
			String form = sol.getFormato();
			if(form.equals("xml")) {
				this.datos.set(2, 0); 
			}else {
				this.datos.set(2, 1);
			}
			break;
		case "stop":
			this.datos.set(3, 1);
			
			break;
		case "continue":
			this.datos.set(3, 0);
			break;
		case "cambioFreq":
			int interv = sol.getIntervalo();
			datos.set(4, interv);
			break;
		}
	}
}
