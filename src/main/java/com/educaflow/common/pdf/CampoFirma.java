package com.educaflow.common.pdf;

/**
 *
 * @author logongas
 */
public class CampoFirma {

    public final static int DEFAULT_FONT_SIZE=8;

    private final String mensaje;
    private final Rectangulo rectanguloMensaje;
    private final int fontSize;
    private final int numeroPagina;

    public CampoFirma(String mensaje, Rectangulo rectanguloMensaje, int fontSize, int numeroPagina) {
        this.mensaje = mensaje;
        this.rectanguloMensaje = rectanguloMensaje;
        this.fontSize = 12;
        this.numeroPagina = numeroPagina;
    }

    public CampoFirma(Rectangulo rectanguloMensaje, int numeroPagina) {
        this.mensaje = null;
        this.rectanguloMensaje = rectanguloMensaje;
        this.fontSize = DEFAULT_FONT_SIZE;
        this.numeroPagina = numeroPagina;
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
    public int getNumeroPagina() {
        return numeroPagina;
    }
    
    
    
}
