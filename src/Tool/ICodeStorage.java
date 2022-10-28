package Tool;

import ClassFile.InterCode.ConstDecl;
import ClassFile.InterCode.FuncDecl;
import ClassFile.InterCode.ICode;
import ClassFile.InterCode.Label;
import ClassFile.InterCode.VarDecl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ICodeStorage {
    private final ArrayList<ICode> OriginCodes;
    private final ArrayList<ICode> InterCodes = new ArrayList<>();
    private final ArrayList<ICode> GlobalCodes = new ArrayList<>();
    private final ArrayList<ICode> InitCodes = new ArrayList<>();
    private final HashMap<String, ArrayList<ICode>> FuncLabels = new HashMap<>();

    public ICodeStorage(ArrayList<ICode> iCodes) {
        this.OriginCodes = iCodes;
        RangeUp();
    }

    public void RangeUp() {
        for (ICode code : OriginCodes) {
            if (isGlobalDecl(code)) {
                InterCodes.add(code);
                GlobalCodes.add(code);
            }
        }
        int CodeNum = OriginCodes.size();
        for (int i = 0; i < CodeNum; i++) {
            if (OriginCodes.get(i) instanceof Label && OriginCodes.get(i).toString().equals("$$Init$$:")) {
                InitCodes.add(OriginCodes.get(i));
                InterCodes.add(OriginCodes.get(i));
                i++;
                while (!(OriginCodes.get(i) instanceof Label) && i < CodeNum) {
                    if (!isGlobalDecl(OriginCodes.get(i))) {
                        InitCodes.add(OriginCodes.get(i));
                        InterCodes.add(OriginCodes.get(i));
                    }
                    i++;
                }
            }
        }
        int i = 0;
        while (i < CodeNum) {
            if (i + 1 < CodeNum && OriginCodes.get(i) instanceof Label && OriginCodes.get(i + 1) instanceof FuncDecl) {
                ArrayList<ICode> codes = new ArrayList<>();
                codes.add(OriginCodes.get(i));
                InterCodes.add(OriginCodes.get(i));
                FuncLabels.put(OriginCodes.get(i).toString(), codes);
                i++;
                while (i + 1 < CodeNum && !(OriginCodes.get(i) instanceof Label && OriginCodes.get(i + 1) instanceof FuncDecl)) {
                    if (!isGlobalDecl(OriginCodes.get(i))) {
                        codes.add(OriginCodes.get(i));
                        InterCodes.add(OriginCodes.get(i));
                    }
                    i++;
                }
                if (i + 1 == CodeNum) {
                    codes.add(OriginCodes.get(i));
                    InterCodes.add(OriginCodes.get(i));
                }
                continue;
            }
            i++;
        }
    }

    public boolean isGlobalDecl(ICode code) {
        if (code instanceof ConstDecl) {
            return ((ConstDecl) (code)).isGlobal();
        } else if (code instanceof VarDecl) {
            return ((VarDecl) (code)).isGlobal();
        }
        return false;
    }

    public void OutputAllICodeA() {
        String path = "internA.txt";
        File file = new File(path);
        try (FileWriter writer = new FileWriter(file)) {
            for (ICode code : OriginCodes) {
                writer.write(code.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void OutputAllICodeB() {
        String path = "internB.txt";
        File file = new File(path);
        try (FileWriter writer = new FileWriter(file)) {
            for (ICode code : GlobalCodes) {
                writer.write(code.toString() + "\n");
            }
            for (ICode code : InitCodes) {
                writer.write(code.toString() + "\n");
            }
            for (String str : FuncLabels.keySet()) {
                for (ICode code : FuncLabels.get(str)) {
                    writer.write(code.toString() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ICode> getGlobalCodes() {
        return GlobalCodes;
    }

    public ArrayList<ICode> getInterCodes() {
        return InterCodes;
    }

    public ArrayList<ICode> getInitCodes() {
        return InitCodes;
    }

    public ArrayList<ICode> getOriginCodes() {
        return OriginCodes;
    }

    public HashMap<String, ArrayList<ICode>> getFuncLabels() {
        return FuncLabels;
    }
}
