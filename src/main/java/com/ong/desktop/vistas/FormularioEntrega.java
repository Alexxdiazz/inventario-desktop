package com.ong.desktop.vistas;

import com.ong.desktop.modelos.*;
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

public class FormularioEntrega {

    public interface AlGuardar {
        void guardar(EntregaDefinitiva entrega);
    }

    public static void mostrar(Stage padre, EntregaDefinitiva entregaExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(entregaExistente == null ? "Nueva Entrega" : "Editar Entrega");

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

        Label titulo = new Label(entregaExistente == null ? "Nueva Entrega Definitiva" : "Editar Entrega");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #212529;");

        header.getChildren().addAll(iconoBack, titulo);

        // ===== Contenido =====
        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(20));

        // ===== Cargar datos =====
        ApiServicio api = new ApiServicio();
        ComboBox<Entidad> cmbReceptor = new ComboBox<>();
        ComboBox<Articulo> cmbArticulo = new ComboBox<>();
        ComboBox<Usuario> cmbResponsable = new ComboBox<>();

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

        Label lblArticulo = new Label("📦 Seleccioná un artículo");
        lblArticulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblArticulo.setStyle("-fx-text-fill: #d63384;");

        Label lblCategoria = new Label("");
        lblCategoria.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        cmbArticulo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                lblArticulo.setText("📦 " + newV.getNombre());
                if (newV.getIdCategoria() != null) {
                    try {
                        for (Categoria cat : api.listarCategorias()) {
                            if (cat.getIdCategoria().equals(newV.getIdCategoria())) {
                                lblCategoria.setText(cat.getNombre().toUpperCase());
                                break;
                            }
                        }
                    } catch (Exception ex) { }
                }
            }
        });

        tarjetaArticulo.getChildren().addAll(lblCategoria, lblArticulo);

        // ===== Grid de campos =====
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        Label lblArt = new Label("📦 Artículo *");
        lblArt.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        cmbArticulo.setMaxWidth(Double.MAX_VALUE);
        cmbArticulo.setPrefHeight(38);

        Label lblReceptor = new Label("👤 Receptor *");
        lblReceptor.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        cmbReceptor.setMaxWidth(Double.MAX_VALUE);
        cmbReceptor.setPrefHeight(38);

        Label lblCantidad = new Label("🔢 Cantidad *");
        lblCantidad.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        TextField txtCantidad = new TextField("1");
        txtCantidad.setPrefHeight(38);

        Label lblResponsable = new Label("👤 Responsable *");
        lblResponsable.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        cmbResponsable.setMaxWidth(Double.MAX_VALUE);
        cmbResponsable.setPrefHeight(38);

        Label lblMotivo = new Label("🎁 Motivo / Campaña");
        lblMotivo.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        TextField txtMotivo = new TextField();
        txtMotivo.setPrefHeight(38);

        Label lblObs = new Label("💬 Observaciones");
        lblObs.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        TextArea txtObservaciones = new TextArea();
        txtObservaciones.setPrefRowCount(3);
        txtObservaciones.setPrefHeight(70);
        txtObservaciones.setWrapText(true);

        grid.add(lblArt, 0, 0);
        grid.add(cmbArticulo, 0, 1);
        grid.add(lblResponsable, 1, 0);
        grid.add(cmbResponsable, 1, 1);

        grid.add(lblReceptor, 0, 2);
        grid.add(cmbReceptor, 0, 3);
        grid.add(lblMotivo, 1, 2);
        grid.add(txtMotivo, 1, 3);

        grid.add(lblCantidad, 0, 4);
        grid.add(txtCantidad, 0, 5);

        grid.add(lblObs, 0, 6, 2, 1);
        grid.add(txtObservaciones, 0, 7, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // ===== Precargar si es edición =====
        if (entregaExistente != null) {
            txtCantidad.setText(String.valueOf(entregaExistente.getCantidad()));
            txtMotivo.setText(entregaExistente.getMotivoCampana());
            txtObservaciones.setText(entregaExistente.getObservaciones());
            if (entregaExistente.getArticulo() != null) {
                for (Articulo a : cmbArticulo.getItems()) {
                    if (a.getIdArticulo().equals(entregaExistente.getArticulo().getIdArticulo())) {
                        cmbArticulo.setValue(a);
                        break;
                    }
                }
            }
            if (entregaExistente.getReceptor() != null) {
                for (Entidad e : cmbReceptor.getItems()) {
                    if (e.getIdEntidad().equals(entregaExistente.getReceptor().getIdEntidad())) {
                        cmbReceptor.setValue(e);
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

        contenido.getChildren().addAll(tarjetaArticulo, grid);

        // ===== Botones =====
        Button btnGuardar = new Button("💾  Guardar Entrega");
        btnGuardar.setPrefHeight(45);
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setStyle(
                "-fx-background-color: #d63384; -fx-text-fill: white;" +
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
                if (cmbArticulo.getValue() == null) throw new Exception("Seleccioná un artículo");
                if (cmbReceptor.getValue() == null) throw new Exception("Seleccioná un receptor");
                if (cmbResponsable.getValue() == null) throw new Exception("Seleccioná un responsable");

                EntregaDefinitiva en = new EntregaDefinitiva();
                en.setArticulo(cmbArticulo.getValue());
                en.setReceptor(cmbReceptor.getValue());
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

        Scene scene = new Scene(root, 700, 600);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}