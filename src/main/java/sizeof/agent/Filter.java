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

/*
 * Filter.java
 * Copyright (C) 2018-2024 University of Waikato, Hamilton, NZ
 */

package sizeof.agent;

import java.lang.reflect.Field;

/**
 * Filter for influencing inspection of objects/fields/class hierachy.
 */
public interface Filter {

  /**
   * Checks whether to skip this superclass (and everything upwards).
   *
   * @param superclass the class to check
   * @return true if to skip, otherwise we will contain traversing the hierarchy
   */
  public boolean skipSuperClass(Class superclass);

  /**
   * Returns whether to skip the object.
   *
   * @param obj the object to check
   * @return true if to skip the object, otherwise it will get inspected
   */
  public boolean skipObject(Object obj);

  /**
   * Returns whether to skip the field.
   *
   * @param field the field to check
   * @return true if to skip the field, otherwise it will get inspected
   */
  public boolean skipField(Field field);
}
