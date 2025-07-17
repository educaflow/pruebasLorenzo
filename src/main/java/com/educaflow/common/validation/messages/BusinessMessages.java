package com.educaflow.common.validation.messages;

import java.util.ArrayList;

public class BusinessMessages extends ArrayList<BusinessMessage> {

    public boolean isValid() {
        return this.isEmpty();
    }





    static public BusinessMessages single(String message) {
        BusinessMessage businessMessage = new BusinessMessage(null, message, null);
        BusinessMessages businessMessages = new BusinessMessages();
        businessMessages.add(businessMessage);
        return businessMessages;
    }

}
