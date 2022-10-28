package Tool;

import java.util.HashMap;

public class RegDistributor {
    public static String SPReg = "$sp";
    public static String ZEROReg = "$0";
    public static String V0Reg = "$v0";
    public static String V1Reg = "$v1";
    public static String A0Reg = "$a0";
    public static String RAReg = "$ra";
    private final HashMap<Integer, String> RegName = new HashMap<Integer, String>() {
        {
            put(0, "$zero");
            put(1, "$at");
            put(2, "$v0");
            put(3, "$v1");
            put(4, "$a0");
            put(5, "$a1");
            put(6, "$a2");
            put(7, "$a3");
            put(8, "$t0");
            put(9, "$t1");
            put(10, "$t2");
            put(11, "$t3");
            put(12, "$t4");
            put(13, "$t5");
            put(14, "$t6");
            put(15, "$t7");
            put(16, "$s0");
            put(17, "$s1");
            put(18, "$s2");
            put(19, "$s3");
            put(20, "$s4");
            put(21, "$s5");
            put(22, "$s6");
            put(23, "$s7");
            put(24, "$t8");
            put(25, "$t9");
            put(28, "$gp");
            put(29, "$sp");
            put(30, "$fp");
            put(31, "$ra");
        }
    };
    private final HashMap<Integer, Boolean> TempReg = new HashMap<Integer, Boolean>() {
        {
            put(8, false);
            put(9, false);
            put(10, false);
            put(11, false);
            put(12, false);
            put(13, false);
            put(14, false);
            put(15, false);
            put(24, false);
            put(25, false);
        }
    };
    private int TempRegPtr = 8;

    public String TempRegDistribute() {
        if (TempRegPtr == 16) {
            TempRegPtr = 8;
        }
        String name = RegName.get(TempRegPtr);
        TempRegPtr++;
        return name;
    }
}
