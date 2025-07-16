package com.educaflow.common.validation.dsl

import com.educaflow.common.validation.engine.BeanValidationRules
import com.educaflow.common.validation.engine.FieldValidationRules
import com.educaflow.common.validation.engine.ValidationRule
import kotlin.reflect.KFunction

@DslMarker
annotation class BeanValidationDSL




@BeanValidationDSL
class FieldValidationRulesBuilder(private val property: KFunction<*>) {
    private val rules = mutableListOf<ValidationRule>() // La lista de reglas se mueve aquí

    /**
     * Construye y devuelve el objeto [FieldValidationRules].
     */
    fun build(): FieldValidationRules {
        return FieldValidationRules(property, rules)
    }

    @BeanValidationDSL
    operator fun ValidationRule.unaryPlus() {
        rules += this
    }
}

/**
 * Constructor principal para definir todas las reglas de validación de un bean.
 * Permite añadir múltiples conjuntos de reglas para diferentes campos.
 */
@BeanValidationDSL
class BeanValidationRulesBuilder {
    private val fieldValidationRules = mutableListOf<FieldValidationRules>()

    /**
     * Define un bloque de reglas de validación para un campo específico del modelo.
     * Este es el método principal para la sintaxis `fieldValidationRules { ... }`.
     * @param property La función getter del modelo a la que se aplican las reglas.
     * @param setup Lambda con el receptor [FieldValidationRulesBuilder] para definir las reglas del campo.
     */
    @BeanValidationDSL
    public fun field(property: KFunction<*>, setup: FieldValidationRulesBuilder.() -> Unit) {
        val builder = FieldValidationRulesBuilder(property)
        builder.setup()
        fieldValidationRules += builder.build()
    }

    /**
     * Permite añadir directamente un objeto [FieldValidationRules] ya construido.
     * Esto soporta la sintaxis `+FieldValidationRules(...)` similar al ejemplo de `village`.
     * @param rule La regla de validación de campo a añadir.
     */
    @BeanValidationDSL
    operator fun FieldValidationRules.unaryPlus() {
        fieldValidationRules += this
    }

    /**
     * Construye y devuelve el objeto [BeanValidationRules] final.
     */
    fun build(): BeanValidationRules {
        return BeanValidationRules(fieldValidationRules)
    }

    /**
     * Este método sombrea el método [beanValidationRules] de nivel superior cuando se está dentro del ámbito
     * de un [BeanValidationRulesBuilder], evitando que las reglas de validación de beans se aniden.
     */
    @Suppress("UNUSED_PARAMETER")
    @Deprecated(
        level = DeprecationLevel.ERROR,
        message = "BeanValidationRules can't be nested."
    )
    fun beanValidationRules(param: () -> Unit = {}) {
    }
}

/**
 * Función de nivel superior para iniciar la construcción de las reglas de validación de un bean.
 * @param setup Lambda con el receptor [BeanValidationRulesBuilder] para definir todas las reglas.
 * @return Un objeto [BeanValidationRules] que contiene todas las reglas definidas.
 */
@BeanValidationDSL
fun rules(setup: BeanValidationRulesBuilder.() -> Unit): BeanValidationRules {
    val builder = BeanValidationRulesBuilder()
    builder.setup()
    return builder.build()
}