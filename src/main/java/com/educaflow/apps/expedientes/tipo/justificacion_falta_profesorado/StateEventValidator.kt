package com.educaflow.apps.expedientes.tipo.justificacion_falta_profesorado

import com.educaflow.apps.expedientes.common.StateEventValidator
import com.educaflow.apps.expedientes.common.annotations.BeanValidationRulesForStateAndEvent
import com.educaflow.apps.expedientes.db.MotivoFaltaJustificacionFaltaProfesorado
import com.educaflow.common.validation.engine.BeanValidationRules
import com.educaflow.common.validation.engine.FieldValidationRules
import com.educaflow.common.validation.rules.ListIntNumbers
import com.educaflow.common.validation.rules.MaxLength
import com.educaflow.common.validation.rules.MaxValue
import com.educaflow.common.validation.rules.MinLength
import com.educaflow.common.validation.rules.MinValue
import com.educaflow.common.validation.rules.Required
import com.educaflow.common.validation.rules.RequiredIf
import com.educaflow.common.validation.rules.NoAllUpperCase
import com.educaflow.common.validation.rules.Pattern
import java.time.LocalDate
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado as model

class StateEventValidator: StateEventValidator {

    @BeanValidationRulesForStateAndEvent
    public fun getForEntradaDatosInPresentar(): BeanValidationRules {
        return BeanValidationRules(
            fieldValidationRules = listOf(
                FieldValidationRules(
                    methodField = model::getDias,
                    validationRules = listOf(
                        Required(),
                        Pattern("^(\\s*\\b(?:[1-9]|[12][0-9]|3[01])\\b\\s*)(?:,\\s*\\b(?:[1-9]|[12][0-9]|3[01])\\b\\s*)*\$")
                    )
                ),
                FieldValidationRules(
                    methodField = model::getMes,
                    validationRules = listOf(
                        Required()
                    )
                ),
                FieldValidationRules(
                    methodField = model::getAnyo,
                    validationRules = listOf(
                        MaxValue(LocalDate.now().year),
                        MinValue(LocalDate.now().year-1),
                        Required()
                    )
                ),
                FieldValidationRules(
                    methodField = model::getTipoJornadaFalta,
                    validationRules = listOf(
                        Required()
                    )
                ),
                FieldValidationRules(
                    methodField = model::getMotivoFalta,
                    validationRules = listOf(
                        Required()
                    )
                ),

                FieldValidationRules(
                    methodField = model::getOtroMotivo,
                    validationRules = listOf(
                        MinLength(3),
                        MaxLength(10),
                        RequiredIf(model::getMotivoFalta, MotivoFaltaJustificacionFaltaProfesorado.OTROS),
                        NoAllUpperCase()
                    )
                ),

                FieldValidationRules(
                    methodField = model::getJustificante,
                    validationRules = listOf(
                        Required()
                    )
                ),



            )
        )
    }

}