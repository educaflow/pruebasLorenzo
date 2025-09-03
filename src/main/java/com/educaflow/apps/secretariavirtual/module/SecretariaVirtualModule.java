package com.educaflow.apps.secretariavirtual.module;

import com.axelor.app.AppSettings;
import com.axelor.app.AvailableAppSettings;
import com.axelor.app.AxelorModule;
import com.educaflow.common.criptografia.EntornoCriptografico;
import com.educaflow.common.criptografia.config.AlmacenCertificadosConfiablesConfig;
import com.educaflow.common.criptografia.config.EntornoCriptograficoConfig;
import com.educaflow.common.criptografia.config.DispositivoCriptograficoConfig;
import com.educaflow.common.db.BulkTables;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SecretariaVirtualModule extends AxelorModule {

    protected void configure() {


        EntornoCriptograficoConfig entornoCriptograficoConfig = getEntornoCriptograficoConfigFromAppSettings();
        EntornoCriptografico.configure(entornoCriptograficoConfig);


        String dataBaseDriver = AppSettings.get().get(AvailableAppSettings.DB_DEFAULT_DRIVER);
        String dataBaseURL = AppSettings.get().get(AvailableAppSettings.DB_DEFAULT_URL);
        String dataBaseUser = AppSettings.get().get(AvailableAppSettings.DB_DEFAULT_USER);
        String dataBasePassword = AppSettings.get().get(AvailableAppSettings.DB_DEFAULT_PASSWORD);
        String schemaName = "public";

        Set<String> tablasExcluidas = Set.of("meta_file", "meta_sequence", "auth_user", "auth_group", "meta_filter");
        Set<String> tablasIncluidas = Set.of("expedientes_estado_tipo_expediente");

        BulkTables bulkTables = new BulkTables();
        bulkTables.truncateTables(dataBaseDriver,dataBaseURL,dataBaseUser,dataBasePassword,schemaName,tablasExcluidas,tablasIncluidas);
    }

    public EntornoCriptograficoConfig getEntornoCriptograficoConfigFromAppSettings()  {

        String pathAlmacen = AppSettings.get().get("entornoCriptografico.almacenCertificadosConfiables.path");
        String passwordAlmacen = AppSettings.get().get("entornoCriptografico.almacenCertificadosConfiables.password");
        InputStream inputStreamAlamacen=SecretariaVirtualModule.class.getClassLoader().getResourceAsStream(pathAlmacen) ;
        AlmacenCertificadosConfiablesConfig almacenConfig = new AlmacenCertificadosConfiablesConfig(inputStreamAlamacen, passwordAlmacen);

        List<DispositivoCriptograficoConfig> dispositivos = new ArrayList<>();
        int indice=0;
        while (AppSettings.get().get("entornoCriptografico.dispositivoCriptografico." + indice + ".pkcs11LibraryPath") != null) {
            Path pkcs11LibraryPath = Path.of(AppSettings.get().get("entornoCriptografico.dispositivoCriptografico." + indice + ".pkcs11LibraryPath"));
            int slot = Integer.parseInt(AppSettings.get().get("entornoCriptografico.dispositivoCriptografico." + indice + ".slot"));
            String pin = AppSettings.get().get("entornoCriptografico.dispositivoCriptografico." + indice + ".pin");
            dispositivos.add(new DispositivoCriptograficoConfig(pkcs11LibraryPath, slot,pin));
            indice++;
        }

        return new EntornoCriptograficoConfig(almacenConfig, dispositivos);
    }


}
