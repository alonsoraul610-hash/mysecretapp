package com.example.aplicacion_sencilla_publicidad;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.view.View;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.widget.ArrayAdapter;

import android.text.Editable;
import android.text.TextWatcher;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.SetOptions;


import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    //private AutoCompleteTextView searchAutoComplete;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> suggestions = new ArrayList<>();
    private RequestQueue requestQueue;
    private long lastTextEditTime = 0;
    private final long DEBOUNCE_DELAY = 500;
    private final android.os.Handler handler = new android.os.Handler();
    private Runnable searchRunnable;

    private MaterialAutoCompleteTextView searchAutoComplete;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        // Revisar si el usuario ya está logueado
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            // Usuario no logueado → abrir LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return; // evita ejecutar más código
        }

        if (user != null) {
            String uid = user.getUid(); // Identificador único
            String name = user.getDisplayName(); // Nombre del usuario
            String email = user.getEmail(); // Correo electrónico
            //String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("uid", uid);
            //userData.put("photoUrl", photoUrl);

            db.collection("users").document(uid)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario actualizado correctamente"))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error al guardar usuario", e));



            // Puedes mostrar los datos o guardarlos en la base de datos
        }

        // Usuario logueado → continuar con la MainActivity
        //setContentView(R.layout.activity_main);



        // Obtener el usuario actual de Firebase
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Accedemos al NavigationView que está en tu activity_main.xml
            NavigationView navigationView = findViewById(R.id.navigationView);

            // Obtenemos la vista del header (la parte superior del menú lateral)
            View headerView = navigationView.getHeaderView(0);

            // Referencias a los elementos del header
            TextView textUserName = headerView.findViewById(R.id.textUserName);
            ImageView imageProfile = headerView.findViewById(R.id.imageProfile);

            // Asignamos el nombre del usuario (si existe)
            String nombreUsuario = user.getDisplayName();
            if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
                textUserName.setText(nombreUsuario);
            } else {
                textUserName.setText("Usuario sin nombre");
            }

            // Asignamos la foto de perfil (si tiene)
            Uri fotoPerfil = user.getPhotoUrl();
            if (fotoPerfil != null) {
                Glide.with(this)
                        .load(fotoPerfil)
                        .placeholder(R.drawable.ic_menu) // Imagen por defecto mientras carga
                        .into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.ic_menu); // Imagen por defecto
            }
        }


        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main);


        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Aquí defines la variable toolbar y la conectas con el layout
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);


        // (Opcional) Para usar la Toolbar como ActionBar
        setSupportActionBar(toolbar);



        // Mostrar botón de retroceso (flecha atrás) si en el futuro añades más pantallas:
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Cambiar el título dinámicamente:
        getSupportActionBar().setTitle("Buscador");
        //getSupportActionBar().setTitle("Inicio");
        //toolbar.setTitle("Nueva sección");


        toolbar.setNavigationOnClickListener(v ->
                Toast.makeText(this, "Menú hamburguesa pulsado", Toast.LENGTH_SHORT).show()
        );

        // 🔹 Sincroniza el botón hamburguesa con el Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigationView);

        /// ///
        View headerView = navigationView.getHeaderView(0);
        TextView textUserName = headerView.findViewById(R.id.textUserName);
        /// //

        TextInputLayout searchInputLayout = findViewById(R.id.searchInputLayout);

        // 🔹 Manejo de clics en las opciones del menú
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Inicio seleccionado", Toast.LENGTH_SHORT).show();
                searchInputLayout.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle("Buscador");
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Perfil seleccionado", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_favoritos) {
                Toast.makeText(this, "Favoritos seleccionado", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Configuración seleccionada", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_help) {
                Toast.makeText(this, "Ayuda seleccionada", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_create) {
                Toast.makeText(this, "Crear anuncio", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(this, CrearAnuncioActivity.class));
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new CrearAnuncioFragment())
                        .addToBackStack(null)
                        .commit();
                searchInputLayout.setVisibility(View.GONE);
                getSupportActionBar().setTitle("Crear anuncio");

            }

            drawerLayout.closeDrawers(); // Cierra el menú tras pulsar
            return true;
        });



        // logica buscar localidades ---------
        abstract class SimpleTextWatcher implements TextWatcher {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        }

        // Búsqueda por localidad
        searchAutoComplete = findViewById(R.id.searchAutoComplete);
        requestQueue = Volley.newRequestQueue(this);

        // Adaptador del autocompletado
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        searchAutoComplete.setAdapter(adapter);

        // Listener simplificado de texto
        searchAutoComplete.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
                if (s.length() >= 2) {
                    searchRunnable = () -> buscarLocalidades(s.toString());
                    handler.postDelayed(searchRunnable, 500); // Espera antes de buscar
                }
            }
        });

    }



    //metodo que se utiliza en el main, para realizar la busqueda de las localidades
    private void buscarLocalidades(String texto) {
        String busquedaActual = texto;
        String url = String.format(
                "https://nominatim.openstreetmap.org/search?q=%s&format=jsonv2&limit=10&countrycodes=ES",
                Uri.encode(texto)
        );

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    // Ignorar si el texto cambió mientras se hacía la búsqueda
                    if (!busquedaActual.equals(searchAutoComplete.getText().toString())) return;

                    List<String> resultados = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            String displayName = response.getJSONObject(i).getString("display_name");
                            // Tomar solo los primeros 3 segmentos separados por coma
                            String corto = abreviarDisplayName(displayName, 3);
                            resultados.add(corto); // <-- SOLO la versión corta
                        }

                        // Actualizar adaptador
                        adapter.clear();
                        adapter.addAll(resultados);
                        adapter.notifyDataSetChanged();

                        // Mostrar desplegable si hay resultados
                        if (!resultados.isEmpty()) {
                            searchAutoComplete.post(() -> {
                                String textoActual = searchAutoComplete.getText().toString();
                                if (searchAutoComplete.hasFocus() && !textoActual.isEmpty()) {
                                    adapter.getFilter().filter(textoActual, count -> searchAutoComplete.showDropDown());
                                }
                            });
                        }

                    } catch (Exception e) {
                        mostrarToast("Error procesando la respuesta");
                        e.printStackTrace();
                    }
                },
                error -> mostrarToast("Error de red: " + error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return Map.of("User-Agent", "MiApp/1.0 (alonsoraul610@gmail.com)");
            }
        };

        requestQueue.add(request);
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private String abreviarDisplayName(String displayName, int maxSegmentos) {
        if (displayName == null || displayName.isEmpty()) return displayName;

        // Separar por coma
        String[] partes = displayName.split(",");
        int take = Math.min(partes.length, maxSegmentos);
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < take; j++) {
            // Limpiar espacios y eliminar el texto después de "/" en este segmento
            String p = partes[j].trim();
            if (p.contains("/")) {
                p = p.substring(0, p.indexOf("/")).trim();
            }

            if (p.isEmpty()) continue;

            if (sb.length() > 0) sb.append(", ");
            sb.append(p);
        }

        return sb.toString();
    }

}



