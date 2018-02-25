package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * @author Mio Mattila
 * @author Paula Heino
 */

/**
 * Luo serveriolion ja listaa sen RMIregistryyn.
 * Serverin nimi: KPS
 */
public class KPSServerDriver {

	public static void main(String[] args) throws RemoteException, MalformedURLException {
		Naming.rebind("KPS", new KPSServer());
		System.out.println("Server started");
		
		
	}
	

	

}