package com.ong.desktop.vistas;
import com.ong.desktop.modelos.Articulo;
import com.ong.desktop.modelos.Prestamo;
import com.ong.desktop.servicios.ApiServicio;
import com.ong.desktop.servicios.Exportador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PanelPrestamos {

    private final ApiServicio api = new ApiServicio();
    private final TableView<Prestamo> tabla = new TableView<>();
    private final Stage owner;
    private final String rol;

    private final ObservableList<Prestamo> todosLosPrestamos = FXCollections.observableArrayList();

    private final TextField txtBuscar = new TextField();
    private final ComboBox<String> cmbEstado = new ComboBox<>();
    private final Label lblContador = new Label();

    public PanelPrestamos(Stage owner, String rol) {
        this.owner = owner;
        this.rol = rol;
    }

    public VBox construir() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #fdf6fa;");

        // ===== Breadcrumb + Contador =====
        HBox breadcrumb = new HBox(10);
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblRuta = new Label("🏠 / Préstamos");
        lblRuta.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d63384;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblContador.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        breadcrumb.getChildren().addAll(lblRuta, spacer, lblContador);

        // ===== Barra de filtros =====
        txtBuscar.setPromptText("🔍 Buscar por artículo, receptor o responsable...");
        txtBuscar.setPrefWidth(320);
        txtBuscar.setPrefHeight(35);

        cmbEstado.getItems().addAll("Todos", "ACTIVO", "DEVUELTO", "VENCIDO");
        cmbEstado.setValue("Todos");
        cmbEstado.setPrefWidth(140);
        cmbEstado.setPrefHeight(35);

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px;");
        btnLimpiar.setOnAction(e -> {
            txtBuscar.clear();
            cmbEstado.setValue("Todos");
            aplicarFiltros();
        });

        // Escuchar cambios
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbEstado.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());

        HBox barraFiltros = new HBox(10, txtBuscar, cmbEstado, btnLimpiar);
        barraFiltros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ===== Botones CRUD =====
        Button btnNuevo = new Button("➕ Nuevo");
        Button btnEditar = new Button("✏ Editar");
        Button btnEliminar = new Button("🗑 Eliminar");
        Button btnRecargar = new Button("🔄 Recargar");
        Button btnDevolver = new Button("↩ Devolver");
        Button btnExportar = new Button("📊 Exportar");

        btnDevolver.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 8px 14px; -fx-font-weight: bold;");

        btnNuevo.setOnAction(e -> abrirFormulario(null));
        btnEditar.setOnAction(e -> {
            Prestamo sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) { mostrarAlerta("Seleccioná un préstamo primero"); return; }
            abrirFormulario(sel);
        });
        btnEliminar.setOnAction(e -> eliminarSeleccionado());
        btnRecargar.setOnAction(e -> cargar());
        btnDevolver.setOnAction(e -> registrarDevolucion());
        btnExportar.setOnAction(e -> exportar());

        HBox barraBotones;
        if ("CONSULTA".equals(rol)) {
            barraBotones = new HBox(8, btnRecargar, btnExportar);
        } else {
            barraBotones = new HBox(8, btnNuevo, btnEditar, btnEliminar, btnDevolver, btnRecargar, btnExportar);
        }
        barraBotones.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ===== Tabla =====
        TableColumn<Prestamo, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        colId.setPrefWidth(50);

        TableColumn<Prestamo, Object> colReceptor = new TableColumn<>("Receptor");
        colReceptor.setCellValueFactory(new PropertyValueFactory<>("receptor"));
        colReceptor.setPrefWidth(180);

        TableColumn<Prestamo, Object> colArticulo = new TableColumn<>("Artículo");
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

        TableColumn<Prestamo, Object> colDevuelto = new TableColumn<>("Estado");
        colDevuelto.setCellValueFactory(new PropertyValueFactory<>("fechaRealDevolucion"));
        colDevuelto.setPrefWidth(120);

        tabla.getColumns().addAll(colId, colReceptor, colArticulo, colCantidad, colResponsable, colFecha, colDevuelto);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        panel.getChildren().addAll(breadcrumb, barraFiltros, barraBotones, tabla);

        cargar();
        return panel;
    }

    private void cargar() {
        try {
            List<Prestamo> lista = api.listarPrestamos();
            todosLosPrestamos.setAll(lista);
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar con la API:\n" + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
        String estado = cmbEstado.getValue();

        LocalDate hoy = LocalDate.now();

        List<Prestamo> filtrados = todosLosPrestamos.stream()
                .filter(p -> {
                    // Filtro de texto
                    if (!texto.isEmpty()) {
                        String art = p.getArticulo() != null && p.getArticulo().getNombre() != null
                                ? p.getArticulo().getNombre().toLowerCase() : "";
                        String rec = p.getReceptor() != null && p.getReceptor().getNombreCompleto() != null
                                ? p.getReceptor().getNombreCompleto().toLowerCase() : "";
                        String resp = p.getResponsable() != null && p.getResponsable().getNombre() != null
                                ? p.getResponsable().getNombre().toLowerCase() : "";
                        if (!art.contains(texto) && !rec.contains(texto) && !resp.contains(texto)) {
                            return false;
                        }
                    }
                    // Filtro de estado
                    if (estado != null && !estado.equals("Todos")) {
                        boolean devuelto = p.getFechaRealDevolucion() != null;
                        boolean vencido = !devuelto
                                && p.getFechaPrevistaDevolucion() != null
                                && p.getFechaPrevistaDevolucion().isBefore(hoy);

                        switch (estado) {
                            case "ACTIVO":   if (devuelto) return false; break;
                            case "DEVUELTO": if (!devuelto) return false; break;
                            case "VENCIDO":  if (!vencido) return false; break;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        tabla.setItems(FXCollections.observableArrayList(filtrados));
        lblContador.setText("Mostrando " + filtrados.size() + " préstamos");
    }

    private void abrirFormulario(Prestamo existente) {
        FormularioPrestamo.mostrar(owner, existente, prestamo -> {
            try {
                if (prestamo.getIdPrestamo() == null) {
                    Prestamo creado = api.crearPrestamo(prestamo);
                    if (creado.getArticulo() != null && creado.getResponsable() != null) {
                        String receptor = creado.getReceptor() != null ? creado.getReceptor().getNombreCompleto() : "sin receptor";
                        api.registrarMovimiento(
                                creado.getArticulo().getIdArticulo(),
                                creado.getResponsable().getIdUsuario(),
                                "PRESTAMO",
                                "DISPONIBLE",
                                "PRESTADO",
                                "Préstamo a " + receptor + " (cantidad: " + creado.getCantidad() + ")"
                        );
                    }
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
        if (sel == null) { mostrarAlerta("Seleccioná un préstamo primero"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setHeaderText("¿Eliminar este préstamo?");
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

    private void registrarDevolucion() {
        Prestamo sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Seleccioná un préstamo primero");
            return;
        }
        if (sel.getFechaRealDevolucion() != null) {
            mostrarAlerta("Este préstamo ya fue devuelto.\nFecha: " + sel.getFechaRealDevolucion());
            return;
        }

        FormularioDevolucion.mostrar(owner, sel, prestamo -> {
            try {
                api.actualizarPrestamo(prestamo.getIdPrestamo(), prestamo);
                if (prestamo.getArticulo() != null) {
                    Articulo articulo = prestamo.getArticulo();
                    articulo.setEstadoActual("DISPONIBLE");
                    api.actualizarArticulo(articulo.getIdArticulo(), articulo);

                    if (prestamo.getResponsableRecepcionDevolucion() != null) {
                        String receptor = prestamo.getReceptor() != null ? prestamo.getReceptor().getNombreCompleto() : "sin receptor";
                        api.registrarMovimiento(
                                articulo.getIdArticulo(),
                                prestamo.getResponsableRecepcionDevolucion().getIdUsuario(),
                                "DEVOLUCION",
                                "PRESTADO",
                                "DISPONIBLE",
                                "Devolución de " + receptor + " (estado: " + prestamo.getEstadoDevolucion() + ")"
                        );
                    }
                }
                cargar();
                mostrarAlerta("Devolución registrada correctamente.\nEl artículo volvió a estar DISPONIBLE.");
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Error al registrar devolución:\n" + ex.getMessage());
            }
        });
    }

    private void exportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte de préstamos");
        fc.setInitialFileName("prestamos");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;
        try {
            String[] encabezados = {"ID", "Receptor", "Artículo", "Cantidad", "Responsable", "Fecha prevista"};
            List<String[]> filas = new ArrayList<>();
            for (Prestamo p : tabla.getItems()) {
                filas.add(new String[]{
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
                Exportador.exportarPDF(ruta, "Reporte de Préstamos", encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, "Préstamos", encabezados, filas);
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