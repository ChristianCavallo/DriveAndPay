package com.example.drivepay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.drivepay.Connection.OnCommandReceivedListener;
import com.jeevandeshmukh.glidetoastlib.GlideToast;

import org.apache.commons.validator.routines.EmailValidator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import SerializedObjects.Command;
import SerializedObjects.CommandsEnum;
import SerializedObjects.UserObjects.CartaCredito;
import SerializedObjects.UserObjects.Documento;
import SerializedObjects.UserObjects.InformazioniUtente;
import SerializedObjects.UserObjects.Utente;
import id.zelory.compressor.Compressor;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class RegisterActivity extends AppCompatActivity implements OnCommandReceivedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Integer step = 0;

    private EditText emailView;
    private EditText passwordView;
    private EditText nomeView;
    private EditText cognomeView;
    private EditText nascitaView;
    private EditText cfView;
    private ImageView cartaView;
    private ImageView patenteView;
    private EditText ccView;
    private EditText cvvView;

    private Compressor c;
    private OnCommandReceivedListener context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        c = new Compressor(this);
        context = this;


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mViewPager.setCurrentItem(step);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    public void pickImage(int tipo){
        cartaView = findViewById(R.id.imageViewCarta);
        patenteView = findViewById(R.id.imageViewPatente);
        EasyImage.openGallery(this, tipo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the images
                byte[] getBytes = {};
                Bitmap x;
                System.out.println("File Originale: " + imageFile.length() / 1024 + "kb");
                try {
                    c.setCompressFormat(Bitmap.CompressFormat.PNG);
                    c.setQuality(60);

                    x = c.compressToBitmap(imageFile);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    x.compress(Bitmap.CompressFormat.JPEG, 60, stream);

                   getBytes = stream.toByteArray();


                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                Documento d = new Documento(1, "");
                d.setRaw(getBytes);
                if(type == 1){
                    mainCore.getUtente().getInfoUtente().setCartaIdentita(d);
                    System.out.println("Carta d'identità selezionata: " + getBytes.length / 1024 + "kb");
                    cartaView.setImageBitmap(x);

                } else {
                    d.setTipo(2);
                    mainCore.getUtente().getInfoUtente().setPatente(d);
                    System.out.println("Patente selezionata: " + getBytes.length / 1024 + "kb");
                    patenteView.setImageBitmap(x);
                }

                //mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.UPLOAD_DOCUMENT_REQUEST,d), context);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registrazione, menu);
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

    public void selectCarta(View view) {
        pickImage(1);
    }

    public void selectPatente(View view) {
        pickImage(2);
    }

    public void AnnullaRegistrazione(View view){
        finish();
    }

    public void AvantiFase1(View view){
        emailView = findViewById(R.id.emailText);
        passwordView = findViewById(R.id.passwordText);
        nomeView = findViewById(R.id.nameText);
        cognomeView = findViewById(R.id.surnameText);
        nascitaView = findViewById(R.id.dateText);
        cfView = findViewById(R.id.cfText);

        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);
        nomeView.setError(null);
        cognomeView.setError(null);
        nascitaView.setError(null);
        cfView.setError(null);


        // Store values at the time of the login attempt.
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        String nome = nomeView.getText().toString();
        String cognome = cognomeView.getText().toString();
        String nascita = nascitaView.getText().toString();
        String cf = cfView.getText().toString();

        boolean cancel = false;
        View focusView = null;



        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!EmailValidator.getInstance().isValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (password.length() > 15) {
            passwordView.setError("La password è troppo lunga!");
            focusView = passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(nome)) {
            nomeView.setError(getString(R.string.error_field_required));
            focusView = nomeView;
            cancel = true;
        } else if (nome.length() > 30) {
            nomeView.setError("Nome troppo lungo, usa qualche diminutitivo");
            focusView = nomeView;
            cancel = true;
        }
        if (TextUtils.isEmpty(cognome)) {
            cognomeView.setError(getString(R.string.error_field_required));
            focusView = cognomeView;
            cancel = true;
        } else if (nome.length() > 30) {
            cognomeView.setError("nome troppo lungo, usa qualche diminutitivo");
            focusView = cognomeView;
            cancel = true;
        }

        try {
            Date date=new SimpleDateFormat("dd/MM/yyyy").parse(nascita);

            if (TextUtils.isEmpty(nascita)) {
                nascitaView.setError(getString(R.string.error_field_required));
                focusView = nascitaView;
                cancel = true;
            } else if (mainCore.getDiffYears(date, new Date())< 18) {
                nascitaView.setError("Devi essere maggiorenne per registrarti!");
                focusView = nascitaView;
                cancel = true;
            }
        } catch (ParseException e) {
            nascitaView.setError("Inserisci la data nel formato dd/MM/yyyy");
            focusView = nascitaView;
            cancel = true;
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(cf)) {
            cfView.setError(getString(R.string.error_field_required));
            focusView = cfView;
            cancel = true;
        } else if ( cf.length() != 16) {
            cfView.setError("Il codice fiscale deve essere di 16 caratteri");
            focusView = cfView;
            cancel = true;
        }

        if (cancel) {

            focusView.requestFocus();
        } else {

            Utente u = new Utente(email.trim(), password.trim());
            InformazioniUtente i = new InformazioniUtente(nome.trim(), cognome.trim(), nascita.trim(), cf.trim());
            u.setInfoUtente(i);
            mainCore.setUtente(u);
            System.out.println("Step 1 compleato!");
            step = 1;
            mViewPager.setCurrentItem(step);
            new GlideToast.makeToast(this, "Inserisci la documentazione adesso", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
        }
    }


    public void IndietroFase(View view){
        step--;
        mViewPager.setCurrentItem(step);
    }

    int uploadFase = 0;
    public void AvantiFase2(View view){
        Documento carta = mainCore.getUtente().getInfoUtente().getCartaIdentita();
        Documento patente = mainCore.getUtente().getInfoUtente().getPatente();

        if(carta != null && patente != null){

            if (uploadFase == 0) {
                System.out.println("Dimensione foto carta: " + carta.getRaw().length / 1024 + "kb");
                mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.UPLOAD_DOCUMENT_REQUEST, carta), context);
                new GlideToast.makeToast(this, "Sto caricando i documenti... Attendi", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
                uploadFase++;
            }

        } else {
            new GlideToast.makeToast(this, "Devi selezionare entrambi i documenti", GlideToast.LENGTHTOOLONG, GlideToast.WARNINGTOAST).show();
            System.out.println("Nessun file selezionato");
        }
    }

    public void FineFase3(View view){
        ccView = findViewById(R.id.ccText);
        cvvView = findViewById(R.id.cvvText);

        // Reset errors.
        ccView.setError(null);
        cvvView.setError(null);

        String cardNumber = ccView.getText().toString();
        String cvv = cvvView.getText().toString();
        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(cardNumber)) {
            ccView.setError(getString(R.string.error_field_required));
            focusView = ccView;
            cancel = true;
        } else if (cardNumber.length() != 16) {
            ccView.setError("Formato previsto: 16 numeri");
            focusView = ccView;
            cancel = true;
        }

        if (TextUtils.isEmpty(cvv)) {
            cvvView.setError(getString(R.string.error_field_required));
            focusView = cvvView;
            cancel = true;
        } else if (cvv.length() != 3) {
            ccView.setError("Formato previsto: 3 numeri");
            focusView = cvvView;
            cancel = true;
        } else {
            try{
                Integer.parseInt(cvv);
            } catch(Exception e){
                ccView.setError("Sono ammessi solo caratteri numerici");
                focusView = ccView;
                cancel = true;
            }
        }


        if (cancel) {

            focusView.requestFocus();
        } else {

            CartaCredito cc = new CartaCredito(cardNumber, cvv);
            mainCore.getUtente().getInfoUtente().setCarta(cc);
            System.out.println("Step 3 compleato!");
            new GlideToast.makeToast(RegisterActivity.this, "Sto terminando la registrazione...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();


            mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.REGISTER_REQUEST, mainCore.getUtente()), this);

        }


    }

    @Override
    public void OnCommandReceivedCallback(Command cmd) {
        if(cmd.getType() == CommandsEnum.CommandType.UPLOAD_DOCUMENT_RESPONSE){

            if(cmd.getArg() != "Errore"){
                String docname = (String) cmd.getArg();
                if(docname.contains("patente")){
                    mainCore.getUtente().getInfoUtente().getPatente().setDocumentFileName(docname);
                    uploadFase = 0;
                } else{
                    mainCore.getUtente().getInfoUtente().getCartaIdentita().setDocumentFileName(docname);
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Dimensione foto carta: " + mainCore.getUtente().getInfoUtente().getPatente().getRaw().length / 1024 + "kb");
                            new GlideToast.makeToast(RegisterActivity.this, "Upload patente...", GlideToast.LENGTHTOOLONG, GlideToast.INFOTOAST).show();
                            mainCore.getClient().SendCommandAsync(new Command(CommandsEnum.CommandType.UPLOAD_DOCUMENT_REQUEST, mainCore.getUtente().getInfoUtente().getPatente()), context);
                        }
                    };
                    mainHandler.post(myRunnable);

                }
            } else {
                System.out.println("Errore nell'upload dei documenti");
                return;
            }

            if(mainCore.getUtente().getInfoUtente().getPatente().getDocumentFileName().length() > 5 && mainCore.getUtente().getInfoUtente().getCartaIdentita().getDocumentFileName().length() > 5){
                System.out.println("Step 2 completato");

                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(RegisterActivity.this, "Documenti caricati correttamente!", GlideToast.LENGTHTOOLONG, GlideToast.SUCCESSTOAST).show();
                        step++;
                        mViewPager.setCurrentItem(step);
                    }
                };
                mainHandler.post(myRunnable);

            }

        }

        if(cmd.getType() == CommandsEnum.CommandType.REGISTER_RESPONSE){
            String response = (String) cmd.getArg();
            if(response.length() == 2){
                mainCore.getClient().UnregisterListener(this);
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        TerminaRegistrazione();
                    }
                };
                mainHandler.post(myRunnable);

            } else {
                System.out.println(response);
                Handler mainHandler = new Handler(Looper.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        new GlideToast.makeToast(RegisterActivity.this, "Errore nella registrazione!", GlideToast.LENGTHTOOLONG, GlideToast.FAILTOAST);

                    }
                };
                mainHandler.post(myRunnable);

            }
        }
    }

    public void TerminaRegistrazione(){
        finish();
    }


    public static class FragmentRegistrazione1 extends Fragment {


        public FragmentRegistrazione1() {
            // Required empty public constructor
        }
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static FragmentRegistrazione1 newInstance(int sectionNumber) {
            FragmentRegistrazione1 fragment = new FragmentRegistrazione1();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_registrazione, container, false);
        }

    }

    public static class FragmentRegistrazione2 extends Fragment {


        public FragmentRegistrazione2() {
            // Required empty public constructor
        }

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static FragmentRegistrazione2 newInstance(int sectionNumber) {
            FragmentRegistrazione2 fragment = new FragmentRegistrazione2();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_registrazione_2, container, false);
        }

    }

    public static class FragmentRegistrazione3 extends Fragment {


        public FragmentRegistrazione3() {
            // Required empty public constructor
        }

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static FragmentRegistrazione3 newInstance(int sectionNumber) {
            FragmentRegistrazione3 fragment = new FragmentRegistrazione3();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_registrazione_3, container, false);
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    return FragmentRegistrazione1.newInstance(position+1);

                case 1:
                    return FragmentRegistrazione2.newInstance(position+1);

                case 2:
                    return FragmentRegistrazione3.newInstance(position+1);

                default:
                    return null;
            }
            //return new FragmentRegistrazione3();
           // return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
