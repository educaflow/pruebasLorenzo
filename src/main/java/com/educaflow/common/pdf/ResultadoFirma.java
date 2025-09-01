package com.educaflow.common.pdf;

import java.time.LocalDateTime;

public interface ResultadoFirma {

    boolean isCorrecta();
    LocalDateTime getFechaFirma();
    DatosCertificado getDatosCertificado();

}
