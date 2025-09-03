package com.educaflow.common.domains;

import javax.persistence.PostLoad;

import com.educaflow.common.pdf.DocumentoPdf;
import com.educaflow.common.pdf.ResultadoFirma;

import java.util.List;
import java.util.ArrayList;

import com.educaflow.common.domains.db.MetaFilePdf;
import com.educaflow.common.domains.db.DatosFirma;

public class MetaFilePdfListener {
    @PostLoad
    private void onPostLoad(MetaFilePdf metaFilePdf) {

        DocumentoPdf documentoPdf = metaFilePdf.getDocumentoPdf();

        List<DatosFirma> datosFirmas = new ArrayList<>();

        for (ResultadoFirma resultadoFirma : documentoPdf.getFirmasPdf()) {
            DatosFirma datosFirma = new DatosFirma();
            datosFirma.setValida(resultadoFirma.isCorrecta() && resultadoFirma.getDatosCertificado().isValidoEnListaCertificadosConfiables());
            datosFirma.setFechaFirma(resultadoFirma.getFechaFirma());
            datosFirma.setCnSubject(resultadoFirma.getDatosCertificado().getCnSubject());
            datosFirma.setCnIssuer(resultadoFirma.getDatosCertificado().getCnIssuer());

            datosFirmas.add(datosFirma);
        }

        metaFilePdf.setDatosFirmas(datosFirmas);


    }
}
