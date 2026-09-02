package org.jabref.logic.os;

import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@NullMarked
class CommandLineParserTest {

    private static final String DIRECTORY = "/home/user/My Documents";

    private static Stream<Arguments> toArguments() {
        return Stream.of(
                Arguments.of(List.of("gnome-terminal", "--working-directory=/home/user/My Documents"),
                        "gnome-terminal --working-directory=%DIR%", DIRECTORY),

                Arguments.of(List.of("gnome-terminal", "--working-directory=/home/user/My Documents"),
                        "gnome-terminal --working-directory=%DIR", DIRECTORY),

                Arguments.of(List.of("/usr/bin/gnome-terminal", "--working-directory=/home/user/My Documents"),
                        "/usr/bin/gnome-terminal --working-directory='%DIR%'", DIRECTORY),

                Arguments.of(List.of("/usr/bin/gnome-terminal", "--working-directory=/home/user/My Documents"),
                        "/usr/bin/gnome-terminal --working-directory=\"%DIR\"", DIRECTORY),

                Arguments.of(List.of("konsole", "--workdir", "/home/user/My Documents"),
                        "konsole   --workdir \t '%DIR%'  ", DIRECTORY),

                Arguments.of(List.of("xfce4-terminal", "--working-directory=/home/user/My Documents/sub folder"),
                        "xfce4-terminal --working-directory=%DIR%/sub\\ folder", DIRECTORY),

                Arguments.of(List.of("C:\\Program Files\\ConEmu\\ConEmu64.exe", "/single", "/dir", "/home/user/My Documents"),
                        "\"C:\\Program Files\\ConEmu\\ConEmu64.exe\" /single /dir \"%DIR%\"", DIRECTORY),

                // the inserted directory must not be scanned for the placeholder again
                Arguments.of(List.of("terminal", "/home/%DIR/project"),
                        "terminal %DIR%", "/home/%DIR/project"),

                // a quoted empty argument must survive, repeated unquoted whitespace must not
                Arguments.of(List.of("tool", "--title", "", "/home/user/My Documents"),
                        "tool --title \"\"   %DIR%", DIRECTORY),

                Arguments.of(List.of(), "   ", DIRECTORY)
        );
    }

    @ParameterizedTest
    @MethodSource
    void toArguments(List<String> expected, String commandLine, String directory) {
        assertEquals(expected, CommandLineParser.toArguments(commandLine, directory));
    }
}
