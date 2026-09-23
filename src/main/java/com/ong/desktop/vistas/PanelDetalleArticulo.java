package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Articulo;
import com.ong.desktop.servicios.ApiServicio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;

public class PanelDetalleArticulo {

    public interface AlVolver {
        void volver();
    }

    private final javafx.stage.Stage owner;
    private final Articulo articulo;
    private final String rol;
    private final AlVolver alVolver;
    private final ApiServicio api = new ApiServicio();

    public PanelDetalleArticulo(javafx.stage.Stage owner, Articulo articulo, String rol, AlVolver alVolver) {
        this.owner = owner;
        this.articulo = articulo;
        this.rol = rol;
        this.alVolver = alVolver;
    }

    public VBox construir() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        // ===== Header =====
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("↩ Volver");
        btnVolver.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #d63384;" +
                "-fx-border-color: #d63384; -fx-border-width: 1px;" +
                "-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand;"
        );
        btnVolver.setOnAction(e -> alVolver.volver());

        Label lblTitulo = new Label("Detalle del Artículo");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblTitulo.setStyle("-fx-text-fill: #212529;");

        header.getChildren().addAll(btnVolver, lblTitulo);

        HBox tarjeta = new HBox(25);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);"
        );
        tarjeta.setAlignment(Pos.CENTER_LEFT);

        // Imagen
        StackPane imagenPane = new StackPane();
        imagenPane.setPrefSize(200, 200);
        imagenPane.setMinSize(200, 200);
        imagenPane.setMaxSize(200, 200);
        imagenPane.setStyle(
                "-fx-background-color: #f8c8e0;" +
                "-fx-background-radius: 12px;"
        );

        ImageView iv = new ImageView();
        iv.setFitWidth(180);
        iv.setFitHeight(180);
        iv.setPreserveRatio(true);

        boolean imagenCargada = false;
        if (articulo.getRutaFoto() != null && !articulo.getRutaFoto().isEmpty()) {
            try {
                File f = new File(articulo.getRutaFoto());
                if (f.exists()) {
                    iv.setImage(new Image(f.toURI().toString()));
                    imagenCargada = true;
                }
            } catch (Exception ex) {
            }
        }

        if (imagenCargada) {
            imagenPane.getChildren().add(iv);
        } else {
            Label noImage = new Label("📦");
            noImage.setStyle("-fx-font-size: 80px;");
            imagenPane.getChildren().add(noImage);
        }

        // Info del artículo
        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label lblNombre = new Label(articulo.getNombre() != null ? articulo.getNombre() : "(Sin nombre)");
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblNombre.setStyle("-fx-text-fill: #d63384;");
        lblNombre.setWrapText(true);

        Label lblCodigo = new Label("📦 Código: " + (articulo.getCodigoInventario() != null ? articulo.getCodigoInventario() : "—"));
        lblCodigo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        Label lblEstado = new Label("✅ Estado: " + (articulo.getEstadoActual() != null ? articulo.getEstadoActual() : "—"));
        lblEstado.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        Label lblConservacion = new Label("🔧 Conservación: " + (articulo.getEstadoConservacion() != null ? articulo.getEstadoConservacion() : "—"));
        lblConservacion.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        Label lblCantidad = new Label("🔢 Cantidad: " + (articulo.getCantidad() != null ? articulo.getCantidad() : 0));
        lblCantidad.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        info.getChildren().addAll(lblNombre, lblCodigo, lblEstado, lblConservacion, lblCantidad);

        tarjeta.getChildren().addAll(imagenPane, info);

        // ===== Información detallada =====
        Label lblInfoTitulo = new Label("INFORMACIÓN SOBRE EL PRODUCTO");
        lblInfoTitulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6c757d;");

        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(20);
        gridInfo.setVgap(10);
        gridInfo.setPadding(new Insets(15));
        gridInfo.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 3);"
        );

        gridInfo.add(crearLabel("Código:"), 0, 0);
        gridInfo.add(crearValor(articulo.getCodigoInventario()), 1, 0);
        gridInfo.add(crearLabel("Color:"), 2, 0);
        gridInfo.add(crearValor(articulo.getColor()), 3, 0);
        gridInfo.add(crearLabel("Tamaño:"), 4, 0);
        gridInfo.add(crearValor(articulo.getTamano()), 5, 0);

        gridInfo.add(crearLabel("Marca/Modelo:"), 0, 1);
        gridInfo.add(crearValor(articulo.getMarcaModelo()), 1, 1);
        gridInfo.add(crearLabel("Nº Serie:"), 2, 1);
        gridInfo.add(crearValor(articulo.getNumeroSerie()), 3, 1);
        gridInfo.add(crearLabel("Procedencia:"), 4, 1);
        gridInfo.add(crearValor(articulo.getProcedencia()), 5, 1);

        gridInfo.add(crearLabel("Observaciones:"), 0, 2);
        Label obs = crearValor(articulo.getObservaciones());
        gridInfo.add(obs, 1, 2, 5, 1);

        Label lblAcciones = new Label("¿Qué deseas hacer con este producto?");
        lblAcciones.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        Button btnPrestamo = crearBotonAccion("🔄 Préstamo", "#17a2b8");
        Button btnEntrega = crearBotonAccion("📤 Entrega Definitiva", "#d63384");
        Button btnHistorial = crearBotonAccion("📜 Ver Historial", "#6f42c1");
        Button btnEditar = crearBotonAccion("✏ Editar Información", "#28a745");
        Button btnEliminar = crearBotonAccion("🗑 Eliminar Registro", "#dc3545");

        if ("CONSULTA".equals(rol)) {
            btnPrestamo.setVisible(false);
            btnPrestamo.setManaged(false);
            btnEntrega.setVisible(false);
            btnEntrega.setManaged(false);
            btnEditar.setVisible(false);
            btnEditar.setManaged(false);
            btnEliminar.setVisible(false);
            btnEliminar.setManaged(false);
        }

        btnPrestamo.setOnAction(e -> FormularioPrestamo.mostrar(owner, null, prestamo -> {
            prestamo.setArticulo(articulo);
            try {
                api.crearPrestamo(prestamo);
                mostrarAlerta("Préstamo creado exitosamente");
            } catch (Exception ex) {
                mostrarAlerta("Error al crear préstamo: " + ex.getMessage());
            }
        }));

        btnEntrega.setOnAction(e -> FormularioEntrega.mostrar(owner, null, entrega -> {
            entrega.setArticulo(articulo);
            try {
                api.crearEntrega(entrega);
                mostrarAlerta("Entrega creada exitosamente");
            } catch (Exception ex) {
                mostrarAlerta("Error al crear entrega: " + ex.getMessage());
            }
        }));

        btnHistorial.setOnAction(e -> {
            mostrarAlerta("En desarrollo: ver historial del artículo " + articulo.getIdArticulo());
        });

        btnEditar.setOnAction(e -> FormularioArticulo.mostrar(owner, articulo, articuloEditado -> {
            try {
                api.actualizarArticulo(articuloEditado.getIdArticulo(), articuloEditado);
                mostrarAlerta("Artículo actualizado");
            } catch (Exception ex) {
                mostrarAlerta("Error al actualizar: " + ex.getMessage());
            }
        }));

        btnEliminar.setOnAction(e -> {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setHeaderText("¿Eliminar este artículo?");
            confirmacion.setContentText(articulo.getNombre());
            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    try {
                        api.borrarArticulo(articulo.getIdArticulo());
                        mostrarAlerta("Artículo eliminado");
                        alVolver.volver();
                    } catch (Exception ex) {
                        mostrarAlerta("Error al eliminar: " + ex.getMessage());
                    }
                }
            });
        });

        VBox acciones = new VBox(10);
        acciones.setPadding(new Insets(15));
        acciones.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 3);"
        );
        acciones.getChildren().addAll(lblAcciones, btnPrestamo, btnEntrega, btnHistorial, btnEditar, btnEliminar);

        // ===== Scroll =====
        VBox contenido = new VBox(20, tarjeta, lblInfoTitulo, gridInfo, acciones);
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fdf6fa; -fx-background: #fdf6fa;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(header, scroll);
        return panel;
    }

    private Label crearLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");
        return lbl;
    }

    private Label crearValor(String valor) {
        Label lbl = new Label(valor != null && !valor.isEmpty() ? valor : "—");
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #212529;");
        return lbl;
    }

    private Button crearBotonAccion(String texto, String color) {
        Button btn = new Button(texto);
        btn.setPrefWidth(250);
        btn.setPrefHeight(40);
        btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white;" +
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-background-radius: 8px; -fx-cursor: hand;" +
                "-fx-alignment: center-left; -fx-padding: 8px 15px;"
        );
        return btn;
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}