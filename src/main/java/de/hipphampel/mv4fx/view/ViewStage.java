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

import de.hipphampel.mv4fx.utils.Utils;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ViewStage extends Stage {

  // Public constants
  public static final String PROPERTY_AUTO_CLOSE = "autoClose";

  private final BooleanProperty autoClose = new SimpleBooleanProperty(this, PROPERTY_AUTO_CLOSE, true);

  public ViewStage() {
    setOnCloseRequest(this::handleCloseRequest);
  }

  public ViewStage(StageStyle style) {
    super(style);
    setOnCloseRequest(this::handleCloseRequest);
  }

  protected void handleCloseRequest(WindowEvent evt) {
    List<View> views = Utils.getAllViewsIn(this.getScene().getRoot()).toList();
    if (views.stream().allMatch(View::canClose)) {
      views.forEach(View::close);
    } else {
      evt.consume();
    }
  }

  /**
   * Convenience getter for {@code autoClose} property.
   * <p>
   * See documentation of {@link #autoCloseProperty() autoClose} property for details.
   *
   * @return The property value
   */
  public boolean isAutoClose() {
    return autoClose.get();
  }

  /**
   * The {@code autoClose} property.
   * <p>
   * This property is decides, whether the stage is automatically closed/disposed, when the last view of the stage is removed, either
   * because the view has closed or moved away.
   *
   * @return The property.
   */
  public BooleanProperty autoCloseProperty() {
    return autoClose;
  }

  /**
   * Convenience setter for {@code autoClose} property.
   * <p>
   * See documentation of {@link #autoCloseProperty() autoClose} property for details.
   *
   * @param autoClose The property value
   */
  public void setAutoClose(boolean autoClose) {
    this.autoClose.set(autoClose);
  }
}
