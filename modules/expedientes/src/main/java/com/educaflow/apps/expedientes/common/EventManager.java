package com.educaflow.apps.expedientes.common;

import com.axelor.meta.service.MetaService;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.ViewForState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.common.util.AxelorViewUtil;
import com.educaflow.common.util.ReflectionUtil;
import com.google.common.base.CaseFormat;
import com.google.inject.Inject;


import java.lang.reflect.Method;

public abstract class EventManager<T extends Expediente, Estado extends Enum<Estado>, Evento extends Enum<Evento> > {

    final private String VIEW_NAME_FULL_FORMAT="exp-${EXPEDIENT_CODE}-${ROLE_NAME}-${STATE}-form";

    @Inject
    private MetaService metaService;

    private final Class<T> modelClass;
    private final Class<Estado> stateClass;
    private final Class<Evento> eventClass;

    public EventManager(Class<T> modelClass, Class<Estado> stateClass, Class<Evento> eventClass) {
        this.modelClass = modelClass;
        this.stateClass = stateClass;
        this.eventClass = eventClass;
    }

    public abstract Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext eventContext);


    public void triggerEvent(String strEvent, T expediente, T expedienteOriginal, EventContext eventContext) {
        try {
            Evento event = (Evento) Enum.valueOf(eventClass, strEvent);
            String methodName = "trigger" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,event.name());
            Method method = ReflectionUtil.getMethod(this.getClass(), methodName, void.class, WhenEvent.class, new Class<?>[]{modelClass,modelClass, EventContext.class});

            method.invoke(this, expediente, expedienteOriginal, eventContext);
        } catch (Exception ex) {
            throw new RuntimeException("Error al invocar el evento: " + strEvent , ex);
        }
    }

    public void onEnterState(T expediente, EventContext eventContext) {
        Estado estado=null;
        try {
            estado = (Estado) Enum.valueOf(stateClass, expediente.getCodeState());
            String methodName = "onEnter" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,estado.name());
            Method method = ReflectionUtil.getMethod(this.getClass(), methodName, void.class, OnEnterState.class, new Class<?>[]{modelClass, EventContext.class});

            method.invoke(this, expediente, eventContext);
        } catch (Exception ex) {
            throw new RuntimeException("Error al invocar el estado: " + estado, ex);
        }
    }

    public String getViewName(T expediente, EventContext eventContext) {
        String defaultViewName=getDefaultViewName(expediente, eventContext);



        boolean existsView=AxelorViewUtil.existsView(defaultViewName,"form",this.getModelClass().getName());

        if (existsView==false) {
            throw new RuntimeException("Para el expediente no existe la vista para el estado y rol concretos: "+defaultViewName);
        }

        return defaultViewName;
    }

    private String getDefaultViewName(T expediente, EventContext eventContext) {
        String viewName = VIEW_NAME_FULL_FORMAT.replace("${EXPEDIENT_CODE}", expediente.getTipoExpediente().getCode())
                .replace("${ROLE_NAME}", eventContext.getPerfil().name())
                .replace("${STATE}", expediente.getCodeState());

        return viewName;
    }

    public Class<T> getModelClass() {
        return modelClass;
    }

}
