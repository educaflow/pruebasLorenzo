package com.educaflow.common.pdf;


import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DocumentoPdf {


    public List<String> getNombreCamposFormulario();
    public Map<String,DatosCertificado> getFirmasPdf();    
    public int getNumeroPaginas();    
    
    
    
    public DocumentoPdf setValorCamposFormulario(Map<String,String> valores);
    public DocumentoPdf firmar(AlmacenClave almacenClave,CampoFirma campoFirma);
    public DocumentoPdf anyadirDocumentoPdf(DocumentoPdf documentoPdf);
    
    public byte[] getDatos();
}