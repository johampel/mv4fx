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
 * CSS metadata representing a set of string values.
 *
 * @param <S> The styleable
 */
public class StringSetCssMetaData<S extends Styleable> extends
    CssMetaData<S, Set<String>> {

  private final Function<S, StyleableProperty<Set<String>>> propertyFunction;

  /**
   * Constructor
   *
   * @param property         Name of the property
   * @param propertyFunction Function to look up the property
   * @param initialValue     The initial value
   * @param inherits         Indicates, whether CSS inheritance is wanted
   */
  public StringSetCssMetaData(String property,
      Function<S, StyleableProperty<Set<String>>> propertyFunction, Set<String> initialValue, boolean inherits) {
    super(property, new StringSetConverter(), initialValue, inherits);
    this.propertyFunction = propertyFunction;
  }

  @Override
  public boolean isSettable(S styleable) {
    StyleableProperty<Set<String>> sp = getStyleableProperty(styleable);
    return sp != null && (!(sp instanceof Property<?> p) || !p.isBound());
  }

  @Override
  public StyleableProperty<Set<String>> getStyleableProperty(S styleable) {
    return propertyFunction.apply(styleable);
  }

  static class StringSetConverter extends StyleConverter<String, Set<String>> {

    @Override
    public Set<String> convert(ParsedValue<String, Set<String>> value, Font font) {
      String string = value.getValue();

      return Arrays.stream(string.split(","))
          .map(String::trim)
          .filter(s -> !s.isBlank()).collect(Collectors.toUnmodifiableSet());
    }
  }
}
