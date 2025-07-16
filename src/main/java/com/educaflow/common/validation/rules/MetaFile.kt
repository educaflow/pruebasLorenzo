package com.educaflow.common.validation.rules

import com.axelor.meta.db.MetaFile
import com.educaflow.common.validation.engine.ValidationRule
import java.util.regex.Pattern

data class FileMaxSize(val max: Int) : ValidationRule {

    override fun validate(value: Any?,bean: Any): String? {
        if (value is MetaFile) {
            if (value.fileSize<=max) {
                return null
            } else {
                return "El tamaño del archivo debe ser como máximo de ${max / 1024} KB pero tiene un tamaño de ${value.fileSize / 1024} KB"
            }
        }
        return null
    }
}

data class FileType(val fileTypes: List<String>) : ValidationRule {

    override fun validate(value: Any?,bean: Any): String? {
        if (value is MetaFile) {
            if (value.fileType in fileTypes) {
                return null
            } else {
                return "El tipo de archivo debe ser uno de los siguientes: ${fileTypes.joinToString(", ")} pero es ${value.fileType}"
            }
        }
        return null
    }
}


data class FileName(val regex: String) : ValidationRule {

    private val pattern = Pattern.compile(regex)

    override fun validate(value: Any?, bean: Any): String? {
        if (value is MetaFile) {
            val fileName = value.fileName
            return if (pattern.matcher(fileName).matches()) {
                null
            } else {
                "El nombre de archivo '$fileName' no cumple con el patrón '$regex'."
            }
        }
        return null
    }
}