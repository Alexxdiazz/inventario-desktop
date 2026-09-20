package com.ong.desktop.vistas;

import java.io.File;
import java.util.List;

import com.ong.desktop.modelos.HistorialMovimiento;
import com.ong.desktop.servicios.ApiServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.ong.desktop.servicios.Exportador;
import javafx.stage.FileChooser;
import java.io.File;

public class PanelHistorial {

    private final ApiServicio api = new ApiServicio();
    private final TableView<HistorialMovimiento> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;

    public PanelHistorial(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
        
    }

    public VBox construir() {
        TableColumn<HistorialMovimiento, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idMovimiento"));
        colId.setPrefWidth(50);

        TableColumn<HistorialMovimiento, Object> colArticulo = new TableColumn<>("Articulo");
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colArticulo.setPrefWidth(200);

        TableColumn<HistorialMovimiento, Object> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colUsuario.setPrefWidth(180);

        TableColumn<HistorialMovimiento, String> colTipo = new TableColumn<>("Operacion");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoOperacion"));
        colTipo.setPrefWidth(120);

        TableColumn<HistorialMovimiento, String> colAnterior = new TableColumn<>("Estado anterior");
        colAnterior.setCellValueFactory(new PropertyValueFactory<>("estadoAnterior"));
        colAnterior.setPrefWidth(140);

        TableColumn<HistorialMovimiento, String> colNuevo = new TableColumn<>("Estado nuevo");
        colNuevo.setCellValueFactory(new PropertyValueFactory<>("estadoNuevo"));
        colNuevo.setPrefWidth(140);

        TableColumn<HistorialMovimiento, String> colDesc = new TableColumn<>("Descripcion");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcionMovimiento"));
        colDesc.setPrefWidth(220);

        tabla.getColumns().addAll(colId, colArticulo, colUsuario, colTipo, colAnterior, colNuevo, colDesc);

        Button btnNuevo = new Button("Nuevo");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnRecargar = new Button("Recargar");
        Button btnExportar = new Button("Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            HistorialMovimiento sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Selecciona un movimiento primero"); return; }
            abrirFormulario(sel);
        });
        btnEliminar.setOnAction(e -> eliminarSeleccionado());
        btnRecargar.setOnAction(e -> cargar());
        btnExportar.setOnAction(e -> exportar());

        HBox barraBotones = new HBox(10, btnNuevo, btnEditar, btnEliminar, btnRecargar, btnExportar);
        barraBotones.setStyle("-fx-padding: 10px;");

        VBox panel = new VBox(10, barraBotones, tabla);
        panel.setStyle("-fx-padding: 15px;");

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            ObservableList<HistorialMovimiento> datos = FXCollections.observableArrayList(api.listarHistorial());
            tabla.setItems(datos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
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
        if (sel == null) { mostrarAlerta("Selecciona un movimiento primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("Eliminar este movimiento?");
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

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
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
            List<String[]> filas = new java.util.ArrayList<>();
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
}