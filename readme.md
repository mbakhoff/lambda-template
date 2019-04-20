# Inner classes, lambdas

Here we will look at some of the different ways of implementing interfaces in Java and the tricks the compiler uses when dealing with interfaces.

A simple imaginary file server will be used as an example.
The file server can serve files over the network, but it doesn't know which users have access to which files.
An interface must be implemented to help the server make the permission checks:
```java
interface PermissionChecker {
  boolean hasPermission(Path path, String user);
}
```

The most basic way to implement it is to create a separate class:
```java
class PermissiveChecker implements PermissionChecker {

  @Override
  public boolean hasPermission(Path path, String user) {
    return true;
  }
}
```

The file server can then be configured to use the permission checking logic:
```java
public class SimpleApplication {
  public void start() {
    PermissionChecker checker = new PermissiveChecker();
    new FileServer(checker).start();
  }
}
```

## Inner classes

Java allows declaring classes inside other classes.
Such classes are called inner classes (nested classes).
Inner classes are often used to implement an interface or extend a class when the functionality is related to the enclosing class.

```java
package app;

public class SimplerApplication {

  class Checker implements PermissionChecker {
    @Override
    public boolean hasPermission(Path path, String user) {
      return true;
    }
  }

  public void start() {
    PermissionChecker checker = new Checker();
    new FileServer(checker).start();
  }
}
```

The `SimplerApplication.java` file contains two classes, thus the compiler will generate two class files from it: `app/SimplerApplication.class` and `app/SimplerApplication$Checker.class`.

A class can contain multiple inner classes.
The top level class is called the **nest host** and the types nested inside it (including the host) are called **nest members**.
Members of the same nest (nest mates) have unrestricted access to each other, including to private fields, methods, and constructors.

```java
package app;

public class CountingApplication {

  private int filesChecked;

  class Checker implements PermissionChecker {
    @Override
    public boolean hasPermission(Path path, String user) {
      filesChecked++; // use private field of enclosing class
      return true;
    }
  }

  public void start() {
    PermissionChecker checker = new Checker();
    new FileServer(checker).start();
  }
}
```

The methods inside an inner class can access member of the enclosing classes.
To support this, the compiler automatically adds a constructor parameter in the inner class that is used to pass a reference to the instance of the enclosing class.

The compiler will transform the inner class in the previous code sample as follows:

```java
package app;

public class CountingApplication$Checker implements PermissionChecker {

  final CountingApplication this$0;

  public CountingApplication$Checker(CountingApplication app) {
    this$0 = app;
  }

  public boolean hasPermission(Path path, String user) {
    this$0.filesChecked++;
    return true;
  }
}
```

Clearly the compiler is doing a lot of extra work.
The most invasive of the changes is adding the additional constructor parameter to the inner class' constructors.
This makes it tricky to create instances of the inner class from outside the enclosing class.

```java
class SomeOtherClass {
  static PermissionChecker createChecker() {
    return new CountingApplication.Checker(); // error: missing constructor arg
  }
}
```

Note that the name of the class file is `CountingApplication$Checker.class` but the code must use `CountingApplication.Checker`.
That's just how the compiler likes it.

## Static inner classes

An inner class can be marked static in case the enclosing class' fields and methods are not used.
Adding the static keyword will prevent the compiler from adding the extra constructor parameter for the enclosing class instance.

```java
package app;

public class SimplerApplication {

  static class Checker implements PermissionChecker {
    @Override
    public boolean hasPermission(Path path, String user) {
      return true;
    }
  }

  public void start() {
    PermissionChecker checker = new Checker();
    new FileServer(checker).start();
  }
}

class SomeOtherClass {
  static PermissionChecker createChecker() {
    return new SimplerApplication.Checker(); // this works
  }
}
```

Always prefer static inner classes to non-static inner classes.
These have less compiler magic and are easier to understand.

## Anonymous inner classes

Anonymous inner classes are inner classes that are used when a class is needed only in a single place.

