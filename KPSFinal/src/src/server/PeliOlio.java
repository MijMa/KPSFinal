package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import client.KPSClientIF;
/**
 * @author Mio Mattila
 * @author Paula Heino
 */

/**
 * PeliOlio-luokka, Toimii Pelin roolissa,
 *  Tallennetaan listaan serverin puolella jossa jokaisella PeliOliolla on omat pelaajat ja vastaukset 
 *  Thread.Sleep ei pitaisi varmaan kayttaa servupuolella?
 */
public class PeliOlio extends UnicastRemoteObject{
	
	KPSClientIF ykkosPelaaja;
	KPSClientIF kakkosPelaaja;
	private static int P1vastaus = 0;
	private static int P2vastaus = 0;
	private int kierros = 1;
	
	public PeliOlio(KPSClientIF eka, KPSClientIF toka) throws RemoteException {
		this.ykkosPelaaja = eka;
		this.kakkosPelaaja = toka;
	}
	
    /**
     * Getterit ja setterit
     */
	public KPSClientIF annaYkkospelaaja(){
		return this.ykkosPelaaja;
	}
	public KPSClientIF annaKakkospelaaja(){
		return this.kakkosPelaaja;
	}
	public void asetaYkkospelaaja(KPSClientIF pelaaja){
		this.ykkosPelaaja = pelaaja;
	}
	public void asetaKakkospelaaja(KPSClientIF pelaaja){
		this.kakkosPelaaja = pelaaja;
	}
	public int annaYkkosVastaus(){
		return this.P1vastaus;
	}
	public int annaKakkosVastaus(){
		return this.P2vastaus;
	}
	
	public int annaKierros(){
		return kierros;
	}
	
	public void asetaKierros(int i){
		this.kierros = i;
	}
	
	//tarkistavat kumpi pelaaja annettu client on
	public boolean onP1(KPSClientIF p){
		if (p.equals(ykkosPelaaja)) return true;
		else return false;
	}
	public boolean onP2(KPSClientIF p){
		if (p.equals(kakkosPelaaja)) return true;
		else return false;
	}

