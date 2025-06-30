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
import com.educaflow.common.util.mapper.BeanMapperModel;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
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
            expediente.setTipoExpediente(tipoExpediente);
            if (expediente.getCodeState()==null) {
                throw new RuntimeException("El estado del expediente no puede ser null");
            }
            updateState(expediente,eventManager.getStateClass());
            updateName(expediente);
            addHistorialEstado(expediente,null);
            eventManager.onEnterState(expediente, eventContext);


            saveExpediente(expedienteRepository,expediente);

            String viewName = eventManager.getViewName(expediente, eventContext);
            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente,expediente.getName(),eventContext.getProfile().name());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @CallMethod
    @Transactional
    public void triggerEvent(ActionRequest request, ActionResponse response) {
        try {
            String eventName=getEventName(request);
            EventContext eventContext = getEventContext(request);
            Expedientes expedientes=getExpedientes(request,eventName, eventContext);
            Expediente expedienteOriginal=expedientes.getOriginalExpediente();
            Expediente expediente=expedientes.getCurrentExpediente();

            EventManager eventManager=getEventManager(expediente.getTipoExpediente());
            JpaRepository<Expediente> expedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());


            String originalState =  expedienteOriginal.getCodeState();
            eventManager.triggerEvent(eventName, expediente, expedienteOriginal, eventContext);
            String newState = expediente.getCodeState();

            if (newState.equals(originalState)==false) {
                updateState(expediente,eventManager.getStateClass());
                addHistorialEstado(expediente,eventName);
                eventManager.onEnterState(expediente, eventContext);
            }

            saveExpediente(expedienteRepository,expediente);

            String viewName = eventManager.getViewName(expediente, eventContext);
            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente,expediente.getName(),eventContext.getProfile().name());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @CallMethod
    @Transactional
    public void borrarExpediente(ActionRequest request, ActionResponse response) {
        try {
            EventContext eventContext = getEventContext(request);
            Expedientes expedientes=getExpedientes(request,null, eventContext);
            Expediente expediente=expedientes.getCurrentExpediente();

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
            EventContext eventContext = getEventContext(request);
            Expedientes expedientes=getExpedientes(request,null, eventContext);
            Expediente expediente=expedientes.getCurrentExpediente();
            EventManager eventManager=getEventManager(expediente.getTipoExpediente());

            String viewName = eventManager.getViewName(expediente, eventContext);

            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente,expediente.getName(),eventContext.getProfile().name());

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

    private void updateState(Expediente expediente,Class<? extends Enum> enumClass) {
        assertValidState(expediente,enumClass);


        expediente.setNameState(com.educaflow.common.util.TextUtil.getHumanCaseFromScreamingSnakeCase(expediente.getCodeState()));
        expediente.setFechaUltimoEstado(java.time.LocalDateTime.now());
    }

    private void updateName(Expediente expediente) {
        expediente.setName(expediente.getTipoExpediente().getName());
    }


    void assertValidState(Expediente expediente,Class<? extends Enum> enumClass) {
        String stateCode=expediente.getCodeState();
        boolean isValid = Arrays.stream(enumClass.getEnumConstants()).anyMatch(enumConstant -> stateCode.equals(enumConstant.name()));

        if (isValid==false) {
            throw new IllegalArgumentException("Invalid state code '" + stateCode + "'  "+enumClass.getSimpleName());
        }
    }


    /*******************************************************************/
    /*************** Obtener los datos del ActionRequest ***************/
    /*******************************************************************/

    private TipoExpediente getTipoExpediente(ActionRequest request) {
        long id=objectToLong(getActionRequestContext(request).get("id"));

        TipoExpediente tipoExpediente=findTipoExpediente(tipoExpedienteRepository,id);

        return tipoExpediente;
    }

    private Expedientes getExpedientes(ActionRequest request,String eventName,EventContext eventContext) {
        long id=objectToLong(getActionRequestContext(request).get("id"));
        JpaRepository<Expediente> expedienteRepository =getJpaRepository(id);

        //No cambiar el orden de estas 3 lineas
        Expediente expediente=findExpediente(expedienteRepository,id);
        Expediente expedienteOriginal=(Expediente) BeanMapperModel.getEntityCloned(expediente.getClass(), expediente);
        populateExpedienteFromActionRequest(expediente,request,eventName, eventContext);

        return new  Expedientes(expediente,expedienteOriginal);
    }

    /**
     * Esta es la funcion más importante ya que pasa los datos del GUI al modelo
     * Y hay que comprobar que se puede pasar y que no se puede.
     * @param expediente
     * @param request
     * @param eventName
     * @param eventContext
     */
    private void populateExpedienteFromActionRequest(Expediente expediente, ActionRequest request,String eventName, EventContext eventContext) {
        if (eventName!=null) {
            BeanMapperModel.copyMapToEntity(expediente.getClass(), getActionRequestContext(request), expediente);
        }
    }


    private String getEventName(ActionRequest request) {

        String eventName=(String)getActionRequestContext(request).get("_signal");

        if (eventName==null) {
            throw new RuntimeException("eventName is null");
        }

        return eventName;
    }


    private EventContext getEventContext(ActionRequest request) {
        String profileName=(String)getActionRequestContext(request).get("_profile");
        if (profileName==null) {
            throw new RuntimeException("profileName is null");
        }

        Profile profile=Profile.valueOf(profileName);
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

    private static  EventManager getEventManager(TipoExpediente tipoExpediente) {
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

    private Map<String,Object> getActionRequestContext(ActionRequest request) {
         Map<String,Object> requestcontext=(Map<String,Object>) request.getData().get("context");

         if (requestcontext == null) {
             throw new RuntimeException("requestcontext es null");
         }
         return (Map<String,Object>) requestcontext;
    }


    /**
     * Obtiene el Repository de un expediente en función del id del expediente.
     * Se usa este método porque de otra forma se retornaría el Repositorio de Expediente y no del expdiente en concreto.
     * @param idExpediente
     * @return
     */
    private JpaRepository<Expediente> getJpaRepository(long idExpediente) {
        JpaRepository<Expediente> onlyExpedienteRepository = AxelorDBUtil.getRepository(Expediente.class);
        Expediente expediente=onlyExpedienteRepository.find(idExpediente);
        EventManager eventManager=getEventManager(expediente.getTipoExpediente());
        JpaRepository<Expediente> realExpedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());
        JPA.em().detach(expediente);

        return realExpedienteRepository;
    }


    private long objectToLong(Object obj) {
        return ((Number)obj).longValue();
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
