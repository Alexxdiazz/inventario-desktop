package com.ong.desktop.vistas;

import com.ong.desktop.modelos.EntregaDefinitiva;
import com.ong.desktop.servicios.ApiServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PanelEntregas {

    private final ApiServicio api = new ApiServicio();
    private final TableView<EntregaDefinitiva> tabla = new TableView<>();
    private final Stage owner;

    public PanelEntregas(Stage owner) {
        this.owner = owner;
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

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            EntregaDefinitiva sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Selecciona una entrega primero"); return; }
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
                    api.crearEntrega(entrega);
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
}