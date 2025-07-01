package com.educaflow.apps.expedientes.common;

import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.common.util.AxelorViewUtil;
import com.educaflow.common.util.ReflectionUtil;
import com.google.common.base.CaseFormat;


import java.lang.reflect.Method;
import java.util.Arrays;

public abstract class EventManager<T extends Expediente, Estado extends Enum<Estado>, Evento extends Enum<Evento> > {

    final private String VIEW_NAME_DEFAULT_FORMAT ="exp-${EXPEDIENT_CODE}-${ROLE_NAME}-${STATE}-form";
    final private String VIEW_NAME_NO_ROLE_FORMAT="exp-${EXPEDIENT_CODE}-${STATE}-form";
    final private String VIEW_NAME_NO_STATE_FORMAT="exp-${EXPEDIENT_CODE}-${ROLE_NAME}-form";
    final private String VIEW_NAME_NO_STATE_NO_ROLE_FORMAT="exp-${EXPEDIENT_CODE}-form";

    private final Class<T> modelClass;
    private final Class<Estado> stateClass;
    private final Class<Evento> eventClass;

    public EventManager(Class<T> modelClass, Class<Estado> stateClass, Class<Evento> eventClass) {
        this.modelClass = modelClass;
        this.stateClass = stateClass;
        this.eventClass = eventClass;

        checkMethods();
    }

