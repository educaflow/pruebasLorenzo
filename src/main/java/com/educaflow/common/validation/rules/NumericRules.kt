package com.educaflow.common.validation.rules

import com.educaflow.common.validation.engine.ValidationRule

data class MinValue(val min: Int) : ValidationRule {

    override fun validate(value: Any?,bean: Any): List<String>? {
        if (value is Int) {
            return if (value < min) listOf("Debe tener como mínimo el valor de $min pero tiene el valor $value") else null
        }
        return null
    }
}

data class MaxValue(val max: Int) : ValidationRule {

    override fun validate(value: Any?,bean: Any): List<String>? {
        if (value is Int) {
            return if (value > max) listOf("Debe tener como máximo el valor de $max pero tiene el valor $value") else null
        }
        return null
    }
}