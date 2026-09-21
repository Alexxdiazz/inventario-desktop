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

public class VentanaRecuperar {

    public static void mostrar(Stage padre) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle("Recuperar Contraseña");

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8c8e0, #d63384);");

        VBox tarjeta = new VBox(12);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(30));
        tarjeta.setMaxWidth(400);
        tarjeta.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 20px;");
        DropShadow sombra = new DropShadow();
        sombra.setRadius(20);
        sombra.setColor(Color.rgb(0, 0, 0, 0.2));
        tarjeta.setEffect(sombra);

        StackPane avatar = new StackPane();
        Circle circulo = new Circle(40);
        circulo.setFill(Color.rgb(255, 255, 255, 0.6));
        Text icono = new Text("🔑");
        icono.setFont(Font.font(40));
        avatar.getChildren().addAll(circulo, icono);

        Label titulo = new Label("Recuperar Contraseña");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        titulo.setStyle("-fx-text-fill: #d63384;");

        Label info = new Label("Ingresá tu email y elegí una nueva contraseña");
        info.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        info.setWrapText(true);
        info.setMaxWidth(300);
        info.setAlignment(Pos.CENTER);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("📧  Email");
        txtEmail.setPrefHeight(38);
        txtEmail.setStyle("-fx-background-radius: 20px; -fx-padding: 0 15px;");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("🔒  Nueva contraseña");
        txtPassword.setPrefHeight(38);
        txtPassword.setStyle("-fx-background-radius: 20px; -fx-padding: 0 15px;");

        Button btnCambiar = new Button("Cambiar Contraseña");
        btnCambiar.setPrefWidth(280);
        btnCambiar.setPrefHeight(40);
        btnCambiar.setStyle(
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

        ApiServicio api = new ApiServicio();
        btnCambiar.setOnAction(e -> {
            lblError.setVisible(false);
            if (txtEmail.getText().isEmpty() || txtPassword.getText().isEmpty()) {
                lblError.setText("Completá todos los campos");
                lblError.setVisible(true);
                return;
            }

            try {
                Usuario u = api.buscarPorEmail(txtEmail.getText().trim());
                if (u == null) {
                    lblError.setText("No existe una cuenta con ese email");
                    lblError.setVisible(true);
                    return;
                }

                api.cambiarPassword(u.getIdUsuario(), txtPassword.getText());

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Contraseña actualizada");
                ok.setContentText("Ya podés iniciar sesión con la nueva contraseña.");
                ok.showAndWait();

                ventana.close();
            } catch (Exception ex) {
                lblError.setText("❌ " + ex.getMessage());
                lblError.setVisible(true);
            }
        });

        tarjeta.getChildren().addAll(
                avatar, titulo, info,
                txtEmail, txtPassword,
                btnCambiar, btnCancelar,
                lblError
        );

        root.getChildren().add(tarjeta);

        Scene scene = new Scene(root, 500, 620);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}