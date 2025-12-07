package com.mi.appanuncios;

public class Anuncio {
    private String descripcion;
    private String telefono;
    private String localidad;
    private String imagenUri;
    private String id;


    public Anuncio() {} // Requerido por Firestore

    public Anuncio(String descripcion, String telefono, String localidad, String imagenUri) {
        this.descripcion = descripcion;
        this.telefono = telefono;
        this.localidad = localidad;
        this.imagenUri = imagenUri;
    }





    public String getDescripcion() { return descripcion; }
    public String getTelefono() { return telefono; }
    public String getLocalidad() { return localidad; }
    public String getImagenUri() { return imagenUri; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }




}