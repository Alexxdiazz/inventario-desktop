
package com.ong.desktop.vistas;

import java.io.File;
import java.util.List;

import com.ong.desktop.modelos.Prestamo;
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

public class PanelPrestamos {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Prestamo> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;

    public PanelPrestamos(Stage owner,String rol) {
        this.owner = owner;
        this.rol = rol;

    }

    public VBox construir() {
        TableColumn<Prestamo, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        colId.setPrefWidth(50);

        TableColumn<Prestamo, Object> colReceptor = new TableColumn<>("Receptor");
        colReceptor.setCellValueFactory(new PropertyValueFactory<>("receptor"));
        colReceptor.setPrefWidth(180);

        TableColumn<Prestamo, Object> colArticulo = new TableColumn<>("ArtÃ­culo");
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colArticulo.setPrefWidth(200);

        TableColumn<Prestamo, Integer> colCantidad = new TableColumn<>("Cant.");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setPrefWidth(60);

        TableColumn<Prestamo, Object> colResponsable = new TableColumn<>("Responsable");
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        colResponsable.setPrefWidth(180);

        TableColumn<Prestamo, Object> colFecha = new TableColumn<>("Fecha prevista");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPrevistaDevolucion"));
        colFecha.setPrefWidth(120);

        TableColumn<Prestamo, Object> colDevuelto = new TableColumn<>("Devuelto");
        colDevuelto.setCellValueFactory(new PropertyValueFactory<>("fechaRealDevolucion"));
        colDevuelto.setPrefWidth(140);

        tabla.getColumns().addAll(colId, colReceptor, colArticulo, colCantidad, colResponsable, colFecha, colDevuelto);

        Button btnNuevo = new Button("Nuevo");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnRecargar = new Button("Recargar");
        Button btnDevolver = new Button("Registrar DevoluciÃ³n");
        btnDevolver.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
        Button btnExportar = new Button("Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Prestamo sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) {
                mostrarAlerta("Selecciona un prÃ©stamo primero");
                return;
            }
            abrirFormulario(sel);
        });
        btnEliminar.setOnAction(e -> eliminarSeleccionado());
        btnRecargar.setOnAction(e -> cargar());
        btnExportar.setOnAction(e -> exportar());
        btnDevolver.setOnAction(e -> registrarDevolucion());

        HBox barraBotones;
        if ("CONSULTA".equals(rol)) {
            barraBotones = new HBox(10, btnRecargar, btnExportar);
        } else {
            barraBotones = new HBox(10, btnNuevo, btnEditar, btnEliminar, btnRecargar, btnDevolver, btnExportar);
        }
        barraBotones.setStyle("-fx-padding: 10px;");

        VBox panel = new VBox(10, barraBotones, tabla);
        panel.setStyle("-fx-padding: 15px;");

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            ObservableList<Prestamo> datos = FXCollections.observableArrayList(api.listarPrestamos());
            tabla.setItems(datos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void abrirFormulario(Prestamo existente) {
        FormularioPrestamo.mostrar(owner, existente, prestamo -> {
            try {
                if (prestamo.getIdPrestamo() == null) {
                    api.crearPrestamo(prestamo);
                } else {
                    api.actualizarPrestamo(prestamo.getIdPrestamo(), prestamo);
                }
                cargar();
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al guardar:\n" + ex.getMessage());
            }
        });
    }

    private void eliminarSeleccionado() {
        Prestamo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Selecciona un prÃ©stamo primero");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("Â¿Eliminar este prÃ©stamo?");
        confirmacion.setContentText("ID: " + sel.getIdPrestamo());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarPrestamo(sel.getIdPrestamo());
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
        fc.setTitle("Guardar reporte de prÃ©stamos");
        fc.setInitialFileName("prestamos");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null)
            return;
        try {
            String[] encabezados = { "ID", "Receptor", "ArtÃ­culo", "Cantidad", "Responsable", "Fecha prevista" };
            List<String[]> filas = new java.util.ArrayList<>();
            for (Prestamo p : tabla.getItems()) {
                filas.add(new String[] {
                        String.valueOf(p.getIdPrestamo()),
                        p.getReceptor() != null ? p.getReceptor().getNombreCompleto() : "",
                        p.getArticulo() != null ? p.getArticulo().getNombre() : "",
                        String.valueOf(p.getCantidad()),
                        p.getResponsable() != null ? p.getResponsable().getNombre() : "",
                        p.getFechaPrevistaDevolucion() != null ? p.getFechaPrevistaDevolucion().toString() : ""
                });
            }
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, "Reporte de PrÃ©stamos", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx"))
                    ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "PrÃ©stamos", encabezados, filas);
            }
            mostrarAlerta("Reporte exportado:\n" + ruta);
        } catch (Exception ex) {
            mostrarAlerta("Error al exportar:\n" + ex.getMessage());
        }
    }

    private void registrarDevolucion() {
        Prestamo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("SeleccionÃ¡ un prÃ©stamo primero");
            return;
        }
        if (sel.getFechaRealDevolucion() != null) {
            mostrarAlerta("Este prÃ©stamo ya fue devuelto.\nFecha: " + sel.getFechaRealDevolucion());
            return;
        }

        FormularioDevolucion.mostrar(owner, sel, prestamo -> {
            try {
                api.actualizarPrestamo(prestamo.getIdPrestamo(), prestamo);
                cargar();
                mostrarAlerta("DevoluciÃ³n registrada correctamente");
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al registrar devoluciÃ³n:\n" + ex.getMessage());
            }
        });
    }
}