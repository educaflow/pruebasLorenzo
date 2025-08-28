package com.educaflow.common.evaluator.impl;

import com.educaflow.common.evaluator.Evaluator;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluatorImplGroovy implements Evaluator {

    public Map<String,Object> evaluate(List<String> expressions, Map<String,Object> context) {
        Map<String,Object> results = new HashMap<>();

        Binding binding = new Binding();
        for(Map.Entry<String,Object> entry : context.entrySet()) {
            binding.setVariable(entry.getKey(), entry.getValue());
        }

        GroovyShell shell = new GroovyShell(binding);

        for (String expression : expressions) {
            Object result = shell.evaluate(expression);
            results.put(expression, result);
        }

        return results;
    }

}
