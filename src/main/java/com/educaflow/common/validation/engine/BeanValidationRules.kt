package com.educaflow.common.validation.engine

data class BeanValidationRules(val fieldValidationRules: List<FieldValidationRules>) {

    fun getAllFieldNames(): List<String> {
        return fieldValidationRules.map { it.getFieldName() }
    }
}