package com.educaflow.common.pdf;

import com.educaflow.common.evaluator.Evaluator;
import com.educaflow.common.evaluator.impl.EvaluatorImplGroovy;
import com.educaflow.common.util.Convert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentoPdfUtil {


    public static byte[] generate(DocumentoPdf documentoPdf,Map<String,Object> context) {

        List<String> expressions= documentoPdf.getNombreCamposFormulario();

        Evaluator evaluator= new EvaluatorImplGroovy();
        Map<String,Object> result=evaluator.evaluate(expressions, context);

        Map<String, String> resultString = getStringMap(result);


        DocumentoPdf documentoPdfDatos= documentoPdf.setValorCamposFormularioAndFlatten(resultString);

        return documentoPdfDatos.getDatos();

    }

    private static Map<String, String> getStringMap(Map<String, Object> result) {
        Map<String,String> resultString= new HashMap<>();
        for(Map.Entry<String,Object> entry : result.entrySet()) {
            resultString.put(entry.getKey(), Convert.objectToUserString(entry.getValue()));
        }
        return resultString;
    }





}
