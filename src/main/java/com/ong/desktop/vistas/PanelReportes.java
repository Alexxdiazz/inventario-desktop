package com.ong.desktop.vistas;

import com.ong.desktop.modelos.*;
import com.ong.desktop.servicios.ApiServicio;
import com.ong.desktop.servicios.Exportador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PanelReportes {

    private final ApiServicio api = new ApiServicio();
    private final Stage owner;

    public PanelReportes(Stage owner) {
        this.owner = owner;
    }

    public VBox construir() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f8f9fa;");

        // Encabezado
        Label titulo = new Label("Reportes");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 24));
        titulo.setStyle("-fx-text-fill: #d63384;");

        Label subtitulo = new Label("Generá reportes específicos del sistema");
        subtitulo.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");

        // Sección: Stock
        Label lblStock = new Label("STOCK");
        lblStock.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6c757d;");

        Button btnStockCategoria = crearBotonReporte("Stock por categoría");
        btnStockCategoria.setOnAction(e -> generarStockPorCategoria());

        Button btnStockUbicacion = crearBotonReporte("Stock por ubicación");
        btnStockUbicacion.setOnAction(e -> generarStockPorUbicacion());

        HBox filaStock = new HBox(15, btnStockCategoria, btnStockUbicacion);

        // Sección: Préstamos
        Label lblPrestamos = new Label("PRÉSTAMOS");
        lblPrestamos.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6c757d; -fx-padding: 15px 0 0 0;");

        Button btnPrestamosVigentes = crearBotonReporte("Préstamos vigentes");
        btnPrestamosVigentes.setOnAction(e -> generarPrestamosVigentes());

        Button btnPrestamosVencidos = crearBotonReporte("Préstamos vencidos");
        btnPrestamosVencidos.setOnAction(e -> generarPrestamosVencidos());

        HBox filaPrestamos = new HBox(15, btnPrestamosVigentes, btnPrestamosVencidos);

        // Sección: Historial
        Label lblHistorial = new Label("HISTORIAL");
        lblHistorial.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6c757d; -fx-padding: 15px 0 0 0;");

        Button btnHistorialArticulo = crearBotonReporte("Historial por artículo");
        btnHistorialArticulo.setOnAction(e -> generarHistorialPorArticulo());

        HBox filaHistorial = new HBox(15, btnHistorialArticulo);

        panel.getChildren().addAll(
                titulo, subtitulo,
                lblStock, filaStock,
                lblPrestamos, filaPrestamos,
                lblHistorial, filaHistorial
        );

        return panel;
    }

    private Button crearBotonReporte(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(240);
        btn.setPrefHeight(70);
        btn.setStyle("-fx-background-color: white; -fx-border-color: #d63384; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-text-fill: #d63384; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #d63384; -fx-text-fill: white; -fx-border-color: #d63384; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-border-color: #d63384; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-text-fill: #d63384; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;"));
        return btn;
    }

    // ==================== REPORTE: STOCK POR CATEGORÍA ====================
    private void generarStockPorCategoria() {
        try {
            List<Articulo> articulos = api.listarArticulos();
            List<Categoria> categorias = api.listarCategorias();

            Map<Integer, String> nombreCategoria = categorias.stream()
                    .collect(Collectors.toMap(Categoria::getIdCategoria, Categoria::getNombre));

            // Agrupar
            Map<Integer, List<Articulo>> porCategoria = articulos.stream()
                    .filter(a -> a.getIdCategoria() != null)
                    .collect(Collectors.groupingBy(Articulo::getIdCategoria));

            String[] encabezados = {"Categoría", "Cantidad de artículos", "Stock total"};
            List<String[]> filas = new ArrayList<>();
            for (Map.Entry<Integer, List<Articulo>> entry : porCategoria.entrySet()) {
                String nombre = nombreCategoria.getOrDefault(entry.getKey(), "Sin categoría");
                int cantArticulos = entry.getValue().size();
                int stockTotal = entry.getValue().stream()
                        .mapToInt(a -> a.getCantidad() != null ? a.getCantidad() : 0)
                        .sum();
                filas.add(new String[]{nombre, String.valueOf(cantArticulos), String.valueOf(stockTotal)});
            }

            exportar("stock_por_categoria", "Stock por Categoría", encabezados, filas);
        } catch (Exception e) {
            mostrarError("Error al generar reporte:\n" + e.getMessage());
        }
    }

    // ==================== REPORTE: STOCK POR UBICACIÓN ====================
    private void generarStockPorUbicacion() {
        try {
            List<Articulo> articulos = api.listarArticulos();
            List<Ubicacion> ubicaciones = api.listarUbicaciones();

            Map<Integer, String> nombreUbicacion = ubicaciones.stream()
                    .collect(Collectors.toMap(Ubicacion::getIdUbicacion, Ubicacion::getNombre));

            Map<Integer, List<Articulo>> porUbicacion = articulos.stream()
                    .filter(a -> a.getIdUbicacion() != null)
                    .collect(Collectors.groupingBy(Articulo::getIdUbicacion));

            String[] encabezados = {"Ubicación", "Cantidad de artículos", "Stock total"};
            List<String[]> filas = new ArrayList<>();
            for (Map.Entry<Integer, List<Articulo>> entry : porUbicacion.entrySet()) {
                String nombre = nombreUbicacion.getOrDefault(entry.getKey(), "Sin ubicación");
                int cantArticulos = entry.getValue().size();
                int stockTotal = entry.getValue().stream()
                        .mapToInt(a -> a.getCantidad() != null ? a.getCantidad() : 0)
                        .sum();
                filas.add(new String[]{nombre, String.valueOf(cantArticulos), String.valueOf(stockTotal)});
            }

            exportar("stock_por_ubicacion", "Stock por Ubicación", encabezados, filas);
        } catch (Exception e) {
            mostrarError("Error al generar reporte:\n" + e.getMessage());
        }
    }

    // ==================== REPORTE: PRÉSTAMOS VIGENTES ====================
    private void generarPrestamosVigentes() {
        try {
            List<Prestamo> prestamos = api.listarPrestamos();

            String[] encabezados = {"ID", "Artículo", "Receptor", "Cantidad", "Fecha préstamo", "Fecha prevista devolución"};
            List<String[]> filas = new ArrayList<>();
            for (Prestamo p : prestamos) {
                if (p.getFechaRealDevolucion() == null) {   // Solo los no devueltos
                    filas.add(new String[]{
                            String.valueOf(p.getIdPrestamo()),
                            p.getArticulo() != null ? p.getArticulo().getNombre() : "",
                            p.getReceptor() != null ? p.getReceptor().getNombreCompleto() : "",
                            String.valueOf(p.getCantidad()),
                            p.getFechaPrestamo() != null ? p.getFechaPrestamo().toString() : "",
                            p.getFechaPrevistaDevolucion() != null ? p.getFechaPrevistaDevolucion().toString() : ""
                    });
                }
            }

            exportar("prestamos_vigentes", "Préstamos Vigentes", encabezados, filas);
        } catch (Exception e) {
            mostrarError("Error al generar reporte:\n" + e.getMessage());
        }
    }

    // ==================== REPORTE: PRÉSTAMOS VENCIDOS ====================
    private void generarPrestamosVencidos() {
        try {
            List<Prestamo> prestamos = api.listarPrestamos();
            LocalDate hoy = LocalDate.now();

            String[] encabezados = {"ID", "Artículo", "Receptor", "Fecha préstamo", "Fecha prevista", "Días de atraso"};
            List<String[]> filas = new ArrayList<>();
            for (Prestamo p : prestamos) {
                if (p.getFechaRealDevolucion() == null
                        && p.getFechaPrevistaDevolucion() != null
                        && p.getFechaPrevistaDevolucion().isBefore(hoy)) {
                    long diasAtraso = java.time.temporal.ChronoUnit.DAYS.between(p.getFechaPrevistaDevolucion(), hoy);
                    filas.add(new String[]{
                            String.valueOf(p.getIdPrestamo()),
                            p.getArticulo() != null ? p.getArticulo().getNombre() : "",
                            p.getReceptor() != null ? p.getReceptor().getNombreCompleto() : "",
                            p.getFechaPrestamo() != null ? p.getFechaPrestamo().toString() : "",
                            p.getFechaPrevistaDevolucion().toString(),
                            String.valueOf(diasAtraso)
                    });
                }
            }

            exportar("prestamos_vencidos", "Préstamos Vencidos", encabezados, filas);
        } catch (Exception e) {
            mostrarError("Error al generar reporte:\n" + e.getMessage());
        }
    }

    // ==================== REPORTE: HISTORIAL POR ARTÍCULO ====================
    private void generarHistorialPorArticulo() {
        try {
            // 1. Pedir al usuario que elija un artículo
            List<Articulo> articulos = api.listarArticulos();
            ChoiceDialog<Articulo> dialog = new ChoiceDialog<>(null, articulos);
            dialog.setTitle("Elegir artículo");
            dialog.setHeaderText("¿De qué artículo querés el historial?");
            dialog.setContentText("Artículo:");
            dialog.initOwner(owner);

            java.util.Optional<Articulo> resultado = dialog.showAndWait();
            if (resultado.isEmpty() || resultado.get() == null) return;

            Articulo elegido = resultado.get();

            // 2. Filtrar historial
            List<HistorialMovimiento> movimientos = api.listarHistorial();
            String[] encabezados = {"ID", "Fecha", "Usuario", "Operación", "Estado anterior", "Estado nuevo", "Descripción"};
            List<String[]> filas = new ArrayList<>();
            for (HistorialMovimiento h : movimientos) {
                if (h.getArticulo() != null && h.getArticulo().getIdArticulo().equals(elegido.getIdArticulo())) {
                    filas.add(new String[]{
                            String.valueOf(h.getIdMovimiento()),
                            h.getFechaRegistro() != null ? h.getFechaRegistro().toString() : "",
                            h.getUsuario() != null ? h.getUsuario().getNombre() : "",
                            h.getTipoOperacion() != null ? h.getTipoOperacion() : "",
                            h.getEstadoAnterior() != null ? h.getEstadoAnterior() : "",
                            h.getEstadoNuevo() != null ? h.getEstadoNuevo() : "",
                            h.getDescripcionMovimiento() != null ? h.getDescripcionMovimiento() : ""
                    });
                }
            }

            String nombreArchivo = "historial_" + elegido.getCodigoInventario().replaceAll("[^a-zA-Z0-9]", "_");
            exportar(nombreArchivo, "Historial de: " + elegido.getNombre(), encabezados, filas);
        } catch (Exception e) {
            mostrarError("Error al generar reporte:\n" + e.getMessage());
        }
    }

    // ==================== EXPORTAR ====================
    private void exportar(String nombreBase, String titulo, String[] encabezados, List<String[]> filas) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte");
        fc.setInitialFileName(nombreBase);
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File archivo = fc.showSaveDialog(owner);
        if (archivo == null) return;

        try {
            String ruta = archivo.getAbsolutePath();
            if (ruta.toLowerCase().endsWith(".pdf")) {
                Exportador.exportarPDF(ruta, titulo, encabezados, filas);
            } else {
                if (!ruta.toLowerCase().endsWith(".xlsx")) ruta += ".xlsx";
                Exportador.exportarExcel(ruta, titulo, encabezados, filas);
            }
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText("Reporte generado");
            ok.setContentText("Archivo: " + ruta + "\nFilas: " + filas.size());
            ok.showAndWait();
        } catch (Exception e) {
            mostrarError("Error al exportar:\n" + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setHeaderText("Error");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}