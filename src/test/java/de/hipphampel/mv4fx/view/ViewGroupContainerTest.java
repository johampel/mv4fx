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

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.mv4fx.view.skin.ViewGroupContainerSkin;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupContainerTest {

  private Stage stage;
  private ViewGroupContainer container;

  @Start
  public void start(Stage stage) {
    container = new ViewGroupContainer();
    container.setSkin(new ViewGroupContainerSkin(container));

    stage.setScene(new Scene(container));
    stage.setHeight(480 + container.getDividerSize());
    stage.setWidth(420 + container.getDividerSize());
    stage.show();
    this.stage = stage;
  }

  @Test
  public void splitLeftTop_noChildren(FxRobot robot) {
    populateContainer(robot, null, null);
    robot.interact(() -> container.splitLeftTop(Side.TOP, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, null, null);
    robot.interact(() -> container.splitLeftTop(Side.RIGHT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);

    populateContainer(robot, null, null);
    robot.interact(() -> container.splitLeftTop(Side.BOTTOM, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, null, null);
    robot.interact(() -> container.splitLeftTop(Side.LEFT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);
  }

  @Test
  public void splitLeftTop_withLeftTopChildPresent(FxRobot robot) {
    ViewGroup child1 = new ViewGroup();

    populateContainer(robot, child1, null);
    robot.interact(() -> container.splitLeftTop(Side.TOP, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, child1, null);
    robot.interact(() -> container.splitLeftTop(Side.RIGHT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isSameAs(child1);
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);

    populateContainer(robot, child1, null);
    robot.interact(() -> container.splitLeftTop(Side.BOTTOM, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isSameAs(child1);
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, child1, null);
    robot.interact(() -> container.splitLeftTop(Side.LEFT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);
  }

  @Test
  public void splitLeftTop_withRightBottomChildPresent(FxRobot robot) {
    ViewGroup child1 = new ViewGroup();

    populateContainer(robot, null, child1);
    robot.interact(() -> container.splitLeftTop(Side.TOP, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, null, child1);
    robot.interact(() -> container.splitLeftTop(Side.RIGHT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isSameAs(child1);
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);

    populateContainer(robot, null, child1);
    robot.interact(() -> container.splitLeftTop(Side.BOTTOM, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isSameAs(child1);
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, null, child1);
    robot.interact(() -> container.splitLeftTop(Side.LEFT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);
  }

  @Test
  public void splitLeftTop_withTwoChildrenPresent(FxRobot robot) {
    ViewGroup child1 = new ViewGroup();
    ViewGroup child2 = new ViewGroup();

    populateContainer(robot, child1, child2);
    robot.interact(() -> container.splitLeftTop(Side.TOP, ViewGroupContainer::new));
    assertThat(container.getLeftTop().asViewGroupContainer().getLeftTop()).isSameAs(child1);
    assertThat(container.getLeftTop().asViewGroupContainer().getRightBottom()).isNull();
    assertThat(container.getLeftTop().asViewGroupContainer().getOrientation()).isEqualTo(
        Orientation.VERTICAL);
    assertThat(container.getRightBottom()).isSameAs(child2);

    populateContainer(robot, child1, child2);
    robot.interact(() -> container.splitLeftTop(Side.RIGHT, ViewGroupContainer::new));
    assertThat(container.getLeftTop().asViewGroupContainer().getLeftTop()).isNull();
    assertThat(container.getLeftTop().asViewGroupContainer().getRightBottom()).isSameAs(child1);
    assertThat(container.getLeftTop().asViewGroupContainer().getOrientation()).isEqualTo(
        Orientation.HORIZONTAL);
    assertThat(container.getRightBottom()).isSameAs(child2);

    populateContainer(robot, child1, child2);
    robot.interact(() -> container.splitLeftTop(Side.BOTTOM, ViewGroupContainer::new));
    assertThat(container.getLeftTop().asViewGroupContainer().getLeftTop()).isNull();
    assertThat(container.getLeftTop().asViewGroupContainer().getRightBottom()).isSameAs(child1);
    assertThat(container.getLeftTop().asViewGroupContainer().getOrientation()).isEqualTo(
        Orientation.VERTICAL);
    assertThat(container.getRightBottom()).isSameAs(child2);

    populateContainer(robot, child1, child2);
    robot.interact(() -> container.splitLeftTop(Side.LEFT, ViewGroupContainer::new));
    assertThat(container.getLeftTop().asViewGroupContainer().getLeftTop()).isSameAs(child1);
    assertThat(container.getLeftTop().asViewGroupContainer().getRightBottom()).isNull();
    assertThat(container.getLeftTop().asViewGroupContainer().getOrientation()).isEqualTo(
        Orientation.HORIZONTAL);
    assertThat(container.getRightBottom()).isSameAs(child2);
  }

  @Test
  public void splitRightBottom_noChildren(FxRobot robot) {
    populateContainer(robot, null, null);
    robot.interact(() -> container.splitRightBottom(Side.TOP, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, null, null);
    robot.interact(() -> container.splitRightBottom(Side.RIGHT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);

    populateContainer(robot, null, null);
    robot.interact(() -> container.splitRightBottom(Side.BOTTOM, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, null, null);
    robot.interact(() -> container.splitRightBottom(Side.LEFT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);
  }

  @Test
  public void splitRightBottom_withLeftTopChildPresent(FxRobot robot) {
    ViewGroup child1 = new ViewGroup();

    populateContainer(robot, child1, null);
    robot.interact(() -> container.splitRightBottom(Side.TOP, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, child1, null);
    robot.interact(() -> container.splitRightBottom(Side.RIGHT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isSameAs(child1);
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);

    populateContainer(robot, child1, null);
    robot.interact(() -> container.splitRightBottom(Side.BOTTOM, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isSameAs(child1);
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, child1, null);
    robot.interact(() -> container.splitRightBottom(Side.LEFT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);
  }

  @Test
  public void splitRightBottom_withRightBottomChildPresent(FxRobot robot) {
    ViewGroup child1 = new ViewGroup();

    populateContainer(robot, null, child1);
    robot.interact(() -> container.splitRightBottom(Side.TOP, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, null, child1);
    robot.interact(() -> container.splitRightBottom(Side.RIGHT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isSameAs(child1);
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);

    populateContainer(robot, null, child1);
    robot.interact(() -> container.splitRightBottom(Side.BOTTOM, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isNull();
    assertThat(container.getRightBottom()).isSameAs(child1);
    assertThat(container.getOrientation()).isEqualTo(Orientation.VERTICAL);

    populateContainer(robot, null, child1);
    robot.interact(() -> container.splitRightBottom(Side.LEFT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom()).isNull();
    assertThat(container.getOrientation()).isEqualTo(Orientation.HORIZONTAL);
  }

  @Test
  public void splitRightBottom_withTwoChildrenPresent(FxRobot robot) {
    ViewGroup child1 = new ViewGroup();
    ViewGroup child2 = new ViewGroup();

    populateContainer(robot, child1, child2);
    robot.interact(() -> container.splitRightBottom(Side.TOP, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom().asViewGroupContainer().getLeftTop()).isSameAs(child2);
    assertThat(container.getRightBottom().asViewGroupContainer().getRightBottom()).isNull();
    assertThat(container.getRightBottom().asViewGroupContainer().getOrientation()).isEqualTo(
        Orientation.VERTICAL);

    populateContainer(robot, child1, child2);
    robot.interact(() -> container.splitRightBottom(Side.RIGHT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom().asViewGroupContainer().getLeftTop()).isNull();
    assertThat(container.getRightBottom().asViewGroupContainer().getRightBottom()).isSameAs(child2);
    assertThat(container.getRightBottom().asViewGroupContainer().getOrientation()).isEqualTo(
        Orientation.HORIZONTAL);

    populateContainer(robot, child1, child2);
    robot.interact(() -> container.splitRightBottom(Side.BOTTOM, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom().asViewGroupContainer().getLeftTop()).isNull();
    assertThat(container.getRightBottom().asViewGroupContainer().getRightBottom()).isSameAs(child2);
    assertThat(container.getRightBottom().asViewGroupContainer().getOrientation()).isEqualTo(
        Orientation.VERTICAL);

    populateContainer(robot, child1, child2);
    robot.interact(() -> container.splitRightBottom(Side.LEFT, ViewGroupContainer::new));
    assertThat(container.getLeftTop()).isSameAs(child1);
    assertThat(container.getRightBottom().asViewGroupContainer().getLeftTop()).isSameAs(child2);
    assertThat(container.getRightBottom().asViewGroupContainer().getRightBottom()).isNull();
    assertThat(container.getRightBottom().asViewGroupContainer().getOrientation()).isEqualTo(
        Orientation.HORIZONTAL);
  }

  @Test
  public void normalize_closeViewGroupWithAutoClose(FxRobot robot) {
    // Arrange
    ViewGroup left = new ViewGroup();
    left.setAutoClose(true);
    ViewGroup right = new ViewGroup();
    right.setAutoClose(false);
    populateContainer(robot, left, right);

    // Act
    robot.interact(() -> container.normalize());

    // Assert
    assertThat(container.getLeftTop()).isSameAs(right);
    assertThat(container.getRightBottom()).isNull();
  }

  @Test
  public void normalize_collapsesSuperfluousContainers(FxRobot robot) {
    // Arrange
    ViewGroup left = new ViewGroup();
    left.setAutoClose(false);
    ViewGroup right = new ViewGroup();
    right.setAutoClose(false);
    ViewGroupContainer superfluous = new ViewGroupContainer();

    populateContainer(robot, superfluous, left, right);
    populateContainer(robot, null, superfluous);

    // Act
    robot.interact(() -> container.normalize());

    // Assert
    assertThat(container.getLeftTop()).isSameAs(left);
    assertThat(container.getRightBottom()).isSameAs(right);
  }

  private void populateContainer(FxRobot robot, GroupOrContainer leftTop,
      GroupOrContainer rightBottom) {
    populateContainer(robot, container, leftTop, rightBottom);
  }

  private void populateContainer(FxRobot robot, ViewGroupContainer container,
      GroupOrContainer leftTop, GroupOrContainer rightBottom) {
    robot.interact(() -> {
      container.setLeftTop(null);
      container.setRightBottom(null);
      container.setLeftTop(leftTop);
      container.setRightBottom(rightBottom);
    });
  }
}
