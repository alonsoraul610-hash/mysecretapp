package com.example.aplicacion_sencilla_publicidad;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.util.Log;
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
import java.util.List;
import android.widget.ArrayAdapter;

import android.text.Editable;
import android.text.TextWatcher;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import java.util.Map;

import android.content.Intent;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

// imports para los campos del cuestionario
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.provider.MediaStore;
import android.content.ActivityNotFoundException;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

//imports del firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



public class CrearAnuncioActivity extends AppCompatActivity {


    private ArrayAdapter<String> adapter;  //BUSCADOR
    private ArrayList<String> suggestions = new ArrayList<>();  //BUSCADOR
    private RequestQueue requestQueue;  //BUSCADOR
    private long lastTextEditTime = 0; //BUSCADOR
    private final android.os.Handler handler = new android.os.Handler(); //BUSCADOR
    private Runnable searchRunnable; //BUSCADOR

    private MaterialAutoCompleteTextView searchAutoComplete;  // BUSCADOR

    private EditText editDescripcion, editTelefono;
    private ImageView imagePreview;
    private Button btnSubirImagen, btnPublicar;
    private Uri imagenUri = null;

    //instancias firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // para la geolocalizacion
    private double latitudSeleccionada;
    private double longitudSeleccionada;

    // Lista de mapas que guardará el nombre corto y sus coordenadas
    private List<Map<String, String>> localidadesConCoordenadas = new ArrayList<>();


    // Launcher para seleccionar imagen
    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imagenUri = result.getData().getData();
                    // Cargar con Glide para manejar uri null o problemas de escalado
                    Glide.with(this)
                            .load(imagenUri)
                            .placeholder(R.drawable.ic_persona) // imagen por defecto
                            .into(imagePreview);
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crear_anuncio);

        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();



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
        getSupportActionBar().setTitle("Crear Anuncio");



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
        actualizarHeaderUsuario(navigationView);

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




        // Cuando el usuario selecciona una localidad del desplegable
        searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = (String) parent.getItemAtPosition(position);

            for (Map<String, String> loc : localidadesConCoordenadas) {
                if (loc.get("nombre").equals(seleccionado)) {
                    try {
                        latitudSeleccionada = Double.parseDouble(loc.get("lat"));
                        longitudSeleccionada = Double.parseDouble(loc.get("lon"));
                        Toast.makeText(this, "Coordenadas guardadas ✅ Lat: " + latitudSeleccionada + ", Lon: " + longitudSeleccionada, Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Error al convertir coordenadas", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        });




        // Campos del formulario
        editDescripcion = findViewById(R.id.descriptionEditText);
        editTelefono = findViewById(R.id.phoneEditText);
        imagePreview = findViewById(R.id.imagePreview);
        btnSubirImagen = findViewById(R.id.buttonUploadImage);
        btnPublicar = findViewById(R.id.buttonCreateAd);

// Acción de subir imagen
        btnSubirImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            try {
                seleccionarImagenLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No se pudo abrir la galería", Toast.LENGTH_SHORT).show();
            }
        });

// Acción de publicar
        btnPublicar.setOnClickListener(v -> {
            String descripcion = editDescripcion.getText().toString().trim();
            String telefono = editTelefono.getText().toString().trim();
            String localidad = searchAutoComplete.getText().toString().trim();




            if (descripcion.isEmpty()) {
                Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show();
                return;
            }

            if (telefono.isEmpty()) {
                Toast.makeText(this, "El número de teléfono es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            if (localidad.isEmpty()) {
                Toast.makeText(this, "La localidad es obligatoria", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Obtener UID del usuario autenticado
            String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

            if (uid == null) {
                Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Crear mapa con los datos del anuncio
            Map<String, Object> anuncio = new HashMap<>();
            anuncio.put("descripcion", descripcion);
            anuncio.put("telefono", telefono);
            anuncio.put("localidad", localidad);
            anuncio.put("latitud", latitudSeleccionada);    // <-- Añadido
            anuncio.put("longitud", longitudSeleccionada);  // <-- Añadido
            anuncio.put("timestamp", System.currentTimeMillis());
            if (imagenUri != null) {
                // Crear referencia Storage con nombre único
                //FirebaseStorage storage = FirebaseStorage.getInstance();
                FirebaseStorage storage = FirebaseStorage.getInstance("gs://my-secret-project-1420.firebasestorage.app");
                StorageReference storageRef = storage.getReference()
                        .child("anuncios/" + uid + "_" + System.currentTimeMillis() + ".jpg");

                // Subir la imagen
                storageRef.putFile(imagenUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Obtener URL pública
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imagenUrl = uri.toString();

                                // Añadir URL al anuncio
                                Map<String, Object> anuncioConImagen = new HashMap<>(anuncio);
                                anuncioConImagen.put("imagenUri", imagenUrl);

                                // Guardar en Firestore
                                db.collection("users")
                                        .document(uid)
                                        .collection("anuncios")
                                        .add(anuncioConImagen)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(this, "✅ Anuncio publicado correctamente", Toast.LENGTH_SHORT).show();
                                            limpiarCampos();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "❌ Error al guardar el anuncio: " + e.getMessage(), Toast.LENGTH_LONG).show());
                            });
                        })
                        .addOnFailureListener(e ->
                                Log.e("FirebaseStorage", "❌ Error al subir la imagen: " + e.getMessage(), e)
                        );

            } else {
                // Si no hay imagen, simplemente guarda el anuncio
                db.collection("users")
                        .document(uid)
                        .collection("anuncios")
                        .add(anuncio)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "✅ Anuncio publicado correctamente", Toast.LENGTH_SHORT).show();
                            limpiarCampos();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "❌ Error al guardar el anuncio: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }



        });


    }

    private void inicializarBusquedaLocalidades() {
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

                    localidadesConCoordenadas.clear(); // limpiar lista antes de agregar nuevos resultados
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            if (obj.has("lat") && obj.has("lon") && obj.has("display_name")) {
                                String displayName = obj.getString("display_name");
                                String lat = obj.getString("lat");
                                String lon = obj.getString("lon");

                                String corto = abreviarDisplayName(displayName, 3);
                                resultados.add(corto);

                                Map<String, String> map = new HashMap<>();
                                map.put("nombre", corto);
                                map.put("lat", lat);
                                map.put("lon", lon);
                                localidadesConCoordenadas.add(map);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Puedes ignorar o loguear la posición i
                        }
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

    private void limpiarCampos() {
        editDescripcion.setText("");
        editTelefono.setText("");
        searchAutoComplete.setText("");
        imagePreview.setImageResource(R.drawable.ic_persona);
        imagenUri = null;
    }


}



