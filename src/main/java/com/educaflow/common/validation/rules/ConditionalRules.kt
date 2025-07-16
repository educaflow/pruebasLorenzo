package com.educaflow.common.validation.rules

import com.educaflow.common.validation.engine.ValidationRule
import kotlin.reflect.KFunction

data class IfValue(val dependentField:KFunction<*>, val dependentValues: List<Any>, val validationRules: List<ValidationRule>) : ValidationRule {

    override fun validate(value: Any?, bean: Any): List<String>? {
        val currentDependentValue=dependentField.call(bean);

        if (currentDependentValue !in dependentValues) {
            return null
        }

        val messages=mutableListOf<String>()
        for (validationRule in validationRules) {
            val innerMessages = validationRule.validate(value, bean)
            if ((innerMessages!= null) && (innerMessages.isNotEmpty())) {
                messages.addAll(innerMessages)
            }
        }
        return messages
    }
}

data class IfNotValue(val dependentField:KFunction<*>, val dependentValues: List<Any>, val validationRules: List<ValidationRule>) : ValidationRule {

    override fun validate(value: Any?, bean: Any): List<String>? {
        val currentDependentValue=dependentField.call(bean);

        if (currentDependentValue in dependentValues) {
            return null
        }

        val messages=mutableListOf<String>()
        for (validationRule in validationRules) {
            val innerMessages = validationRule.validate(value, bean)
            if ((innerMessages!= null) && (innerMessages.isNotEmpty())) {
                messages.addAll(innerMessages)
            }
        }
        return messages
    }
}