    /**
     * Metodi joka tallentaa clientin antaman vastauksen
     */
	public void AnnaVastaus(int numeroP, int i) throws RemoteException {
		int vastausnumero = numeroP;

		if (i == 0){
			P1vastaus = vastausnumero;
			System.out.println("P1 tallennettu "  + vastausnumero);
		}else if (i == 1){
			P2vastaus = vastausnumero;
			System.out.println("P2 tallennettu "  + vastausnumero);
		} else{
			ykkosPelaaja.otaViesti("Virhe vastauksen kasittelyssa");
			kakkosPelaaja.otaViesti("Virhe vastauksen kasittelyssa");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//tyhjentaa molempien pelaajien vastaukset
	public void nollaavastaukset(){
		P1vastaus = 0;
		P2vastaus = 0;
	}
	
    /**
     * kun pelaaja tai serveri kutsuu tata komentoa PelaajaArraylistin PelaajaOliolle
     *  se tulostaa PelaajaOlion(pelin) voittajan
     *  Eli vertailee pelaajien vastauksia.
     *  Jos vain yksi pelaaja on antanut vastauksen, keskeytetaan suoritus
     */
	public void TarkistaVastaus() throws RemoteException{
		
		int ykkonen = P1vastaus;
		int kakkonen = P2vastaus;
		String voitto = "\n******************\n   Voitit pelin!\n******************\n";
        String havio = "\n******************\n   Havisit pelin.\n******************\n";
        String tasapeli = "\n******************\n   Tasapeli\n******************\n";
        
        //jos jompikumpi pelaajista ei ole viela vastannut, lopetetaan metodin suoritus
		if (P1vastaus != 0 && P2vastaus == 0){
			this.ykkosPelaaja.otaViesti("Odotetetaan kakkospelaajan vastausta");
			this.kakkosPelaaja.otaViesti("Ykkospelaaja antoi vastauksen");
			return;
		}
		if (P1vastaus == 0 && P2vastaus != 0){
			this.kakkosPelaaja.otaViesti("Odotetetaan ykkospelaajan vastausta");
			this.ykkosPelaaja.otaViesti("Kakkospelaaja antoi vastauksen");
			return;
		}
		
        //pieni odotus (3 pistetta) ennen tuloksen julkistamista
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        for (int i = 0; i<3; i++){
	        ykkosPelaaja.otaViesti(".");
	        kakkosPelaaja.otaViesti(".");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        //jos pelaajien vastaus on eri, tarkistetaan kumpi voitti
		if(P1vastaus != P2vastaus){
			String switcherino =P1vastaus+""+P2vastaus;
			String viesti = "";
			boolean ykkonenVoitti = false;
			
	        switch (switcherino) {
	            case "13":  
	             	viesti = "Ykkospelaaja voitti!";
	             	ykkonenVoitti = true;
	                break;
	                
	            case "12":  
	            	viesti = "Kakkospelaaja voitti!";
	            	break;
	            	
	            case "21":  
	            	viesti = "Ykkospelaaja voitti!";
	            	ykkonenVoitti = true;
                    break;
                    
	            case "23":  
	            	viesti = "Kakkospelaaja voitti!";
	            	break;
	            	
	            case "31":
	            	viesti = "Kakkospelaaja voitti!";
	            	break;
	            	
	            case "32":  
	            	viesti = "Ykkospelaaja voitti!";
	            	ykkonenVoitti = true;
	            	break;
	            	
	            default: 
	            	viesti = "Virhe tuloksen tarkistamisessa";
	                break;
	        }
	        // Tulostaa tuloksen pelin pelaajille ja serverille
	        System.out.println(viesti);
	        if (ykkonenVoitti){
	        	ykkosPelaaja.asetaScore(ykkosPelaaja.annaScore() + 1);
	        	ykkosPelaaja.otaViesti(voitto);
	        	kakkosPelaaja.otaViesti(havio);
	        } else{
	        	kakkosPelaaja.asetaScore(kakkosPelaaja.annaScore() + 1);
	        	ykkosPelaaja.otaViesti(havio);
	        	kakkosPelaaja.otaViesti(voitto);
	        }
	    //Jos pelaajien vastaus on sama, tulos on tasapeli
		} else{
			System.out.println("Tasapeli");
			ykkosPelaaja.otaViesti(tasapeli);
			kakkosPelaaja.otaViesti(tasapeli);

		}
        // Tulostaa viela lopuksi vastaukset pelista joka juuri loppui
		//Seka pelaajien pisteet.
        try {
        	String vastaukset = "Vastaukset: \n" + ykkosPelaaja.annaNimi() +": "+ kaannaVastaus(ykkonen) +" |  "
    				+ kakkosPelaaja.annaNimi() +": "+ kaannaVastaus(kakkonen);
            String pisteet = "Pisteet: \n"+ ykkosPelaaja.annaNimi() +": "+ ykkosPelaaja.annaScore() +" |  "
    				+ kakkosPelaaja.annaNimi() +": "+ kakkosPelaaja.annaScore();
	        System.out.println(vastaukset);
	        ykkosPelaaja.otaViesti(vastaukset);
	        kakkosPelaaja.otaViesti(vastaukset);	        	
	        ykkosPelaaja.otaViesti(pisteet);
	        kakkosPelaaja.otaViesti(pisteet);
	        
		} catch (RemoteException e) {
			e.printStackTrace();
		}
        kierros++;

	}
	
	//Muutetaan vastausnumero sita vastaavaksi sanaksi
	public String kaannaVastaus(int v){
		String tulos = "";
		switch(v){
			case(1): 
				tulos = "kivi";
				break;
			case(2): 
				tulos = "paperi";
				break;
			case(3): 
				tulos = "sakset";
				break;
		}
		return tulos;
		
	}

}
