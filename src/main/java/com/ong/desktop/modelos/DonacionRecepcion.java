package com.ong.desktop.modelos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DonacionRecepcion {

    private Integer idDonacion;
    private Entidad donante;
    private Usuario responsableRecepcion;
    private LocalDateTime fechaRecepcion;
    private String observaciones;

    public Integer getIdDonacion() { return idDonacion; }
    public void setIdDonacion(Integer idDonacion) { this.idDonacion = idDonacion; }

    public Entidad getDonante() { return donante; }
    public void setDonante(Entidad donante) { this.donante = donante; }

    public Usuario getResponsableRecepcion() { return responsableRecepcion; }
    public void setResponsableRecepcion(Usuario responsableRecepcion) { this.responsableRecepcion = responsableRecepcion; }

    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}