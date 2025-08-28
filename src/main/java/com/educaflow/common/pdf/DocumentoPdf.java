package com.educaflow.common.pdf;


import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DocumentoPdf {


    List<String> getNombreCamposFormulario();
    Map<String,DatosCertificado> getFirmasPdf();
    int getNumeroPaginas();
    String getFileName();
    
    
    DocumentoPdf setValorCamposFormularioAndFlatten(Map<String,String> valores);
    DocumentoPdf firmar(AlmacenClave almacenClave,CampoFirma campoFirma);
    DocumentoPdf anyadirDocumentoPdf(DocumentoPdf documentoPdf);
    DocumentoPdf anyadirDocumentoPdf(DocumentoPdf documentoPdf,String fileName);

    byte[] getDatos();
}