package com.educaflow.common.criptografia.config;

import java.io.InputStream;
import java.nio.file.Path;

public class AlmacenCertificadosConfiablesConfig {
    private final InputStream inputStream;
    private final String password;

    public AlmacenCertificadosConfiablesConfig(InputStream inputStream, String password) {
        this.inputStream = inputStream;
        this.password = password;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getPassword() {
        return password;
    }
}
