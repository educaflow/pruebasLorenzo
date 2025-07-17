package com.educaflow.common.validation.messages;

public class BusinessMessage {

    private final String fieldName;
    private final String message;
    private final String label;


    public BusinessMessage(String fieldName, String message, String label) {
        this.fieldName = fieldName;
        this.message = message;
        this.label = label;
    }
    public String getMessage() {
        return message;
    }
    public String getFieldName() {
        return fieldName;
    }
    public String getLabel() { return label; }

}
