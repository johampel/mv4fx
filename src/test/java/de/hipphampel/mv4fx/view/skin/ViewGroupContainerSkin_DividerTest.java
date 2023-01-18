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

import de.hipphampel.mv4fx.utils.Range;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.ViewGroupContainer;
import de.hipphampel.mv4fx.view.ViewGroupContainer.DividerDragMode;
import de.hipphampel.mv4fx.view.skin.ViewGroupContainerSkin.Divider;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupContainerSkin_DividerTest {

  private ViewGroup viewGroup1;
  private ViewGroup viewGroup2;
  private ViewGroupContainer container;
  private Divider divider;

  @Start
  public void start(Stage stage) {
    viewGroup1 = new ViewGroup();
    viewGroup2 = new ViewGroup();
    container = new ViewGroupContainer();
    container.setLeftTop(viewGroup1);
    container.setRightBottom(viewGroup2);
    container.setSkin(new ViewGroupContainerSkin(container));

    stage.setScene(new Scene(container));
    stage.setHeight(480);
    stage.setWidth(420);
    stage.show();
    divider = ((ViewGroupContainerSkin) container.getSkin()).getDivider();
  }

  @Test
  public void ctor_setCorrectStyler(FxRobot robot) {
    assertThat(divider.getStyleClass()).contains("divider");
  }

  @Test
  public void getDragRange_returnsNullIfDividerDragModeIsFixed(FxRobot robot) {
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.FIXED));

    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    assertThat(divider.getDragRange()).isNull();

    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    assertThat(divider.getDragRange()).isNull();
  }

  @Test
  public void getDragRange_returnsFullPossibleRangeIfDividerDragModeIsFree(FxRobot robot) {
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.FREE));
    robot.interact(() -> container.setPadding(new Insets(1, 2, 3, 4)));

    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    assertThat(divider.getDragRange()).isEqualTo(
        new Range(container.snappedTopInset(),
            container.getHeight() - container.getDividerSize() - container.snappedBottomInset()));

    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    assertThat(divider.getDragRange()).isEqualTo(
        new Range(container.snappedLeftInset(),
            container.getWidth() - container.getDividerSize() - container.snappedRightInset()));
  }

  @Test
  public void getDragRange_returnsRestrictedRangeIfDividerDragModeIsRespectMinSize(FxRobot robot) {
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.RESPECT_MIN_SIZE));
    robot.interact(() -> container.setPadding(new Insets(1, 2, 3, 4)));
    robot.interact(() -> viewGroup1.setMinWidth(5));
    robot.interact(() -> viewGroup1.setMinHeight(10));
    robot.interact(() -> viewGroup2.setMinWidth(15));
    robot.interact(() -> viewGroup2.setMinHeight(20));
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));

    assertThat(divider.getDragRange()).isEqualTo(
        new Range(container.snappedTopInset() + viewGroup1.getMinHeight(),
            container.getHeight() - container.getDividerSize() - container.snappedBottomInset()
                - viewGroup2.getMinHeight()));

    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    assertThat(divider.getDragRange()).isEqualTo(
        new Range(container.snappedLeftInset() + viewGroup1.getMinWidth(),
            container.getWidth() - container.getDividerSize() - container.snappedRightInset()
                - viewGroup2.getMinWidth()));
  }

  @Test
  public void moveDivider_horizontalAndDividerDragModeFixed(FxRobot robot)
      throws InterruptedException {
    viewGroup1.setMinWidth(100);
    viewGroup2.setMinWidth(100);
    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.FIXED));

    double width = container.getWidth();
    double pos = container.getAbsolutePosition();
    // drag to left
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(-pos, 0);
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);

    // drag to middle
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(pos, 0);
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);

    // drag to right
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(width - pos, 0);
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);
  }

  @Test
  public void moveDivider_verticalAndDividerDragModeFixed(FxRobot robot)
      throws InterruptedException {
    viewGroup1.setMinHeight(100);
    viewGroup2.setMinHeight(100);
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.FIXED));

    double height = container.getHeight();
    double pos = container.getAbsolutePosition();
    // drag to top
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, -pos);
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);

    // drag to middle
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, pos);
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);

    // drag to bottom
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, height - pos);
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);
  }

  @Test
  public void moveDivider_horizontalAndDividerDragModeFree(FxRobot robot) {
    viewGroup1.setMinWidth(100);
    viewGroup2.setMinWidth(100);
    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.FREE));

    double width = container.getWidth();
    double pos = container.getAbsolutePosition();
    // drag to left
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(-pos, 1);
    assertThat(container.getAbsolutePosition()).isEqualTo(0);

    // drag to middle
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(pos, 1);
    assertThat(container.getAbsolutePosition()).isCloseTo(pos, Offset.offset(1.0));

    // drag to right
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(width - pos, 0);
    assertThat(container.getAbsolutePosition()).isCloseTo(width - container.getDividerSize(),
        Offset.offset(1.0));
  }

  @Test
  public void moveDivider_verticalAndDividerDragModeFree(FxRobot robot) {
    viewGroup1.setMinHeight(100);
    viewGroup2.setMinHeight(100);
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.FREE));

    double height = container.getHeight();
    double pos = container.getAbsolutePosition();
    // drag to top
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, -pos);
    assertThat(container.getAbsolutePosition()).isEqualTo(0);

    // drag to middle
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, pos);
    assertThat(container.getAbsolutePosition()).isCloseTo(pos, Offset.offset(1.0));

    // drag to bottom
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, height - pos);
    assertThat(container.getAbsolutePosition()).isCloseTo(height - container.getDividerSize(),
        Offset.offset(1.0));
  }

  @Test
  public void moveDivider_horizontalAndDividerDragModeRespectMinSize(FxRobot robot) {
    viewGroup1.setMinWidth(100);
    viewGroup2.setMinWidth(100);
    robot.interact(() -> container.setOrientation(Orientation.HORIZONTAL));
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.RESPECT_MIN_SIZE));

    double width = container.getWidth();
    double pos = container.getAbsolutePosition();
    // drag to left
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(-pos, 0);
    assertThat(container.getAbsolutePosition()).isCloseTo(viewGroup1.getMinWidth(),
        Offset.offset(1.0));

    // drag to middle
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(pos - viewGroup1.getMinWidth(), 0);
    assertThat(container.getAbsolutePosition()).isCloseTo(pos, Offset.offset(2.0));

    // drag to right
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(width - pos, 0);
    assertThat(container.getAbsolutePosition()).isCloseTo(
        width - viewGroup2.getMinWidth() - container.getDividerSize(),
        Offset.offset(1.0));
  }

  @Test
  public void moveDivider_verticalAndDividerDragModeRespectMinSize(FxRobot robot) {
    viewGroup1.setMinHeight(100);
    viewGroup2.setMinHeight(100);
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.RESPECT_MIN_SIZE));

    double height = container.getHeight();
    double pos = container.getAbsolutePosition();
    // drag to top
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, -pos);
    assertThat(container.getAbsolutePosition()).isCloseTo(viewGroup1.getMinHeight(),
        Offset.offset(2.0));

    // drag to middle
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, pos - viewGroup1.getMinHeight());
    assertThat(container.getAbsolutePosition()).isCloseTo(pos, Offset.offset(2.0));

    // drag to bottom
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, height - pos);
    assertThat(container.getAbsolutePosition()).isCloseTo(
        height - viewGroup2.getMinHeight() - container.getDividerSize(),
        Offset.offset(2.0));
  }

  @Test
  public void dispose_removesTheListeners(FxRobot robot) {
    // We test that implicitly by showing that the divider is not moved
    robot.interact(() -> container.setOrientation(Orientation.VERTICAL));
    robot.interact(() -> container.setDividerDragMode(DividerDragMode.RESPECT_MIN_SIZE));
    robot.interact(() -> divider.dispose());

    double height = container.getHeight();
    double pos = container.getAbsolutePosition();
    // drag to top
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, -pos);
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);

    // drag to middle
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, pos - viewGroup1.getMinHeight());
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);

    // drag to bottom
    robot.moveTo(divider, new Point2D(1, 1));
    robot.drag(MouseButton.PRIMARY).dropBy(0, height - pos);
    assertThat(container.getAbsolutePosition()).isEqualTo(pos);
  }
}
