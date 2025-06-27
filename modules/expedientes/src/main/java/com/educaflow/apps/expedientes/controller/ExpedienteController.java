package com.educaflow.apps.expedientes.controller;

import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.rpc.*;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.EventManager;
import com.educaflow.apps.expedientes.common.Profile;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.ExpedienteHistorialEstados;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.repo.TipoExpedienteRepository;
import com.educaflow.common.util.*;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDateTime;
import java.util.Map;


public class ExpedienteController {


    @Inject
    TipoExpedienteRepository tipoExpedienteRepository;


    public ExpedienteController() {;

    }

    @CallMethod
    @Transactional
    public void triggerInitialEvent(ActionRequest request, ActionResponse response) {
        try {
            TipoExpediente tipoExpediente = getTipoExpediente(request);
            EventManager eventManager=getEventManager(tipoExpediente);
            EventContext eventContext = getEventContext(request);
            JpaRepository<Expediente> expedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());


            Expediente expediente=eventManager.triggerInitialEvent(tipoExpediente, eventContext);
            addHistorialEstado(expediente,null);
            eventManager.onEnterState(expediente, eventContext);


            saveExpediente(expedienteRepository,expediente);

            String viewName = eventManager.getViewForState(expediente, eventContext);
            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente,expediente.getName());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @CallMethod
    @Transactional
    public void triggerEvent(ActionRequest request, ActionResponse response) {
        try {
            String eventName=getEventName(request);
            Expedientes expedientes=getExpedientes(request);
            Expediente expedienteOriginal=expedientes.getOriginalExpediente();
            Expediente expediente=expedientes.getCurrentExpediente();
            EventContext eventContext = getEventContext(request);
            EventManager eventManager=getEventManager(expediente.getTipoExpediente());
            JpaRepository<Expediente> expedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());


            String originalState =  expedienteOriginal.getCodeState();
            eventManager.triggerEvent(eventName, expediente, expedienteOriginal, eventContext);
            String newState = expediente.getCodeState();

            addHistorialEstado(expediente, eventName);

            if (newState.equals(originalState)==false) {
                eventManager.onEnterState(expediente, eventContext);
            }

            saveExpediente(expedienteRepository,expediente);

            String viewName = eventManager.getViewForState(expediente, eventContext);
            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente,expediente.getName());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @CallMethod
    @Transactional
    public void borrarExpediente(ActionRequest request, ActionResponse response) {
        try {
            Expedientes expedientes=getExpedientes(request);
            Expediente expediente=expedientes.getCurrentExpediente();
            EventContext eventContext = getEventContext(request);
            EventManager eventManager=getEventManager(expediente.getTipoExpediente());
            JpaRepository<Expediente> expedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());

            removeExpediente(expedienteRepository,expediente);

            response.setSignal("refresh-app",null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @CallMethod
    public void viewExpediente(ActionRequest request, ActionResponse response) {
        try {
            Expedientes expedientes=getExpedientes(request);
            Expediente expediente=expedientes.getCurrentExpediente();
            EventContext eventContext = getEventContext(request);
            EventManager eventManager=getEventManager(expediente.getTipoExpediente());

            String viewName = eventManager.getViewForState(expediente, eventContext);

            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente,expediente.getName());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    /*******************************************************************/
    /********************** Funciones de Negocio  **********************/
    /*******************************************************************/

    private static void addHistorialEstado(Expediente expediente, String eventName) {
        ExpedienteHistorialEstados historialEstado = new ExpedienteHistorialEstados();
        historialEstado.setCodeState(expediente.getCodeState());
        historialEstado.setNameState(TextUtil.getHumanCaseFromScreamingSnakeCase(expediente.getCodeState()));
        historialEstado.setCodeEvent((eventName!=null)?eventName:"");
        historialEstado.setNameEvent((eventName!=null)?TextUtil.getHumanCaseFromScreamingSnakeCase(eventName):"");
        historialEstado.setFecha(LocalDateTime.now());
        expediente.addHistorialEstado(historialEstado);
    }



    /*******************************************************************/
    /*************** Obtener los datos del ActionRequest ***************/
    /*******************************************************************/

    private TipoExpediente getTipoExpediente(ActionRequest request) {
        long id=objectToLong(getActionRequestContext(request).get("id"));

        TipoExpediente tipoExpediente=findTipoExpediente(tipoExpedienteRepository,id);

        return tipoExpediente;
    }

    private Expedientes getExpedientes(ActionRequest request) {
        long id=objectToLong(getActionRequestContext(request).get("id"));
        JpaRepository<Expediente> expedienteRepository =getJpaRepository(id);

        //No cambiar el orden de estas 3 lineas
        Expediente expediente=findExpediente(expedienteRepository,id);
        Expediente expedienteOriginal=(Expediente) BeanUtil.cloneEntity(expediente.getClass(), expediente);
        BeanUtil.copyMapToEntity(expediente.getClass(),getActionRequestContext(request),expediente);


        return new  Expedientes(expediente,expedienteOriginal);
    }


    private String getEventName(ActionRequest request) {

        String eventName=(String)getActionRequestContext(request).get("_signal");

        if (eventName==null) {
            throw new RuntimeException("eventName is null");
        }

        return eventName;
    }


    private EventContext getEventContext(Request request) {
        //String signal=(String)request.getData().get("_signal");
        Profile profile=Profile.CREADOR; //signal!=null ? Profile.valueOf(signal) : null;
        return new EventContext(profile);
    }


    /*******************************************************************/
    /******************* Funciones de Acceso a datos *******************/
    /*******************************************************************/


    private <T extends Expediente> void saveExpediente(JpaRepository<T> jpaRepository, T expediente) {
        jpaRepository.save(expediente);
    }

    private <T extends Expediente> void removeExpediente(JpaRepository<T> jpaRepository, T expediente) {
        jpaRepository.remove(expediente);
    }

    private <T extends Expediente> T findExpediente(JpaRepository<T> jpaRepository, long id) {
        return jpaRepository.find(id);
    }

    private <T extends TipoExpediente> T findTipoExpediente(JpaRepository<T> jpaRepository, long id) {
        return jpaRepository.find(id);
    }


    /*******************************************************************/
    /********************** Funciones de Utilidad **********************/
    /*******************************************************************/

    public static  EventManager getEventManager(TipoExpediente tipoExpediente) {
        try {
            if (tipoExpediente == null) {
                throw new RuntimeException("No existe el tipo del expediente a crear.");
            }
            String fqcnEventManager = tipoExpediente.getFqcnEventManager();
            if (fqcnEventManager == null || fqcnEventManager.isEmpty()) {
                throw new RuntimeException("No existe el fqcnEventManager para el tipo de expediente: " + tipoExpediente.getName());
            }
            Class<EventManager> eventManagerClass = (Class<EventManager>) Class.forName(tipoExpediente.getFqcnEventManager());

            EventManager eventManager = (EventManager) Beans.get(eventManagerClass);

            return eventManager;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private long objectToLong(Object obj) {
        return ((Number)obj).longValue();
    }


    private Map<String,Object> getActionRequestContext(ActionRequest request) {
         Map<String,Object> requestcontext=(Map<String,Object>) request.getData().get("context");

         if (requestcontext == null) {
             throw new RuntimeException("requestcontext es null");
         }
         return (Map<String,Object>) requestcontext;
    }


    private JpaRepository<Expediente> getJpaRepository(long idExpediente) {
        JpaRepository<Expediente> onlyExpedienteRepository = AxelorDBUtil.getRepository(Expediente.class);
        Expediente expediente=onlyExpedienteRepository.find(idExpediente);
        EventManager eventManager=getEventManager(expediente.getTipoExpediente());
        JpaRepository<Expediente> realExpedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());
        JPA.em().detach(expediente);

        return realExpedienteRepository;
    }



    private class Expedientes {
        final private Expediente currentExpediente;
        final private Expediente originalExpediente;

        public Expedientes(Expediente currentExpedient, Expediente originalExpediente) {
            this.currentExpediente = currentExpedient;
            this.originalExpediente = originalExpediente;
        }

        public Expediente getCurrentExpediente() {
            return currentExpediente;
        }

        public Expediente getOriginalExpediente() {
            return originalExpediente;
        }

    }


}
