package com.ong.desktop.modelos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntregaDefinitiva {

    private Integer idEntrega;
    private Entidad receptor;
    private Articulo articulo;
    private Integer cantidad;
    private Usuario responsable;
    private String motivoCampana;
    private LocalDateTime fechaEntrega;
    private String observaciones;

    public Integer getIdEntrega() { return idEntrega; }
    public void setIdEntrega(Integer idEntrega) { this.idEntrega = idEntrega; }

    public Entidad getReceptor() { return receptor; }
    public void setReceptor(Entidad receptor) { this.receptor = receptor; }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }

    public String getMotivoCampana() { return motivoCampana; }
    public void setMotivoCampana(String motivoCampana) { this.motivoCampana = motivoCampana; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}