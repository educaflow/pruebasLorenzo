package com.educaflow.common.criptografia;

import com.educaflow.common.criptografia.config.AlmacenCertificadosConfiablesConfig;
import com.educaflow.common.criptografia.config.ConfiguracionCriptografica;
import com.educaflow.common.criptografia.config.DispositivoCriptograficoConfig;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EntornoCriptografico {
    private static AlmacenCertificadosConfiables almacenCertificadosConfiables;
    private static Map<Integer, DispositivoCriptografico> dispositivosCriptograficos;



    public static void configure(ConfiguracionCriptografica configuracionCriptografica) {
        AlmacenCertificadosConfiablesConfig almacenCertificadosConfiablesConfig=configuracionCriptografica.getAlmacenCertificadosConfiablesConfig();
        List<DispositivoCriptograficoConfig> dispositivoCritograficoConfigs=configuracionCriptografica.getDispositivoCritograficoConfigs();

        Security.addProvider(new BouncyCastleProvider());
        almacenCertificadosConfiables=getAlmacenCertificadosConfiables(almacenCertificadosConfiablesConfig);
        dispositivosCriptograficos=getDispositivosCriptograficos(dispositivoCritograficoConfigs);
    }


    public static AlmacenCertificadosConfiables getAlmacenCertificadosConfiables() {
        return almacenCertificadosConfiables;
    }

    public static DispositivoCriptografico getDispositivoCriptografico(int slot) {
        if (!dispositivosCriptograficos.containsKey(slot)) {
            throw new RuntimeException("No se ha configurado ningún proveedor PKCS#11 para el slot: " + slot);
        }

        return dispositivosCriptograficos.get(slot);
    }

    /*********************************************************************************/
    /***************************** Creación de los datos *****************************/
    /*********************************************************************************/

    private static AlmacenCertificadosConfiables getAlmacenCertificadosConfiables(AlmacenCertificadosConfiablesConfig almacenCertificadosConfiablesConfig) {
        try {
            Path pathAlmacenCertificadosConfiables=almacenCertificadosConfiablesConfig.getPath();
            String passwordAlmacenCertificadosConfiables=almacenCertificadosConfiablesConfig.getPassword();

            AlmacenCertificadosConfiables almacenCertificadosConfiables;
            try (InputStream inputStreamKeyStore = EntornoCriptografico.class.getClassLoader().getResourceAsStream(pathAlmacenCertificadosConfiables.toString())) {
                KeyStore trustedKeyStore = getKeyStore(inputStreamKeyStore,passwordAlmacenCertificadosConfiables);
                almacenCertificadosConfiables = new AlmacenCertificadosConfiables(trustedKeyStore);
            }

            return almacenCertificadosConfiables;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    private static Map<Integer, DispositivoCriptografico> getDispositivosCriptograficos(List<DispositivoCriptograficoConfig> dispositivoCritograficoConfigs) {
        Map<Integer, DispositivoCriptografico> dispositivosCriptograficos = new HashMap<>();

        for(DispositivoCriptograficoConfig dispositivoCriptograficoConfig : dispositivoCritograficoConfigs) {
            int slot = dispositivoCriptograficoConfig.getSlot();

            if (dispositivosCriptograficos.containsKey(slot)) {
                throw new RuntimeException("Yas existe un PKCS#11 para el slot: " + slot);
            }

            DispositivoCriptografico dispositivoCriptografico=getDispositivoCriptografico(dispositivoCriptograficoConfig);
            dispositivosCriptograficos.put(slot,dispositivoCriptografico);
        }

        return dispositivosCriptograficos;
    }

    private static DispositivoCriptografico getDispositivoCriptografico(DispositivoCriptograficoConfig dispositivoCriptograficoConfig) {
        Path pkcs11LibraryPath = dispositivoCriptograficoConfig.getPkcs11LibraryPath();
        int slot = dispositivoCriptograficoConfig.getSlot();
        String pin = dispositivoCriptograficoConfig.getPin();

        if (Files.exists(pkcs11LibraryPath)==false) {
            throw new RuntimeException("La librería PKCS#11 no existe" + pkcs11LibraryPath);
        }
        if (Files.isRegularFile(pkcs11LibraryPath)==false) {
            throw new RuntimeException("La librería PKCS#11 no es un archivo regular" + pkcs11LibraryPath);
        }

        Provider providerPkcs11 = getProviderPKCS11(pkcs11LibraryPath, slot);
        KeyStore devicePkcs11KeyStore=getKeyStoreDevicePkc11(providerPkcs11, pin);
        DispositivoCriptografico dispositivoCriptografico=new DispositivoCriptografico(devicePkcs11KeyStore, pin.toCharArray());

        return dispositivoCriptografico;

    }



    private static KeyStore getKeyStoreDevicePkc11(Provider providerPKCS11, String pin) {
        try {
            KeyStore pkcs11KeyStore = KeyStore.getInstance("PKCS11", providerPKCS11);
            pkcs11KeyStore.load(null, pin.toCharArray());

            return pkcs11KeyStore;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Provider getProviderPKCS11(Path path, int slot) {
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

    private static KeyStore getKeyStore(InputStream inputStreamKeyStore, String keyStorePassword) {
        try {
            KeyStore trustedKeyStore = KeyStore.getInstance("JKS");
            trustedKeyStore.load(inputStreamKeyStore, keyStorePassword.toCharArray());

            return trustedKeyStore;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}

