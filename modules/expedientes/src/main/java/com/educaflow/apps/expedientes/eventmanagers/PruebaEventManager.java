package com.educaflow.apps.expedientes.eventmanagers;

import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.EventManager;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.ViewForState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.Prueba;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.repo.PruebaRepository;
import com.google.inject.Inject;


public class PruebaEventManager extends EventManager<Prueba,Prueba.Estado,Prueba.Evento> {

    private final PruebaRepository pruebaRepository;

    @Inject
    public PruebaEventManager(PruebaRepository pruebaRepository) {
        super(Prueba.class, Prueba.Estado.class, Prueba.Evento.class);
        this.pruebaRepository = pruebaRepository;
    }

    @Override
    public Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext eventContext) {
        Prueba prueba=new Prueba();
        prueba.setTipoExpediente(tipoExpediente);
        prueba.changeState(Prueba.Estado.ENTRADA_DATOS);

        return prueba;
    }


    @WhenEvent
    public void triggerPresentar(Prueba prueba,Prueba pruebaOriginal, EventContext eventContext) {
        prueba.changeState(Prueba.Estado.REVISION);
    }


    @WhenEvent
    public void triggerSubsanar(Prueba prueba,Prueba pruebaOriginal, EventContext eventContext) {
        prueba.changeState(Prueba.Estado.ENTRADA_DATOS);
    }

    @WhenEvent
    public void triggerAceptar(Prueba prueba,Prueba pruebaOriginal, EventContext eventContext) {
        prueba.changeState(Prueba.Estado.ACEPTADO);
        prueba.setAbierto(false);
    }

    @WhenEvent
    public void triggerRechazar(Prueba prueba,Prueba pruebaOriginal, EventContext eventContext) {
        prueba.changeState(Prueba.Estado.RECHAZADO);
        prueba.setAbierto(false);
    }



    @OnEnterState
    public void onEnterEntradaDatos(Prueba prueba, EventContext eventContext) {
        System.out.println("onEnterEntradaDatos");
    }

    @OnEnterState
    public void onEnterRevision(Prueba prueba, EventContext eventContext) {
        System.out.println("onRevision");
    }

    @OnEnterState
    public void onEnterAceptado(Prueba prueba, EventContext eventContext) {
        System.out.println("onAceptado");
    }

    @OnEnterState
    public void onEnterRechazado(Prueba prueba, EventContext eventContext) {
        System.out.println("onRechazado");
    }


}
