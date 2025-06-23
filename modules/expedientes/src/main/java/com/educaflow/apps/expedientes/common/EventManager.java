package com.educaflow.apps.expedientes.common;

import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.ViewForState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.common.util.ReflectionUtil;
import com.educaflow.common.util.TextUtil;
import com.google.common.base.CaseFormat;


import java.lang.reflect.Method;

public abstract class EventManager<T extends Expediente, Estado extends Enum<Estado>, Evento extends Enum<Evento> > {

    private final Class<T> modelClass;
    private final Class<Estado> stateClass;
    private final Class<Evento> eventClass;

    public EventManager(Class<T> modelClass, Class<Estado> stateClass, Class<Evento> eventClass) {
        this.modelClass = modelClass;
        this.stateClass = stateClass;
        this.eventClass = eventClass;
    }

    public abstract void triggerInitialEvent(TipoExpediente tipoExpediente, Contexto contexto);
    public abstract  String getViewForNullState(TipoExpediente tipoExpediente, Contexto contexto);



    public void triggerEvent(String strEvent, T expediente, T expedienteOriginal, Contexto contexto) {
        try {
            Evento event = (Evento) Enum.valueOf(eventClass, strEvent);
            String methodName = "trigger" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,event.name());
            Method method = ReflectionUtil.getMethod(this.getClass(), methodName, void.class, WhenEvent.class, new Class<?>[]{modelClass,modelClass, Contexto.class});

            method.invoke(this, expediente, expedienteOriginal, contexto);
        } catch (Exception ex) {
            throw new RuntimeException("Error al invocar el evento: " + strEvent , ex);
        }
    }

    public void onEnterState(T expediente, Contexto contexto) {
        Estado estado=null;
        try {
            estado = (Estado) Enum.valueOf(stateClass, expediente.getCodeState());
            String methodName = "onEnter" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,estado.name());
            Method method = ReflectionUtil.getMethod(this.getClass(), methodName, void.class, OnEnterState.class, new Class<?>[]{modelClass, Contexto.class});

            method.invoke(this, expediente, contexto);
        } catch (Exception ex) {
            throw new RuntimeException("Error al invocar el estado: " + estado, ex);
        }
    }

    public String getViewForState(T expediente, Contexto contexto) {

        Estado estado=null;
        try {
            estado = (Estado) Enum.valueOf(stateClass, expediente.getCodeState());
            String methodName = "getViewFor" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,estado.name());
            Method method = ReflectionUtil.getMethod(this.getClass(), methodName, String.class, ViewForState.class, new Class<?>[]{modelClass, Contexto.class});

            String viewName =(String)method.invoke(this,  expediente, contexto);

            return viewName;

        } catch (Exception ex) {
            throw new RuntimeException("Error al obtener la vista del estado: " + estado, ex);
        }
    }


    public Class<T> getModelClass() {
        return modelClass;
    }

}
