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
    String getCnSubject();
    String getCnIssuer();
    TipoEmisorCertificado getTipoEmisorCertificado();


    /**
     * Valida contra https://sedediatid.digital.gob.es/Prestadores/Paginas/Inicio.aspx
     * @return
     */
    boolean isValidoEnListaCertificadosConfiables();
    
}
