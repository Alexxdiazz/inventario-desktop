package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Articulo;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormularioArticulo {

    public interface AlGuardar {
        void guardar(Articulo articulo);
    }

    public static void mostrar(Stage padre, Articulo articuloExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(articuloExistente == null ? "Nuevo Artículo" : "Editar Artículo");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtCodigo = new TextField();
        TextField txtNombre = new TextField();
        TextField txtDescripcion = new TextField();
        TextField txtCantidad = new TextField();
        TextField txtStockMinimo = new TextField();
        ComboBox<String> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll("DISPONIBLE", "RESERVADO", "PRESTADO", "EN_REPARACION", "ENTREGADO", "BAJA");
        ComboBox<String> cmbConservacion = new ComboBox<>();
        cmbConservacion.getItems().addAll("NUEVO", "BUENO", "REGULAR", "DETERIORADO");

        System.out.println("ABriendo formulario");
        System.out.println("Articulo existente: "
                + (articuloExistente != null ? "Si (id=" + articuloExistente.getIdArticulo() + ")" : "No(nuevo)"));
        if (articuloExistente != null) {
            System.out.println("Conservacion recibida: [" + articuloExistente.getEstadoConservacion() + "]");
            System.out.println("Estado recibido:[" + articuloExistente.getEstadoActual() + "]");

        }

        if (articuloExistente != null) {
            txtCodigo.setText(articuloExistente.getCodigoInventario());
            txtNombre.setText(articuloExistente.getNombre());
            txtDescripcion.setText(articuloExistente.getDescripcion());
            txtCantidad.setText(String.valueOf(articuloExistente.getCantidad()));
            txtStockMinimo.setText(String.valueOf(articuloExistente.getStockMinimo() != null ? articuloExistente.getStockMinimo() : 0));


            String estado = articuloExistente.getEstadoActual();
            cmbEstado.setValue(estado != null ? estado : "DISPONIBLE");

            String conservacion = articuloExistente.getEstadoConservacion();
            cmbConservacion.setValue(conservacion != null ? conservacion : "BUENO");
        } else {
            cmbEstado.setValue("DISPONIBLE");
            cmbConservacion.setValue("BUENO");
        }

        grid.add(new Label("Código:"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(txtNombre, 1, 1);
        grid.add(new Label("Descripción:"), 0, 2);
        grid.add(txtDescripcion, 1, 2);
        grid.add(new Label("Cantidad:"), 0, 3);
        grid.add(txtCantidad, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(cmbEstado, 1, 4);
        grid.add(new Label("Conservación:"), 0, 5);
        grid.add(cmbConservacion, 1, 5);
        grid.add(new Label("Stock mínimo:"), 0, 6);
        grid.add(txtStockMinimo, 1, 6);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                Articulo a = new Articulo();
                a.setCodigoInventario(txtCodigo.getText());
                a.setNombre(txtNombre.getText());
                a.setDescripcion(txtDescripcion.getText());
                a.setCantidad(Integer.parseInt(txtCantidad.getText()));
                a.setEstadoActual(cmbEstado.getValue());
                a.setEstadoConservacion(cmbConservacion.getValue());

                 if (!txtStockMinimo.getText().isEmpty()) {
            a.setStockMinimo(Integer.parseInt(txtStockMinimo.getText()));
        } else {
            a.setStockMinimo(0);
        }



                if (articuloExistente != null) {
                    a.setIdArticulo(articuloExistente.getIdArticulo());
                    a.setIdCategoria(articuloExistente.getIdCategoria());
                    a.setIdUbicacion(articuloExistente.getIdUbicacion());
                } else {
                    a.setIdCategoria(1);
                    a.setIdUbicacion(1);
                }
                callback.guardar(a);
                ventana.close();
            } catch (Exception ex) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Error");
                alerta.setHeaderText("Datos inválidos");
                alerta.setContentText(ex.getMessage());
                alerta.showAndWait();
            }
        });

        grid.add(btnGuardar, 0, 7);
        grid.add(btnCancelar, 1, 7);

        Scene scene = new Scene(grid, 400, 350);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}