package com.educaflow.common.util;

import com.axelor.db.Model;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionResponse;

public class AxelorViewUtil {
    public static void doResponseViewForm(ActionResponse response, String viewName, Class<? extends Model> modelClass, Model entity,String title) {
        ActionView.ActionViewBuilder actionViewBuilder=ActionView.define(title)
                .model(modelClass.getName())
                .add("form", viewName)
                .param("forceEdit", "true")
                .param("forceTitle", "true")
                .param("show-confirm", "false")
                .param("show-toolbar", "false");

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
}
