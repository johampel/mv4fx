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
import de.hipphampel.mv4fx.view.View;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.ViewGroup.ViewSelectorControls;
import de.hipphampel.mv4fx.view.ViewOrGroup.DropTargetType;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.TabContainer;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.TabControl;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin.ViewSelector;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
public class ViewGroupSkin_TabContainerTest {

  private ViewGroup viewGroup;
  private TabContainer tabContainer;

  @Start
  public void start(Stage stage) {
    viewGroup = new ViewGroup();
    viewGroup.setSkin(new ViewGroupSkin(viewGroup));
    viewGroup.setViewSelectorControls(ViewSelectorControls.BOTH);
    stage.setScene(new Scene(viewGroup));
    stage.setHeight(400);
    stage.setWidth(400);
    stage.show();
    tabContainer = ((ViewGroupSkin) viewGroup.getSkin()).getHeaderContainer().getTabContainer();
  }

  @Test
  public void ctor_setTheTabContainerStyle() {
    assertThat(tabContainer.getStyleClass()).contains("tab-container");
  }

  @Test
  public void addView_addsATabControlForTheGivenView(FxRobot robot) {
    assertThat(tabContainer.getChildren()).hasSize(2); // ViewSelector + Drop box

    View view1 = new View();
    view1.setTabLabel("View1");
    robot.interact(() -> tabContainer.addView(0, view1));
    assertThat(tabContainer.getChildren()).hasSize(3);
    TabControl tabControl = (TabControl) tabContainer.getChildren().get(0);
    assertThat(tabControl.getChildren()).isNotEmpty();
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("View1");

    View view2 = new View();
    view2.setTabLabel("View2");
    robot.interact(() -> tabContainer.addView(0, view2));
    assertThat(tabContainer.getChildren()).hasSize(4);
    tabControl = (TabControl) tabContainer.getChildren().get(0);
    assertThat(tabControl.getChildren()).isNotEmpty();
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("View2");

    View view3 = new View();
    view3.setTabLabel("View3");
    robot.interact(() -> tabContainer.addView(1, view3));
    assertThat(tabContainer.getChildren()).hasSize(5);
    tabControl = (TabControl) tabContainer.getChildren().get(1);
    assertThat(tabControl.getChildren()).isNotEmpty();
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("View3");
  }

  @Test
  public void removeView_removesATabControlForTheGivenView(FxRobot robot) {
    View view1 = new View();
    view1.setTabLabel("View1");
    View view2 = new View();
    view2.setTabLabel("View2");
    View view3 = new View();
    view3.setTabLabel("View3");
    robot.interact(() -> tabContainer.addView(0, view1));
    robot.interact(() -> tabContainer.addView(1, view2));
    robot.interact(() -> tabContainer.addView(2, view3));

    assertThat(tabContainer.getChildren()).hasSize(5);
    assertThat(((Label) ((TabControl) tabContainer.getChildren().get(0)).getChildren()
        .get(0)).getText()).isEqualTo("View1");
    assertThat(((Label) ((TabControl) tabContainer.getChildren().get(1)).getChildren()
        .get(0)).getText()).isEqualTo("View2");
    assertThat(((Label) ((TabControl) tabContainer.getChildren().get(2)).getChildren()
        .get(0)).getText()).isEqualTo("View3");

    robot.interact(() -> tabContainer.removeView(view2));
    assertThat(tabContainer.getChildren()).hasSize(4);
    assertThat(((Label) ((TabControl) tabContainer.getChildren().get(0)).getChildren()
        .get(0)).getText()).isEqualTo("View1");
    assertThat(((Label) ((TabControl) tabContainer.getChildren().get(1)).getChildren()
        .get(0)).getText()).isEqualTo("View3");
  }

