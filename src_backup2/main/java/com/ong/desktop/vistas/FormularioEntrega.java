package com.ong.desktop.vistas;

import com.ong.desktop.modelos.*;
import com.ong.desktop.servicios.ApiServicio;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormularioEntrega {

    public interface AlGuardar {
        void guardar(EntregaDefinitiva entrega);
    }

    public static void mostrar(Stage padre, EntregaDefinitiva entregaExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(entregaExistente == null ? "Nueva Entrega" : "Editar Entrega");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Entidad> cmbReceptor = new ComboBox<>();
        ComboBox<Articulo> cmbArticulo = new ComboBox<>();
        TextField txtCantidad = new TextField("1");
        ComboBox<Usuario> cmbResponsable = new ComboBox<>();
        TextField txtMotivo = new TextField();
        TextField txtObservaciones = new TextField();

        ApiServicio api = new ApiServicio();
        try {
            cmbReceptor.getItems().addAll(api.listarEntidades());
            cmbArticulo.getItems().addAll(api.listarArticulos());
            cmbResponsable.getItems().addAll(api.listarUsuarios());
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("Error al cargar datos");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }

        if (entregaExistente != null) {
            txtCantidad.setText(String.valueOf(entregaExistente.getCantidad()));
            txtMotivo.setText(entregaExistente.getMotivoCampana());
            txtObservaciones.setText(entregaExistente.getObservaciones());
            if (entregaExistente.getReceptor() != null) {
                for (Entidad e : cmbReceptor.getItems()) {
                    if (e.getIdEntidad().equals(entregaExistente.getReceptor().getIdEntidad())) {
                        cmbReceptor.setValue(e);
                        break;
                    }
                }
            }
            if (entregaExistente.getArticulo() != null) {
                for (Articulo a : cmbArticulo.getItems()) {
                    if (a.getIdArticulo().equals(entregaExistente.getArticulo().getIdArticulo())) {
                        cmbArticulo.setValue(a);
                        break;
                    }
                }
            }
            if (entregaExistente.getResponsable() != null) {
                for (Usuario u : cmbResponsable.getItems()) {
                    if (u.getIdUsuario().equals(entregaExistente.getResponsable().getIdUsuario())) {
                        cmbResponsable.setValue(u);
                        break;
                    }
                }
            }
        }

        grid.add(new Label("Receptor:"), 0, 0);
        grid.add(cmbReceptor, 1, 0);
        grid.add(new Label("Articulo:"), 0, 1);
        grid.add(cmbArticulo, 1, 1);
        grid.add(new Label("Cantidad:"), 0, 2);
        grid.add(txtCantidad, 1, 2);
        grid.add(new Label("Responsable:"), 0, 3);
        grid.add(cmbResponsable, 1, 3);
        grid.add(new Label("Motivo / Campana:"), 0, 4);
        grid.add(txtMotivo, 1, 4);
        grid.add(new Label("Observaciones:"), 0, 5);
        grid.add(txtObservaciones, 1, 5);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                if (cmbReceptor.getValue() == null) throw new Exception("Selecciona un receptor");
                if (cmbArticulo.getValue() == null) throw new Exception("Selecciona un articulo");
                if (cmbResponsable.getValue() == null) throw new Exception("Selecciona un responsable");

                EntregaDefinitiva en = new EntregaDefinitiva();
                en.setReceptor(cmbReceptor.getValue());
                en.setArticulo(cmbArticulo.getValue());
                en.setCantidad(Integer.parseInt(txtCantidad.getText()));
                en.setResponsable(cmbResponsable.getValue());
                en.setMotivoCampana(txtMotivo.getText());
                en.setObservaciones(txtObservaciones.getText());
                if (entregaExistente != null) {
                    en.setIdEntrega(entregaExistente.getIdEntrega());
                }
                callback.guardar(en);
                ventana.close();
            } catch (Exception ex) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setHeaderText("Datos invalidos");
                alerta.setContentText(ex.getMessage());
                alerta.showAndWait();
            }
        });

        grid.add(btnGuardar, 0, 6);
        grid.add(btnCancelar, 1, 6);

        Scene scene = new Scene(grid, 500, 350);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}