package com.ong.desktop.modelos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reserva {

    private Integer idReserva;
    private Articulo articulo;
    private Entidad receptor;
    private Integer cantidad;
    private Usuario responsable;
    private LocalDateTime fechaReserva;
    private LocalDate fechaPrevistaEntrega;
    private String estado;
    private String observaciones;

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public Entidad getReceptor() { return receptor; }
    public void setReceptor(Entidad receptor) { this.receptor = receptor; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }

    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }

    public LocalDate getFechaPrevistaEntrega() { return fechaPrevistaEntrega; }
    public void setFechaPrevistaEntrega(LocalDate fechaPrevistaEntrega) { this.fechaPrevistaEntrega = fechaPrevistaEntrega; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}