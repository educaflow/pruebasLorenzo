package com.educaflow.common.pdf.impl;

import com.educaflow.common.pdf.DatosCertificado;
import com.educaflow.common.pdf.TipoEmisorCertificado;
import com.educaflow.common.pdf.impl.helper.CertificateParser;

import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

/**
 *
 * @author logongas
 */
public class DatosCertificadoImpl implements DatosCertificado {

    private static final String OID_NIF_FNMT = "1.3.6.1.4.1.5734.1.4";
    private static final String OID_NOMBRE_FNMT = "1.3.6.1.4.1.5734.1.1";
    private static final String OID_APE1_FNMT = "1.3.6.1.4.1.5734.1.2";
    private static final String OID_APE2_FNMT = "1.3.6.1.4.1.5734.1.3";
    private static final String OID_NIF_ACCV = "0.9.2342.19200300.100.1.1";
    private static final String OID_NOMBRE_APELLIDOS_ACCV = "2.5.4.3";
    private static final String OID_NOMBRE_APELLIDOS_ACCV_LOCATION = "2.5.29.17";

    private final X509Certificate certificate;
    private final TipoEmisorCertificado tipoEmisorCertificado;
    private String dni;
    private String nombre;
    private String apellidos;
    private String cnSubject;
    private String cnIssuer;
    private final boolean validoEnListaCertificadosConfiables;


