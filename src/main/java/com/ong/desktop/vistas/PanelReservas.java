package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Reserva;
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

public class PanelReservas {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Reserva> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;
    private final Label lblContador = new Label();

    private final ObservableList<Reserva> todasLasReservas = FXCollections.observableArrayList();
    private final TextField txtBuscar = new TextField();
    private final ComboBox<String> cmbEstado = new ComboBox<>();

    public PanelReservas(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        HBox breadcrumb = new HBox(10);
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblRuta = new Label("🏠 / Reservas");
        lblRuta.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d63384;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        breadcrumb.getChildren().addAll(lblRuta, spacer, lblContador);

        txtBuscar.setPromptText("🔍 Buscar por artículo, receptor o responsable...");
        txtBuscar.setPrefWidth(320);
        txtBuscar.setPrefHeight(35);

        cmbEstado.getItems().addAll("Todos", "ACTIVA", "ENTREGADA", "CANCELADA");
        cmbEstado.setValue("Todos");
        cmbEstado.setPrefWidth(160);
        cmbEstado.setPrefHeight(35);

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px;");
        btnLimpiar.setOnAction(e -> {
            txtBuscar.clear();
            cmbEstado.setValue("Todos");
            aplicarFiltros();
        });

        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbEstado.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());

        HBox barraFiltros = new HBox(10, txtBuscar, cmbEstado, btnLimpiar);
        barraFiltros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button btnNuevo = new Button("➕ Nueva");
        Button btnEditar = new Button("✏ Editar");
        Button btnEliminar = new Button("🗑 Eliminar");
        Button btnRecargar = new Button("🔄 Recargar");
        Button btnExportar = new Button("📊 Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Reserva sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Seleccioná una reserva primero"); return; }
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

        TableColumn<Reserva, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        colId.setPrefWidth(50);

        TableColumn<Reserva, Object> colArticulo = new TableColumn<>("Artículo");
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colArticulo.setPrefWidth(180);

        TableColumn<Reserva, Object> colReceptor = new TableColumn<>("Receptor");
        colReceptor.setCellValueFactory(new PropertyValueFactory<>("receptor"));
        colReceptor.setPrefWidth(180);

        TableColumn<Reserva, Integer> colCantidad = new TableColumn<>("Cant.");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setPrefWidth(60);

        TableColumn<Reserva, Object> colResponsable = new TableColumn<>("Responsable");
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        colResponsable.setPrefWidth(160);

        TableColumn<Reserva, Object> colFecha = new TableColumn<>("Fecha prevista");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPrevistaEntrega"));
        colFecha.setPrefWidth(120);

        TableColumn<Reserva, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(110);

        tabla.getColumns().addAll(colId, colArticulo, colReceptor, colCantidad, colResponsable, colFecha, colEstado);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        panel.getChildren().addAll(breadcrumb, barraFiltros, barraBotones, tabla);

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            List<Reserva> lista = api.listarReservas();
            todasLasReservas.setAll(lista);
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
        String estado = cmbEstado.getValue();

        List<Reserva> filtrados = todasLasReservas.stream()
                .filter(r -> {
                    if (!texto.isEmpty()) {
                        String art = r.getArticulo() != null && r.getArticulo().getNombre() != null
                                ? r.getArticulo().getNombre().toLowerCase() : "";
                        String rec = r.getReceptor() != null && r.getReceptor().getNombreCompleto() != null
                                ? r.getReceptor().getNombreCompleto().toLowerCase() : "";
                        String resp = r.getResponsable() != null && r.getResponsable().getNombre() != null
                                ? r.getResponsable().getNombre().toLowerCase() : "";
                        if (!art.contains(texto) && !rec.contains(texto) && !resp.contains(texto)) return false;
                    }
                    if (estado != null && !estado.equals("Todos")) {
                        if (!estado.equals(r.getEstado())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        tabla.setItems(FXCollections.observableArrayList(filtrados));
        lblContador.setText("Mostrando " + filtrados.size() + " reservas");
    }

    private void abrirFormulario(Reserva existente) {
        FormularioReserva.mostrar(owner, existente, reserva -> {
            try {
                if (reserva.getIdReserva() == null) {
                    api.crearReserva(reserva);
                } else {
                    api.actualizarReserva(reserva.getIdReserva(), reserva);
                }
                cargar();
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al guardar:\n" + ex.getMessage());
            }
        });
    }

    private void eliminarSeleccionado() {
        Reserva sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Seleccioná una reserva primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("¿Eliminar esta reserva?");
        confirmacion.setContentText("ID: " + sel.getIdReserva());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarReserva(sel.getIdReserva());
                    cargar();
                } catch (Exception ex) {
                    mostrarAlerta("Error al eliminar:\n" + ex.getMessage());
                }
            }
        });
    }

    private void exportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de reservas");
        fc.setInitialFileName("reservas");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;
        try {
            String[] encabezados = {"ID", "Artículo", "Receptor", "Cantidad", "Responsable", "Fecha prevista", "Estado"};
            List<String[]> filas = new ArrayList<>();
            for (Reserva r : tabla.getItems()) {
                filas.add(new String[]{
                        String.valueOf(r.getIdReserva()),
                        r.getArticulo() != null ? r.getArticulo().getNombre() : "",
                        r.getReceptor() != null ? r.getReceptor().getNombreCompleto() : "",
                        String.valueOf(r.getCantidad()),
                        r.getResponsable() != null ? r.getResponsable().getNombre() : "",
                        r.getFechaPrevistaEntrega() != null ? r.getFechaPrevistaEntrega().toString() : "",
                        r.getEstado() != null ? r.getEstado() : ""
                });
            }
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, "Reporte de Reservas", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "Reservas", encabezados, filas);
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