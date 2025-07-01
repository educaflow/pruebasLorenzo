package com.educaflow.apps.expedientes.eventmanagers;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.EventManager;
import com.educaflow.apps.expedientes.common.Profile;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.repo.JustificacionFaltaProfesoradoRepository;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;


public class JustificacionFaltaProfesoradoEventManager extends EventManager<JustificacionFaltaProfesorado, JustificacionFaltaProfesorado.Estado, JustificacionFaltaProfesorado.Evento> {

    private final JustificacionFaltaProfesoradoRepository repository;

    @Inject
    public JustificacionFaltaProfesoradoEventManager(JustificacionFaltaProfesoradoRepository repository) {
        super(JustificacionFaltaProfesorado.class, JustificacionFaltaProfesorado.Estado.class, JustificacionFaltaProfesorado.Evento.class);
        this.repository = repository;
    }

    @Override
    public Expediente triggerInitialEvent(TipoExpediente tipoExpediente, EventContext eventContext) {
        JustificacionFaltaProfesorado justificacionFaltaProfesorado = new JustificacionFaltaProfesorado();
        justificacionFaltaProfesorado.setTipoExpediente(tipoExpediente);
        justificacionFaltaProfesorado.changeState(JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS);
        justificacionFaltaProfesorado.setAnyo(LocalDate.now().getYear());
        justificacionFaltaProfesorado.setNombre("Lorenzo");
        justificacionFaltaProfesorado.setApellidos("García García");
        justificacionFaltaProfesorado.setDni("12345678Z");

        return justificacionFaltaProfesorado;
    }

    @WhenEvent
    public void triggerPresentar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        appendDocuments(justificacionFaltaProfesorado);
        justificacionFaltaProfesorado.changeState(JustificacionFaltaProfesorado.Estado.FIRMA_POR_USUARIO);
    }

    @WhenEvent
    public void triggerPresentarDocumentosFirmados(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.changeState(JustificacionFaltaProfesorado.Estado.REVISION_Y_FIRMA_POR_RESPONSABLE);
    }

    @WhenEvent
    public void triggerBorrar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {

    }

    @WhenEvent
    public void triggerSubsanar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.changeState(JustificacionFaltaProfesorado.Estado.ENTRADA_DATOS);
    }

    @WhenEvent
    public void triggerAceptar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.changeState(JustificacionFaltaProfesorado.Estado.ACEPTADO);
    }

    @WhenEvent
    public void triggerRechazar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) {
        justificacionFaltaProfesorado.changeState(JustificacionFaltaProfesorado.Estado.RECHAZADO);
    }

    @OnEnterState
    public void onEnterEntradaDatos(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }


    @OnEnterState
    public void onEnterFirmaPorUsuario(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(Profile.CREADOR);
        justificacionFaltaProfesorado.setAbierto(true);
    }

    @OnEnterState
    public void onEnterRevisionYFirmaPorResponsable(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
        justificacionFaltaProfesorado.setCurrentActionProfiles(Profile.RESPONSABLE);
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
        String fileName = "documentoCompleto.pdf";
        String resourcePath = "pdf-templates/" + fileName;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            justificacionFaltaProfesorado.setDocumentoCompletoSinFirmar(Beans.get(com.axelor.meta.MetaFiles.class).upload(inputStream, fileName));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }


            justificacionFaltaProfesorado.setDocumentoCompletoFirmadoUsuario(Beans.get(com.axelor.meta.MetaFiles.class).upload(inputStream, fileName));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            justificacionFaltaProfesorado.setDocumentoCompletoFirmadoUsuarioDirector(Beans.get(com.axelor.meta.MetaFiles.class).upload(inputStream, fileName));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}