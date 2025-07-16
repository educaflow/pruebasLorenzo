package com.educaflow.common.validation.messages;

public class BusinessMessage {

    private final String fieldName;
    private final String message;
    private final String type;
    private final String label;


    public BusinessMessage(String fieldName, String message, String type,String label) {
        this.fieldName = fieldName;
        this.message = message;
        this.type = type;
        this.label = label;
    }
    public String getMessage() {
        return message;
    }
    public String getFieldName() {
        return fieldName;
    }
    public String getType() { return type; }
    public String getLabel() { return label; }
}
