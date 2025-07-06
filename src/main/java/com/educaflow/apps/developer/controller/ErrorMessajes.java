package com.educaflow.apps.developer.controller;

import com.axelor.rpc.ActionResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ErrorMessajes extends ArrayList {

    public void addErroMessaje(String fieldName, String value) {
        Map<String,String> messaje=new HashMap<>();
        messaje.put("fieldName", fieldName);
        messaje.put("value", value);
        this.add(messaje);
    }


    public void storeInActionResponse(ActionResponse response) {
        response.setValue("mensajes",this);
    }

}
