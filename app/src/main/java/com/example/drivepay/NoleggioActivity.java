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
import android.widget.TextView;

import com.example.drivepay.Connection.OnCommandReceivedListener;
import com.jeevandeshmukh.glidetoastlib.GlideToast;

import net.crosp.libs.android.circletimeview.CircleTimeView;

import java.text.SimpleDateFormat;
import java.util.Date;

import SerializedObjects.Command;
import SerializedObjects.CommandsEnum;
import SerializedObjects.coreObjects.Fattura;
import SerializedObjects.coreObjects.Noleggio;
import SerializedObjects.coreObjects.Sconto;
import SerializedObjects.coreObjects.Veicolo;

public class NoleggioActivity extends AppCompatActivity implements OnCommandReceivedListener {

    LinearLayout configurazioneLayout;
    LinearLayout noleggioLayout;
    LinearLayout postnoleggioLayout;
    LinearLayout fatturaLayout;

    EditText veicoloText;
    EditText scontoText;
    Button scontoButton;
    Button veicoloButton;
    Button avviaNoleggioButton;
    Button terminaNoleggioButton;
    CircleTimeView timerView;
    Thread timerThread;

    TextView noleggioIdText;
    TextView veicoloIdText;
    TextView inizioText;
    TextView fineText;
    TextView tariffaText;
    TextView scontoApplicatoText;
    TextView durataText;
    TextView costoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noleggio);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        configurazioneLayout = findViewById(R.id.configurazioneLayout);
        noleggioLayout = findViewById(R.id.noleggioLayout);
        postnoleggioLayout = findViewById(R.id.postnoleggioLayout);
        fatturaLayout = findViewById(R.id.fatturaLayout);

        veicoloText = findViewById(R.id.veicoloText);
        scontoText = findViewById(R.id.scontoText);
        noleggioIdText = findViewById(R.id.noleggioIdText);
        veicoloIdText = findViewById(R.id.veicoloIdText);
        inizioText = findViewById(R.id.inizioText);
        fineText = findViewById(R.id.fineText);
        tariffaText = findViewById(R.id.tariffaText);
        scontoApplicatoText = findViewById(R.id.scontoApplicatoText);
        durataText = findViewById(R.id.durataText);
        costoText = findViewById(R.id.costoText);

        scontoButton = findViewById(R.id.scontoButton);
        veicoloButton = findViewById(R.id.veicoloButton);
        avviaNoleggioButton = findViewById(R.id.avviaNoleggioButton);
        terminaNoleggioButton = findViewById(R.id.terminaNoleggioButton);
        timerView = findViewById(R.id.timerView);

        configurazioneLayout.setVisibility(View.GONE);
        noleggioLayout.setVisibility(View.GONE);
        postnoleggioLayout.setVisibility(View.GONE);
        fatturaLayout.setVisibility(View.GONE);
        mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.EXIST_NOLEGGIO_REQUEST, mainCore.getInstance().getUtente()), this);
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

            mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.VEICOLO_REQUEST, Integer.parseInt(num_v)), this);
            new GlideToast.makeToast(this, "Sto confermando il veicolo...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
            configurazioneLayout.setEnabled(false);
        }
    }

    public void confermaNoleggio(View view) {
        if (mainCore.getInstance().getNoleggioCorrente().getVeicoloCorrente() == null) {
            new GlideToast.makeToast(this, "Veicolo non confermato", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();
        } else {
            mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.NOLEGGIO_REQUEST, mainCore.getInstance().getNoleggioCorrente()), this);
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

            mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.SCONTO_REQUEST, sconto), this);
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

            @Override
            public void run() {

                while (true) {
                    timerView.setCurrentTime(timerView.getCurrentTimeInSeconds() + 1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        timerThread.start();
    }

    public void RecoverNoleggio() {
        AvviaNoleggio();
        int secondsBetween = (int) ((new Date().getTime() - mainCore.getInstance().getNoleggioCorrente().getInizio().getTime()) / 1000);
        timerView.setCurrentTime(secondsBetween);

    }

    public void TerminaNoleggio(View view) {
        timerThread.interrupt();
        new GlideToast.makeToast(this, "Richiedo la terminazione del noleggio", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
        mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.TERMINA_NOLEGGIO_REQUEST, mainCore.getInstance().getUtente()), this);

    }

    private void StopNoleggio() {
        noleggioLayout.setVisibility(View.GONE);
        postnoleggioLayout.setVisibility(View.VISIBLE);
    }

    public void MostraFattura(View view) {
        mainCore.getInstance().getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.GET_FATTURA_NOLEGGIO_REQUEST, mainCore.getInstance().getNoleggioCorrente()), this);
        new GlideToast.makeToast(NoleggioActivity.this, "Richiedo la fattura...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();

    }

    private void LoadFattura() {
        try {
            postnoleggioLayout.setVisibility(View.GONE);
            fatturaLayout.setVisibility(View.VISIBLE);

            Noleggio n = mainCore.getInstance().getNoleggioCorrente();
            noleggioIdText.setText(Integer.toString(n.getId()));
            veicoloIdText.setText(Integer.toString(n.getVeicoloCorrente().getCodice()));

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            inizioText.setText(format.format(n.getInizio()));

            Date fine = new Date();
            fine.setTime((long) (n.getInizio().getTime() + n.getFatturaCorrente().getDurata() * 60 * 1000));
            fineText.setText(format.format(fine));

            tariffaText.setText(Double.toString(n.getFatturaCorrente().getTariffa()) + "€ al min");
            if (n.getScontoCorrente() != null) {
                scontoApplicatoText.setText(Integer.toString(n.getScontoCorrente().getPerc_sconto()) + "%");
            } else {
                scontoApplicatoText.setText(Integer.toString(0) + "%");
            }

            durataText.setText(Double.toString(n.getFatturaCorrente().getDurata()) + " min");
            costoText.setText(Double.toString(n.getFatturaCorrente().getTotale()) + "€");

            new GlideToast.makeToast(NoleggioActivity.this, "Fattura caricata!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    public void ChiudiNoleggio(View view) {
        mainCore.getInstance().setNoleggioCorrente(null);
        finish();
    }

    @Override
    public void OnCommandReceivedCallback(Command cmd) {
        switch (cmd.getType()) {
            case EXIST_NOLEGGIO_RESPONSE:
                Noleggio n = (Noleggio) cmd.getArg();
                if (n == null) {
                    //TODO: Gestire la non autorizzazione al noleggio
                    finish();
                }

                mainCore.getInstance().setNoleggioCorrente(n);
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
                    n.setInizio(new Date());
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            configurazioneLayout.setEnabled(true);
                            configurazioneLayout.setVisibility(View.VISIBLE);
                        }
                    };
                    mainHandler.post(myRunnable);
                }
                break;

            case VEICOLO_RESPONSE:
                Veicolo v = (Veicolo) cmd.getArg();
                if (v != null) {
                    mainCore.getInstance().getNoleggioCorrente().setVeicoloCorrente(v);
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
                break;

            case SCONTO_RESPONSE:
                Sconto s = (Sconto) cmd.getArg();
                if (s != null) {
                    mainCore.getInstance().getNoleggioCorrente().setScontoCorrente(s);
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
                break;
            case NOLEGGIO_RESPONSE:
                String response = (String) cmd.getArg();
                response = response.toUpperCase().trim();
                if (response.equals("OK")) {
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
                break;

            case TERMINA_NOLEGGIO_RESPONSE:
                String res = (String) cmd.getArg();
                res = res.toUpperCase().trim();
                if (res.equals("OK")) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new GlideToast.makeToast(NoleggioActivity.this, "Noleggio terminato!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
                            StopNoleggio();
                        }
                    };
                    mainHandler.post(myRunnable);

                } else {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new GlideToast.makeToast(NoleggioActivity.this, "Errore nella terminazione del noleggio", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();

                        }
                    };
                    mainHandler.post(myRunnable);
                }
                break;

            case GET_FATTURA_NOLEGGIO_RESPONSE:
                Fattura f = (Fattura) cmd.getArg();
                if (f != null) {
                    mainCore.getInstance().getNoleggioCorrente().setFatturaCorrente(f);
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
                            new GlideToast.makeToast(NoleggioActivity.this, "Fattura non trovata per il noleggio corrente!", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST).show();

                        }
                    };
                    mainHandler.post(myRunnable);
                }
                break;
        }

        mainCore.getInstance().getClient().UnregisterListener(this);
    }

}
