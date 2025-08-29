package com.educaflow.common.pdf;


import com.educaflow.common.criptografia.config.ConfiguracionCriptografica;
import com.educaflow.common.pdf.impl.DocumentoPdfImplIText;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;


public class DocumentoPdfFactory {



    public static DocumentoPdf getPdf(byte[] bytesPdf,String fileName) {


        return new DocumentoPdfImplIText(bytesPdf,fileName);

    }


    
}