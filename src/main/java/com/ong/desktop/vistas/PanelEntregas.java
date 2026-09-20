package com.ong.desktop.vistas;

import java.io.File;
import java.util.List;

import com.ong.desktop.modelos.EntregaDefinitiva;
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

public class PanelEntregas {

    private final ApiServicio api = new ApiServicio();
    private final TableView<EntregaDefinitiva> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;

    public PanelEntregas(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        TableColumn<EntregaDefinitiva, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idEntrega"));
        colId.setPrefWidth(50);

        TableColumn<EntregaDefinitiva, Object> colReceptor = new TableColumn<>("Receptor");
        colReceptor.setCellValueFactory(new PropertyValueFactory<>("receptor"));
        colReceptor.setPrefWidth(180);

        TableColumn<EntregaDefinitiva, Object> colArticulo = new TableColumn<>("Articulo");
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colArticulo.setPrefWidth(200);

        TableColumn<EntregaDefinitiva, Integer> colCantidad = new TableColumn<>("Cant.");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setPrefWidth(60);

        TableColumn<EntregaDefinitiva, Object> colResponsable = new TableColumn<>("Responsable");
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        colResponsable.setPrefWidth(180);

        TableColumn<EntregaDefinitiva, String> colMotivo = new TableColumn<>("Motivo/Campana");
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivoCampana"));
        colMotivo.setPrefWidth(180);

        tabla.getColumns().addAll(colId, colReceptor, colArticulo, colCantidad, colResponsable, colMotivo);

        Button btnNuevo = new Button("Nuevo");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnRecargar = new Button("Recargar");
        Button btnExportar = new Button("Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            EntregaDefinitiva sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Selecciona una entrega primero"); return; }
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
            ObservableList<EntregaDefinitiva> datos = FXCollections.observableArrayList(api.listarEntregas());
            tabla.setItems(datos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void abrirFormulario(EntregaDefinitiva existente) {
    FormularioEntrega.mostrar(owner, existente, entrega -> {
        try {
            if (entrega.getIdEntrega() == null) {
                // NUEVA entrega
                EntregaDefinitiva creada = api.crearEntrega(entrega);
                // Registrar en historial
                if (creada.getArticulo() != null && creada.getResponsable() != null) {
                    String receptor = creada.getReceptor() != null ? creada.getReceptor().getNombreCompleto() : "sin receptor";
                    api.registrarMovimiento(
                            creada.getArticulo().getIdArticulo(),
                            creada.getResponsable().getIdUsuario(),
                            "ENTREGA",
                            "DISPONIBLE",
                            "ENTREGADO",
                            "Entrega a " + receptor + " (cantidad: " + creada.getCantidad() + ")"
                    );
                }
            } else {
                api.actualizarEntrega(entrega.getIdEntrega(), entrega);
            }
            cargar();
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta("Error al guardar:\n" + ex.getMessage());
        }
    });
}

    private void eliminarSeleccionado() {
        EntregaDefinitiva sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona una entrega primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("Eliminar esta entrega?");
        confirmacion.setContentText("ID: " + sel.getIdEntrega());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarEntrega(sel.getIdEntrega());
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
        fc.setTitle("Guardar reporte de entregas");
        fc.setInitialFileName("entregas");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;
        try {
            String[] encabezados = {"ID", "Receptor", "ArtÃ­culo", "Cantidad", "Responsable", "Motivo/CampaÃ±a"};
            List<String[]> filas = new java.util.ArrayList<>();
            for (EntregaDefinitiva en : tabla.getItems()) {
                filas.add(new String[]{
                        String.valueOf(en.getIdEntrega()),
                        en.getReceptor() != null ? en.getReceptor().getNombreCompleto() : "",
                        en.getArticulo() != null ? en.getArticulo().getNombre() : "",
                        String.valueOf(en.getCantidad()),
                        en.getResponsable() != null ? en.getResponsable().getNombre() : "",
                        en.getMotivoCampana() != null ? en.getMotivoCampana() : ""
                });
            }
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, "Reporte de Entregas", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "Entregas", encabezados, filas);
            }
            mostrarAlerta("Reporte exportado:\n" + ruta);
        } catch (Exception ex) {
            mostrarAlerta("Error al exportar:\n" + ex.getMessage());
        }
    }
    
}