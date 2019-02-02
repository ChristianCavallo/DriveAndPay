package com.example.drivepay;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;

import SerializedObjects.Command;
import SerializedObjects.CommandsEnum;
import SerializedObjects.coreObjects.Veicolo;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, OnCommandReceivedListener {

    MapView mapView;
    GoogleMap googleMap;
    TextView carburanteText;
    SeekBar carburanteBar;
    LinearLayout carburanteLayout;

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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Button noleggioButton = findViewById(R.id.noleggioButton);
        noleggioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NoleggioActivity.class));
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.GET_LISTA_VEICOLI_REQUEST, "per favore"), this);

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

    @Override
    public void OnCommandReceivedCallback(Command cmd) {
        if (cmd.getType() == CommandsEnum.CommandType.GET_LISTA_VEICOLI_RESPONSE) {

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
        }
    }



}
