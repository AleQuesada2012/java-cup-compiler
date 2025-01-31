package main.java;

public class MIPSTest {
    public static void main(String[] args) {
        MIPS mips = new MIPS("output.asm");

        // Test arithmetic
        String resultReg = mips.generateArithmetic("5", "+", "10");
        mips.generateAssignment("a", resultReg);

        // Test condition
        String endLabel = mips.generateCondition("a", ">", "10");
        mips.generateAssignment("a", "5");
        mips.textSection.append(endLabel + ":\n");

        // Test array access
        mips.declareArray("arr", 10); // Declare an array of size 10
        String indexReg = mips.generateArithmetic("2", "+", "3");
        String arrayElement = mips.generateArrayAccess("arr", indexReg);
        mips.generateAssignment("b", arrayElement);

        // Write to file
        mips.writeToFile();
    }
}