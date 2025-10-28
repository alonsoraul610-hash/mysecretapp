package com.example.aplicacion_sencilla_publicidad;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class AnuncioAdapter extends RecyclerView.Adapter<AnuncioAdapter.AnuncioViewHolder> {

    private Context context;
    private List<Anuncio> listaAnuncios;
    private OnAnuncioClickListener listener;

    public interface OnAnuncioClickListener {
        void onAnuncioClick(Anuncio anuncio);
    }

    public AnuncioAdapter(Context context, List<Anuncio> listaAnuncios, OnAnuncioClickListener listener) {
        this.context = context;
        this.listaAnuncios = listaAnuncios;
        this.listener = listener;
    }

    @Override
    public AnuncioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio, parent, false);
        return new AnuncioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AnuncioViewHolder holder, int position) {
        Anuncio anuncio = listaAnuncios.get(position);

        holder.textLocalidad.setText(anuncio.getLocalidad());
        holder.textDescripcion.setText(anuncio.getDescripcion());
        holder.textTelefono.setText("Tlf: " + anuncio.getTelefono());

        // ✅ Cargar imagen solo si existe
        String imagenUrl = anuncio.getImagenUri();

        Log.d("AnuncioAdapter", "Imagen URL para el anuncio '" + anuncio.getDescripcion() + "': " + imagenUrl);

        if (imagenUrl != null && !imagenUrl.trim().isEmpty() && !imagenUrl.equals("null")) {
            Glide.with(context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.ic_persona) // mientras carga
                    .error(R.drawable.ic_persona)       // si falla la carga
                    .into(holder.imageAnuncio);
        } else {
            holder.imageAnuncio.setImageResource(R.drawable.ic_persona);
        }

        // 🔹 Click listener
        holder.itemView.setOnClickListener(v -> {
            Log.d("AnuncioAdapter", "✅ Click en anuncio: " + anuncio.getDescripcion());
            if (listener != null) {
                listener.onAnuncioClick(anuncio);
            }
        });
    }


    @Override
    public int getItemCount() {
        return listaAnuncios.size();
    }

    public static class AnuncioViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAnuncio;
        TextView textLocalidad, textDescripcion, textTelefono;

        public AnuncioViewHolder(View itemView) {
            super(itemView);
            imageAnuncio = itemView.findViewById(R.id.imageAnuncio);
            textLocalidad = itemView.findViewById(R.id.textLocalidad);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            textTelefono = itemView.findViewById(R.id.textTelefono);
        }
    }
}
