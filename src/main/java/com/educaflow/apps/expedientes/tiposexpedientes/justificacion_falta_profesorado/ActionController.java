package com.educaflow.apps.expedientes.tiposexpedientes.justificacion_falta_profesorado;

import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.educaflow.apps.expedientes.tiposexpedientes.shared.AutoFirma;
import com.educaflow.common.pdf.Rectangulo;

import java.util.Map;

public class ActionController {

    @CallMethod
    public void firmarDocumentacionParaPresentar(ActionRequest actionRequest, ActionResponse actionResponse) {

        AutoFirma autofirma = (new AutoFirma())
            .setRectangulo(new Rectangulo(100,200,200,150))
            .setPageNumber(1)
            .setSourceField("documentacionParaPresentarSinFirmar")
            .setTargetField("documentacionPresentadaFirmadaUsuario");

        AutoFirma.sendToActionResponse(autofirma,actionResponse);
    }
}
