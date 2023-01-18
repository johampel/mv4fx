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
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.ViewSelector;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupSkin_ViewSelectorTest {

  private ViewGroup viewGroup;
  private ViewSelector selector;

  @Start
  public void start(Stage stage) {
    viewGroup = new ViewGroup();
    viewGroup.setViewSelectorControls(ViewSelectorControls.BOTH);
    viewGroup.setSkin(new ViewGroupSkin(viewGroup));
    for (int i = 0; i < 10; i++) {
      View view = new View();
      view.setTabLabel("View #" + i);
      viewGroup.addView(view);
    }
    stage.setScene(new Scene(viewGroup));
    stage.setHeight(400);
    stage.setWidth(400);
    stage.show();
    selector = ((ViewGroupSkin) viewGroup.getSkin()).getHeaderContainer().getTabContainer()
        .getViewSelector();
  }

  @Test
  public void ctor_setsCorrectStyle(FxRobot robot) {
    assertThat(selector.getStyleClass()).contains("view-selector");
  }

  @Test
  public void hasMatchingControlsAccordingToViewSelectorPropertyControlsProperty(FxRobot robot) {
    robot.interact(() -> viewGroup.setViewSelectorControls(ViewSelectorControls.ARROWS));
    assertThat(robot.lookup(".prev-view").queryAll()).hasSize(1);
    assertThat(robot.lookup(".next-view").queryAll()).hasSize(1);
    assertThat(robot.lookup(".select-view").queryAll()).isEmpty();

    robot.interact(() -> viewGroup.setViewSelectorControls(ViewSelectorControls.BOTH));
    assertThat(robot.lookup(".prev-view").queryAll()).hasSize(1);
    assertThat(robot.lookup(".next-view").queryAll()).hasSize(1);
    assertThat(robot.lookup(".select-view").queryAll()).hasSize(1);

    robot.interact(() -> viewGroup.setViewSelectorControls(ViewSelectorControls.DROPDOWN));
    assertThat(robot.lookup(".prev-view").queryAll()).isEmpty();
    assertThat(robot.lookup(".next-view").queryAll()).isEmpty();
    assertThat(robot.lookup(".select-view").queryAll()).hasSize(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void prevViewControl_isDisabledSelectingFirstTabAndHeaderIsNotLeft(String side,
      FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));
    Node control = robot.lookup(".prev-view").query().getParent();

    robot.interact(() -> viewGroup.getSelection().select(0));
    assertThat(control.isDisabled()).isTrue();
    robot.interact(() -> viewGroup.getSelection().select(1));
    assertThat(control.isDisabled()).isFalse();
  }

  @Test
  public void prevViewControl_isDisabledSelectingLastTabAndHeaderIsLeft(FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    int last = viewGroup.getViews().size() - 1;
    Node control = robot.lookup(".prev-view").query().getParent();

    robot.interact(() -> viewGroup.getSelection().select(last));
    assertThat(control.isDisabled()).isTrue();
    robot.interact(() -> viewGroup.getSelection().select(last - 1));
    assertThat(control.isDisabled()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void prevViewControl_onMouseClick_selectsPrevTabTabAndHeaderIsNotLeft(String side,
      FxRobot robot) {
    int last = viewGroup.getViews().size() - 1;
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));
    robot.interact(() -> viewGroup.getSelection().select(last));

    Node control = robot.lookup(".prev-view").query().getParent();

    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(last);
    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(last - 1);
    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(last - 2);
  }

  @Test
  public void prevViewControl_onMouseClick_selectsNextTabTabAndHeaderIsLeft(FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    robot.interact(() -> viewGroup.getSelection().select(0));

    Node control = robot.lookup(".prev-view").query().getParent();

    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(0);
    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(1);
    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void nextViewControl_isDisabledSelectingLastTabAndHeaderIsNotLeft(String side,
      FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));
    Node control = robot.lookup(".next-view").query().getParent();
    int last = viewGroup.getViews().size() - 1;

    robot.interact(() -> viewGroup.getSelection().select(last));
    assertThat(control.isDisabled()).isTrue();
    robot.interact(() -> viewGroup.getSelection().select(last - 1));
    assertThat(control.isDisabled()).isFalse();
  }

  @Test
  public void nextViewControl_isDisabledSelectingFirstTabAndHeaderIsLeft(FxRobot robot) {
    Node control;

    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    control = robot.lookup(".next-view").query().getParent();
    robot.interact(() -> viewGroup.getSelection().select(0));
    assertThat(control.isDisabled()).isTrue();
    robot.interact(() -> viewGroup.getSelection().select(1));
    assertThat(control.isDisabled()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void nextViewControl_onMouseClick_selectsNextTabAndHeaderIsNotLeft(String side,
      FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));
    robot.interact(() -> viewGroup.getSelection().select(0));

    Node control = robot.lookup(".next-view").query().getParent();

    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(0);
    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(1);
    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(2);
  }

  @Test
  public void nextViewControl_onMouseClick_selectsPrevTabAndHeaderIsLeft(FxRobot robot) {
    int last = viewGroup.getViews().size() - 1;
    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    robot.interact(() -> viewGroup.getSelection().select(last));

    Node control = robot.lookup(".next-view").query().getParent();

    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(last);
    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(last - 1);
    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(last - 2);
  }

  @Test
  public void selectViewControl_onMouseClick_opensAContextMenuToSelectView(FxRobot robot) {
    Node control = robot.lookup(".select-view").query().getParent();

    robot.interact(() -> viewGroup.getSelection().select(0));

    assertThat(selector.menu).isNull();
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(0);

    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(selector.menu).isNotNull();
    assertThat(selector.menu.isShowing()).isTrue();
    assertThat(selector.menu.getItems()).hasSize(viewGroup.getViews().size());

    robot.clickOn(robot.targetWindow(selector.menu).lookup("#view4").queryAs(Node.class));
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(4);
    assertThat(selector.menu).isNull();
  }

  @Test
  public void selectViewControl_onMouseClick_closesAnOpenedContextMenu(FxRobot robot) {
    Node control = robot.lookup(".select-view").query().getParent();

    robot.interact(() -> viewGroup.getSelection().select(0));

    assertThat(selector.menu).isNull();
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(0);

    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(selector.menu).isNotNull();
    assertThat(selector.menu.isShowing()).isTrue();
    assertThat(selector.menu.getItems()).hasSize(viewGroup.getViews().size());

    robot.clickOn(control, MouseButton.PRIMARY);
    assertThat(viewGroup.getSelection().getSelectedIndex()).isEqualTo(0);
    assertThat(selector.menu).isNull();
  }

  @Test
  public void dispose_removesAllListeners(FxRobot robot) {
    Node selectView = robot.lookup(".select-view").query().getParent();
    Node prevView = robot.lookup(".prev-view").query().getParent();
    Node nextView = robot.lookup(".next-view").query().getParent();

    robot.interact(() -> viewGroup.getSelection().select(0));
    assertThat(selector.getChildren()).containsExactly(prevView, nextView, selectView);
    assertThat(prevView.isDisabled()).isTrue();

    selector.dispose();

    robot.interact(() -> viewGroup.getSelection().select(1));
    assertThat(selector.getChildren()).containsExactly(prevView, nextView, selectView);
    assertThat(prevView.isDisabled()).isTrue();

    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    assertThat(selector.getChildren()).containsExactly(prevView, nextView, selectView);
    assertThat(prevView.isDisabled()).isTrue();

    robot.interact(() -> viewGroup.setViewSelectorControls(ViewSelectorControls.DROPDOWN));
    assertThat(selector.getChildren()).containsExactly(prevView, nextView, selectView);
    assertThat(prevView.isDisabled()).isTrue();
  }
}
