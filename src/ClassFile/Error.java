package ClassFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Error {
    private static final ArrayList<Integer> errorMessage = new ArrayList<>();
    private static final HashMap<Integer, String> errorMap = new HashMap<>();

    public static void addErrorMessage(int line, String type) {
        if (!errorMap.containsKey(line)) {
            errorMessage.add(line);
            errorMap.put(line, type);
        }
    }

    public static void printErrorMessage() {
        errorMessage.sort(Comparator.naturalOrder());
        String path = "error.txt";
        File file = new File(path);
        System.out.println(errorMessage.size());
        try (FileWriter writer = new FileWriter(file)) {
            for (int line : errorMessage) {
                writer.write(line + " " + errorMap.get(line) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (errorMessage.size() != 0) {
            throw new RuntimeException("-----Testfile includes ERROR-----");
        }
    }
}
