package com.educaflow.common.validation.engine

import kotlin.reflect.KFunction

data class FieldValidationRules(val methodField:KFunction<*>, val validationRules: List<ValidationRule>) {
    public fun getFieldName(): String {
        val methodName=methodField.name

        val fieldName = when {
            methodName.startsWith("get") && methodName.length > 3 -> methodName.substring(3)
            methodName.startsWith("is") && methodName.length > 2 -> methodName.substring(2)
            else -> throw IllegalArgumentException("El método $methodName no es un getter válido")
        }

        return if (fieldName.length >= 2 && fieldName[0].isUpperCase() && fieldName[1].isUpperCase()) {
            fieldName
        } else {
            fieldName.replaceFirstChar { it.lowercaseChar() }
        }

    }
}