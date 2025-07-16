package com.educaflow.common.util;

import com.axelor.db.Model;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.rpc.ActionResponse;
import com.educaflow.common.validation.messages.BusinessMessage;
import com.educaflow.common.validation.messages.BusinessMessages;
import com.oracle.truffle.api.profiles.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AxelorViewUtil {
    public static void doResponseViewForm(ActionResponse response, String viewName, Class<? extends Model> modelClass, Model entity, String title, String profile) {
        ActionView.ActionViewBuilder actionViewBuilder=ActionView.define(title)
                .model(modelClass.getName())
                .add("form", viewName)
                .param("forceEdit", "true")
                .param("forceTitle", "true")
                .param("show-confirm", "false")
                .param("show-toolbar", "false")
                .context("_profile",profile);

        if ((entity != null)  && (entity.getId() != null)) {
            actionViewBuilder.context("_showRecord", entity.getId()).param("forceEdit", "true");
        } else {
            actionViewBuilder.context("newEntity", entity);
        }


        response.setView(actionViewBuilder.map());
    }

    public static void doResponseViewGrid(ActionResponse response, String viewName, Class<? extends Model> modelClass) {
        ActionView.ActionViewBuilder actionViewBuilder=ActionView.define("Hola")
                .model(modelClass.getName())
                .add("grid", viewName)
                .name("Pepe");

        response.setView(actionViewBuilder.map());
    }

    public static boolean existsView(String name, String type, String model) {
        AbstractView data = XMLViews.findView(name, type, model);
        if (data==null) {
            return false;
        } else {
            return true;
        }
    }

    public static void doResponseBusinessMessages(ActionResponse response, BusinessMessages businessMessages) {


        Map<String, String> mapErrors = new HashMap<>();
        for(BusinessMessage businessMessage : businessMessages) {
            String fieldName = businessMessage.getFieldName();
            String message = businessMessage.getMessage();
            String type = businessMessage.getType();

            if (type.startsWith("Required")) {
                mapErrors.put(fieldName, message);
            }
        }
        //response.setErrors(mapErrors);

        ErrorMessajes errors = new ErrorMessajes();
        for(BusinessMessage businessMessage : businessMessages) {
            String fieldName = businessMessage.getFieldName();
            String message = businessMessage.getMessage();
            String type = businessMessage.getType();
            String label = businessMessage.getLabel();
            errors.addErroMessaje(fieldName,message,type,label);
        }
        errors.storeInActionResponse(response);

    }


    private static class ErrorMessajes extends ArrayList {

        public void addErroMessaje(String fieldName, String message, String type, String label) {
            Map<String,String> messaje=new HashMap<>();
            messaje.put("fieldName", fieldName);
            messaje.put("message", message);
            messaje.put("type", type);
            messaje.put("label", label);
            this.add(messaje);
        }


        public void storeInActionResponse(ActionResponse response) {
            response.setValue("errorMensajes",this);
        }

    }

}
