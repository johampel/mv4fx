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

import de.hipphampel.mv4fx.view.View;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.ViewGroup.ViewSelectorControls;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.HeaderContainer;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.ViewSelector;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupSkin_HeaderContainerTest {

  private ViewGroup viewGroup;
  private HeaderContainer headerContainer;

  @Start
  public void start(Stage stage) {
    viewGroup = new ViewGroup();
    viewGroup.setSkin(new ViewGroupSkin(viewGroup));
    viewGroup.setViewSelectorControls(ViewSelectorControls.BOTH);
    stage.setScene(new Scene(viewGroup));
    stage.setHeight(400);
    stage.setWidth(400);
    stage.show();
    headerContainer = ((ViewGroupSkin) viewGroup.getSkin()).getHeaderContainer();
  }

  @Test
  public void ctor_setCorrectStyleAndTabContainer(FxRobot robot) {
    assertThat(headerContainer.getTabContainer()).isNotNull();
    assertThat(headerContainer.getStyleClass()).contains("header");
  }

  @Test
  public void leftTopArea_isAutomaticallyUpdated(FxRobot robot) {
    Label leftTop = new Label("LeftTop");
    assertThat(headerContainer.getChildren()).containsExactlyInAnyOrder(
        headerContainer.getTabContainer());

    robot.interact(() -> viewGroup.setLeftTopHeaderArea(side -> leftTop));
    assertThat(headerContainer.getChildren()).containsExactlyInAnyOrder(
        headerContainer.getTabContainer(),
        leftTop);

    robot.interact(() -> viewGroup.setLeftTopHeaderArea(null));
    assertThat(headerContainer.getChildren()).containsExactlyInAnyOrder(
        headerContainer.getTabContainer());
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void leftTopArea_isShownLeftIfSideIsNotLeft(String side, FxRobot robot) {
    Label leftTop = new Label("LeftTop");
    robot.interact(() -> viewGroup.setLeftTopHeaderArea(ignore -> leftTop));
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));

    assertThat(leftTop.getLayoutX()).isEqualTo(headerContainer.snappedLeftInset());
  }

  @Test
  public void leftTopArea_isShownRightIfSideIsLeft(FxRobot robot) {
    Label leftTop = new Label("LeftTop");
    robot.interact(() -> viewGroup.setLeftTopHeaderArea(ignore -> leftTop));
    robot.interact(() -> viewGroup.setSide(Side.LEFT));

    assertThat(leftTop.getLayoutX() + leftTop.getWidth()).isEqualTo(
        headerContainer.getWidth() - headerContainer.snappedRightInset());
  }

  @Test
  public void rightBottomArea_isAutomaticallyUpdated(FxRobot robot) {
    Label rightBottom = new Label("LeftTop");
    assertThat(headerContainer.getChildren()).containsExactlyInAnyOrder(
        headerContainer.getTabContainer());

    robot.interact(() -> viewGroup.setRightBottomHeaderArea(side -> rightBottom));
    assertThat(headerContainer.getChildren()).containsExactlyInAnyOrder(
        headerContainer.getTabContainer(),
        rightBottom);

    robot.interact(() -> viewGroup.setRightBottomHeaderArea(null));
    assertThat(headerContainer.getChildren()).containsExactlyInAnyOrder(
        headerContainer.getTabContainer());
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void rightBottomArea_isShownRightIfSideIsNotLeft(String side, FxRobot robot) {
    Label rightBottom = new Label("LeftTop");
    robot.interact(() -> viewGroup.setRightBottomHeaderArea(ignore -> rightBottom));
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));

    assertThat(rightBottom.getLayoutX() + rightBottom.getWidth()).isEqualTo(
        headerContainer.getWidth() - headerContainer.snappedRightInset());
  }

  @Test
  public void rightBottomArea_isShownLeftIfSideIsLeft(FxRobot robot) {
    Label rightBottom = new Label("LeftTop");
    robot.interact(() -> viewGroup.setRightBottomHeaderArea(ignore -> rightBottom));
    robot.interact(() -> viewGroup.setSide(Side.LEFT));

    assertThat(rightBottom.getLayoutX()).isEqualTo(headerContainer.snappedLeftInset());
  }

  @Test
  public void dispose_removesAllListeners(FxRobot robot) {
    viewGroup.setTabMinWidth(190); // Space for 2 views
    ViewSelector viewSelector = headerContainer.getTabContainer().getViewSelector();
    Node selectPrevBtn = viewSelector.getChildren().get(0);
    robot.interact(() -> viewGroup.setSide(Side.TOP));
    List<View> views = IntStream.range(0, 3).boxed()
        .map(i -> {
          View view = new View();
          view.setTabLabel("View #" + i);
          return view;
        })
        .collect(Collectors.toList());
    robot.interact(() -> viewGroup.getViews().setAll(views));
    robot.interact(() -> viewGroup.selectView(views.get(0)));

    headerContainer.dispose();
    Label label = new Label("label");

    robot.interact(() -> viewGroup.selectView(views.get(1)));
    assertThat(selectPrevBtn.isDisabled()).isTrue();

    robot.interact(() -> viewGroup.setRightBottomHeaderArea(side -> label));
    assertThat(headerContainer.getChildren()).containsExactlyInAnyOrder(
        headerContainer.getTabContainer());

    robot.interact(() -> viewGroup.setLeftTopHeaderArea(side -> label));
    assertThat(headerContainer.getChildren()).containsExactlyInAnyOrder(
        headerContainer.getTabContainer());

  }
}
