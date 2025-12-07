package com.mi.appanuncios;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class MainActivity extends AppCompatActivity {


    private ArrayAdapter<String> adapter;  //BUSCADOR
    private ArrayList<String> suggestions = new ArrayList<>();  //BUSCADOR
    private RequestQueue requestQueue;  //BUSCADOR
    private long lastTextEditTime = 0; //BUSCADOR
    private final android.os.Handler handler = new android.os.Handler(); //BUSCADOR
    private Runnable searchRunnable; //BUSCADOR

    private MaterialAutoCompleteTextView searchAutoComplete;  // BUSCADOR


    private Map<String, double[]> coordenadasLocalidades = new HashMap<>();
    private double selectedLat = 0.0;
    private double selectedLon = 0.0;

    // para mostrar los anuncios por pantalla
    private RecyclerView recyclerAnuncios;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private AnuncioAdapter anuncioAdapter;
    private EditText editRadio;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        editRadio = findViewById(R.id.editRadio);
        //para que cada vez que se modifique el valor del radio se lance una busqueda
        editRadio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesitamos hacer nada aquí
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cada vez que cambia el texto, lanzamos la búsqueda
                double radio = 0;
                try {
                    radio = Double.parseDouble(s.toString());
                } catch (NumberFormatException e) {
                    radio = 0; // valor por defecto si el usuario borra el texto
                }

                // Llamamos a la búsqueda solo si ya hay coordenadas seleccionadas
                if (selectedLat != 0 && selectedLon != 0) {
                    buscarAnunciosPorCoordenadas(selectedLat, selectedLon, radio);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesitamos hacer nada aquí
            }
        });

        //fin para que cada vez que se modifique el valor del radio se lance una busqueda


        // INICIO ---------FIREBASE   y FIRESTORE -------------//

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
                        .placeholder(R.drawable.ic_persona) // Imagen por defecto mientras carga
                        .into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.ic_persona); // Imagen por defecto
            }
        }

        // FINAL ---------FIREBASE   y  FIRESTORE -------------//

        // START -------- BANER SUPERIOR Y MENU ---------------------//

        EdgeToEdge.enable(this);

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

        View headerView = navigationView.getHeaderView(0);
        TextView textUserName = headerView.findViewById(R.id.textUserName);

        TextInputLayout searchInputLayout = findViewById(R.id.searchInputLayout);

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

        // FINAL -------- BANER SUPERIOR Y MENU ---------------------//

        // Búsqueda por localidad
        inicializarBusquedaLocalidades();
        searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String seleccion = (String) parent.getItemAtPosition(position);
            if (coordenadasLocalidades.containsKey(seleccion)) {
                double[] coords = coordenadasLocalidades.get(seleccion);
                selectedLat = coords[0];
                selectedLon = coords[1];

                Log.d("LocalidadSeleccionada", "Lat: " + selectedLat + ", Lon: " + selectedLon);
                Toast.makeText(this, "Seleccionado: " + seleccion +
                        "\nLat: " + selectedLat + ", Lon: " + selectedLon, Toast.LENGTH_SHORT).show();

                // 🔹 par el radio

                double radio =0;
                try {
                    radio = Double.parseDouble(editRadio.getText().toString());
                } catch (NumberFormatException e) {
                    radio = 0; // si no es válido, tomamos 0 por defecto
                }
                // 🔹 Llama aquí a la búsqueda en Firebase
                buscarAnunciosPorCoordenadas(selectedLat, selectedLon, radio);
                //buscarAnunciosPorCoordenadas(selectedLat, selectedLon);
            }
        });

        //FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users");


// Latitud y longitud seleccionadas
        double lat = selectedLat;
        double lon = selectedLon;

// Tolerancia pequeña por si hay ligeras variaciones
       // double epsilon = 0.0001; // antes 0.0001

