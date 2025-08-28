package com.educaflow.common.pdf;

/**
 *
 * @author logongas
 */
public class CampoFirma {
    
    private final String mensaje;
    private final Rectangulo rectanguloMensaje;
    private final int fontSize;
    private final int numerpoPagina;

    public CampoFirma(String mensaje, Rectangulo rectanguloMensaje, int fontSize, int numerpoPagina) {
        this.mensaje = mensaje;
        this.rectanguloMensaje = rectanguloMensaje;
        this.fontSize = fontSize;
        this.numerpoPagina = numerpoPagina;
    }

    /**
     * @return the mensaje
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * @return the rectanguloMensaje
     */
    public Rectangulo getRectanguloMensaje() {
        return rectanguloMensaje;
    }

    /**
     * @return the fontSize
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * @return the numerpoPagina
     */
    public int getNumerpoPagina() {
        return numerpoPagina;
    }
    
    
    
}
