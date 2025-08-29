package com.educaflow.apps.expedientes.tiposexpedientes.shared;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.educaflow.common.pdf.DocumentoPdf;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TipoExpedienteUtil {

    public static MetaFile getMetaFileFromDocumentoPdf(DocumentoPdf documentoPdf) {
        try {
            byte[] pdfBytes = documentoPdf.getDatos();
            InputStream inputStream = new ByteArrayInputStream(pdfBytes);

            final MetaFile metaFile = new MetaFile();
            metaFile.setFileName(documentoPdf.getFileName());
            metaFile.setFileType("application/pdf");

            Beans.get(com.axelor.meta.MetaFiles.class).upload(inputStream, metaFile);

            return metaFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
