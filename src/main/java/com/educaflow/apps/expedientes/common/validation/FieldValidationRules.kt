package com.educaflow.apps.expedientes.common.validation

import kotlin.reflect.KFunction

data class FieldValidationRules(val field:KFunction<*>,val validationRules: List<ValidationRule>)