package com.educaflow.apps.expedientes.common;

import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.rpc.*;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.ExpedienteHistorialEstados;
import com.educaflow.apps.expedientes.db.Prueba;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.repo.ExpedienteRepository;
import com.educaflow.apps.expedientes.db.repo.TipoExpedienteRepository;
import com.educaflow.common.util.*;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExpedienteController {


    @Inject
    TipoExpedienteRepository tipoExpedienteRepository;

    public ExpedienteController() {;

    }

    @Transactional
    public void triggerInitialEvent(ActionRequest request, ActionResponse response) {
        try {
            Map<String,Object> context=(Map<String,Object>)request.getData().get("context");
            long id=((Integer)context.get("id")).longValue();
            TipoExpediente tipoExpediente=tipoExpedienteRepository.find(id);
            EventManager eventManager=getEventManager(tipoExpediente);
            Contexto contexto = getContextoFromRequest(request);

            Expediente expediente=eventManager.triggerInitialEvent(tipoExpediente, contexto);

            JpaRepository jpaRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());


            ExpedienteHistorialEstados historialEstado = new ExpedienteHistorialEstados();
            historialEstado.setCodeState(expediente.getCodeState());
            historialEstado.setNameState(TextUtil.getHumanCaseFromScreamingSnakeCase(expediente.getCodeState()));
            historialEstado.setCodeEvent("");
            historialEstado.setNameEvent("");
            historialEstado.setFecha(LocalDateTime.now());
            expediente.addHistorialEstado(historialEstado);

            eventManager.onEnterState(expediente, contexto);


            jpaRepository.save(expediente);

            String viewName = eventManager.getViewForState(expediente, contexto);

            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Transactional
    public Expediente triggerEvent(Request request, ActionResponse response) {
        try {

            Expediente expedienteProxy = new Expediente();

            if (request.getData().containsKey("_original")) {
                BeanUtil.copyMapToEntity(Expediente.class, (Map<String, Object>) request.getData().get("_original"), expedienteProxy);
            }
            BeanUtil.copyMapToEntity(Expediente.class,request.getData(),expedienteProxy);
            TipoExpediente tipoExpedienteProxy = expedienteProxy.getTipoExpediente();
            EventManager eventManager=getEventManager(tipoExpedienteProxy);
            String eventName=expedienteProxy.getCurrentEvent();
            if (eventName==null) {
                throw new RuntimeException("eventName is null");
            }
            Contexto contexto = getContextoFromRequest(request);
            JpaRepository jpaRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());

            Expediente expediente;
            Expediente expedienteOriginal;
            expediente = (Expediente) jpaRepository.find(expedienteProxy.getId());
            expedienteOriginal=(Expediente) BeanUtil.cloneEntity(eventManager.getModelClass(), expediente);
            BeanUtil.copyMapToEntity(eventManager.getModelClass(),request.getData(),expediente);


            String originalState = (expedienteOriginal == null) ? null : expedienteOriginal.getCodeState();
            eventManager.triggerEvent(eventName, expediente, expedienteOriginal, contexto);
            String newState = expediente.getCodeState();

            ExpedienteHistorialEstados historialEstado = new ExpedienteHistorialEstados();
            historialEstado.setCodeState(expediente.getCodeState());
            historialEstado.setNameState(TextUtil.getHumanCaseFromScreamingSnakeCase(expediente.getCodeState()));
            historialEstado.setCodeEvent(eventName);
            historialEstado.setNameEvent(TextUtil.getHumanCaseFromScreamingSnakeCase(eventName));
            historialEstado.setFecha(LocalDateTime.now());
            expediente.addHistorialEstado(historialEstado);

            if (newState.equals(originalState)==false) {
                eventManager.onEnterState(expediente, contexto);
            }


            jpaRepository.save(expediente);

            String viewName = eventManager.getViewForState(expediente, contexto);


            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente);

            return expediente;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @CallMethod
    @Transactional
    public void borrarExpediente(ActionRequest request, ActionResponse response) {
        try {
            Expediente expedienteProxy = request.getContext().asType(Expediente.class);
            TipoExpediente tipoExpedienteProxy = expedienteProxy.getTipoExpediente();
            EventManager eventManager=getEventManager(tipoExpedienteProxy);
            Contexto contexto = getContextoFromRequest(request);


            JpaRepository jpaRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());

            Expediente expediente = (Expediente) jpaRepository.find(expedienteProxy.getId());

            jpaRepository.remove(expediente);

            String viewName="action-nuevo-expediente";
            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @CallMethod
    public void viewExpediente(ActionRequest request, ActionResponse response) {
        try {
            Expediente expedienteProxy = request.getContext().asType(Expediente.class);
            TipoExpediente tipoExpedienteProxy = expedienteProxy.getTipoExpediente();
            EventManager eventManager=getEventManager(tipoExpedienteProxy);
            Expediente expediente = (Expediente) BeanUtil.cloneEntity(eventManager.getModelClass(), expedienteProxy);
            Contexto contexto = getContextoFromRequest(request);

            String viewName = eventManager.getViewForState(expediente, contexto);

            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }



    private Contexto getContextoFromRequest(Request request) {
        return new Contexto();
    }



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


}
