package com.example.aplicacion_sencilla_publicidad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class AnuncioAdapterPerfil extends RecyclerView.Adapter<AnuncioAdapterPerfil.AnuncioViewHolder> {

    private Context context;
    private List<Anuncio> listaAnuncios;
    private OnAnuncioClickListener listener;
    private OnEliminarClickListener eliminarListener;

    // Callback para click general en el anuncio
    public interface OnAnuncioClickListener {
        void onAnuncioClick(Anuncio anuncio);
    }

    // Callback para click en el botón eliminar
    public interface OnEliminarClickListener {
        void onEliminarClick(Anuncio anuncio, int position);
    }

    public AnuncioAdapterPerfil(Context context, List<Anuncio> listaAnuncios,
                                OnAnuncioClickListener listener,
                                OnEliminarClickListener eliminarListener) {
        this.context = context;
        this.listaAnuncios = listaAnuncios;
        this.listener = listener;
        this.eliminarListener = eliminarListener;
    }

    @Override
    public AnuncioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio_perfil, parent, false);
        return new AnuncioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AnuncioViewHolder holder, int position) {
        Anuncio anuncio = listaAnuncios.get(position);

        holder.textLocalidad.setText(anuncio.getLocalidad());
        holder.textDescripcion.setText(anuncio.getDescripcion());
        holder.textTelefono.setText(anuncio.getTelefono());

        Glide.with(context)
                .load(anuncio.getImagenUrl())
                .placeholder(R.drawable.ic_menu)
                .into(holder.imageAnuncio);

        // Click en todo el item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnuncioClick(anuncio);
        });

        // Click en botón eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (eliminarListener != null) {
                eliminarListener.onEliminarClick(anuncio, position);
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
        ImageButton btnEliminar;

        public AnuncioViewHolder(View itemView) {
            super(itemView);
            imageAnuncio = itemView.findViewById(R.id.imageAnuncio);
            textLocalidad = itemView.findViewById(R.id.textLocalidad);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            textTelefono = itemView.findViewById(R.id.textTelefono);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    // Método para eliminar localmente un anuncio
    public void eliminarAnuncio(int position) {
        listaAnuncios.remove(position);
        notifyItemRemoved(position);
    }
}
