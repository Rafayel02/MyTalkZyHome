package com.app.talkzy.OtherClasses;

public class StringFilter {
    private static final char[] notAllowedSymbols = {'*', ' ', '\\', '#', '(', ')', '{', '}', '[', ']', '|', '`', '-', '_'};
    public static Boolean passwordCheck(String password) {
        char[] passwordSymbolsArray = password.toCharArray();
        for(int i = 0; i < passwordSymbolsArray.length; i++) {
            for(int j = 0; j < notAllowedSymbols.length; j++) {
                if(passwordSymbolsArray[i] == notAllowedSymbols[j]) {
                    return false;
                }
            }
        }
        return true;
    }

}
