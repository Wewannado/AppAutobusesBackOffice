package net.blusoft.appautobusesbackoffice;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextMatricula;
    private int opcion;
    RadioGroup radioGroup;
    TextView editTextHInicio;
    TextView editTextHFinal;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;

    private SimpleDateFormat dateFormatter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnEnviar = (Button) findViewById(R.id.buttonEnviar);
        btnEnviar.setOnClickListener(this);
        editTextMatricula = (EditText)findViewById(R.id.editTextMatricula);
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
    }

    @Override
    public void onClick(View view) {

        if(view == editTextHInicio) {
            fromDatePickerDialog.show();
        } else if(view == editTextHFinal) {
            toDatePickerDialog.show();
        }
        if (R.id.buttonEnviar == view.getId())
        {
            System.out.println(opcion);
            Intent i = new Intent(this, MapsActivity.class);
            i.putExtra("matricula", editTextMatricula.toString());
            i.putExtra("opcion", opcion);
            i.putExtra("fechaInicio", editTextHInicio.toString());
            i.putExtra("fechaFinal", editTextHFinal.toString());
            startActivity(i);
        }
    }

    public void onRadioButtonClicked(View view)
    {
        switch (radioGroup.getCheckedRadioButtonId())
        {
            case R.id.radioButtonHoras:
                editTextHInicio.setVisibility(View.VISIBLE);
                editTextHFinal.setVisibility(View.VISIBLE);
                opcion = MapsActivity.OPCIO_ULTIMA;
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

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                editTextHFinal.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/


}
