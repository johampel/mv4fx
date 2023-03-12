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


import de.hipphampel.mv4fx.view.ViewGroup.ViewSelectorControls;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(ApplicationExtension.class)
public class DemoApp {

  static final String CSS_URL = Objects.requireNonNull(DemoApp.class.getResource("/demo.css"))
      .toExternalForm();

  static final AtomicInteger counter = new AtomicInteger();

  @Test
  void test(FxRobot robot) {
    robot.interact(() -> {
      Stage newStage = new Stage();

      View view = new DemoView();
      view.setTabLabel("View #" + counter.incrementAndGet());
      ViewGroup group = new ViewGroup();
      group.addAndSelectView(view);
      ViewGroupContainer container = new ViewGroupContainer();
      container.setLeftTop(group);
      newStage.setScene(new Scene(container));
      newStage.showAndWait();
    });
  }

  static class DemoView extends View {

    private ComboBox<Side> groupSideComboBox;
    private Spinner<Number> groupMinTabWidthSpinner;
    private Spinner<Number> groupMaxTabWidthSpinner;
    private ComboBox<ViewSelectorControls> groupViewSelectorControlsComboBox;

    private CheckBox groupLeftTopAreaCheckBox;
    private CheckBox groupRightBottomAreaCheckBox;
    private CheckBox groupChangeGroupCheckBox;
    private CheckBox groupNewWindowCheckBox;
    private final BooleanProperty confirmClose;
    private CheckBox groupDropSideSplitTopCheckBox;
    private CheckBox groupDropSideSplitRightCheckBox;
    private CheckBox groupDropSideSplitBottomCheckBox;
    private CheckBox groupDropSideSplitLeftCheckBox;
    private TextField groupDragTagsTextField;
    private TextField groupDropTagsTextField;
    private final ChangeListener<Set<DropTargetType>> groupDropTargetTypesChangeListener;
    private final ChangeListener<Set<Side>> groupDropSplitSidesChangeListener;
    private final ChangeListener<Callback<Side, Node>> groupLeftAreaChangeListener;
    private final ChangeListener<Callback<Side, Node>> groupRightAreaChangeListener;
    private final ChangeListener<Set<String>> groupDragTagsListener;
    private final ChangeListener<Set<String>> groupDropTagsListener;

    public DemoView() {
      this.confirmClose = new SimpleBooleanProperty();
      this.groupDropTargetTypesChangeListener = (i, ov, nv) -> {
        groupChangeGroupCheckBox.setSelected(nv.contains(DropTargetType.CHANGE_GROUP));
        groupNewWindowCheckBox.setSelected(nv.contains(DropTargetType.NEW_WINDOW));
      };
      this.groupDropSplitSidesChangeListener = (i, ov, nv) -> {
        groupDropSideSplitTopCheckBox.setSelected(nv.contains(Side.TOP));
        groupDropSideSplitRightCheckBox.setSelected(nv.contains(Side.RIGHT));
        groupDropSideSplitBottomCheckBox.setSelected(nv.contains(Side.BOTTOM));
        groupDropSideSplitLeftCheckBox.setSelected(nv.contains(Side.LEFT));
      };
      this.groupLeftAreaChangeListener = (i, ov, nv) -> groupLeftTopAreaCheckBox.setSelected(nv != null);
      this.groupRightAreaChangeListener = (i, ov, nv) -> groupRightBottomAreaCheckBox.setSelected(nv != null);
      this.groupDragTagsListener = (i, ov, nv) -> {
        groupDragTagsTextField.setText(String.join(",", nv));
      };
      this.groupDropTagsListener = (i, ov, nv) -> {
        groupDropTagsTextField.setText(String.join(",", nv));
      };
      setTabLabel("DemoView");
      setTabTooltip(new Tooltip("Controls the properties of this ViewGroup"));
      setContent(createContent());
      viewGroupProperty().addListener((ignore, oldGroup, newGroup) -> onGroupChanged(oldGroup, newGroup));
    }

