package com.ong.desktop.vistas;

import com.ong.desktop.modelos.DonacionRecepcion;
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

public class PanelDonaciones {

    private final ApiServicio api = new ApiServicio();
    private final TableView<DonacionRecepcion> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;
    private final Label lblContador = new Label();

    private final ObservableList<DonacionRecepcion> todasLasDonaciones = FXCollections.observableArrayList();
    private final TextField txtBuscar = new TextField();

    public PanelDonaciones(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        HBox breadcrumb = new HBox(10);
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblRuta = new Label("🏠 / Donaciones");
        lblRuta.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d63384;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        breadcrumb.getChildren().addAll(lblRuta, spacer, lblContador);

        txtBuscar.setPromptText("🔍 Buscar por donante, responsable u observaciones...");
        txtBuscar.setPrefWidth(420);
        txtBuscar.setPrefHeight(35);

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px;");
        btnLimpiar.setOnAction(e -> {
            txtBuscar.clear();
            aplicarFiltros();
        });

        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());

        HBox barraFiltros = new HBox(10, txtBuscar, btnLimpiar);
        barraFiltros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button btnNuevo = new Button("➕ Nueva");
        Button btnEditar = new Button("✏ Editar");
        Button btnEliminar = new Button("🗑 Eliminar");
        Button btnRecargar = new Button("🔄 Recargar");
        Button btnExportar = new Button("📊 Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            DonacionRecepcion sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Seleccioná una donación primero"); return; }
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

        TableColumn<DonacionRecepcion, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idDonacion"));
        colId.setPrefWidth(50);

        TableColumn<DonacionRecepcion, Object> colDonante = new TableColumn<>("Donante");
        colDonante.setCellValueFactory(new PropertyValueFactory<>("donante"));
        colDonante.setPrefWidth(220);

        TableColumn<DonacionRecepcion, Object> colResponsable = new TableColumn<>("Responsable");
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsableRecepcion"));
        colResponsable.setPrefWidth(200);

        TableColumn<DonacionRecepcion, Object> colFecha = new TableColumn<>("Fecha recepción");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaRecepcion"));
        colFecha.setPrefWidth(160);

        TableColumn<DonacionRecepcion, String> colObs = new TableColumn<>("Observaciones");
        colObs.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        colObs.setPrefWidth(260);

        tabla.getColumns().addAll(colId, colDonante, colResponsable, colFecha, colObs);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        panel.getChildren().addAll(breadcrumb, barraFiltros, barraBotones, tabla);

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            List<DonacionRecepcion> lista = api.listarDonaciones();
            todasLasDonaciones.setAll(lista);
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";

        List<DonacionRecepcion> filtrados = todasLasDonaciones.stream()
                .filter(d -> {
                    if (texto.isEmpty()) return true;
                    String don = d.getDonante() != null && d.getDonante().getNombreCompleto() != null
                            ? d.getDonante().getNombreCompleto().toLowerCase() : "";
                    String resp = d.getResponsableRecepcion() != null && d.getResponsableRecepcion().getNombre() != null
                            ? d.getResponsableRecepcion().getNombre().toLowerCase() : "";
                    String obs = d.getObservaciones() != null ? d.getObservaciones().toLowerCase() : "";
                    return don.contains(texto) || resp.contains(texto) || obs.contains(texto);
                })
                .collect(Collectors.toList());

        tabla.setItems(FXCollections.observableArrayList(filtrados));
        lblContador.setText("Mostrando " + filtrados.size() + " donaciones");
    }

    private void abrirFormulario(DonacionRecepcion existente) {
        FormularioDonacion.mostrar(owner, existente, donacion -> {
            try {
                if (donacion.getIdDonacion() == null) {
                    api.crearDonacion(donacion);
                } else {
                    api.actualizarDonacion(donacion.getIdDonacion(), donacion);
                }
                cargar();
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al guardar:\n" + ex.getMessage());
            }
        });
    }

    private void eliminarSeleccionado() {
        DonacionRecepcion sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Seleccioná una donación primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("¿Eliminar esta donación?");
        confirmacion.setContentText("ID: " + sel.getIdDonacion());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarDonacion(sel.getIdDonacion());
                    cargar();
                } catch (Exception ex) {
                    mostrarAlerta("Error al eliminar:\n" + ex.getMessage());
                }
            }
        });
    }

    private void exportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de donaciones");
        fc.setInitialFileName("donaciones");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;
        try {
            String[] encabezados = {"ID", "Donante", "Responsable", "Observaciones"};
            List<String[]> filas = new ArrayList<>();
            for (DonacionRecepcion d : tabla.getItems()) {
                filas.add(new String[]{
                        String.valueOf(d.getIdDonacion()),
                        d.getDonante() != null ? d.getDonante().getNombreCompleto() : "",
                        d.getResponsableRecepcion() != null ? d.getResponsableRecepcion().getNombre() : "",
                        d.getObservaciones() != null ? d.getObservaciones() : ""
                });
            }
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, "Reporte de Donaciones", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "Donaciones", encabezados, filas);
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