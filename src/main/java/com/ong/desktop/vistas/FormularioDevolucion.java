package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Prestamo;
import com.ong.desktop.modelos.Usuario;
import com.ong.desktop.servicios.ApiServicio;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class FormularioDevolucion {

    public interface AlGuardar {
        void guardar(Prestamo prestamo);
    }

    public static void mostrar(Stage padre, Prestamo prestamo, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle("Registrar Devolución");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label lblArticulo = new Label(prestamo.getArticulo() != null ? prestamo.getArticulo().getNombre() : "—");
        Label lblReceptor = new Label(prestamo.getReceptor() != null ? prestamo.getReceptor().getNombreCompleto() : "—");
        Label lblCantidad = new Label(String.valueOf(prestamo.getCantidad()));
        Label lblFechaPrestamo = new Label(prestamo.getFechaPrestamo() != null ? prestamo.getFechaPrestamo().toString() : "—");
        Label lblFechaPrevista = new Label(prestamo.getFechaPrevistaDevolucion() != null ? prestamo.getFechaPrevistaDevolucion().toString() : "—");

        ComboBox<String> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll("NUEVO", "BUENO", "REGULAR", "DETERIORADO");
        cmbEstado.setValue("BUENO");

        ComboBox<Usuario> cmbResponsable = new ComboBox<>();
        TextField txtObservaciones = new TextField();

        ApiServicio api = new ApiServicio();
        try {
            cmbResponsable.getItems().addAll(api.listarUsuarios());
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("Error al cargar usuarios");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }

        grid.add(new Label("Artículo:"), 0, 0);
        grid.add(lblArticulo, 1, 0);
        grid.add(new Label("Receptor:"), 0, 1);
        grid.add(lblReceptor, 1, 1);
        grid.add(new Label("Cantidad:"), 0, 2);
        grid.add(lblCantidad, 1, 2);
        grid.add(new Label("Fecha del préstamo:"), 0, 3);
        grid.add(lblFechaPrestamo, 1, 3);
        grid.add(new Label("Fecha prevista:"), 0, 4);
        grid.add(lblFechaPrevista, 1, 4);

        Separator sep = new Separator();
        grid.add(sep, 0, 5, 2, 1);

        grid.add(new Label("Estado de devolución:"), 0, 6);
        grid.add(cmbEstado, 1, 6);
        grid.add(new Label("Recibido por:"), 0, 7);
        grid.add(cmbResponsable, 1, 7);
        grid.add(new Label("Observaciones:"), 0, 8);
        grid.add(txtObservaciones, 1, 8);

        Button btnGuardar = new Button("Registrar Devolución");
        btnGuardar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 8px 20px; -fx-cursor: hand;");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
    try {
        if (cmbResponsable.getValue() == null) {
            throw new Exception("Seleccioná quién recibe la devolución");
        }

        // ===== 1. Actualizar el préstamo =====
        prestamo.setFechaRealDevolucion(LocalDateTime.now());
        prestamo.setEstadoDevolucion(cmbEstado.getValue());
        prestamo.setResponsableRecepcionDevolucion(cmbResponsable.getValue());
        if (txtObservaciones.getText() != null && !txtObservaciones.getText().isEmpty()) {
            String obs = prestamo.getObservaciones() != null ? prestamo.getObservaciones() : "";
            prestamo.setObservaciones(obs + "\n[DEVOLUCIÓN] " + txtObservaciones.getText());
        }

        // ===== 2. Actualizar el estado del artículo =====
        if (prestamo.getArticulo() != null) {
            prestamo.getArticulo().setEstadoActual("DISPONIBLE");
        }

        callback.guardar(prestamo);
        ventana.close();
    } catch (Exception ex) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setHeaderText("Datos inválidos");
        alerta.setContentText(ex.getMessage());
        alerta.showAndWait();
    }
});

        grid.add(btnGuardar, 0, 9);
        grid.add(btnCancelar, 1, 9);

        Scene scene = new Scene(grid, 500, 450);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}