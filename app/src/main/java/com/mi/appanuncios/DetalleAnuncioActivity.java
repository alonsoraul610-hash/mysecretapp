package com.mi.appanuncios;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

public class DetalleAnuncioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_anuncio);

        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // 🔹 Configurar la barra superior (Toolbar)
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Detalles del anuncio");


        if (getSupportActionBar() != null) {
            // Mostrar el botón de retroceso en la Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Acción del botón de retroceso
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 🔹 Referencias a las vistas
        ImageView imageView = findViewById(R.id.imageDetalle);
        TextView textLocalidad = findViewById(R.id.textDetalleLocalidad);
        TextView textDescripcion = findViewById(R.id.textDetalleDescripcion);
        TextView textTelefono = findViewById(R.id.textDetalleTelefono);

        // 🔹 Obtener datos del Intent
        String descripcion = getIntent().getStringExtra("descripcion");
        String telefono = getIntent().getStringExtra("telefono");
        String localidad = getIntent().getStringExtra("localidad");
        String imagenUri = getIntent().getStringExtra("imagenUri");

        // 🔹 Asignar los datos
        textLocalidad.setText(localidad);
        textDescripcion.setText(descripcion);
        textTelefono.setText("Teléfono: " + telefono);

        // 🔹 Cargar imagen con Glide
        Glide.with(this)
                .load(imagenUri)
                .placeholder(R.drawable.ic_persona)
                .into(imageView);
    }
}
