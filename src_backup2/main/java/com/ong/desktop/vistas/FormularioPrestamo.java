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

public class FormularioPrestamo {

    public interface AlGuardar {
        void guardar(Prestamo prestamo);
    }

    public static void mostrar(Stage padre, Prestamo prestamoExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(prestamoExistente == null ? "Nuevo Préstamo" : "Editar Préstamo");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Entidad> cmbReceptor = new ComboBox<>();
        ComboBox<Articulo> cmbArticulo = new ComboBox<>();
        ComboBox<Usuario> cmbResponsable = new ComboBox<>();
        TextField txtCantidad = new TextField("1");
        DatePicker dpFechaPrevista = new DatePicker(LocalDate.now().plusDays(7));
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

        if (prestamoExistente != null) {
            txtCantidad.setText(String.valueOf(prestamoExistente.getCantidad()));
            txtObservaciones.setText(prestamoExistente.getObservaciones());
            if (prestamoExistente.getFechaPrevistaDevolucion() != null) {
                dpFechaPrevista.setValue(prestamoExistente.getFechaPrevistaDevolucion());
            }
            // Seleccionar items por ID
            if (prestamoExistente.getReceptor() != null) {
                for (Entidad e : cmbReceptor.getItems()) {
                    if (e.getIdEntidad().equals(prestamoExistente.getReceptor().getIdEntidad())) {
                        cmbReceptor.setValue(e);
                        break;
                    }
                }
            }
            if (prestamoExistente.getArticulo() != null) {
                for (Articulo a : cmbArticulo.getItems()) {
                    if (a.getIdArticulo().equals(prestamoExistente.getArticulo().getIdArticulo())) {
                        cmbArticulo.setValue(a);
                        break;
                    }
                }
            }
            if (prestamoExistente.getResponsable() != null) {
                for (Usuario u : cmbResponsable.getItems()) {
                    if (u.getIdUsuario().equals(prestamoExistente.getResponsable().getIdUsuario())) {
                        cmbResponsable.setValue(u);
                        break;
                    }
                }
            }
        }

        grid.add(new Label("Receptor:"), 0, 0);
        grid.add(cmbReceptor, 1, 0);
        grid.add(new Label("Artículo:"), 0, 1);
        grid.add(cmbArticulo, 1, 1);
        grid.add(new Label("Cantidad:"), 0, 2);
        grid.add(txtCantidad, 1, 2);
        grid.add(new Label("Responsable:"), 0, 3);
        grid.add(cmbResponsable, 1, 3);
        grid.add(new Label("Fecha prevista devolución:"), 0, 4);
        grid.add(dpFechaPrevista, 1, 4);
        grid.add(new Label("Observaciones:"), 0, 5);
        grid.add(txtObservaciones, 1, 5);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                if (cmbReceptor.getValue() == null) throw new Exception("Selecciona un receptor");
                if (cmbArticulo.getValue() == null) throw new Exception("Selecciona un artículo");
                if (cmbResponsable.getValue() == null) throw new Exception("Selecciona un responsable");
                if (dpFechaPrevista.getValue() == null) throw new Exception("Selecciona la fecha prevista");

                Prestamo p = new Prestamo();
                p.setReceptor(cmbReceptor.getValue());
                p.setArticulo(cmbArticulo.getValue());
                p.setCantidad(Integer.parseInt(txtCantidad.getText()));
                p.setResponsable(cmbResponsable.getValue());
                p.setFechaPrevistaDevolucion(dpFechaPrevista.getValue());
                p.setObservaciones(txtObservaciones.getText());
                if (prestamoExistente != null) {
                    p.setIdPrestamo(prestamoExistente.getIdPrestamo());
                }
                callback.guardar(p);
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

        Scene scene = new Scene(grid, 500, 350);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}