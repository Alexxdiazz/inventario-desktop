package com.ong.desktop.vistas;

import com.ong.desktop.modelos.*;
import com.ong.desktop.servicios.ApiServicio;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormularioHistorial {

    public interface AlGuardar {
        void guardar(HistorialMovimiento movimiento);
    }

    public static void mostrar(Stage padre, HistorialMovimiento historialExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(historialExistente == null ? "Nuevo Movimiento" : "Editar Movimiento");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Articulo> cmbArticulo = new ComboBox<>();
        ComboBox<Usuario> cmbUsuario = new ComboBox<>();
        TextField txtTipoOperacion = new TextField();
        ComboBox<String> cmbEstadoAnterior = new ComboBox<>();
        cmbEstadoAnterior.getItems().addAll("DISPONIBLE", "RESERVADO", "PRESTADO", "EN_REPARACION", "ENTREGADO", "BAJA");
        ComboBox<String> cmbEstadoNuevo = new ComboBox<>();
        cmbEstadoNuevo.getItems().addAll("DISPONIBLE", "RESERVADO", "PRESTADO", "EN_REPARACION", "ENTREGADO", "BAJA");
        TextField txtDescripcion = new TextField();

        ApiServicio api = new ApiServicio();
        try {
            cmbArticulo.getItems().addAll(api.listarArticulos());
            cmbUsuario.getItems().addAll(api.listarUsuarios());
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("Error al cargar datos");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }

        if (historialExistente != null) {
            txtTipoOperacion.setText(historialExistente.getTipoOperacion());
            txtDescripcion.setText(historialExistente.getDescripcionMovimiento());
            cmbEstadoAnterior.setValue(historialExistente.getEstadoAnterior());
            cmbEstadoNuevo.setValue(historialExistente.getEstadoNuevo());
            if (historialExistente.getArticulo() != null) {
                for (Articulo a : cmbArticulo.getItems()) {
                    if (a.getIdArticulo().equals(historialExistente.getArticulo().getIdArticulo())) {
                        cmbArticulo.setValue(a);
                        break;
                    }
                }
            }
            if (historialExistente.getUsuario() != null) {
                for (Usuario u : cmbUsuario.getItems()) {
                    if (u.getIdUsuario().equals(historialExistente.getUsuario().getIdUsuario())) {
                        cmbUsuario.setValue(u);
                        break;
                    }
                }
            }
        }

        grid.add(new Label("Articulo:"), 0, 0);
        grid.add(cmbArticulo, 1, 0);
        grid.add(new Label("Usuario:"), 0, 1);
        grid.add(cmbUsuario, 1, 1);
        grid.add(new Label("Tipo operacion:"), 0, 2);
        grid.add(txtTipoOperacion, 1, 2);
        grid.add(new Label("Estado anterior:"), 0, 3);
        grid.add(cmbEstadoAnterior, 1, 3);
        grid.add(new Label("Estado nuevo:"), 0, 4);
        grid.add(cmbEstadoNuevo, 1, 4);
        grid.add(new Label("Descripcion:"), 0, 5);
        grid.add(txtDescripcion, 1, 5);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                if (cmbArticulo.getValue() == null) throw new Exception("Selecciona un articulo");
                if (cmbUsuario.getValue() == null) throw new Exception("Selecciona un usuario");

                HistorialMovimiento h = new HistorialMovimiento();
                h.setArticulo(cmbArticulo.getValue());
                h.setUsuario(cmbUsuario.getValue());
                h.setTipoOperacion(txtTipoOperacion.getText());
                h.setEstadoAnterior(cmbEstadoAnterior.getValue());
                h.setEstadoNuevo(cmbEstadoNuevo.getValue());
                h.setDescripcionMovimiento(txtDescripcion.getText());
                if (historialExistente != null) {
                    h.setIdMovimiento(historialExistente.getIdMovimiento());
                }
                callback.guardar(h);
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