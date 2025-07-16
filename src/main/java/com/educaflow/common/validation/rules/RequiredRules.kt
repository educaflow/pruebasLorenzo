package com.educaflow.common.validation.rules

import com.axelor.meta.db.MetaFile
import com.educaflow.common.validation.engine.ValidationRule
import java.math.BigDecimal
import kotlin.reflect.KFunction

class Required : ValidationRule {

    override fun validate(value: Any?, bean: Any): String? {
        return required(value)
    }
}

data class RequiredIf(val dependentField:KFunction<*>,val dependentValue: Any?) : ValidationRule {

    override fun validate(value: Any?, bean: Any): String? {
        val currentDependentValue=dependentField.call(bean);

        if (currentDependentValue != dependentValue) {
            return null
        }

        return required(value)
    }
}


private fun required(value: Any?): String? {
    if (value == null) {
        return "Es requerido"
    }

    if (value is String && value.trim().isEmpty()) {
        return "Es requerido"
    }

    if (value is MetaFile) {
        if (value.fileName.isEmpty()) {
            return "Es requerido"
        }
        if (value.fileSize <= 0) {
            return "No puede estar vacío"
        }
    }

    if (value is Number) {
        when (value) {
            is BigDecimal -> if (value.compareTo(BigDecimal.ZERO) == 0) return "No puede ser cero"
            is Int, is Long, is Short, is Byte -> if (value.toLong() == 0L) return "No puede ser cero"
            is Float, is Double -> if (value.toDouble() == 0.0) return "No puede ser cero"
        }
    }

    return null
}