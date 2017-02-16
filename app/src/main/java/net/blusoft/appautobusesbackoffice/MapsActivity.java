package net.blusoft.appautobusesbackoffice;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    String matricula_Autobus = null;
    int opcioEscollida;
    static final int OPCIO_ULTIMA = 1;
    static final int OPCIO_ENTRE_DADES = 2;


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
                System.out.println("Hola!!!");
                Random rand = new Random();
                LatLng posAutobus = new LatLng(posicio.getLatitude() + rand.nextFloat(), posicio.getLongitude() + rand.nextFloat());
                mMap.addMarker(new MarkerOptions()
                        .position(posAutobus)
                        .title(matricula_Autobus));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posAutobus, 10));

                if (1 == 1) {
                    handler.postDelayed(this, 10000);
                }
            }
        };
        handler.postDelayed(r, 10000);
    }


    @Override
    public void onClick(View view) {
        if (R.id.btnOpciones == view.getId()) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }
}
