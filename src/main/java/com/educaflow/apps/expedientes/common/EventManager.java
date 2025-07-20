package com.educaflow.apps.expedientes.common;

import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.common.util.AxelorViewUtil;
import com.educaflow.common.util.ReflectionUtil;
import com.google.common.base.CaseFormat;


import java.lang.reflect.Method;
import java.util.Arrays;

public abstract class EventManager<T extends Expediente, State extends Enum<State>, Event extends Enum<Event>, Profile extends Enum<Profile> > {

    final private String VIEW_NAME_DEFAULT_FORMAT ="exp-${EXPEDIENT_CODE}-${ROLE_NAME}-${STATE}-form";

    private final Class<T> modelClass;
    private final Class<State> stateClass;
    private final Class<Event> eventClass;
    private final Class<Profile> profileClass;

    public EventManager(Class<T> modelClass, Class<State> stateClass, Class<Event> eventClass, Class<Profile> profileClass) {
        this.modelClass = modelClass;
        this.stateClass = stateClass;
        this.eventClass = eventClass;
        this.profileClass = profileClass;
    }

    public abstract Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext<Profile> eventContext);


    public void triggerEvent(String strEvent, T expediente, T expedienteOriginal, EventContext<Profile> eventContext) {
        try {
            Enum event;

            try {
                event  =  Enum.valueOf(eventClass, strEvent);
            } catch (Exception e) {
                event  =  Enum.valueOf(CommonEvent.class, strEvent);
            }
            String methodName = "trigger" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,event.name());
            Method method = ReflectionUtil.getMethod(this.getClass(), methodName, void.class, WhenEvent.class, new Class<?>[]{modelClass,modelClass, EventContext.class});

            method.invoke(this, expediente, expedienteOriginal, eventContext);
        } catch (Exception ex) {
            throw new RuntimeException("Error al invocar el event: " + strEvent , ex);
        }
    }

    public void onEnterState(T expediente, EventContext<Profile> eventContext) {
        State state=null;
        try {
            state = (State) Enum.valueOf(stateClass, expediente.getCodeState());
            String methodName = "onEnter" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,state.name());
            Method method = ReflectionUtil.getMethod(this.getClass(), methodName, void.class, OnEnterState.class, new Class<?>[]{modelClass, EventContext.class});

            method.invoke(this, expediente, eventContext);
        } catch (Exception ex) {
            throw new RuntimeException("Error al invocar el state: " + state, ex);
        }
    }

    public String getViewName(T expediente, EventContext<Profile> eventContext) {


        String viewName=getDefaultViewName(expediente.getTipoExpediente().getCode(), eventContext.getProfile(),(State)ReflectionUtil.getEnumConstant(stateClass,expediente.getCodeState()));

        if (existsView(viewName)==false) {
            throw new RuntimeException("No existe la vista:" + viewName + " en el expediente:" + expediente + " en el contexto:" + eventContext);
        }

        return viewName;

    }

    private boolean existsView(String viewName) {
        return AxelorViewUtil.existsView(viewName,"form",this.getModelClass().getName());
    }

    private String getDefaultViewName(String tipoExpedienteCode, Profile profile, State state) {
        String viewName = VIEW_NAME_DEFAULT_FORMAT.replace("${EXPEDIENT_CODE}", tipoExpedienteCode)
                .replace("${ROLE_NAME}", profile.name())
                .replace("${STATE}", state.name());

        return viewName;
    }

    public Class<T> getModelClass() {
        return modelClass;
    }
    public Class<State> getStateClass() {
        return stateClass;
    }
    public Class<Event> getEventClass() {
        return eventClass;
    }
    public Class<Profile> getProfileClass() {
        return profileClass;
    }

}
