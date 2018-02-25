package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import client.KPSClientIF;

/**
 * @author Mio Mattila
 * @author Paula Heino
 * Serverin toteutusluokka, pitaa ylla listaa clienteista ja pelaajista
 * Printtausmetodit antavat tietoa serverille, ne eivat nay pelaajille
 */
public class KPSServer extends UnicastRemoteObject implements KPSServerIF{
	// Pelit ja Clientit
	public static int pelaajatJaVastauksetCounter;
	public static ArrayList<PeliOlio> pelioliot;
	private ArrayList<KPSClientIF> clientit;
	private volatile boolean ei2pelaajaa = true;

	/* Konstruktori */
	protected KPSServer() throws RemoteException {
		clientit = new ArrayList<KPSClientIF>();
		pelioliot = new ArrayList<PeliOlio>();	
	}
	
    /**
     * get-metodi clienteille
     */
	public ArrayList<KPSClientIF> annaClientit (){
		return clientit;
	}

    /**
     * Clienttien lisaysmetodi listaan
     */
	public synchronized void rekisteroiClient(KPSClientIF KPSClient) throws RemoteException {
		this.clientit.add(KPSClient);		
	}
	

    /**
     * Scannerista saadun tekstin kasittely; Suorittaa komentoja sen mukaan mita tekstia
     * client antaa scannerille. 
     * Syotteina kasiteltava komento seka sen lahettava client
     * Vain lowercase toimii komennoissa
     */
	public void kasitteleKomento(String s, KPSClientIF client) throws RemoteException{
		String viesti = "";
		int i = 0;
		int vastaus = 0;
		
    	
        switch (s) {
        	//kivi-paperi-sakset -komennot
    		case ("k"):
        	case ("p"):
        	case ("s"):
        		//muuttaa komennot sanoiksi
        		switch (s){
        		case "k":
        			vastaus = 1;
        			client.otaViesti("Vastauksesi: kivi");
        			break;
        		case "p":
        			vastaus = 2;
        			client.otaViesti("Vastauksesi: paperi");
        			break;
        		case "s":
        			vastaus = 3;
        			client.otaViesti("Vastauksesi: sakset");
        			break;
        		}		
        	
				//haetaan nykyisen clientin peli
				int pelinro = haePelinro(client);
				PeliOlio tamapeli = pelioliot.get(pelinro);
				
				//tarkistetaan onko pelissa kaksi pelaajaa
    			//jos ei, niin komentoa ei suoriteta
    			if (tamapeli.ykkosPelaaja == null || tamapeli.kakkosPelaaja == null){
    				client.otaViesti("Toinen pelaaja puuttuu, vastausta ei tallennettu");
    				return;
    			}
    			//tarkistetaan onko syotettu client jompikumpi nykyisen pelin pelaajista
    			//jos on, niin tallennetaan vastaus
				if (tamapeli.ykkosPelaaja.equals(client)|| tamapeli.kakkosPelaaja.equals(client)){
					if(tamapeli.onP1(client)){
						tamapeli.AnnaVastaus(vastaus, 0);
					}
					else if(tamapeli.onP2(client)){
						tamapeli.AnnaVastaus(vastaus, 1);
					}
					else{
						System.out.println("Vastauksen tallennus epaonnistui");
					}
					//vertaillaan vastauksia tai odotetaan toisen pelaajan vastausta
					tamapeli.TarkistaVastaus();
					
					//nollataan vastaukset uutta kierrosta varten
					if (tamapeli.annaYkkosVastaus() != 0 && tamapeli.annaKakkosVastaus() != 0){
						tamapeli.nollaavastaukset();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						tamapeli.ykkosPelaaja.otaViesti("\n\n----------Kierros " + tamapeli.annaKierros() + "----------");
						tamapeli.kakkosPelaaja.otaViesti("\n\n----------Kierros " + tamapeli.annaKierros() + "----------");
						String pelinaloitus = "\n(k)ivi,(p)aperi vai (s)akset?";
		        		tamapeli.annaYkkospelaaja().otaViesti(pelinaloitus);
		        		tamapeli.annaKakkospelaaja().otaViesti(pelinaloitus);
						return;	
					}	
        		}
        		break;

    	    /**
    	     * Metodi jota tarvitaan clienttien liittamiseksi PelaajaOlioihin.
    	     *  Peliin liittymismetodien suoritus ja pelin luonti mikali vapaata pelia ei ole olemassa
    	     */	
            case "liity":
            	//jos client on pelissa, ei tehda mitaan
            	if (client.annaOlotila()){
            		client.otaViesti("Olet jo pelissa, et voi luoda uutta");
            		return;
            	}
            	
            	//jos pelilista on tyhja, luodaan tyhja peli
        		if (KPSServer.pelioliot.isEmpty()){
        			KPSServer.pelioliot.add(new PeliOlio(null,null));
        			System.out.println("Uusi peli aloitettu");
        		}
        		
        		for (PeliOlio p : KPSServer.pelioliot){
        			
        			//jos pelissa on jo kakkospelaaja (eli joku on keskeyttanyt pelin) liitytaan taman peliin
        			if (p.annaYkkospelaaja() == null && p.annaKakkospelaaja() != null){
        				client.asetaOlotila(true);
        				p.asetaYkkospelaaja(client);
        				client.otaViesti("\nLiityit pelaajan " + p.annaKakkospelaaja().annaNimi() + " peliin.");

						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						client.otaViesti("\n\n----------Kierros " + p.annaKierros() + "----------");
						p.annaKakkospelaaja().otaViesti("\nPelaaja " + p.annaKakkospelaaja().annaNimi() + " liittyi. ");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					   
						p.annaKakkospelaaja().otaViesti("\n\n----------Kierros " + p.annaKierros() + "----------");
						
						String pelinaloitus = "\n(k)ivi,(p)aperi vai (s)akset?";
						int pelinro2 = haePelinro(client);
						
		        		pelioliot.get(pelinro2).annaYkkospelaaja().otaViesti(pelinaloitus);
		        		pelioliot.get(pelinro2).annaKakkospelaaja().otaViesti(pelinaloitus);
		        		return;
        			}
        			
        			//jos peli on tyhja asetetaan pelaaja ykkospelaajaksi
        			if (p.annaYkkospelaaja() == null){
        				p.asetaYkkospelaaja(client);
        				client.asetaOlotila(true);
        				client.otaViesti("Olet ilmoittautunut ykkospelaajaksi, odotetaan kakkospelaajaa...");
        				System.out.println("Pelaaja 1 luotu");
        				//loopataan kunnes toinen pelaaja liittyy
        				while (ei2pelaajaa){
        					if (p.annaKakkospelaaja() != null){
                				try {
        							Thread.sleep(2000);
        						} catch (InterruptedException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        						client.otaViesti("Pelaaja " + p.annaKakkospelaaja().annaNimi() + " liittyi. "
        								+ "\n\n----------Kierros " + p.annaKierros() + "----------");
        						pelinro = haePelinro(client);
        		        		tamapeli = pelioliot.get(pelinro);
        						try {
        							Thread.sleep(1000);
        						} catch (InterruptedException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        		        		String pelinaloitus = "\n(k)ivi,(p)aperi vai (s)akset?";
        		        		tamapeli.annaYkkospelaaja().otaViesti(pelinaloitus);
        		        		tamapeli.annaKakkospelaaja().otaViesti(pelinaloitus);
        						return;
        					}
        				}
        				return;
        			}	
        			//jos pelissa on ykkospelaaja ja kakkospelaaja on tyhja, asetetaan client siihen
        			else if(p.annaKakkospelaaja() == null){
        				p.asetaKakkospelaaja(client);
        				client.asetaOlotila(true);
        				//keskeyttaa ykkospelaajan odotusloopin
        				ei2pelaajaa = false;
        				client.otaViesti("Olet pelissa pelaajan " + p.annaYkkospelaaja().annaNimi() +" kanssa. ");
           				try {
    							Thread.sleep(2000);
    						} catch (InterruptedException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
        				client.otaViesti("\n\n----------Kierros " + p.annaKierros() + "----------");
        				System.out.println("Pelaaja 2 luotu");
        				return;
        			}		
        		}
        		
        		//jos mikaan ylemmista ei toteudu, pelit ovat taynna.
        		//luodaan uusi peli ja asetetaan client ykkospelaajaksi
        		PeliOlio uusipeli = new PeliOlio(client,null);
    			KPSServer.pelioliot.add(uusipeli);	
    			client.otaViesti("Peli taynna, uusi peli aloitettu. Odotetaan kakkospelaajaa...");
    			System.out.println("Pelaaja 1 luotu");
    			//1. pelaajan pitaa taas odottaa toista pelaajaa
    			ei2pelaajaa = true;
				while (ei2pelaajaa){
					if (uusipeli.annaKakkospelaaja() != null){
						client.otaViesti("Pelaaja " + uusipeli.annaKakkospelaaja().annaNimi() + " liittyi.");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						client.otaViesti("\n\n----------Kierros " + uusipeli.annaKierros() + "----------");
						int pelinro2 = haePelinro(client);
						PeliOlio tamapeli2 = pelioliot.get(pelinro2);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        		String pelinaloitus = "\n(k)ivi,(p)aperi vai (s)akset?";
		        		tamapeli2.annaYkkospelaaja().otaViesti(pelinaloitus);
		        		tamapeli2.annaKakkospelaaja().otaViesti(pelinaloitus);
						return;
					}
				}
            	break;
            
        	/*nullaa clientin roolin, jos se on pelissa
        	 *samalla nollaa omat seka vastustajan pisteet seka pelikierrokset
        	 *nollaa myos tallennetut vastaukset */
            case "poistu":
            	if (!client.annaOlotila()){
            		client.otaViesti("Et ole pelissa");
            		return;
            	} else{
            		int pelinro2 = 0;
	        		for (int pelit = 0; pelit<pelioliot.size(); pelit++){
	        			if (pelioliot.get(pelit).annaYkkospelaaja().equals(client) || pelioliot.get(pelit).annaKakkospelaaja().equals(client)){
	        				pelinro2 = pelit;
	        				System.out.println("pelinro test: " + pelinro2);
	        			}else System.out.println("pelinumeron haku ei onnistunut");
	        		}
	        		PeliOlio tamapeli2 = pelioliot.get(pelinro2);
	        		tamapeli2.nollaavastaukset();
	        		client.asetaScore(0);
	        		tamapeli2.asetaKierros(1);
	        		//ilmoitetaan myos vastustajalle pelaajan poistuminen
	        		if (tamapeli2.onP1(client)){
	        			tamapeli2.annaKakkospelaaja().otaViesti("Pelaaja " + tamapeli2.annaYkkospelaaja().annaNimi() 
	        					+ " poistui pelista");
	        			tamapeli2.asetaYkkospelaaja(null);
	        			tamapeli2.kakkosPelaaja.asetaScore(0);
	        			
	        		}
	        		if (tamapeli2.onP2(client)){
	        			tamapeli2.annaYkkospelaaja().otaViesti("Pelaaja " + tamapeli2.annaKakkospelaaja().annaNimi() 
	        					+ " poistui pelista");
	        			tamapeli2.asetaKakkospelaaja(null);
	        			tamapeli2.annaYkkospelaaja().asetaScore(0);
	        			
	        		}
	        		client.asetaOlotila(false);
	        		client.otaViesti("Poistuit pelista");
            	}
            	break;
            	
            	
            	
            //listaa kaikki komennot	
        	case "help":
        		viesti = "\nKomennot:\n"
        				+ "\nliity - liity valmiiseen peliin tai luo uusi"
        				+ "\npoistu - poistu pelista. Voit liittya heti uuteen peliin"
        				+ "\npelaajat - listaa kaikki pelaajat (myos ne, jotka eivat ole pelissa)"
        				+ "\npelit - listaa kaikki kaynnissa olevat pelit ja niiden pelaajat"
        				+ "\npisteet - antaa pisteesi\n";
        		client.otaViesti(viesti);
        		break;
        		
        		
        	//listaa kaikki clienttien nimet
            case "pelaajat":  
            	viesti = "Pelaajat: ";;
            	for (KPSClientIF c : clientit){
					try {
						viesti = viesti + (c.annaNimi() + " ") ;
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
        		client.otaViesti(viesti);
                break;
                
            /**
             * Tulostaa ne pelaajat jotka ovat ilmoittautuneet peliin
             * Seka kyseisen pelin
             */
            case "pelit":
            	int laskuri = 0;
            	for(int u = 0; u<pelioliot.size(); u++){
            		laskuri++;
            		viesti += ("Peli " + (u+1) + ": ");
            		if(KPSServer.pelioliot.get(u).annaYkkospelaaja() != null){
            			viesti += (KPSServer.pelioliot.get(u).annaYkkospelaaja().annaNimi() + " ");	
            		} else{
            			viesti += "[tyhja]";
            		}
            		if(KPSServer.pelioliot.get(u).annaKakkospelaaja() != null){
            			viesti += (KPSServer.pelioliot.get(u).annaKakkospelaaja().annaNimi() + " ");
            		} else{
            			viesti += "[tyhja]";
            		}
            		
            		viesti += "\n";
            	}
            	client.otaViesti("Peleja kaynnissa " + laskuri);
            	client.otaViesti(viesti);

            	break;
            	
            //palauttaa clientin pisteet
            case "pisteet":
            	viesti = "Pisteesi: " + client.annaScore(); 
            	client.otaViesti(viesti);
            	break;

        }
        
        	
        
	}
	
	//hakee annetun clientin kohdan pelioliolistassa
	//kaytetaan kasitteleKomennossa
	public int haePelinro(KPSClientIF client){
		int pelinro = 0;
		for (int pelit = 0; pelit<pelioliot.size(); pelit++){
			if (pelioliot.get(pelit).annaYkkospelaaja().equals(client) || pelioliot.get(pelit).annaKakkospelaaja().equals(client)){
				pelinro = pelit;
			}else System.out.println("pelinumeron haku ei onnistunut");
		}
		return pelinro;
	}


}
