package com.example.aplicacion_sencilla_publicidad;

public class Anuncio {
    private String descripcion;
    private String telefono;
    private String localidad;
    private String imagenUrl;
    private String id;

    public Anuncio() {} // Requerido por Firestore

    public Anuncio(String descripcion, String telefono, String localidad, String imagenUrl) {
        this.descripcion = descripcion;
        this.telefono = telefono;
        this.localidad = localidad;
        this.imagenUrl = imagenUrl;
    }

    public String getDescripcion() { return descripcion; }
    public String getTelefono() { return telefono; }
    public String getLocalidad() { return localidad; }
    public String getImagenUrl() { return imagenUrl; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // interfaz para manejar clics:
    public interface OnAnuncioClickListener {
        void onAnuncioClick(Anuncio anuncio);
    }


}