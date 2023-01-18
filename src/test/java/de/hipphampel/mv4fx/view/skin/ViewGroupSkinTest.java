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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hipphampel.mv4fx.view.DragAndDropContext;
import de.hipphampel.mv4fx.view.DropTarget;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.ContentContainer;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.HeaderContainer;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupSkinTest {

  private ViewGroup viewGroup;
  private ViewGroupSkin skin;
  private HeaderContainer headerContainer;
  private ContentContainer contentContainer;

  @Start
  public void start(Stage stage) {
    viewGroup = new ViewGroup();
    skin = new ViewGroupSkin(viewGroup);
    viewGroup.setSkin(skin);
    stage.setScene(new Scene(viewGroup));
    stage.setHeight(450);
    stage.setWidth(350);
    stage.show();
    contentContainer = skin.getContentContainer();
    headerContainer = skin.getHeaderContainer();
  }

  @Test
  public void ctor_initializesFieldsCorrectly(FxRobot robot) {
    assertThat(skin.getHeaderContainer()).isNotNull();
    assertThat(skin.getContentContainer()).isNotNull();
  }

  @Test
  public void headersTopIfSideIsTop(FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.TOP));

    assertThat(headerContainer.getLayoutX()).isEqualTo(viewGroup.snappedLeftInset());
    assertThat(headerContainer.getLayoutY()).isEqualTo(viewGroup.snappedTopInset());
    assertThat(headerContainer.getWidth()).isEqualTo(
        viewGroup.getWidth() - viewGroup.snappedLeftInset() - viewGroup.snappedRightInset());
    assertThat(headerContainer.getHeight()).isGreaterThan(0);
    assertThat(headerContainer.getTransforms()).isEmpty();

    assertThat(contentContainer.getLayoutX()).isEqualTo(viewGroup.snappedLeftInset());
    assertThat(contentContainer.getLayoutY()).isEqualTo(
        viewGroup.snappedTopInset() + headerContainer.getHeight());
    assertThat(contentContainer.getWidth()).isEqualTo(
        viewGroup.getWidth() - viewGroup.snappedLeftInset() - viewGroup.snappedRightInset());
    assertThat(contentContainer.getHeight()).isEqualTo(
        viewGroup.getHeight() - viewGroup.snappedTopInset() - viewGroup.snappedBottomInset()
            - headerContainer.getHeight()
    );
    assertThat(contentContainer.getTransforms()).isEmpty();
  }

  @Test
  public void headersRightIfSideIsRight(FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.RIGHT));

    assertThat(headerContainer.getLayoutX()).isEqualTo(
        viewGroup.getWidth() - viewGroup.snappedRightInset() - headerContainer.getHeight());
    assertThat(headerContainer.getLayoutY()).isEqualTo(
        viewGroup.snappedTopInset() - headerContainer.getHeight());
    assertThat(headerContainer.getWidth()).isEqualTo(
        viewGroup.getHeight() - viewGroup.snappedTopInset() - viewGroup.snappedBottomInset());
    assertThat(headerContainer.getHeight()).isGreaterThan(0);
    assertThat(headerContainer.getTransforms().toString()).isEqualTo(
        List.of(new Rotate(90, 0, headerContainer.getHeight())).toString());

    assertThat(contentContainer.getLayoutX()).isEqualTo(viewGroup.snappedLeftInset());
    assertThat(contentContainer.getLayoutY()).isEqualTo(viewGroup.snappedTopInset());
    assertThat(contentContainer.getWidth()).isEqualTo(
        viewGroup.getWidth() - viewGroup.snappedLeftInset() - viewGroup.snappedRightInset()
            - headerContainer.getHeight());
    assertThat(contentContainer.getHeight()).isEqualTo(
        viewGroup.getHeight() - viewGroup.snappedTopInset() - viewGroup.snappedBottomInset());
    assertThat(contentContainer.getTransforms()).isEmpty();
  }

  @Test
  public void headersBottomIfSideIsBottom(FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.BOTTOM));

    assertThat(headerContainer.getLayoutX()).isEqualTo(viewGroup.snappedLeftInset());
    assertThat(headerContainer.getLayoutY()).isEqualTo(
        viewGroup.getHeight() - viewGroup.snappedBottomInset() - viewGroup.snappedTopInset()
            - headerContainer.getHeight());
    assertThat(headerContainer.getWidth()).isEqualTo(
        viewGroup.getWidth() - viewGroup.snappedLeftInset() - viewGroup.snappedRightInset());
    assertThat(headerContainer.getHeight()).isGreaterThan(0);
    assertThat(headerContainer.getTransforms()).isEqualTo(List.of());

    assertThat(contentContainer.getLayoutX()).isEqualTo(viewGroup.snappedLeftInset());
    assertThat(contentContainer.getLayoutY()).isEqualTo(viewGroup.snappedTopInset());
    assertThat(contentContainer.getWidth()).isEqualTo(
        viewGroup.getWidth() - viewGroup.snappedLeftInset() - viewGroup.snappedRightInset());
    assertThat(contentContainer.getHeight()).isEqualTo(
        viewGroup.getHeight() - viewGroup.snappedTopInset() - viewGroup.snappedBottomInset()
            - headerContainer.getHeight());
    assertThat(contentContainer.getTransforms()).isEmpty();
  }

  @Test
  public void headersLeftIfSideIsLeft(FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.LEFT));

    assertThat(headerContainer.getLayoutX()).isEqualTo(
        viewGroup.snappedLeftInset() + headerContainer.getHeight());
    assertThat(headerContainer.getLayoutY()).isEqualTo(
        viewGroup.snappedTopInset() + viewGroup.getHeight() - headerContainer.getHeight());
    assertThat(headerContainer.getWidth()).isEqualTo(
        viewGroup.getHeight() - viewGroup.snappedTopInset() - viewGroup.snappedBottomInset());
    assertThat(headerContainer.getHeight()).isGreaterThan(0);
    assertThat(headerContainer.getTransforms().toString()).isEqualTo(
        List.of(new Rotate(-90, 0, headerContainer.getHeight())).toString());

    assertThat(contentContainer.getLayoutX()).isEqualTo(
        viewGroup.snappedLeftInset() + headerContainer.getHeight());
    assertThat(contentContainer.getLayoutY()).isEqualTo(viewGroup.snappedTopInset());
    assertThat(contentContainer.getWidth()).isEqualTo(
        viewGroup.getWidth() - viewGroup.snappedLeftInset() - viewGroup.snappedRightInset()
            - headerContainer.getHeight());
    assertThat(contentContainer.getHeight()).isEqualTo(
        viewGroup.getHeight() - viewGroup.snappedTopInset() - viewGroup.snappedBottomInset());
    assertThat(contentContainer.getTransforms()).isEmpty();
  }

  @Test
  public void findDropTarget_returns() {
    DragAndDropContext context = mock(DragAndDropContext.class);
    ViewGroup source = new ViewGroup();
    when(context.getDragSource()).thenReturn(source);

    Point2D pos = this.viewGroup.localToParent(
        this.headerContainer.getTabContainer().localToParent(new Point2D(5, 5)));

    DropTarget target = skin.findDropTarget(context, pos).orElseThrow();
    assertThat(target).isInstanceOf(DropTarget.MoveToGroup.class);
  }
}
