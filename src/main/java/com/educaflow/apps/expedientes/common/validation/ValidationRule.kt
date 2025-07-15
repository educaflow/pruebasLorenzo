package com.educaflow.apps.expedientes.common.validation

interface ValidationRule {

    /**
     * Validates a bean against the rule.
     * Si retorna null es que la validación ha sido correcta. En caso contrario, retorna el mensaje de error.
     */
    fun validate(bean: Any?): String?

}