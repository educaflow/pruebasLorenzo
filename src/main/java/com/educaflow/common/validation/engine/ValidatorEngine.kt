package com.educaflow.common.validation.engine

import com.axelor.db.annotations.Widget
import com.educaflow.common.validation.messages.BusinessMessage
import com.educaflow.common.validation.messages.BusinessMessages
import java.lang.reflect.Field

class ValidatorEngine {

    fun validate(bean: Any,validationRules: BeanValidationRules) : BusinessMessages {
        val businessMessages = BusinessMessages();

        for (fieldValidationRule in validationRules.fieldValidationRules) {
            val methodField= fieldValidationRule.methodField
            for( validationRule in fieldValidationRule.validationRules) {
                val value=methodField.call(bean)
                val message = validationRule.validate( value ,bean)
                if (message!=null) {
                    val fieldName= fieldValidationRule.getFieldName();
                    val type = validationRule::class.simpleName ?: "Unknown"
                    val label = getLabel(bean.javaClass, fieldName);

                    businessMessages.add(BusinessMessage(fieldName, message,type,label))
                }
            }
        }

        return businessMessages
    }


    fun getLabel(clazz: Class<*>, nombreCampo: String): String {
        try {
            val field: Field = clazz.getDeclaredField(nombreCampo)
            if (field.isAnnotationPresent(Widget::class.java)) {
                val widget: Widget = field.getAnnotation(Widget::class.java)
                return widget.title ?: nombreCampo
            } else {
                return nombreCampo
            }
        } catch (e: NoSuchFieldException) {
            throw IllegalArgumentException("El campo '$nombreCampo' no existe en la clase ${clazz.simpleName}", e)
        }
    }

}