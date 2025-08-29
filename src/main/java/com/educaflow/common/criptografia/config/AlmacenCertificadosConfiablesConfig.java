package com.educaflow.common.criptografia.config;

import java.nio.file.Path;

public class AlmacenCertificadosConfiablesConfig {
    private final Path path;
    private final String password;

    public AlmacenCertificadosConfiablesConfig(Path path, String password) {
        this.path = path;
        this.password = password;
    }

    public Path getPath() {
        return path;
    }

    public String getPassword() {
        return password;
    }
}
