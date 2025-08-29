package com.educaflow.apps.expedientes.tiposexpedientes.justificacion_falta_profesorado;

import com.axelor.meta.db.MetaFile;
import com.educaflow.apps.expedientes.common.EventContext;
import com.educaflow.apps.expedientes.common.annotations.OnEnterState;
import com.educaflow.apps.expedientes.common.annotations.WhenEvent;
import com.educaflow.apps.expedientes.db.JustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.TipoResolucionJustificacionFaltaProfesorado;
import com.educaflow.apps.expedientes.db.repo.JustificacionFaltaProfesoradoRepository;
import com.educaflow.apps.expedientes.tiposexpedientes.shared.TipoExpedienteUtil;
import com.educaflow.common.pdf.*;

import com.educaflow.common.validation.messages.BusinessException;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.time.LocalDate;


public class EventManagerImpl extends com.educaflow.apps.expedientes.common.EventManager<JustificacionFaltaProfesorado, JustificacionFaltaProfesorado.State, JustificacionFaltaProfesorado.Event,JustificacionFaltaProfesorado.Profile> {

    private final JustificacionFaltaProfesoradoRepository repository;

    @Inject
    public EventManagerImpl(JustificacionFaltaProfesoradoRepository repository) {
        super(JustificacionFaltaProfesorado.class, JustificacionFaltaProfesorado.State.class, JustificacionFaltaProfesorado.Event.class,JustificacionFaltaProfesorado.Profile.class);
        this.repository = repository;
    }

    @Override
    public void triggerInitialEvent(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) throws BusinessException {


        justificacionFaltaProfesorado.setAnyo(LocalDate.now().getYear());
        justificacionFaltaProfesorado.setNombre("Lorenzo");
        justificacionFaltaProfesorado.setApellidos("Acción García");
        justificacionFaltaProfesorado.setDni("12345678Z");

    }

    @WhenEvent
    public void triggerPresentar(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) throws BusinessException {
        DocumentoPdf documentoPdf = justificacionFaltaProfesorado.getDocumentoPdf(JustificacionFaltaProfesorado.TipoDocumentoPdf.SOLICITUD);
        DocumentoPdf documentoPdfRegistroEntrada = justificacionFaltaProfesorado.getDocumentoPdf(JustificacionFaltaProfesorado.TipoDocumentoPdf.REGISTRO);
        DocumentoPdf solicitudPdf=documentoPdfRegistroEntrada.anyadirDocumentoPdf(documentoPdf,"solicitud.pdf");
        DocumentoPdf justificantePdf=TipoExpedienteUtil.getDocumentoPdfFromMetaFile(justificacionFaltaProfesorado.getJustificante());
        solicitudPdf=solicitudPdf.anyadirDocumentoPdf(justificantePdf);

        AlmacenClave almacenClave=new AlmacenClaveFichero(Path.of("mi_certificado.p12"),"nadanada");
        //AlmacenClave almacenClave=new AlmacenClaveDispositivo(0,"CertFirmaDigital");
        CampoFirma campoFirma=new CampoFirma(new Rectangulo(100,450,200,150),1);
        solicitudPdf=solicitudPdf.firmar(almacenClave,campoFirma);

        MetaFile metaFile= TipoExpedienteUtil.getMetaFileFromDocumentoPdf(solicitudPdf);
        justificacionFaltaProfesorado.setDocumentoCompletoSinFirmar(metaFile);


        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.FIRMA_POR_USUARIO);
    }
    @WhenEvent
    public void triggerPresentarDocumentosFirmados(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) throws BusinessException {
        justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.REVISION_Y_FIRMA_POR_RESPONSABLE);
        justificacionFaltaProfesorado.setDisconformidad(null);
        justificacionFaltaProfesorado.setResolucion(null);
    }

    @WhenEvent
    public void triggerResolver(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext)  throws BusinessException {
        TipoResolucionJustificacionFaltaProfesorado tipoResolucion = justificacionFaltaProfesorado.getTipoResolucion();

        switch (tipoResolucion) {
            case ACEPTAR:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.ACEPTADO);
                break;
            case RECHAZAR:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.RECHAZADO);
                break;
            case SUBSANAR_DATOS:
                justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.State.ENTRADA_DATOS);
                break;
            default:
                throw new IllegalArgumentException("Tipo de resolución no reconocido: " + tipoResolucion);
        }
    }


    @WhenEvent
    public void triggerBack(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext)  throws BusinessException {
            JustificacionFaltaProfesorado.State state=JustificacionFaltaProfesorado.State.valueOf(justificacionFaltaProfesorado.getCodeState());

            switch (state) {
                case ENTRADA_DATOS:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.ENTRADA_DATOS);
                    break;
                case FIRMA_POR_USUARIO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.ENTRADA_DATOS);
                    break;
                case REVISION_Y_FIRMA_POR_RESPONSABLE:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.ENTRADA_DATOS);
                    break;
                case ACEPTADO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.REVISION_Y_FIRMA_POR_RESPONSABLE);
                    break;
                case RECHAZADO:
                    justificacionFaltaProfesorado.updateState( JustificacionFaltaProfesorado.State.REVISION_Y_FIRMA_POR_RESPONSABLE);
                    break;
                default:
                    throw new IllegalArgumentException("State no reconocido: " + state);
            }

    }


    @WhenEvent
    public void triggerDelete(JustificacionFaltaProfesorado justificacionFaltaProfesorado, JustificacionFaltaProfesorado original, EventContext eventContext) throws BusinessException {
        //justificacionFaltaProfesorado.updateState(JustificacionFaltaProfesorado.Estado.);
    }


    @OnEnterState
    public void onEnterEntradaDatos(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }


    @OnEnterState
    public void onEnterFirmaPorUsuario(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }

    @OnEnterState
    public void onEnterRevisionYFirmaPorResponsable(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }


    @OnEnterState
    public void onEnterAceptado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }

    @OnEnterState
    public void onEnterRechazado(JustificacionFaltaProfesorado justificacionFaltaProfesorado, EventContext eventContext) {
    }



}