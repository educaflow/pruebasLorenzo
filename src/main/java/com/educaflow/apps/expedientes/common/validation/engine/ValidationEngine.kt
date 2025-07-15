package com.educaflow.apps.expedientes.common.validation.engine

import com.educaflow.apps.expedientes.common.validation.BeanValidationRules
import com.educaflow.common.messages.BusinessMessage
import com.educaflow.common.messages.BusinessMessages

class ValidationEngine {

    fun validate(bean: Any,validationRules: BeanValidationRules) : BusinessMessages {
        val businessMessages = BusinessMessages();

        for (fieldValidationRule in validationRules.fieldValidationRules) {
            val field= fieldValidationRule.field
            for( validationRule in fieldValidationRule.validationRules) {
                val message = validationRule.validate( field.call(bean) )
                if (message!=null) {
                    businessMessages.add(BusinessMessage(field.name, message))
                }
            }
        }

        return businessMessages
    }


}