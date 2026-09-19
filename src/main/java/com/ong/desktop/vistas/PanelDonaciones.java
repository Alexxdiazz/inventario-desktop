package com.ong.desktop.vistas;

import com.ong.desktop.modelos.DonacionRecepcion;
import com.ong.desktop.servicios.ApiServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PanelDonaciones {

    private final ApiServicio api = new ApiServicio();
    private final TableView<DonacionRecepcion> tabla = new TableView<>();
    private final Stage owner;

    public PanelDonaciones(Stage owner) {
        this.owner = owner;
    }

    public VBox construir() {
        TableColumn<DonacionRecepcion, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idDonacion"));
        colId.setPrefWidth(60);

        TableColumn<DonacionRecepcion, Object> colDonante = new TableColumn<>("Donante");
        colDonante.setCellValueFactory(new PropertyValueFactory<>("donante"));
        colDonante.setPrefWidth(220);

        TableColumn<DonacionRecepcion, Object> colResponsable = new TableColumn<>("Responsable");
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsableRecepcion"));
        colResponsable.setPrefWidth(220);

        TableColumn<DonacionRecepcion, String> colObservaciones = new TableColumn<>("Observaciones");
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        colObservaciones.setPrefWidth(250);

        tabla.getColumns().addAll(colId, colDonante, colResponsable, colObservaciones);

        Button btnNuevo = new Button("Nuevo");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnRecargar = new Button("Recargar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            DonacionRecepcion sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Selecciona una donación primero"); return; }
            abrirFormulario(sel);
        });
        btnEliminar.setOnAction(e -> eliminarSeleccionado());
        btnRecargar.setOnAction(e -> cargar());

        HBox barraBotones = new HBox(10, btnNuevo, btnEditar, btnEliminar, btnRecargar);
        barraBotones.setStyle("-fx-padding: 10px;");

        VBox panel = new VBox(10, barraBotones, tabla);
        panel.setStyle("-fx-padding: 15px;");

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            ObservableList<DonacionRecepcion> datos = FXCollections.observableArrayList(api.listarDonaciones());
            tabla.setItems(datos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
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
        if (sel == null) { mostrarAlerta("Selecciona una donación primero"); return; }
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

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}