  @Test
  public void removeView_callDisposeOnRemovedTabControl(FxRobot robot) {
    View view = new View();
    view.setTabLabel("Original Name");
    robot.interact(() -> tabContainer.addView(0, view));

    assertThat(tabContainer.getChildren()).hasSize(3);
    TabControl tabControl = (TabControl) tabContainer.getChildren().get(0);
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("Original Name");

    robot.interact(() -> view.setTabLabel("New Name"));
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("New Name");

    // Now remove
    robot.interact(() -> tabContainer.removeView(view));
    robot.interact(() -> view.setTabLabel("Alternative Name"));
    assertThat(((Label) tabControl.getChildren().get(0)).getText()).isEqualTo("New Name");
  }

  @Test
  public void selectView_setsTheSelectedPseudoClassOnTheTabControl(FxRobot robot) {
    View view1 = new View();
    view1.setTabLabel("View1");
    robot.interact(() -> tabContainer.addView(0, view1));
    TabControl tabControl1 = (TabControl) tabContainer.getChildren().get(0);
    View view2 = new View();
    view2.setTabLabel("View2");
    robot.interact(() -> tabContainer.addView(1, view2));
    TabControl tabControl2 = (TabControl) tabContainer.getChildren().get(1);

    assertThat(tabControl1.getPseudoClassStates()).doesNotContain(
        ViewGroupSkin.PSEUDO_CLASS_SELECTED);
    assertThat(tabControl2.getPseudoClassStates()).doesNotContain(
        ViewGroupSkin.PSEUDO_CLASS_SELECTED);

    robot.interact(() -> tabContainer.selectView(view1));
    assertThat(tabControl1.getPseudoClassStates()).contains(ViewGroupSkin.PSEUDO_CLASS_SELECTED);
    assertThat(tabControl2.getPseudoClassStates()).doesNotContain(
        ViewGroupSkin.PSEUDO_CLASS_SELECTED);

    robot.interact(() -> tabContainer.selectView(view2));
    assertThat(tabControl1.getPseudoClassStates()).doesNotContain(
        ViewGroupSkin.PSEUDO_CLASS_SELECTED);
    assertThat(tabControl2.getPseudoClassStates()).contains(ViewGroupSkin.PSEUDO_CLASS_SELECTED);

    robot.interact(() -> tabContainer.selectView(null));
    assertThat(tabControl1.getPseudoClassStates()).doesNotContain(
        ViewGroupSkin.PSEUDO_CLASS_SELECTED);
    assertThat(tabControl2.getPseudoClassStates()).doesNotContain(
        ViewGroupSkin.PSEUDO_CLASS_SELECTED);
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void selectView_scrollTheViewToVisibleAreaIfSideIsNotLeft(String side, FxRobot robot) {
    viewGroup.setTabMinWidth(190); // Space for 2 views
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));
    List<View> views = IntStream.range(1, 11).boxed()
        .map(i -> {
          View view = new View();
          view.setTabLabel("View #" + i);
          return view;
        })
        .collect(Collectors.toList());
    robot.interact(() -> viewGroup.getViews().setAll(views));
    TabControl tabControl0 = (TabControl) tabContainer.getChildren().get(0);
    TabControl tabControl4 = (TabControl) tabContainer.getChildren().get(4);
    TabControl tabControl9 = (TabControl) tabContainer.getChildren().get(9);

    // Select first view
    robot.interact(() -> viewGroup.selectView(views.get(0)));
    assertThat(tabControl0.getLayoutX()).isGreaterThan(0);
    assertThat(tabControl0.getLayoutX() + tabControl0.getWidth()).isLessThan(
        tabContainer.getWidth());
    assertThat(tabControl4.getLayoutX()).isGreaterThan(tabContainer.getWidth());
    assertThat(tabControl9.getLayoutX()).isGreaterThan(tabContainer.getWidth());

    // Select middle view
    robot.interact(() -> viewGroup.selectView(views.get(4)));
    assertThat(tabControl0.getLayoutX() + tabControl0.getWidth()).isLessThan(0);
    assertThat(tabControl4.getLayoutX()).isGreaterThan(0);
    assertThat(tabControl4.getLayoutX() + tabControl4.getWidth()).isLessThan(
        tabContainer.getWidth());
    assertThat(tabControl9.getLayoutX()).isGreaterThan(tabContainer.getWidth());

