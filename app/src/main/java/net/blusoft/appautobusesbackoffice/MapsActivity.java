package net.blusoft.appautobusesbackoffice;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {


    public static final int OPCIO_ULTIMA = 1;
    public static final int OPCIO_ENTRE_DADES = 2;
    private static final String LOGTAG = "BackOfficeAutobuses Map";
    private boolean actiu=true;
    private boolean firstUpdate=true;
    private boolean actualitzacioAutomatica = true;
    private GoogleMap mMap;
    private String matricula_Autobus, dataInici, dataFi = null;
    private int opcioEscollida=OPCIO_ULTIMA;
    JSONObject newPosicio = null;
    JSONObject posicio = null;
    ArrayList marcadors = new ArrayList<>();
    JSONArray llistanovesPosicions;
    private String direccioServidor="http://server.blusoft.net:8080";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Button botoEnrere = (Button) findViewById(R.id.btnOpciones);
        botoEnrere.setOnClickListener(this);
        Button botoActAuto = (Button) findViewById(R.id.btnAturarActualitzacioAutomatica);
        botoActAuto.setOnClickListener(this);
        if (savedInstanceState == null) {
            Intent extras = getIntent();
            opcioEscollida = extras.getIntExtra("opcion", OPCIO_ULTIMA);
            matricula_Autobus = extras.getStringExtra("matricula");
            switch (opcioEscollida) {
                case OPCIO_ULTIMA:
                    break;
                case OPCIO_ENTRE_DADES:
                    dataInici = extras.getStringExtra("fechaInicio");
                    dataFi = extras.getStringExtra("fechaFinal");
                    botoActAuto.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOGTAG, "Onpause...");
        actiu=false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG, "OnResume...");
        if(actualitzacioAutomatica){
            actiu=true;
            Log.d(LOGTAG, "Activat");
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        Log.d(LOGTAG, "Map is ready");
        actualitzaDades();
    }

    private void actualitzaDades(){
        Log.d(LOGTAG, "Metodo actualitzaDades. Actiu:"+actiu+" Actualitzacio Automatica:"+actualitzacioAutomatica);
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                if (actiu && actualitzacioAutomatica && opcioEscollida!=OPCIO_ENTRE_DADES) {
                    AsyncTask tarea = new refrescarPosiciones();
                    final AsyncTask execute = tarea.execute();
                    handler.postDelayed(this, 3000);
                }
                else{
                    AsyncTask tarea = new refrescarPosiciones();
                    final AsyncTask execute = tarea.execute();
                }
            }
        };
        handler.postDelayed(r, 0);
    }

    private void desactivarActualitzacionsAutomatiques(){
        Button boto= (Button) findViewById(R.id.btnAturarActualitzacioAutomatica);
        boto.setText(R.string.activaActAut);
        Log.d(LOGTAG, "Actualitzacio automatica desactivada");
        actualitzacioAutomatica=false;
    }
    @Override
    public void onClick(View view) {
        if (R.id.btnAturarActualitzacioAutomatica == view.getId()) {
            Button boto= (Button) findViewById(R.id.btnAturarActualitzacioAutomatica);
            if(actualitzacioAutomatica){
               desactivarActualitzacionsAutomatiques();
            }
            else{
                boto.setText(R.string.atura_act_aut);
                Log.d(LOGTAG, "Actualitzacio automatica activada");
                actiu=true;
                actualitzacioAutomatica=!actualitzacioAutomatica;
                actualitzaDades();
            }
        }

        if (R.id.btnOpciones == view.getId()) {
            Log.d(LOGTAG, "Retornant a pantalla principal.");
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            actiu = false;
            finish();
        }
    }

    class refrescarPosiciones extends AsyncTask<Object, Integer, Boolean> {


        public static final float HUE_RED = 0.0F;
        public static final float HUE_ORANGE = 30.0F;
        public static final float HUE_YELLOW = 60.0F;
        public static final float HUE_GREEN = 120.0F;
        public static final float HUE_CYAN = 180.0F;
        public static final float HUE_AZURE = 210.0F;
        public static final float HUE_BLUE = 240.0F;
        public static final float HUE_VIOLET = 270.0F;
        public static final float HUE_MAGENTA = 300.0F;


        @Override
        protected Boolean doInBackground(Object... params) {
            Log.d(LOGTAG, "Do in background....");
            switch (opcioEscollida) {
                case MapsActivity.OPCIO_ULTIMA:
                    ultimaPosicion(matricula_Autobus);
                    break;
                case MapsActivity.OPCIO_ENTRE_DADES:
                    entreFechas(matricula_Autobus);
            }
            return true;
        }

        private void entreFechas(String matricula) {
            System.out.println("TODO ENTRE FECHAS");
            BufferedReader reader;
            URL url = null;
            try {
                if (matricula.equals("Todas")) {
                    System.out.println("Not supported yet");
                } else {
                    url = new URL(direccioServidor+"/ServicioWeb/webresources/generic/obtenerPosiciones/" + matricula+"/"+dataInici+"-00:00:00/"+dataFi+"-23:59:00");
                    reader = getBufferedReader(url);
                    llistanovesPosicions = new JSONArray(reader.readLine());
                }
            }catch (java.io.FileNotFoundException ex) {
                Log.e(LOGTAG, "Error al obtenir la posicio de:" + url.toString()+"\n"+ex);
            }catch (java.io.IOException ex) {
                Log.e(LOGTAG, "Temps d'espera esgotat al iniciar la conexio amb la BBDD externa:" + url.toString()+"\n"+ex);
            } catch (org.json.JSONException ex) {
                Log.e(LOGTAG, "Error en la transformacio de l'objecte JSON: " + ex);
            }
        }

        private void ultimaPosicion(String matricula) {
            BufferedReader reader;
            URL url = null;
            try {
                if (matricula.equals("Todas")) {
                    Log.d(LOGTAG, "Obteniendo las ultimas posiciones de todos los autobuses");
                    url = new URL(direccioServidor+"/ServicioWeb/webresources/generic/obtenerUltimasPosicionesTodos/");
                    reader = getBufferedReader(url);
                    llistanovesPosicions = new JSONArray(reader.readLine());
                    System.out.println((llistanovesPosicions));
                } else {
                    url = new URL(direccioServidor+"/ServicioWeb/webresources/generic/obtenerUltimaPosicion/" + matricula);
                    reader = getBufferedReader(url);
                    newPosicio = new JSONObject(reader.readLine());
                }
            }
            catch (java.io.FileNotFoundException ex) {
                Log.e(LOGTAG, "Error al obtenir la posicio de:" + url.toString()+"\n"+ex);
            }catch (java.io.IOException ex) {
                Log.e(LOGTAG, "Temps d'espera esgotat al iniciar la conexio amb la BBDD externa:" + url.toString()+"\n"+ex);
            } catch (org.json.JSONException ex) {
                Log.e(LOGTAG, "Error en la transformacio de l'objecte JSON: " + ex);
            }
        }

        /**
         * Obte un objecte Buffered Reader que llegeix de l'objecte URL donat.
         *
         * @param url
         * @return
         * @throws java.io.IOException
         */
        private BufferedReader getBufferedReader(URL url) throws java.io.IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000 /*milliseconds*/);
            conn.setConnectTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json");
            return new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }

        /**
         * Pinta una posicio al mapa. Nomes pot ser cridat des del fil de la UI, per tant, en el
         * metode onPostExecute() de l'Async task o en el propi fil de la UI.
         *
         * @param posicio
         * @throws JSONException
         */
        private void pintar(JSONObject posicio, int id_Color) throws JSONException {

            LatLng posAutobus = new LatLng(posicio.getDouble("posX"), posicio.getDouble("posY"));
            MarkerOptions marker = new MarkerOptions()
                    .position(posAutobus)
                    .snippet("Fecha posicion: "+posicio.getString("fecha"))
                    .title(posicio.getString("matricula"))
                    .icon(BitmapDescriptorFactory.defaultMarker(getColor(id_Color)));
            if(firstUpdate) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posAutobus, 12));
                firstUpdate=false;
                Log.d(LOGTAG, "Moving zoom to first position.");
            }
            marcadors.add(mMap.addMarker(marker));
        }

        private float getColor(int id){
            float color;
            switch(id%9){
                case 1:
                    color=HUE_ORANGE;
                    break;
                case 2:
                    color=HUE_YELLOW;
                    break;
                case 3:
                    color=HUE_GREEN;
                    break;
                case 4:
                    color=HUE_CYAN;
                    break;
                case 5:
                    color=HUE_AZURE;
                    break;
                case 6:
                    color=HUE_BLUE;
                    break;
                case 7:
                    color=HUE_VIOLET;
                    break;
                case 8:
                    color=HUE_MAGENTA;
                    break;
                default:
                    color=HUE_RED;
                    break;
            }
            return color;
        }

        private void pintarLinea(JSONObject posicio, JSONObject newPosicio) throws JSONException {
            LatLng antPosAutobus = new LatLng(posicio.getDouble("posX"), posicio.getDouble("posY"));
            LatLng novaPosAutobus = new LatLng(newPosicio.getDouble("posX"), newPosicio.getDouble("posY"));
            PolylineOptions lineas = new PolylineOptions()
                    .add(antPosAutobus)
                    .add(novaPosAutobus);
            lineas.width(8);
            lineas.color(Color.RED);
            mMap.addPolyline(lineas);
            System.out.println("Pintando entre posiciones "+antPosAutobus.toString()+novaPosAutobus.toString());
        }

        private void pintarLinea(JSONArray posicions) throws JSONException {
            ArrayList<LatLng> llistaLatLonAutobusos = new ArrayList<>();
            for (int i = 0; i < llistanovesPosicions.length(); i++) {
                JSONObject jsonobject = llistanovesPosicions.getJSONObject(i);
                LatLng PosAutobus = new LatLng(jsonobject.getDouble("posX"), jsonobject.getDouble("posY"));
                System.out.println("pintando linea"+i);
                llistaLatLonAutobusos.add(PosAutobus);
            }
            mMap.addPolyline(new PolylineOptions().addAll(llistaLatLonAutobusos).color(Color.RED));
        }

        private Marker buscarMarcador(String matricula) {
            Marker item = null;
            for (Marker marcador : (Iterable<Marker>) marcadors) {
                item = marcador;
                if (item.getTitle().equals(matricula)) {
                    break;
                }
            }
            return item;
        }

        private void eliminarMarcador(Marker marcadorAEliminar) {
            if (marcadorAEliminar != null) {
                for (Marker item : (Iterable<Marker>) marcadors) {
                    if (item.getTitle().equals(marcadorAEliminar.getTitle())) {
                        item.remove();
                    }
                }
            }
        }

        /**
         * Metode que s'executa sempre que l'Async Task acaba
         *
         * @param result
         */
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Log.e(LOGTAG, "No se ha podido acceder a la posicion.");
            } else {
                try {
                    switch (opcioEscollida) {
                        case OPCIO_ULTIMA:
                            if (matricula_Autobus.equals("Todas")) {
                                //TODO este codigo ha estado fallando aleatoriamente y no localizo el origen.
                                Log.d(LOGTAG, "Pintando ultimas posiciones de todos los autobuses.");
                                for (int i = 0; i < llistanovesPosicions.length(); i++) {
                                    JSONObject jsonobject = llistanovesPosicions.getJSONObject(i);
                                    Marker marcador=buscarMarcador(jsonobject.getString("matricula"));
                                    if(marcador!=null){
                                        eliminarMarcador(marcador);
                                    }
                                    pintar(jsonobject, i);
                                }
                            } else {
                                if (newPosicio != null) {
                                    Log.d(LOGTAG, "Pintando ultima posicion del autobus:"+matricula_Autobus);
                                    if (posicio != null) {
                                        if (!newPosicio.toString().equals(posicio.toString())) {
                                            eliminarMarcador(buscarMarcador(matricula_Autobus));
                                            pintar(newPosicio,1);
                                            posicio = newPosicio;
                                        } else {
                                            Log.d(LOGTAG, "La posicio no ha cambiat");
                                        }
                                    } else {
                                        pintar(newPosicio,1);

                                        posicio = newPosicio;
                                    }
                                } else {
                                    Toast.makeText(MapsActivity.this, "No se ha podido acceder a la posicion del autobus:"+matricula_Autobus, Toast.LENGTH_SHORT).show();
                                    Log.e(LOGTAG, "No se ha podido acceder a la posicion del autobus:"+matricula_Autobus);
                                    desactivarActualitzacionsAutomatiques();
                                }
                            }
                            break;
                        case OPCIO_ENTRE_DADES:
                            Log.d(LOGTAG, "Pintando ultimas posiciones de todos los autobuses.");
                            JSONObject jsonobject = llistanovesPosicions.getJSONObject(1);
                            Marker marcador=buscarMarcador(jsonobject.getString("matricula"));
                            if(marcador!=null){
                                eliminarMarcador(marcador);
                            }
                            Log.d(LOGTAG, "Posiciones obtenidas: "+llistanovesPosicions.length());
                            pintarLinea(llistanovesPosicions);
                            pintar(jsonobject,1);
                            break;
                    }
                } catch (org.json.JSONException ex) {
                    Log.e(LOGTAG, "Error en la transformacio de l'objecte JSON: " + ex);
                }
            }
        }

    }
}

