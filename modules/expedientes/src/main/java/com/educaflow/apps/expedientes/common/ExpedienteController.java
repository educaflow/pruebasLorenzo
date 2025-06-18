package com.educaflow.apps.expedientes.common;

import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class ExpedienteController {

    private final Injector injector;

    @Inject
    public ExpedienteController(Injector injector) {
        this.injector = injector;
    }

    @CallMethod
    public void triggerInitialEvent(ActionRequest request, ActionResponse response) {
        try {
            TipoExpediente tipoExpediente=request.getContext().asType(TipoExpediente.class);
            EventManager eventManager=getEventManager(tipoExpediente);

            eventManager.triggerInitialEvent(request,response);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @CallMethod
    public void triggerEvent(ActionRequest request, ActionResponse response) {
        try {
            Expediente expediente=request.getContext().asType(Expediente.class);
            TipoExpediente tipoExpediente=expediente.getTipoExpediente();
            EventManager eventManager=getEventManager(tipoExpediente);
            String eventName=(String)request.getContext().get("_signal");

            String newState=eventManager.triggerEvent(eventName, request, response);
            if (newState != null && !newState.isEmpty()) {
                eventManager.onState(newState, request, response);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }




    /********************************************/
    /************ Funciones Privadas ************/
    /********************************************/


    private EventManager getEventManager(TipoExpediente tipoExpediente) {
        try {
            if (tipoExpediente == null) {
                throw new RuntimeException("No existe el tipo del expediente a crear.");
            }
            String fqcnEventManager = tipoExpediente.getFqcnEventManager();
            if (fqcnEventManager == null || fqcnEventManager.isEmpty()) {
                throw new RuntimeException("No existe el EventManager para el tipo de expediente: " + tipoExpediente.getName());
            }
            Class<EventManager> eventManagerClass = (Class<EventManager>) Class.forName(tipoExpediente.getFqcnEventManager());

            EventManager eventManager = (EventManager) injector.getInstance(eventManagerClass);

            return eventManager;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }



}
