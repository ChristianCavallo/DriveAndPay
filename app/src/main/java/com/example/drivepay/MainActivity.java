package com.example.drivepay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.drivepay.Connection.OnCommandReceivedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeevandeshmukh.glidetoastlib.GlideToast;

import net.crosp.libs.android.circletimeview.CircleTimeView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import SerializedObjects.Command;
import SerializedObjects.CommandsEnum;
import SerializedObjects.coreObjects.Fattura;
import SerializedObjects.coreObjects.Prenotazione;
import SerializedObjects.coreObjects.Veicolo;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, OnCommandReceivedListener {

    MapView mapView;
    GoogleMap googleMap;
    TextView carburanteText;
    SeekBar carburanteBar;
    LinearLayout carburanteLayout;

    LinearLayout mainLayout;
    LinearLayout prenotazioneLayout;
    LinearLayout postprenotazioneLayout;
    LinearLayout fatturaPrenotazioneLayout;

    CircleTimeView timerPrenotazioneView;
    Thread timerThread;

    TextView prenotazioneIdText;
    TextView veicoloIdText;
    TextView inizioText;
    TextView fineText;
    TextView tariffaText;
    TextView durataText;
    TextView costoText;
    Veicolo veicoloCorrente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        carburanteLayout = findViewById(R.id.carburanteLayout);
        carburanteText = findViewById(R.id.carburanteText);
        carburanteBar = findViewById(R.id.carburanteBar);
        carburanteBar.setEnabled(false);

        mainLayout = findViewById(R.id.mainLayout);
        prenotazioneLayout = findViewById(R.id.prenotazioneLayout);
        postprenotazioneLayout = findViewById(R.id.postPrenotazioneLayout);
        fatturaPrenotazioneLayout = findViewById(R.id.fatturaPrenotazioneLayout);

        mainLayout.setVisibility(View.VISIBLE);
        prenotazioneLayout.setVisibility(View.GONE);
        postprenotazioneLayout.setVisibility(View.GONE);
        fatturaPrenotazioneLayout.setVisibility(View.GONE);

        prenotazioneIdText = findViewById(R.id.prenotazioneIdText);
        veicoloIdText = findViewById(R.id.veicoloIdText);
        inizioText = findViewById(R.id.inizioText);
        fineText = findViewById(R.id.fineText);
        tariffaText = findViewById(R.id.tariffaText);
        durataText = findViewById(R.id.durataText);
        costoText = findViewById(R.id.costoText);

        timerPrenotazioneView = findViewById(R.id.prenotazioneTimerView);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Button noleggioButton = findViewById(R.id.noleggioButton);
        noleggioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainCore.getInstance().setNoleggioCorrente(null);
                startActivity(new Intent(MainActivity.this, NoleggioActivity.class));
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.EXIST_PRENOTAZIONE_REQUEST, mainCore.getInstance().getUtente()), this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.GET_LISTA_VEICOLI_REQUEST, "per favore"), this);

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Veicolo v = (Veicolo) marker.getTag();
                MostraVeicolo(v);
                return false;
            }
        });

    }

    public void MostraVeicolo(Veicolo v) {
        int carburante = v.getCarburante();
        carburanteText.setText("LIVELLO CARBURANTE: " + carburante + "%");
        carburanteBar.setProgress(carburante);
        carburanteLayout.setVisibility(View.VISIBLE);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(v.getLatitude(), v.getLongitude())));
        veicoloCorrente = v;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (carburanteLayout.getVisibility() == View.VISIBLE) {
            carburanteLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action

        } else if (id == R.id.nav_share) {


        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void RecoverPrenotazione() {
        avviaPrenotazione();
        Long diff = 20 * 60 - TimeUnit.SECONDS.toSeconds(new Date().getTime() - mainCore.getInstance().getPrenotazioneCorrente().getInizio().getTime());
        if (diff > 0) {
            timerPrenotazioneView.setCurrentTime(diff);
        } else {
            timerPrenotazioneView.setCurrentTime(0);
        }

    }

    public void Home(View view) {
        mainLayout.setVisibility(View.VISIBLE);
        prenotazioneLayout.setVisibility(View.GONE);
        postprenotazioneLayout.setVisibility(View.GONE);
        fatturaPrenotazioneLayout.setVisibility(View.GONE);
    }

    public void RichiediPrenotazione(View view) {
        if (veicoloCorrente == null) {

            new GlideToast.makeToast(MainActivity.this, "Seleziona un veicolo dalla mappa prima", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();
            return;
        }
        Prenotazione p = new Prenotazione((int) (Math.random() * 999999999));
        p.setUtente(mainCore.getInstance().getUtente());
        p.setVeicolo(veicoloCorrente);
        mainCore.getInstance().setPrenotazioneCorrente(p);
        mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.PRENOTAZIONE_REQUEST, mainCore.getInstance().getPrenotazioneCorrente()), this);
        new GlideToast.makeToast(MainActivity.this, "Richiedo la prenotazione...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();

    }

    private void avviaPrenotazione() {
        timerPrenotazioneView.setCurrentTime(20 * 60);
        mainLayout.setVisibility(View.GONE);
        prenotazioneLayout.setVisibility(View.VISIBLE);
        postprenotazioneLayout.setVisibility(View.GONE);
        fatturaPrenotazioneLayout.setVisibility(View.GONE);

        timerPrenotazioneView.startTimer();
    }

    public void TerminaPrenotazione(View view) {

        timerPrenotazioneView.stopTimer();
        new GlideToast.makeToast(this, "Richiedo la terminazione della prenotazione...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
        mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.TERMINA_PRENOTAZIONE_REQUEST, mainCore.getInstance().getUtente()), this);

    }

    private void fermaPrenotazione() {

        mainLayout.setVisibility(View.GONE);
        prenotazioneLayout.setVisibility(View.GONE);
        postprenotazioneLayout.setVisibility(View.VISIBLE);
        fatturaPrenotazioneLayout.setVisibility(View.GONE);

    }

    public void MostraFatturaPrenotazione(View view) {
        mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.GET_FATTURA_PRENOTAZIONE_REQUEST, mainCore.getInstance().getPrenotazioneCorrente()), this);
        new GlideToast.makeToast(MainActivity.this, "Richiedo la fattura...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();

    }

    private void LoadFattura() {
        try {
            mainLayout.setVisibility(View.GONE);
            prenotazioneLayout.setVisibility(View.GONE);
            postprenotazioneLayout.setVisibility(View.GONE);
            fatturaPrenotazioneLayout.setVisibility(View.VISIBLE);

            Prenotazione n = mainCore.getInstance().getPrenotazioneCorrente();
            prenotazioneIdText.setText(Integer.toString(n.getId()));
            veicoloIdText.setText(Integer.toString(n.getVeicolo().getCodice()));

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            inizioText.setText(format.format(n.getInizio()));

            Date fine = new Date();
            fine.setTime((long) (n.getInizio().getTime() + n.getFattura().getDurata() * 60 * 1000));
            fineText.setText(format.format(fine));

            tariffaText.setText(Double.toString(n.getFattura().getTariffa()) + "€ al min");

            durataText.setText(Double.toString(n.getFattura().getDurata()) + " min");
            costoText.setText(Double.toString(n.getFattura().getTotale()) + "€");

            new GlideToast.makeToast(MainActivity.this, "Fattura caricata!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    @Override
    public void OnCommandReceivedCallback(Command cmd) {
        switch (cmd.getType()) {
            case GET_LISTA_VEICOLI_RESPONSE:
                ArrayList<Veicolo> veicoli = (ArrayList<Veicolo>) cmd.getArg();

                MarkerOptions options = new MarkerOptions();
                LatLng pos = new LatLng(0, 0);
                double totalLat = 0;
                double totalLon = 0;
                for (int x = 0; x < veicoli.size(); x++) {
                    Veicolo v = veicoli.get(x);
                    pos = new LatLng(v.getLatitude(), v.getLongitude());
                    totalLat += v.getLatitude();
                    totalLon += v.getLongitude();
                    options.position(pos);
                    options.title("Veicolo");
                    Marker m = this.googleMap.addMarker(options);
                    m.setTag(v);
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(totalLat / veicoli.size(), totalLon / veicoli.size()), 15.0f));
                break;
            case EXIST_PRENOTAZIONE_RESPONSE:
                if (cmd.getArg() != null) {
                    Prenotazione p = (Prenotazione) cmd.getArg();
                    mainCore.getInstance().setPrenotazioneCorrente(p);
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new GlideToast.makeToast(MainActivity.this, "Recupero prenotazione effettuato!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
                            RecoverPrenotazione();
                        }
                    };
                    mainHandler.post(myRunnable);
                }
                break;
            case PRENOTAZIONE_RESPONSE:
                final String resp = (String) cmd.getArg();

                if (resp.toUpperCase().trim().equals("OK")) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new GlideToast.makeToast(MainActivity.this, "Prenotazione avviata correttamente!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
                            mainCore.getInstance().getPrenotazioneCorrente().setInizio(new Date());
                            avviaPrenotazione();
                        }
                    };
                    mainHandler.post(myRunnable);
                } else {

                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new GlideToast.makeToast(MainActivity.this, resp, GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();

                        }
                    };
                    mainHandler.post(myRunnable);

                }
                break;
            case TERMINA_PRENOTAZIONE_RESPONSE:
                final String res = (String) cmd.getArg();

                if (res.toUpperCase().trim().equals("OK")) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new GlideToast.makeToast(MainActivity.this, "Prenotazione Terminata!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
                            fermaPrenotazione();
                        }
                    };
                    mainHandler.post(myRunnable);
                } else {

                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new GlideToast.makeToast(MainActivity.this, res, GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();

                        }
                    };
                    mainHandler.post(myRunnable);

                }
                break;
            case GET_FATTURA_PRENOTAZIONE_RESPONSE:
                Fattura f = (Fattura) cmd.getArg();
                if (f != null) {
                    mainCore.getInstance().getPrenotazioneCorrente().setFattura(f);
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            LoadFattura();
                        }
                    };
                    mainHandler.post(myRunnable);

                } else {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new GlideToast.makeToast(MainActivity.this, "Fattura non trovata per la prenotazione corrente!", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();

                        }
                    };
                    mainHandler.post(myRunnable);
                }
                break;
        }




    }



}
