package com.zolomon.simplechat.client.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BasicCommand implements Command {
    private Pattern regex;

    public BasicCommand(String pattern) {
        regex = Pattern.compile(pattern);
    }

    public boolean isMatch(String line) {
        return regex.matcher(line).matches();
    }

    public abstract void execute();

    public boolean terminate() {
        return false;
    }
}
