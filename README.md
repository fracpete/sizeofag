# sizeofag

*sizeofag* is a Java Agent that allows you to determine the size of Java
objects from within the JVM at runtime. This makes it very useful for developing
Java frameworks that take memory constraints into account. This project is based
on [Maxim Zakharenkov's code](http://jroller.com/maxim/entry/again_about_determining_size_of).

Please note, that the String class maintains an *internal pool* of String objects
for optimization reasons. These internal strings won't get counted 
(see [String.intern()](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#intern--))
and you may see 0 for some String objects.

*sizeofag* is used, for instance, in [MOA (Massive Online Analysis)](http://moa.cms.waikato.ac.nz/),
a machine learning framework for data streams.

Minimum Java version:
* `<= 1.0.4`: Java 6
* `>= 1.1.0`: Java 8


## License
*sizeofag* is released under [LGPL 3](http://www.gnu.org/licenses/lgpl-3.0.txt).

## Maven
Include the following dependency in your `pom.xml`:

```xml
<dependency>
  <groupId>com.github.fracpete</groupId>
  <artifactId>sizeofag</artifactId>
  <version>1.1.0</version>
</dependency>
```

**NB:** You still need to add the `-javaagent` parameter to your Java call.
See example below for details.

## Examples

You have to start up the JVM with the following additional parameter (adjusting
the path to the jar, of course):

```bash
-javaagent:/path/to/sizeofag-1.1.0.jar
```

If your path contains spaces, then the whole parameter must be enclosed in double quotes:

```bash
"-javaagent:/path/with some spaces/sizeofag-1.1.0.jar"
```

### Total size

The following example code:

```java
import sizeof.agent.SizeOfAgent;

public class SizeTest {

  double d;
  float f;
  int i;
  long l;

  public static String[] randomStrings(int num) {
    String[] result = new String[num];
    for (int i = 0; i < result.length; i++)
      result[i] = "" + Math.random();
    return result;
  }

  public static String randomString() {
    return "" + Math.random();
  }

  public static void main(String[] args) {
    // NB: The JVM maintains an internal pool of String objects.
    // These "internal" String objects won't get counted, so you might
    // see a count of 0 for some strings
    System.out.println("String: " + SizeOfAgent.fullSizeOf("Hello World"));
    System.out.println("int: " + SizeOfAgent.fullSizeOf(2));
    System.out.println("double: " + SizeOfAgent.fullSizeOf(2.3));
    System.out.println("float: " + SizeOfAgent.fullSizeOf(1.5f));
    System.out.println("Object: " + SizeOfAgent.fullSizeOf(new SizeTest()));
    System.out.println("int array: " + SizeOfAgent.fullSizeOf(new int[]{1, 2, 3, 4}));
    System.out.println("String array: " + SizeOfAgent.fullSizeOf(new String[]{"1", "2", "3", "4"}));
    System.out.println("Object array: " + SizeOfAgent.fullSizeOf(new SizeTest[]{new SizeTest()}));
    System.out.println("random String: " + SizeOfAgent.fullSizeOf(randomString()));
    System.out.println("random String array[1]: " + SizeOfAgent.fullSizeOf(randomStrings(1)));
    System.out.println("random String array[10]: " + SizeOfAgent.fullSizeOf(randomStrings(10)));
    System.out.println("random String array[1000]: " + SizeOfAgent.fullSizeOf(randomStrings(1000)));
    System.out.println("random String array[2000]: " + SizeOfAgent.fullSizeOf(randomStrings(2000)));
  }
}
```

will output something like this (21.0.1+12, 64bit on Linux):

```
String: 0
int: 16
double: 24
float: 16
Object: 40
int array: 32
String array: 32
Object array: 64
random String: 0
random String array[1]: 24
random String array[10]: 56
random String array[1000]: 4016
random String array[2000]: 8016
```

### Breakdown per class

It is also possible to break down the size per class by using the `fullSizePerClass` method as in the
following example. This method returns a map of `Class` to `sizeof.agent.Statistics` relation, with
the `Statistics` container class containing the accumulated size for the class and the number of
instances of this object were encountered. 

```java
import sizeof.agent.SizeOfAgent;

public class SizeTest {

  double d;
  float f;
  int i;
  long l;

  public static class InnerClass {
    double d;
    float f;
  }

  public static class AnotherClass {
    InnerClass inner = new InnerClass();
  }

  AnotherClass another1 = new AnotherClass();
  AnotherClass another2 = new AnotherClass();
  AnotherClass another3 = new AnotherClass();
  InnerClass inner1 = new InnerClass();
  InnerClass inner2 = new InnerClass();

  public static void main(String[] args) {
    System.out.println("AnotherClass (full): " + SizeOfAgent.fullSizeOf(new AnotherClass()));
    System.out.println("InnerClass (full): " + SizeOfAgent.fullSizeOf(new InnerClass()));
    System.out.println("SizeTest (full): " + SizeOfAgent.fullSizeOf(new SizeTest()));
    System.out.println("AnotherClass (per class): " + SizeOfAgent.fullSizePerClass(new AnotherClass()));
    System.out.println("InnerClass (per class): " + SizeOfAgent.fullSizePerClass(new InnerClass()));
    System.out.println("SizeTest (per class): " + SizeOfAgent.fullSizePerClass(new SizeTest()));
  }
}
```

Will output something like this (21.0.1+12, 64bit on Linux):

```
AnotherClass (full): 40
InnerClass (full): 24
SizeTest (full): 224
AnotherClass (per class): {class SizeTest$AnotherClass={count:1, total:16}, class SizeTest$InnerClass={count:1, total:24}}
InnerClass (per class): {class SizeTest$InnerClass={count:1, total:24}}
SizeTest (per class): {class SizeTest$AnotherClass={count:3, total:48}, class SizeTest={count:1, total:56}, class SizeTest$InnerClass={count:5, total:120}}
```

### Filtering

With the `sizeof.agent.Filter` class, you can influence how the calculation and
traversal is occurring:

* `public boolean skipSuperClass(Class superclass);`

  Allows you to stop the traversal up the class hierarchy.

* `public boolean skipObject(Object obj);`

  Here you can skip certain objects, e.g., ones that implement a certain 
  interface. The supplied object, however, is always inspected.

* `public boolean skipField(Field field);`

  With this method, you can avoid specific fields declared by the class of the 
  current object.

The following example class will skip instances of `InnerClass` in its 
calculation:

```java
import sizeof.agent.Filter;
import sizeof.agent.SizeOfAgent;

import java.lang.reflect.Field;

public class SizeTest {

  double d;
  float f;
  int i;
  long l;

  public static class InnerClass {
    double d;
    float f;
  }

  public static class AnotherClass {
    InnerClass inner = new InnerClass();
  }

  AnotherClass another1 = new AnotherClass();
  AnotherClass another2 = new AnotherClass();
  AnotherClass another3 = new AnotherClass();
  InnerClass inner1 = new InnerClass();
  InnerClass inner2 = new InnerClass();

  public static void main(String[] args) {
    Filter filter = new Filter() {
      public boolean skipSuperClass(Class superclass) {
        return false;
      }
      public boolean skipObject(Object obj) {
        return (obj instanceof InnerClass);
      }
      public boolean skipField(Field field) {
        return false;
      }
    };
    System.out.println("AnotherClass (per class): " + SizeOfAgent.fullSizePerClass(new AnotherClass(), filter));
    System.out.println("InnerClass (per class): " + SizeOfAgent.fullSizePerClass(new InnerClass(), filter));
    System.out.println("SizeTest (per class): " + SizeOfAgent.fullSizePerClass(new SizeTest(), filter));
  }
}
```

Will output something like this (21.0.1+12, 64bit on Linux):

```
AnotherClass (per class): {class SizeTest$AnotherClass={count:1, total:16}}
InnerClass (per class): {class SizeTest$InnerClass={count:1, total:24}}
SizeTest (per class): {class SizeTest$AnotherClass={count:3, total:48}, class SizeTest={count:1, total:56}}
```
