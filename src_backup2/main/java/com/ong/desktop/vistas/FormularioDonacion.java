package com.ong.desktop.vistas;

import com.ong.desktop.modelos.DonacionRecepcion;
import com.ong.desktop.modelos.Entidad;
import com.ong.desktop.modelos.Usuario;
import com.ong.desktop.servicios.ApiServicio;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormularioDonacion {

    public interface AlGuardar {
        void guardar(DonacionRecepcion donacion);
    }

    public static void mostrar(Stage padre, DonacionRecepcion donacionExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(donacionExistente == null ? "Nueva Donación" : "Editar Donación");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // ComboBox de donantes (entidades)
        ComboBox<Entidad> cmbDonante = new ComboBox<>();
        // ComboBox de responsables (usuarios)
        ComboBox<Usuario> cmbResponsable = new ComboBox<>();
        TextField txtObservaciones = new TextField();

        // Cargar los datos de la API
        ApiServicio api = new ApiServicio();
        try {
            cmbDonante.getItems().addAll(api.listarEntidades());
            cmbResponsable.getItems().addAll(api.listarUsuarios());
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("Error al cargar datos");
            alerta.setContentText("No se pudieron cargar entidades o usuarios:\n" + e.getMessage());
            alerta.showAndWait();
        }

        // Si estamos editando, precargamos los datos
        if (donacionExistente != null) {
            txtObservaciones.setText(donacionExistente.getObservaciones());
            // Buscar el donante en el ComboBox por ID
            if (donacionExistente.getDonante() != null) {
                for (Entidad e : cmbDonante.getItems()) {
                    if (e.getIdEntidad().equals(donacionExistente.getDonante().getIdEntidad())) {
                        cmbDonante.setValue(e);
                        break;
                    }
                }
            }
            // Buscar el responsable en el ComboBox por ID
            if (donacionExistente.getResponsableRecepcion() != null) {
                for (Usuario u : cmbResponsable.getItems()) {
                    if (u.getIdUsuario().equals(donacionExistente.getResponsableRecepcion().getIdUsuario())) {
                        cmbResponsable.setValue(u);
                        break;
                    }
                }
            }
        }

        grid.add(new Label("Donante:"), 0, 0);
        grid.add(cmbDonante, 1, 0);
        grid.add(new Label("Responsable:"), 0, 1);
        grid.add(cmbResponsable, 1, 1);
        grid.add(new Label("Observaciones:"), 0, 2);
        grid.add(txtObservaciones, 1, 2);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                if (cmbDonante.getValue() == null) {
                    throw new Exception("Debes seleccionar un donante");
                }
                if (cmbResponsable.getValue() == null) {
                    throw new Exception("Debes seleccionar un responsable");
                }
                DonacionRecepcion d = new DonacionRecepcion();
                d.setDonante(cmbDonante.getValue());
                d.setResponsableRecepcion(cmbResponsable.getValue());
                d.setObservaciones(txtObservaciones.getText());
                if (donacionExistente != null) {
                    d.setIdDonacion(donacionExistente.getIdDonacion());
                }
                callback.guardar(d);
                ventana.close();
            } catch (Exception ex) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setHeaderText("Datos inválidos");
                alerta.setContentText(ex.getMessage());
                alerta.showAndWait();
            }
        });

        grid.add(btnGuardar, 0, 3);
        grid.add(btnCancelar, 1, 3);

        Scene scene = new Scene(grid, 500, 250);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}