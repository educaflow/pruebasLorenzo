package com.educaflow.common.pdf.impl;

import com.educaflow.common.pdf.DatosCertificado;
import com.educaflow.common.pdf.ResultadoFirma;
import com.itextpdf.signatures.PdfPKCS7;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Calendar;

public class ResultadoFirmaImpl implements ResultadoFirma {

    private final DatosCertificado datosCertificado;
    private LocalDateTime fechaFirma;
    private final boolean correcta;

    public ResultadoFirmaImpl(PdfPKCS7 pdfPKCS7, KeyStore trustedKeyStore) {

        try {
            X509Certificate certificate = pdfPKCS7.getSigningCertificate();
            this.datosCertificado = new DatosCertificadoImpl(certificate, trustedKeyStore);
            Calendar signDateCalendar=pdfPKCS7.getSignDate();
            fechaFirma=toLocalDateTime(signDateCalendar);

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
    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    @Override
    public DatosCertificado getDatosCertificado() {
        return datosCertificado;
    }

    private LocalDateTime toLocalDateTime(Calendar calendar) {
        LocalDateTime localDateTime = LocalDateTime.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MILLISECOND) * 1_000_000
        );

        return localDateTime;
    }

}
