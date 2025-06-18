package com.educaflow.apps.expedientes.flujos.prueba;

public enum Estado {
    // Cada constante del enum tiene una descripción asociada
    ESTADO_INICIAL("Estado Inicial"),
    ESTADO2("Estado 2"),
    ESTADO3("Estado 3");

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
