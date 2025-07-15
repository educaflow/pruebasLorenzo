package com.educaflow.common.messages;

import java.util.ArrayList;

public class BusinessMessages extends ArrayList<BusinessMessage> {

    public boolean isValid() {
        return this.isEmpty();
    }

}
