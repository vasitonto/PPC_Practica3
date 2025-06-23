package HTTPBroker;

import clienteBroker.DataBroker;

public class HTMLResourceCreator {
	
	public static String creaIndex(String recurso) {
		String doc = "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Bienvenido al Broker de datos meteorol&oacutegicos</h2>\r\n"
				+ ""
				+ "<p>Desde aqu&iacute puedes consultar los datos proporcionados por las estaciones meteorol&oacutegicas </p>\r\n"
				+ "<p>Pincha <a href=\"http://localhost:8000/meteorologia.html\">aqu&iacute</a>"
					+ " para acceder a los datos de todos los servidores</p>\r\n"
				+ "<p>Tambi&eacuten puedes hacer peticiones a nuestra API. Abajo encontrar&aacutes los comandos disponibles</p>\r\n"
				//TODO cambiar el mailto
				+ "<p>Por &uacuteltimo, tambi&eacuten puedes recibir los datos por correo pinchando en <a href=\"mailto:m.bluth@example.com\">este enlace</a></p>\r\n"
				+ "<p>Simplemente, env&iacutea un correo a esa direcci&oacuten</p>"
				+ "<br>"
				+ "<h3>Como usar la API REST</h3>\r\n"
				+ "<p>Es muy sencillo, s&oacutelo tienes que incluir \"/apirest\" como direcci&oacuten del recurso, seguido del servidor al que le quieras enviar la petici&oacuten, y los par&aacutemetros para hacer la consulta</p>\r\n"
				+ "<p>Los servidores pueden ser 1, 2 o 3</p>\r\n"
				+ "<p>A uno de estos servidores puedes pedirle que muestre sus valores con \"/valores\",\r\n"
				+ " o enviarle un mensaje de control con \"/control\", aunque &eacuteste &uacutelimo tendr&aacute que ir seguido de m&aacutes informaci&oacuten</p>\r\n"
				+ "<br>"
				+ "<h3>Enviando mensajes de control</h3>\r\n"
				+ "<p>Para enviar un mensaje de control tendr&aacutes que escribir \"/control\" seguido de uno de los siguientes comandos:</p>\r\n"
				+ "<ul>\r\n"
					+ "<li>/stop: Har&aacute que el servidor seleccionado deje de enviar mensajes, hasta que reciba un \"/continue\"</li>\r\n"
					+ "<li>/continue: Har&aacute que el servidor seleccionado reanude el env&iacuteo de mensajes si ha dejado de enviarlos</li>\r\n"
					+ "<li>/send_json: Har&aacute que el servidor seleccionado env&iacutee los mensajes en formato JSON</li>\r\n"
					+ "<li>/send_xml: Lo mismo que el anterior, pero en formato XML</li>\r\n"
					+ "<li>/mod_freq: Har&aacute que el servidor cambie la frecuencia de env&iacuteo de mensajes. Debe ir seguido de \"?freq=X\", siendo X el espacio entre mensajes deseado en ms.\n"
					+ "Si no tiene este formato espec&iacutefico, no funcionar&aacute.</li>\r\n"
				+ "</ul>\r\n"
				+ "<br>"
				+ "<h3>Algunos ejemplos</h3>\r\n"
				+ "<ul>\r\n"
					+ "<li><a href=\"http://localhost:8000/apirest/2/valores\">http://localhost:8000/apirest/2/valores</a></li>\r\n"
					+ "<li><a href=\"http://localhost:8000/apirest/1/control/stop\">http://localhost:8000/apirest/1/control/stop</a></li>\r\n"
					+ "<li><a href=\"http://localhost:8000/apirest/3/control/send_json\">http://localhost:8000/apirest/3/send_json</a></li>\r\n"
					+ "<li><a href=\"http://localhost:8000/apirest/1/control/mod_freq?freq=2000\">http://localhost:8000/apirest/1/mod_freq?freq=2000</a></li>\r\n" 
				+ "</ul>\r\n"
				+ "</body>\r\n</html>";
		return doc;
	};
	
	public static String creaMeteorologia() {
		String doc = "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Datos meteorol&oacutegicos:</h2>\r\n";
		if (DataBroker.obtenerTodos().isEmpty()) {
			doc += "<p>No hay datos disponibles.</p>\r\n";
        } else {
            doc += "<ul>\r\n";
            for (int id : DataBroker.obtenerTodos().keySet()) {

                doc += "<li>" + DataBroker.obtener(id).replace("\u00B0", "&deg") + "</li>\r\n";
            }
            doc += "</ul>\r\n";
		}
		doc	+= "</body>\r\n</html>";
		return doc;
	}
	
	public static String crea404() {
		return "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Error 404: Busca en tu interior, sabes que la URL no es correcta...</h2>\r\n"
				+ "</body>\r\n</html>";
	}
	
	public static String crea404Servidores() {
		return "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Error 404: Lo siento, estos no son los servidores que estabas buscando...</h2>\r\n"
				+ "</body>\r\n</html>";
	}
	
	public static String crea400() {
		return "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Error 400: Vaya, parece que has escrito mal la sintaxis, pero no pasa nada, era una conversación aburrida de todas formas</h2>\r\n"
				+ "</body>\r\n</html>";
	}
	
	public static String creaMensajeEnviado() {
		return "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Mensaje enviado!</h2>\r\n"
				+ "<p>Puedes volver a la p&aacutegina principal pinchando <a href=\"http://localhost:8000/index.html\">aqu&iacute</a>"
				+ "</body>\r\n</html>";
	}
}
