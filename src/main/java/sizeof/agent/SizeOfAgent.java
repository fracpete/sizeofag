/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sizeof.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Instrumentation agent used.
 * <br>
 * This version merely outputs an error message on the commandline if the agent
 * is not present instead of throwing an exception.
 *
 * @author Maxim Zakharenkov
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SizeOfAgent {

  static Instrumentation inst;

  static Boolean messageDisplayed;

  /**
   * initializes agent.
   *
   * @param agentArgs the arguments (ignored)
   * @param instP the instrumentation to use
   */
  public static void premain(String agentArgs, Instrumentation instP) {
    inst = instP;
  }

  /**
   * Returns object size without member sub-objects.
   *
   * @param o object to get size of
   * @return object size, 0 is agent not present
   */
  public static long sizeOf(Object o) {
    if(inst == null) {
      if (messageDisplayed == null) {
	messageDisplayed = true;
	System.err.println("Can not access instrumentation environment.\n" +
	  "Please check if jar file containing SizeOfAgent class is \n" +
	  "specified in the java's \"-javaagent\" command line argument.");
      }
      return 0;
    }
    return inst.getObjectSize(o);
  }

  /**
   * Calculates full size of object iterating over
   * its hierarchy graph.
   *
   * @param obj object to calculate size of
   * @return object size
   */
  public static long fullSizeOf(Object obj) {
    return fullSizeOf(obj, null);
  }

  /**
   * Calculates full size of object iterating over
   * its hierarchy graph.
   *
   * @param obj object to calculate size of, filter does not skip this object
   * @param filter the filter for the objects/fields, can be null
   * @return object size
   */
  public static long fullSizeOf(Object obj, Filter filter) {
    Map<Object, Object> visited = new IdentityHashMap<Object, Object>();
    Stack<Object> stack = new Stack<Object>();

    long result = internalSizeOf(obj, stack, visited, null, null);
    while (!stack.isEmpty())
      result += internalSizeOf(stack.pop(), stack, visited, null, filter);
    visited.clear();
    return result;
  }

  /**
   * Calculates size/count statistics of object iterating over
   * its hierarchy graph, breaking it down per class.
   *
   * @param obj object to calculate size of
   * @return the breakdown per class
   */
  public static Map<Class,Statistics> fullSizePerClass(Object obj) {
    return fullSizePerClass(obj, null);
  }

  /**
   * Calculates size/count statistics of object iterating over
   * its hierarchy graph, breaking it down per class.
   *
   * @param obj object to calculate size of, filter does not skip this object
   * @param filter the filter for the objects/fields, can be null
   * @return the breakdown per class
   */
  public static Map<Class,Statistics> fullSizePerClass(Object obj, Filter filter) {
    Map<Class,Statistics> perClass = new HashMap<Class, Statistics>();
    Map<Object, Object> visited = new IdentityHashMap<Object, Object>();
    Stack<Object> stack = new Stack<Object>();

    long result = internalSizeOf(obj, stack, visited, perClass, null);
    while (!stack.isEmpty())
      result += internalSizeOf(stack.pop(), stack, visited, perClass, filter);
    visited.clear();
    return perClass;
  }

  /**
   * Determines whether to skip an object.
   *
   * @param obj	the object to check
   * @param visited the objects visited so far
   * @return true if to skip
   */
  private static boolean skipObject(Object obj, Map<Object, Object> visited, Filter filter) {
    if (obj == null)
      return true;

    if (obj instanceof String) {
      // skip interned string
      if (obj == ((String) obj).intern())
	return true;
    }

    if (filter != null) {
      if (filter.skipObject(obj))
        return true;
    }

    // skip visited object
    return (visited.containsKey(obj));
  }

  private static long internalSizeOf(Object obj, Stack<Object> stack, Map<Object, Object> visited, Map<Class,Statistics> perClass, Filter filter) {
    if (skipObject(obj, visited, filter))
      return 0;

    visited.put(obj, null);

    long result = 0;
    // get size of object + primitive variables + member pointers
    result += sizeOf(obj);
    if (perClass != null) {
      Class cls = obj.getClass();
      if (!perClass.containsKey(cls))
        perClass.put(cls, new Statistics());
      perClass.get(cls).count++;
      perClass.get(cls).total += result;
    }

    // process all array elements
    Class clazz = obj.getClass();
    if (clazz.isArray()) {
      if(clazz.getName().length() != 2) {// skip primitive type array
	int length =  Array.getLength(obj);
	for (int i = 0; i < length; i++)
	  stack.add(Array.get(obj, i));
      }
      return result;
    }

    // process all fields of the object
    while (clazz != null) {
      Field[] fields = clazz.getDeclaredFields();
      for (int i = 0; i < fields.length; i++) {
	if (!Modifier.isStatic(fields[i].getModifiers())) {
	  // skip primitive fields
	  if (fields[i].getType().isPrimitive())
	    continue;

	  // field filter?
	  if (filter != null) {
	    if (filter.skipField(fields[i]))
	      continue;
	  }

	  try {
	    fields[i].setAccessible(true);
	    try {
	      // objects to be estimated are put to stack
	      Object objectToAdd = fields[i].get(obj);
	      if (objectToAdd != null) {
		stack.add(objectToAdd);
	      }
	    }
	    catch (IllegalAccessException ex) {
	      assert false;
	    }
	  }
	  catch (Throwable e) {
	    // if we can't make it accessible, then just leave it be
	  }
	}
      }

      clazz = clazz.getSuperclass();
      // skip superclass?
      if (filter != null) {
        if (filter.skipSuperClass(clazz))
          clazz = null;
      }
    }
    return result;
  }
}
