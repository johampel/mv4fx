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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.hipphampel.mv4fx.utils.EnumSetCssMetaData.EnumSetConverter;
import java.util.Set;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.geometry.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnumSetCessMetaData_EnumSetConverterTest {

  private EnumSetConverter<Side> converter;

  @BeforeEach
  public void beforeEach() {
    converter = new EnumSetConverter<>(Side.class);
  }

  @Test
  public void convert_convetsIfValuesAreValid() {
    TestValue value = new TestValue("left,TOP", converter);
    assertThat(converter.convert(value, null)).isEqualTo(Set.of(Side.LEFT, Side.TOP));
  }

  @Test
  public void convert_throwsExceptionsIfNotValid() {
    TestValue value = new TestValue("not,valid", converter);
    assertThatThrownBy(() -> converter.convert(value, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  public static class TestValue extends ParsedValue<String, Set<Side>> {

    public TestValue(String value, StyleConverter<String, Set<Side>> converter) {
      super(value, converter);
    }
  }
}
