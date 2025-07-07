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

public abstract class EventManager<T extends Expediente, Estado extends Enum<Estado>, Evento extends Enum<Evento>, Profile extends Enum<Profile> > {

    final private String VIEW_NAME_DEFAULT_FORMAT ="exp-${EXPEDIENT_CODE}-${ROLE_NAME}-${STATE}-form";

    private final Class<T> modelClass;
    private final Class<Estado> stateClass;
    private final Class<Evento> eventClass;
    private final Class<Profile> profileClass;

    public EventManager(Class<T> modelClass, Class<Estado> stateClass, Class<Evento> eventClass, Class<Profile> profileClass) {
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
            throw new RuntimeException("Error al invocar el evento: " + strEvent , ex);
        }
    }

    public void onEnterState(T expediente, EventContext<Profile> eventContext) {
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

    public String getViewName(T expediente, EventContext<Profile> eventContext) {


        String viewName=getDefaultViewName(expediente.getTipoExpediente().getCode(), eventContext.getProfile(),(Estado)ReflectionUtil.getEnumConstant(stateClass,expediente.getCodeState()));

        if (existsView(viewName)==false) {
            throw new RuntimeException("No existe la vista:" + viewName + "en el expediente:" + expediente + " en el contexto:" + eventContext);
        }

        return viewName;

    }

    private boolean existsView(String viewName) {
        return AxelorViewUtil.existsView(viewName,"form",this.getModelClass().getName());
    }

    private String getDefaultViewName(String tipoExpedienteCode, Profile profile, Estado estado) {
        String viewName = VIEW_NAME_DEFAULT_FORMAT.replace("${EXPEDIENT_CODE}", tipoExpedienteCode)
                .replace("${ROLE_NAME}", profile.name())
                .replace("${STATE}", estado.name());

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
    public Class<Profile> getProfileClass() {
        return profileClass;
    }

    public void validateExpediente() {
        StringBuilder messagesFaltanMetodosEventos = new StringBuilder();
        StringBuilder messagesSobranMetodosEventos = new StringBuilder();
        StringBuilder messagesFaltanMetodosEstados = new StringBuilder();
        StringBuilder messagesSobranMetodosEstados = new StringBuilder();
        StringBuilder messagesFaltanVistas = new StringBuilder();
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


        String baseViewName= "exp-" + modelClass.getSimpleName() + "-Base";
        if (AxelorViewUtil.existsView(baseViewName,modelClass.getSimpleName(),"form")==false) {
            //messagesFaltanVistas.append("Falta la vista: " + baseViewName + "\n");
            System.out.println("Falta la vista: " + baseViewName + "\n");
        }
        for (Profile profile : profileClass.getEnumConstants()) {
            for (Estado state : stateClass.getEnumConstants()) {
                String viewName = getDefaultViewName(modelClass.getSimpleName(), profile, state);

                if (AxelorViewUtil.existsView(viewName,modelClass.getSimpleName(),"form")==false) {
                    //messagesFaltanVistas.append("Falta la vista: " + viewName + "\n");
                    System.out.println("Falta la vista: " + viewName + "\n");
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
        if (messagesFaltanVistas.length()>0) {
            messages.append("\n\n\nFaltan las siguientes vistas******:\n"+ messagesFaltanVistas.toString());
        }

        if (messages.length()>0) {
            messages.append("\n\n\n");
            throw new RuntimeException(messages.toString());
        }
    }
}
