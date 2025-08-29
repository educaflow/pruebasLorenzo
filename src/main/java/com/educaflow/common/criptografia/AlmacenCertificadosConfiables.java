package com.educaflow.common.criptografia;

import java.security.KeyStore;

public class AlmacenCertificadosConfiables {

    private KeyStore trustedKeyStore;

    public AlmacenCertificadosConfiables(KeyStore trustedKeyStore) {
        this.trustedKeyStore = trustedKeyStore;
    }

    public KeyStore getTrustedKeyStore() {
        return trustedKeyStore;
    }

}
