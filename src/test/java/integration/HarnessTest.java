package integration;
 
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
 
import org.junit.jupiter.api.Test;
 
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
 
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Runs each command-line tool on the JSON inputs in
 * src/test/resources/integration/<tool>/N-in.json and checks that its
 * output matches N-out.json. Outputs are compared as JSON values, so
 * whitespace differences don't matter.
 */
public class HarnessTest {
 
    private static final Path ROOT = Paths.get("src/test/resources/integration");
 
    @Test
    void equationFiltering() throws IOException {
        runAll("equations", xeq.XEq::main);
    }
 
    @Test
    void turnStateExtraction() throws IOException {
        runAll("turn", xturn.XTurn::main);
    }
 
    @Test
    void strategyDecisions() throws IOException {
        runAll("strategy", xstrategy.XStrategy::main);
    }
 
    @Test
    void exchangeRules() throws IOException {
        runAll("rules", xrules.XRules::main);
    }
 
    @Test
    void completeGames() throws IOException {
        runAll("games", xgames.XGames::main);
    }
 
    // runs one tool on every input file in its directory
    private static void runAll(String dir, Consumer<String[]> tool)
            throws IOException {
        List<Path> inputs;
        try (var files = Files.list(ROOT.resolve(dir))) {
            inputs = files.filter(p -> p.toString().endsWith("-in.json"))
                          .sorted()
                          .collect(Collectors.toList());
        }
        assertFalse(inputs.isEmpty(), "no test inputs found in " + dir);
 
        for (Path in : inputs) {
            Path out = Paths.get(in.toString().replace("-in.json", "-out.json"));
            String actual   = run(tool, Files.readString(in));
            String expected = Files.readString(out);
            assertEquals(parseAll(expected), parseAll(actual),
                dir + "/" + in.getFileName() + " produced the wrong output");
        }
    }
 
    // runs a tool's main method with the given stdin and returns its stdout
    private static String run(Consumer<String[]> tool, String input) {
        InputStream oldIn  = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            tool.accept(new String[0]);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
 
    // parses a sequence of JSON values
    private static List<JsonElement> parseAll(String text) {
        List<JsonElement> values = new ArrayList<>();
        JsonStreamParser parser = new JsonStreamParser(text);
        while (parser.hasNext()) values.add(parser.next());
        return values;
    }
}
