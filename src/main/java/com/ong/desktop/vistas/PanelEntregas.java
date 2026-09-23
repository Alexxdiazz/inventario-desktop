package com.ong.desktop.vistas;
import com.ong.desktop.modelos.EntregaDefinitiva;
import com.ong.desktop.servicios.ApiServicio;
import com.ong.desktop.servicios.Exportador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PanelEntregas {

    private final ApiServicio api = new ApiServicio();
    private final TableView<EntregaDefinitiva> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;
    private final Label lblContador = new Label();

    private final ObservableList<EntregaDefinitiva> todasLasEntregas = FXCollections.observableArrayList();
    private final TextField txtBuscar = new TextField();

    public PanelEntregas(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        // ===== Breadcrumb =====
        HBox breadcrumb = new HBox(10);
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblRuta = new Label("🏠 / Entregas");
        lblRuta.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d63384;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        breadcrumb.getChildren().addAll(lblRuta, spacer, lblContador);

        // ===== Filtros =====
        txtBuscar.setPromptText("🔍 Buscar por artículo, receptor o motivo...");
        txtBuscar.setPrefWidth(400);
        txtBuscar.setPrefHeight(35);

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px;");
        btnLimpiar.setOnAction(e -> {
            txtBuscar.clear();
            aplicarFiltros();
        });

        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());

        HBox barraFiltros = new HBox(10, txtBuscar, btnLimpiar);
        barraFiltros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ===== Botones =====
        Button btnNuevo = new Button("➕ Nueva");
        Button btnEditar = new Button("✏ Editar");
        Button btnEliminar = new Button("🗑 Eliminar");
        Button btnRecargar = new Button("🔄 Recargar");
        Button btnExportar = new Button("📊 Exportar");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            EntregaDefinitiva sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Seleccioná una entrega primero"); return; }
            abrirFormulario(sel);
        });
        btnEliminar.setOnAction(e -> eliminarSeleccionado());
        btnRecargar.setOnAction(e -> cargar());
        btnExportar.setOnAction(e -> exportar());

        HBox barraBotones;
        if ("CONSULTA".equals(rol)) {
            barraBotones = new HBox(8, btnRecargar, btnExportar);
        } else {
            barraBotones = new HBox(8, btnNuevo, btnEditar, btnEliminar, btnRecargar, btnExportar);
        }
        barraBotones.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ===== Tabla =====
        TableColumn<EntregaDefinitiva, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idEntrega"));
        colId.setPrefWidth(50);

        TableColumn<EntregaDefinitiva, Object> colReceptor = new TableColumn<>("Receptor");
        colReceptor.setCellValueFactory(new PropertyValueFactory<>("receptor"));
        colReceptor.setPrefWidth(180);

        TableColumn<EntregaDefinitiva, Object> colArticulo = new TableColumn<>("Artículo");
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        colArticulo.setPrefWidth(180);

        TableColumn<EntregaDefinitiva, Integer> colCantidad = new TableColumn<>("Cant.");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setPrefWidth(60);

        TableColumn<EntregaDefinitiva, Object> colResponsable = new TableColumn<>("Responsable");
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        colResponsable.setPrefWidth(160);

        TableColumn<EntregaDefinitiva, String> colMotivo = new TableColumn<>("Motivo / Campaña");
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivoCampana"));
        colMotivo.setPrefWidth(180);

        tabla.getColumns().addAll(colId, colReceptor, colArticulo, colCantidad, colResponsable, colMotivo);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        panel.getChildren().addAll(breadcrumb, barraFiltros, barraBotones, tabla);

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            List<EntregaDefinitiva> lista = api.listarEntregas();
            todasLasEntregas.setAll(lista);
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";

        List<EntregaDefinitiva> filtrados = todasLasEntregas.stream()
                .filter(en -> {
                    if (texto.isEmpty()) return true;
                    String art = en.getArticulo() != null && en.getArticulo().getNombre() != null
                            ? en.getArticulo().getNombre().toLowerCase() : "";
                    String rec = en.getReceptor() != null && en.getReceptor().getNombreCompleto() != null
                            ? en.getReceptor().getNombreCompleto().toLowerCase() : "";
                    String mot = en.getMotivoCampana() != null ? en.getMotivoCampana().toLowerCase() : "";
                    return art.contains(texto) || rec.contains(texto) || mot.contains(texto);
                })
                .collect(Collectors.toList());

        tabla.setItems(FXCollections.observableArrayList(filtrados));
        lblContador.setText("Mostrando " + filtrados.size() + " entregas");
    }

    private void abrirFormulario(EntregaDefinitiva existente) {
        FormularioEntrega.mostrar(owner, existente, entrega -> {
            try {
                if (entrega.getIdEntrega() == null) {
                    EntregaDefinitiva creada = api.crearEntrega(entrega);
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
        if (sel == null) { mostrarAlerta("Seleccioná una entrega primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("¿Eliminar esta entrega?");
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
            String[] encabezados = {"ID", "Receptor", "Artículo", "Cantidad", "Responsable", "Motivo/Campaña"};
            List<String[]> filas = new ArrayList<>();
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

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}