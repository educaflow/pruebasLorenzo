package com.educaflow.apps.expedientes.tipo.justificacion_falta_profesorado

import com.axelor.meta.CallMethod
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.educaflow.apps.expedientes.common.validation.Field
import com.educaflow.apps.expedientes.common.validation.MaxLength
import com.educaflow.apps.expedientes.common.validation.Past
import com.educaflow.apps.expedientes.common.validation.Regex
import com.educaflow.apps.expedientes.common.validation.Required
import com.educaflow.apps.expedientes.common.validation.ViewValidador
import com.educaflow.apps.expedientes.common.validation.ViewsValidator
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado

class Validator {

    /**
    fun validate(): ViewsValidator<JustificacionFaltaProfesorado.Estado> {

        val validator = ViewsValidator(
            model = JustificacionFaltaProfesorado::class,
            state = JustificacionFaltaProfesorado.Estado,
            views = listOf(
                ViewValidador(
                    viewName = JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS,
                    fields = listOf(
                        Field(
                            name = JustificacionFaltaProfesorado::getNombre,
                            validationRules = listOf(Required(), MaxLength(50))
                        ),
                        Field(
                            name = JustificacionFaltaProfesorado::getDni,
                            validationRules = listOf(Required(), Regex("^[0-9]{8}[A-Z]$"))
                        ),
                        Field(
                            name = JustificacionFaltaProfesorado::getHoraInicio,
                            validationRules = listOf(Past())
                        )
                    )
                )
            )
        )

        return validator;

    }

    **/
}

