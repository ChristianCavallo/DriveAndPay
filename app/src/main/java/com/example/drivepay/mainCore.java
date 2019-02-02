package com.example.drivepay;

import com.example.drivepay.Connection.TestClient;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import SerializedObjects.UserObjects.Utente;
import SerializedObjects.coreObjects.Noleggio;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

class mainCore {
    private static final mainCore ourInstance = new mainCore();

    static mainCore getInstance() {
        return ourInstance;
    }

    private static final TestClient client = new TestClient("10.0.2.2", 9696);
    private static Utente utente;
    private static Noleggio noleggioCorrente;

    private mainCore() {
    }

    public static TestClient getClient() {
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
