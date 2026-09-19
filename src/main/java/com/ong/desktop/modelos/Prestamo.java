package com.ong.desktop.modelos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prestamo {

    private Integer idPrestamo;
    private Entidad receptor;
    private Articulo articulo;
    private Integer cantidad;
    private Usuario responsable;
    private LocalDateTime fechaPrestamo;
    private LocalDate fechaPrevistaDevolucion;
    private LocalDateTime fechaRealDevolucion;
    private String estadoDevolucion;
    private Usuario responsableRecepcionDevolucion;
    private String observaciones;

    public Integer getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(Integer idPrestamo) { this.idPrestamo = idPrestamo; }

    public Entidad getReceptor() { return receptor; }
    public void setReceptor(Entidad receptor) { this.receptor = receptor; }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }

    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDateTime fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaPrevistaDevolucion() { return fechaPrevistaDevolucion; }
    public void setFechaPrevistaDevolucion(LocalDate fechaPrevistaDevolucion) { this.fechaPrevistaDevolucion = fechaPrevistaDevolucion; }

    public LocalDateTime getFechaRealDevolucion() { return fechaRealDevolucion; }
    public void setFechaRealDevolucion(LocalDateTime fechaRealDevolucion) { this.fechaRealDevolucion = fechaRealDevolucion; }

    public String getEstadoDevolucion() { return estadoDevolucion; }
    public void setEstadoDevolucion(String estadoDevolucion) { this.estadoDevolucion = estadoDevolucion; }

    public Usuario getResponsableRecepcionDevolucion() { return responsableRecepcionDevolucion; }
    public void setResponsableRecepcionDevolucion(Usuario responsableRecepcionDevolucion) { this.responsableRecepcionDevolucion = responsableRecepcionDevolucion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}