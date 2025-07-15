package com.educaflow.apps.expedientes.common.validation.rules

import com.educaflow.apps.expedientes.common.validation.ValidationRule

data class MinLength(val min: Int) : ValidationRule {

    override fun validate(bean: Any?): String? {
        if (bean is String) {
            return if (bean.length < min) "Debe tener como mínimo una longitud de $min pero tiene ${bean.length}" else null
        }
        return null
    }
}

data class MaxLength(val max: Int) : ValidationRule {

    override fun validate(bean: Any?): String? {
        if (bean is String) {
            return if (bean.length > max) "Debe tener como máximo una longitud de $max pero tiene ${bean.length}" else null
        }
        return null
    }
}