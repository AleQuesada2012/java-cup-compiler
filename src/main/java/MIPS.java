package main.java;

import java.util.*;


public class MIPS {

    private String fileName;
    private StringBuilder textSection;
    private StringBuilder dataSection;
    private Hashtable<String, Integer> structCounter = new Hashtable<>();

    private Stack<String> structController; //--> Como los scopes, saber la estructura en la que voy para saber dónde saltar.
    private HashMap<String,String> registerMap; //sin usar aún --> para saber que variable quedó en qué registro.
    private HashMap<String, Stack<String>> registerHandler; //sin usar aún --> saber que registros están disponibles.


    public void MIPSGenerator(String outputFileName) {
        this.fileName = outputFileName;
        this.textSection = new StringBuilder();
        this.dataSection = new StringBuilder();
        this.structController = new Stack<>();
        this.registerMap = new HashMap<>();
        this.registerHandler = new HashMap<>();

        String[] structures = {"IF", "ELSE", "WHILE", "FOR", "SWITCH", "CASE", "DEFAULT"};
        for (String struct : structures) {structCounter.put(struct, 0);}
    }




    private int getStruct(String structKey) {
        return structCounter.getOrDefault(structKey.toUpperCase(), -1);
    }

    public void makeStartTag(String structKey) {
        structKey = structKey.toUpperCase(); // Convertir a mayúsculas
        int structCount = structCounter.get(structKey);

        this.textSection.append(structKey + "Inicio" + structCount + ":\n");
        this.structController.add(structKey + ":" + structCount);
        structCounter.put(structKey, structCount + 1);
    }

    public void makeEndTag() {
        if (!structController.empty()) {
            String[] data = this.structController.pop().split(":");
            this.textSection.append(data[0] + "FIN" + data[1] + ":\n");
        }
    }


    public void breakLastestLoop() {
        for (int i = structController.size() - 1; i >= 0; i--) {
            String[] data = this.structController.get(i).split(":");
            String structType = data[0].toUpperCase();

            if (structType.equals("SWITCH") || structType.equals("FOR") || structType.equals("WHILE")) {
                this.textSection.append("j " + structType + "END" + data[1] + "\n");
            }
        }
    }

    public void exitProgram() {
        this.textSection.append("li $v0, 10\nsyscall\n");
    }



    public void printInt(int print_value) {
        this.textSection.append("li $v0, 1\nli $a0, " + print_value + "\nsyscall\n");
    }

}