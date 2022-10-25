import ClassFile.Error;
import ClassFile.Node;
import Tool.Generator;
import Tool.ICodeStorage;
import Tool.TableMaster;
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
        TableMaster master = new TableMaster(parser.getHead());
        master.Build();
        //  master.printAllTables();
        Error.printErrorMessage();
        Generator generator = new Generator(parser.getHead(), master.getHeadTable());
        generator.Generate();
        ICodeStorage storage = new ICodeStorage(generator.getCodes());
        storage.OutputAllICode();
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
