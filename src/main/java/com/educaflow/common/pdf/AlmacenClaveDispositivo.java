/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.pdf;

import java.nio.file.Path;

/**
 *
 * @author logongas
 */
public class AlmacenClaveDispositivo implements AlmacenClave {
    
    private final String pin;
    private final Path libraryOpenscPkcs11;
    private final int slot;
    private final String alias;

    /**
     *
     * @param pin El pin del certificado
     * @param libraryOpenscPkcs11 "/usr/lib/x86_64-linux-gnu/opensc-pkcs11.so"
     * @param slot 0 para el primer dispositivo, 1 para el segundo...Se obtiene con el comando "pkcs11-tool --list-slots"
     * @param alias En el eDNI los valores son "CertAutenticacion" "CertFirmaDigital"
     */
    public AlmacenClaveDispositivo(String pin, Path libraryOpenscPkcs11, int slot, String alias) {
        this.pin = pin;
        this.libraryOpenscPkcs11 = libraryOpenscPkcs11;
        this.slot = slot;
        this.alias = alias;
    }

    /**
     * @return the pin
     */
    public String getPin() {
        return pin;
    }

    /**
     * @return the libraryOpenscPkcs11
     */
    public Path getLibraryOpenscPkcs11() {
        return libraryOpenscPkcs11;
    }

    /**
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }


    
    
    
    
}
