package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Entidad;
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

public class PanelEntidades {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Entidad> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;
    private final Label lblContador = new Label();

    private final ObservableList<Entidad> todasLasEntidades = FXCollections.observableArrayList();
    private final TextField txtBuscar = new TextField();
    private final ComboBox<String> cmbTipo = new ComboBox<>();

    public PanelEntidades(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        HBox breadcrumb = new HBox(10);
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblRuta = new Label("🏠 / Entidades");
        lblRuta.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d63384;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        breadcrumb.getChildren().addAll(lblRuta, spacer, lblContador);

        txtBuscar.setPromptText("🔍 Buscar por nombre, documento o email...");
        txtBuscar.setPrefWidth(320);
        txtBuscar.setPrefHeight(35);

        cmbTipo.getItems().addAll("Todos", "PERSONA", "FAMILIA", "INSTITUCION", "DONANTE");
        cmbTipo.setValue("Todos");
        cmbTipo.setPrefWidth(180);
        cmbTipo.setPrefHeight(35);

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px;");
        btnLimpiar.setOnAction(e -> {
            txtBuscar.clear();
            cmbTipo.setValue("Todos");
            aplicarFiltros();
        });

        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbTipo.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());

        HBox barraFiltros = new HBox(10, txtBuscar, cmbTipo, btnLimpiar);
        barraFiltros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button btnNuevo = new Button("➕ Nuevo");
        Button btnEditar = new Button("✏ Editar");
        Button btnEliminar = new Button("🗑 Eliminar");
        Button btnRecargar = new Button("🔄 Recargar");
        Button btnExportar = new Button("📊 Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Entidad sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Seleccioná una entidad primero"); return; }
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

        TableColumn<Entidad, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idEntidad"));
        colId.setPrefWidth(50);

        TableColumn<Entidad, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setPrefWidth(120);

        TableColumn<Entidad, String> colNombre = new TableColumn<>("Nombre completo");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colNombre.setPrefWidth(200);

        TableColumn<Entidad, String> colDoc = new TableColumn<>("Documento");
        colDoc.setCellValueFactory(new PropertyValueFactory<>("documentoIdentidad"));
        colDoc.setPrefWidth(120);

        TableColumn<Entidad, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTel.setPrefWidth(120);

        TableColumn<Entidad, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        tabla.getColumns().addAll(colId, colTipo, colNombre, colDoc, colTel, colEmail);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        panel.getChildren().addAll(breadcrumb, barraFiltros, barraBotones, tabla);

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            List<Entidad> lista = api.listarEntidades();
            todasLasEntidades.setAll(lista);
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
        String tipo = cmbTipo.getValue();

        List<Entidad> filtrados = todasLasEntidades.stream()
                .filter(en -> {
                    if (!texto.isEmpty()) {
                        String nom = en.getNombreCompleto() != null ? en.getNombreCompleto().toLowerCase() : "";
                        String doc = en.getDocumentoIdentidad() != null ? en.getDocumentoIdentidad().toLowerCase() : "";
                        String mail = en.getEmail() != null ? en.getEmail().toLowerCase() : "";
                        if (!nom.contains(texto) && !doc.contains(texto) && !mail.contains(texto)) return false;
                    }
                    if (tipo != null && !tipo.equals("Todos")) {
                        if (!tipo.equals(en.getTipo())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        tabla.setItems(FXCollections.observableArrayList(filtrados));
        lblContador.setText("Mostrando " + filtrados.size() + " entidades");
    }

    private void abrirFormulario(Entidad existente) {
        FormularioEntidad.mostrar(owner, existente, entidad -> {
            try {
                if (entidad.getIdEntidad() == null) {
                    api.crearEntidad(entidad);
                } else {
                    api.actualizarEntidad(entidad.getIdEntidad(), entidad);
                }
                cargar();
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al guardar:\n" + ex.getMessage());
            }
        });
    }

    private void eliminarSeleccionado() {
        Entidad sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Seleccioná una entidad primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("¿Eliminar esta entidad?");
        confirmacion.setContentText(sel.getNombreCompleto());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarEntidad(sel.getIdEntidad());
                    cargar();
                } catch (Exception ex) {
                    mostrarAlerta("Error al eliminar:\n" + ex.getMessage());
                }
            }
        });
    }

    private void exportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de entidades");
        fc.setInitialFileName("entidades");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;
        try {
            String[] encabezados = {"ID", "Tipo", "Nombre", "Documento", "Teléfono", "Email", "Dirección"};
            List<String[]> filas = new ArrayList<>();
            for (Entidad en : tabla.getItems()) {
                filas.add(new String[]{
                        String.valueOf(en.getIdEntidad()),
                        en.getTipo() != null ? en.getTipo() : "",
                        en.getNombreCompleto() != null ? en.getNombreCompleto() : "",
                        en.getDocumentoIdentidad() != null ? en.getDocumentoIdentidad() : "",
                        en.getTelefono() != null ? en.getTelefono() : "",
                        en.getEmail() != null ? en.getEmail() : "",
                        en.getDireccion() != null ? en.getDireccion() : ""
                });
            }
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, "Reporte de Entidades", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "Entidades", encabezados, filas);
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