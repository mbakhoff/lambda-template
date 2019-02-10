# Inner classes, lambdas

## Inner classes

Java allows declaring classes inside other classes.
Such classes are called inner classes (nested classes).

```java
package app;

public class Outer {
  
  public void runOuter() {
    System.out.println("at outer");
    new Inner().runInner();
  }
  
  public class Inner {
   
    public void runInner() {
      System.out.println("at inner");
    }
  }  
}
```

The `Outer.java` file contains two classes, thus the compiler will generate two class files from it: `app/Outer.class` and `app/Outer$Inner.class`.

A class can contain multiple inner classes.
The top level class is called the **nest host** and the types nested inside it (including the host) are called **nest members**.
Members of the same nest (nest mates) have unrestricted access to each other, including to private fields, methods, and constructors.

```java
package app;

public class Outer {

  private int secret = 42;
  
  public void runOuter() {
    // call private method of nest mate
    new Inner().tellSecret();
  }
  
  private void onSecretExposed() {
    secret = (int) (Math.random() * 1000);   
  }
  
  public class Inner {
   
    private void tellSecret() {
      // access private field of nest mate
      System.out.println("secret is " + secret);
      // call an instance method of enclosing class
      onSecretExposed();
    }
  }  
}
```

The methods inside an inner class can also access instance fields and methods of enclosing classes.
To support this, the compiler automatically adds a constructor parameter in the inner class that is used to pass a reference to the instance of the enclosing class.

The compiler will transform the inner class in the previous code sample as follows:

```java
package app;

public class Outer$Inner {

  final Outer this$0;

  public Outer$Inner(Outer outer) {
    this$0 = outer;
  }

  private tellSecret() {
    System.out.println("secret is " + this$0.secret);
    this$0.onSecretExposed();    
  }
}
```

Clearly the compiler is doing a lot of extra work.
The most invasive of the changes is adding the additional constructor parameter to the inner class' constructors.
This makes it tricky to create instances of the inner class from outside the enclosing class.

```java
class SomethingDifferent {
  static void createInstance() {
    new Outer.Inner(); // error: missing constructor arg
  }
}
```

Note that the name of the class file is `Outer$Inner.class` but the code must use `Outer.Inner`.
That's just how the compiler likes it.

## Static inner classes

An inner class can be marked static in case the magic access to the enclosing classes is not needed.
Adding the static keyword will prevent the compiler from adding the extra constructor parameter for the enclosing class instance.

```java
package app;

public class Outer {
  
  public void runBoth() {
    new Inner1().run();
    new Inner2().run();
  }
  
  public static class Inner1 implements Runnable {   
    public void run() {
      System.out.println("Inner1");
    }
  }
  
  public static class Inner2 implements Runnable {   
    public void run() {
      System.out.println("Inner2");
    }
  }  
}
```

## Anonymous inner classes

Anonymous inner classes are inner classes that are used when a class is needed only in a single place.

```java
package app;

public class Sample {
  public static void main(String[] args) {
    String answer = "An anonymous class.";
    Runnable r = new Runnable() {
      public void run() {
        System.out.println("What am I? " + answer);
      }
    };
    System.out.println(r.getClass().getName()); // app.Sample$1
  }
}
```

The above code declares a new class that implements `Runnable` and also creates an instance of it.
The code doesn't specify a name for the class, so the compiler generates a name automatically.

The code inside the anonymous class can use objects from the enclosing scope.
A constructor is generated for the inner class to pass the necessary references (similar to non-static inner classes). 

The compiler would transform the above example so that the answer string is passed to the inner class:

```java
package app;

class Sample$1 implements Runnable {

  final String val$answer;  

  Sample$1(String answer) {
    val$answer = answer;
  }

  public void run() {
    System.out.println("Who am I? " + val$answer);
  }
}
```

## Lambdas

WIP


