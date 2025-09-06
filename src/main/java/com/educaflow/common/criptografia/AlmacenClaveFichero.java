package com.educaflow.common.criptografia;

import java.io.InputStream;

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
