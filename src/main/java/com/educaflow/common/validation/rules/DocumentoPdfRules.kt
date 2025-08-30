package com.educaflow.common.validation.rules

import com.axelor.meta.db.MetaFile
import com.educaflow.apps.expedientes.tiposexpedientes.shared.TipoExpedienteUtil
import com.educaflow.common.pdf.DocumentoPdf
import com.educaflow.common.pdf.DocumentoPdfUtil
import com.educaflow.common.validation.engine.ValidationRule
import com.educaflow.common.validation.messages.BusinessMessages
import java.time.LocalDate

class DocumentoPdfFirmaValida : ValidationRule {
    override fun validate(value: Any?, bean: Any): BusinessMessages? {
        val metaFile: MetaFile = if (value is MetaFile) value else return null

        val documentoPdf: DocumentoPdf

        try {
            documentoPdf = TipoExpedienteUtil.getDocumentoPdfFromMetaFile(metaFile);
        } catch (e: Exception) {
            return BusinessMessages.single("El archivo no es un documento PDF correcto");
        }


        if (DocumentoPdfUtil.isValidasTodasFirmas(documentoPdf)==false) {
            return BusinessMessages.single("Las firmas del documento debe ser válidas");
        }

        return null;
    }
}

