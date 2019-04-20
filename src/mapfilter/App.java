package mapfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class App {

  public static void main(String[] args) {
    List<String> values = List.of("java", "python", "c++");
    List<String> reversed = map(values, str -> new StringBuilder(str).reverse().toString());
    List<String> filtered = filter(reversed, str -> str.length() > 3);
    filtered.forEach(System.out::println);
  }

  private static List<String> map(List<String> values, Function<String, String> mapper) {
    var result = new ArrayList<String>();
    for (String value : values) {
      result.add(mapper.apply(value));
    }
    return result;
  }

  private static List<String> filter(List<String> values, Predicate<String> filter) {
    var result = new ArrayList<String>();
    for (String value : values) {
      if (filter.test(value))
        result.add(value);
    }
    return result;
  }
}
