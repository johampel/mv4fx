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

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.css.CssMetaData;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.scene.text.Font;

/**
 * CSS metadata representing a set of enum values.
 *
 * @param <S> The styleable
 * @param <T> Type of the enum
 */
public class EnumSetCssMetaData<S extends Styleable, T extends Enum<T>> extends CssMetaData<S, Set<T>> {

  private final Function<S, StyleableProperty<Set<T>>> propertyFunction;

  /**
   * Constructor
   *
   * @param property         Name of the property
   * @param propertyFunction Function to look up the property
   * @param enumClass        The enum type
   * @param initialValue     The initial value
   * @param inherits         Indicates, whether CSS inheritance is wanted
   */
  public EnumSetCssMetaData(String property, Function<S, StyleableProperty<Set<T>>> propertyFunction, Class<T> enumClass,
      Set<T> initialValue, boolean inherits) {
    super(property, new EnumSetConverter<>(enumClass), initialValue, inherits);
    this.propertyFunction = propertyFunction;
  }

  @Override
  public boolean isSettable(S styleable) {
    StyleableProperty<Set<T>> sp = getStyleableProperty(styleable);
    return sp != null && (!(sp instanceof Property<?> p) || !p.isBound());
  }

  @Override
  public StyleableProperty<Set<T>> getStyleableProperty(S styleable) {
    return propertyFunction.apply(styleable);
  }

  static class EnumSetConverter<T extends Enum<T>> extends
      StyleConverter<String, Set<T>> {

    private final Class<T> enumClass;

    public EnumSetConverter(Class<T> enumClass) {
      this.enumClass = enumClass;
    }

    @Override
    public Set<T> convert(ParsedValue<String, Set<T>> value, Font font) {
      if (enumClass == null) {
        return Set.of();
      }

      String string = value.getValue();
      if (string == null) {
        return Set.of();
      }

      return Arrays.stream(string.split(","))
          .map(String::trim)
          .filter(s -> !s.isBlank())
          .map(this::toEnumValue)
          .collect(Collectors.toUnmodifiableSet());
    }

    private T toEnumValue(String string) {
      final int dotPos = string.lastIndexOf('.');
      if (dotPos > -1) {
        string = string.substring(dotPos + 1);
      }
      try {
        string = string.replace('-', '_');
        return Enum.valueOf(enumClass, string.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
        // may throw another IllegalArgumentException
        return Enum.valueOf(enumClass, string);
      }
    }
  }
}
