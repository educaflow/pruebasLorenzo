package com.educaflow.apps.expedientes.tipo.justificacion_falta_profesorado;

import com.axelor.inject.Beans;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.TipoResolucionJustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.repo.JustificacionFaltaProfesoradoRepository;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;


public class EventManager extends com.educaflow.apps.expedientes.common.EventManager<JustificacionFaltaProfesorado, JustificacionFaltaProfesorado.Estado, JustificacionFaltaProfesorado.Evento,JustificacionFaltaProfesorado.Profile> {

    private final JustificacionFaltaProfesoradoRepository repository;

    @Inject
    public EventManager(JustificacionFaltaProfesoradoRepository repository) {
        super(JustificacionFaltaProfesorado.class, JustificacionFaltaProfesorado.Estado.class, JustificacionFaltaProfesorado.Evento.class,JustificacionFaltaProfesorado.Profile.class);
        this.repository = repository;
    }

    @Override
    public Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext eventContext) {
        //PruebaKotlin pruebaKotlin = new PruebaKotlin();

        JustificacionFaltaProfesorado justificacionFaltaProfesorado = new JustificacionFaltaProfesorado();
        justificacionFaltaProfesorado.setTipoExpediente(tipoExpediente);
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS);
        justificacionFaltaProfesorado.setAnyo(LocalDate.now().getYear());
        justificacionFaltaProfesorado.setNombre("Lorenzo");
        justificacionFaltaProfesorado.setApellidos("García García");
        justificacionFaltaProfesorado.setDni("12345678A");
        //justificacionFaltaProfesorado.setPrueba("Está vivo!!!!!!!");

        return justificacionFaltaProfesorado;
    }

    @WhenEvent
    public void triggerPresentar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        appendDocuments(justificacionFaltaProfesorado);
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.FIRMA_POR_USUARIO);
    }
    @WhenEvent
    public void triggerPresentarDocumentosFirmados(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.REVISION_Y_FIRMA_POR_RESPONSABLE);
        justificacionFaltaProfesorado.setTipoResolucion(TipoResolucionJustificacionFaltaProfesorado.ACEPTAR);
        justificacionFaltaProfesorado.setDisconformidad(null);
        justificacionFaltaProfesorado.setResolucion(null);
    }

    @WhenEvent
    public void triggerResolver(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        TipoResolucionJustificacionFaltaProfesorado tipoResolucion = justificacionFaltaProfesorado.getTipoResolucion();

        switch (tipoResolucion) {
            case ACEPTAR:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.ACEPTADO);
                break;
            case RECHAZAR:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.RECHAZADO);
                break;
            case SUBSANAR_DATOS:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS);
                break;
            default:
                throw new IllegalArgumentException("Tipo de resolución no reconocido: " + tipoResolucion);
        }
    }



    @WhenEvent
    public void triggerDelete(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {

    }

    @WhenEvent
    public void triggerBack(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
            JustificacionFaltaProfesorado.Estado estado=JustificacionFaltaProfesorado.Estado.valueOf(justificacionFaltaProfesorado.getCodeState());

            switch (estado) {
                case ENTRADA_DATOS:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS);
                    break;
                case FIRMA_POR_USUARIO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS);
                    break;
                case REVISION_Y_FIRMA_POR_RESPONSABLE:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS);
                    break;
                case ACEPTADO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.Estado.REVISION_Y_FIRMA_POR_RESPONSABLE);
                    break;
                case RECHAZADO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.Estado.REVISION_Y_FIRMA_POR_RESPONSABLE);
                    break;
                default:
                    throw new IllegalArgumentException("Estado no reconocido: " + estado);
            }

    }

    @WhenEvent
    public void triggerExit(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {

    }



    @OnEnterState
    public void onEnterEntradaDatos(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(JustificacionFaltaProfesorado.Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }


    @OnEnterState
    public void onEnterFirmaPorUsuario(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(JustificacionFaltaProfesorado.Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }

    @OnEnterState
    public void onEnterRevisionYFirmaPorResponsable(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(JustificacionFaltaProfesorado.Profile.RESPONSABLE);
        justificacionFaltaProfesorado.setAbierto(true);
    }


    @OnEnterState
    public void onEnterAceptado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setAbierto(false);
    }

    @OnEnterState
    public void onEnterRechazado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setAbierto(false);
    }



    public void appendDocuments(JustificacionFaltaProfesorado justificacionFaltaProfesorado) {
        String fileName = "justificacion_falta_profesor.pdf";
        String resourcePath = "pdf-templates/" + fileName;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            justificacionFaltaProfesorado.setDocumentoCompletoSinFirmar(Beans.get(com.axelor.meta.MetaFiles.class).upload(inputStream, fileName));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}