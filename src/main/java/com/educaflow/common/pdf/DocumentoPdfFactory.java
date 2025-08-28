package com.educaflow.common.pdf;


import com.educaflow.common.pdf.impl.DocumentoPdfImplIText;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;


public class DocumentoPdfFactory {

    private static final String keyStorePassword="s3cr3T";
    private static final KeyStore keyStore;
    
    static {
        try {
            
            try (FileInputStream inputStreamKeyStore = new FileInputStream("/home/logongas/Documentos/desarrollo/educaflow/pruebaiText/truststore.jks")) {
                keyStore = getKeyStore(inputStreamKeyStore,keyStorePassword);
            } 
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static DocumentoPdf getPdf(byte[] bytesPdf,String fileName) {


        return new DocumentoPdfImplIText(bytesPdf,fileName,keyStore);

    }

    private static KeyStore getKeyStore(InputStream inputStreamKeyStore,String keyStorePassword) {
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(inputStreamKeyStore, keyStorePassword.toCharArray());
            
            return trustStore;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }    
    
}