    public DatosCertificadoImpl(X509Certificate certificate, KeyStore trustedKeyStore) {

        try {
            this.certificate = certificate;
            this.tipoEmisorCertificado = getTipoEmisorCertificado(this.certificate);
            populateDNINombreApellidosSegunTipoEmisorCertificado(this.tipoEmisorCertificado);

            validoEnListaCertificadosConfiables = isValidoEnListaCertificadosConfiables(certificate, trustedKeyStore);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }



    @Override
    public String getDNI() {
        return dni;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public String getApellidos() {
        return apellidos;
    }

    @Override
    public String getCnSubject() {
        return cnSubject;
    }

    @Override
    public String getCnIssuer() {
        return cnIssuer;
    }

    @Override
    public TipoEmisorCertificado getTipoEmisorCertificado() {
        return tipoEmisorCertificado;
    }

    @Override
    public boolean isValidoEnListaCertificadosConfiables() {
        return validoEnListaCertificadosConfiables;
    }



    private boolean isValidoEnListaCertificadosConfiables(X509Certificate certificate, KeyStore trustStore) {
        try {
            if (trustStore==null) {
                return false;
            }


            CertPathValidator validator = CertPathValidator.getInstance("PKIX");

            PKIXParameters params = new PKIXParameters(trustStore);
            params.setRevocationEnabled(false); // desactivar CRL/OCSP si no tienes

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            List<Certificate> certs = Collections.singletonList(certificate);
            CertPath certPath = cf.generateCertPath(certs);

            try {
                PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validator.validate(certPath, params);

                TrustAnchor trustAnchor = result.getTrustAnchor();
                X509Certificate ca = trustAnchor.getTrustedCert();

                return true;
            } catch (CertPathValidatorException ex) {
                return false;
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private TipoEmisorCertificado getTipoEmisorCertificado(X509Certificate x509Certificate) {
        TipoEmisorCertificado tipoEmisorCertificado;
        String strCertificado = x509Certificate.toString();
        if (strCertificado.contains("FNMT") && !strCertificado.contains("ACCV") && !strCertificado.contains("DNI")) {
            tipoEmisorCertificado = TipoEmisorCertificado.FNMT;
        } else if (!strCertificado.contains("FNMT") && strCertificado.contains("ACCV") && !strCertificado.contains("DNI")) {
            tipoEmisorCertificado = TipoEmisorCertificado.ACCV;
        } else if (!strCertificado.contains("FNMT") && !strCertificado.contains("ACCV") && strCertificado.contains("DNI")) {
            tipoEmisorCertificado = TipoEmisorCertificado.DNI;
        } else {
            tipoEmisorCertificado = null;
        }

        return tipoEmisorCertificado;
    }
    
    /*******************************************************************************************************/
    /********************** Extraer Información del certificado según el emissor (CA) **********************/
    /*******************************************************************************************************/     
    

    private void populateDNINombreApellidosSegunTipoEmisorCertificado(TipoEmisorCertificado tipoEmisorCertificado) {

        if (tipoEmisorCertificado != null) {
            switch (tipoEmisorCertificado) {
                case FNMT:
                    populateDNINombreApellidosFNMT();
                    break;
                case ACCV:
                    populateDNINombreApellidosACCV();
                case DNI:
                    populateDNINombreApellidosDNI();
                    break;
                default:
                    throw new RuntimeException("El tipo de emisor es desconocido:" + this.tipoEmisorCertificado);
            }
        } else {
            populateDNINombreApellidosEmpy();
        }

        cnSubject = getCnPrincipal(certificate.getSubjectX500Principal());
        cnIssuer = getCnPrincipal(certificate.getIssuerX500Principal());
    }

    private void populateDNINombreApellidosFNMT() {
        dni = getOnlyValueInMap(CertificateParser.findOidsWithLocation(certificate, OID_NIF_FNMT));
        nombre = getOnlyValueInMap(CertificateParser.findOidsWithLocation(certificate, OID_NOMBRE_FNMT));
        apellidos = (getOnlyValueInMap(CertificateParser.findOidsWithLocation(certificate, OID_APE1_FNMT)) + " " + getOnlyValueInMap(CertificateParser.findOidsWithLocation(certificate, OID_APE2_FNMT))).trim();
    }

    private void populateDNINombreApellidosACCV() {
        dni = getOnlyValueInMap(CertificateParser.findOidsWithLocation(certificate, OID_NIF_ACCV));

        String nombreApellidos = CertificateParser.findOidsWithLocation(certificate, OID_NOMBRE_APELLIDOS_ACCV).get(OID_NOMBRE_APELLIDOS_ACCV_LOCATION);
        String[] arrNombreApellidos = nombreApellidos.split("\\|");

        nombre = arrNombreApellidos[0];
        apellidos = String.join(" ", Arrays.copyOfRange(arrNombreApellidos, 1, arrNombreApellidos.length));
    }

    private void populateDNINombreApellidosDNI() {
        X500Principal x500Principal = certificate.getSubjectX500Principal();
        Map<String, String> data = getX500PrincipalData(x500Principal);

        dni = data.get("SERIALNUMBER");
        nombre = data.get("GIVENNAME");
        apellidos = data.get("CN").split(",")[0].trim();
    }



    private void populateDNINombreApellidosEmpy() {
        dni = "";
        nombre = "";
        apellidos = "";
    }

    
    
    /*************************************************************************************/
    /********************************** Datos Principal **********************************/
    /*************************************************************************************/
    
    private Map<String, String> getX500PrincipalData(X500Principal x500Principal) {

        try {

            LdapName ldapDN = new LdapName(x500Principal.getName());
            Map<String, String> campos = new HashMap<>();

            Map<String, String> oidMap = Map.of(
                    "2.5.4.42", "GIVENNAME",
                    "2.5.4.4", "SURNAME",
                    "2.5.4.5", "SERIALNUMBER"
            );

            for (Rdn rdn : ldapDN.getRdns()) {
                String type = rdn.getType();
                Object value = rdn.getValue();
                String strValue;

                // convertir byte[] a String si es necesario
                if (value instanceof byte[]) {
                    strValue = new String((byte[]) value).trim();
                } else if (value == null) {
                    strValue = "";
                } else {
                    strValue = value.toString().trim();
                }

                // usar nombre legible si existe en el mapa
                if (oidMap.containsKey(type)) {
                    campos.put(oidMap.get(type), strValue);
                } else {
                    campos.put(type, strValue);
                }
            }

            return campos;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getCnPrincipal(X500Principal certificate) {
        Map<String, String> datos=getX500PrincipalData(certificate);
        String cn=datos.get("CN");

        return cn;
    }
    
    /**************************************************************************************/
    /************************************* Utilidades *************************************/
    /**************************************************************************************/
    
    private String getOnlyValueInMap(Map<String, String> map) {
        if (map.size() != 1) {
            throw new RuntimeException("Existe más de un elemento en el Map:" + map.size() + " " + this.certificate.toString());
        }

        Map.Entry<String, String> entry = map.entrySet().iterator().next();
        return entry.getValue();
    }    
    
    
    @Override
    public String toString() {
        return this.getDNI() + ":" + this.getNombre() + "," + this.getApellidos() + "--Tipo:" + this.getTipoEmisorCertificado() + "(" + this.isValidoEnListaCertificadosConfiables() + ")";
    }
    
  


}
