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

import java.util.Objects;

/**
 * Collection of constants.
 */
public class Constants {

  /**
   * URL of the CSS.
   */
  public static String CSS_URL = Objects.requireNonNull(Constants.class.getResource(
          "/de/hipphampel/mv4fx/mv4fx.css"))
      .toExternalForm();


  /**
   * Style class for view group container divider
   */
  public static final String CLASS_VIEW_GROUP_CONTAINER_DIVIDER = "divider";

  /**
   * Style class for view groups
   */
  public static final String CLASS_VIEW_GROUP = "view-group";

  /**
   * Style class for view group content
   */
  public static final String CLASS_VIEW_GROUP_CONTENT = "content";

  /**
   * Style class for view group header
   */
  public static final String CLASS_VIEW_GROUP_HEADER = "header";


  /**
   * Style class for view group tab container
   */
  public static final String CLASS_VIEW_GROUP_TAB_CONTAINER = "tab-container";

  /**
   * Style class for view group tab
   */
  public static final String CLASS_VIEW_GROUP_TAB = "tab";

  /**
   * Style class for view group tab button
   */
  public static final String CLASS_VIEW_GROUP_TAB_BUTTON = "tab-button";

  /**
   * Style class for view group tab button
   */
  public static final String CLASS_VIEW_GROUP_TAB_BUTTON_CLOSE = "close-button";

  /**
   * Style class for view group tab button
   */
  public static final String CLASS_VIEW_GROUP_TAB_BUTTON_MAXIMIZE = "maximize-button";

  /**
   * Style class for view group selector
   */
  public static final String CLASS_VIEW_GROUP_SELECTOR = "view-selector";

  /**
   * Style class for view group selector control
   */
  public static final String CLASS_VIEW_GROUP_SELECTOR_CONTROL = "view-selector-control";

  /**
   * Style class for view group selector control
   */
  public static final String CLASS_VIEW_GROUP_SELECTOR_CONTROL_DECORATION = "control-decoration";

  /**
   * Style class for view group selector previous view
   */
  public static final String CLASS_VIEW_GROUP_SELECTOR_PREV = "prev-view";

  /**
   * Style class for view group selector next view
   */
  public static final String CLASS_VIEW_GROUP_SELECTOR_NEXT = "next-view";

  /**
   * Style class for view group selector select view
   */
  public static final String CLASS_VIEW_GROUP_SELECTOR_SELECT = "select-view";

  /**
   * Style class for a drop box
   */
  public static final String CLASS_DROP_TARGET = "drop-target";
}
