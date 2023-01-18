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
package de.hipphampel.mv4fx.view.skin;

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.ViewGroupContainer;
import de.hipphampel.mv4fx.view.ViewGroupContainer.ResizePolicy;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupContainerSkinTest {

  private Stage stage;
  private ViewGroup viewGroup1;
  private ViewGroup viewGroup2;
  private ViewGroupContainer container;

  @Start
  public void start(Stage stage) {
    viewGroup1 = new ViewGroup();
    viewGroup2 = new ViewGroup();
    container = new ViewGroupContainer();
    container.setLeftTop(viewGroup1);
    container.setRightBottom(viewGroup2);
    container.setSkin(new ViewGroupContainerSkin(container));

    stage.setScene(new Scene(container));
    stage.setHeight(480 + container.getDividerSize());
    stage.setWidth(420 + container.getDividerSize());
    stage.show();
    this.stage = stage;
  }


  @Test
  public void ifNoChildren_skinHasNoChildrenAsWell(FxRobot robot) {
    robot.interact(() -> container.setLeftTop(null));
    robot.interact(() -> container.setRightBottom(null));

    assertThat(((ViewGroupContainerSkin) container.getSkin()).getChildren()).isEmpty();
  }

  @Test
  public void ifLeftTopOnly_skinHasOnlyLeftTopAsChild(FxRobot robot) {
    robot.interact(() -> container.setLeftTop(null));
    robot.interact(() -> container.setRightBottom(null));
    robot.interact(() -> container.setLeftTop(viewGroup1));

    assertThat(((ViewGroupContainerSkin) container.getSkin()).getChildren()).containsExactly(
        viewGroup1.asControl()
    );
    assertThat(viewGroup1.getWidth()).isEqualTo(container.getWidth());
    assertThat(viewGroup1.getHeight()).isEqualTo(container.getHeight());
  }

  @Test
  public void ifRightBottomOnly_skinHasOnlyRIghtBottomAsChild(FxRobot robot) {
    robot.interact(() -> container.setLeftTop(null));
    robot.interact(() -> container.setRightBottom(null));
    robot.interact(() -> container.setRightBottom(viewGroup1));

    assertThat(((ViewGroupContainerSkin) container.getSkin()).getChildren()).containsExactly(
        viewGroup1.asControl()
    );
    assertThat(viewGroup1.getWidth()).isEqualTo(container.getWidth());
    assertThat(viewGroup1.getHeight()).isEqualTo(container.getHeight());
  }

  @Test
  public void ifBothSet_skinHasDivider(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setLeftTop(null));
    robot.interact(() -> container.setRightBottom(null));

    robot.interact(() -> container.setPosition(0.25));
    robot.interact(() -> container.setLeftTop(viewGroup1));
    robot.interact(() -> container.setRightBottom(viewGroup2));

    assertThat(
        ((ViewGroupContainerSkin) container.getSkin()).getChildren()).containsExactlyInAnyOrder(
        viewGroup1.asControl(),
        viewGroup2.asControl(),
        ((ViewGroupContainerSkin) container.getSkin()).getDivider()
    );
    assertThat(viewGroup1.getWidth()).isEqualTo(container.getWidth());
    assertThat(viewGroup1.getHeight()).isEqualTo(120);
    assertThat(viewGroup2.getWidth()).isEqualTo(container.getWidth());
    assertThat(viewGroup2.getHeight()).isEqualTo(360);
  }

  @Test
  public void setDividerSize_updatesTheLayoutIfVertical(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RATIO));

    double size1 = viewGroup1.getHeight();
    double size2 = viewGroup2.getHeight();

    robot.interact(() -> container.setDividerSize(20 + container.getDividerSize()));

    assertThat(viewGroup1.getHeight()).isEqualTo(size1 - 10);
    assertThat(viewGroup2.getHeight()).isEqualTo(size2 - 10);
  }

  @Test
  public void dispose_removesListeners(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RATIO));

    double size1 = viewGroup1.getHeight();
    double size2 = viewGroup2.getHeight();

    container.getSkin().dispose();

    robot.interact(() -> container.setDividerSize(20 + container.getDividerSize()));
    assertThat(viewGroup1.getHeight()).isEqualTo(size1);
    assertThat(viewGroup2.getHeight()).isEqualTo(size2);

    robot.interact(() -> container.setPosition(0.20));
    assertThat(viewGroup1.getHeight()).isEqualTo(size1);
    assertThat(viewGroup2.getHeight()).isEqualTo(size2);

    robot.interact(() -> container.setAbsolutePosition(20));
    assertThat(viewGroup1.getHeight()).isEqualTo(size1);
    assertThat(viewGroup2.getHeight()).isEqualTo(size2);

    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    assertThat(viewGroup1.getHeight()).isEqualTo(size1);
    assertThat(viewGroup2.getHeight()).isEqualTo(size2);
  }

  @Test
  public void setDividerSize_updatesTheLayoutHorizontal(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RATIO));

    double size1 = viewGroup1.getWidth();
    double size2 = viewGroup2.getWidth();

    robot.interact(() -> container.setDividerSize(20 + container.getDividerSize()));

    assertThat(viewGroup1.getWidth()).isEqualTo(size1 - 10);
    assertThat(viewGroup2.getWidth()).isEqualTo(size2 - 10);
  }

  @Test
  public void setPadding_updatesTheLayoutIfVertical(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RATIO));

    double size1 = viewGroup1.getHeight();
    double size2 = viewGroup2.getHeight();

    robot.interact(() -> container.setPadding(new Insets(30)));

    assertThat(viewGroup1.getHeight()).isEqualTo(size1 - 30);
    assertThat(viewGroup2.getHeight()).isEqualTo(size2 - 30);
  }

  @Test
  public void setPadding_updatesTheLayoutIfHorizontal(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RATIO));

    double size1 = viewGroup1.getWidth();
    double size2 = viewGroup2.getWidth();
    robot.interact(() -> container.setPadding(new Insets(30)));

    assertThat(viewGroup1.getWidth()).isEqualTo(size1 - 30);
    assertThat(viewGroup2.getWidth()).isEqualTo(size2 - 30);
  }

  @Test
  public void resize_verticalAndResizePolicyKeepRatio(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RATIO));

    double size1 = viewGroup1.getHeight();
    double size2 = viewGroup2.getHeight();

    robot.interact(() -> stage.setHeight(240 + container.getDividerSize()));

    assertThat(viewGroup1.getHeight()).isEqualTo(size1 / 2);
    assertThat(viewGroup2.getHeight()).isEqualTo(size2 / 2);
  }

  @Test
  public void resize_horizontalAndResizePolicyKeepRatio(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RATIO));

    double size1 = viewGroup1.getWidth();
    double size2 = viewGroup2.getWidth();
    robot.interact(() -> stage.setWidth(210 + container.getDividerSize()));

    assertThat(viewGroup1.getWidth()).isEqualTo(size1 / 2);
    assertThat(viewGroup2.getWidth()).isEqualTo(size2 / 2);
  }

  @Test
  public void resize_verticalAndResizePolicyKeepLeftTopSize(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_LEFT_TOP_SIZE));

    double size1 = viewGroup1.getHeight();

    robot.interact(() -> stage.setHeight(360 + container.getDividerSize()));

    assertThat(viewGroup1.getHeight()).isEqualTo(size1);
    assertThat(viewGroup2.getHeight()).isEqualTo(
        container.getHeight() - container.getDividerSize() - size1);
  }

  @Test
  public void resize_horizontalAndResizePolicyKeepLeftTopSize(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_LEFT_TOP_SIZE));

    double size1 = viewGroup1.getWidth();

    robot.interact(() -> stage.setHeight(305 + container.getDividerSize()));

    assertThat(viewGroup1.getWidth()).isEqualTo(size1);
    assertThat(viewGroup2.getWidth()).isEqualTo(
        container.getWidth() - container.getDividerSize() - size1);
  }

  @Test
  public void resize_verticalAndResizePolicyKeepRightBottomSize(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RIGHT_BOTTOM_SIZE));

    double size2 = viewGroup2.getHeight();

    robot.interact(() -> stage.setHeight(360 + container.getDividerSize()));

    assertThat(viewGroup1.getHeight()).isEqualTo(
        container.getHeight() - container.getDividerSize() - size2);
    assertThat(viewGroup2.getHeight()).isEqualTo(size2);
  }

  @Test
  public void resize_horizontallAndResizePolicyKeepRightBottomSize(FxRobot robot) {
    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    robot.interact(() -> container.setResizePolicy(ResizePolicy.KEEP_RIGHT_BOTTOM_SIZE));
    double size2 = viewGroup2.getWidth();

    robot.interact(() -> stage.setWidth(305 + container.getDividerSize()));

    assertThat(viewGroup1.getWidth()).isEqualTo(
        container.getWidth() - container.getDividerSize() - size2);
    assertThat(viewGroup2.getWidth()).isEqualTo(size2);
  }
}
