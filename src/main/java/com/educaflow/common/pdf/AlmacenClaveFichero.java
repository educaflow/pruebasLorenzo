package com.educaflow.common.pdf;

import java.nio.file.Path;

/**
 *
 * @author logongas
 */
public class AlmacenClaveFichero implements AlmacenClave {
    
    private final Path fileCertificate;
    private final String password;

    public AlmacenClaveFichero(Path fileCertificate, String password) {
        this.fileCertificate = fileCertificate;
        this.password = password;
    }

    /**
     * @return the fileCertificate
     */
    public Path getFileCertificate() {
        return fileCertificate;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    
    
    
}
