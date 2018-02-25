package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import client.KPSClientIF;

/**
 * @author Mio Mattila
 * @author Paula Heino
 */

/**
 * Serveriolion remote-rajapinta
 */
public interface KPSServerIF extends Remote {
	void rekisteroiClient (KPSClientIF KPSClient) throws RemoteException;
	ArrayList<KPSClientIF> annaClientit() throws RemoteException;
	void kasitteleKomento(String s, KPSClientIF i) throws RemoteException;
}
