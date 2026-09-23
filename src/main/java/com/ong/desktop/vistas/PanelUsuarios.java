package com.ong.desktop.vistas;
import com.ong.desktop.modelos.Usuario;
import com.ong.desktop.servicios.ApiServicio;
import com.ong.desktop.servicios.Exportador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PanelUsuarios {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Usuario> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;
    private final Label lblContador = new Label();

    private final ObservableList<Usuario> todosLosUsuarios = FXCollections.observableArrayList();
    private final TextField txtBuscar = new TextField();
    private final ComboBox<String> cmbRol = new ComboBox<>();

    public PanelUsuarios(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        HBox breadcrumb = new HBox(10);
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblRuta = new Label("🏠 / Usuarios");
        lblRuta.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d63384;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        breadcrumb.getChildren().addAll(lblRuta, spacer, lblContador);

        txtBuscar.setPromptText("🔍 Buscar por nombre o email...");
        txtBuscar.setPrefWidth(320);
        txtBuscar.setPrefHeight(35);

        cmbRol.getItems().addAll("Todos", "ADMINISTRADOR", "RESPONSABLE_INVENTARIO", "USUARIO_OPERATIVO", "CONSULTA");
        cmbRol.setValue("Todos");
        cmbRol.setPrefWidth(200);
        cmbRol.setPrefHeight(35);

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px;");
        btnLimpiar.setOnAction(e -> {
            txtBuscar.clear();
            cmbRol.setValue("Todos");
            aplicarFiltros();
        });

        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbRol.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());

        HBox barraFiltros = new HBox(10, txtBuscar, cmbRol, btnLimpiar);
        barraFiltros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button btnNuevo = new Button("➕ Nuevo");
        Button btnEditar = new Button("✏ Editar");
        Button btnEliminar = new Button("🗑 Eliminar");
        Button btnRecargar = new Button("🔄 Recargar");
        Button btnExportar = new Button("📊 Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Usuario sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Seleccioná un usuario primero"); return; }
            abrirFormulario(sel);
        });
        btnEliminar.setOnAction(e -> eliminarSeleccionado());
        btnRecargar.setOnAction(e -> cargar());
        btnExportar.setOnAction(e -> exportar());

        HBox barraBotones;
        if ("CONSULTA".equals(rol)) {
            barraBotones = new HBox(8, btnRecargar, btnExportar);
        } else {
            barraBotones = new HBox(8, btnNuevo, btnEditar, btnEliminar, btnRecargar, btnExportar);
        }
        barraBotones.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TableColumn<Usuario, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colId.setPrefWidth(60);

        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Usuario, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(280);

        TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colRol.setPrefWidth(200);

        TableColumn<Usuario, Boolean> colActivo = new TableColumn<>("Activo");
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colActivo.setPrefWidth(80);

        tabla.getColumns().addAll(colId, colNombre, colEmail, colRol, colActivo);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        panel.getChildren().addAll(breadcrumb, barraFiltros, barraBotones, tabla);

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            List<Usuario> lista = api.listarUsuarios();
            todosLosUsuarios.setAll(lista);
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
        String rolFiltro = cmbRol.getValue();

        List<Usuario> filtrados = todosLosUsuarios.stream()
                .filter(u -> {
                    if (!texto.isEmpty()) {
                        String nombre = u.getNombre() != null ? u.getNombre().toLowerCase() : "";
                        String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                        if (!nombre.contains(texto) && !email.contains(texto)) return false;
                    }
                    if (rolFiltro != null && !rolFiltro.equals("Todos")) {
                        if (!rolFiltro.equals(u.getRol())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        tabla.setItems(FXCollections.observableArrayList(filtrados));
        lblContador.setText("Mostrando " + filtrados.size() + " usuarios");
    }

    private void abrirFormulario(Usuario existente) {
        FormularioUsuario.mostrar(owner, existente, usuario -> {
            try {
                if (usuario.getIdUsuario() == null) {
                    api.crearUsuario(usuario);
                } else {
                    api.actualizarUsuario(usuario.getIdUsuario(), usuario);
                }
                cargar();
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al guardar:\n" + ex.getMessage());
            }
        });
    }

    private void eliminarSeleccionado() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Seleccioná un usuario primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("¿Eliminar este usuario?");
        confirmacion.setContentText(sel.getNombre());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarUsuario(sel.getIdUsuario());
                    cargar();
                } catch (Exception ex) {
                    mostrarAlerta("Error al eliminar:\n" + ex.getMessage());
                }
            }
        });
    }

    private void exportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de usuarios");
        fc.setInitialFileName("usuarios");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;
        try {
            String[] encabezados = {"ID", "Nombre", "Email", "Rol", "Activo"};
            List<String[]> filas = new ArrayList<>();
            for (Usuario u : tabla.getItems()) {
                filas.add(new String[]{
                        String.valueOf(u.getIdUsuario()),
                        u.getNombre() != null ? u.getNombre() : "",
                        u.getEmail() != null ? u.getEmail() : "",
                        u.getRol() != null ? u.getRol() : "",
                        String.valueOf(u.getActivo())
                });
            }
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, "Reporte de Usuarios", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "Usuarios", encabezados, filas);
            }
            mostrarAlerta("Reporte exportado:\n" + ruta);
        } catch (Exception ex) {
            mostrarAlerta("Error al exportar:\n" + ex.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}