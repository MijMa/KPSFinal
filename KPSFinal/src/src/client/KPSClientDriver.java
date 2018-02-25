package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import server.KPSServerIF;

/**
 * @author Mio Mattila
 * @author Paula Heino
 *  ajetaan main-metodi ja yhdistetaan clientti serverin nimella nimirekisteriin
 *  luodaan uusi client Threadiin ja startataan clientin run-metodi
 *  Pelaaja syottaa valitsemansa nimen nimen.
 */

public class KPSClientDriver {
	
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		String serverURL = "rmi://localhost/KPS";
		String nimi = luoNimi();
		//Testausta varten
	    //String nimi = "Pertti";
		KPSServerIF KPSServer = (KPSServerIF) Naming.lookup(serverURL);
		System.out.println("Pelaaja luotu: "+nimi);
		new Thread(new KPSClient(nimi, KPSServer)).start();
	}
	//Pelaaja luo talla nimen
	private static String luoNimi()throws RemoteException{
		Scanner scanner = new Scanner(System.in);
		String syotto;
		System.out.println("Anna nimi: ");
		syotto = scanner.nextLine();
		return syotto;
	}
} 
