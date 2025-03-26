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
package de.hipphampel.mv4fx.view;

import javafx.scene.control.Control;

/**
 * Supplemental interface implemented by {@link ViewGroup ViewGroups} and
 * {@link ViewGroupContainer ViewGroupContainers}.
 * <p>
 * Can be used in all situations, where an object might be either a {@code ViewGroup} or a
 * {@code ViewGroupContainer}
 */
public interface GroupOrContainer {


  /**
   * Returns this as a {@link Control} instance.
   *
   * @return This instance
   */
  default Control asControl() {
    return (Control) this;
  }

  /**
   * Returns, whether this is a {@link ViewGroup}
   *
   * @return {@code true}, if a {@code ViewGroup}
   */
  default boolean isViewGroup() {
    return this instanceof ViewGroup;
  }

  /**
   * Returns this as a {@link ViewGroup} instance.
   * <p>
   * Fails, if a not a {@link ViewGroup}
   *
   * @return This instance
   */
  default ViewGroup asViewGroup() {
    return (ViewGroup) this;
  }

  /**
   * Returns, whether this is a {@link ViewGroupContainer}
   *
   * @return {@code true}, if a {@code ViewGroupContainer}
   */
  default boolean isViewGroupContainer() {
    return this instanceof ViewGroupContainer;
  }

  /**
   * Returns this as a {@link ViewGroupContainer} instance.
   * <p>
   * Fails, if a not a {@link ViewGroupContainer}
   *
   * @return This instance
   */
  default ViewGroupContainer asViewGroupContainer() {
    return (ViewGroupContainer) this;
  }
}
