package servidor;

import java.io.IOException;  
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.w3c.dom.Document;
import java.net.MulticastSocket;

public class ServerBroadcaster extends Thread{
	
	private InetSocketAddress BCADDR;
	private DatagramSocket socket;
	private byte[] buf = new byte[4096];
	
	// 	posicion 0: id del servidor
	// posicion 1: tipo del servidor [0: agua, 1: viento, 2: precipitaciones]
	// posicion 2: formato de mensajes [0: xml, 1: json]
	// posicion 3: 1/0 si está a 0 envia mensajes, si está a 0 no
	// posicion 4: intervalo de tiempo en ms durante el cual espera entre mensajes
	private AtomicIntegerArray datos;
	
	public ServerBroadcaster(InetSocketAddress dir, MulticastSocket socket, AtomicIntegerArray datos) {
		this.BCADDR = dir;
		this.socket = socket;
		this.datos = datos;
	}
	
	public void run() {
		int format;
//		for(int i = 0; i < 3; i++) {
		while (true) {
			if (datos.get(3) == 0) {
				
				try {
					String fecha = LocalDateTime.now().toString();
					String hora = fecha.substring(11, 22);
					format = datos.get(2); // 0: xml, 1: json
					
					switch(datos.get(1)) {
					case 0:
						String aber = ServerParser.getDatosAgua(datos.get(0), format);
						System.out.println("Server "+ datos.get(0) + " Enviando datos del agua");
						buf = aber.getBytes();
						break;
					case 1:
						buf = ServerParser.getDatosPrecip(datos.get(0), format).getBytes();
						System.out.println("Server "+ datos.get(0) + " Enviando datos de precipitaciones");
						break;
					case 2:
						buf = ServerParser.getDatosAire(datos.get(0), format).getBytes();
						System.out.println("Server "+ datos.get(0) + " Enviando datos del viento");
						break;
					default: break;	
					}
					DatagramPacket packet = new DatagramPacket(buf, buf.length, BCADDR);
					socket.send(packet);
					sleep(this.datos.get(4));
				}
				catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
			else
				try {
					sleep(this.datos.get(4));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}
	
}
