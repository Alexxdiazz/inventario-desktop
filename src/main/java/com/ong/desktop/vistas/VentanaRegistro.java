package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Usuario;
import com.ong.desktop.servicios.ApiServicio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VentanaRegistro {

    public static void mostrar(Stage padre) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle("Registrarse");

        // === Fondo con degradado ===
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8c8e0, #d63384);");

        // === Tarjeta ===
        VBox tarjeta = new VBox(12);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(30));
        tarjeta.setMaxWidth(400);
        tarjeta.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 20px;");
        DropShadow sombra = new DropShadow();
        sombra.setRadius(20);
        sombra.setColor(Color.rgb(0, 0, 0, 0.2));
        tarjeta.setEffect(sombra);

        // === Avatar ===
        StackPane avatar = new StackPane();
        Circle circulo = new Circle(40);
        circulo.setFill(Color.rgb(255, 255, 255, 0.6));
        Text icono = new Text("👤");
        icono.setFont(Font.font(40));
        avatar.getChildren().addAll(circulo, icono);

        // === Título ===
        Label titulo = new Label("Crear Cuenta");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        titulo.setStyle("-fx-text-fill: #d63384;");

        // === Campos ===
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("👤  Nombre");
        txtNombre.setPrefHeight(38);
        txtNombre.setStyle("-fx-background-radius: 20px; -fx-padding: 0 15px;");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("📧  Email");
        txtEmail.setPrefHeight(38);
        txtEmail.setStyle("-fx-background-radius: 20px; -fx-padding: 0 15px;");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("🔒  Contraseña");
        txtPassword.setPrefHeight(38);
        txtPassword.setStyle("-fx-background-radius: 20px; -fx-padding: 0 15px;");

        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.getItems().addAll("CONSULTA", "USUARIO_OPERATIVO", "RESPONSABLE_INVENTARIO");
        cmbRol.setValue("CONSULTA");
        cmbRol.setPrefHeight(38);
        cmbRol.setMaxWidth(Double.MAX_VALUE);

        // === Botones ===
        Button btnGuardar = new Button("Registrarme");
        btnGuardar.setPrefWidth(280);
        btnGuardar.setPrefHeight(40);
        btnGuardar.setStyle(
                "-fx-background-color: #d63384; -fx-text-fill: white;" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20px; -fx-cursor: hand;"
        );

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(280);
        btnCancelar.setPrefHeight(38);
        btnCancelar.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #6c757d;" +
                "-fx-border-color: #6c757d; -fx-border-width: 1px;" +
                "-fx-background-radius: 20px; -fx-border-radius: 20px; -fx-cursor: hand;"
        );
        btnCancelar.setOnAction(e -> ventana.close());

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
        lblError.setVisible(false);

        // === Lógica ===
        ApiServicio api = new ApiServicio();
        btnGuardar.setOnAction(e -> {
            lblError.setVisible(false);

            if (txtNombre.getText().isEmpty() || txtEmail.getText().isEmpty() || txtPassword.getText().isEmpty()) {
                lblError.setText("Completá todos los campos");
                lblError.setVisible(true);
                return;
            }

            try {
                Usuario u = new Usuario();
                u.setNombre(txtNombre.getText());
                u.setEmail(txtEmail.getText());
                u.setPasswordHash(txtPassword.getText());
                u.setRol(cmbRol.getValue());
                u.setActivo(true);

                api.registrar(u);

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("¡Registro exitoso!");
                ok.setContentText("Ya podés iniciar sesión con tu email y contraseña.");
                ok.showAndWait();

                ventana.close();
            } catch (Exception ex) {
                lblError.setText("❌ " + ex.getMessage());
                lblError.setVisible(true);
            }
        });

        tarjeta.getChildren().addAll(
                avatar, titulo,
                txtNombre, txtEmail, txtPassword,
                new Label("Rol:"), cmbRol,
                btnGuardar, btnCancelar,
                lblError
        );

        root.getChildren().add(tarjeta);

        Scene scene = new Scene(root, 500, 650);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}