package com.educaflow.common.pdf.impl;

import com.educaflow.common.pdf.AlmacenClave;
import com.educaflow.common.pdf.AlmacenClaveDispositivo;
import com.educaflow.common.pdf.AlmacenClaveFichero;
import com.educaflow.common.pdf.CampoFirma;
import com.educaflow.common.pdf.DatosCertificado;
import com.educaflow.common.pdf.DocumentoPdf;
import com.educaflow.common.pdf.DocumentoPdfFactory;
import com.educaflow.common.pdf.Rectangulo;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.forms.form.element.SignatureFieldAppearance;
import com.itextpdf.kernel.crypto.DigestAlgorithms;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.signatures.SignerProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class DocumentoPdfImplIText implements DocumentoPdf {

    private final byte[] bytesPdf;
    protected final PdfDocument pdfDocument;
    private final KeyStore keyStore;
    private final String fileName;

    public DocumentoPdfImplIText(byte[] bytesPdf,String fileName, KeyStore keyStore) {
        this.bytesPdf=bytesPdf;
        this.fileName=fileName;
        this.pdfDocument = getPdfDocument(bytesPdf);
        this.keyStore = keyStore;        
    }

    @Override
    public byte[] getDatos() {
        return bytesPdf;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }
    
    @Override
    public int getNumeroPaginas() {
        return this.pdfDocument.getNumberOfPages();
    }    


    @Override
    public List<String> getNombreCamposFormulario() {
        List<String> fields = new ArrayList<>();

        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocument, false);

        if (form != null) {
            Map<String, PdfFormField> PdfFormFields = form.getAllFormFields();

            for (Entry<String, PdfFormField> entry : PdfFormFields.entrySet()) {
                String name = entry.getKey();
                PdfFormField pdfFormField = entry.getValue();
                System.out.println("Campo:" + name + " tipo:" + pdfFormField.getFormType() + " valor:" + pdfFormField.getValueAsString());
                System.out.println(Arrays.toString(pdfFormField.getAppearanceStates()));
                if (isSignatureFormField(pdfFormField) == false) {
                    fields.add(name);
                }
            }
        }

        return fields;
    }
    
    @Override
    public Map<String, DatosCertificado> getFirmasPdf() {
        Map<String, DatosCertificado> firmas = new HashMap<>();

        SignatureUtil signatureUtil = new SignatureUtil(pdfDocument);

        List<String> signatureNames = signatureUtil.getSignatureNames();

        for (String signatureName : signatureNames) {
            PdfPKCS7 pkcs7 = signatureUtil.readSignatureData(signatureName);
            DatosCertificado datosCertificado = new DatosCertificadoImpl(pkcs7, keyStore);
            firmas.put(signatureName, datosCertificado);
        }

        return firmas;

    }    
    

    @Override
    public DocumentoPdf setValorCamposFormularioAndFlatten(Map<String,String> valores) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


            PdfDocument pdfDocumentNuevosValoresCampos = new PdfDocument(
                    new PdfReader(new ByteArrayInputStream(bytesPdf)),
                    new PdfWriter(byteArrayOutputStream)
            );        


            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDocumentNuevosValoresCampos, false);

            if (form == null) {
                throw new RuntimeException("No existe ningun formulario en el pdf");
            }

            for(Map.Entry<String, String> entry:valores.entrySet()) {
                String nombre=entry.getKey();
                String valor=entry.getValue();

                PdfFormField pdfFormField = form.getField(nombre);
                if (pdfFormField == null) {
                    throw new RuntimeException("No existe en el formulario el campo:" + pdfFormField);
                }

                pdfFormField.setValue(valor);            

            }
            
            form.flattenFields();
            
            pdfDocumentNuevosValoresCampos.close();
            byteArrayOutputStream.close();
            
            return DocumentoPdfFactory.getPdf(byteArrayOutputStream.toByteArray(),this.fileName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        

    }


       

    @Override
    public DocumentoPdf firmar(AlmacenClave almacenClave,CampoFirma campoFirma) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            
            Certificate[] chain=null;
            KeyStore ks=null;
            PrivateKey pk=null;
            if (almacenClave instanceof AlmacenClaveFichero) {
                AlmacenClaveFichero almacenClaveFichero=(AlmacenClaveFichero)almacenClave;
                Path fileCertificate=almacenClaveFichero.getFileCertificate();
                String password=almacenClaveFichero.getPassword();
                
                
                ks = KeyStore.getInstance("PKCS12");
                ks.load(new FileInputStream(fileCertificate.toFile()), password.toCharArray());
                String alias = ks.aliases().nextElement();
                pk = (PrivateKey) ks.getKey(alias, password.toCharArray());
                chain = ks.getCertificateChain(alias);
            } else if (almacenClave instanceof AlmacenClaveDispositivo) {
                AlmacenClaveDispositivo almacenClaveDispositivo=(AlmacenClaveDispositivo)almacenClave;
                
                String pin = almacenClaveDispositivo.getPin();
                Path libraryOpenscPkcs11= almacenClaveDispositivo.getLibraryOpenscPkcs11();
                int slot= almacenClaveDispositivo.getSlot();
                String alias= almacenClaveDispositivo.getAlias();


                String pkcs11Config = String.format("name=eDNI\nlibrary=%s\nslot=%d\n",libraryOpenscPkcs11.toAbsolutePath().toString(),slot);
                File tempFileConf = File.createTempFile("eDNI", ".cfg");
                tempFileConf.deleteOnExit(); // se borrará al salir del programa
                Files.writeString(tempFileConf.toPath(), pkcs11Config);                
                

                try (ByteArrayInputStream confStream = new ByteArrayInputStream(pkcs11Config.getBytes())) {
                    Provider pkcs11Provider = Security.getProvider("SunPKCS11").configure(tempFileConf.getAbsolutePath());
                    Security.addProvider(pkcs11Provider);

                    ks = KeyStore.getInstance("PKCS11", pkcs11Provider);
                    ks.load(null, pin.toCharArray());

                    //Muestra los alias es decir los certificados que hay en el dispositivo
                    //En el DNI hay: "CertAutenticacion" "CertFirmaDigital"
                    //Enumeration<String> aliasesDevice = ks.aliases();
                    //while(aliasesDevice.hasMoreElements()) {
                    //    alias = aliasesDevice.nextElement();
                    //    System.out.println("Alias---->"+alias);
                    //}                    
                    
                    // Buscar el alias si no está dado
                    if (alias == null || alias.isEmpty()) {
                        Enumeration<String> aliases = ks.aliases();
                        if (!aliases.hasMoreElements()) {
                            throw new RuntimeException("No se encontró ningún alias en el dispositivo");
                        }
                        alias = aliases.nextElement();
                    }

                    pk = (PrivateKey) ks.getKey(alias, pin.toCharArray());
                    chain = ks.getCertificateChain(alias);
                }  
                
                

                
            } else {
                throw new RuntimeException("Almacen desconocido:"+almacenClave.getClass().getName());
            }



            SignatureFieldAppearance appearance = new SignatureFieldAppearance(SignerProperties.IGNORED_ID);
            appearance.setContent(campoFirma.getMensaje());
            appearance.setFontSize(campoFirma.getFontSize());

            SignerProperties signerProperties = new SignerProperties();
            signerProperties.setFieldName(getSignatureFieldName());
            signerProperties.setPageRect(new Rectangle(campoFirma.getRectanguloMensaje().getX(), campoFirma.getRectanguloMensaje().getY(), campoFirma.getRectanguloMensaje().getWidth(), campoFirma.getRectanguloMensaje().getHeight()));
            signerProperties.setPageNumber(campoFirma.getNumerpoPagina());
            signerProperties.setSignatureAppearance(appearance);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            PdfReader pdfReader = new PdfReader(new ByteArrayInputStream(bytesPdf));

            PdfSigner signer = new PdfSigner(
                    pdfReader,
                    new PdfWriter(byteArrayOutputStream, new WriterProperties()),
                    new StampingProperties().useAppendMode()
            );
            signer.setSignerProperties(signerProperties);
            
            
            IExternalSignature pks;
            if (almacenClave instanceof AlmacenClaveFichero) {
                pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, "BC");
            } else if (almacenClave instanceof AlmacenClaveDispositivo) {
                pks = new PKCS11ExternalSignature(pk, DigestAlgorithms.SHA256, "RSA");
            } else {
                throw new RuntimeException("Almacen desconocido:"+almacenClave.getClass().getName());
            }

            IExternalDigest digest = new BouncyCastleDigest();
            signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

            
            byteArrayOutputStream.close();

            return DocumentoPdfFactory.getPdf(byteArrayOutputStream.toByteArray(),this.fileName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }



    @Override
    public DocumentoPdf anyadirDocumentoPdf(DocumentoPdf documentoPdf2) {
        return anyadirDocumentoPdf(documentoPdf2,this.fileName);
    }

    @Override
    public DocumentoPdf anyadirDocumentoPdf(DocumentoPdf documentoPdf2,String fileName) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfDocument pdfDestino = new PdfDocument(new PdfWriter(byteArrayOutputStream));
            PdfMerger merger = new PdfMerger(pdfDestino);
            merger.merge(this.pdfDocument, 1, this.pdfDocument.getNumberOfPages());
            merger.merge(getPdfDocument(documentoPdf2), 1, getPdfDocument(documentoPdf2).getNumberOfPages());

            merger.close();
            byteArrayOutputStream.close();

            return DocumentoPdfFactory.getPdf(byteArrayOutputStream.toByteArray(),fileName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    
    /**********************************************************************************/
    /*********************************** Utilidades ***********************************/
    /**********************************************************************************/
    
    
    
    private PdfDocument getPdfDocument(byte[] bytesPdf) {
        try {
            PdfReader reader = new PdfReader(new ByteArrayInputStream(bytesPdf));
            PdfDocument pdfDocumentNuevo = new PdfDocument(reader);

            return pdfDocumentNuevo;
        } catch (Exception e) {
            throw new RuntimeException("Error al leer el PDF", e);
        }
    }    
    
    private boolean isSignatureFormField(PdfFormField pdfFormField) {
        return pdfFormField instanceof PdfSignatureFormField;
    }
    
    
    private String getSignatureFieldName() {
        Map<String, DatosCertificado> firmas = this.getFirmasPdf();
        String signatureFieldName;

        for (int i = 1; i < 100; i++) {
            signatureFieldName = "Signature" + Integer.toString(i);

            if (firmas.containsKey(signatureFieldName) == false) {
                return signatureFieldName;
            }

        }

        throw new RuntimeException("No se encontró ningun campo donde firmar , están todos usados");
    }



    private PdfDocument getPdfDocument(DocumentoPdf documentoPdf) {
        try {
            Field campo = DocumentoPdfImplIText.class.getDeclaredField("pdfDocument");
            campo.setAccessible(true);
            return (PdfDocument) campo.get(documentoPdf);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
