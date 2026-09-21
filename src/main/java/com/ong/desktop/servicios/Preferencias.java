package com.ong.desktop.servicios;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class Preferencias {

    private static final String ARCHIVO = System.getProperty("user.home") + File.separator + ".inventario_config";
    private static Properties props = null;

    private static void cargar() {
        if (props != null) return;
        props = new Properties();
        try {
            if (Files.exists(Paths.get(ARCHIVO))) {
                try (InputStream in = new FileInputStream(ARCHIVO)) {
                    props.load(in);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void guardar(String clave, String valor) {
        cargar();
        if (valor == null || valor.isEmpty()) {
            props.remove(clave);
        } else {
            props.setProperty(clave, valor);
        }
        try (OutputStream out = new FileOutputStream(ARCHIVO)) {
            props.store(out, "Preferencias de Inventario - ONG");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String leer(String clave) {
        cargar();
        return props.getProperty(clave, "");
    }
}