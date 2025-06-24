package servidor;

import java.io.StringReader;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class ServerParser{

	private enum aireDireccion {Norte, Sur, Este, Oeste, Noroeste, Noreste, Suroeste, Sureste};
	private enum precipitacionTipo {Lluvia, Nieve, Granizo, Tormenta};
	private enum precipitacionIntensidad {Nula, Suave, Ligera, Moderada, Fuerte, Intensa, Extrema};
	
	
	public static Document loadXMLFromString(String xml) throws Exception
	{
	    DocumentBuilderFactory DBFactory= DocumentBuilderFactory.newInstance();
	    DocumentBuilder DocBuilder = DBFactory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return DocBuilder.parse(is);
	}
	
	public static Solicitud parseaCtrl(String query) {
		Gson gson = new Gson();
		
		return gson.fromJson(query, Solicitud.class);
	}
	
	public static String getDatosAgua(int id, int format) {
		String reportAgua;
		Gson gson = new Gson();
		if(format == 0) {
			reportAgua = "<?xml version=\"1.0\"?>\r\n"
					+ "<report servername=\"Server" + id + "\" formato=\"XML\" tipo=\"agua\">\r\n"
					+ "	 <datos>\r\n"
					+ "	    <agua>\r\n"
					+ "	        <temperatura>" + ThreadLocalRandom.current().nextInt(-30, 64) + "</temperatura>\r\n"
					+ "	        <nivel>" + ThreadLocalRandom.current().nextInt(1, 101)+"</nivel>\r\n"
					+ "	        <ph>" + String.valueOf(ThreadLocalRandom.current().nextDouble(2.0, 8.0)).substring(0, 4) + "</ph>\r\n"
					+ "	    </agua>\r\n"
					+ "	 </datos>\r\n"
					+ "</report>";	
		}else {
			JsonObject report = new JsonObject();
			report.addProperty("servername", "Server" + id);
			report.addProperty("formato", "json");
			report.addProperty("tipo", "agua");
			
			JsonObject datos = new JsonObject();
			JsonObject agua = new JsonObject();
			agua.addProperty("temperatura", ThreadLocalRandom.current().nextInt(-30, 64));
			agua.addProperty("nivel", ThreadLocalRandom.current().nextInt(1, 101));
			agua.addProperty("ph", String.valueOf(ThreadLocalRandom.current().nextDouble(2.0, 8.0)).substring(0, 4));
			
			datos.add("agua", agua);
			report.add("datos", datos);
			
			reportAgua = gson.toJson(report);			
		}
		return reportAgua;
	}
	
//	public static String getDatosSuelo(int id) {
//		String reportSuelo = "hola";
//		return reportSuelo;
//	}
	
	public static String getDatosAire(int id, int format) {
		String reportAire;
		Gson gson = new Gson();
		if(format == 0) {
			reportAire = "<?xml version=\"1.0\"?>\r\n"
					+ "<report servername=\"Server" + id + "\" formato=\"XML\" tipo=\"aire\">\r\n"
					+ "<datos>\r\n"
					+ "<aire>\r\n"
					+ "<temperatura>" + ThreadLocalRandom.current().nextInt(-30, 64) + "</temperatura>\r\n"
					+ "<velocidad>" + ThreadLocalRandom.current().nextInt(0, 125)+"</velocidad>\r\n"
					+ "<humedad>" + ThreadLocalRandom.current().nextInt(1, 101) + "</humedad>\r\n"
					+ "<direccion>" + aireDireccion.values()[ThreadLocalRandom.current().nextInt(0, 8)] + "</direccion>\r\n"	
					+ "</aire>\r\n" 
					+ "</datos>\r\n"
					+ "</report>";	
		}else {

	        JsonObject report = new JsonObject();
	        report.addProperty("servername", "Server" + id);
	        report.addProperty("formato", "json");
	        report.addProperty("tipo", "aire");

	        JsonObject datos = new JsonObject();
	        JsonObject aire = new JsonObject();
	        aire.addProperty("temperatura", ThreadLocalRandom.current().nextInt(-30, 64));
	        aire.addProperty("velocidad", ThreadLocalRandom.current().nextInt(0, 125));
	        aire.addProperty("humedad", ThreadLocalRandom.current().nextInt(1, 101));
	        aire.addProperty("direccion", aireDireccion.values()[ThreadLocalRandom.current().nextInt(0, 8)].toString()); // Ejemplo est�tico

	        datos.add("aire", aire);
	        report.add("datos", datos);

	        reportAire = gson.toJson(report);
		}
		return reportAire;
	}
	
	public static String getDatosPrecip(int id, int format) {
		String reportPrecip;
		Gson gson = new Gson();
		if(format == 0) {reportPrecip = "<?xml version=\"1.0\"?>\r\n"
				+ "<report servername=\"Server" + id + "\" formato=\"XML\" tipo=\"precipitacion\">\r\n"
				+ "<datos>\r\n"
				+ "<precipitacion>\r\n"
				+ "<tipo>" + precipitacionTipo.values()[ThreadLocalRandom.current().nextInt(0, 4)] + "</tipo>\r\n"
				+ "<intensidad>" + precipitacionIntensidad.values()[ThreadLocalRandom.current().nextInt(0, 7)] +"</intensidad>\r\n"
				+ "<cantidad>" + ThreadLocalRandom.current().nextInt(0, 3001) + "</cantidad>\r\n"
				+ "</precipitacion>\r\n"
				+ "</datos>\r\n"
				+ "</report>";
		}else {
			JsonObject report = new JsonObject();
			report.addProperty("servername", "Server" + id);
			report.addProperty("formato", "json");
			report.addProperty("tipo", "precipitacion");
			
			JsonObject datos = new JsonObject();
			JsonObject precip = new JsonObject();
			precip.addProperty("tipo", precipitacionTipo.values()[ThreadLocalRandom.current().nextInt(0, 4)].toString());
			precip.addProperty("intensidad", precipitacionIntensidad.values()[ThreadLocalRandom.current().nextInt(0, 7)].toString());
			precip.addProperty("cantidad", ThreadLocalRandom.current().nextInt(0, 3001));
			
			datos.add("precipitacion", precip);
			report.add("datos", datos);
			reportPrecip = gson.toJson(report); 			
		}
		
		return reportPrecip;
	}
}

