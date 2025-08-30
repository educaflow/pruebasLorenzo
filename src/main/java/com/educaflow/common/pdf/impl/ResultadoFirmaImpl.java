package com.educaflow.common.pdf.impl;

import com.educaflow.common.pdf.DatosCertificado;
import com.educaflow.common.pdf.ResultadoFirma;
import com.itextpdf.signatures.PdfPKCS7;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class ResultadoFirmaImpl implements ResultadoFirma {

    private final DatosCertificado datosCertificado;
    private final boolean correcta;
    public ResultadoFirmaImpl(PdfPKCS7 pdfPKCS7, KeyStore trustedKeyStore) {

        try {
            X509Certificate certificate = pdfPKCS7.getSigningCertificate();
            this.datosCertificado = new DatosCertificadoImpl(certificate, trustedKeyStore);

            correcta = pdfPKCS7.verifySignatureIntegrityAndAuthenticity();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isCorrecta() {
        return correcta;
    }

    @Override
    public DatosCertificado getDatosCertificado() {
        return datosCertificado;
    }
}
