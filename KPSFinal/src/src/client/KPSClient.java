package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import server.KPSServerIF;

/**
 * @author Mio Mattila
 * @author Paula Heino
 * Lahettaa pelaajan komennot serveriin
 * Yllapitaa nimea ja pisteita
 * Pisteiden pitaisi mahdollisesti olla tallennettu serveriin?
 */

public class KPSClient extends UnicastRemoteObject implements KPSClientIF, Runnable{

	private String name = null;
	private KPSServerIF KPSServer;
	private int score = 0;
	private volatile boolean onPelissa;
	
	protected KPSClient(String name, KPSServerIF KPSServer) throws RemoteException {
		this.name = name;
		this.KPSServer = KPSServer;
		this.score = 0;
		KPSServer.rekisteroiClient(this);
	}
    /**
     * Kaytetaan brodcastmessage-metodin yhteydessa tulostamaan syote pelaajille 
     */
	@Override
	public void otaViesti(String message) throws RemoteException {
		System.out.println(message);

	}
    /**
     * Set- ja get metodit
     */
	public String annaNimi() throws RemoteException {
		return name;
	}
	
	public int annaScore() throws RemoteException {
		return score;
	}
	
	public void asetaScore(int i) throws RemoteException {
		this.score = i;
	}
	
	public void asetaOlotila(boolean b){
		this.onPelissa = b;
	}
	
	public boolean annaOlotila(){
		return this.onPelissa;
	}
	
    /**
     * ajamismetodi Clientille, tulostetaan ohjeet ja napataan kayttajan syote scannerilla
     * ja lahetetaan kasiteltavaksi serverille
     */
	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		String message;
		System.out.println("\nClientti kaynnistynyt, liity peliin kirjoittamalla komento 'liity'."
				+ "\nSen jalkeen anna valintasi komennoilla k, p, s (kivi, paperi, sakset)."
				+ "\nVoit poistua pelistä komennolla 'poistu'."
				+ "\nListan kaikista komennoista saat komennolla 'help'");
		
		while(true){
			message = scanner.nextLine();
			try {
				KPSServer.kasitteleKomento(message, this);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}
	}
}
