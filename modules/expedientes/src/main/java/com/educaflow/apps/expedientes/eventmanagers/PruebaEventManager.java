package com.educaflow.apps.expedientes.eventmanagers;

import com.educaflow.apps.expedientes.common.Contexto;
import com.educaflow.apps.expedientes.common.EventManager;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.ViewForState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
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
    public void triggerInitialEvent(TipoExpediente tipoExpediente,Contexto contexto) {

    }

    @WhenEvent
    public void triggerGuardarDatos(Prueba prueba,Prueba pruebaOriginal, Contexto contexto) {
        prueba.changeState(Prueba.Estado.DATOS_GUARDADOS);
    }

    @WhenEvent
    public void triggerPresentar(Prueba prueba,Prueba pruebaOriginal, Contexto contexto) {
        prueba.changeState(Prueba.Estado.REVISION);
    }


    @WhenEvent
    public void triggerSubsanar(Prueba prueba,Prueba pruebaOriginal, Contexto contexto) {
        prueba.changeState(Prueba.Estado.DATOS_GUARDADOS);
    }

    @WhenEvent
    public void triggerAceptar(Prueba prueba,Prueba pruebaOriginal, Contexto contexto) {
        prueba.changeState(Prueba.Estado.ACEPTADO);
        prueba.setAbierto(false);
    }

    @WhenEvent
    public void triggerRechazar(Prueba prueba,Prueba pruebaOriginal, Contexto contexto) {
        prueba.changeState(Prueba.Estado.RECHAZADO);
        prueba.setAbierto(false);
    }



    @OnEnterState
    public void onEnterDatosGuardados(Prueba prueba, Contexto contexto) {
        System.out.println("onDatosGuardados");
    }

    @OnEnterState
    public void onEnterRevision(Prueba prueba, Contexto contexto) {
        System.out.println("onRevision");
    }

    @OnEnterState
    public void onEnterAceptado(Prueba prueba, Contexto contexto) {
        System.out.println("onAceptado");
    }

    @OnEnterState
    public void onEnterRechazado(Prueba prueba, Contexto contexto) {
        System.out.println("onRechazado");
    }

    @Override
    public String getViewForNullState(TipoExpediente tipoExpediente, Contexto contexto) {
        return "form-expediente-prueba-estado-inicial-form";
    }

    @ViewForState
    public String getViewForDatosGuardados(Prueba prueba, Contexto contexto) {
        return "form-expediente-prueba-datos-guardados-form";
    }

    @ViewForState
    public String getViewForRevision(Prueba prueba, Contexto contexto) {
        return "form-expediente-prueba-revision-form";
    }

    @ViewForState
    public String getViewForAceptado(Prueba prueba, Contexto contexto) {
        return "form-expediente-prueba-aceptado-form";
    }


    @ViewForState
    public String getViewForRechazado(Prueba prueba, Contexto contexto) {
        return "form-expediente-prueba-rechazado-form";
    }



}
