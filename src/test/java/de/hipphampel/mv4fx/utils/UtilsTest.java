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

import de.hipphampel.mv4fx.view.View;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.ViewGroupContainer;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class UtilsTest {

  private Stage stage;

  @Start
  public void start(Stage stage) {
    this.stage = stage;
  }

  @Test
  public void ensureValueIsBetween() {
    assertThat(Utils.ensureValueIsBetween(10.0, -1, -1)).isEqualTo(10.0);
    assertThat(Utils.ensureValueIsBetween(10.0, 20.0, -1)).isEqualTo(20.0);
    assertThat(Utils.ensureValueIsBetween(10.0, -1, 5.0)).isEqualTo(5.0);
    assertThat(Utils.ensureValueIsBetween(10.0, 12.0, 5.0)).isEqualTo(5.0);
  }

  @Test
  public void sum() {
    assertThat(Utils.sum()).isEqualTo(0.0);
    assertThat(Utils.sum(1.0)).isEqualTo(1.0);
    assertThat(Utils.sum(1.0 + 2.0)).isEqualTo(3.0);
    assertThat(Utils.sum(1.0 + 2.0 + 3.0)).isEqualTo(6.0);
    assertThat(Utils.sum(1.0 + 2.0 + 3.0 + 4.0)).isEqualTo(10.0);
  }


  @ParameterizedTest
  @CsvSource({
      " ,,true",
      "a,,false",
      ",a,true",
      "a,a,true",
      "a:b,a,false",
      "a,a:b,true"
  })
  public void isDragAndDropTagMatch(String dragTagsStr, String dropTagsString, boolean expected) {
    Set<String> dragTags = dragTagsStr == null ? null : Arrays.stream(dragTagsStr.split(":"))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .collect(Collectors.toSet());
    Set<String> dropTags = dropTagsString == null ? null : Arrays.stream(dropTagsString.split(":"))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .collect(Collectors.toSet());
    assertThat(Utils.isDragAndDropTagMatch(dragTags, dropTags)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "BOTTOM, TOP",
      "TOP,    BOTTOM",
      "RIGHT,  LEFT",
      "LEFT,   RIGHT",
      ",       "
  })
  public void getOppositeSide(Side in, Side out) {
    assertThat(Utils.getOppositeSide(in)).isEqualTo(out);
  }

  @Test
  public void getParentViewGroupContainer(FxRobot robot) {
    // Arrange
    ViewGroupContainer vgcRoot = new ViewGroupContainer();
    ViewGroupContainer vgcParent = new ViewGroupContainer();
    ViewGroupContainer vgcChild = new ViewGroupContainer();
    ViewGroup vgRoot = new ViewGroup();
    ViewGroup vgParent = new ViewGroup();
    ViewGroup vgChild = new ViewGroup();
    View vRoot = new View();
    vRoot.setTabLabel("root");
    View vParent = new View();
    vParent.setTabLabel("parent");
    View vChild = new View();
    vChild.setTabLabel("child");
    robot.interact(() -> {
      vgChild.addView(vChild);
      vgParent.addView(vParent);
      vgRoot.addView(vRoot);
      vgcChild.setLeftTop(vgChild);
      vgcParent.setLeftTop(vgParent);
      vgcParent.setRightBottom(vgcChild);
      vgcRoot.setLeftTop(vgcParent);
      vgcRoot.setRightBottom(vgRoot);
      stage.setScene(new Scene(vgcRoot));
      stage.show();
    });

    assertThat(Utils.getParentViewGroupContainer(vgcRoot)).isEmpty();
    assertThat(Utils.getParentViewGroupContainer(vgcParent)).contains(vgcRoot);
    assertThat(Utils.getParentViewGroupContainer(vgcChild)).contains(vgcParent);
    assertThat(Utils.getParentViewGroupContainer(vgRoot)).contains(vgcRoot);
    assertThat(Utils.getParentViewGroupContainer(vgParent)).contains(vgcParent);
    assertThat(Utils.getParentViewGroupContainer(vgChild)).contains(vgcChild);
    assertThat(Utils.getParentViewGroupContainer(vRoot.asControl())).contains(vgcRoot);
    assertThat(Utils.getParentViewGroupContainer(vParent.asControl())).contains(vgcParent);
    assertThat(Utils.getParentViewGroupContainer(vChild.asControl())).contains(vgcChild);
  }

  @Test
  public void getRootViewGroupContainer(FxRobot robot) {
    // Arrange
    ViewGroupContainer vgcRoot = new ViewGroupContainer();
    ViewGroupContainer vgcParent = new ViewGroupContainer();
    ViewGroupContainer vgcChild = new ViewGroupContainer();
    ViewGroup vgRoot = new ViewGroup();
    ViewGroup vgParent = new ViewGroup();
    ViewGroup vgChild = new ViewGroup();
    View vRoot = new View();
    vRoot.setTabLabel("root");
    View vParent = new View();
    vParent.setTabLabel("parent");
    View vChild = new View();
    vChild.setTabLabel("child");
    robot.interact(() -> {
      vgChild.addView(vChild);
      vgParent.addView(vParent);
      vgRoot.addView(vRoot);
      vgcChild.setLeftTop(vgChild);
      vgcParent.setLeftTop(vgParent);
      vgcParent.setRightBottom(vgcChild);
      vgcRoot.setLeftTop(vgcParent);
      vgcRoot.setRightBottom(vgRoot);
      stage.setScene(new Scene(vgcRoot));
      stage.show();
    });

    assertThat(Utils.getRootViewGroupContainer(vgcRoot)).contains(vgcRoot);
    assertThat(Utils.getRootViewGroupContainer(vgcParent)).contains(vgcRoot);
    assertThat(Utils.getRootViewGroupContainer(vgcChild)).contains(vgcRoot);
    assertThat(Utils.getRootViewGroupContainer(vgRoot)).contains(vgcRoot);
    assertThat(Utils.getRootViewGroupContainer(vgParent)).contains(vgcRoot);
    assertThat(Utils.getRootViewGroupContainer(vgChild)).contains(vgcRoot);
    assertThat(Utils.getRootViewGroupContainer(vRoot.asControl())).contains(vgcRoot);
    assertThat(Utils.getRootViewGroupContainer(vParent.asControl())).contains(vgcRoot);
    assertThat(Utils.getRootViewGroupContainer(vChild.asControl())).contains(vgcRoot);
  }
}
