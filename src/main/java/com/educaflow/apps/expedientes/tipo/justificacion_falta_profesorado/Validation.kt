package com.educaflow.apps.expedientes.tipo.justificacion_falta_profesorado

import com.axelor.meta.CallMethod
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

class PruebaKotlin {
    fun helloFromKotlin(): String {
        return "¡Hola desde Kotlin!"
    }

    @CallMethod
    fun errors(request: ActionRequest, response: ActionResponse) {
        response.setValue("info", "Hola Mundo desde Kotlin");

        //val map: MutableMap<String?, String?> = java.util.HashMap<String?, String?>()
        //map.put("info", "Hola Mundo desde kotlin")

        //response.setErrors(map)
    }

}

fun topLevelFunction(): String {
    return "Soy una función top-level en Kotlin."
}