package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Reserva;
import com.ong.desktop.servicios.ApiServicio;
import com.ong.desktop.servicios.Exportador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PanelReservas {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Reserva> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;

    public PanelReservas(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        TableColumn<Reserva, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        colId.setPrefWidth(50);

        TableColumn<Reserva, Object> colArticulo = new TableColumn<>("Artículo");
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colArticulo.setPrefWidth(200);

        TableColumn<Reserva, Object> colReceptor = new TableColumn<>("Receptor");
        colReceptor.setCellValueFactory(new PropertyValueFactory<>("receptor"));
        colReceptor.setPrefWidth(180);

        TableColumn<Reserva, Integer> colCantidad = new TableColumn<>("Cant.");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setPrefWidth(60);

        TableColumn<Reserva, Object> colResponsable = new TableColumn<>("Responsable");
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        colResponsable.setPrefWidth(180);

        TableColumn<Reserva, Object> colFecha = new TableColumn<>("Fecha prevista");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPrevistaEntrega"));
        colFecha.setPrefWidth(120);

        TableColumn<Reserva, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(100);

        tabla.getColumns().addAll(colId, colArticulo, colReceptor, colCantidad, colResponsable, colFecha, colEstado);

        Button btnNuevo = new Button("Nuevo");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnRecargar = new Button("Recargar");
        Button btnExportar = new Button("Exportar");

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
            ObservableList<Reserva> datos = FXCollections.observableArrayList(api.listarReservas());
            tabla.setItems(datos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
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
            java.util.List<String[]> filas = new java.util.ArrayList<>();
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