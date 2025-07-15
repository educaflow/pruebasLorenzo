package com.educaflow.apps.expedientes.tipo.justificacion_falta_profesorado

import com.educaflow.apps.expedientes.common.validation.BeanValidationRules
import com.educaflow.apps.expedientes.common.validation.FieldValidationRules
import com.educaflow.apps.expedientes.common.validation.rules.MaxLength
import com.educaflow.apps.expedientes.common.validation.rules.MaxValue
import com.educaflow.apps.expedientes.common.validation.rules.MinLength
import com.educaflow.apps.expedientes.common.validation.rules.MinValue
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado as model

class Validation {

    public fun getExampleValidationRules(): BeanValidationRules {
        return BeanValidationRules(
            fieldValidationRules = listOf(
                FieldValidationRules(
                    field = model::getAnyo,
                    validationRules = listOf(
                        MaxValue(2030),
                        MinValue(2000)
                    )
                ),
                FieldValidationRules(
                    field = model::getOtroMotivo,
                    validationRules = listOf(
                        MinLength(3),
                        MaxLength(10)
                    )
                )
            )
        )

    }

}