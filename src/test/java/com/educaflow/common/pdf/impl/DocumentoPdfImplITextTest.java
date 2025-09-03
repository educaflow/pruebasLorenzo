package com.educaflow.common.pdf.impl;

import com.educaflow.common.criptografia.EntornoCriptografico;
import com.educaflow.common.pdf.AlmacenClaveFichero;
import com.educaflow.common.pdf.DocumentoPdf;
import com.educaflow.common.pdf.DocumentoPdfFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentoPdfImplITextTest {

    public static final String FILE_CERTIFICADO="../mi_certificado_password_nadanada.p12";
    public static final String PASSWORD_CERTIFICADO="nadanada";
    public static final String FILE_PDF_1b="../prueba_pdf_1b.pdf";
    public static final String FILE_PDF_2b="../prueba_pdf_2b.pdf";
    public static final String SUBJECT="Juan Garcia NIF:1234567Z";
    public static final String ISSUER="Juan Garcia NIF:1234567Z";

    @BeforeAll
    static void initAll() {
        EntornoCriptografico.configure(null);
    }


    @Test
    void getDatos()  {
        String nombreFichero=FILE_PDF_1b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdf = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);

        assertArrayEquals(bytes,documentoPdf.getDatos());
    }

    @Test
    void getFileName() {
        String nombreFichero=FILE_PDF_1b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdf = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);
        assertEquals(nombreFichero,documentoPdf.getFileName());
    }

    @Test
    void getNumeroPaginas() {
        String nombreFichero=FILE_PDF_1b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdf = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);

        assertEquals(1,documentoPdf.getNumeroPaginas());
    }

    @Test
    void getNombreCamposFormulario() {
        String nombreFichero=FILE_PDF_1b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdf = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);

        assertEquals(3,documentoPdf.getNombreCamposFormulario().size());
        assertEquals((List<String>)List.of("campo1","campo2","campo3"),documentoPdf.getNombreCamposFormulario());
    }

    @Test
    void getFirmasPdfNinguna() {
        String nombreFichero=FILE_PDF_1b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdf = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);

        assertEquals(0,documentoPdf.getFirmasPdf().size());
    }

    @Test
    void getFirmasPdf1b() {
        String nombreFichero=FILE_PDF_1b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdfPlantilla = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);
        Map<String,String> mapaCampos= new HashMap<>();
        DocumentoPdf documentoPdfConDatos=documentoPdfPlantilla.setValorCamposFormularioAndFlatten(mapaCampos);

        DocumentoPdf documentoPdfFirmado= firmarPdf(documentoPdfConDatos,FILE_CERTIFICADO,PASSWORD_CERTIFICADO);

        assertEquals(1,documentoPdfFirmado.getFirmasPdf().size());
        assertEquals(true,documentoPdfFirmado.getFirmasPdf().get(0).isCorrecta());
        assertEquals(false,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().isValidoEnListaCertificadosConfiables());
        assertEquals(null,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getTipoEmisorCertificado());
        assertEquals(SUBJECT,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getCnSubject());
        assertEquals(ISSUER,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getCnIssuer());
    }

    @Test
    void getFirmasPdf2b() {
        String nombreFichero=FILE_PDF_2b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdfPlantilla = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);
        Map<String,String> mapaCampos= new HashMap<>();
        DocumentoPdf documentoPdfConDatos=documentoPdfPlantilla.setValorCamposFormularioAndFlatten(mapaCampos);

        DocumentoPdf documentoPdfFirmado= firmarPdf(documentoPdfConDatos,FILE_CERTIFICADO,PASSWORD_CERTIFICADO);

        assertEquals(1,documentoPdfFirmado.getFirmasPdf().size());
        assertEquals(true,documentoPdfFirmado.getFirmasPdf().get(0).isCorrecta());
        assertEquals(false,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().isValidoEnListaCertificadosConfiables());
        assertEquals(null,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getTipoEmisorCertificado());
        assertEquals(SUBJECT,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getCnSubject());
        assertEquals(ISSUER,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getCnIssuer());
    }

    @Test
    void getFirmasPdf1b_2firmas() {
        String nombreFichero=FILE_PDF_1b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdfPlantilla = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);
        Map<String,String> mapaCampos= new HashMap<>();
        DocumentoPdf documentoPdfConDatos=documentoPdfPlantilla.setValorCamposFormularioAndFlatten(mapaCampos);

        DocumentoPdf documentoPdfFirmado= firmarPdf(documentoPdfConDatos,FILE_CERTIFICADO,PASSWORD_CERTIFICADO);
        documentoPdfFirmado= firmarPdf(documentoPdfFirmado,FILE_CERTIFICADO,PASSWORD_CERTIFICADO);

        assertEquals(2,documentoPdfFirmado.getFirmasPdf().size());
        assertEquals(true,documentoPdfFirmado.getFirmasPdf().get(0).isCorrecta());
        assertEquals(false,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().isValidoEnListaCertificadosConfiables());
        assertEquals(null,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getTipoEmisorCertificado());
        assertEquals(SUBJECT,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getCnSubject());
        assertEquals(ISSUER,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getCnIssuer());

        assertEquals(true,documentoPdfFirmado.getFirmasPdf().get(1).isCorrecta());
        assertEquals(false,documentoPdfFirmado.getFirmasPdf().get(1).getDatosCertificado().isValidoEnListaCertificadosConfiables());
        assertEquals(null,documentoPdfFirmado.getFirmasPdf().get(1).getDatosCertificado().getTipoEmisorCertificado());
        assertEquals(SUBJECT,documentoPdfFirmado.getFirmasPdf().get(1).getDatosCertificado().getCnSubject());
        assertEquals(ISSUER,documentoPdfFirmado.getFirmasPdf().get(1).getDatosCertificado().getCnIssuer());
    }

    @Test
    void getFirmasPdf2b_2firmas() {
        String nombreFichero=FILE_PDF_2b;
        byte[] bytes= getBytes(nombreFichero);
        DocumentoPdf documentoPdfPlantilla = DocumentoPdfFactory.getDocumentoPdf(bytes,nombreFichero);
        Map<String,String> mapaCampos= new HashMap<>();
        DocumentoPdf documentoPdfConDatos=documentoPdfPlantilla.setValorCamposFormularioAndFlatten(mapaCampos);

        DocumentoPdf documentoPdfFirmado= firmarPdf(documentoPdfConDatos,FILE_CERTIFICADO,PASSWORD_CERTIFICADO);
        documentoPdfFirmado= firmarPdf(documentoPdfFirmado,FILE_CERTIFICADO,PASSWORD_CERTIFICADO);

        assertEquals(2,documentoPdfFirmado.getFirmasPdf().size());
        assertEquals(true,documentoPdfFirmado.getFirmasPdf().get(0).isCorrecta());
        assertEquals(false,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().isValidoEnListaCertificadosConfiables());
        assertEquals(null,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getTipoEmisorCertificado());
        assertEquals(SUBJECT,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getCnSubject());
        assertEquals(ISSUER,documentoPdfFirmado.getFirmasPdf().get(0).getDatosCertificado().getCnIssuer());

        assertEquals(true,documentoPdfFirmado.getFirmasPdf().get(1).isCorrecta());
        assertEquals(false,documentoPdfFirmado.getFirmasPdf().get(1).getDatosCertificado().isValidoEnListaCertificadosConfiables());
        assertEquals(null,documentoPdfFirmado.getFirmasPdf().get(1).getDatosCertificado().getTipoEmisorCertificado());
        assertEquals(SUBJECT,documentoPdfFirmado.getFirmasPdf().get(1).getDatosCertificado().getCnSubject());
        assertEquals(ISSUER,documentoPdfFirmado.getFirmasPdf().get(1).getDatosCertificado().getCnIssuer());
    }


    @Test
    void firmar() {
    }

    @Test
    void anyadirDocumentoPdf() {
    }

    @Test
    void testAnyadirDocumentoPdf() {
    }


    public byte[] getBytes(String resourcePath)  {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("No se encontró el recurso: " + resourcePath);
            }

            // 2️⃣ Lee todos los bytes
            byte[] bytes = is.readAllBytes();

            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DocumentoPdf firmarPdf(DocumentoPdf documentoPdf,String ficheroCertificado, String passwordCertificado) {


        AlmacenClaveFichero almacenClaveSistemaArchivos = new AlmacenClaveFichero(this.getClass().getResourceAsStream(ficheroCertificado),passwordCertificado);
        byte[] certificadoBytes= getBytes(ficheroCertificado);
        DocumentoPdf documentoPdfFirmado= documentoPdf.firmar(almacenClaveSistemaArchivos,null);



        return documentoPdfFirmado;
    }
}