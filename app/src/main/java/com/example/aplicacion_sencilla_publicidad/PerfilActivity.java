package com.example.aplicacion_sencilla_publicidad;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.navigation.NavigationView;
import android.view.View;


import android.content.Intent;

//para cerrar sesion
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class PerfilActivity extends AppCompatActivity  {

    private AnuncioAdapterPerfil adapter; // <-- Variable de clase


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_perfil);






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
        getSupportActionBar().setTitle("Mi perfil");



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
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Configuración seleccionada", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_help) {
                Toast.makeText(this, "Ayuda seleccionada", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_create) {
                Toast.makeText(this, "Crear anuncio", Toast.LENGTH_SHORT).show();
                getSupportActionBar().setTitle("Crear anuncio");
                startActivity(new Intent(this, CrearAnuncioActivity.class));


            }

            drawerLayout.closeDrawers(); // Cierra el menú tras pulsar
            return true;
        });

        // FINAL -------- BANER SUPERIOR Y MENU ---------------------//

        //-----------botones de cerrar sesion y eliminar cuenta ------------//

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        Button btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        btnCerrarSesion.setOnClickListener(v -> {

            // 1️⃣ Cerrar sesión de Firebase
            FirebaseAuth.getInstance().signOut();

            // 2️⃣ Intentar cerrar sesión de Google (si está logueado con Google)
            try {
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(
                        PerfilActivity.this,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .build()
                );

                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    // Después de cerrar sesión, ir al LoginActivity
                    Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                // Si hubo algún error con Google, seguimos con Firebase
                Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

        });


        btnEliminarCuenta.setOnClickListener(v -> {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Confirmar antes de eliminar
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Eliminar cuenta")
                    .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
                    .setPositiveButton("Sí", (dialog, which) -> {

                        // Reautenticación necesaria
                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                        if (account == null) {
                            Toast.makeText(this, "Error: no hay sesión de Google activa", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                            if (reauthTask.isSuccessful()) {

                                String uid = user.getUid();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                // 1️⃣ Borrar documento en Firestore
                                db.collection("users").document(uid)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // 2️⃣ Borrar cuenta de Firebase Auth
                                            user.delete().addOnCompleteListener(deleteTask -> {
                                                if (deleteTask.isSuccessful()) {
                                                    Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show();

                                                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                                    prefs.edit().putBoolean("cuenta_borrada", true).apply();


                                                    Intent intent = new Intent(this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(this, "Error al eliminar la cuenta: " + deleteTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    Log.e("EliminarCuenta", deleteTask.getException().getMessage());
                                                }
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error al borrar datos de Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            Log.e("EliminarCuenta", e.getMessage());
                                        });

                            } else {
                                Toast.makeText(this, "Reautenticación fallida: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("EliminarCuenta", "Reautenticación fallida: " + reauthTask.getException().getMessage());
                            }
                        });

                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

        });

        //FINAL------botones de cerrar sesion y eliminar cuenta ------------//



        // para listar los anuncios creados por el usuario

        // RecyclerView para los anuncios
        RecyclerView recyclerView = findViewById(R.id.recyclerAnunciosUsuario);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Anuncio> listaAnuncios = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

// Configuramos el adaptador (sin funcionalidad de eliminar todavía)
        adapter = new AnuncioAdapterPerfil(
                this,
                listaAnuncios,
                anuncio -> {
                    // Abrir DetalleAnuncioActivity
                    Intent intent = new Intent(PerfilActivity.this, DetalleAnuncioActivity.class);
                    intent.putExtra("descripcion", anuncio.getDescripcion());
                    intent.putExtra("localidad", anuncio.getLocalidad());
                    intent.putExtra("telefono", anuncio.getTelefono());
                    intent.putExtra("imagenUrl", anuncio.getImagenUrl());
                    startActivity(intent);
                },
                (anuncio, position) -> {


                    // Confirmar antes de eliminar
                    new androidx.appcompat.app.AlertDialog.Builder(PerfilActivity.this)
                            .setTitle("Eliminar anuncio")
                            .setMessage("¿Estás seguro de que quieres eliminar este anuncio?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    //FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    // 🔹 El ID del documento debe estar en el objeto Anuncio
                                    String anuncioId = anuncio.getId(); // Asegúrate de tener un campo 'id' en Anuncio
                                    db.collection("users")
                                            .document(user.getUid())
                                            .collection("anuncios")
                                            .document(anuncioId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Borrar localmente y actualizar RecyclerView
                                                listaAnuncios.remove(position);
                                                adapter.notifyItemRemoved(position);
                                                Toast.makeText(PerfilActivity.this, "Anuncio eliminado", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(PerfilActivity.this, "Error al eliminar anuncio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                },
        (anuncio, position) -> { // EDITAR
            Intent intent = new Intent(PerfilActivity.this, EditarAnuncioActivity.class);
            intent.putExtra("id", anuncio.getId());
            intent.putExtra("descripcion", anuncio.getDescripcion());
            intent.putExtra("localidad", anuncio.getLocalidad());
            intent.putExtra("telefono", anuncio.getTelefono());
            startActivity(intent);
        }
        );


        recyclerView.setAdapter(adapter);

// Cargar los anuncios de Firestore
        db.collection("users")
                .document(uid)
                .collection("anuncios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaAnuncios.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Anuncio anuncio = doc.toObject(Anuncio.class);
                        if (anuncio != null) {
                            anuncio.setId(doc.getId()); // 🔹 Guardamos el ID del documento
                            listaAnuncios.add(anuncio);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (listaAnuncios.isEmpty()) {
                        Toast.makeText(this, "Todavía no has publicado ningún anuncio.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar anuncios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PerfilActivity", "Error Firestore: ", e);
                });












    }




    //para actualizar la lista cuando se edita un anuncio
    @Override
    protected void onResume() {
        super.onResume();
        recargarAnuncios();
    }

    private void recargarAnuncios() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Anuncio> listaAnuncios = new ArrayList<>();

        db.collection("users")
                .document(user.getUid())
                .collection("anuncios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaAnuncios.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Anuncio anuncio = doc.toObject(Anuncio.class);
                        if (anuncio != null) {
                            anuncio.setId(doc.getId());
                            listaAnuncios.add(anuncio);
                        }
                    }

                    // Actualizar los datos del adaptador
                    adapter.actualizarLista(listaAnuncios);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar anuncios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Glide.with(this).load(fotoPerfil).placeholder(R.drawable.ic_menu).into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.ic_menu);
            }
        }
    }


}



