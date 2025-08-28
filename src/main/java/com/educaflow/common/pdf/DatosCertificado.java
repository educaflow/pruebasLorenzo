/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.pdf;

/**
 *
 * @author logongas
 */
public interface DatosCertificado {
    
    String getDNI();
    String getNombre();
    String getApellidos();
    EmisorCertificado getEmisorCertificado();
    boolean isValid();
    
}
