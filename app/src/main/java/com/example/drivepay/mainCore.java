package com.example.drivepay;

import com.example.drivepay.Connection.Client;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import SerializedObjects.UserObjects.Utente;
import SerializedObjects.coreObjects.Noleggio;
import SerializedObjects.coreObjects.Prenotazione;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

class mainCore {
    private static final mainCore ourInstance = new mainCore();

    static mainCore getInstance() {
        return ourInstance;
    }

    // LOCAL private static final Client client = new Client("10.0.2.2", 9696);
    private static final Client client = new Client("212.24.111.96", 9696); //REMOTE
    private static Utente utente;
    private static Noleggio noleggioCorrente;
    private static Prenotazione prenotazioneCorrente;

    private mainCore() {
    }

    public static Client getClient() {
        return client;
    }

    public static Utente getUtente() {
        return utente;
    }

    public static void setUtente(Utente utente) {
        mainCore.utente = utente;
    }

    public static Noleggio getNoleggioCorrente() {
        return noleggioCorrente;
    }

    public static void setNoleggioCorrente(Noleggio noleggioCorrente) {
        mainCore.noleggioCorrente = noleggioCorrente;
    }

    public static Prenotazione getPrenotazioneCorrente() {
        return prenotazioneCorrente;
    }

    public static void setPrenotazioneCorrente(Prenotazione prenotazioneCorrente) {
        mainCore.prenotazioneCorrente = prenotazioneCorrente;
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.ITALIAN);
        cal.setTime(date);
        return cal;
    }
}
