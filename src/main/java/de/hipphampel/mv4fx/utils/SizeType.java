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

import javafx.scene.Node;

/**
 * Enum describing the type of a size.
 */
public enum SizeType {
  /** Describes the minimum size of a component. */
  MIN,
  /** Describes the maximum size of a component. */
  MAX,
  /** Describes the preferred size of a component. */
  PREF;

  /**
   * Gets the height of the {@code node}
   *
   * @param node  The node
   * @param width The width
   * @return The height, depending on this instance, min, max, or pref height
   */
  public double getHeight(Node node, double width) {
    return switch (this) {
      case MIN -> node.minHeight(width);
      case MAX -> node.maxHeight(width);
      case PREF -> node.prefHeight(width);
    };
  }

  /**
   * Gets the width of the {@code node}
   *
   * @param node   The node
   * @param height The height
   * @return The width, depending on this instance, min, max, or pref width
   */
  public double getWidth(Node node, double height) {
    return switch (this) {
      case MIN -> node.minWidth(height);
      case MAX -> node.maxWidth(height);
      case PREF -> node.prefWidth(height);
    };
  }


}
