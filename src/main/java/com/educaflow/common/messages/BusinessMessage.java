package com.educaflow.common.messages;

public class BusinessMessage {

    private final String fieldName;
    private final String message;


    public BusinessMessage( String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public String getFieldName() {
        return fieldName;
    }
}
