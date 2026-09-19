package com.ong.desktop.vistas;

import com.ong.desktop.modelos.HistorialMovimiento;
import com.ong.desktop.servicios.ApiServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PanelHistorial {

    private final ApiServicio api = new ApiServicio();
    private final TableView<HistorialMovimiento> tabla = new TableView<>();
    private final Stage owner;

    public PanelHistorial(Stage owner) {
        this.owner = owner;
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

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            HistorialMovimiento sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Selecciona un movimiento primero"); return; }
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
}