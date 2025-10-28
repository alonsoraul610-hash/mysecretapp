package com.example.aplicacion_sencilla_publicidad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditarAnuncioActivity extends AppCompatActivity {

    private EditText editDescripcion, editTelefono;
    private ImageView imageEditarAnuncio;
    private Button btnGuardarCambios, btnCambiarImagen;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri imagenUriSeleccionada = null;
    private String anuncioId;
    private String imagenActualUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_anuncio);

        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // 🔹 Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔹 Referencias UI
        editDescripcion = findViewById(R.id.editDescripcion);
        editTelefono = findViewById(R.id.editTelefono);
        imageEditarAnuncio = findViewById(R.id.imageEditarAnuncio);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnCambiarImagen = findViewById(R.id.btnCambiarImagen);

        // 🔹 Configurar Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 🔹 Obtener datos del Intent
        anuncioId = getIntent().getStringExtra("id");
        String descripcion = getIntent().getStringExtra("descripcion");
        String telefono = getIntent().getStringExtra("telefono");
        imagenActualUrl = getIntent().getStringExtra("imagenUrl");

        // 🔹 Mostrar datos actuales
        editDescripcion.setText(descripcion);
        editTelefono.setText(telefono);
        Glide.with(this)
                .load(imagenActualUrl)
                .placeholder(R.drawable.ic_persona)
                .into(imageEditarAnuncio);

        // 🔹 Selector de imagen
        ActivityResultLauncher<Intent> launcherSeleccionImagen =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                                imagenUriSeleccionada = result.getData().getData();
                                imageEditarAnuncio.setImageURI(imagenUriSeleccionada);
                            }
                        });

        btnCambiarImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            launcherSeleccionImagen.launch(intent);
        });

        // 🔹 Guardar cambios
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {
        String descripcion = editDescripcion.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();

        if (descripcion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagenUriSeleccionada != null) {
            subirImagenYActualizar(descripcion, telefono);
        } else {
            actualizarAnuncio(descripcion, telefono, imagenActualUrl);
        }
    }

    private void subirImagenYActualizar(String descripcion, String telefono) {
        String userId = auth.getCurrentUser().getUid();
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("anuncios/" + userId + "/" + UUID.randomUUID().toString() + ".jpg");

        ref.putFile(imagenUriSeleccionada)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String nuevaImagenUrl = uri.toString();
                    actualizarAnuncio(descripcion, telefono, nuevaImagenUrl);
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
    }

    private void actualizarAnuncio(String descripcion, String telefono, String imagenUrl) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("anuncios")
                .document(anuncioId)
                .update(
                        "descripcion", descripcion,
                        "telefono", telefono,
                        "imagenUrl", imagenUrl
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Anuncio actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // 🔹 Cierra y vuelve atrás
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar el anuncio", Toast.LENGTH_SHORT).show());
    }



}
