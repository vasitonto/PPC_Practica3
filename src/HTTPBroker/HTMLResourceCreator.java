package HTTPBroker;

public class HTMLResourceCreator {

	public HTMLResourceCreator() {};
	
	public String creaHTML(String recurso) {
		String doc = "<!DOCTYPE html>\n<html>\n<body>\n"
				+ "<h1>Bienvenido al servidor de PPC</h1>\n"
				+ "<p>recurso pedido: " +  recurso + "</p>\n"
						+ "<a href=\"https://www.w3schools.com\">hola</a> "
		  		+ "</body>\n</html>";
		return doc;
	};
}
