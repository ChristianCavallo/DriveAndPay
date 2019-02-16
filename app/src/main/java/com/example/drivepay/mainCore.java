package com.example.drivepay;

import com.example.drivepay.Connection.Client;

import SerializedObjects.UserObjects.Utente;
import SerializedObjects.coreObjects.Noleggio;
import SerializedObjects.coreObjects.Prenotazione;

class mainCore {
    private static final mainCore ourInstance = new mainCore();

    static mainCore getInstance() {
        return ourInstance;
    }

    // LOCAL private static final Client client = new Client("10.0.2.2", 9696);
    private final Client client;
    private Utente utente;
    private Noleggio noleggioCorrente;
    private Prenotazione prenotazioneCorrente;

    private mainCore() {
        client = new Client("212.24.111.96", 9696); //REMOTE
    }

    public Client getClient() {
        return client;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public Noleggio getNoleggioCorrente() {
        return noleggioCorrente;
    }

    public void setNoleggioCorrente(Noleggio noleggioCorrente) {
        this.noleggioCorrente = noleggioCorrente;
    }

    public Prenotazione getPrenotazioneCorrente() {
        return prenotazioneCorrente;
    }

    public void setPrenotazioneCorrente(Prenotazione prenotazioneCorrente) {
        this.prenotazioneCorrente = prenotazioneCorrente;
    }

}
