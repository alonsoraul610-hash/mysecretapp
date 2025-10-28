package com.example.aplicacion_sencilla_publicidad;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.EdgeToEdge;
import android.view.View;

public class EnDesarrolloActivity_v2 extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_en_desarrollo_v2);
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Edge-to-edge
        EdgeToEdge.enable(this);

        drawerLayout = findViewById(R.id.drawerLayout);
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("En desarrollo");

        // Botón hamburguesa
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Menú lateral
        NavigationView navigationView = findViewById(R.id.navigationView);
        actualizarHeaderUsuario(navigationView);
        // 🔹 Manejo de clics en las opciones del menú
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Inicio seleccionado", Toast.LENGTH_SHORT).show();
                getSupportActionBar().setTitle("Buscador");
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Perfil seleccionado", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, PerfilActivity.class));
            } else if (id == R.id.nav_favoritos) {
                Toast.makeText(this, "Favoritos seleccionado", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, EnDesarrolloActivity.class));
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Configuración seleccionada", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, EnDesarrolloActivity.class));
            } else if (id == R.id.nav_help) {
                Toast.makeText(this, "Ayuda seleccionada", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, EnDesarrolloActivity.class));
            } else if (id == R.id.nav_logout) {
                Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, EnDesarrolloActivity_v2.class));
            } else if (id == R.id.nav_message) {
                Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, EnDesarrolloActivity.class));
            } else if (id == R.id.nav_create) {
                Toast.makeText(this, "Crear anuncio", Toast.LENGTH_SHORT).show();
                getSupportActionBar().setTitle("Crear anuncio");
                startActivity(new Intent(this, CrearAnuncioActivity.class));
            }
            drawerLayout.closeDrawers(); // Cierra el menú tras pulsar
            return true;
        });
    }

    protected void actualizarHeaderUsuario(NavigationView navigationView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView textUserName = headerView.findViewById(R.id.textUserName);
            ImageView imageProfile = headerView.findViewById(R.id.imageProfile);

            String nombreUsuario = user.getDisplayName();
            if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
                textUserName.setText(nombreUsuario);
            }

            Uri fotoPerfil = user.getPhotoUrl();
            if (fotoPerfil != null) {
                Glide.with(this).load(fotoPerfil).placeholder(R.drawable.ic_persona).into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.ic_persona);
            }
        }
    }
}
