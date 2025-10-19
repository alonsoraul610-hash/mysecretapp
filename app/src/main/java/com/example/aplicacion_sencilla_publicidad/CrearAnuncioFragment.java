package com.example.aplicacion_sencilla_publicidad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CrearAnuncioFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri imageUri;
    private MaterialAutoCompleteTextView inputLocalidad;
    private TextInputEditText inputDescripcion, inputTelefono;
    private ImageView imagePreview;
    private RequestQueue requestQueue;
    private ArrayAdapter<String> adapter;

    public CrearAnuncioFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_anuncio, container, false);

        inputLocalidad = view.findViewById(R.id.inputLocalidad);
        inputDescripcion = view.findViewById(R.id.inputDescripcion);
        inputTelefono = view.findViewById(R.id.inputTelefono);
        imagePreview = view.findViewById(R.id.imagePreview);
        Button buttonSeleccionarImagen = view.findViewById(R.id.buttonSeleccionarImagen);
        Button buttonPublicar = view.findViewById(R.id.buttonPublicar);

        requestQueue = Volley.newRequestQueue(requireContext());
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        inputLocalidad.setAdapter(adapter);

        // Búsqueda de localidades
        inputLocalidad.addTextChangedListener(new android.text.TextWatcher() {
            private android.os.Handler handler = new android.os.Handler();
            private Runnable searchRunnable;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
                if (s.length() >= 2) {
                    searchRunnable = () -> buscarLocalidades(s.toString());
                    handler.postDelayed(searchRunnable, 500);
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        buttonSeleccionarImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        buttonPublicar.setOnClickListener(v -> {
            String localidad = inputLocalidad.getText().toString().trim();
            String telefono = inputTelefono.getText().toString().trim();

            if (localidad.isEmpty()) {
                inputLocalidad.setError("La localidad es obligatoria");
                return;
            }
            if (telefono.isEmpty()) {
                inputTelefono.setError("El teléfono es obligatorio");
                return;
            }

            Toast.makeText(requireContext(), "✅ Anuncio publicado correctamente", Toast.LENGTH_LONG).show();
        });

        return view;
    }

    private void buscarLocalidades(String texto) {
        String url = String.format(
                "https://nominatim.openstreetmap.org/search?q=%s&format=jsonv2&limit=10&countrycodes=ES",
                Uri.encode(texto)
        );

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<String> resultados = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            resultados.add(obj.getString("display_name"));
                        }
                        adapter.clear();
                        adapter.addAll(resultados);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).centerCrop().into(imagePreview);
        }
    }
}
