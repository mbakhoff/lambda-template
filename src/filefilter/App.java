package filefilter;

import java.io.File;
import java.nio.file.Path;

public class App {

  public static void main(String[] args) {
    Path dir = Path.of(args[0]);
    printFiles(dir, file -> file.toString().endsWith(".java"));
    printFiles(dir, file -> file.toString().endsWith(".class"));
  }

  private static void printFiles(Path path, FileFilter filter) {
    for (File file : path.toFile().listFiles()) {
      if (filter.accept(file.toPath())) {
        System.out.println(file);
      }
    }
  }
}
