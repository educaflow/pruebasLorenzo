package com.educaflow.common.pdf.impl;

import com.educaflow.common.pdf.DatosCertificado;
import com.educaflow.common.pdf.EmisorCertificado;
import static com.educaflow.common.pdf.EmisorCertificado.ACCV;
import static com.educaflow.common.pdf.EmisorCertificado.DNI;
import static com.educaflow.common.pdf.EmisorCertificado.FNMT;
import com.itextpdf.signatures.PdfPKCS7;
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
    private final EmisorCertificado emisorCertificado;
    private String dni;
    private String nombre;
    private String apellidos;
    private final boolean valid;

    public DatosCertificadoImpl(PdfPKCS7 pdfPKCS7, KeyStore keyStore) {

        try {
            this.certificate = pdfPKCS7.getSigningCertificate();
            this.emisorCertificado = getEmisorCertificado(this.certificate);
            populateDNINombreApellidosSegunEmisorCertificado(this.emisorCertificado);

            valid = pdfPKCS7.verifySignatureIntegrityAndAuthenticity() && isValidoCertificadoSegunListaAutorizadesCertificacion(certificate, keyStore);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public DatosCertificadoImpl(X509Certificate certificate, KeyStore keyStore) {

        try {
            this.certificate = certificate;
            this.emisorCertificado = getEmisorCertificado(this.certificate);
            populateDNINombreApellidosSegunEmisorCertificado(this.emisorCertificado);

            valid = isValidoCertificadoSegunListaAutorizadesCertificacion(certificate, keyStore);
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
    public EmisorCertificado getEmisorCertificado() {
        return emisorCertificado;
    }

    @Override
    public boolean isValid() {
        return valid;
    }
    


    private boolean isValidoCertificadoSegunListaAutorizadesCertificacion(X509Certificate certificate, KeyStore trustStore) {
        try {

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

    private EmisorCertificado getEmisorCertificado(X509Certificate x509Certificate) {
        EmisorCertificado emisorCertificado;
        String strCertificado = x509Certificate.toString();
        if (strCertificado.contains("FNMT") && !strCertificado.contains("ACCV") && !strCertificado.contains("DNI")) {
            emisorCertificado = EmisorCertificado.FNMT;
        } else if (!strCertificado.contains("FNMT") && strCertificado.contains("ACCV") && !strCertificado.contains("DNI")) {
            emisorCertificado = EmisorCertificado.ACCV;
        } else if (!strCertificado.contains("FNMT") && !strCertificado.contains("ACCV") && strCertificado.contains("DNI")) {
            emisorCertificado = EmisorCertificado.DNI;
        } else {
            emisorCertificado = null;
        }

        return emisorCertificado;
    }
    
    /*******************************************************************************************************/
    /********************** Extraer Información del certificado según el emissor (CA) **********************/
    /*******************************************************************************************************/     
    

    private void populateDNINombreApellidosSegunEmisorCertificado(EmisorCertificado emisorCertificado) {

        if (emisorCertificado != null) {
            switch (emisorCertificado) {
                case FNMT:
                    populateDNINombreApellidosFNMT();
                    break;
                case ACCV:
                    populateDNINombreApellidosACCV();
                case DNI:
                    populateDNINombreApellidosDNI();
                    break;
                default:
                    throw new RuntimeException("El tipo de emisor es desconocido:" + this.emisorCertificado);
            }
        } else {
            populateDNINombreApellidosEmpy();
        }
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
        X500Principal x500Principal = certificate.getSubjectX500Principal();
        Map<String, String> data = getX500PrincipalData(x500Principal);

        dni = "";
        nombre = data.get("CN");
        apellidos = "";
    }

    
    
    /**************************************************************************************/
    /************************************* Parsear CN *************************************/
    /**************************************************************************************/    
    
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
        return this.getDNI() + ":" + this.getNombre() + "," + this.getApellidos() + "--Tipo:" + this.getEmisorCertificado() + "(" + this.isValid() + ")";
    }
    
  

}
