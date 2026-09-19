package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Usuario;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormularioUsuario {

    public interface AlGuardar {
        void guardar(Usuario usuario);
    }

    public static void mostrar(Stage padre, Usuario usuarioExistente, AlGuardar callback) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(padre);
        ventana.setTitle(usuarioExistente == null ? "Nuevo Usuario" : "Editar Usuario");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtNombre = new TextField();
        TextField txtEmail = new TextField();
        PasswordField txtPassword = new PasswordField();
        ComboBox<String> cmbRol = new ComboBox<>();
        cmbRol.getItems().addAll("ADMINISTRADOR", "RESPONSABLE_INVENTARIO", "USUARIO_OPERATIVO", "CONSULTA");
        CheckBox chkActivo = new CheckBox("Activo");
        chkActivo.setSelected(true);

        if (usuarioExistente != null) {
            txtNombre.setText(usuarioExistente.getNombre());
            txtEmail.setText(usuarioExistente.getEmail());
            txtPassword.setText(usuarioExistente.getPasswordHash());
            String rol = usuarioExistente.getRol();
            cmbRol.setValue(rol != null ? rol : "CONSULTA");
            chkActivo.setSelected(usuarioExistente.getActivo() != null ? usuarioExistente.getActivo() : true);
        } else {
            cmbRol.setValue("CONSULTA");
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2);
        grid.add(txtPassword, 1, 2);
        grid.add(new Label("Rol:"), 0, 3);
        grid.add(cmbRol, 1, 3);
        grid.add(chkActivo, 1, 4);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> ventana.close());

        btnGuardar.setOnAction(e -> {
            try {
                Usuario u = new Usuario();
                u.setNombre(txtNombre.getText());
                u.setEmail(txtEmail.getText());
                u.setPasswordHash(txtPassword.getText());
                u.setRol(cmbRol.getValue());
                u.setActivo(chkActivo.isSelected());
                if (usuarioExistente != null) {
                    u.setIdUsuario(usuarioExistente.getIdUsuario());
                }
                callback.guardar(u);
                ventana.close();
            } catch (Exception ex) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setHeaderText("Datos inválidos");
                alerta.setContentText(ex.getMessage());
                alerta.showAndWait();
            }
        });

        grid.add(btnGuardar, 0, 5);
        grid.add(btnCancelar, 1, 5);

        Scene scene = new Scene(grid, 450, 300);
        ventana.setScene(scene);
        ventana.showAndWait();
    }
}