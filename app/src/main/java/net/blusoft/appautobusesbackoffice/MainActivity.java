package net.blusoft.appautobusesbackoffice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnEnviar = (Button) findViewById(R.id.buttonEnviar);
        btnEnviar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (R.id.buttonEnviar == view.getId())
        {
            Intent i = new Intent(this, MapsActivity.class);
            i.putExtra("matricula", "4617DNO");
            i.putExtra("opcion", MapsActivity.OPCIO_ULTIMA);
            startActivity(i);
        }
    }
}
