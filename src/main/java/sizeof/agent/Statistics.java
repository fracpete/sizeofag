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
 * Statistics.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package sizeof.agent;

import java.io.Serializable;

/**
 * Simple container for collecting statistics per class.
 */
public class Statistics
  implements Serializable {

  /** the overall size calculated for this class. */
  public long total = 0;

  /** the number of object instances. */
  public int count = 0;

  /**
   * Returns count and total as string.
   *
   * @return		the content as string
   */
  @Override
  public String toString() {
    return "{count:" + count + ", total:" + total + "}";
  }
}