// 🔹 Buscar todos los anuncios de todos los usuarios


        //esto es para mostrar los anuncios por pantalla
        recyclerAnuncios = findViewById(R.id.recyclerAnuncios);
        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        anuncioAdapter = new AnuncioAdapter(this, listaAnuncios, anuncio -> {
            Intent intent = new Intent(MainActivity.this, DetalleAnuncioActivity.class);
            intent.putExtra("descripcion", anuncio.getDescripcion());
            intent.putExtra("telefono", anuncio.getTelefono());
            intent.putExtra("localidad", anuncio.getLocalidad());
            intent.putExtra("imagenUri", anuncio.getImagenUri());
            startActivity(intent);
        });
        recyclerAnuncios.setAdapter(anuncioAdapter);


    }

    private void inicializarBusquedaLocalidades() {
        // Búsqueda por localidad
        searchAutoComplete = findViewById(R.id.searchAutoComplete);
        requestQueue = Volley.newRequestQueue(this);

        // Adaptador del autocompletado
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        searchAutoComplete.setAdapter(adapter);



        searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String seleccion = (String) parent.getItemAtPosition(position);

            if (coordenadasLocalidades.containsKey(seleccion)) {
                double[] coords = coordenadasLocalidades.get(seleccion);
                double lat = coords[0];
                double lon = coords[1];

                // 🔹 Imprime en el Logcat
                Log.d("LocalidadSeleccionada", "Localidad: " + seleccion + " | Lat: " + lat + " | Lon: " + lon);

                // (Opcional) Toast para confirmarlo visualmente
                Toast.makeText(this, "Lat: " + lat + "\nLon: " + lon, Toast.LENGTH_SHORT).show();
            }
        });





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
                        coordenadasLocalidades.clear(); // Limpiamos resultados previos
                        for (int i = 0; i < response.length(); i++) {
                            String displayName = response.getJSONObject(i).getString("display_name");
                            double lat = response.getJSONObject(i).getDouble("lat");
                            double lon = response.getJSONObject(i).getDouble("lon");

                            // Tomar solo los primeros 3 segmentos del nombre
                            String corto = abreviarDisplayName(displayName, 3);

                            resultados.add(corto);
                            coordenadasLocalidades.put(corto, new double[]{lat, lon});
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
                String email = "default@example.com";
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getEmail() != null) {
                    email = user.getEmail();
                }
                return Map.of("User-Agent", "MiApp/1.0 (" + email + ")");
            }

        };

        requestQueue.add(request);
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

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }



    private void buscarAnunciosPorCoordenadas(double lat, double lon, double radioKm) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (radioKm == 0) {
            // Búsqueda exacta (como ya tenías)
            double epsilon = 0.0001;

            db.collectionGroup("anuncios")
                    .whereGreaterThanOrEqualTo("latitud", lat - epsilon)
                    .whereLessThanOrEqualTo("latitud", lat + epsilon)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        listaAnuncios.clear();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Double lonAnuncio = doc.getDouble("longitud");
                            if (lonAnuncio != null && Math.abs(lonAnuncio - lon) < epsilon) {
                                String descripcion = doc.getString("descripcion");
                                String telefono = doc.getString("telefono");
                                String localidad = doc.getString("localidad");
                                String imagenUri = doc.getString("imagenUri");

                                listaAnuncios.add(new Anuncio(descripcion, telefono, localidad, imagenUri));
                            }
                        }

                        anuncioAdapter.notifyDataSetChanged();

                        if (listaAnuncios.isEmpty()) {
                            Toast.makeText(this, "No hay anuncios en esta localidad", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Se encontraron " + listaAnuncios.size() + " anuncios", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("FirestoreError", "Error al buscar anuncios", e));

        } else {
            // Búsqueda por radio
            double deltaLat = radioKm / 111.0; // 1° aprox = 111 km
            double deltaLon = radioKm / (111.0 * Math.cos(Math.toRadians(lat)));

            double minLat = lat - deltaLat;
            double maxLat = lat + deltaLat;
            double minLon = lon - deltaLon;
            double maxLon = lon + deltaLon;

            db.collectionGroup("anuncios")
                    .whereGreaterThanOrEqualTo("latitud", minLat)
                    .whereLessThanOrEqualTo("latitud", maxLat)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        listaAnuncios.clear();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Double lonAnuncio = doc.getDouble("longitud");
                            if (lonAnuncio != null && lonAnuncio >= minLon && lonAnuncio <= maxLon) {
                                String descripcion = doc.getString("descripcion");
                                String telefono = doc.getString("telefono");
                                String localidad = doc.getString("localidad");
                                String imagenUri = doc.getString("imagenUri");

                                listaAnuncios.add(new Anuncio(descripcion, telefono, localidad, imagenUri));
                            }
                        }

                        anuncioAdapter.notifyDataSetChanged();

                        if (listaAnuncios.isEmpty()) {
                            Toast.makeText(this, "No hay anuncios en el radio seleccionado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Se encontraron " + listaAnuncios.size() + " anuncios", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("FirestoreError", "Error al buscar anuncios", e));
        }
    }




}



