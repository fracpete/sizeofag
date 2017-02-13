sizeofag
========

**sizeofag** is a Java Agent that allows you to determine the size of Java objects from within the JVM at runtime. This makes it very useful for developing Java frameworks that take memory constraints into account. This project is based on [Maxim Zakharenkov's code](http://jroller.com/maxim/entry/again_about_determining_size_of).

**sizeofag** is used, for instance, in [MOA (Massive Online Analysis)](http://moa.cms.waikato.ac.nz/), a machine learning framework for data streams.

**sizeofag** is released under [LGPL 3](http://www.gnu.org/licenses/lgpl-3.0.txt).

Maven
=====

Include the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.fracpete</groupId>
    <artifactId>sizeofag</artifactId>
    <version>1.0.1</version>
</dependency>
```

**NB:** You still need to add the `-javaagent` parameter to your Java call.
See example below for details.

Example
=======

You have to start up the JVM with the following additional parameter:

```bash
-javaagent:/path/to/sizeofag-1.0.1.jar
```

The following example code:

```java
import sizeof.agent.SizeOfAgent;

public class SizeTest {

  public static void main(String[] args) throws Exception {
    System.out.println(SizeOfAgent.fullSizeOf(new String("Hello World")));
    System.out.println(SizeOfAgent.fullSizeOf(new Integer(2)));
    System.out.println(SizeOfAgent.fullSizeOf(new Double(2.3)));
    System.out.println(SizeOfAgent.fullSizeOf(new Float(1.5f)));
  }
}
```

will output something like this (1.8.0_121, 64bit on Linux):

```
64
16
24
16
```

