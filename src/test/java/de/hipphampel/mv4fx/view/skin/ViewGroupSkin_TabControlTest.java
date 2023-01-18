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
import de.hipphampel.mv4fx.view.View.TabActionVisibility;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.TabControl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javafx.css.PseudoClass;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupSkin_TabControlTest {

  private ViewGroup viewGroup;
  private View view;
  private TabControl tabControl;

  @Start
  public void start(Stage stage) {
    this.viewGroup = new ViewGroup();
    stage.setScene(new Scene(viewGroup));
    stage.setHeight(400);
    stage.setWidth(400);
    stage.show();
  }

  @BeforeEach
  public void beforeEach(FxRobot robot) {
    ViewGroupSkin skin = (ViewGroupSkin) viewGroup.getSkin();
    this.view = new View();
    view.setTabLabel("Test");
    robot.interact(() -> viewGroup.getViews().add(view));
    robot.interact(() -> viewGroup.getViews().add(new View()));
    this.tabControl = (TabControl) skin.getHeaderContainer().getTabContainer().getChildren().get(0);
  }

  @Test
  public void ctor_setTheTabStyle() {
    assertThat(tabControl.getStyleClass()).contains("tab");
  }

  @Test
  public void onMouseClick_changesTheActiveView(FxRobot robot) {
    robot.interact(() -> viewGroup.getSelection().select(viewGroup.getViews().get(1)));
    assertThat(viewGroup.getSelection().getSelectedItem()).isSameAs(viewGroup.getViews().get(1));

    robot.clickOn(tabControl);
    assertThat(viewGroup.getSelection().getSelectedItem()).isSameAs(view);
  }

  @Test
  public void dispose_removesAllListeners(FxRobot robot) {
    Node tabGraphic = new Line(0, 0, 1, 1);
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabNode(null));
    robot.interact(() -> view.setTabLabel("TabLabel"));
    robot.interact(() -> view.setTabGraphic(tabGraphic));
    robot.interact(() -> view.setTabStyle("-fx-background: yellow;"));

    // Assert current state
    assertThat(tabControl.getChildren()).hasSize(1);
    assertThat(tabControl.getChildren().get(0)).isInstanceOf(Label.class);
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("TabLabel");
    assertThat(((Label) tabControl.getChildren().get(0)).getGraphic()).isSameAs(tabGraphic);
    assertThat(tabControl.getChildren().get(0).getStyle()).isEqualTo("-fx-background: yellow;");

    tabControl.dispose();

    // Call the listeners (implicit)
    robot.interact(() -> view.setTabLabel("New Label"));
    robot.interact(() -> view.setTabGraphic(new Line(0, 0, 2, 2)));
    robot.interact(() -> view.setTabStyle("-fx-background: black;"));
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.ALWAYS));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.ALWAYS));
    robot.interact(() -> view.setTabNode(side -> new ColorPicker()));
    robot.interact(() -> viewGroup.setSide(Side.BOTTOM));

    // Should be same as the original state
    assertThat(tabControl.getChildren()).hasSize(1);
    assertThat(tabControl.getChildren().get(0)).isInstanceOf(Label.class);
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("TabLabel");
    assertThat(((Label) tabControl.getChildren().get(0)).getGraphic()).isSameAs(tabGraphic);
    assertThat(tabControl.getChildren().get(0).getStyle()).isEqualTo("-fx-background: yellow;");
  }


  @Test
  public void onTabControlChanged_usesResultOfTabNodeCallbackIfPresent(FxRobot robot) {
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.NEVER));
    Button node = new Button("NODE");
    robot.interact(() -> view.setTabNode(side -> {
      node.setText(side + "");
      return node;
    }));

    // Side top
    robot.interact(() -> viewGroup.setSide(Side.TOP));
    robot.interact(() -> view.setTabLabel("ignore"));
    robot.interact(() -> view.setTabGraphic(new Line(0, 0, 10, 10)));
    robot.interact(() -> view.setTabStyle("-fx-background: black;"));
    assertThat(tabControl.getChildren()).containsExactly(node);
    assertThat(node.getText()).isEqualTo(Side.TOP + "");
    assertThat(node.getGraphic()).isNull();
    assertThat(node.getStyle()).isEmpty();

    // Side right
    robot.interact(() -> viewGroup.setSide(Side.RIGHT));
    robot.interact(() -> view.setTabLabel("ignore"));
    robot.interact(() -> view.setTabGraphic(new Line(0, 0, 10, 10)));
    robot.interact(() -> view.setTabStyle("-fx-background: black;"));
    assertThat(tabControl.getChildren()).containsExactly(node);
    assertThat(node.getText()).isEqualTo(Side.RIGHT + "");
    assertThat(node.getGraphic()).isNull();
    assertThat(node.getStyle()).isEmpty();

    // Side bottom
    robot.interact(() -> viewGroup.setSide(Side.BOTTOM));
    robot.interact(() -> view.setTabLabel("ignore"));
    robot.interact(() -> view.setTabGraphic(new Line(0, 0, 10, 10)));
    robot.interact(() -> view.setTabStyle("-fx-background: black;"));
    assertThat(tabControl.getChildren()).containsExactly(node);
    assertThat(node.getText()).isEqualTo(Side.BOTTOM + "");
    assertThat(node.getGraphic()).isNull();
    assertThat(node.getStyle()).isEmpty();

    // Side left
    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    robot.interact(() -> view.setTabLabel("ignore"));
    robot.interact(() -> view.setTabGraphic(new Line(0, 0, 10, 10)));
    robot.interact(() -> view.setTabStyle("-fx-background: black;"));
    assertThat(tabControl.getChildren()).containsExactly(node);
    assertThat(node.getText()).isEqualTo(Side.LEFT + "");
    assertThat(node.getGraphic()).isNull();
    assertThat(node.getStyle()).isEmpty();
  }

  @Test
  public void onTabControlChanged_geenratesLabelifTabNodeCallbackIfNotPresent(FxRobot robot) {
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabNode(null));

    // TabLabel
    robot.interact(() -> view.setTabLabel("TabLabel"));
    robot.interact(() -> view.setTabGraphic(null));
    robot.interact(() -> view.setTabStyle(null));
    assertThat(tabControl.getChildren()).hasSize(1);
    assertThat(tabControl.getChildren().get(0)).isInstanceOf(Label.class);
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("TabLabel");
    assertThat(((Label) tabControl.getChildren().get(0)).getGraphic()).isNull();
    assertThat(tabControl.getChildren().get(0).getStyle()).isEmpty();

    // TabGraphic
    Node tabGraphic = new Line(0, 0, 1, 1);
    robot.interact(() -> view.setTabGraphic(tabGraphic));
    assertThat(tabControl.getChildren()).hasSize(1);
    assertThat(tabControl.getChildren().get(0)).isInstanceOf(Label.class);
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("TabLabel");
    assertThat(((Label) tabControl.getChildren().get(0)).getGraphic()).isSameAs(tabGraphic);
    assertThat(tabControl.getChildren().get(0).getStyle()).isEmpty();

    // TabStyle
    robot.interact(() -> view.setTabStyle("-fx-background: yellow;"));
    assertThat(tabControl.getChildren()).hasSize(1);
    assertThat(tabControl.getChildren().get(0)).isInstanceOf(Label.class);
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("TabLabel");
    assertThat(((Label) tabControl.getChildren().get(0)).getGraphic()).isSameAs(tabGraphic);
    assertThat(tabControl.getChildren().get(0).getStyle()).isEqualTo("-fx-background: yellow;");
  }

  @Test
  public void onTabActionVisibilityChanged_showsButtonsAccordingToCorrespondingViewProperties(
      FxRobot robot) {
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.NEVER));

    List<Node> children;

    // No controls
    children = tabControl.getChildren();
    assertThat(children).hasSize(1);

    // Close button
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.ALWAYS));
    children = tabControl.getChildren();
    assertThat(children).hasSize(2);
    assertThat(children.get(1)).isInstanceOf(Button.class);
    assertThat(children.get(1).getStyleClass()).contains("tab-button", "close-button");

    // Maximize button
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.ALWAYS));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.NEVER));
    children = tabControl.getChildren();
    assertThat(children).hasSize(2);
    assertThat(children.get(1)).isInstanceOf(Button.class);
    assertThat(children.get(1).getStyleClass()).contains("tab-button", "maximize-button");

    // Close+Maximize button
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.ALWAYS));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.ALWAYS));
    children = tabControl.getChildren();
    assertThat(children).hasSize(3);
    assertThat(children.get(1)).isInstanceOf(Button.class);
    assertThat(children.get(1).getStyleClass()).contains("tab-button", "maximize-button");
    assertThat(children.get(2)).isInstanceOf(Button.class);
    assertThat(children.get(2).getStyleClass()).contains("tab-button", "close-button");
  }

  @Test
  public void updateChildren_containsOnlyLabelIfNoMaximizeOrCloseButtonAndNoTabNode(FxRobot robot) {
    robot.interact(() -> view.setTabNode(null));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabLabel("MY LABEL"));

    List<Node> children = tabControl.getChildren();
    assertThat(children).hasSize(1);
    assertThat(children.get(0)).isInstanceOf(Label.class);
    assertThat(((Label) children.get(0)).getText()).isEqualTo("MY LABEL");
  }

  @Test
  public void updateChildren_containsConrolAndCloseButtonIfCloseIsVisible(FxRobot robot) {
    Button node = new Button("NODE");
    robot.interact(() -> view.setTabNode(side -> node));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.ALWAYS));
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.NEVER));

    List<Node> children = tabControl.getChildren();
    assertThat(children).hasSize(2);
    assertThat(children.get(0)).isSameAs(node);
    assertThat(children.get(1)).isInstanceOf(Button.class);
    assertThat(children.get(1).getStyleClass()).contains("tab-button", "close-button");
  }

  @Test
  public void updateChildren_containsConrolAndMaximizeButtonIfMaximizeIsVisible(FxRobot robot) {
    Button node = new Button("NODE");
    robot.interact(() -> view.setTabNode(side -> node));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.ALWAYS));

    List<Node> children = tabControl.getChildren();
    assertThat(children).hasSize(2);
    assertThat(children.get(0)).isSameAs(node);
    assertThat(children.get(1)).isInstanceOf(Button.class);
    assertThat(children.get(1).getStyleClass()).contains("tab-button", "maximize-button");
  }

  @Test
  public void updateChildren_containsConrolAndCloseAndMaximizeButtonIfMaximizeAndCloseIsVisible(
      FxRobot robot) {
    Button node = new Button("NODE");
    robot.interact(() -> view.setTabNode(side -> node));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.ALWAYS));
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.ALWAYS));

    List<Node> children = tabControl.getChildren();
    assertThat(children).hasSize(3);
    assertThat(children.get(0)).isSameAs(node);
    assertThat(children.get(1)).isInstanceOf(Button.class);
    assertThat(children.get(1).getStyleClass()).contains("tab-button", "maximize-button");
    assertThat(children.get(2)).isInstanceOf(Button.class);
    assertThat(children.get(2).getStyleClass()).contains("tab-button", "close-button");
  }

  @Test
  public void updateChildren_containsOnlyNotIfNoMaximizeOrCloseButtonAndTabNode(FxRobot robot) {
    Button node = new Button("NODE");
    robot.interact(() -> view.setTabNode(side -> node));
    robot.interact(() -> view.setTabCloseActionVisibility(TabActionVisibility.NEVER));
    robot.interact(() -> view.setTabMaximizeActionVisibility(TabActionVisibility.NEVER));

    List<Node> children = tabControl.getChildren();
    assertThat(children).containsExactly(node);
  }

  @Test
  public void createCloseButton_createsAButtonWithCorrectStyles(FxRobot robot) {
    Button button = tabControl.createCloseButton();
    assertThat(button).isNotNull();
    assertThat(button.getStyleClass()).contains("tab-button", "close-button");
  }

  @Test
  public void createMaximizeButton_createsAButtonWithCorrectStyles(FxRobot robot) {
    Button button = tabControl.createMaximizeButton();
    assertThat(button).isNotNull();
    assertThat(button.getStyleClass()).contains("tab-button", "maximize-button");
  }

  @Test
  public void isActionVisible_withAlways_returnsTrueAlways(FxRobot robot) {
    robot.interact((Runnable) this::applyPseudoClasses);
    assertThat(tabControl.isActionVisible(TabActionVisibility.ALWAYS)).isTrue();
    robot.interact(() -> applyPseudoClasses(ViewGroupSkin.PSEUDO_CLASS_SELECTED));
    assertThat(tabControl.isActionVisible(TabActionVisibility.ALWAYS)).isTrue();
    robot.interact(() -> applyPseudoClasses(ViewGroupSkin.PSEUDO_CLASS_HOVER));
    assertThat(tabControl.isActionVisible(TabActionVisibility.ALWAYS)).isTrue();
  }

  @Test
  public void isActionVisible_withSelected_returnsTrueIfViewSelected(FxRobot robot) {
    robot.interact((Runnable) this::applyPseudoClasses);
    assertThat(tabControl.isActionVisible(TabActionVisibility.SELECTED)).isFalse();
    robot.interact(() -> applyPseudoClasses(ViewGroupSkin.PSEUDO_CLASS_SELECTED));
    assertThat(tabControl.isActionVisible(TabActionVisibility.SELECTED)).isTrue();
    robot.interact(() -> applyPseudoClasses(ViewGroupSkin.PSEUDO_CLASS_HOVER));
    assertThat(tabControl.isActionVisible(TabActionVisibility.SELECTED)).isFalse();
  }

  @Test
  public void isActionVisible_withMouseOver_returnsTrueIfHoverPseudoClassPresent(FxRobot robot) {
    robot.interact((Runnable) this::applyPseudoClasses);
    assertThat(tabControl.isActionVisible(TabActionVisibility.MOUSE_OVER)).isFalse();
    robot.interact(() -> applyPseudoClasses(ViewGroupSkin.PSEUDO_CLASS_SELECTED));
    assertThat(tabControl.isActionVisible(TabActionVisibility.MOUSE_OVER)).isFalse();
    robot.interact(() -> applyPseudoClasses(ViewGroupSkin.PSEUDO_CLASS_HOVER));
    assertThat(tabControl.isActionVisible(TabActionVisibility.MOUSE_OVER)).isTrue();
  }

  @Test
  public void isActionVisible_withNever_returnsFalseAlways(FxRobot robot) {
    robot.interact((Runnable) this::applyPseudoClasses);
    assertThat(tabControl.isActionVisible(TabActionVisibility.NEVER)).isFalse();
    robot.interact(() -> applyPseudoClasses(ViewGroupSkin.PSEUDO_CLASS_SELECTED));
    assertThat(tabControl.isActionVisible(TabActionVisibility.NEVER)).isFalse();
    robot.interact(() -> applyPseudoClasses(ViewGroupSkin.PSEUDO_CLASS_HOVER));
    assertThat(tabControl.isActionVisible(TabActionVisibility.NEVER)).isFalse();
  }


  @Test
  public void tabNeverBecomesSmallerThanTabMinWidthOfGroup(FxRobot robot) {
    assertThat(tabControl.getWidth()).isLessThan(100.0);
    robot.interact(() -> viewGroup.setTabMinWidth(100));
    assertThat(tabControl.getWidth()).isEqualTo(100.0);
  }

  @Test
  public void tabNeverBecomesBiggerThanTabMaxWidthOfGroup(FxRobot robot) {
    robot.interact(() -> view.setTabLabel("012345678901234"));
    assertThat(tabControl.getWidth()).isGreaterThan(100.0);
    robot.interact(() -> viewGroup.setTabMaxWidth(100));
    assertThat(tabControl.getWidth()).isEqualTo(100.0);
  }

  private void applyPseudoClasses(PseudoClass... pseudoClasses) {
    Set<PseudoClass> current = new HashSet<>(tabControl.getPseudoClassStates());
    current.forEach(pc -> tabControl.pseudoClassStateChanged(pc, false));
    Stream.of(pseudoClasses).forEach(pc -> tabControl.pseudoClassStateChanged(pc, true));
  }

}
