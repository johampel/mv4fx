/*
 * The MIT License
 * Copyright © 2022 Johannes Hampel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.hipphampel.mv4fx.utils;

/**
 * A range, defined by a {@code lower} and {@code upper} bound.
 *
 * @param lower The lower bound (included in the range)
 * @param upper The upper bound (included in the range)
 */
public record Range(double lower, double upper) {

  /**
   * Returns, whether the range is empty
   *
   * @return {@code true} if empty
   */
  public boolean isEmpty() {
    return lower > upper;
  }

  /**
   * Checks, whether the {@code value} is in this range
   *
   * @param value The value to check
   * @return true if in range
   */
  public boolean contains(double value) {
    return value >= lower && value <= upper;
  }

  /**
   * Gets the size of the range.
   *
   * @return The distance of upper and lower, if not empty.
   */
  public double size() {
    return isEmpty() ? 0 : upper - lower;
  }
}
