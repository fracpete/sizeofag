# Command line #

Simply add the following parameter to your Java call:
```
  -javaagent:sizeofag.jar
```

# Source code #

The following class outputs the size of an Integer object and of an instance of itself:

```
import sizeof.agent.SizeOfAgent;

public class Blah {
  
  protected Boolean b = false;

  protected Double d = 3.1415;

  protected Integer i = 42;

  public static void main(String[] args) throws Exception {
    Integer i = 3;
    System.out.println(i + ": " + SizeOfAgent.sizeOf(i));

    Blah b = new Blah();
    System.out.println(b + ": " + SizeOfAgent.sizeOf(b));
  } 
} 
```

Command-line:

```
java -cp .:sizeofag.jar -javaagent:sizeofag.jar Blah
```

Output:

```
3: 16
Blah@45bab50a: 24
```