package com.educaflow.apps.expedientes.common;


public class EventContext {

    final private Profile profile;

    public EventContext(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }

    public String toString() {
        return "EventContext [profile=" + profile + "]";
    }
}
