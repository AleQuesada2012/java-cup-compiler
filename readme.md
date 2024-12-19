# Instrucciones de ejecución del analizador léxico

---
## Usando CMD/Línea de Comandos
Se puede tomar el estado actual del proyecto y su estructura de archivos, de modo que
no hace falta recompilar los archivos `.flex` y `.cup` para tener que generar las clases
necesarias para su ejecución.

Sim embargo, sí es necesario ejecutar al menos el siguiente comando (si se desea trabajar desde
la línea de comandos) para ejecutar el programa:

`javac -cp "lib/*" -d bin src/main/java/*.java`

posteriormente, se puede ejecutar con el comando: `java -cp "bin:lib/*" main.java.Main`

### Si se desea generar/utilizar el .jar en lugar de compilar el código fuente
Para este caso, ya el proyecto cuenta con una versión `.jar` en el directorio raíz. Si se desea
re-generar este archivo para corroborar su congruencia con el código, o bien, ejecutarlo
usando el JAR, se deben seguir las siguientes instrucciones:

1. `jar cfm LexerProyecto.jar MANIFEST.MF -C bin .`
2. Especificar las dependencias
   3. En Linux/MacOS: `java -cp "LexerProyecto.jar:lib/*" main.java.Main`
   4. En Windows: `java -cp "LexerProyecto.jar;lib/*" main.java.Main`
5. `java -jar LexerProyecto.jar`




## Usando el IDE IntelliJ IDEA de JetBrains

Basta con entrar a la estructura de archivos al siguiente directorio: `src/main/java/`
y al encontrar la clase `Main.java`, dar click derecho sobre este archivo en el árbol de
archivos y seleccionar "ejecutar Main", dado que los archivos compilados necesarios están
presentes en la estructura del proyecto (que son el Lexer, el Parser y la clase Sym).

## Re-compilar los archivos generados por jFlex y Cup
Para re-compilar las clases generadas, hará falta ejecutar los siguientes comandos estando
posicionado en el directorio raíz de este proyecto:
* `java -jar lib/jflex-full-1.9.1.jar -d src/main/java src/main/resources/lexerProy1.flex`

* `java -jar lib/java-cup-11b.jar -parser Parser -symbols sym -destdir src/main/java src/main/resources/definitions.cup`

## Bibliotecas necesarias para la ejecución y funcionamiento del proyecto

Las bibliotecas necesarias se encuentran ya en el repositorio dentro del directorio `lib/`
las cuales son:
* `java-cup-11b.jar`
* `java-cup-11b-runtime.jar`
* `jflex-full-1.9.1.jar`

Por lo cual no hace falta descargar archivos adicionales al clonar este repositorio.