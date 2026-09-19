package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Categoria;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormularioCategoria {

    public interface AlGuardar {
        void guardar(Categoria categoria);
    }

    public static void mostrar(Stage padre, Categoria categoriaExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(categoriaExistente == null ? "Nueva Categoria" : "Editar Categoria");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtNombre = new TextField();
        TextField txtDescripcion = new TextField();

        if (categoriaExistente != null) {
            txtNombre.setText(categoriaExistente.getNombre());
            txtDescripcion.setText(categoriaExistente.getDescripcion());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripcion:"), 0, 1);
        grid.add(txtDescripcion, 1, 1);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                Categoria c = new Categoria();
                c.setNombre(txtNombre.getText());
                c.setDescripcion(txtDescripcion.getText());
                if (categoriaExistente != null) {
                    c.setIdCategoria(categoriaExistente.getIdCategoria());
                }
                callback.guardar(c);
                ventana.close();
            } catch (Exception ex) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setHeaderText("Datos invalidos");
                alerta.setContentText(ex.getMessage());
                alerta.showAndWait();
            }
        });

        grid.add(btnGuardar, 0, 2);
        grid.add(btnCancelar, 1, 2);

        Scene scene = new Scene(grid, 400, 200);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}
