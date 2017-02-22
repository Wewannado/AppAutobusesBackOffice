package net.blusoft.appautobusesbackoffice;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int opcion = MapsActivity.OPCIO_ULTIMA;
    RadioGroup radioGroup;
    TextView editTextHInicio;
    TextView editTextHFinal;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    Spinner spinnerMatricules;
    private SimpleDateFormat dateFormatter;
    private ArrayList<String> matricules=new ArrayList<>();
    private boolean spinnerCarregat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnEnviar = (Button) findViewById(R.id.buttonEnviar);
        btnEnviar.setOnClickListener(this);
        spinnerMatricules = (Spinner) findViewById(R.id.spinnerMatricules);
        radioGroup = (RadioGroup) findViewById(R.id.rdgGrupo);
        editTextHInicio = (TextView) findViewById(R.id.editTextHInicio);
        editTextHFinal = (TextView) findViewById(R.id.editTextHFin);
        //DATE
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
        editTextHFinal.setInputType(InputType.TYPE_NULL);
        editTextHFinal.requestFocus();
        editTextHInicio.setInputType(InputType.TYPE_NULL);
        editTextHInicio.requestFocus();
        setDateTimeField();
        //visibilidad
        editTextHInicio.setVisibility(View.INVISIBLE);
        editTextHFinal.setVisibility(View.INVISIBLE);
        //Listeners
        editTextHInicio.setOnClickListener(this);
        editTextHFinal.setOnClickListener(this);
        /*
        Al crear la aplicacio, obtenim les dades remotes per tal d'omplir l'Spinner.
         */
        new populateSpinner().execute();
    }

    @Override
    public void onClick(View view) {
        //Si s'ha clicat al edit Text per seleccionar una data inicial
        if (view == editTextHInicio) {
            fromDatePickerDialog.show();
        }
        //Si s'ha clicat al edit Text per seleccionar una data final
        if (view == editTextHFinal) {
            toDatePickerDialog.show();
        }

        //Si s'ha clicat al boto d'enviar dades
        if (R.id.buttonEnviar == view.getId()) {
            //Validem que s'hagi carregat les dades de l'Spinner
            if (spinnerCarregat) {
                //Si s'ha seleccionat la opcio entre dades, es obligatori posar les mateixes
                if (editTextHInicio.getText().toString().equals("") && editTextHFinal.getText().toString().equals("") && opcion == MapsActivity.OPCIO_ENTRE_DADES) {
                    Toast.makeText(this, "Fechas Obligatorias", Toast.LENGTH_SHORT).show();
                    //En cas contrari...
                } else {
                    //TODO En futures versions es podran veure el recorregut de tots els autobusos entre dues dades.
                    if (opcion == MapsActivity.OPCIO_ENTRE_DADES && spinnerMatricules.getSelectedItem().toString().equals("Todas")) {
                        Toast.makeText(MainActivity.this, "Veure les posicions entre dades de tots els autobusos no soportat en aquesta versió.", Toast.LENGTH_LONG).show();
                    } else {
                        //Si no s'ha complert cap de les condicions anteriors, pasem a la segona activity.
                        System.out.println("opcion marcada: " + opcion);
                        Intent i = new Intent(this, MapsActivity.class);
                        i.putExtra("matricula", spinnerMatricules.getSelectedItem().toString());
                        //La opcio seleccionada
                        i.putExtra("opcion", opcion);
                        i.putExtra("fechaInicio", editTextHInicio.getText().toString());
                        i.putExtra("fechaFinal", editTextHFinal.getText().toString());
                        startActivity(i);
                        finish();
                    }
                }
            }
            // No s'ha carregat l'Spinner, per tant, no pasem a la segona activity.
            else {
                Toast.makeText(MainActivity.this, "No s'han pogut carregar les dades. Hi ha conexió a internet?", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onRadioButtonClicked(View view) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            //Si s'ha seleccionat veure el mapa entre dues dades
            case R.id.radioButtonHoras:
                //Mostrem els inputs per a que l'usuari pugui inserir les dates entre les que vol veure
                editTextHInicio.setVisibility(View.VISIBLE);
                editTextHFinal.setVisibility(View.VISIBLE);
                opcion = MapsActivity.OPCIO_ENTRE_DADES;
                break;
            //Si s'ha seleccionar veure les ultimes posicions
            case R.id.radioButtonUltPos:
                //Ocultem els fields per a l'input de dates
                editTextHInicio.setVisibility(View.INVISIBLE);
                editTextHFinal.setVisibility(View.INVISIBLE);
                opcion = MapsActivity.OPCIO_ULTIMA;
                break;
        }
    }

    /**
     * Metode que crea dos datePickerDialog que mostren un DatePicker i, en retorna una data, la insereixen
     * als editText corresponents
     */
    private void setDateTimeField() {


        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                editTextHInicio.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                editTextHFinal.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }


    private class populateSpinner extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = true;
            try {
                String direccioServidor = "http://server.blusoft.net:8080";
                URL url = new URL(direccioServidor + "/ServicioWeb/webresources/generic/autobuses/");
                BufferedReader reader = getBufferedReader(url);
                JSONArray jsonarray = new JSONArray(reader.readLine());
                //Afegim tots els autobusos trobats a un array
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    matricules.add(jsonobject.optString("matricula"));
                }
                //Per ultim, afegim una opcio "totes"
                matricules.add("Todas");
            }
            /*Fem catch dels diferents errors que podem trobar. Cualsevol error que ens doni
             aqui impedira que es carregui completament l'Spinner.
            */
            catch (Exception e) {
                result = false;
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return result;
        }

        /**
         * Given a URL, returns a initialitzed BufferedReader. Default TimeOut is 5 seconds.
         * Expects to receive a JSON response
         * @param url The URL we want to connect to.
         * @return The Buffered reader
         * @throws java.io.IOException
         */
        private BufferedReader getBufferedReader(URL url) throws java.io.IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000 /*milliseconds*/);
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");
            return new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // si s'han carregat les dades, inflem l'Spinner
            if (result) {
                Spinner mySpinner = (Spinner) findViewById(R.id.spinnerMatricules);
                // Spinner adapter
                mySpinner
                        .setAdapter(new ArrayAdapter<>(MainActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                matricules));
                //Boolea de control per determinar si s'ha rebut totes les dades extenes
                spinnerCarregat = true;
            } else {
                Toast.makeText(MainActivity.this, "No s'han pogut carregar les dades. Hi ha conexió a internet?", Toast.LENGTH_LONG).show();
            }
        }
    }

}
