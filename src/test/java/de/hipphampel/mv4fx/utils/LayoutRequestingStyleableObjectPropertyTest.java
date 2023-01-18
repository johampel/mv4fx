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

import javafx.css.CssMetaData;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class LayoutRequestingStyleableObjectPropertyTest {

  private StyleablePropertyFactory<Region> factory;
  private Region region;
  private Pane parent;
  private CssMetaData<Region, Side> cssMetaData;
  private LayoutRequestingStyleableObjectProperty<Side> property;
  private int requestLayoutCalls;

  @Start
  public void beforeEach(Stage stage) {
    factory = new StyleablePropertyFactory<>(Control.getClassCssMetaData());
    region = new Region();
    parent = new Pane(region) {
      @Override
      public void requestLayout() {
        super.requestLayout();
        requestLayoutCalls++;
      }
    };
    cssMetaData = factory.createEnumCssMetaData(Side.class, "some-property", s -> property, Side.TOP, false);
    requestLayoutCalls = 0;
    stage.setScene(new Scene(new HBox()));
    stage.show();
  }

  @Test
  public void ctor_populatesFieldsCorrectly() {
    property = new LayoutRequestingStyleableObjectProperty<>(region, "aName", cssMetaData);

    assertThat(property.getBean()).isSameAs(region);
    assertThat(property.getName()).isEqualTo("aName");
    assertThat(property.getCssMetaData()).isSameAs(cssMetaData);
    assertThat(property.getValue()).isEqualTo(Side.TOP);
  }

  @Test
  public void invalidated_callsRequestLayoutOnParent() {
    property = new LayoutRequestingStyleableObjectProperty(region, "aName", cssMetaData);

    assertThat(requestLayoutCalls).isEqualTo(0);

    property.invalidated();
    assertThat(requestLayoutCalls).isEqualTo(1);
  }

}
