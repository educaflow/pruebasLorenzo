package com.educaflow.apps.expedientes.tipo.justificacion_falta_profesorado

import com.educaflow.apps.expedientes.common.StateEventValidator
import com.educaflow.apps.expedientes.common.annotations.BeanValidationRulesForStateAndEvent
import com.educaflow.apps.expedientes.db.MotivoFaltaJustificacionFaltaProfesorado
import com.educaflow.apps.expedientes.db.TipoJornadaFaltaJustificacionFaltaProfesorado
import com.educaflow.common.validation.dsl.rules
import com.educaflow.common.validation.engine.BeanValidationRules
import com.educaflow.common.validation.rules.FileMaxSize
import com.educaflow.common.validation.rules.FileType
import com.educaflow.common.validation.rules.GreaterThan
import com.educaflow.common.validation.rules.GreaterThanIf
import com.educaflow.common.validation.rules.MaxValue
import com.educaflow.common.validation.rules.MinValue
import com.educaflow.common.validation.rules.NoAllUpperCase
import com.educaflow.common.validation.rules.Required
import com.educaflow.common.validation.rules.Pattern
import com.educaflow.common.validation.rules.RequiredIf
import com.educaflow.common.validation.rules.SizeUnit
import java.time.LocalDate
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado as model

class StateEventValidator: StateEventValidator {

    @BeanValidationRulesForStateAndEvent
    public fun getForEntradaDatosInPresentar(): BeanValidationRules {
        return rules {
            field(model::getDias) {
                +Required()
                +Pattern("^(\\s*\\b(?:[1-9]|[12][0-9]|3[01])\\b\\s*)(?:,\\s*\\b(?:[1-9]|[12][0-9]|3[01])\\b\\s*)*\$")
            }
            field(model::getMes) {
                +Required()
            }
            field(model::getAnyo) {
                +MaxValue(LocalDate.now().year)
                +MinValue(LocalDate.now().year - 1)
                +Required()
            }
            field(model::getTipoJornadaFalta) {
                +Required()
            }
            field(model::getHoraInicio) {
                +RequiredIf(model::getTipoJornadaFalta, TipoJornadaFaltaJustificacionFaltaProfesorado.JORNADA_PARCIAL)
            }
            field(model::getHoraFin) {
                +RequiredIf(model::getTipoJornadaFalta, TipoJornadaFaltaJustificacionFaltaProfesorado.JORNADA_PARCIAL)
                +GreaterThanIf(model::getHoraInicio, model::getTipoJornadaFalta, TipoJornadaFaltaJustificacionFaltaProfesorado.JORNADA_PARCIAL)
            }
            field(model::getMotivoFalta) {
                +Required()
            }
            field(model::getOtroMotivo) {
                +RequiredIf(model::getMotivoFalta, MotivoFaltaJustificacionFaltaProfesorado.OTROS)
                +NoAllUpperCase()
            }
            field(model::getJustificante) {
                +Required()
                +FileType(listOf("image/png","image/jpeg","image/gif","application/pdf"))
                +FileMaxSize(5, SizeUnit.MB)
            }
        }
    }

    @BeanValidationRulesForStateAndEvent
    fun getForFirmaPorUsuarioInBack():BeanValidationRules {
        return rules {
        }
    }


}