    // Select last view
    robot.interact(() -> viewGroup.selectView(views.get(9)));
    assertThat(tabControl0.getLayoutX() + tabControl0.getWidth()).isLessThan(0);
    assertThat(tabControl4.getLayoutX() + tabControl0.getWidth()).isLessThan(0);
    assertThat(tabControl9.getLayoutX()).isGreaterThan(0);
    assertThat(tabControl9.getLayoutX() + tabControl9.getWidth()).isLessThan(
        tabContainer.getWidth());
  }

  @Test
  public void selectView_scrollTheViewToVisibleAreaIfSideIsLeft(FxRobot robot) {
    viewGroup.setTabMinWidth(190); // Space for 2 views
    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    List<View> views = IntStream.range(1, 11).boxed()
        .map(i -> {
          View view = new View();
          view.setTabLabel("View #" + i);
          return view;
        })
        .collect(Collectors.toList());
    robot.interact(() -> viewGroup.getViews().setAll(views));
    TabControl tabControl0 = (TabControl) tabContainer.getChildren().get(0);
    TabControl tabControl4 = (TabControl) tabContainer.getChildren().get(4);
    TabControl tabControl9 = (TabControl) tabContainer.getChildren().get(9);

    // Select first view
    robot.interact(() -> viewGroup.selectView(views.get(0)));
    assertThat(tabControl0.getLayoutX()).isGreaterThan(0);
    assertThat(tabControl0.getLayoutX() + tabControl0.getWidth()).isLessThan(
        tabContainer.getWidth());
    assertThat(tabControl4.getLayoutX() + tabControl4.getWidth()).isLessThan(0);
    assertThat(tabControl9.getLayoutX() + tabControl9.getWidth()).isLessThan(0);

    // Select middle view
    robot.interact(() -> viewGroup.selectView(views.get(4)));
    assertThat(tabControl0.getLayoutX()).isGreaterThan(tabContainer.getWidth());
    assertThat(tabControl4.getLayoutX()).isGreaterThan(0);
    assertThat(tabControl4.getLayoutX() + tabControl4.getWidth()).isLessThan(
        tabContainer.getWidth());
    assertThat(tabControl9.getLayoutX() + tabControl9.getWidth()).isLessThan(0);

    // Select last view
    robot.interact(() -> viewGroup.selectView(views.get(9)));
    assertThat(tabControl0.getLayoutX()).isGreaterThan(tabContainer.getWidth());
    assertThat(tabControl4.getLayoutX()).isGreaterThan(tabContainer.getWidth());
    assertThat(tabControl9.getLayoutX()).isGreaterThan(0);
    assertThat(tabControl9.getLayoutX() + tabControl9.getWidth()).isLessThan(
        tabContainer.getWidth());
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void layoutChildren_layoutChildrenInOrderInOneLineIfNotLeftSide(String side,
      FxRobot robot) {
    View view1 = new View();
    view1.setTabLabel("View1");
    View view2 = new View();
    view2.setTabLabel("View2");
    View view3 = new View();
    view3.setTabLabel("View3");
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));
    robot.interact(() -> tabContainer.addView(0, view1));
    robot.interact(() -> tabContainer.addView(1, view2));
    robot.interact(() -> tabContainer.addView(2, view3));

    assertThat(tabContainer.getChildren()).hasSize(5); // +2 for view selector and drop box
    TabControl tabControl1 = (TabControl) tabContainer.getChildren().get(0);
    TabControl tabControl2 = (TabControl) tabContainer.getChildren().get(1);
    TabControl tabControl3 = (TabControl) tabContainer.getChildren().get(2);

    assertThat(tabControl1.getWidth()).isGreaterThan(0);
    assertThat(tabControl2.getWidth()).isGreaterThan(0);
    assertThat(tabControl3.getWidth()).isGreaterThan(0);
    assertThat(tabControl1.getHeight()).isGreaterThan(0);
    assertThat(tabControl2.getHeight()).isEqualTo(tabControl1.getHeight());
    assertThat(tabControl3.getHeight()).isEqualTo(tabControl1.getHeight());
    assertThat(tabControl2.getLayoutY()).isEqualTo(tabControl1.getLayoutY());
    assertThat(tabControl3.getLayoutY()).isEqualTo(tabControl1.getLayoutY());
    assertThat(tabControl1.getLayoutX()).isEqualTo(tabContainer.snappedLeftInset());
    assertThat(tabControl2.getLayoutX()).isEqualTo(
        tabControl1.getLayoutX() + tabControl1.getWidth());
    assertThat(tabControl3.getLayoutX()).isEqualTo(
        tabControl2.getLayoutX() + tabControl2.getWidth());
  }

  @Test
  public void layoutChildren_layoutChildrenInReverseOrderInOneLineIfLeftSide(FxRobot robot) {
    View view1 = new View();
    view1.setTabLabel("View1");
    View view2 = new View();
    view2.setTabLabel("View2");
    View view3 = new View();
    view3.setTabLabel("View3");
    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    robot.interact(() -> tabContainer.addView(0, view1));
    robot.interact(() -> tabContainer.addView(1, view2));
    robot.interact(() -> tabContainer.addView(2, view3));

    assertThat(tabContainer.getChildren()).hasSize(5); // +2 for view selector and drop box
    TabControl tabControl1 = (TabControl) tabContainer.getChildren().get(0);
    TabControl tabControl2 = (TabControl) tabContainer.getChildren().get(1);
    TabControl tabControl3 = (TabControl) tabContainer.getChildren().get(2);

    assertThat(tabControl1.getWidth()).isGreaterThan(0);
    assertThat(tabControl2.getWidth()).isGreaterThan(0);
    assertThat(tabControl3.getWidth()).isGreaterThan(0);
    assertThat(tabControl1.getHeight()).isGreaterThan(0);
    assertThat(tabControl2.getHeight()).isEqualTo(tabControl1.getHeight());
    assertThat(tabControl3.getHeight()).isEqualTo(tabControl1.getHeight());
    assertThat(tabControl2.getLayoutY()).isEqualTo(tabControl1.getLayoutY());
    assertThat(tabControl3.getLayoutY()).isEqualTo(tabControl1.getLayoutY());
    assertThat(tabControl1.getLayoutX()).isEqualTo(
        tabContainer.getWidth() - tabControl1.getWidth() - tabContainer.snappedRightInset());
    assertThat(tabControl2.getLayoutX()).isEqualTo(
        tabControl1.getLayoutX() - tabControl2.getWidth());
    assertThat(tabControl3.getLayoutX()).isEqualTo(
        tabControl2.getLayoutX() - tabControl3.getWidth());
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM", "LEFT"})
  public void viewSelectorIsVisibleOnlyIfNotAllTabsFit(String side, FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));
    ViewSelector viewSelector = tabContainer.getViewSelector();
    viewGroup.setTabMinWidth(190); // Space for 2 views

    assertThat(viewSelector.isVisible()).isFalse();

    View view1 = new View();
    view1.setTabLabel("View1");
    robot.interact(() -> viewGroup.addView(view1));
    assertThat(viewSelector.isVisible()).isFalse();

    View view2 = new View();
    view2.setTabLabel("View2");
    robot.interact(() -> viewGroup.addView(view2));
    assertThat(viewSelector.isVisible()).isFalse();

    View view3 = new View();
    view3.setTabLabel("View3");
    robot.interact(() -> viewGroup.addView(view3));
    assertThat(viewSelector.isVisible()).isTrue();

  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM"})
  public void viewSelectoIsLocatedRightIfSideIsNotLeft(String side, FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.valueOf(side)));
    ViewSelector viewSelector = tabContainer.getViewSelector();
    viewGroup.setTabMinWidth(190); // Space for 2 views
    IntStream.range(1, 4).boxed()
        .forEach(i -> {
          View view = new View();
          view.setTabLabel("View #" + i);
          robot.interact(() -> viewGroup.addView(view));
        });
    assertThat(viewSelector.isVisible()).isTrue();
    assertThat(viewSelector.getLayoutX() + viewSelector.getWidth()).isEqualTo(
        tabContainer.getWidth() - tabContainer.snappedRightInset());
    assertThat(viewSelector.getLayoutX()).isGreaterThan(tabContainer.snappedLeftInset());
  }

  @Test
  public void viewSelectorIsLocatedLeftIfSideIsLeft(FxRobot robot) {
    robot.interact(() -> viewGroup.setSide(Side.LEFT));
    ViewSelector viewSelector = tabContainer.getViewSelector();
    viewGroup.setTabMinWidth(190); // Space for 2 views
    IntStream.range(1, 4).boxed()
        .forEach(i -> {
          View view = new View();
          view.setTabLabel("View #" + i);
          robot.interact(() -> viewGroup.addView(view));
        });
    assertThat(viewSelector.isVisible()).isTrue();
    assertThat(viewSelector.getLayoutX()).isEqualTo(tabContainer.snappedLeftInset());
    assertThat(viewSelector.getLayoutX() + viewSelector.getWidth()).isLessThan(
        tabContainer.getWidth() - tabContainer.snappedRightInset());
  }

  @Test
  public void dispose_removesAllListenersOnViewSelectorAndTabs(FxRobot robot) {
    viewGroup.setTabMinWidth(190); // Space for 2 views
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

    // Check current values
    ViewSelector viewSelector = tabContainer.getViewSelector();
    Node selectPrevBtn = viewSelector.getChildren().get(0);
    TabControl tabControl = (TabControl) tabContainer.getChildren().get(0);
    Label tabControlLabel = (Label) tabControl.getChildren().get(0);

    assertThat(selectPrevBtn.isDisabled()).isTrue();
    assertThat(tabControlLabel.getText()).isEqualTo("View #0");

    // Check, whether events current propagated
    robot.interact(() -> views.get(0).setTabLabel("New Label"));
    robot.interact(() -> viewGroup.selectView(views.get(1)));
    assertThat(selectPrevBtn.isDisabled()).isFalse();
    assertThat(tabControlLabel.getText()).isEqualTo("New Label");

    // Dispose
    tabContainer.dispose();

    // Now redo
    robot.interact(() -> views.get(0).setTabLabel("View #0"));
    robot.interact(() -> viewGroup.selectView(views.get(0)));
    assertThat(selectPrevBtn.isDisabled()).isFalse();
    assertThat(tabControlLabel.getText()).isEqualTo("New Label");
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOP", "RIGHT", "BOTTOM", "LEFT"})
  public void findDropTarget_returnsEmptyIfPositionNotInBounds(Side side, FxRobot robot) {
    // Arrange
    robot.interact(() -> viewGroup.setSide(side));
    DragAndDropContext context = mock(DragAndDropContext.class);

    assertThat(tabContainer.findDropTarget(context, new Point2D(-1, -1))).isEmpty();
    assertThat(tabContainer.findDropTarget(context,
        new Point2D(tabContainer.getWidth() + 1, tabContainer.getHeight() + 1))).isEmpty();
  }

  @Test
  public void findDropTarget_forViewGroup_returnsNoneIfSourceSameAsTarget(FxRobot robot) {
    // Arrange
    DragAndDropContext context = mock(DragAndDropContext.class);
    when(context.getDragSource()).thenReturn(viewGroup);
    Point2D pos = new Point2D(
        tabContainer.getBoundsInLocal().getMinX() + 1,
        tabContainer.getBoundsInLocal().getMinY() + 1);

    // Act and assert
    assertThat(tabContainer.findDropTarget(context, pos)).contains(DropTarget.none());
  }

  @Test
  public void findDropTarget_forViewGroup_returnsNoneIfDropTargetTypeDoesNotContainNewGroup(
      FxRobot robot) {
    // Arrange
    DragAndDropContext context = mock(DragAndDropContext.class);
    ViewGroup source = new ViewGroup();
    source.setDragTags(Set.of());
    source.setDropTargetTypes(Set.of(DropTargetType.REORDER, DropTargetType.NEW_WINDOW)); // cause
    when(context.getDragSource()).thenReturn(source);
    viewGroup.setDropTags(Set.of());
    Point2D pos = new Point2D(
        tabContainer.getBoundsInLocal().getMinX() + 1,
        tabContainer.getBoundsInLocal().getMinY() + 1);

    // Act and assert
    assertThat(tabContainer.findDropTarget(context, pos)).contains(DropTarget.none());
  }

  @Test
  public void findDropTarget_forViewGroup_returnsNoneTagsDontMatch(FxRobot robot) {
    // Arrange
    DragAndDropContext context = mock(DragAndDropContext.class);
    ViewGroup source = new ViewGroup();
    source.setDragTags(Set.of("unexpected")); // cause
    source.setDropTargetTypes(Set.of(DropTargetType.CHANGE_GROUP));
    when(context.getDragSource()).thenReturn(source);
    viewGroup.setDropTags(Set.of());
    Point2D pos = new Point2D(
        tabContainer.getBoundsInLocal().getMinX() + 1,
        tabContainer.getBoundsInLocal().getMinY() + 1);
    assertThat(tabContainer.findDropTarget(context, pos)).contains(DropTarget.none());
  }

  @ParameterizedTest
  @CsvSource({
      "TOP, 3, RIGHT, 4",
      "TOP, 4, LEFT, 4",
      "TOP, 4, RIGHT, 5",
      "TOP, 5, LEFT, 5",
      "LEFT, 3, BOTTOM, 3",
      "LEFT, 4, TOP, 4",
      "LEFT, 4, BOTTOM, 4",
      "LEFT, 5, TOP, 5"
  })
  public void findDropTarget_forViewGroup_returnsMoveToGroupTargetIfValid(Side side,
      int clickView, Side clickSide, int expectedIndex, FxRobot robot) {
    // Arrange
    robot.interact(() -> viewGroup.setViewSelectorControls(ViewSelectorControls.DROPDOWN));
    robot.interact(() -> viewGroup.setTabMinWidth(70));
    robot.interact(() -> viewGroup.setTabMaxWidth(70));
    robot.interact(() -> viewGroup.setSide(side));
    List<View> views = IntStream.range(0, 8).boxed()
        .map(i -> {
          View view = new View();
          view.setTabLabel("#" + i);
          return view;
        })
        .collect(Collectors.toList());
    robot.interact(() -> viewGroup.getViews().setAll(views));
    robot.interact(() -> viewGroup.selectView(views.get(views.size() - 1))); // Scroll correctly
    robot.interact(() -> viewGroup.selectView(views.get(5)));
    DragAndDropContext context = mock(DragAndDropContext.class);
    ViewGroup source = new ViewGroup();
    when(context.getDragSource()).thenReturn(source);

    TabControl clickControl = tabContainer.getTabControls()[clickView];
    Point2D pos = switch (clickSide) {
      case LEFT -> new Point2D(
          clickControl.getBoundsInParent().getMinX() + 1,
          clickControl.getBoundsInParent().getMinY() + clickControl.getHeight() / 2);
      case RIGHT -> new Point2D(
          clickControl.getBoundsInParent().getMaxX() - 1,
          clickControl.getBoundsInParent().getMinY() + clickControl.getHeight() / 2);
      case TOP -> new Point2D(
          clickControl.getBoundsInParent().getMinX() + clickControl.getWidth() / 2,
          clickControl.getBoundsInParent().getMinY() + 1);
      case BOTTOM -> new Point2D(
          clickControl.getBoundsInParent().getMinX() + clickControl.getWidth() / 2,
          clickControl.getBoundsInParent().getMaxY() - 1);
    };

    Optional<DropTarget> target = tabContainer.findDropTarget(context, pos);
    assertThat(target).contains(DropTarget.moveToGroup(viewGroup, expectedIndex));
  }

  @Test
  public void findDropTarget_forViewFromSameGroup_returnsNoneIfDropTargetTypesDoesNotContainReorder(FxRobot robot) {
    // Arrange
    robot.interact(() -> viewGroup.setViewSelectorControls(ViewSelectorControls.DROPDOWN));
    robot.interact(() -> viewGroup.setTabMinWidth(70));
    robot.interact(() -> viewGroup.setTabMaxWidth(70));
    View targetView = new View();
    targetView.setTabLabel("source");
    View sourceView = new View();
    sourceView.setDropTargetTypes(
        Set.of(DropTargetType.NEW_WINDOW, DropTargetType.CHANGE_GROUP)); //cause
    sourceView.setTabLabel("source");
    robot.interact(() -> viewGroup.addView(targetView));
    robot.interact(() -> viewGroup.addView(sourceView));
    DragAndDropContext context = mock(DragAndDropContext.class);
    when(context.getDragSource()).thenReturn(sourceView);
    TabControl clickControl = tabContainer.getTabControls()[0];
    Point2D pos = new Point2D(
        clickControl.getBoundsInParent().getMinX() + 1,
        clickControl.getBoundsInParent().getMinY() + clickControl.getHeight() / 2);

    // Act and assert
    assertThat(tabContainer.findDropTarget(context, pos)).contains(DropTarget.none());
  }

  @ParameterizedTest
  @CsvSource({
      "TOP, 3, RIGHT, 4",
      "TOP, 4, LEFT, 4",
      "TOP, 4, RIGHT, -1",
      "TOP, 5, LEFT, -1",
      "TOP, 5, RIGHT, -1",
      "TOP, 6, LEFT, -1",
      "TOP, 6, RIGHT, 7",
      "TOP, 7, LEFT, 7",
      "TOP, 7, RIGHT, 8",
      "LEFT, 3, BOTTOM, 3",
      "LEFT, 4, TOP, 4",
      "LEFT, 4, BOTTOM, 4",
      "LEFT, 5, TOP, -1"
  })
  public void findDropTarget_forViewFromSameGroup_returnsMoveToGroupTargetIfPositionChanges(Side side,
      int clickView, Side clickSide, int expectedIndex, FxRobot robot) {
    // Arrange
    robot.interact(() -> viewGroup.setViewSelectorControls(ViewSelectorControls.DROPDOWN));
    robot.interact(() -> viewGroup.setTabMinWidth(70));
    robot.interact(() -> viewGroup.setTabMaxWidth(70));
    robot.interact(() -> viewGroup.setSide(side));
    List<View> views = IntStream.range(0, 8).boxed()
        .map(i -> {
          View view = new View();
          view.setTabLabel("#" + i);
          return view;
        })
        .collect(Collectors.toList());
    robot.interact(() -> viewGroup.getViews().setAll(views));
    robot.interact(() -> viewGroup.selectView(views.get(views.size() - 1))); // Scroll correctly
    robot.interact(() -> viewGroup.selectView(views.get(5)));
    DragAndDropContext context = mock(DragAndDropContext.class);
    View source = viewGroup.getViews().get(5);
    when(context.getDragSource()).thenReturn(source);

    TabControl clickControl = tabContainer.getTabControls()[clickView];
    Point2D pos = switch (clickSide) {
      case LEFT -> new Point2D(
          clickControl.getBoundsInParent().getMinX() + 1,
          clickControl.getBoundsInParent().getMinY() + clickControl.getHeight() / 2);
      case RIGHT -> new Point2D(
          clickControl.getBoundsInParent().getMaxX() - 1,
          clickControl.getBoundsInParent().getMinY() + clickControl.getHeight() / 2);
      case TOP -> new Point2D(
          clickControl.getBoundsInParent().getMinX() + clickControl.getWidth() / 2,
          clickControl.getBoundsInParent().getMinY() + 1);
      case BOTTOM -> new Point2D(
          clickControl.getBoundsInParent().getMinX() + clickControl.getWidth() / 2,
          clickControl.getBoundsInParent().getMaxY() - 1);
    };

    Optional<DropTarget> target = tabContainer.findDropTarget(context, pos);
    if (expectedIndex != -1) {
      assertThat(target).contains(DropTarget.moveToGroup(viewGroup, expectedIndex));
    } else {
      assertThat(target).contains(DropTarget.none());
    }
  }
}
