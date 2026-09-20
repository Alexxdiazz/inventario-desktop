package com.ong.desktop;

import com.ong.desktop.vistas.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private BorderPane root;
    private VBox menuLateral;
    private Button botonActivo;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();

        // ===== Barra superior =====
        Label titulo = new Label("Sistema de Inventario - Mujeres Celebran la Vida");
        titulo.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 15px; -fx-background-color: #d63384; -fx-text-fill: white;");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);
        root.setTop(titulo);

        // ===== Menú lateral =====
        menuLateral = new VBox(5);
        menuLateral.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 15px; -fx-min-width: 200px; -fx-border-color: #dee2e6; -fx-border-width: 0 1px 0 0;");

        Label lblMenu = new Label("MÓDULOS");
        lblMenu.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6c757d; -fx-padding: 5px;");
        menuLateral.getChildren().add(lblMenu);

        Button btnDashboard = crearBotonMenu("Dashboard");
        Button btnArticulos = crearBotonMenu("Artículos");
        Button btnCategorias = crearBotonMenu("Categorías");
        Button btnUbicaciones = crearBotonMenu("Ubicaciones");
        Button btnUsuarios = crearBotonMenu("Usuarios");
        Button btnEntidades = crearBotonMenu("Entidades");
        Button btnDonaciones = crearBotonMenu("Donaciones");
        Button btnPrestamos = crearBotonMenu("Préstamos");
        Button btnEntregas = crearBotonMenu("Entregas");
        Button btnHistorial = crearBotonMenu("Historial");

        btnDashboard.setOnAction(e -> mostrarPanel(btnDashboard, new PanelDashboard().construir()));
        btnArticulos.setOnAction(e -> mostrarPanel(btnArticulos, new PanelArticulos(stage).construir()));
        btnCategorias.setOnAction(e -> mostrarPanel(btnCategorias, new PanelCategorias(stage).construir()));
        btnUbicaciones.setOnAction(e -> mostrarPanel(btnUbicaciones, new PanelUbicaciones(stage).construir()));
        btnUsuarios.setOnAction(e -> mostrarPanel(btnUsuarios, new PanelUsuarios(stage).construir()));
        btnEntidades.setOnAction(e -> mostrarPanel(btnEntidades, new PanelEntidades(stage).construir()));
        btnDonaciones.setOnAction(e -> mostrarPanel(btnDonaciones, new PanelDonaciones(stage).construir()));
        btnPrestamos.setOnAction(e -> mostrarPanel(btnPrestamos, new PanelPrestamos(stage).construir()));
        btnEntregas.setOnAction(e -> mostrarPanel(btnEntregas, new PanelEntregas(stage).construir()));
        btnHistorial.setOnAction(e -> mostrarPanel(btnHistorial, new PanelHistorial(stage).construir()));

        menuLateral.getChildren().addAll(
                btnDashboard, btnArticulos, btnCategorias, btnUbicaciones, btnUsuarios, btnEntidades, btnDonaciones,
                btnPrestamos,
                btnEntregas, btnHistorial);

        root.setLeft(menuLateral);

        // ===== Mostrar la primera pantalla por defecto =====
        mostrarPanel(btnDashboard, new PanelDashboard().construir());
        // ===== Escena =====
        Scene scene = new Scene(root, 1200, 700);
        stage.setTitle("Inventario - ONG");
        stage.setScene(scene);
        stage.show();
    }

    private Button crearBotonMenu(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(
                "-fx-background-color: transparent; -fx-padding: 10px 15px; -fx-font-size: 14px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> {
            if (btn != botonActivo) {
                btn.setStyle(
                        "-fx-background-color: #e9ecef; -fx-padding: 10px 15px; -fx-font-size: 14px; -fx-cursor: hand;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != botonActivo) {
                btn.setStyle(
                        "-fx-background-color: transparent; -fx-padding: 10px 15px; -fx-font-size: 14px; -fx-cursor: hand;");
            }
        });
        return btn;
    }

    private void mostrarPanel(Button boton, javafx.scene.layout.VBox panel) {
        // Resetear estilo del botón anterior
        if (botonActivo != null) {
            botonActivo.setStyle(
                    "-fx-background-color: transparent; -fx-padding: 10px 15px; -fx-font-size: 14px; -fx-cursor: hand;");
        }
        // Marcar nuevo botón activo
        botonActivo = boton;
        boton.setStyle(
                "-fx-background-color: #d63384; -fx-text-fill: white; -fx-padding: 10px 15px; -fx-font-size: 14px; -fx-cursor: hand;");
        // Cambiar el panel
        root.setCenter(panel);
    }

    public static void main(String[] args) {
        launch(args);
    }
}