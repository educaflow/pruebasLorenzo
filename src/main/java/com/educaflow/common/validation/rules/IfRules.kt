package com.educaflow.common.validation.rules

import com.educaflow.common.validation.engine.ValidationRule
import kotlin.reflect.KFunction

data class IfRule(val dependentField:KFunction<*>,val dependentValue: Any?, val validationRules: List<ValidationRule>) : ValidationRule {

    override fun validate(value: Any?, bean: Any): String? {
        val currentDependentValue=dependentField.call(bean);

        if (currentDependentValue != dependentValue) {
            return null
        }

        for (validationRule in validationRules) {
            val message = validationRule.validate(value, bean)
            if (message != null) {
                return message
            }
        }
        return null
    }
}