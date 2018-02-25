package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * @author Mio Mattila
 * @author Paula Heino
 */

/**
 * Rajapinta clientille
 */
public interface KPSClientIF extends Remote{
	void otaViesti(String message) throws RemoteException;
	String annaNimi() throws RemoteException;
	int annaScore() throws RemoteException;
	void asetaScore(int i) throws RemoteException;
	void asetaOlotila(boolean b) throws RemoteException;
	boolean annaOlotila() throws RemoteException;
}