    public abstract Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext eventContext);


    public void triggerEvent(String strEvent, T expediente, T expedienteOriginal, EventContext eventContext) {
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
        String noRoleViewName= getNoRoleViewName(expediente, eventContext);
        String noStateViewName= getNoStateViewName(expediente, eventContext);
        String noRoleNoStateViewName= getNoRoleNoStateViewName(expediente, eventContext);


        //Los nombre de las vistas tienen una prioridad en caso de que existan varias.
        if (existsView(defaultViewName)) {
            return defaultViewName;
        }
        if (existsView(noRoleViewName)) {
            return noRoleViewName;
        }
        if (existsView(noStateViewName)) {
            return noStateViewName;
        }
        if (existsView(noRoleNoStateViewName)) {
            return noRoleNoStateViewName;
        }

        throw new RuntimeException("No existe la vista" + expediente + "---" + eventContext);
    }

    private boolean existsView(String viewName) {
        return AxelorViewUtil.existsView(viewName,"form",this.getModelClass().getName());
    }

    private String getDefaultViewName(Expediente expediente, EventContext eventContext) {
        return interpolateViewName(VIEW_NAME_DEFAULT_FORMAT,expediente,eventContext);
    }
    private String getNoRoleViewName(Expediente expediente, EventContext eventContext) {
        return interpolateViewName(VIEW_NAME_NO_ROLE_FORMAT,expediente,eventContext);
    }
    private String getNoStateViewName(Expediente expediente, EventContext eventContext) {
        return interpolateViewName(VIEW_NAME_NO_STATE_FORMAT,expediente,eventContext);
    }
    private String getNoRoleNoStateViewName(Expediente expediente, EventContext eventContext) {
        return interpolateViewName(VIEW_NAME_NO_STATE_NO_ROLE_FORMAT,expediente,eventContext);
    }


    private String interpolateViewName(String template,Expediente expediente, EventContext eventContext) {
        String viewName = template.replace("${EXPEDIENT_CODE}", expediente.getTipoExpediente().getCode())
                .replace("${ROLE_NAME}", eventContext.getProfile().name())
                .replace("${STATE}", expediente.getCodeState());

        return viewName;
    }

    public Class<T> getModelClass() {
        return modelClass;
    }
    public Class<Estado> getStateClass() {
        return stateClass;
    }
    public Class<Evento> getEventClass() {
        return eventClass;
    }

    private void checkMethods() {
        StringBuilder messagesFaltanMetodosEventos = new StringBuilder();
        StringBuilder messagesSobranMetodosEventos = new StringBuilder();
        StringBuilder messagesFaltanMetodosEstados = new StringBuilder();
        StringBuilder messagesSobranMetodosEstados = new StringBuilder();
        for (Evento event : eventClass.getEnumConstants()) {
            String methodName = "trigger" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, event.name());
            boolean hasMethod = ReflectionUtil.hasMethod(this.getClass(), methodName, void.class, WhenEvent.class, new Class<?>[]{modelClass,modelClass, EventContext.class});

            if (hasMethod==false) {
                messagesFaltanMetodosEventos.append("@WhenEvent\n");
                messagesFaltanMetodosEventos.append("public void " + methodName + "(" + modelClass.getSimpleName() + " " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,modelClass.getSimpleName()) + ", " + modelClass.getSimpleName() + " original, EventContext eventContext) {\n");
                messagesFaltanMetodosEventos.append("\n");
                messagesFaltanMetodosEventos.append("}\n");
                messagesFaltanMetodosEventos.append("\n");
            }
        }
        for (Enum event : CommonEvent.class.getEnumConstants()) {
            String methodName = "trigger" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, event.name());
            boolean hasMethod = ReflectionUtil.hasMethod(this.getClass(), methodName, void.class, WhenEvent.class, new Class<?>[]{modelClass,modelClass, EventContext.class});

            if (hasMethod==false) {
                messagesFaltanMetodosEventos.append("@WhenEvent\n");
                messagesFaltanMetodosEventos.append("public void " + methodName + "(" + modelClass.getSimpleName() + " " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,modelClass.getSimpleName()) + ", " + modelClass.getSimpleName() + " original, EventContext eventContext) {\n");
                messagesFaltanMetodosEventos.append("\n");
                messagesFaltanMetodosEventos.append("}\n");
                messagesFaltanMetodosEventos.append("\n\n");
            }
        }


        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(WhenEvent.class)) {
                String methodName = method.getName();
                boolean matchesEventClass = Arrays.stream(eventClass.getEnumConstants())
                        .anyMatch(event -> methodName.equals("trigger" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, event.name())));
                boolean matchesEventoComunClass = Arrays.stream(CommonEvent.class.getEnumConstants())
                        .anyMatch(event -> methodName.equals("trigger" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, event.name())));


                if (matchesEventClass==false && matchesEventoComunClass==false) {
                    messagesSobranMetodosEventos.append("@WhenEvent " + methodName);
                }
            }
        }


        for (Estado state : stateClass.getEnumConstants()) {
            String methodName = "onEnter" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, state.name());
            boolean hasMethod = ReflectionUtil.hasMethod(this.getClass(), methodName, void.class, OnEnterState.class, new Class<?>[]{modelClass, EventContext.class});
            if (hasMethod==false) {
                messagesFaltanMetodosEstados.append("@OnEnterState\n");
                messagesFaltanMetodosEstados.append("public void " + methodName + "(" + modelClass.getSimpleName() + " " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,modelClass.getSimpleName()) + ", EventContext eventContext) {\n");
                messagesFaltanMetodosEstados.append("\n");
                messagesFaltanMetodosEstados.append("}\n");
                messagesFaltanMetodosEstados.append("\n");
            }
        }


        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(OnEnterState.class)) {
                String methodName = method.getName();
                boolean matches = Arrays.stream(stateClass.getEnumConstants())
                        .anyMatch(state -> methodName.equals("onEnter" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, state.name())));
                if (!matches) {
                    messagesSobranMetodosEstados.append("@OnEnterState: " + methodName+"\n");
                }
            }
        }

        StringBuilder messages = new StringBuilder();
        if (messagesFaltanMetodosEventos.length()>0) {
            messages.append("\n\n\nFaltan métodos para los eventos*****:\n\n"+ messagesFaltanMetodosEventos.toString());
        }
        if (messagesSobranMetodosEventos.length()>0) {
            messages.append("\n\n\nSobran métodos para los eventos******:\n"+ messagesSobranMetodosEventos.toString());
        }
        if (messagesFaltanMetodosEstados.length()>0) {
            messages.append("\n\n\nFaltan métodos para los estados*******:\n"+ messagesFaltanMetodosEstados.toString());
        }
        if (messagesSobranMetodosEstados.length()>0) {
            messages.append("\n\n\nSobran métodos para los estados******:\n"+ messagesSobranMetodosEstados.toString());
        }


        if (messages.length()>0) {
            messages.append("\n\n\n");
            throw new RuntimeException(messages.toString());
        }
    }
}
