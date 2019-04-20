package fileeditor;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class App {

  public static void main(String[] args) throws Exception {
    Path in = Path.of(args[0]);
    Path out = Path.of(args[1]);
    String mode = args[2];

    Function<String, String> transform;
    if (mode.equals("tabs")) {
      transform = App::spacesToTabs;
    } else {
      transform = App::tabsToSpaces;
    }

    edit(in, out, transform);
  }

  private static void edit(Path in, Path out, Function<String, String> transform) throws IOException {
    try (Writer writer = Files.newBufferedWriter(out)) {
      for (String line : Files.readAllLines(in)) {
        writer.write(transform.apply(line));
        writer.write("\n");
      }
    }
  }

  private static String tabsToSpaces(String line) {
    return line.replace("\t", "    ");
  }

  private static String spacesToTabs(String line) {
    return line.replace("    ", "\t");
  }
}
