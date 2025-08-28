package com.educaflow.common.pdf.impl;



import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.ISignatureMechanismParams;
import java.security.*;

public class PKCS11ExternalSignature implements IExternalSignature {

    private final PrivateKey privateKey;
    private final String digestAlgorithm;
    private final String encryptionAlgorithm;

    public PKCS11ExternalSignature(PrivateKey privateKey,
                                   String digestAlgorithm,
                                   String encryptionAlgorithm) {
        this.privateKey = privateKey;
        this.digestAlgorithm = digestAlgorithm;
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    @Override
    public byte[] sign(byte[] message) throws GeneralSecurityException {
        
        //Muestra la lista de algoritmos
        //Provider p = Security.getProviders("Signature.NONEwithRSA")[0];
        //for (Provider.Service s : p.getServices()) {
        //    if (s.getType().equals("Signature")) {
        //        System.out.println("Algoritmo soportado: " + s.getAlgorithm());
        //    }
        //}
        
        String algoritmo = digestAlgorithm.replace("-", "") + "WITH" + encryptionAlgorithm;
        Signature signature = Signature.getInstance(algoritmo);
        signature.initSign(privateKey);
        signature.update(message);
        return signature.sign();
    }

    @Override
    public String getDigestAlgorithmName() {
        return digestAlgorithm;
    }

    @Override
    public String getSignatureAlgorithmName() {
        return encryptionAlgorithm;
    }

    @Override
    public ISignatureMechanismParams getSignatureMechanismParameters() {
        return null;
    }
}

