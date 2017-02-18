package net.blusoft.appautobusesbackoffice;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    String matricula_Autobus = null;
    int opcioEscollida;
    static final int OPCIO_ULTIMA = 1;
    static final int OPCIO_ENTRE_DADES = 2;
    private static final String LOGTAG = "BackOfficeAutobuses Map";
    boolean actiu= true;


    Location posicio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Obtenim l'intent i les dades necesaries.
        Button boto = (Button) findViewById(R.id.btnOpciones);
        boto.setOnClickListener(this);
        if (savedInstanceState == null) {
            Intent extras = getIntent();
            matricula_Autobus = extras.getStringExtra("matricula");
            opcioEscollida = extras.getIntExtra("opcion", 0);
        }
        if (matricula_Autobus != null) {
            //TODO DO things...

            posicio = new Location("");//provider name is unecessary
            posicio.setLatitude(41.483945);//your coords of course
            posicio.setLongitude(2.177165);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        LatLng posAutobus = new LatLng(posicio.getLatitude(), posicio.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(posAutobus)
                .title(matricula_Autobus));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posAutobus, 10));

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                AsyncTask tarea = new TareaObtenerUltimaPosicion();
                tarea.execute();
                Random rand = new Random();
                /*LatLng posAutobus = new LatLng(posicio.getLatitude() + rand.nextFloat() / 1000, posicio.getLongitude() + rand.nextFloat() / 1000);
                mMap.addMarker(new MarkerOptions()
                        .position(posAutobus)
                        .title(matricula_Autobus));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posAutobus, 15));*/
                if (actiu) {
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.postDelayed(r, 3000);
    }


    @Override
    public void onClick(View view) {
        if (R.id.btnOpciones == view.getId()) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            actiu=false;
            finish();
        }
    }

    class TareaObtenerUltimaPosicion extends AsyncTask<Object, Integer, Boolean> {
        JSONObject posicio;

        @Override
        protected Boolean doInBackground(Object... params) {
            if(opcioEscollida==MapsActivity.OPCIO_ULTIMA){
                pintarUltimaPosicion(matricula_Autobus);
            }
            return true;
        }

       // private void pintarEntrePosiciones()
        private void pintarUltimaPosicion(String matricula){
            BufferedReader reader;
            try {
                URL url = new URL("http://192.168.50.28:8080/ServicioWeb/webresources/generic/obtenerUltimaPosicion/" + matricula);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(1000 /*milliseconds*/);
                conn.setConnectTimeout(5000);
                conn.setRequestProperty("Content-Type", "application/json");
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer buffer = new StringBuffer();
                String line = reader.readLine();
                System.out.println(line);
                posicio = new JSONObject(line);
                System.out.println(posicio.toString());
                System.out.println(line);

            } catch (java.io.IOException ex) {
                Log.e(LOGTAG, "Temps d'espera esgotat al iniciar la conexio amb la BBDD extena");
            } catch (org.json.JSONException ex) {
                Log.e(LOGTAG, "Error en la transformacio de l'objecte JSON: " + ex);
            }
        }
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Log.e(LOGTAG, "No se ha podido acceder a la posicion.");
            } else {
                try {
                    if (posicio != null) {
                        LatLng posAutobus = new LatLng(posicio.getDouble("posX"), posicio.getDouble("posY"));
                        mMap.addMarker(new MarkerOptions()
                                .position(posAutobus)
                                .title(posicio.getString("matricula")));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posAutobus, 15));
                    }
                    else{
                        Log.e(LOGTAG, "Error en la obtencio de la posicio");
                    }
                }catch(org.json.JSONException ex){
                        Log.e(LOGTAG, "Error en la transformacio de l'objecte JSON: " + ex);
                    }
            }
        }
    }

}

