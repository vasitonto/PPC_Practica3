package HTTPBroker;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GestorPeticion extends Thread
{
	Socket s;
	
	
	public GestorPeticion (Socket s )
	{
		this.s = s;
	}
	@SuppressWarnings("deprecation")
	public void run()
	{
		DataInputStream sIn;
		PrintStream sOut;
//		String peticion = "";
		String texto;
		String respuesta;
		String cuerpoRespuesta;
		String cookies = "";
		String recurso = "";
		ServerHeaderFactory creadorCabeceras = new ServerHeaderFactory();
		HTMLResourceCreator creadorCuerpo = new HTMLResourceCreator();
		SetCookieHeaderGenerator creadorCookies = new SetCookieHeaderGenerator();
		try {
			sIn = new DataInputStream(s.getInputStream());
			sOut = new PrintStream(s.getOutputStream());
			
			while(!s.isClosed()) {
				texto = "";
				while(texto != null && !(texto = sIn.readLine()).isEmpty()) {	
					//aqui voy a procesar la primera l�nea de la peticion
					if(texto.contains("GET") || texto.contains("POST")) {recurso = texto.split(" ")[1];}
					
					if(texto.contains("Cookie:")) cookies += creadorCookies.generarCookies(recurso, texto.substring(8));
				}
				
				//compruebo si el string de cookies est� vac�o. Si lo est� es que es la primera conexi�n.
				if(cookies.isEmpty()) cookies = creadorCookies.generarCookiesNuevas(recurso);
				
				//preparo la respuesta con cabeceras y un HTML que devuelve el recurso pedido
				cuerpoRespuesta = creadorCuerpo.creaHTML(recurso);
				respuesta = creadorCabeceras.generarCabeceras("html", cuerpoRespuesta.length(), recurso) + cookies + "\n" + cuerpoRespuesta + "\r\n";
//				System.out.print("\nAqu� est� la respuesta que se va a enviar: \n"+ respuesta + "\n");
				sOut.writeBytes(respuesta.getBytes());	
				
			}
			sIn.close();
			sOut.close();
			s.close();
		//} catch (SocketException sockex) {
			//System.out.println("Conexi�n terminada con el usuario anterior. Esperando nueva conexi�n...");
		} catch (IOException e) { e.printStackTrace (); }
	}
	/*
	 * private String gestionarCookies() {
	 * 
	 * return String;
		};
	 */
		
		
}

