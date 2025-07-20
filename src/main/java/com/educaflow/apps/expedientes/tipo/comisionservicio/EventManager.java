package com.educaflow.apps.expedientes.tipo.comisionservicio;

import com.axelor.inject.Beans;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.ComisionServicio;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.repo.ComisionServicioRepository;
import com.google.inject.Inject;



public class EventManager extends com.educaflow.apps.expedientes.common.EventManager<ComisionServicio, ComisionServicio.State, ComisionServicio.Event,ComisionServicio.Profile> {

    private final ComisionServicioRepository repository;

    @Inject
    public EventManager(ComisionServicioRepository repository) {
        super(ComisionServicio.class, ComisionServicio.State.class, ComisionServicio.Event.class,ComisionServicio.Profile.class);
        this.repository = repository;
    }

    @Override
    public void triggerInitialEvent(ComisionServicio comisionServicio, EventContext eventContext) {

    }



    @WhenEvent
    public void triggerPresentar(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        //comisionServicio.updateState(ComisionServicio.Estado.);
    }
    @WhenEvent
    public void triggerBack(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        //comisionServicio.updateState(ComisionServicio.Estado.);
    }
    @WhenEvent
    public void triggerPresentarDocumentosFirmados(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        //comisionServicio.updateState(ComisionServicio.Estado.);
    }
    @WhenEvent
    public void triggerResolver(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        //comisionServicio.updateState(ComisionServicio.Estado.);
    }

    @WhenEvent
    public void triggerDelete(ComisionServicio comisionServicio, ComisionServicio original, EventContext eventContext) {
        //comisionServicio.updateState(ComisionServicio.Estado.);
    }






    @OnEnterState
    public void onEnterEntradaDatos(ComisionServicio comisionServicio, EventContext eventContext) {
        //comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.);
    }
    @OnEnterState
    public void onEnterFirmaPorUsuario(ComisionServicio comisionServicio, EventContext eventContext) {
        //comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.);
    }
    @OnEnterState
    public void onEnterResolverPermitirComision(ComisionServicio comisionServicio, EventContext eventContext) {
        //comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.);
    }
    @OnEnterState
    public void onEnterEntregaTickets(ComisionServicio comisionServicio, EventContext eventContext) {
        //comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.);
    }
    @OnEnterState
    public void onEnterAceptado(ComisionServicio comisionServicio, EventContext eventContext) {
        //comisionServicio.setCurrentActionProfiles(ComisionServicio.Profile.);
    }












}