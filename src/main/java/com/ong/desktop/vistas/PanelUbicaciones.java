package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Ubicacion;
import com.ong.desktop.servicios.ApiServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.ong.desktop.servicios.Exportador;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class PanelUbicaciones {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Ubicacion> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;

    public PanelUbicaciones(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        TableColumn<Ubicacion, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idUbicacion"));
        colId.setPrefWidth(60);

        TableColumn<Ubicacion, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Ubicacion, String> colDescripcion = new TableColumn<>("Descripcion");
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDescripcion.setPrefWidth(300);

        tabla.getColumns().addAll(colId, colNombre, colDescripcion);

        Button btnNuevo = new Button("Nuevo");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnRecargar = new Button("Recargar");
        Button btnExportar = new Button("Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Ubicacion sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Selecciona una ubicacion primero"); return; }
            abrirFormulario(sel);
        });
        btnEliminar.setOnAction(e -> eliminarSeleccionado());
        btnRecargar.setOnAction(e -> cargar());
        btnExportar.setOnAction(e -> exportar());

        HBox barraBotones;
        if ("CONSULTA".equals(rol)) {
            barraBotones = new HBox(10, btnRecargar, btnExportar);
        } else {
            barraBotones = new HBox(10, btnNuevo, btnEditar, btnEliminar, btnRecargar, btnExportar);
        }
        barraBotones.setStyle("-fx-padding: 10px;");

        VBox panel = new VBox(10, barraBotones, tabla);
        panel.setStyle("-fx-padding: 15px;");

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            ObservableList<Ubicacion> datos = FXCollections.observableArrayList(api.listarUbicaciones());
            tabla.setItems(datos);
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
        if (sel == null) { mostrarAlerta("Selecciona una ubicacion primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("Eliminar esta ubicacion?");
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

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
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
            String[] encabezados = {"ID", "Nombre", "DescripciÃ³n"};
            List<String[]> filas = new java.util.ArrayList<>();
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
}
