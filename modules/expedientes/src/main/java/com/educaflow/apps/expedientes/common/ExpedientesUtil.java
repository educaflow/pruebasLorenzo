package com.educaflow.apps.expedientes.common;

import com.axelor.db.Model;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ExpedientesUtil {


    public static void  returnView (ActionResponse response, Model entity, String viewName) {
        response.setView(
                ActionView.define(entity.getClass().getSimpleName())
                        .model(entity.getClass().getName())
                        .add("form", viewName)
                        .param("forceEdit", "true")
                        .context("_showRecord", entity.getId())
                        .map());
    }

    public static String getLowerCamelCaseFromScreamingSnakeCase(String screamingSnakeCase) {
        if (screamingSnakeCase == null || screamingSnakeCase.isEmpty()) {
            return screamingSnakeCase;
        }
        StringBuilder lowerCamelCase = new StringBuilder();

        String[] parts = screamingSnakeCase.split("_");

        for (String part : parts) {
            if (!part.isEmpty()) {
                lowerCamelCase.append(getLowerFirstLetter(part));
            } else {
                lowerCamelCase.append(part);
            }
        }

        return lowerCamelCase.toString();
    }

    public static String getLowerFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }



    public static Method getMethod(Class<?> baseClass, String methodName, Class<?> returnClass, Class<? extends Annotation> annotation,Class<?>[] parameterTypes) {
        for (Method method : baseClass.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (!returnClass.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            if (!method.isAnnotationPresent(annotation)) {
                continue;
            }
            Class<?>[] methodParamTypes = method.getParameterTypes();
            if (methodParamTypes.length != parameterTypes.length) {
                continue;
            }

            boolean paramsMatch = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!methodParamTypes[i].equals(parameterTypes[i])) {
                    paramsMatch = false;
                    break;
                }
            }

            if (paramsMatch) {
                return method;
            }
        }
        return null;
    }

}
