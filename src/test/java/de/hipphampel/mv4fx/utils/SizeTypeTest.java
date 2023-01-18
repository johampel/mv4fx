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

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class SizeTypeTest {

  private HBox node;

  @Start
  public void beforeEach(Stage stage) {
    node = new HBox();
    node.setMinWidth(10);
    node.setMinHeight(20);
    node.setPrefWidth(30);
    node.setPrefHeight(40);
    node.setMaxWidth(50);
    node.setMaxHeight(60);
    stage.setScene(new Scene(node));
  }

  @Test
  public void getHeight() {
    assertThat(SizeType.MIN.getHeight(node, -1)).isEqualTo(20);
    assertThat(SizeType.PREF.getHeight(node, -1)).isEqualTo(40);
    assertThat(SizeType.MAX.getHeight(node, -1)).isEqualTo(60);
  }

  @Test
  public void getWidth() {
    assertThat(SizeType.MIN.getWidth(node, -1)).isEqualTo(10);
    assertThat(SizeType.PREF.getWidth(node, -1)).isEqualTo(30);
    assertThat(SizeType.MAX.getWidth(node, -1)).isEqualTo(50);
  }
}
