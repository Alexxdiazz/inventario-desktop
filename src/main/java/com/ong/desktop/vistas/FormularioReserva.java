package com.ong.desktop.vistas;

import com.ong.desktop.modelos.*;
import com.ong.desktop.servicios.ApiServicio;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

public class FormularioReserva {

    public interface AlGuardar {
        void guardar(Reserva reserva);
    }

    public static void mostrar(Stage padre, Reserva reservaExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(reservaExistente == null ? "Nueva Reserva" : "Editar Reserva");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Articulo> cmbArticulo = new ComboBox<>();
        ComboBox<Entidad> cmbReceptor = new ComboBox<>();
        TextField txtCantidad = new TextField("1");
        ComboBox<Usuario> cmbResponsable = new ComboBox<>();
        DatePicker dpFechaPrevista = new DatePicker(LocalDate.now().plusDays(7));
        ComboBox<String> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll("ACTIVA", "ENTREGADA", "CANCELADA");
        cmbEstado.setValue("ACTIVA");
        TextField txtObservaciones = new TextField();

        ApiServicio api = new ApiServicio();
        try {
            cmbArticulo.getItems().addAll(api.listarArticulos());
            cmbReceptor.getItems().addAll(api.listarEntidades());
            cmbResponsable.getItems().addAll(api.listarUsuarios());
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("Error al cargar datos");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }

        if (reservaExistente != null) {
            txtCantidad.setText(String.valueOf(reservaExistente.getCantidad()));
            txtObservaciones.setText(reservaExistente.getObservaciones());
            if (reservaExistente.getFechaPrevistaEntrega() != null) {
                dpFechaPrevista.setValue(reservaExistente.getFechaPrevistaEntrega());
            }
            if (reservaExistente.getEstado() != null) {
                cmbEstado.setValue(reservaExistente.getEstado());
            }
            if (reservaExistente.getArticulo() != null) {
                for (Articulo a : cmbArticulo.getItems()) {
                    if (a.getIdArticulo().equals(reservaExistente.getArticulo().getIdArticulo())) {
                        cmbArticulo.setValue(a);
                        break;
                    }
                }
            }
            if (reservaExistente.getReceptor() != null) {
                for (Entidad e : cmbReceptor.getItems()) {
                    if (e.getIdEntidad().equals(reservaExistente.getReceptor().getIdEntidad())) {
                        cmbReceptor.setValue(e);
                        break;
                    }
                }
            }
            if (reservaExistente.getResponsable() != null) {
                for (Usuario u : cmbResponsable.getItems()) {
                    if (u.getIdUsuario().equals(reservaExistente.getResponsable().getIdUsuario())) {
                        cmbResponsable.setValue(u);
                        break;
                    }
                }
            }
        }

        grid.add(new Label("Artículo:"), 0, 0);
        grid.add(cmbArticulo, 1, 0);
        grid.add(new Label("Receptor:"), 0, 1);
        grid.add(cmbReceptor, 1, 1);
        grid.add(new Label("Cantidad:"), 0, 2);
        grid.add(txtCantidad, 1, 2);
        grid.add(new Label("Responsable:"), 0, 3);
        grid.add(cmbResponsable, 1, 3);
        grid.add(new Label("Fecha prevista entrega:"), 0, 4);
        grid.add(dpFechaPrevista, 1, 4);
        grid.add(new Label("Estado:"), 0, 5);
        grid.add(cmbEstado, 1, 5);
        grid.add(new Label("Observaciones:"), 0, 6);
        grid.add(txtObservaciones, 1, 6);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                if (cmbArticulo.getValue() == null) throw new Exception("Seleccioná un artículo");
                if (cmbReceptor.getValue() == null) throw new Exception("Seleccioná un receptor");
                if (cmbResponsable.getValue() == null) throw new Exception("Seleccioná un responsable");
                if (dpFechaPrevista.getValue() == null) throw new Exception("Seleccioná la fecha prevista");

                Reserva r = new Reserva();
                r.setArticulo(cmbArticulo.getValue());
                r.setReceptor(cmbReceptor.getValue());
                r.setCantidad(Integer.parseInt(txtCantidad.getText()));
                r.setResponsable(cmbResponsable.getValue());
                r.setFechaPrevistaEntrega(dpFechaPrevista.getValue());
                r.setEstado(cmbEstado.getValue());
                r.setObservaciones(txtObservaciones.getText());
                if (reservaExistente != null) {
                    r.setIdReserva(reservaExistente.getIdReserva());
                }
                callback.guardar(r);
                ventana.close();
            } catch (Exception ex) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setHeaderText("Datos inválidos");
                alerta.setContentText(ex.getMessage());
                alerta.showAndWait();
            }
        });

        grid.add(btnGuardar, 0, 7);
        grid.add(btnCancelar, 1, 7);

        Scene scene = new Scene(grid, 500, 400);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}