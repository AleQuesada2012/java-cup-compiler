package main.java;

import java.util.HashMap;

public class Scope {
    public String name;
    public HashMap<String, String> scopeValues;

    /**
     * Constructor para nuestro scope que permite asignarles un nombre.
     * @param scopeName: string que consiste del nombre deseado para el scope. Casi siempre es el identificador de una función
     *                 pero puede ser el tipo de estructura de control en la que se encuentra.
     */
    public Scope(String scopeName) {
        scopeValues = new HashMap<>();
        this.name = scopeName;
    }
}

