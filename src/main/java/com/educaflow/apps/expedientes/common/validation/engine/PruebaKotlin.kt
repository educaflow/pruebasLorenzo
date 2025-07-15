package com.educaflow.apps.expedientes.common.validation.engine

import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado
import kotlin.reflect.KFunction



class PruebaKotlin  {


    fun getDatos(justificacionFaltaProfesorado:JustificacionFaltaProfesorado): String {

        val nombre=justificacionFaltaProfesorado.getNombre();
        val metodo=JustificacionFaltaProfesorado::getNombre
        val resultado= metodo.call(justificacionFaltaProfesorado);
        return resultado.toString() + " - " + metodo.name;
    }


    fun metodo(bean: Any, metodo: KFunction<*>) : String {
        val resultado = metodo.call(bean)
        return resultado.toString() + " - " + metodo.name
    }


}







