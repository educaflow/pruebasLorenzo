package com.educaflow.apps.developer.controller;

import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.educaflow.apps.developer.db.DevInfo;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


        response.setErrors(map);

        ErrorMessajes errors = new ErrorMessajes();
        errors.addErroMessaje("Provincia", "El valor no existe");
        errors.addErroMessaje("Importe", "No puede ser negativo");
        errors.addErroMessaje("Precio", "está vacio");

        errors.storeInActionResponse(response);

    }
    @CallMethod
    public void ok(ActionRequest request, ActionResponse response) {

        response.setValue("info", "Hola Mundo");

        StringBuilder sb=new StringBuilder();
        List<URL> xmlFiles=findXmlFilesOnClasspath();
        for (URL url : xmlFiles) {
            sb.append(url.toString()).append("\n");
        }

        response.setValue("info2", sb);
    }

    public List<URL> findXmlFilesOnClasspath()  {

        try {
            ClassPath classPath = ClassPath.from(getClass().getClassLoader());

            return classPath.getResources().stream()
                    .filter(resourceInfo -> resourceInfo.getResourceName().endsWith(".xml"))
                    .map(ClassPath.ResourceInfo::url)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<URL> findXmlFilesOnClasspath2()  {

        try {
            ClassPath classPath = ClassPath.from(getClass().getClassLoader());

            return classPath.getResources().stream()
                    .filter(resourceInfo -> resourceInfo.getResourceName().endsWith(".xml"))
                    .map(ClassPath.ResourceInfo::url)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
