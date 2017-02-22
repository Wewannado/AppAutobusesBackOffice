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
    private ArrayList<String> matricules;
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
        new populateSpinner().execute();
    }

    @Override
    public void onClick(View view) {
//TODO falta que los datepicker recojan tambien la hora. el formato tiene que ser dd-mm-yyyy hh24:mm:ss
        if (view == editTextHInicio) {
            fromDatePickerDialog.show();
        } else if (view == editTextHFinal) {
            toDatePickerDialog.show();
        }
        System.out.println("date inicio vale" + editTextHInicio.getText().toString());
        System.out.println("date fin vale " + editTextHFinal.getText().toString());

        if (R.id.buttonEnviar == view.getId()) {
            if (spinnerCarregat) {
                if (editTextHInicio.getText().toString().equals("") && editTextHFinal.getText().toString().equals("") && opcion == MapsActivity.OPCIO_ENTRE_DADES) {
                    Toast.makeText(this, "Fechas Obligatorias", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("opcion marcada: " + opcion);
                    Intent i = new Intent(this, MapsActivity.class);
                    i.putExtra("matricula", spinnerMatricules.getSelectedItem().toString());
                    i.putExtra("opcion", opcion);
                    i.putExtra("fechaInicio", editTextHInicio.getText().toString());
                    i.putExtra("fechaFinal", editTextHFinal.getText().toString());
                    startActivity(i);
                }
            } else {
                Toast.makeText(MainActivity.this, "No s'han pogut carregar les dades. Hi ha conexió a internet?", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onRadioButtonClicked(View view) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButtonHoras:
                editTextHInicio.setVisibility(View.VISIBLE);
                editTextHFinal.setVisibility(View.VISIBLE);
                opcion = MapsActivity.OPCIO_ENTRE_DADES;
                break;
            case R.id.radioButtonUltPos:
                editTextHInicio.setVisibility(View.INVISIBLE);
                editTextHFinal.setVisibility(View.INVISIBLE);
                opcion = MapsActivity.OPCIO_ULTIMA;
                break;
        }
    }


    private void setDateTimeField() {
        editTextHInicio.setOnClickListener(this);
        editTextHFinal.setOnClickListener(this);

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
            JSONObject jsonobject;
            JSONArray jsonarray;
            matricules = new ArrayList<>();
            try {
                String direccioServidor = "http://server.blusoft.net:8080";
                URL url = new URL(direccioServidor + "/ServicioWeb/webresources/generic/autobuses/");
                BufferedReader reader = getBufferedReader(url);
                jsonarray = new JSONArray(reader.readLine());
                for (int i = 0; i < jsonarray.length(); i++) {
                    jsonobject = jsonarray.getJSONObject(i);
                    matricules.add(jsonobject.optString("matricula"));
                }
                matricules.add("Todas");
            } catch (Exception e) {
                result = false;
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return result;
        }

        private BufferedReader getBufferedReader(URL url) throws java.io.IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000 /*milliseconds*/);
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");
            return new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }

        @Override
        protected void onPostExecute(Boolean results) {
            // Locate the spinner in activity_main.xml
            System.out.println("onpostexecute");
            if (results) {
                Spinner mySpinner = (Spinner) findViewById(R.id.spinnerMatricules);
                // Spinner adapter
                mySpinner
                        .setAdapter(new ArrayAdapter<>(MainActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                matricules));
                spinnerCarregat = true;
            } else {
                Toast.makeText(MainActivity.this, "No s'han pogut carregar les dades. Hi ha conexió a internet?", Toast.LENGTH_LONG).show();
            }
        }
    }

}
