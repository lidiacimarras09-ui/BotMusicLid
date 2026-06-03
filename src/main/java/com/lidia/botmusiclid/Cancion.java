package com.lidia.botmusiclid;

public class Cancion {

    private final String nombre;
    private final String url;

    public Cancion(String nombre, String url) {
        this.nombre = nombre;
        this.url = url;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return nombre;
    }
}