# Prueba de Lorenzo de Axelor


```bash

./run.sh

```




Iconos:

https://fonts.google.com/icons?icon.set=Material+Icons

# Parametros de Tomcat
TomcatSupport: Crea el fichero de configuración de Tomcat llamado axelor-tomcat.properties
TomcatRun: Parsea lo parámetros de entrada de "./gradlew --no-daemon run --port 8080 --contextPath /"
TomcatRunner: Es la tarea de Grandle que ejecuta la clase que levanta Tomcat
TomcatServer: Es la clase que ejecuta Tomcat finalmente

# Página de inicio
En las view-action añadir el atributo home="true"
Luego en el grupo hay un atributo llamado "homeAction" que hay que poner alguna de las acciones que tengan el atributo home="true"

