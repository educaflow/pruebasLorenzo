package com.educaflow.apps.expedientes.common;

import com.axelor.db.JpaRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.ExpedienteHistorialEstados;
import com.educaflow.apps.expedientes.db.Prueba;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.common.util.AxelorDBUtil;
import com.educaflow.common.util.AxelorViewUtil;
import com.educaflow.common.util.BeanUtil;
import com.educaflow.common.util.TextUtil;
import com.google.inject.persist.Transactional;

import java.time.LocalDateTime;


public class ExpedienteController {


    public ExpedienteController() {;
    }

    public void triggerInitialEvent(ActionRequest request, ActionResponse response) {
        try {
            TipoExpediente tipoExpedienteProxy = request.getContext().asType(TipoExpediente.class);
            TipoExpediente tipoExpediente = BeanUtil.cloneEntity(TipoExpediente.class, tipoExpedienteProxy);
            EventManager eventManager=getEventManager(tipoExpediente);
            Contexto contexto = getContextoFromRequest(request);

            eventManager.triggerInitialEvent(tipoExpediente, contexto);
            Expediente expediente = (Expediente) eventManager.getModelClass().getDeclaredConstructor().newInstance();
            expediente.setTipoExpediente(tipoExpediente);

            String viewName = eventManager.getViewForNullState(tipoExpediente, contexto);

            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Transactional
    public void triggerEvent(ActionRequest request, ActionResponse response) {
        try {
            Expediente expedienteProxy = request.getContext().asType(Expediente.class);
            TipoExpediente tipoExpedienteProxy = expedienteProxy.getTipoExpediente();
            EventManager eventManager=getEventManager(tipoExpedienteProxy);
            String eventName=(String)request.getContext().get("_signal");
            Contexto contexto = getContextoFromRequest(request);
            JpaRepository jpaRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());

            if (expedienteProxy!=null) {
                Prueba prueba=(Prueba)expedienteProxy;
                System.out.println(prueba.getJustificante());
            }


            Expediente expediente;
            Expediente expedienteOriginal;
            if ((expedienteProxy!=null) && (expedienteProxy.getId() != null)) {
                expediente = (Expediente) jpaRepository.find(expedienteProxy.getId());
                expedienteOriginal=(Expediente) BeanUtil.cloneEntity(eventManager.getModelClass(), expediente);
                BeanUtil.copyEntity(eventManager.getModelClass(),expedienteProxy,expediente);
            } else {
                expediente = (Expediente) eventManager.getModelClass().getDeclaredConstructor().newInstance();
                expedienteOriginal=null;
                BeanUtil.copyEntity(eventManager.getModelClass(),expedienteProxy,expediente);
            }

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
            String eventName=(String)request.getContext().get("_signal");
            Contexto contexto = getContextoFromRequest(request);

            String viewName = eventManager.getViewForState(expediente, contexto);

            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }



    private Contexto getContextoFromRequest(ActionRequest request) {
        return new Contexto();
    }



    public static  EventManager getEventManager(TipoExpediente tipoExpediente) {
        try {
            if (tipoExpediente == null) {
                throw new RuntimeException("No existe el tipo del expediente a crear.");
            }
            String fqcnEventManager = tipoExpediente.getFqcnEventManager();
            if (fqcnEventManager == null || fqcnEventManager.isEmpty()) {
                throw new RuntimeException("No existe el EventManager para el tipo de expediente: " + tipoExpediente.getName());
            }
            Class<EventManager> eventManagerClass = (Class<EventManager>) Class.forName(tipoExpediente.getFqcnEventManager());

            EventManager eventManager = (EventManager) Beans.get(eventManagerClass);

            return eventManager;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}
