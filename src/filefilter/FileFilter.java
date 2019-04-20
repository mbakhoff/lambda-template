package filefilter;

import java.nio.file.Path;

@FunctionalInterface
public interface FileFilter {
  boolean accept(Path path);
}
