package lanzador;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import clienteBroker.Client;
import servidor.Server;

public class Lanzador {
	private static ExecutorService exec;
	public static void main(String[] args) {
		exec = java.util.concurrent.Executors.newFixedThreadPool(2);
		exec.submit(() -> {
			try {
				Server.main(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		Client clienteBroker = new Client();
		exec.submit(() -> clienteBroker.run());
	}
}
