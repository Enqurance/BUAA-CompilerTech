package Tool;

import ClassFile.Token;
import ClassFile.TokenType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Lexer {
    private final ArrayList<Token> tokenList = new ArrayList<>();
    private final HashSet<String> reserveWords = new HashSet<String>() {
        {
            addAll(Arrays.asList("main", "const", "int", "break", "continue",
                    "if", "else", "while", "getint", "printf", "return", "void"));
        }
    };

    public void lexerAnalyzer(String compUnit) {
        int curPtr = 0;
        int unitLen = compUnit.length();
        int lineCnt = 1;
        while (curPtr < unitLen) {
            StringBuilder context = new StringBuilder();
            while (curPtr < unitLen && isWhiteSpace(compUnit.charAt(curPtr))) { /* Whitespace */
                if (compUnit.charAt(curPtr) == '\n') lineCnt++;
                curPtr++;
            }
            if (curPtr == unitLen) break;
            if (isNonDigit(compUnit.charAt(curPtr))) {  /* Ident and Reserve */
                while (isIdentContext(compUnit.charAt(curPtr))) {
                    context.append(compUnit.charAt(curPtr));
                    curPtr++;
                }
                if (reserveWords.contains(context.toString())) {
                    createToken(context.toString(), lineCnt, TokenType.ReserveWords);
                } else {
                    createToken(context.toString(), lineCnt, TokenType.Ident);
                }
            } else if (isDigit(compUnit.charAt(curPtr))) {  /* Digit */
                while (curPtr < unitLen && isDigit(compUnit.charAt(curPtr))) {
                    context.append(compUnit.charAt(curPtr));
                    curPtr++;
                }
                createToken(context.toString(), lineCnt, TokenType.Digit);
            } else if (compUnit.charAt(curPtr) == '\"') {
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                while (curPtr < unitLen) {
                    context.append(compUnit.charAt(curPtr));
                    if (compUnit.charAt(curPtr) == '\"') {
                        curPtr++;
                        break;
                    }
                    curPtr++;
                }
                createToken(context.toString(), lineCnt, TokenType.FormatString);
            } else if (compUnit.charAt(curPtr) == '/') {    // / and // and /**/
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                if (compUnit.charAt(curPtr) == '/') {
                    curPtr++;
                    while (curPtr < unitLen) {
                        if (compUnit.charAt(curPtr) == '\n') {
                            lineCnt++;
                            curPtr++;
                            break;
                        }
                        curPtr++;
                    }
                } else if (compUnit.charAt(curPtr) == '*') {
                    curPtr++;
                    while (curPtr < unitLen - 1) {
                        if (compUnit.charAt(curPtr) == '*' && compUnit.charAt(curPtr + 1) == '/') {
                            curPtr += 2;
                            break;
                        } else if (compUnit.charAt(curPtr) == '\n') {
                            lineCnt++;
                        }
                        curPtr++;
                    }
                } else {
                    createToken(context.toString(), lineCnt, TokenType.Note);
                }
            } else if (compUnit.charAt(curPtr) == '!') {    /* ! and != */
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                if (compUnit.charAt(curPtr) == '=') {
                    context.append(compUnit.charAt(curPtr));
                    curPtr++;
                }
                createToken(context.toString(), lineCnt, 4);
            } else if (compUnit.charAt(curPtr) == '&') {    /* && */
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                if (compUnit.charAt(curPtr) == '&') {
                    context.append(compUnit.charAt(curPtr));
                    curPtr++;
                }
                createToken(context.toString(), lineCnt, 4);
            } else if (compUnit.charAt(curPtr) == '|') {    /* || */
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                if (compUnit.charAt(curPtr) == '|') {
                    context.append(compUnit.charAt(curPtr));
                    curPtr++;
                }
                createToken(context.toString(), lineCnt, 4);
            } else if (compUnit.charAt(curPtr) == '<') {    /* < and <= */
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                if (compUnit.charAt(curPtr) == '=') {
                    context.append(compUnit.charAt(curPtr));
                    curPtr++;
                }
                createToken(context.toString(), lineCnt, 4);
            } else if (compUnit.charAt(curPtr) == '>') {    /* > and >= */
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                if (compUnit.charAt(curPtr) == '=') {
                    context.append(compUnit.charAt(curPtr));
                    curPtr++;
                }
                createToken(context.toString(), lineCnt, 4);
            } else if (compUnit.charAt(curPtr) == '=') {    /* = and == */
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                if (compUnit.charAt(curPtr) == '=') {
                    context.append(compUnit.charAt(curPtr));
                    curPtr++;
                }
                createToken(context.toString(), lineCnt, 4);
            } else if (isSingleNote(compUnit.charAt(curPtr))) { /* Single note */
                context.append(compUnit.charAt(curPtr));
                curPtr++;
                createToken(context.toString(), lineCnt, 4);
            } else {
                curPtr++;
            }
        }
    }

    public boolean isWhiteSpace(char c) {
        return (c == '\n' || c == '\t' || c == ' ' || c == '\r' || c == '\0');
    }

    public boolean isSingleNote(char c) {
        return (c == '+' || c == '-' || c == '*' || c == '%' || c == ';'
                || c == ',' || c == '(' || c == ')' || c == '[' || c == ']'
                || c == '{' || c == '}');
    }

    public boolean isNonDigit(char c) {
        return (Character.isLetter(c) || c == '_');
    }

    public boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    public boolean isIdentContext(char c) {
        return isNonDigit(c) || isDigit(c);
    }

    public void createToken(String context, int line, int typeNote) {
        tokenList.add(new Token(context, line, typeNote));
    }

    public void printAllToken() {
        for (Token item : tokenList) {
            System.out.println(item.type + " " + item.context);
        }
    }

    public void outputToFile() {
        String path = "output.txt";
        File file = new File(path);
        try (FileWriter writer = new FileWriter(file)) {
            for (Token item : tokenList) {
                writer.write(item.type + " " + item.context + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
