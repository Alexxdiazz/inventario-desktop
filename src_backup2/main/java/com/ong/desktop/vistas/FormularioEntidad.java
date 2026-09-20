package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Entidad;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormularioEntidad {

    public interface AlGuardar {
        void guardar(Entidad entidad);
    }

    public static void mostrar(Stage padre, Entidad entidadExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(entidadExistente == null ? "Nueva Entidad" : "Editar Entidad");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("PERSONA", "FAMILIA", "INSTITUCION", "DONANTE");
        TextField txtNombre = new TextField();
        TextField txtDocumento = new TextField();
        TextField txtTelefono = new TextField();
        TextField txtEmail = new TextField();
        TextField txtDireccion = new TextField();

        if (entidadExistente != null) {
            cmbTipo.setValue(entidadExistente.getTipo());
            txtNombre.setText(entidadExistente.getNombreCompleto());
            txtDocumento.setText(entidadExistente.getDocumentoIdentidad());
            txtTelefono.setText(entidadExistente.getTelefono());
            txtEmail.setText(entidadExistente.getEmail());
            txtDireccion.setText(entidadExistente.getDireccion());
        } else {
            cmbTipo.setValue("PERSONA");
        }

        grid.add(new Label("Tipo:"), 0, 0);
        grid.add(cmbTipo, 1, 0);
        grid.add(new Label("Nombre completo:"), 0, 1);
        grid.add(txtNombre, 1, 1);
        grid.add(new Label("Documento:"), 0, 2);
        grid.add(txtDocumento, 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3);
        grid.add(txtTelefono, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(txtEmail, 1, 4);
        grid.add(new Label("Dirección:"), 0, 5);
        grid.add(txtDireccion, 1, 5);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                Entidad en = new Entidad();
                en.setTipo(cmbTipo.getValue());
                en.setNombreCompleto(txtNombre.getText());
                en.setDocumentoIdentidad(txtDocumento.getText());
                en.setTelefono(txtTelefono.getText());
                en.setEmail(txtEmail.getText());
                en.setDireccion(txtDireccion.getText());
                if (entidadExistente != null) {
                    en.setIdEntidad(entidadExistente.getIdEntidad());
                }
                callback.guardar(en);
                ventana.close();
            } catch (Exception ex) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setHeaderText("Datos inválidos");
                alerta.setContentText(ex.getMessage());
                alerta.showAndWait();
            }
        });

        grid.add(btnGuardar, 0, 6);
        grid.add(btnCancelar, 1, 6);

        Scene scene = new Scene(grid, 450, 350);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}