```java
package app;

public class SmarterApplication {

  public void start() {
    Map<Path, String> ownerByPath = Map.of(
        Path.of("src"), "theCoder",
        Path.of("docs"), "theWriter"
    );
    PermissionChecker checker = new PermissionChecker() {
      @Override
      public boolean hasPermission(Path path, String user) {
        return ownerByPath.get(path).equals(user);
      }
    };
    new FileServer(checker).start();
  }
}
```

The above code declares a new class that implements `PermissionChecker` and also creates an instance of it.
The code doesn't specify a name for the class, so the compiler generates one automatically.

The code inside the anonymous class can use objects from the enclosing scope.
A constructor is generated for the inner class to pass the necessary references (similar to non-static inner classes).

The compiler would transform the above example so that the owner map is passed to the inner class:

```java
package app;

class SmarterApplication$1 implements PermissionChecker {

  final SmarterApplication this$0;
  final Map<Path, String> val$ownerByPath;

  SmarterApplication$1(SmarterApplication app, Map<Path, String> ownerByPath) {
    this$0 = app;
    val$ownerByPath = ownerByPath;
  }

  public boolean hasPermission(Path path, String user) {
    return val$ownerByPath.get(path).equals(user);
  }
}
```

The method that declares the anonymous class is also transformed to use the compiler-generated class name:

```java
package app;

public class SmarterApplication {

  public void start() {
    Map<Path, String> ownerByPath = Map.of(
        Path.of("src"), "theCoder",
        Path.of("docs"), "theWriter"
    );
    PermissionChecker checker = new SmarterApplication$1(this, ownerByPath);
    new FileServer(checker).start();
  }
}
```

Note that if `ownerByPath` is reassigned (`ownerByPath = someOtherMap;`) after `new SmarterApplication$1(ownerByPath)` is called, then the reference in `SmarterApplication$1` won't be updated.
That's not very obvious from looking at the original code and can easily cause bugs.
To avoid any confusion, any local variables used by an anonymous inner class must be effectively final or the compiler will complain.

## Lambdas

Lambda expressions provide a way to implement an interface in a very concise manner.

```java
package app;

public class SmarterApplication {

  public void start() {
    Map<Path, String> ownerByPath = Map.of(
        Path.of("src"), "theCoder",
        Path.of("docs"), "theWriter"
    );
    PermissionChecker checker = (Path path, String user) -> {
      return ownerByPath.get(path).equals(user);
    };
    new FileServer(checker).start();
  }
}
```

A lambda expression is essentially an anonymous method declaration.
It consists of a list of method parameters enclosed in parentheses, the arrow token `->` and the method body.

A lambda expression can only implement an interface that has exactly one abstract method, because a lambda contains only a single method body.
An interface with a single method is called a *functional interface* and is sometimes annotated with `@FunctionalInterface`.

Once again the compiler will transform the code:

```
package app;

public class SmarterApplication {

  // lambda body
  private static boolean lambda$start$0(Map<Path, String> ownerByPath, Path path, String user) {
    return ownerByPath.get(path).equals(user);
  }

  public void start() {
    Map<Path, String> ownerByPath = Map.of(
        Path.of("src"), "theCoder",
        Path.of("docs"), "theWriter"
    );
    PermissionChecker checker = new SmarterApplication$$Lambda$1(ownerByPath);
    new FileServer(checker).start();
  }
}
```

The transformation is similar to how the inner class constructors are modified:
* Move the lambda method body to a separate method `lambda$start$0`.
  If the lambda uses any objects from its enclosing scope, then add method parameters for passing the necessary references.
* Generate a completely new class `app.SmarterApplication$$Lambda$1` that implements `PermissionChecker`.
  * add a constructor and fields to store any objects that the lambda method body uses.
  * add the `hasPermission` method that calls `lambda$start$0` with the correct arguments.
* Modify the method that declares the lambda, replace the lambda expression with `new app.SmarterApplication$$Lambda$1(...)`.

Note that the `app.SmarterApplication$$Lambda$1` class is generated at runtime when the lambda is first used, unlike inner classes that are generated at compile time.
The exact implementation is quite tricky.
Those who are interested can search for `LambdaMetafactory` and `invokedynamic`.

Lambda syntax is very flexible:
* the parameter list can omit parameter types (types are already declared in the interface)
* if there is only one parameter, then the parenthesis can be omitted
* if the method body is a single expression, then the `return` keyword and braces can be omitted

