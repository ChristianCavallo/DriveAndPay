package com.example.drivepay;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.drivepay.Connection.OnCommandReceivedListener;
import com.jeevandeshmukh.glidetoastlib.GlideToast;

import net.crosp.libs.android.circletimeview.CircleTimeView;

import java.util.Date;

import SerializedObjects.Command;
import SerializedObjects.CommandsEnum;
import SerializedObjects.coreObjects.Noleggio;
import SerializedObjects.coreObjects.Sconto;
import SerializedObjects.coreObjects.Veicolo;

public class NoleggioActivity extends AppCompatActivity implements OnCommandReceivedListener {

    LinearLayout configurazioneLayout;
    LinearLayout noleggioLayout;
    EditText veicoloText;
    EditText scontoText;
    Button scontoButton;
    Button veicoloButton;
    Button avviaNoleggioButton;
    Button terminaNoleggioButton;
    CircleTimeView timerView;
    Thread timerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noleggio);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        configurazioneLayout = findViewById(R.id.configurazioneLayout);
        noleggioLayout = findViewById(R.id.noleggioLayout);
        veicoloText = findViewById(R.id.veicoloText);
        scontoText = findViewById(R.id.scontoText);
        scontoButton = findViewById(R.id.scontoButton);
        veicoloButton = findViewById(R.id.veicoloButton);
        avviaNoleggioButton = findViewById(R.id.avviaNoleggioButton);
        terminaNoleggioButton = findViewById(R.id.terminaNoleggioButton);
        timerView = findViewById(R.id.timerView);

        configurazioneLayout.setVisibility(View.GONE);
        noleggioLayout.setVisibility(View.GONE);
        mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.EXIST_NOLEGGIO_REQUEST, mainCore.getUtente()), this);
    }

    @Override
    public void onBackPressed() {
        if (!configurazioneLayout.isEnabled()) {
            configurazioneLayout.setEnabled(true);
            return;
        }
        if (noleggioLayout.getVisibility() == View.VISIBLE) {
            new GlideToast.makeToast(this, "Devi terminare il noleggio prima!", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();
            return;
        }
        super.onBackPressed();
    }

    public void confermaVeicolo(View view) {
        String num_v = veicoloText.getText().toString().trim();
        veicoloText.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(num_v)) {
            veicoloText.setError(getString(R.string.error_field_required));
            focusView = veicoloText;
            cancel = true;
        } else if (num_v.length() != 4) {
            veicoloText.setError("Inserisci i 4 numeri riportati sul veicolo");
            focusView = veicoloText;
            cancel = true;
        } else {
            try {
                Integer.parseInt(num_v);
            } catch (Exception ex) {
                veicoloText.setError("Devono esserci solo numeri");
                focusView = veicoloText;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.VEICOLO_REQUEST, Integer.parseInt(num_v)), this);
            new GlideToast.makeToast(this, "Sto confermando il veicolo...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
            configurazioneLayout.setEnabled(false);
        }
    }

    public void confermaNoleggio(View view) {
        if (mainCore.getNoleggioCorrente().getVeicoloCorrente() == null) {
            new GlideToast.makeToast(this, "Veicolo non confermato", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();
        } else {
            mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.NOLEGGIO_REQUEST, mainCore.getNoleggioCorrente()), this);
            new GlideToast.makeToast(this, "Sto richiedendo l'avvio...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
            configurazioneLayout.setEnabled(false);
        }
    }

    public void confermaSconto(View view) {
        String sconto = scontoText.getText().toString().trim();
        scontoText.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(sconto)) {
            scontoText.setError(getString(R.string.error_field_required));
            focusView = scontoText;
            cancel = true;
        } else if (sconto.length() > 10) {
            scontoText.setError("Codice troppo lungo");
            focusView = scontoText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.SCONTO_REQUEST, sconto), this);
            new GlideToast.makeToast(this, "Sto controllando lo sconto...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
            configurazioneLayout.setEnabled(false);
        }
    }


    public void AvviaNoleggio() {
        timerView.setCurrentTime(0);
        configurazioneLayout.setVisibility(View.GONE);
        noleggioLayout.setVisibility(View.VISIBLE);
        new GlideToast.makeToast(NoleggioActivity.this, "Noleggio avviato!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
        timerThread = new Thread(new Runnable() {
            Long msecs = Long.valueOf(0);

            @Override
            public void run() {
                msecs = timerView.getCurrentTimeInSeconds();
                while (true) {
                    timerView.setCurrentTime(msecs);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    msecs++;
                }
            }
        });
        timerThread.start();
    }

    public void RecoverNoleggio() {
        AvviaNoleggio();
        int secondsBetween = (int) ((new Date().getTime() - mainCore.getNoleggioCorrente().getInizio().getTime()) / 1000);
        timerView.setCurrentTime(secondsBetween);

    }

    @Override
    public void OnCommandReceivedCallback(Command cmd) {
        if (cmd.getType() == CommandsEnum.CommandType.EXIST_NOLEGGIO_RESPONSE) {
            Noleggio n = (Noleggio) cmd.getArg();
            if (n == null) {
                //TODO: Gestire la non autorizzazione al noleggio
                finish();
            }
            mainCore.setNoleggioCorrente(n);
            if (n.getInizio() != null) {
                //Noleggio gia esistente
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(NoleggioActivity.this, "Noleggio ancora in corso!", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
                        RecoverNoleggio();
                    }
                };
                mainHandler.post(myRunnable);
            } else {
                //Nuovo noleggio
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(NoleggioActivity.this, "Autorizzazione al noleggio ottenuta!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
                        configurazioneLayout.setEnabled(true);
                        configurazioneLayout.setVisibility(View.VISIBLE);
                    }
                };
                mainHandler.post(myRunnable);
            }
        } else if (cmd.getType() == CommandsEnum.CommandType.VEICOLO_RESPONSE) {
            Veicolo v = (Veicolo) cmd.getArg();
            if (v != null) {
                mainCore.getNoleggioCorrente().setVeicoloCorrente(v);
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(NoleggioActivity.this, "Veicolo impostato!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
                        configurazioneLayout.setEnabled(true);
                    }
                };
                mainHandler.post(myRunnable);
            } else {
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(NoleggioActivity.this, "Veicolo non trovato!", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();
                        configurazioneLayout.setEnabled(true);
                    }
                };
                mainHandler.post(myRunnable);
            }

        } else if (cmd.getType() == CommandsEnum.CommandType.SCONTO_RESPONSE) {
            Sconto s = (Sconto) cmd.getArg();
            if (s != null) {
                mainCore.getNoleggioCorrente().setScontoCorrente(s);
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(NoleggioActivity.this, "Sconto applicato!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
                        configurazioneLayout.setEnabled(true);
                        scontoText.setEnabled(false);
                        scontoButton.setEnabled(false);
                    }
                };
                mainHandler.post(myRunnable);
            } else {
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(NoleggioActivity.this, "Sconto non trovato!", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();
                        configurazioneLayout.setEnabled(true);

                    }
                };
                mainHandler.post(myRunnable);
            }
        } else if (cmd.getType() == CommandsEnum.CommandType.NOLEGGIO_RESPONSE) {
            String response = (String) cmd.getArg();
            if (response.length() == 2) {
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        AvviaNoleggio();
                    }
                };
                mainHandler.post(myRunnable);
            } else {
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(NoleggioActivity.this, "Errore nell'avvio del noleggio", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();

                    }
                };
                mainHandler.post(myRunnable);
            }
        }
    }
}
