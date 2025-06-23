package clienteBroker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataBroker {

	private final static Map<Integer, String> parsedDataStore = new ConcurrentHashMap<>();
	private final static Map<Integer, String> rawDataStore = new ConcurrentHashMap<>();
	
	public static void addDatos(int id, String datos) {
        parsedDataStore.put(id, datos);
    }

    public static Map<Integer, String> obtenerTodos() {
        return parsedDataStore;
    }

    public static String obtener(int id) {
        return parsedDataStore.get(id);
    }
    
	public static void addRawData(int id, String datos) {
		rawDataStore.put(id, datos);
	}
	
	public static Map<Integer, String> obtenerTodosRaw() {
		return rawDataStore;
	}
	
	public static String obtenerRaw(int id) {
		return rawDataStore.get(id);
	}
}
