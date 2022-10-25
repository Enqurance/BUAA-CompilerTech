package Tool;

import ClassFile.InterCode.ConstDecl;
import ClassFile.InterCode.ICode;
import ClassFile.InterCode.Label;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ICodeStorage {
    private final ArrayList<ICode> OriginCodes;
    private ArrayList<ICode> InterCodes = new ArrayList<>();

    public ICodeStorage(ArrayList<ICode> iCodes) {
        this.OriginCodes = iCodes;
        RangeUp();
    }

    public void RangeUp() {
        for (ICode code : OriginCodes) {
            if (code instanceof Label && code.toString().contains("Init")) {
                InterCodes.add(code);
            } else if (code instanceof ConstDecl) {
                InterCodes.add(code);
            }
        }
        for (ICode code : OriginCodes) {
            if (!(code instanceof ConstDecl)) {
                if (!(code instanceof Label && code.toString().contains("Init"))) {
                    InterCodes.add(code);
                }
            }
        }
    }

    public void OutputAllICode() {
        String path = "intern.txt";
        File file = new File(path);
        try (FileWriter writer = new FileWriter(file)) {
            for (ICode code : InterCodes) {
                writer.write(code.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
