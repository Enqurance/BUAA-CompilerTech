import Tool.Lexer;
import Tool.Parser;

import java.io.*;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        String unit = readFile();
        Lexer lexer = new Lexer();
        lexer.lexerAnalyzer(unit);
        Parser parser = new Parser(lexer.getTokenList());
        parser.parse();
        /* Choose one to output */
//        parser.outputToFile();
        parser.outputTreeToFile();
    }

    public static String readFile() {
        String path = "testfile.txt";
        StringBuilder sb = new StringBuilder();
        File file = new File(path);
        try (Scanner sc = new Scanner(new FileReader(file))) {
            while (sc.hasNextLine()) {  //按行读取字符串
                String line = sc.nextLine();
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
