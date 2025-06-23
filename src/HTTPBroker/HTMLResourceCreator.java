package HTTPBroker;

import clienteBroker.DataBroker;

public class HTMLResourceCreator {
	
	public static String creaIndex(String recurso) {
		String doc = "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Bienvenido al Broker de datos meteorológicos</h2>\r\n"
				+ ""
				+ "<p>recurso pedido: " +  recurso + "</p>\r\n"
				+ "</body>\r\n</html>";
		return doc;
	};
	
	public static String creaMeteorologia() {
		String doc = "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Datos meteorológicos:</h2>\r\n";
		if (DataBroker.obtenerTodos().isEmpty()) {
			doc += "<p>No hay datos disponibles.</p>\r\n";
        } else {
            doc += "<ul>\r\n";
            for (String id : DataBroker.obtenerTodos().keySet()) {
                doc += "<li>" + id + ": " + DataBroker.obtener(id) + "</li>\r\n";
            }
            doc += "</ul>\r\n";
		}
		doc	+= "</body>\r\n</html>";
		return doc;
	}
	
	public static String crea404() {
		return "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n"
				+ "<h2>Error 404: Estos no son los recursos que estabas buscando :(</h2>\r\n" 
				+ "</body>\r\n</html>";
	}
}
