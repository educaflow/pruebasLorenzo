package com.educaflow.apps.developer.controller;

import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.educaflow.apps.developer.db.DevInfo;

import java.util.HashMap;
import java.util.Map;

public class DeveloperController {

    @CallMethod
    public void errors(ActionRequest request, ActionResponse response) {
        //response.setValue("info", "Hola Mundo");

        Map<String, String> map = new HashMap<>();
        map.put("info", "Hola Mundo");
        map.put("info2", "Hola Muhgfghfndo");
        map.put("info3", "Hola Mfhgfundo");
        map.put("info4", "Hhgfhola Mundo");
        map.put("info5", "Hofhgfhgfla Mundo");

    DevInfo a = new DevInfo();

        response.setErrors(map);


    }
    @CallMethod
    public void ok(ActionRequest request, ActionResponse response) {

        ErrorMessajes errors = new ErrorMessajes();
        errors.addErroMessaje("Provincia", "El valor no existe");
        errors.addErroMessaje("Importe", "No puede ser negativo");
        errors.addErroMessaje("Precio", "está vacio");

        errors.storeInActionResponse(response);

    }

}
