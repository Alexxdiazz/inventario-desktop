package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Prestamo;
import com.ong.desktop.modelos.Usuario;
import com.ong.desktop.servicios.ApiServicio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #fdf6fa;");

        // ===== Header =====
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #f0d0e0 transparent; -fx-border-width: 0 0 1px 0;");

        Label iconoBack = new Label("↩");
        iconoBack.setStyle("-fx-font-size: 20px; -fx-cursor: hand;");
        iconoBack.setOnMouseClicked(e -> ventana.close());

        Label titulo = new Label("Registrar Devolución");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #212529;");

        header.getChildren().addAll(iconoBack, titulo);

        // ===== Contenido =====
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(20));

        // ===== Tarjeta del artículo =====
        VBox tarjetaArticulo = new VBox(5);
        tarjetaArticulo.setPadding(new Insets(15));
        tarjetaArticulo.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: #f8c8e0;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 12px;"
        );

        String nombreArt = prestamo.getArticulo() != null ? prestamo.getArticulo().getNombre() : "—";
        String codArt = prestamo.getArticulo() != null && prestamo.getArticulo().getCodigoInventario() != null
                ? prestamo.getArticulo().getCodigoInventario() : "";
        String nombreReceptor = prestamo.getReceptor() != null ? prestamo.getReceptor().getNombreCompleto() : "—";

        Label lblArt = new Label("📦 " + nombreArt);
        lblArt.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblArt.setStyle("-fx-text-fill: #d63384;");

        Label lblInfo = new Label("Código: " + codArt + "  |  Receptor: " + nombreReceptor);
        lblInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        tarjetaArticulo.getChildren().addAll(lblArt, lblInfo);

        // ===== Campos =====
        ApiServicio api = new ApiServicio();
        ComboBox<Usuario> cmbResponsable = new ComboBox<>();
        try {
            cmbResponsable.getItems().addAll(api.listarUsuarios());
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("Error al cargar usuarios");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }

        ComboBox<String> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll("NUEVO", "BUENO", "REGULAR", "DETERIORADO");
        cmbEstado.setValue("BUENO");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        Label lblResponsable = new Label("👤 Recibido por *");
        lblResponsable.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        cmbResponsable.setMaxWidth(Double.MAX_VALUE);
        cmbResponsable.setPrefHeight(38);

        Label lblFecha = new Label("📅 Fecha de devolución");
        lblFecha.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        TextField txtFecha = new TextField(java.time.LocalDate.now().toString());
        txtFecha.setEditable(false);
        txtFecha.setPrefHeight(38);

        Label lblEstado = new Label("📋 Estado del artículo *");
        lblEstado.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        cmbEstado.setMaxWidth(Double.MAX_VALUE);
        cmbEstado.setPrefHeight(38);

        Label lblObs = new Label("💬 Observaciones");
        lblObs.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        TextArea txtObservaciones = new TextArea();
        txtObservaciones.setPrefRowCount(3);
        txtObservaciones.setPrefHeight(70);
        txtObservaciones.setWrapText(true);

        grid.add(lblResponsable, 0, 0);
        grid.add(cmbResponsable, 0, 1);
        grid.add(lblFecha, 1, 0);
        grid.add(txtFecha, 1, 1);

        grid.add(lblEstado, 0, 2);
        grid.add(cmbEstado, 0, 3);

        grid.add(lblObs, 0, 4, 2, 1);
        grid.add(txtObservaciones, 0, 5, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        contenido.getChildren().addAll(tarjetaArticulo, grid);

        // ===== Botones =====
        Button btnGuardar = new Button("💾  Registrar Devolución");
        btnGuardar.setPrefHeight(45);
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white;" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-background-radius: 10px; -fx-cursor: hand;"
        );

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefHeight(45);
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #6c757d;" +
                "-fx-border-color: #6c757d; -fx-border-width: 1px;" +
                "-fx-background-radius: 10px; -fx-border-radius: 10px; -fx-cursor: hand;"
        );
        btnCancelar.setOnAction(e -> ventana.close());

        HBox botones = new HBox(10, btnCancelar, btnGuardar);
        HBox.setHgrow(btnGuardar, Priority.ALWAYS);
        botones.setPadding(new Insets(20));
        botones.setStyle("-fx-background-color: white; -fx-border-color: #f0d0e0 transparent transparent transparent; -fx-border-width: 1px 0 0 0;");

        btnGuardar.setOnAction(e -> {
            try {
                if (cmbResponsable.getValue() == null) throw new Exception("Seleccioná quién recibe la devolución");

                prestamo.setFechaRealDevolucion(LocalDateTime.now());
                prestamo.setEstadoDevolucion(cmbEstado.getValue());
                prestamo.setResponsableRecepcionDevolucion(cmbResponsable.getValue());
                if (txtObservaciones.getText() != null && !txtObservaciones.getText().isEmpty()) {
                    String obs = prestamo.getObservaciones() != null ? prestamo.getObservaciones() : "";
                    prestamo.setObservaciones(obs + "\n[DEVOLUCIÓN] " + txtObservaciones.getText());
                }

                // Cambiar estado del artículo a DISPONIBLE
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

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fdf6fa; -fx-background: #fdf6fa;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll, botones);

        Scene scene = new Scene(root, 700, 550);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}