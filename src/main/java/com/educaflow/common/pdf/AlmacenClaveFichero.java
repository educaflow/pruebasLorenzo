package com.educaflow.common.pdf;

import java.io.InputStream;
import java.nio.file.Path;

/**
 *
 * @author logongas
 */
public class AlmacenClaveFichero implements AlmacenClave {
    
    private final InputStream fileCertificate;
    private final String password;

    public AlmacenClaveFichero(InputStream fileCertificate, String password) {
        this.fileCertificate = fileCertificate;
        this.password = password;
    }

    /**
     * @return the fileCertificate
     */
    public InputStream getFileCertificate() {
        return fileCertificate;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    
    
    
}
