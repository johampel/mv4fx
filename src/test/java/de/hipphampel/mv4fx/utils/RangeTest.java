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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class RangeTest {

  @Test
  public void isEmpty_returnsTrueIfRangeIsEmpty() {
    assertThat(new Range(2, 1).isEmpty()).isTrue();
    assertThat(new Range(1, 0).isEmpty()).isTrue();
  }

  @Test
  public void isEmpty_returnsFalseIfRangeNotEmpty() {
    assertThat(new Range(0, 1).isEmpty()).isFalse();
  }

  @Test
  public void contains_returnsWhetherValueIsInRange() {
    assertThat(new Range(1, 2).contains(0.5)).isFalse();
    assertThat(new Range(1, 2).contains(1.0)).isTrue();
    assertThat(new Range(1, 2).contains(1.5)).isTrue();
    assertThat(new Range(1, 2).contains(2.0)).isTrue();
    assertThat(new Range(1, 2).contains(2.5)).isFalse();
  }

  @Test
  public void size_returnsDistanceOfUpperAndLower() {
    assertThat(new Range(1, -1).size()).isEqualTo(0);
    assertThat(new Range(1, 0).size()).isEqualTo(0);
    assertThat(new Range(1, 1).size()).isEqualTo(0);
    assertThat(new Range(1, 2).size()).isEqualTo(1);
  }
}
