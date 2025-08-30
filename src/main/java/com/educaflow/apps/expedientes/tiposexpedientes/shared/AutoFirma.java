package com.educaflow.apps.expedientes.tiposexpedientes.shared;

import com.axelor.rpc.ActionResponse;
import com.educaflow.common.pdf.Rectangulo;

import java.util.HashMap;
import java.util.Map;

public class AutoFirma {

    private Rectangulo rectangulo;
    private String nif=null;
    private String sourceField;
    private String targetField;
    private String sufijo="_signed";
    private int pageNumber=-1;

    public AutoFirma() {

    }

    public static void sendToActionResponse(AutoFirma autofirma, ActionResponse actionResponse) {

        if (autofirma.getSourceField() == null || autofirma.getSourceField().isEmpty()) {
            throw new RuntimeException("El campo sourceField no puede estar vacio");
        }
        if (autofirma.getTargetField() == null || autofirma.getTargetField().isEmpty()) {
            throw new RuntimeException("El campo targetField no puede estar vacio");
        }
        if (autofirma.getRectangulo() == null) {
            throw new RuntimeException("El campo rectangulo no puede estar vacio");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("nif", autofirma.getNif());
        payload.put("sourceField", autofirma.getSourceField());
        payload.put("targetField", autofirma.getTargetField());
        payload.put("sufijo", autofirma.getSufijo());
        payload.put("pageNumber", autofirma.getPageNumber());
        Rectangulo rectangulo = autofirma.getRectangulo();
        payload.put("signaturePositionOnPageLowerLeftX", rectangulo.getX());
        payload.put("signaturePositionOnPageLowerLeftY", rectangulo.getY());
        payload.put("signaturePositionOnPageUpperRightX", rectangulo.getX() + rectangulo.getWidth());
        payload.put("signaturePositionOnPageUpperRightY", rectangulo.getY() + rectangulo.getHeight());




        actionResponse.setValue("executeJs",true);
        actionResponse.setValue("methodJs","signDocument");
        actionResponse.setValue("payload",payload);

    }

    public AutoFirma setRectangulo(Rectangulo rectangulo) {
        this.rectangulo = rectangulo;
        return this;
    }

    public Rectangulo getRectangulo() {
        return rectangulo;
    }

    public AutoFirma setNif(String nif) {
        this.nif = nif;
        return this;
    }

    public String getNif() {
        return nif;
    }

    public AutoFirma setSourceField(String sourceField) {
        this.sourceField = sourceField;
        return this;
    }

    public String getSourceField() {
        return sourceField;
    }

    public AutoFirma setTargetField(String targetField) {
        this.targetField = targetField;
        return this;
    }

    public String getTargetField() {
        return targetField;
    }

    public AutoFirma setSufijo(String sufijo) {
        this.sufijo = sufijo;
        return this;
    }

    public String getSufijo() {
        return sufijo;
    }

    public AutoFirma setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public int getPageNumber() {
        return pageNumber;
    }




}
