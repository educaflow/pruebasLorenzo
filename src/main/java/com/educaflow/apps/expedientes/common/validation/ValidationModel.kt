package com.educaflow.apps.expedientes.common.validation

import com.educaflow.apps.expedientes.db.Expediente
import java.math.BigDecimal
import kotlin.Enum
import kotlin.reflect.KClass


data class ViewsValidator(val model: KClass<out Expediente>, val state: Enum<*>, val views: List<ViewValidador>)

data class ViewValidador(val viewName: String,val fields: List<Field>)

data class Field(val name: String,val validationRules: List<ValidationRule>)


interface ValidationRule

class Required : ValidationRule
data class MaxInteger(val maxValue:Int) : ValidationRule
data class MinInteger(val minValue:Int) : ValidationRule
data class MaxLength(val maxLength:Int) : ValidationRule
data class MinLength(val minLength:Int) : ValidationRule
data class MaxDecimal(val maxValue: BigDecimal) : ValidationRule
data class MinDecimal(val minValue:BigDecimal) : ValidationRule
data class Regex(val pattern:String) : ValidationRule
class Past: ValidationRule
class Future: ValidationRule
class PartOrPresent: ValidationRule
class FutureOrPresent: ValidationRule
class Positive: ValidationRule
class PositiveOrZero: ValidationRule
class Negative: ValidationRule
class NegativeOrZero: ValidationRule
class Email: ValidationRule
class Url: ValidationRule
class Phone: ValidationRule
class Date: ValidationRule
class Time: ValidationRule
class DateTime: ValidationRule
data class Enum(val values: List<String>) : ValidationRule
data class Range(val min: Int,val max: Int) : ValidationRule
data class Custom(val validator: (Any?) -> String?) : ValidationRule
