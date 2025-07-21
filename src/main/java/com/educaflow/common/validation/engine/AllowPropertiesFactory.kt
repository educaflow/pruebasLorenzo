@file:JvmName("AllowPropertiesFactory")
package com.educaflow.common.validation.engine

import com.axelor.meta.db.MetaFile
import com.fasterxml.jackson.databind.ObjectMapper




public fun getAllowProperties(validationRules: List<ValidationRule?>): MutableMap<String?, Any?>? {
    val allowProperties: MutableMap<String?, Any?> = HashMap<String?, Any?>()

    for (validationRule in validationRules) {
        if ((validationRule is FieldValidationRules)) {
            val fieldValidationRules = validationRule

            if (fieldValidationRules.methodField.returnType.classifier== MetaFile::class) {
                allowProperties.put(fieldValidationRules.getFieldName(),mapOf(
                    "cid" to null,
                    "createdBy" to mapOf("code" to null,"id" to null,"name" to null,),
                    "createdOn" to null,
                    "description" to null,
                    "fileName" to null,
                    "filePath" to null,
                    "fileSize" to null,
                    "fileType" to null,
                    "id" to null,
                    "selected" to null,
                    "updatedBy" to mapOf("code" to null,"id" to null,"name" to null,),
                    "updatedOn" to null,
                    "version" to null
                ))
            } else if (allowProperties.containsKey(fieldValidationRules.getFieldName())) {
                val originalAllowProperties = allowProperties.get(fieldValidationRules.getFieldName()) as MutableMap<String?, Any?>?
                val newAllowProperties = getAllowProperties(fieldValidationRules.validationRules)

                val joinedAllowProperties = joinAllowProperties(originalAllowProperties, newAllowProperties)
                allowProperties.put(fieldValidationRules.getFieldName(), joinedAllowProperties)
            } else {
                allowProperties.put(fieldValidationRules.getFieldName(), getAllowProperties(fieldValidationRules.validationRules))
            }
        }
    }

    if (allowProperties.isEmpty()) {
        return null
    } else {
        //println(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(allowProperties))
        return allowProperties
    }
}

private fun joinAllowProperties(originalAllowProperties: MutableMap<String?, Any?>?, newAllowProperties: MutableMap<String?, Any?>?): MutableMap<String?, Any?>? {
    val allowProperties: MutableMap<String?, Any?> = HashMap<String?, Any?>()

    if (originalAllowProperties != null) {
        for (entry in originalAllowProperties.entries) {
            allowProperties.put(entry.key, entry.value)
        }
    }

    if (newAllowProperties != null) {
        for (entry in newAllowProperties.entries) {
            if (allowProperties.containsKey(entry.key)) {
                if (allowProperties.get(entry.key) == null) {
                    allowProperties.put(entry.key, entry.value)
                } else {
                    allowProperties.put(entry.key, joinAllowProperties(allowProperties.get(entry.key) as MutableMap<String?, Any?>?, entry.value as MutableMap<String?, Any?>?))
                }
            } else {
                allowProperties.put(entry.key, entry.value)
            }
        }
    }

    if (allowProperties.isEmpty()) {
        return null
    } else {
        return allowProperties
    }
}
