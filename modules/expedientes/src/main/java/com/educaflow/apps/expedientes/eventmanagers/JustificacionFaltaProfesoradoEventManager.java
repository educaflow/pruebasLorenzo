package com.educaflow.apps.expedientes.eventmanagers;

import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.EventManager;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.Prueba;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.repo.JustificacionFaltaProfesoradoRepository;
import com.educaflow.apps.expedientes.db.repo.PruebaRepository;
import com.google.inject.Inject;

import java.time.Year;

public class JustificacionFaltaProfesoradoEventManager extends EventManager<JustificacionFaltaProfesorado,JustificacionFaltaProfesorado.Estado,JustificacionFaltaProfesorado.Evento> {


    private final JustificacionFaltaProfesoradoRepository justificacionFaltaProfesoradoRepository;

    @Inject
    public JustificacionFaltaProfesoradoEventManager(JustificacionFaltaProfesoradoRepository justificacionFaltaProfesoradoRepository) {
        super(JustificacionFaltaProfesorado.class, JustificacionFaltaProfesorado.Estado.class, JustificacionFaltaProfesorado.Evento.class);
        this.justificacionFaltaProfesoradoRepository = justificacionFaltaProfesoradoRepository;
    }

    @Override
    public Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext eventContext) {
        JustificacionFaltaProfesorado justificacionFaltaProfesorado=new JustificacionFaltaProfesorado();

        justificacionFaltaProfesorado.setAnyo(Year.now().getValue());
        justificacionFaltaProfesorado.changeState(JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS);


        return justificacionFaltaProfesorado;
    }




    @OnEnterState
    public void onEnterEntradaDatos(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        System.out.println("onEnterEntradaDatos");
    }

    @OnEnterState
    public void onEnterRevision(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        System.out.println("onRevision");
    }

    @OnEnterState
    public void onEnterAceptado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        System.out.println("onAceptado");
    }

    @OnEnterState
    public void onEnterRechazado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        System.out.println("onRechazado");
    }

}