    @Override
    public boolean canClose() {
      if (confirmClose.get()) {
        Alert alert = new Alert(AlertType.CONFIRMATION, "Really close this view?", ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().orElse(ButtonType.YES).equals(ButtonType.YES);
      }
      return true;
    }

    private Node createContent() {

      GridPane content = new GridPane();

      // Common part

      Label descriptionLabel = new Label("""
          This is a View. Views are similar to Tabs of a TabPane. Instead of a TabPane,
          Views are hosted in ViewGroups.  On the left side, you may control the 
          ViewGroup settings, on the right side the View settings. You may drag and 
          drop a View where ever you want. Please add some Views to play around,
          """);
      content.add(descriptionLabel, 0, 0, 5, 1);

      Button addViewButton = new Button("Create new view");
      addViewButton.setTooltip(new Tooltip("Creates a new view in this ViewGroup"));
      addViewButton.setOnAction(evt -> {
        DemoView view = new DemoView();
        view.setTabLabel("View #" + counter.incrementAndGet());
        getViewGroup().addAndSelectView(view);
      });
      content.add(addViewButton, 0, 1, 5, 1);

      content.add(new Label("   "), 2, 2);

      // View Group settings

      Label groupSettingsLabel = new Label("ViewGroup settings");
      groupSettingsLabel.getStyleClass().add("caption");
      content.add(groupSettingsLabel, 0, 2, 2, 1);

      Label groupSideLabel = new Label("Side:");
      content.add(groupSideLabel, 0, 3);
      groupSideComboBox = new ComboBox<>();
      groupSideComboBox.setTooltip(new Tooltip("Specifies on which side to display the tabs"));
      groupSideComboBox.getItems().add(Side.LEFT);
      groupSideComboBox.getItems().add(Side.RIGHT);
      groupSideComboBox.getItems().add(Side.BOTTOM);
      groupSideComboBox.getItems().add(Side.TOP);
      content.add(groupSideComboBox, 1, 3);

      Label groupMinTabWidthLabel = new Label("Min. tab width:");
      content.add(groupMinTabWidthLabel, 0, 4);
      groupMinTabWidthSpinner = new Spinner<>(-1.0, 1000, -1);
      groupMinTabWidthSpinner.setTooltip(new Tooltip("Minimum width of a tab in the tab area"));
      groupMinTabWidthSpinner.setEditable(true);
      content.add(groupMinTabWidthSpinner, 1, 4);

      Label groupMaxTabWidthLabel = new Label("Max. tab width:");
      content.add(groupMaxTabWidthLabel, 0, 5);
      groupMaxTabWidthSpinner = new Spinner<>(-1.0, 1000, -1);
      groupMaxTabWidthSpinner.setTooltip(new Tooltip("Maximum width of a tab in the tab area"));
      groupMaxTabWidthSpinner.setEditable(true);
      content.add(groupMaxTabWidthSpinner, 1, 5);

      Label groupViewSelectorLabel = new Label("View selector:");
      content.add(groupViewSelectorLabel, 0, 6);
      groupViewSelectorControlsComboBox = new ComboBox<>();
      groupViewSelectorControlsComboBox.setTooltip(new Tooltip("Controls to show when more tabs are present than can made visible"));
      groupViewSelectorControlsComboBox.getItems().add(ViewSelectorControls.ARROWS);
      groupViewSelectorControlsComboBox.getItems().add(ViewSelectorControls.DROPDOWN);
      groupViewSelectorControlsComboBox.getItems().add(ViewSelectorControls.BOTH);
      content.add(groupViewSelectorControlsComboBox, 1, 6);

      groupLeftTopAreaCheckBox = new CheckBox("Show left/top header area component");
      groupLeftTopAreaCheckBox.setTooltip(new Tooltip("Show in the left or top header area a component"));
      groupLeftTopAreaCheckBox.selectedProperty().addListener((i, ov, nv) -> {
        if (getViewGroup() == null) {
          return;
        }
        if (nv) {
          Label label = new Label();
          getViewGroup().viewsProperty().addListener((InvalidationListener) observable -> label.setText(
              " " + (getViewGroup() == null ? 0 : getViewGroup().getViews().size()) + " Views "));
          label.setText(" " + getViewGroup().getViews().size() + " Views ");
          getViewGroup().setLeftTopHeaderArea(side -> label);
        } else {
          getViewGroup().setLeftTopHeaderArea(null);
        }
      });
      content.add(groupLeftTopAreaCheckBox, 0, 7, 2, 1);

      groupRightBottomAreaCheckBox = new CheckBox("Show right/bottom area component");
      groupRightBottomAreaCheckBox.setTooltip(new Tooltip("Show in the right or bottom header area a component"));
      groupRightBottomAreaCheckBox.selectedProperty().addListener((i, ov, nv) -> {
        if (getViewGroup() == null) {
          return;
        }
        if (nv) {
          Button button = new Button("Count groups");
          button.setOnAction(evt -> {
            Alert alert = new Alert(AlertType.INFORMATION, "There are " + ViewManager.getAllViewGroups().count() + " groups",
                ButtonType.OK);
            alert.showAndWait();
          });
          getViewGroup().setRightBottomHeaderArea(side -> button);
        } else {
          getViewGroup().setRightBottomHeaderArea(null);
        }
      });
      content.add(groupRightBottomAreaCheckBox, 0, 8, 2, 1);

      groupChangeGroupCheckBox = new CheckBox("Drag to another ViewGroup allowed");
      groupChangeGroupCheckBox.setTooltip(new Tooltip("If checked, all Views of this can be move to a different ViewGroup via drag&drop"));
      groupChangeGroupCheckBox.selectedProperty().addListener((i, ov, nv) ->
          getViewGroup().setDropTargetTypes(addOrRemove(getViewGroup().getDropTargetTypes(), DropTargetType.CHANGE_GROUP, nv)));
      content.add(groupChangeGroupCheckBox, 0, 9, 2, 1);

      groupNewWindowCheckBox = new CheckBox("Drag to create new Window allowed");
      groupNewWindowCheckBox.setTooltip(
          new Tooltip("If checked, all Views of this can be moved outside a window to create a new one via drag&drop"));
      groupNewWindowCheckBox.selectedProperty().addListener((i, ov, nv) ->
          getViewGroup().setDropTargetTypes(addOrRemove(getViewGroup().getDropTargetTypes(), DropTargetType.NEW_WINDOW, nv)));
      content.add(groupNewWindowCheckBox, 0, 10, 2, 1);

      groupDropSideSplitTopCheckBox = new CheckBox("Drop to top half possible");
      groupDropSideSplitTopCheckBox.setTooltip(
          new Tooltip("If checked, objects can be drop in the top area to split this view"));
      groupDropSideSplitTopCheckBox.selectedProperty()
          .addListener((i, ov, nv) -> getViewGroup().setDropSplitSides(addOrRemove(getViewGroup().getDropSplitSides(), Side.TOP, nv)));
      content.add(groupDropSideSplitTopCheckBox, 0, 11, 2, 1);

      groupDropSideSplitRightCheckBox = new CheckBox("Drop to right half possible");
      groupDropSideSplitRightCheckBox.setTooltip(
          new Tooltip("If checked, objects can be drop in the right area to split this view"));
      groupDropSideSplitRightCheckBox.selectedProperty()
          .addListener((i, ov, nv) -> getViewGroup().setDropSplitSides(addOrRemove(getViewGroup().getDropSplitSides(), Side.RIGHT, nv)));
      content.add(groupDropSideSplitRightCheckBox, 0, 12, 2, 1);

      groupDropSideSplitBottomCheckBox = new CheckBox("Drop to bottom half possible");
      groupDropSideSplitBottomCheckBox.setTooltip(
          new Tooltip("If checked, objects can be drop in the bottom area to split this view"));
      groupDropSideSplitBottomCheckBox.selectedProperty()
          .addListener((i, ov, nv) -> getViewGroup().setDropSplitSides(addOrRemove(getViewGroup().getDropSplitSides(), Side.BOTTOM, nv)));
      content.add(groupDropSideSplitBottomCheckBox, 0, 13, 2, 1);

      groupDropSideSplitLeftCheckBox = new CheckBox("Drop to left half possible");
      groupDropSideSplitLeftCheckBox.setTooltip(
          new Tooltip("If checked, objects can be drop in the left area to split this view"));
      groupDropSideSplitLeftCheckBox.selectedProperty()
          .addListener((i, ov, nv) -> getViewGroup().setDropSplitSides(addOrRemove(getViewGroup().getDropSplitSides(), Side.LEFT, nv)));
      content.add(groupDropSideSplitLeftCheckBox, 0, 14, 2, 1);

      Label groupDragTagsLabel = new Label("Drag tags:");
      content.add(groupDragTagsLabel, 0, 15);
      groupDragTagsTextField = new TextField();
      groupDragTagsTextField.textProperty()
          .addListener(
              (i, ov, nv) -> getViewGroup().setDragTags(Arrays.stream(nv.split(",")).map(String::trim).collect(Collectors.toSet())));
      content.add(groupDragTagsTextField, 1, 15);

      Label groupDropTagsLabel = new Label("Drop tags:");
      content.add(groupDropTagsLabel, 0, 16);
      groupDropTagsTextField = new TextField();
      groupDropTagsTextField.textProperty()
          .addListener(
              (i, ov, nv) -> getViewGroup().setDropTags(Arrays.stream(nv.split(",")).map(String::trim).collect(Collectors.toSet())));
      content.add(groupDropTagsTextField, 1, 16);

      // View settings

      Label viewSettingsLabel = new Label("View settings");
      viewSettingsLabel.getStyleClass().add("caption");
      content.add(viewSettingsLabel, 3, 2, 2, 1);

      Label viewTabLabelLabel = new Label("Tab label:");
      content.add(viewTabLabelLabel, 3, 3);
      TextField viewTabLabelTextField = new TextField();
      viewTabLabelTextField.textProperty().bindBidirectional(tabLabelProperty());
      viewTabLabelTextField.setTooltip(new Tooltip("Sets the label of the tab"));
      content.add(viewTabLabelTextField, 4, 3);

      Label viewTabTooltipLabel = new Label("Tab tooltip:");
      content.add(viewTabTooltipLabel, 3, 4);
      TextField viewTabTooltipTextField = new TextField();
      viewTabTooltipTextField.textProperty().addListener((ign, ov, nv) -> tabTooltipProperty().set(nv == null ? null : new Tooltip(nv)));
      viewTabTooltipTextField.setTooltip(new Tooltip("Sets the tooltip of the tab"));
      content.add(viewTabTooltipTextField, 4, 4);

      CheckBox viewTabContextMenuCheckBox = new CheckBox("Context menu");
      viewTabContextMenuCheckBox.setTooltip(new Tooltip("If checked, provide a context menu for the view tab"));
      viewTabContextMenuCheckBox.selectedProperty().addListener((i, ov, nv) -> {
        if (!nv) {
          setTabContextMenu(null);
          return;
        }
        ContextMenu menu = new ContextMenu();
        MenuItem close = new MenuItem("Close tab");
        close.setOnAction(evt -> {
          if (canClose()) {
            close();
          }
        });
        menu.getItems().add(close);
        setTabContextMenu(menu);
      });
      content.add(viewTabContextMenuCheckBox, 3, 5, 2, 1);
      Label viewCloseActionLabel = new Label("Close action:");
      content.add(viewCloseActionLabel, 3, 6);
      ComboBox<TabActionVisibility> viewCloseActionComboBox = new ComboBox<>();
      viewCloseActionComboBox.setTooltip(new Tooltip("Specifies when the close button in the tab is visible"));
      viewCloseActionComboBox.getItems().add(TabActionVisibility.SELECTED);
      viewCloseActionComboBox.getItems().add(TabActionVisibility.ALWAYS);
      viewCloseActionComboBox.getItems().add(TabActionVisibility.MOUSE_OVER);
      viewCloseActionComboBox.getItems().add(TabActionVisibility.NEVER);
      viewCloseActionComboBox.valueProperty().bindBidirectional(tabCloseActionVisibilityProperty());
      content.add(viewCloseActionComboBox, 4, 6);

      CheckBox viewConfirmCloseComboBox = new CheckBox("Confirm close");
      viewConfirmCloseComboBox.setTooltip(new Tooltip("Get confirmation before close this view"));
      viewConfirmCloseComboBox.selectedProperty().bindBidirectional(confirmClose);
      content.add(viewConfirmCloseComboBox, 3, 7, 2, 1);

      Label viewMaximizeActionLabel = new Label("Maximize action:");
      content.add(viewMaximizeActionLabel, 3, 8);
      ComboBox<TabActionVisibility> viewMaximizeActionComboBox = new ComboBox<>();
      viewMaximizeActionComboBox.setTooltip(new Tooltip("Specifies when the maximize button in the tab is visible"));
      viewMaximizeActionComboBox.getItems().add(TabActionVisibility.SELECTED);
      viewMaximizeActionComboBox.getItems().add(TabActionVisibility.ALWAYS);
      viewMaximizeActionComboBox.getItems().add(TabActionVisibility.MOUSE_OVER);
      viewMaximizeActionComboBox.getItems().add(TabActionVisibility.NEVER);
      viewMaximizeActionComboBox.valueProperty().bindBidirectional(tabMaximizeActionVisibilityProperty());
      content.add(viewMaximizeActionComboBox, 4, 8);

      CheckBox viewMaximizableCheckBox = new CheckBox("Can be maximize");
      viewMaximizableCheckBox.setTooltip(new Tooltip("Specifies, whether the view can be maximized"));
      viewMaximizableCheckBox.selectedProperty().bindBidirectional(maximizableProperty());
      content.add(viewMaximizableCheckBox, 3, 9, 2, 1);

      Button viewRestoreButton = new Button("Restore");
      viewRestoreButton.setTooltip(new Tooltip("Restores the view if maximized"));
      maximizedProperty().addListener((ign, ov, nv) -> viewRestoreButton.setDisable(!nv));
      viewRestoreButton.setOnAction(evt -> setMaximized(false));
      viewRestoreButton.setDisable(true);
      content.add(viewRestoreButton, 3, 10, 2, 1);

      CheckBox viewReorderCheckBox = new CheckBox("Drag to reorder allowed");
      viewReorderCheckBox.setTooltip(new Tooltip("If checked, View order in the ViewGroup can be changed via drag&drop"));
      viewReorderCheckBox.setSelected(getDropTargetTypes().contains(DropTargetType.REORDER));
      viewReorderCheckBox.selectedProperty().addListener((i, ov, nv) ->
          setDropTargetTypes(addOrRemove(getDropTargetTypes(), DropTargetType.REORDER, nv)));
      content.add(viewReorderCheckBox, 3, 11, 2, 1);

      CheckBox viewChangeGroupCheckBox = new CheckBox("Drag to another ViewGroup allowed");
      viewChangeGroupCheckBox.setTooltip(new Tooltip("If checked, View can be move to a different ViewGroup via drag&drop"));
      viewChangeGroupCheckBox.setSelected(getDropTargetTypes().contains(DropTargetType.CHANGE_GROUP));
      viewChangeGroupCheckBox.selectedProperty().addListener((i, ov, nv) ->
          setDropTargetTypes(addOrRemove(getDropTargetTypes(), DropTargetType.CHANGE_GROUP, nv)));
      content.add(viewChangeGroupCheckBox, 3, 12, 2, 1);

      CheckBox viewNewWindowCheckBox = new CheckBox("Drag to create new Window allowed");
      viewNewWindowCheckBox.setTooltip(new Tooltip("If checked, View can be moved outside a window to create a new one via drag&drop"));
      viewNewWindowCheckBox.setSelected(getDropTargetTypes().contains(DropTargetType.NEW_WINDOW));
      viewNewWindowCheckBox.selectedProperty().addListener((i, ov, nv) ->
          setDropTargetTypes(addOrRemove(getDropTargetTypes(), DropTargetType.NEW_WINDOW, nv)));
      content.add(viewNewWindowCheckBox, 3, 13, 2, 1);

      Label viewDragTagsLabel = new Label("Drag tags:");
      content.add(viewDragTagsLabel, 3, 14);
      TextField viewDragTagsTextField = new TextField(String.join(",", getDragTags()));
      viewDragTagsTextField.textProperty()
          .addListener((i, ov, nv) -> setDragTags(Arrays.stream(nv.split(",")).map(String::trim).collect(Collectors.toSet())));
      content.add(viewDragTagsTextField, 4, 14);

      ScrollPane scrollPane = new ScrollPane();
      scrollPane.setContent(content);
      scrollPane.getStylesheets().add(CSS_URL);
      scrollPane.getStyleClass().add("sample-view");
      return scrollPane;
    }

    private void onGroupChanged(ViewGroup oldGroup, ViewGroup newGroup) {
      if (oldGroup != null) {
        oldGroup.sideProperty().unbindBidirectional(groupSideComboBox.valueProperty());
        oldGroup.tabMinWidthProperty().unbindBidirectional(groupMinTabWidthSpinner.getValueFactory().valueProperty());
        oldGroup.tabMaxWidthProperty().unbindBidirectional(groupMaxTabWidthSpinner.getValueFactory().valueProperty());
        oldGroup.viewSelectorControlsProperty().unbindBidirectional(groupViewSelectorControlsComboBox.valueProperty());
        oldGroup.leftTopHeaderAreaProperty().removeListener(groupLeftAreaChangeListener);
        oldGroup.rightBottomHeaderAreaProperty().removeListener(groupRightAreaChangeListener);
        oldGroup.dropTargetTypesProperty().removeListener(groupDropTargetTypesChangeListener);
        oldGroup.dropSplitSidesProperty().removeListener(groupDropSplitSidesChangeListener);
        oldGroup.dragTagsProperty().removeListener(groupDragTagsListener);
        oldGroup.dropTagsProperty().removeListener(groupDropTagsListener);
      }
      if (newGroup != null) {
        groupSideComboBox.setValue(newGroup.getSide());
        newGroup.sideProperty().bindBidirectional(groupSideComboBox.valueProperty());
        groupMinTabWidthSpinner.getValueFactory().setValue(newGroup.getTabMinWidth());
        newGroup.tabMinWidthProperty().bindBidirectional(groupMinTabWidthSpinner.getValueFactory().valueProperty());
        groupMaxTabWidthSpinner.getValueFactory().setValue(newGroup.getTabMaxWidth());
        newGroup.tabMaxWidthProperty().bindBidirectional(groupMaxTabWidthSpinner.getValueFactory().valueProperty());
        groupViewSelectorControlsComboBox.setValue(newGroup.getViewSelectorControls());
        newGroup.viewSelectorControlsProperty().bindBidirectional(groupViewSelectorControlsComboBox.valueProperty());
        groupLeftTopAreaCheckBox.setSelected(newGroup.getLeftTopHeaderArea() != null);
        newGroup.leftTopHeaderAreaProperty().addListener(groupLeftAreaChangeListener);
        groupRightBottomAreaCheckBox.setSelected(newGroup.getRightBottomHeaderArea() != null);
        newGroup.rightBottomHeaderAreaProperty().addListener(groupRightAreaChangeListener);
        groupChangeGroupCheckBox.setSelected(newGroup.getDropTargetTypes().contains(DropTargetType.CHANGE_GROUP));
        groupNewWindowCheckBox.setSelected(newGroup.getDropTargetTypes().contains(DropTargetType.NEW_WINDOW));
        newGroup.dropTargetTypesProperty().addListener(groupDropTargetTypesChangeListener);
        groupDropSideSplitTopCheckBox.setSelected(newGroup.getDropSplitSides().contains(Side.TOP));
        groupDropSideSplitRightCheckBox.setSelected(newGroup.getDropSplitSides().contains(Side.RIGHT));
        groupDropSideSplitBottomCheckBox.setSelected(newGroup.getDropSplitSides().contains(Side.BOTTOM));
        groupDropSideSplitLeftCheckBox.setSelected(newGroup.getDropSplitSides().contains(Side.LEFT));
        newGroup.dropSplitSidesProperty().addListener(groupDropSplitSidesChangeListener);
        groupDragTagsTextField.setText(String.join(",", newGroup.getDragTags()));
        newGroup.dragTagsProperty().addListener(groupDragTagsListener);
        groupDropTagsTextField.setText(String.join(",", newGroup.getDropTags()));
        newGroup.dropTagsProperty().addListener(groupDropTagsListener);
      }
    }

    private static <T> Set<T> addOrRemove(Set<T> source, T value, boolean add) {
      Set<T> copy = new HashSet<>(source);
      if (add) {
        copy.add(value);
      } else {
        copy.remove(value);
      }
      return copy;
    }
  }
}
