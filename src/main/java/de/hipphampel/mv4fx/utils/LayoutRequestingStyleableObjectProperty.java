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

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.scene.layout.Region;


/**
 * {@link StyleableObjectProperty} requesting relayout on changes.
 */
public class LayoutRequestingStyleableObjectProperty<T> extends StyleableObjectProperty<T> {

  private final Region bean;
  private final String name;
  private final CssMetaData<? extends Styleable, T> cssMetaData;

  /**
   * Constructor
   *
   * @param bean        The {@link Region}
   * @param name        The name of the property
   * @param cssMetaData The metadata of the CSS style
   */
  public LayoutRequestingStyleableObjectProperty(Region bean, String name,
      CssMetaData<? extends Styleable, T> cssMetaData) {
    super(cssMetaData.getInitialValue(null));
    this.bean = bean;
    this.name = name;
    this.cssMetaData = cssMetaData;
  }

  @Override
  public Object getBean() {
    return bean;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public CssMetaData<? extends Styleable, T> getCssMetaData() {
    return cssMetaData;
  }

  @Override
  protected void invalidated() {
    bean.requestLayout();
  }

}
