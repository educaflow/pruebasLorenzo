package com.educaflow.apps.expedientes.common;

import com.axelor.db.Model;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.educaflow.apps.expedientes.common.annotations.OnState;
import com.educaflow.apps.expedientes.common.annotations.TriggerEvent;


import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class EventManager<T extends Model,Eventos extends Enum,Estados extends Enum > {

    public abstract void triggerInitialEvent(ActionRequest request, ActionResponse response);

    public String triggerEvent(String strEvent, ActionRequest request, ActionResponse response) {
        try {
            Eventos event = (Eventos) Enum.valueOf(getEventClass(), strEvent);
            String methodName = "trigger" + ExpedientesUtil.getLowerCamelCaseFromScreamingSnakeCase(event.name());
            Method method = ExpedientesUtil.getMethod(this.getClass(), methodName, this.getStateClass(), TriggerEvent.class, new Class<?>[]{ActionRequest.class, ActionResponse.class});

            Estados state =(Estados)method.invoke(this, request, response);

            return state.name();

        } catch (Exception ex) {
            throw new RuntimeException("Error al invocar el evento: " + strEvent, ex);
        }
    }

    public void onState(String strState, ActionRequest request, ActionResponse response) {
        try {
            Estados state = (Estados) Enum.valueOf(getEventClass(), strState);
            String methodName = "on" + ExpedientesUtil.getLowerCamelCaseFromScreamingSnakeCase(state.name());
            Method method = ExpedientesUtil.getMethod(this.getClass(), methodName, this.getStateClass(), OnState.class, new Class<?>[]{ActionRequest.class, ActionResponse.class});

            method.invoke(this, request, response);
        } catch (Exception ex) {
            throw new RuntimeException("Error al invocar el estado: " + strState, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private T getRequestEntity(ActionRequest request) {
        Class<T> clazz=null;
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }

        if (clazz == null) {
            throw new IllegalStateException("Cannot determine entity type from EventManager implementation: " + getClass().getName());
        }

        return (T) request.getContext().asType(clazz);
    };

    @SuppressWarnings("unchecked")
    private Class<? extends Enum> getEventClass() {
        Class<? extends Enum> clazz=null;
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            clazz = (Class<? extends Enum>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        }

        if (clazz == null) {
            throw new IllegalStateException("Cannot determine entity type from EventManager implementation: " + getClass().getName());
        }

        return clazz;
    };

    @SuppressWarnings("unchecked")
    private Class<? extends Enum> getStateClass() {
        Class<? extends Enum> clazz=null;
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            clazz = (Class<? extends Enum>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
        }

        if (clazz == null) {
            throw new IllegalStateException("Cannot determine entity type from EventManager implementation: " + getClass().getName());
        }

        return clazz;
    };

}
