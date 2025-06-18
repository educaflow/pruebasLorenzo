package com.educaflow.apps.expedientes.flujos.faltajustificada;

public enum Estado {
    // Cada constante del enum tiene una descripción asociada
    DATOS_INICIALES("Datos iniciales del expediente"),
    PENDIENTE_PRESENTACION("Esperando la presentación"),
    PENDIENTE_APROBACION("Esperando aprobación por parte del supervisor"),
    PENDIENTE_JUSTIFICANTE("Pendiente de recibir justificante del interesado"),
    PENDIENTE_CERRAR("Listo para ser cerrado"),
    CERRADO("Expediente cerrado");

    // Campo privado para almacenar la descripción de cada estado
    private final String description;

    // Constructor del enum: se ejecuta una vez por cada constante al cargar la clase
    Estado(String description) {
        this.description = description;
    }



    @Override
    public String toString() {
        return description;
    }
}
