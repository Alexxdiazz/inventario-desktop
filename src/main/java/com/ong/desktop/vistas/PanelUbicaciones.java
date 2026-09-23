package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Ubicacion;
import com.ong.desktop.servicios.ApiServicio;
import com.ong.desktop.servicios.Exportador;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PanelUbicaciones {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Ubicacion> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;
    private final Label lblContador = new Label();

    public PanelUbicaciones(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        HBox breadcrumb = new HBox(10);
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblRuta = new Label("🏠 / Ubicaciones");
        lblRuta.setStyle("-fx-font-size: 20px; -fx-font-weigccccbold; -fx-text-fill: #d63384;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        breadcrumb.getChildren().addAll(lblRuta, spacer, lblContador);

        Button btnNuevo = new Button("➕ Nuevo");
        Button btnEditar = new Button("✏ Editar");
        Button btnEliminar = new Button("🗑 Eliminar");
        Button btnRecargar = new Button("🔄 Recargar");
        Button btnExportar = new Button("📊 Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Ubicacion sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Seleccioná una ubicación primero"); return; }
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

        TableColumn<Ubicacion, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idUbicacion"));
        colId.setPrefWidth(60);

        TableColumn<Ubicacion, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(250);

        TableColumn<Ubicacion, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDescripcion.setPrefWidth(400);

        tabla.getColumns().addAll(colId, colNombre, colDescripcion);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        panel.getChildren().addAll(breadcrumb, barraBotones, tabla);

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            List<Ubicacion> lista = api.listarUbicaciones();
            tabla.setItems(FXCollections.observableArrayList(lista));
            lblContador.setText("Mostrando " + lista.size() + " ubicaciones");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void abrirFormulario(Ubicacion existente) {
        FormularioUbicacion.mostrar(owner, existente, ubicacion -> {
            try {
                if (ubicacion.getIdUbicacion() == null) {
                    api.crearUbicacion(ubicacion);
                } else {
                    api.actualizarUbicacion(ubicacion.getIdUbicacion(), ubicacion);
                }
                cargar();
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al guardar:\n" + ex.getMessage());
            }
        });
    }

    private void eliminarSeleccionado() {
        Ubicacion sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Seleccioná una ubicación primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("¿Eliminar esta ubicación?");
        confirmacion.setContentText(sel.getNombre());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarUbicacion(sel.getIdUbicacion());
                    cargar();
                } catch (Exception ex) {
                    mostrarAlerta("Error al eliminar:\n" + ex.getMessage());
                }
            }
        });
    }

    private void exportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de ubicaciones");
        fc.setInitialFileName("ubicaciones");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;
        try {
            String[] encabezados = {"ID", "Nombre", "Descripción"};
            List<String[]> filas = new ArrayList<>();
            for (Ubicacion u : tabla.getItems()) {
                filas.add(new String[]{
                        String.valueOf(u.getIdUbicacion()),
                        u.getNombre() != null ? u.getNombre() : "",
                        u.getDescripcion() != null ? u.getDescripcion() : ""
                });
            }
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, "Reporte de Ubicaciones", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "Ubicaciones", encabezados, filas);
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