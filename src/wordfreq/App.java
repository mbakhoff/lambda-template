package wordfreq;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class App {
  public static void main(String[] args) throws Exception {
    String content = Files.readString(Path.of(args[0]));

    Map<String, Integer> counts = new HashMap<>();
    StringTokenizer tokenizer = new StringTokenizer(content);
    while (tokenizer.hasMoreTokens()) {
      String nextWord = tokenizer.nextToken();
      counts.merge(nextWord, 1, (oldValue, newValue) -> oldValue + newValue);
    }

    counts.forEach((word, count) -> System.out.println(word + ": " + count));
  }
}
