package com.ong.desktop.vistas;

import com.ong.desktop.modelos.Entidad;
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

public class PanelEntidades {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Entidad> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;

    public PanelEntidades(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;

    }

    public VBox construir() {
        TableColumn<Entidad, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idEntidad"));
        colId.setPrefWidth(60);

        TableColumn<Entidad, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setPrefWidth(120);

        TableColumn<Entidad, String> colNombre = new TableColumn<>("Nombre completo");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colNombre.setPrefWidth(200);

        TableColumn<Entidad, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documentoIdentidad"));
        colDocumento.setPrefWidth(120);

        TableColumn<Entidad, String> colTelefono = new TableColumn<>("TelÃ©fono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(120);

        TableColumn<Entidad, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        tabla.getColumns().addAll(colId, colTipo, colNombre, colDocumento, colTelefono, colEmail);

        Button btnNuevo = new Button("Nuevo");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnRecargar = new Button("Recargar");
        Button btnExportar = new Button("Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Entidad sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Selecciona una entidad primero"); return; }
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
            ObservableList<Entidad> datos = FXCollections.observableArrayList(api.listarEntidades());
            tabla.setItems(datos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
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
        if (sel == null) { mostrarAlerta("Selecciona una entidad primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("Â¿Eliminar esta entidad?");
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

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
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
            String[] encabezados = {"ID", "Tipo", "Nombre", "Documento", "TelÃ©fono", "Email", "DirecciÃ³n"};
            List<String[]> filas = new java.util.ArrayList<>();
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
    
}