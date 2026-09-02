package org.jabref.logic.os;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jabref.logic.util.strings.StringUtil;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class CommandLineParser {

    private static final Pattern DIRECTORY_PLACEHOLDER = Pattern.compile("%DIR%?");

    private CommandLineParser() {
    }

    /// Splits a command line into single arguments and replaces the directory placeholder in each of them.
    ///
    /// @param commandLine the command line as entered in the preferences
    /// @param directory the directory the command should be started at
    /// @return the arguments to pass to [ProcessBuilder]
    public static List<String> toArguments(String commandLine, String directory) {
        String replacement = Matcher.quoteReplacement(directory);
        return split(commandLine).stream()
                                 .map(argument -> DIRECTORY_PLACEHOLDER.matcher(argument).replaceAll(replacement))
                                 .toList();
    }

    /// Splits a command line into single arguments, honoring single quotes, double quotes and backslash escapes.
    private static List<String> split(String commandLine) {
        List<String> arguments = new ArrayList<>();
        if (StringUtil.isBlank(commandLine)) {
            return arguments;
        }

        StringBuilder currentArgument = new StringBuilder();
        boolean argumentStarted = false;
        boolean insideSingleQuotes = false;
        boolean insideDoubleQuotes = false;

        for (int index = 0; index < commandLine.length(); index++) {
            char character = commandLine.charAt(index);

            if (character == '\\' && !insideSingleQuotes && isEscapable(commandLine, index + 1)) {
                index++;
                currentArgument.append(commandLine.charAt(index));
                argumentStarted = true;
            } else if (character == '\'' && !insideDoubleQuotes) {
                insideSingleQuotes = !insideSingleQuotes;
                argumentStarted = true;
            } else if (character == '"' && !insideSingleQuotes) {
                insideDoubleQuotes = !insideDoubleQuotes;
                argumentStarted = true;
            } else if (Character.isWhitespace(character) && !insideSingleQuotes && !insideDoubleQuotes) {
                if (argumentStarted) {
                    arguments.add(currentArgument.toString());
                    currentArgument.setLength(0);
                    argumentStarted = false;
                }
            } else {
                currentArgument.append(character);
                argumentStarted = true;
            }
        }

        if (argumentStarted) {
            arguments.add(currentArgument.toString());
        }

        return arguments;
    }

    private static boolean isEscapable(String commandLine, int index) {
        if (index >= commandLine.length()) {
            return false;
        }
        char character = commandLine.charAt(index);
        return character == '\'' || character == '"' || character == '\\' || Character.isWhitespace(character);
    }
}
