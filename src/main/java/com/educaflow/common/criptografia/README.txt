https://manpages.ubuntu.com/manpages/focal/man5/opensc.conf.5.html
Es necesario configurar el fichero:

/etc/opensc/opensc.conf


con


app default {
	disable_popups = true
}


los dipositivos se ven con:

opensc-tool -l
o con
pkcs11-tool --list-slots
El numero es el slot.



Y los algoritmos se ven con:

//Muestra la lista de algoritmos
Provider p = Security.getProviders("Signature.NONEwithRSA")[0];
for (Provider.Service s : p.getServices()) {
    if (s.getType().equals("Signature")) {
        System.out.println("Algoritmo soportado: " + s.getAlgorithm());
    }
}

Se necesita el Path a la libreria del dispositivo:
"/usr/lib/x86_64-linux-gnu/opensc-pkcs11.so"
O la que haya