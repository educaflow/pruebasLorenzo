https://manpages.ubuntu.com/manpages/focal/man5/opensc.conf.5.html
Es necesario configurar el fichero:

/etc/opensc/opensc.conf


con


app default {
	disable_popups = true
}


los dipositivos se ven con:

opensc-tool -l
El numero es el slot.


Hay que ver el alias a usar para firmar con el código:

//Muestra los alias es decir los certificados que hay en el dispositivo
//En el DNI hay: "CertAutenticacion" "CertFirmaDigital"
Enumeration<String> aliasesDevice = ks.aliases();
while(aliasesDevice.hasMoreElements()) {
    alias = aliasesDevice.nextElement();
    System.out.println("Alias---->"+alias);
} 


Y los algoritmos se ven con:

//Muestra la lista de algoritmos
Provider p = Security.getProviders("Signature.NONEwithRSA")[0];
for (Provider.Service s : p.getServices()) {
    if (s.getType().equals("Signature")) {
        System.out.println("Algoritmo soportado: " + s.getAlgorithm());
    }
}