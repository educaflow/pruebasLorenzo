package com.educaflow.common.pdf;

import java.awt.*;

/**
 *
 * @author logongas
 */
public class CampoFirma {

    public final static int DEFAULT_FONT_SIZE=8;
    public final static int DEFAULT_NUMERO_PAGINA=-1;

    private String mensaje=null;
    private Rectangulo rectanguloMensaje=null;
    private int fontSize=DEFAULT_FONT_SIZE;
    private int numeroPagina=DEFAULT_NUMERO_PAGINA;
    private byte[] image=null;

    public CampoFirma(Rectangulo rectanguloMensaje) {
        this.rectanguloMensaje=rectanguloMensaje;
    }


    public CampoFirma setMensaje(String mensaje) {
        this.mensaje=mensaje;
        return this;
    }

    public CampoFirma setFontSize(int fontSize) {
        this.fontSize=fontSize;
        return this;
    }

    public CampoFirma setNumeroPagina(int numeroPagina) {
        this.numeroPagina=numeroPagina;
        return this;
    }
    public CampoFirma setRectanguloMensaje(Rectangulo rectanguloMensaje) {
        this.rectanguloMensaje=rectanguloMensaje;
        return this;
    }

    public CampoFirma setImage(byte[] image) {
        this.image=image;
        return this;
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

    public byte[] getImage() {
        return image;
    }
    
    
}
