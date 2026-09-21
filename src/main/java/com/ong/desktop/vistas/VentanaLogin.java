package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Usuario;
import com.ong.desktop.servicios.ApiServicio;
import com.ong.desktop.servicios.Preferencias;
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
import javafx.stage.Stage;

public class VentanaLogin {

    public interface AlIniciarSesion {
        void iniciar(Usuario usuario);
    }

    public static void mostrar(Stage stage, AlIniciarSesion callback) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8c8e0, #d63384);");

        VBox tarjeta = new VBox(15);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(40));
        tarjeta.setMaxWidth(400);
        tarjeta.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.85);" +
                "-fx-background-radius: 20px;"
        );
        DropShadow sombra = new DropShadow();
        sombra.setRadius(20);
        sombra.setColor(Color.rgb(0, 0, 0, 0.2));
        tarjeta.setEffect(sombra);

        // === Avatar circular ===
        StackPane avatar = new StackPane();
        Circle circulo = new Circle(45);
        circulo.setFill(Color.rgb(255, 255, 255, 0.6));
        Text icono = new Text("👤");
        icono.setFont(Font.font(45));
        avatar.getChildren().addAll(circulo, icono);

        Label titulo = new Label("Iniciar Sesión");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 20));
        titulo.setStyle("-fx-text-fill: #d63384;");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("📧  Email");
        txtEmail.setPrefHeight(40);
        txtEmail.setStyle("-fx-background-radius: 20px; -fx-padding: 0 15px; -fx-font-size: 13px;");

        String emailGuardado = Preferencias.leer("email");
        if (!emailGuardado.isEmpty()) {
            txtEmail.setText(emailGuardado);
        }

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("🔒  Contraseña");
        txtPassword.setPrefHeight(40);
        txtPassword.setStyle("-fx-background-radius: 20px; -fx-padding: 0 15px; -fx-font-size: 13px;");

        CheckBox chkRecordar = new CheckBox("Recordar");
        chkRecordar.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        if (!emailGuardado.isEmpty()) {
            chkRecordar.setSelected(true);
        }

        // === Link Olvidé contraseña ===
        Hyperlink linkOlvide = new Hyperlink("¿Olvidaste tu contraseña?");
        linkOlvide.setStyle("-fx-font-size: 11px; -fx-text-fill: #d63384; -fx-border-color: transparent;");
        linkOlvide.setOnAction(e -> VentanaRecuperar.mostrar(stage));

        HBox filaOpciones = new HBox(10, chkRecordar, new Region(), linkOlvide);
        HBox.setHgrow(filaOpciones.getChildren().get(1), Priority.ALWAYS);
        filaOpciones.setAlignment(Pos.CENTER_LEFT);
        filaOpciones.setMaxWidth(320);

        // === Botón Iniciar Sesión ===
        Button btnLogin = new Button("Iniciar Sesión");
        btnLogin.setPrefWidth(280);
        btnLogin.setPrefHeight(40);
        btnLogin.setStyle(
                "-fx-background-color: #d63384; -fx-text-fill: white;" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20px; -fx-cursor: hand;"
        );

        // === Botón Registrarse ===
        Button btnRegistrar = new Button("Registrarse");
        btnRegistrar.setPrefWidth(280);
        btnRegistrar.setPrefHeight(40);
        btnRegistrar.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #d63384;" +
                "-fx-border-color: #d63384; -fx-border-width: 2px;" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20px; -fx-border-radius: 20px; -fx-cursor: hand;"
        );
        btnRegistrar.setOnAction(e -> VentanaRegistro.mostrar(stage));

        // === Label de error ===
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
        lblError.setVisible(false);

        // === Lógica de login ===
        ApiServicio api = new ApiServicio();
        btnLogin.setOnAction(e -> {
            lblError.setVisible(false);
            String email = txtEmail.getText().trim();
            String pass = txtPassword.getText();

            if (email.isEmpty() || pass.isEmpty()) {
                lblError.setText("Completá email y contraseña");
                lblError.setVisible(true);
                return;
            }

            try {
                Usuario usuario = api.login(email, pass);

                // Guardar o borrar el email según "Recordar"
                if (chkRecordar.isSelected()) {
                    Preferencias.guardar("email", email);
                } else {
                    Preferencias.guardar("email", "");
                }

                callback.iniciar(usuario);
            } catch (Exception ex) {
                lblError.setText("❌ " + ex.getMessage());
                lblError.setVisible(true);
            }
        });

        // Permitir Enter
        txtPassword.setOnAction(e -> btnLogin.fire());

        tarjeta.getChildren().addAll(
                avatar, titulo,
                txtEmail, txtPassword,
                filaOpciones,
                btnLogin, btnRegistrar,
                lblError
        );

        root.getChildren().add(tarjeta);

        Scene scene = new Scene(root, 500, 650);
        stage.setScene(scene);
        stage.setTitle("Iniciar Sesión - ONG");
        stage.show();
    }
}