```java
public class Sample {
  public static void main(String[] args) {
    Map<Path, String> ownerByPath = Map.of();
    // long syntax
    PermissionChecker checker = (Path path, String user) -> {
      return ownerByPath.get(path).equals(user);
    };
    // short syntax
    PermissionChecker checker = (path, user) -> ownerByPath.get(path).equals(user);
    // usage
    System.out.println("has access: " + checker.hasPermission(Path.of("src"), "theCoder"));
  }
}
```

## Method references

Method expressions provide a way to implement a functional interface using an existing method.

```java
package app;

public class SmarterApplication {

  final Map<Path, String> ownerByPath  = Map.of(
      Path.of("src"), "theCoder",
      Path.of("docs"), "theWriter"
  );

  boolean isOwnedBy(Path path, String user) {
    return ownerByPath.get(path).equals(user);
  }

  public void start() {
    PermissionChecker checker = this::isOwnedBy; // method reference
    new FileServer(checker).start();
  }
}
```

Method references are very similar to lambdas.
A method reference can only implement a functional interface.
The method's parameters and return type must match the interface's method.
The compiler uses the same logic as it does for lambdas (generate a new class that calls the necessary method).

## Useful built-ins

Java has some useful built-in functional interfaces:
* java.util.function.Consumer\<T\>
  ```
  Consumer<String> println = System.out::println;
  println.accept("hello world");
  ```
* java.util.function.Supplier\<T\>
  ```
  Random r = new Random();
  Supplier<Integer> random = r::nextInt;
  Integer unpredictable = random.get();
  ```
* java.util.function.Function\<T,R\>
  ```
  Function<String, Integer> toInt = Integer::parseInt;
  Integer i123 = toInt.apply("123");
  ```

## Tasks

### 1. FileFilter

1. Create a functional interface `FileFilter`.
   Its only method `accept` should take a `java.nio.file.Path` as an argument and return a boolean.
2. Write a method `printFiles` that takes two parameters: a directory represented as `java.nio.file.Path` and a `FileFilter`.
   The method finds all the files in the given directory (may ignore subdirectories).
   It calls the filter's `accept` method with each file and if the filter returns true, then prints out the file name.
3. Write a main method that takes a directory path as a command line argument.
   Call the `printFiles` method twice.
   In the first call, implement the filter using a lambda, so that only files ending with ".java" are accepted.
   In the second call, use a lambda that only accepts files ending with ".class".

### 2. FileEditor

1. Write a method `edit` that takes three parameters: a transformer (`Function<String, String>`), an input file and an output file (both `java.nio.file.Path`).
   The method reads the input file line-by-line.
   It calls the transformer function with each line and appends the returned value to the output file.
2. Add a method `tabsToSpaces` that replaces each tab (`\t`) with 4 spaces.
3. Add a method `spacesToTabs` that replaces each consecutive 4 spaces with a tab.
4. Add a main method that takes three command line arguments: input file path, output file path and a single word (either "tabs" or "spaces").
   If the argument is "tabs", then call the `edit` method and pass a method reference to `spacesToTabs` as the transformer.
   If the argument is "spaces", then use a method reference to `tabsToSpaces` instead.

### 3. Word frequency

Read a file word-by-word and count how many times each word appears using a HashMap.
Use the `merge` method of the map to increment the counts.
Implement the remapping function for `merge` with a lambda.
After reading the file, output all the values in the map using the `forEach` method of the map.

### 4. Hand crafted map-filter

1. Write a generic method `filter` that takes two parameters: `List<T>` and `Predicate<T>`.
   The method should call the given predicate with all the elements in the given list and return a new `List<T>`.
   The new list should contain only the elements for which the the predicate returned true.
2. Write a generic method `map` that takes two parameters: `List<T>` and `Function<T,U>`.
   The method should call the given function with all the elements in the given list and return a new `List<U>` containing the results.
3. Write a main method that creates a list of strings, each containing some number (e.g `"-100"`, `"100"`).
   Use `map` to convert all numbers to integers.
   Then use `filter` to keep only positive values.
   Finally output all the filtered values using the `forEach` method of the list.
