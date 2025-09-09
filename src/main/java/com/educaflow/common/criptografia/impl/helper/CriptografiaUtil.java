package com.educaflow.common.criptografia.impl.helper;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

public class CriptografiaUtil {

    public static Provider getProviderPKCS11(Path path, int slot) {
        try {
            String pkcs11Config = String.format("name=DispositivoCriptograficoEducaFlow"+slot+"\nlibrary=%s\nslot=%d\n", path.toAbsolutePath().toString(), slot);
            File tempFileConf = File.createTempFile("DispositivoCriptograficoEducaFlow", ".cfg");
            tempFileConf.deleteOnExit(); // se borrará al salir del programa
            Files.writeString(tempFileConf.toPath(), pkcs11Config);

            Provider pkcs11Provider = Security.getProvider("SunPKCS11").configure(tempFileConf.getAbsolutePath());
            Security.addProvider(pkcs11Provider);

            return pkcs11Provider;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public static KeyStore getKeyStore(InputStream inputStreamKeyStore, String keyStorePassword,KeyStoreType type) {
        try {
            KeyStore keyStore = KeyStore.getInstance(type.name());
            keyStore.load(inputStreamKeyStore, keyStorePassword.toCharArray());

            return keyStore;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static KeyStore getKeyStore(Provider provider, String keyStorePassword, KeyStoreType type) {
        try {
            KeyStore keyStore = KeyStore.getInstance(type.name(),provider);
            keyStore.load(null, keyStorePassword.toCharArray());

            return keyStore;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public enum KeyStoreType {
        PKCS12,
        PKCS11
    }

}
