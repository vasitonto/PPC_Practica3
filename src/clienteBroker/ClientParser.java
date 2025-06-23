package clienteBroker;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Resources.ControlCodes;

public class ClientParser {
	//TODO hacer tratamiento de errores
	public static Document loadXMLFromString(String xml) throws ParserConfigurationException, SAXException, IOException {
	    DocumentBuilderFactory DBFactory= DocumentBuilderFactory.newInstance();
	    DocumentBuilder DocBuilder = DBFactory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    Document doc = DocBuilder.parse(is);
	    doc.getDocumentElement().normalize();
	    return doc;
	}
	
public static String parsearPaqueteXML(String msg) {
		try {
			Document reportDoc = ClientParser.loadXMLFromString(msg);
			
			Element root = reportDoc.getDocumentElement();                
			// accedemos a los atribs. del nodo ra�z
			String servername = root.getAttribute("servername");
			String formato = root.getAttribute("formato");
			String tipo = root.getAttribute("tipo");
			
			String texto = servername + " [formato: " + formato + ", datos: "+ tipo + "]: ";
			
			NodeList datosList = reportDoc.getElementsByTagName("datos");
			Node datosNode = datosList.item(0);
			NodeList listaValores= ((Element) datosNode).getElementsByTagName(tipo);
			Node nodoValores = listaValores.item(0);
			// seg�n del tipo que sea el mensaje lo deberemos parsear de una forma u otra
			switch(tipo) {
			case "agua":
				// obtenemos el elemento "datos"
				
				// obtenemos los elementos dentro de "agua"
				
				String temperaturaAgua = ((Element) nodoValores).getElementsByTagName("temperatura").item(0).getTextContent();
				String nivel = ((Element) nodoValores).getElementsByTagName("nivel").item(0).getTextContent();
				String ph = ((Element) nodoValores).getElementsByTagName("ph").item(0).getTextContent();
				texto += "temperatura: " + temperaturaAgua + "\u00B0C, nivel: " + nivel + "cm, ph: " + ph + "\n\n";
				return texto;
				
			case "aire":				
				// obtenemos los elementos para "viento"	
				String temperaturaViento = ((Element) nodoValores).getElementsByTagName("temperatura").item(0).getTextContent();
				String humedad = ((Element) nodoValores).getElementsByTagName("humedad").item(0).getTextContent();
				String direccion = ((Element) nodoValores).getElementsByTagName("direccion").item(0).getTextContent();
				String velocidad = ((Element) nodoValores).getElementsByTagName("velocidad").item(0).getTextContent();
				texto += "temperatura: " + temperaturaViento + "\u00B0C, humedad: " + humedad 
						+ "%, direccion: " + direccion + ", velocidad: " + velocidad + "km/h\n\n";
				return texto;
				
			case "precipitacion":
				String tipoPrecip = ((Element) nodoValores).getElementsByTagName("tipo").item(0).getTextContent();
				String intensidad = ((Element) nodoValores).getElementsByTagName("intensidad").item(0).getTextContent();
				String cantidad = ((Element) nodoValores).getElementsByTagName("cantidad").item(0).getTextContent();
				texto += "tipo: " + tipoPrecip + ", intensidad: " 
						+ intensidad + ", cantidad: " + cantidad + "mm\n\n" ;
				return texto;
				
			default: return "";
			}
//			textAreaSalida.setCaretPosition(textAreaSalida.getDocument().getLength());
			
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return "";
    }
	
	public static String parsearPaqueteJSON(String msg) {
		
		JsonObject report = JsonParser.parseString(msg).getAsJsonObject();

        String servername = report.get("servername").getAsString();
        String formato = report.get("formato").getAsString();
        String tipo = report.get("tipo").getAsString();

        String texto = servername + " [formato: " + formato + ", datos: " + tipo + "]: ";

        JsonObject datos = report.getAsJsonObject("datos");
        JsonObject valores = datos.getAsJsonObject(tipo);

        switch (tipo) {
            case "agua":
                int temperatura = valores.get("temperatura").getAsInt();
                int nivel = valores.get("nivel").getAsInt();
                String ph = valores.get("ph").getAsString();
                texto += "temperatura: " + temperatura + "\u00B0C, nivel: " + nivel + "cm, ph: " + ph + "\n\n";
                break;

            case "aire":
                int tempAire = valores.get("temperatura").getAsInt();
                int velocidad = valores.get("velocidad").getAsInt();
                int humedad = valores.get("humedad").getAsInt();
                String direccion = valores.get("direccion").getAsString();
                texto += "temperatura: " + tempAire + "\u00B0C, velocidad: " + velocidad + "km/h, CO2: " 
                		+ humedad + "%, direcci\u00F3n: " + direccion + "\n\n";
                break;

            case "precipitacion":
            	String tipoPrecip = valores.get("tipo").getAsString();
            	String intensidad = valores.get("intensidad").getAsString();
                int cantidad = valores.get("cantidad").getAsInt();
                texto +=  "tipo: " + tipoPrecip + "intensidad: " + intensidad + "cantidad: " + cantidad + "mm\n\n";
                break;

            default:
                texto += "tipo de datos no reconocido.\n\n";
                break;
        }

        return texto;
    }

	// crea un mensaje de control seg�n el tipo que sea, puede incluir un dato para el cambio de frecuencia
	public static String creaControl(ControlCodes codigo, int dato) {		
		Gson gson = new Gson();
	    Map<String, Object> mensaje = new HashMap<>();

	    switch(codigo) {
	        case STOP -> mensaje.put("solicitud", "stop");
	        case CONTINUE -> mensaje.put("solicitud", "continue");
	        case SEND_JSON -> {
	            mensaje.put("solicitud", "formato");
	            mensaje.put("formato", "json");
	        }
	        case SEND_XML -> {
	            mensaje.put("solicitud", "formato");
	            mensaje.put("formato", "xml");
	        }
	        case MOD_FREQ -> {
	            mensaje.put("solicitud", "cambioFreq");
	            mensaje.put("intervalo", dato); // int, no string
	        }
	        default -> mensaje.put("solicitud", "hello");
	    }

	    return gson.toJson(mensaje);
	}
}