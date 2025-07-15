package com.educaflow.apps.expedientes.common.validation.rules

import com.educaflow.apps.expedientes.common.validation.ValidationRule

data class MinValue(val min: Int) : ValidationRule {

    override fun validate(bean: Any?): String? {
        if (bean is Int) {
            return if (bean < min) "Debe tener como mínimo el valor de $min pero tiene el valor $bean" else null
        }
        return null
    }
}

data class MaxValue(val max: Int) : ValidationRule {

    override fun validate(bean: Any?): String? {
        if (bean is Int) {
            return if (bean > max) "Debe tener como máximo el valor de $max pero tiene el valor $bean" else null
        }
        return null
    }
}