package com.ong.desktop.vistas;
import com.ong.desktop.modelos.Articulo;
import com.ong.desktop.servicios.ApiServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TableRow;

public class PanelArticulos {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Articulo> tabla = new TableView<>();
    private final Stage owner;

    public PanelArticulos(Stage owner) {
        this.owner = owner;
    }

    public VBox construir() {
        TableColumn<Articulo, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idArticulo"));
        colId.setPrefWidth(60);

        TableColumn<Articulo, String> colCodigo = new TableColumn<>("Codigo");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoInventario"));
        colCodigo.setPrefWidth(120);

        TableColumn<Articulo, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Articulo, Integer> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setPrefWidth(80);

        TableColumn<Articulo, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoActual"));
        colEstado.setPrefWidth(120);

        TableColumn<Articulo, String> colDescripcion = new TableColumn<>("Descripcion");
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDescripcion.setPrefWidth(180);

        TableColumn<Articulo, String> colConservacion = new TableColumn<>("Conservacion");
        colConservacion.setCellValueFactory(new PropertyValueFactory<>("estadoConservacion"));
        colConservacion.setPrefWidth(120);

        tabla.getColumns().addAll(colId, colCodigo, colNombre, colCantidad, colEstado, colDescripcion, colConservacion);
                // Pintar de rojo las filas de artículos con stock bajo
                // Pintar de rojo las filas de artículos con stock bajo
        tabla.setRowFactory(tv -> new TableRow<Articulo>() {
            @Override
            protected void updateItem(Articulo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getStockMinimo() != null 
                        && item.getCantidad() != null
                        && item.getCantidad() <= item.getStockMinimo()) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });

        Button btnNuevo = new Button("Nuevo");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");
        Button btnRecargar = new Button("Recargar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Articulo sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Selecciona un articulo primero"); return; }
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
            ObservableList<Articulo> datos = FXCollections.observableArrayList(api.listarArticulos());
            tabla.setItems(datos);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void abrirFormulario(Articulo existente) {
        FormularioArticulo.mostrar(owner, existente, articulo -> {
            try {
                if (articulo.getIdArticulo() == null) {
                    api.crearArticulo(articulo);
                } else {
                    api.actualizarArticulo(articulo.getIdArticulo(), articulo);
                }
                cargar();
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al guardar:\n" + ex.getMessage());
            }
        });
    }

    private void eliminarSeleccionado() {
        Articulo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAlerta("Selecciona un articulo primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("Eliminar este articulo?");
        confirmacion.setContentText(sel.getNombre());
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    api.borrarArticulo(sel.getIdArticulo());
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
