package com.educaflow.apps.expedientes.module;

import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.inject.Beans;
import com.educaflow.apps.expedientes.common.EventManager;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.repo.TipoExpedienteRepository;

public class StartUp {

    public void startUpEventListener(@Observes StartupEvent startupEvent) {
        TipoExpedienteRepository tipoExpedienteRepository = Beans.get(TipoExpedienteRepository.class);

        tipoExpedienteRepository.all().fetch().forEach(
                tipoExpediente -> {
                    EventManager eventManager = getEventManager(tipoExpediente);
                    eventManager.validateExpediente();
                }
        );
        System.out.println("Lógica de inicialización post-arranque completada.");
    }


    private static EventManager getEventManager(TipoExpediente tipoExpediente) {
        try {
            if (tipoExpediente == null) {
                throw new RuntimeException("No existe el tipo del expediente a crear.");
            }
            String fqcnEventManager = tipoExpediente.getFqcnEventManager();
            if (fqcnEventManager == null || fqcnEventManager.isEmpty()) {
                throw new RuntimeException("No existe el fqcnEventManager para el tipo de expediente: " + tipoExpediente.getName());
            }
            Class<EventManager> eventManagerClass = (Class<EventManager>) Class.forName(tipoExpediente.getFqcnEventManager());

            EventManager eventManager = Beans.get(eventManagerClass);

            return eventManager;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}
