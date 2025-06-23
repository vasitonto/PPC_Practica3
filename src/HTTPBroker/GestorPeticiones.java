package HTTPBroker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import clienteBroker.BrokerActionListener;

public class GestorPeticiones extends Thread {

	private Socket s;
	private BrokerActionListener brokerListener;
	
	public GestorPeticiones(Socket s, BrokerActionListener brokerListener) {
		super("GestorPeticiones");
		this.s = s;
		this.brokerListener = brokerListener;
	}
	
	@Override
	public void run() {
			
		BufferedReader sIn;
		PrintWriter sOut;
		String texto;
		String respuesta;
		String cuerpoRespuesta;
		String cookies = "";
		String recurso = "";
		ServerHeaderFactory creadorCabeceras = new ServerHeaderFactory();
		SetCookieHeaderGenerator creadorCookies = new SetCookieHeaderGenerator();
		try {
			sIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
			sOut = new PrintWriter(s.getOutputStream());
			
			while(!s.isClosed()) {
				
				recurso = "";
		        cookies = "";
		        cuerpoRespuesta = "";
		        respuesta="";
		        
		        while ((texto = sIn.readLine()) != null && !texto.isEmpty()) {	
					//aqui voy a procesar la primera l�nea de la peticion
		        	if(texto.contains("exit")) break;
					if(texto.contains("GET") || texto.contains("POST") || texto.contains("HEAD")) {
						recurso = texto.split(" ")[1];
					}
					//else enviar cabecera de error
					
					if(texto.contains("Cookie:")) {
						cookies += creadorCookies.generarCookies(recurso, texto.substring(8));
					}
					respuesta += texto + "\r\n";
				}
		        System.out.print("Se ha recibido:\r\n" + respuesta);
				
				//compruebo si el string de cookies est� vac�o. Si lo est� es que es la primera conexi�n.
				if(cookies.isEmpty()) cookies = creadorCookies.generarCookiesNuevas(recurso);
				
				//preparo la respuesta con cabeceras y un HTML que devuelve el recurso pedido
				if(recurso.equals("index.html") || recurso.equals("/")) {
					cuerpoRespuesta = HTMLResourceCreator.creaIndex(recurso);
				}
				else if (recurso.equals("meteorologia.html")) {
					cuerpoRespuesta = HTMLResourceCreator.creaMeteorologia();
				} else if (recurso.equals("favicon.ico")) {
					cuerpoRespuesta = "";
				} else {
					// si no es ninguno de los anteriores, se devuelve un error 404
					cuerpoRespuesta = HTMLResourceCreator.crea404();
				}
				
				//TODO leer la peticion y ver si incluye parámetros
				
				respuesta = creadorCabeceras.generarCabeceras("text/html", cuerpoRespuesta.length(), recurso) + cookies + "\r\n\r\n" + cuerpoRespuesta + "\r\n\r\n";
				System.out.print("\nSe enviar�: \n"+ respuesta);
				sOut.print(respuesta);
				sOut.flush();
			}
			sIn.close();
			sOut.close();
			s.close();
		} catch (java.net.SocketException sockex) {
			System.out.println("Conexi�n terminada con el usuario anterior. Esperando nueva conexi�n...");
		} catch (IOException e) { e.printStackTrace (); }
	}
}
