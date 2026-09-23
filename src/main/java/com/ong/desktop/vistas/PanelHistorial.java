package com.ong.desktop.vistas;

import com.ong.desktop.modelos.HistorialMovimiento;
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

public class PanelHistorial {

    private final ApiServicio api = new ApiServicio();
    private final TableView<HistorialMovimiento> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;
    private final Label lblContador = new Label();

    private final ObservableList<HistorialMovimiento> todosLosMovimientos = FXCollections.observableArrayList();
    private final TextField txtBuscar = new TextField();
    private final ComboBox<String> cmbTipo = new ComboBox<>();

    public PanelHistorial(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        HBox breadcrumb = new HBox(10);
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblRuta = new Label("🏠 / Historial");
        lblRuta.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d63384;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        breadcrumb.getChildren().addAll(lblRuta, spacer, lblContador);

        txtBuscar.setPromptText("🔍 Buscar por artículo, usuario o descripción...");
        txtBuscar.setPrefWidth(320);
        txtBuscar.setPrefHeight(35);

        cmbTipo.getItems().addAll("Todos", "PRESTAMO", "DEVOLUCION", "ENTREGA", "CREACION", "EDICION", "BAJA");
        cmbTipo.setValue("Todos");
        cmbTipo.setPrefWidth(160);
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
            HistorialMovimiento sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Seleccioná un movimiento primero"); return; }
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

        TableColumn<HistorialMovimiento, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idMovimiento"));
        colId.setPrefWidth(50);

        TableColumn<HistorialMovimiento, Object> colArticulo = new TableColumn<>("Artículo");
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colArticulo.setPrefWidth(180);

        TableColumn<HistorialMovimiento, Object> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colUsuario.setPrefWidth(160);

        TableColumn<HistorialMovimiento, String> colTipo = new TableColumn<>("Operación");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoOperacion"));
        colTipo.setPrefWidth(110);

        TableColumn<HistorialMovimiento, String> colAnterior = new TableColumn<>("Estado anterior");
        colAnterior.setCellValueFactory(new PropertyValueFactory<>("estadoAnterior"));
        colAnterior.setPrefWidth(130);

        TableColumn<HistorialMovimiento, String> colNuevo = new TableColumn<>("Estado nuevo");
        colNuevo.setCellValueFactory(new PropertyValueFactory<>("estadoNuevo"));
        colNuevo.setPrefWidth(130);

        TableColumn<HistorialMovimiento, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcionMovimiento"));
        colDesc.setPrefWidth(240);

        tabla.getColumns().addAll(colId, colArticulo, colUsuario, colTipo, colAnterior, colNuevo, colDesc);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        panel.getChildren().addAll(breadcrumb, barraFiltros, barraBotones, tabla);

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            List<HistorialMovimiento> lista = api.listarHistorial();
            todosLosMovimientos.setAll(lista);
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
        String tipo = cmbTipo.getValue();

        List<HistorialMovimiento> filtrados = todosLosMovimientos.stream()
                .filter(h -> {
                    if (!texto.isEmpty()) {
                        String art = h.getArticulo() != null && h.getArticulo().getNombre() != null
                                ? h.getArticulo().getNombre().toLowerCase() : "";
                        String usr = h.getUsuario() != null && h.getUsuario().getNombre() != null
                                ? h.getUsuario().getNombre().toLowerCase() : "";
                        String desc = h.getDescripcionMovimiento() != null
                                ? h.getDescripcionMovimiento().toLowerCase() : "";
                        if (!art.contains(texto) && !usr.contains(texto) && !desc.contains(texto)) return false;
                    }
                    if (tipo != null && !tipo.equals("Todos")) {
                        if (!tipo.equals(h.getTipoOperacion())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        tabla.setItems(FXCollections.observableArrayList(filtrados));
        lblContador.setText("Mostrando " + filtrados.size() + " movimientos");
    }

    private void abrirFormulario(HistorialMovimiento existente) {
        FormularioHistorial.mostrar(owner, existente, movimiento -> {
            try {
                if (movimiento.getIdMovimiento() == null) {
                    api.crearHistorial(movimiento);
                } else {
                    api.actualizarHistorial(movimiento.getIdMovimiento(), movimiento);
                }
                cargar();
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al guardar:\n" + ex.getMessage());
            }
        });
    }

    private void eliminarSeleccionado() {
        HistorialMovimiento sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Seleccioná un movimiento primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("¿Eliminar este movimiento?");
        confirmacion.setContentText("ID: " + sel.getIdMovimiento());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarHistorial(sel.getIdMovimiento());
                    cargar();
                } catch (Exception ex) {
                    mostrarAlerta("Error al eliminar:\n" + ex.getMessage());
                }
            }
        });
    }

    private void exportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de historial");
        fc.setInitialFileName("historial");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;
        try {
            String[] encabezados = {"ID", "Artículo", "Usuario", "Operación", "Estado anterior", "Estado nuevo", "Descripción"};
            List<String[]> filas = new ArrayList<>();
            for (HistorialMovimiento h : tabla.getItems()) {
                filas.add(new String[]{
                        String.valueOf(h.getIdMovimiento()),
                        h.getArticulo() != null ? h.getArticulo().getNombre() : "",
                        h.getUsuario() != null ? h.getUsuario().getNombre() : "",
                        h.getTipoOperacion() != null ? h.getTipoOperacion() : "",
                        h.getEstadoAnterior() != null ? h.getEstadoAnterior() : "",
                        h.getEstadoNuevo() != null ? h.getEstadoNuevo() : "",
                        h.getDescripcionMovimiento() != null ? h.getDescripcionMovimiento() : ""
                });
            }
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, "Reporte de Historial", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "Historial", encabezados, filas);
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