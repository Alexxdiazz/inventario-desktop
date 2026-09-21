package com.ong.desktop.servicios;

import com.ong.desktop.modelos.Articulo;

import java.util.ArrayList;
import java.util.List;

public class Notificaciones {

    public static class Alerta {
        public String icono;
        public String titulo;
        public String detalle;
        public String color;

        public Alerta(String icono, String titulo, String detalle, String color) {
            this.icono = icono;
            this.titulo = titulo;
            this.detalle = detalle;
            this.color = color;
        }
    }

    public static List<Alerta> calcular(List<Articulo> articulos) {
        List<Alerta> alertas = new ArrayList<>();

        for (Articulo a : articulos) {
            // Stock bajo
            if (a.getStockMinimo() != null && a.getCantidad() != null
                    && a.getStockMinimo() > 0
                    && a.getCantidad() <= a.getStockMinimo()) {
                alertas.add(new Alerta(
                        "⚠",
                        "Stock bajo: " + a.getNombre(),
                        "Quedan " + a.getCantidad() + " (mínimo: " + a.getStockMinimo() + ")",
                        "#dc3545"
                ));
            }
        }

        return alertas;
    }
}