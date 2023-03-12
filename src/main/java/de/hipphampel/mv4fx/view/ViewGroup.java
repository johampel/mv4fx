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

import de.hipphampel.mv4fx.utils.EnumSetCssMetaData;
import de.hipphampel.mv4fx.utils.LayoutRequestingStyleableObjectProperty;
import de.hipphampel.mv4fx.utils.StringSetCssMetaData;
import de.hipphampel.mv4fx.utils.StyleableSizeProperty;
import de.hipphampel.mv4fx.view.skin.ViewGroupSkin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;
import javafx.util.Callback;

/**
 * Control that hosts {@link View Views}.
 * <p>
 * A {@code ViewGroup} is best compared with a {@code TabPane} known from the standard Java FX
 * controls. It allows to contain any number of of {@code Views}, whereas one of it is the active
 * one and its content is displayed in the group content area. Beside the content area the
 * {@code ViewGroup} has a tab area, which shows for each view a "tab". Clicking the tab make the
 * corresponding view to the active one.
 * <p>
 * The {@code ViewGroup} provides several CSS styles to customize the behaviour and appearance:
 * <ul>
 *   <li>{@code -mv4fx-side}: defines on which side the header area show appear, possible values are
 *   {@code top}, {@code right}, {@code bottom}, or {@code left}, whereas {@code top} is the
 *   default</li>
 *   <li>{@code -mv4fx-tab-max-width}: defines the maximum size a tab may have, default is -1, which
 *   means "unlimited"</li>
 *   <li>{@code -mv4fx-tab-min-width}: defines the minimum size a tab may have, default is -1, which
 *   means "unlimited"</li>
 *   <li>{@code -mv4fx-view-selector-controls}: defines, which controls to show in case too many
 *   tabs are present, so that the user can use the controls to navigate to the tabs that are not
 *   visible. Possible values are:
 *   <ul>
 *     <li>{@code arrows}: show two arrow buttons, one to navigate to the previous and an other one
 *     tp the next view</li>
 *     <li>{@code dropdown}: show a button that opens a drop down to select a view. The drop down
 *     is realized as a popup windows, which supports scrolling and things like that. This is the
 *     default</li>
 *     <li>{@code both}: show both kind of button mentioned above</li>
 *   </ul>
 *   </li>
 *   <li>{@code -mv4fx-drag-tags}: defines the drag tags, as described in {@link #dragTagsProperty()
 *   dragTagsProperty}</li>
 *   <li>{@code -mv4fx-drop-tags}: defines the drop tags, as described in {@link #dropTagsProperty()
 *   dropTagsProperty}</li>
 *   <li>{@code -mv4fx-drop-target-types}: defines the drop target types, as described in {@link
 *   #dropTargetTypesProperty() dropTargetTypesProperty}</li>
 *   <li>{@code -mv4fx-drop-split-sides}: If set to one or more sides, this group can be split,
 *   whereas splitting is done vertically or horizontally and the current content is placed on one
 *   of the sides specified in this property and in the opposite side the object being dragged is
 *   dropped; when splitting, this group is reparented in a {@link ViewGroupContainer}, so that
 *   the splitting is effectively realized by this new parent container. The default contains all
 *   sides</li>
 * </ul>
 */
public class ViewGroup extends Control implements GroupOrContainer, ViewOrGroup {

  /**
   * Enumeration describing the controls to use for selecting a {@link View}.
   * <p>
   * In case that too many {@code Views} are present, not all of their tabs   fit in the header, so
   * an overflow occurs. This enum describes which controls are present to select a view that has no
   * visible tab
   */
  public enum ViewSelectorControls {
    /* Indicates that two arrow buttons are shown to iterate through the tabs. */
    ARROWS,

    /* Indicates that a button is shown that opens a drop down to select a view. */
    DROPDOWN,

    /**
     * Indicates that both - {@link #ARROWS} and {@link #DROPDOWN} is active.
     */
    BOTH
  }

  // Public constants
  public static final String PROPERTY_SIDE = "side";
  public static final String PROPERTY_VIEWS = "views";
  public static final String PROPERTY_LEFT_TOP_HEADER = "leftTopHeader";
  public static final String PROPERTY_RIGHT_BOTTOM_HEADER = "rightBottomHeader";
  public static final String PROPERTY_TAB_MIN_WIDTH = "tabMinWidth";
  public static final String PROPERTY_TAB_MAX_WIDTH = "tabMaxWidth";
  public static final String PROPERTY_VIEW_SELECTOR_CONTROLS = "viewSelectorControls";
  public static final String PROPERTY_DRAG_TAGS = "dragTags";
  public static final String PROPERTY_DROP_TAGS = "dropTags";
  public static final String PROPERTY_DROP_TARGET_TYPES = "dropTargetTypes";
  public static final String PROPERTY_DROP_SPLIT_SIDES = "dropSplitSides";
  public static final String PROPERTY_DRAGGING = "dragging";
  public static final String PROPERTY_DROP_TARGET = "dropTarget";

  // Style definition stuff
  private static final StyleablePropertyFactory<ViewGroup> FACTORY = new StyleablePropertyFactory<>(
      Control.getClassCssMetaData());
  private static final CssMetaData<ViewGroup, Side> SIDE = FACTORY.createEnumCssMetaData(
      Side.class, "-mv4fx-side", s -> s.side, Side.TOP, false);
  private static final CssMetaData<ViewGroup, Number> TAB_MAX_WIDTH = FACTORY.createSizeCssMetaData(
      "-mv4fx-tab-max-width", s -> s.tabMaxWidth, -1, false);
  private static final CssMetaData<ViewGroup, Number> TAB_MIN_WIDTH = FACTORY.createSizeCssMetaData(
      "-mv4fx-tab-min-width", s -> s.tabMinWidth, -1, false);
  private static final CssMetaData<ViewGroup, ViewSelectorControls> VIEW_SELECTOR_CONTROLS = FACTORY.createEnumCssMetaData(
      ViewSelectorControls.class, "-mv4fx-view-selector-controls", s -> s.viewSelectorControls,
      ViewSelectorControls.DROPDOWN, false);
  private static final CssMetaData<ViewGroup, Set<String>> DRAG_TAGS = new StringSetCssMetaData<>(
      "-mv4fx-drag-tags", s -> s.dragTags, Set.of(), false);
  private static final CssMetaData<ViewGroup, Set<DropTargetType>> DROP_TARGET_TYPES = new EnumSetCssMetaData<>(
      "-mv4fx-drop-target-types", s -> s.dropTargetTypes, DropTargetType.class,
      Set.of(DropTargetType.REORDER, DropTargetType.CHANGE_GROUP,
          DropTargetType.NEW_WINDOW), false);
  private static final CssMetaData<ViewGroup, Set<String>> DROP_TAGS = new StringSetCssMetaData<>(
      "-mv4fx-drop-tags", s -> s.dropTags, Set.of(), false);
  private static final CssMetaData<ViewGroup, Set<Side>> DROP_SPLIT_SIDES = new EnumSetCssMetaData<>(
      "-mv4fx-drop-split-sides", s -> s.dropSplitSides, Side.class,
      Set.of(Side.TOP, Side.RIGHT, Side.BOTTOM, Side.LEFT), true);

  private final static List<CssMetaData<? extends Styleable, ?>> CLASS_CSS_META_DATA;

  static {
    List<CssMetaData<? extends Styleable, ?>> own = new ArrayList<>(Control.getClassCssMetaData());
    own.add(SIDE);
    own.add(TAB_MAX_WIDTH);
    own.add(TAB_MIN_WIDTH);
    own.add(VIEW_SELECTOR_CONTROLS);
    own.add(DRAG_TAGS);
    own.add(DROP_TAGS);
    own.add(DROP_TARGET_TYPES);
    own.add(DROP_SPLIT_SIDES);
    CLASS_CSS_META_DATA = Collections.unmodifiableList(own);
  }

  /**
   * + Gets the CSS meta data of this class.
   *
   * @return CSS meta data.
   */
  public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
    return CLASS_CSS_META_DATA;
  }

  // Properties
  private final ListProperty<View> views;
  private final SelectionModel<View> selection;
  private final ObjectProperty<Callback<Side, Node>> leftTopHeaderArea;
  private final ObjectProperty<Callback<Side, Node>> rightBottomHeaderArea;
  private final StyleableSizeProperty tabMaxWidth;
  private final StyleableSizeProperty tabMinWidth;
  private final LayoutRequestingStyleableObjectProperty<Side> side;
  private final LayoutRequestingStyleableObjectProperty<ViewSelectorControls> viewSelectorControls;
  private final SimpleStyleableObjectProperty<Set<String>> dragTags;
  private final SimpleStyleableObjectProperty<Set<String>> dropTags;
  private final SimpleStyleableObjectProperty<Set<DropTargetType>> dropTargetTypes;
  private final SimpleStyleableObjectProperty<Set<Side>> dropSplitSides;
  private final BooleanProperty dragging;
  private final ObjectProperty<DropTarget> dropTarget;

  /**
   * Constructor.
   */
  public ViewGroup() {
    this.views = new SimpleListProperty<>(this, PROPERTY_VIEWS,
        FXCollections.observableList(new ArrayList<>()));
    this.selection = new SingleSelectionModel<>() {
      @Override
      protected View getModelItem(int index) {
        return views.size() > index && index >= 0 ? views.get(index) : null;
      }

      @Override
      protected int getItemCount() {
        return views.size();
      }
    };
    this.leftTopHeaderArea = new SimpleObjectProperty<>(this, PROPERTY_LEFT_TOP_HEADER);
    this.rightBottomHeaderArea = new SimpleObjectProperty<>(this, PROPERTY_RIGHT_BOTTOM_HEADER);
    this.tabMaxWidth = new StyleableSizeProperty(this, PROPERTY_TAB_MAX_WIDTH, TAB_MAX_WIDTH);
    this.tabMinWidth = new StyleableSizeProperty(this, PROPERTY_TAB_MIN_WIDTH, TAB_MIN_WIDTH);
    this.side = new LayoutRequestingStyleableObjectProperty<>(this, PROPERTY_SIDE, SIDE);
    this.viewSelectorControls = new LayoutRequestingStyleableObjectProperty<>(this,
        PROPERTY_VIEW_SELECTOR_CONTROLS,
        VIEW_SELECTOR_CONTROLS);
    this.dragTags = new SimpleStyleableObjectProperty<>(DRAG_TAGS, this, PROPERTY_DRAG_TAGS,
        Set.of());
    this.dropTargetTypes = new SimpleStyleableObjectProperty<>(DROP_TARGET_TYPES, this,
        PROPERTY_DROP_TARGET_TYPES,
        Set.of(DropTargetType.REORDER, DropTargetType.CHANGE_GROUP, DropTargetType.NEW_WINDOW));
    this.dropTags = new SimpleStyleableObjectProperty<>(DROP_TAGS, this, PROPERTY_DROP_TAGS,
        Set.of());
    this.dropSplitSides = new SimpleStyleableObjectProperty<>(DROP_SPLIT_SIDES, this,
        PROPERTY_DROP_SPLIT_SIDES,
        Set.of(Side.TOP, Side.RIGHT, Side.BOTTOM, Side.LEFT));
    this.dragging = new SimpleBooleanProperty(this, PROPERTY_DRAGGING, false);
    this.dropTarget = new SimpleObjectProperty<>(this, PROPERTY_DROP_TARGET);
    this.views.addListener(this::onViewsChanged);

    this.getStylesheets().add(Constants.CSS_URL);
    this.getStyleClass().add(Constants.CLASS_VIEW_GROUP);
  }

  @Override
  public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
    return CLASS_CSS_META_DATA;
  }

  /**
   * Specifies, whether auto close this group.
   * <p>
   * The return value decides, whether the group is automatically closed/disposed, when the last
   * view of the group is removed, either because the view has closed or moved away.
   * <p>
   * By default, this method returns {@code true}, derived classes might implement more
   * sophisticated logic
   *
   * @return The auto close flag
   */
  public boolean isAutoClose() {
    return true;
  }

  /**
   * Gets the associated {@link SelectionModel}
   *
   * @return The {@code SelectionModel}
   */
  public SelectionModel<View> getSelection() {
    return selection;
  }

  /**
   * Selects the given {@code view}.
   * <p>
   * This is basically a shortcut for {@code getSelection().select(view)}
   *
   * @param view The {@link View}
   */
  public void selectView(View view) {
    selection.select(view);
  }

  /**
   * Convenience getter for the {@code views} property.
   * <p>
   * See documentation of {@link #viewsProperty() views} property for details.
   *
   * @return The current value
   */
  public ObservableList<View> getViews() {
    return views.get();
  }

  /**
   * The {@code views} property.
   * <p>
   * This property contains the list pf the {@link View Views} currently associated with this
   * group.
   *
   * @return The property
   */
  public ListProperty<View> viewsProperty() {
    return views;
  }

  /**
   * Convenience setter for the {@code views} property.
   * <p>
   * See documentation of {@link #viewsProperty() views} property for details.
   *
   * @param views The new value
   */
  public void setViews(ObservableList<View> views) {
    this.views.set(views);
  }

  /**
   * Convenience method to add the given {@code view}.
   * <p>
   * The {@code view} is added as last to the list of views, but not selected.
   *
   * @param view The {@link View} to add.
   * @see #addAndSelectView(View)
   */
  public void addView(View view) {
    addView(getViews().size(), view);
  }

  /**
   * Adds the geiven {@code view} at the specified {@code index}.
   *
   * @param index The index.
   * @param view  The {@code View}
   */
  public void addView(int index, View view) {
    getViews().add(index, view);
  }

  /**
   * Adds and selects the given {@code view}.
   * <p>
   * This first adds {@code view} and then selects it.
   *
   * @param view The {@link View}
   */
  public void addAndSelectView(View view) {
    addView(view);
    selectView(view);
  }

  /**
   * Adds the geiven {@code view} at the specified {@code index} and selects it.
   *
   * @param index The index where to insert.
   * @param view  The {@code View} to add
   */
  public void addAndSelectView(int index, View view) {
    addView(index, view);
    selectView(view);
  }

  /**
   * Removes the given {@code view} and selects the next one.
   *
   * @param view The {@code View} to remove
   */
  public void removeView(View view) {
    int selectedIndex = selection.getSelectedIndex();
    int index = getViews().indexOf(view);
    if (index != -1) {
      getViews().remove(index);
    }
    if (selectedIndex < getViews().size()) {
      selection.select(selectedIndex);
    }
  }

  /**
   * Convenience getter for the {@code side} property.
   * <p>
   * See documentation of {@link #sideProperty() side} property for details.
   *
   * @return The current value
   */
  public Side getSide() {
    return side.get();
  }

  /**
   * The {@code side} property.
   * <p>
   * This property defines on which side the header with the tabs is displayed.
   * <p>
   * This property can be also set via the CSS style {@code -mv4fx-side}
   *
   * @return The property
   */
  public ObjectProperty<Side> sideProperty() {
    return side;
  }

  /**
   * Convenience setter for the {@code side} property.
   * <p>
   * See documentation of {@link #sideProperty() side} property for details.
   *
   * @param side The new value
   */
  public void setSide(Side side) {
    this.side.set(side);
  }

  /**
   * Convenience getter for the {@code leftTopHeaderArea} property.
   * <p>
   * See documentation of {@link #leftTopHeaderAreaProperty() leftTopHeaderArea} property for
   * details. Depending on the value of the side, the content of the header area is rotated
   * accordingly.
   *
   * @return The current value
   */
  public Callback<Side, Node> getLeftTopHeaderArea() {
    return leftTopHeaderArea.get();
  }

  /**
   * The {@code leftTopHeaderArea} property.
   * <p>
   * This property allows to specify a {@link Callback} that produces a {@code Node} that appears
   * either om the left or on the top side of the header area (in case that the
   * {@link #sideProperty() side} is horizontal, it appears at left, otherwise at top).
   * <p>
   * Although the node is rotated along with the header according to the {@code side}, the
   * {@code Callback} is called each time the {@code side} is changed to allow different kind of
   * nodes per side.
   *
   * @return The  {@code Callback} or  {@code null}
   */
  public ObjectProperty<Callback<Side, Node>> leftTopHeaderAreaProperty() {
    return leftTopHeaderArea;
  }

  /**
   * Convenience setter for the {@code leftTopHeaderArea} property.
   * <p>
   * See documentation of {@link #leftTopHeaderAreaProperty() leftTopHeaderArea} property for
   * details.
   *
   * @param leftTopHeaderArea The new value
   */
  public void setLeftTopHeaderArea(Callback<Side, Node> leftTopHeaderArea) {
    this.leftTopHeaderArea.set(leftTopHeaderArea);
  }

  /**
   * Convenience getter for the {@code rightBottomHeaderArea} property.
   * <p>
   * See documentation of {@link #rightBottomHeaderAreaProperty() rightBottomHeaderArea} property
   * for details.
   *
   * @return The current value
   */
  public Callback<Side, Node> getRightBottomHeaderArea() {
    return rightBottomHeaderArea.get();
  }

  /**
   * The {@code rightBottomHeaderArea} property.
   * <p>
   * This property allows to specify a {@link Callback} that produces a {@code Node} that appears
   * either om the right or on the bottom side of the header area (in case that the
   * {@link #sideProperty() side} is horizontal, it appears at right, otherwise at bottom).
   * <p>
   * Although the node is rotated along with the header according to the {@code side}, the
   * {@code Callback} is called each time the {@code side} is changed to allow different kind of
   * nodes per side.
   *
   * @return The  {@code Callback} or  {@code null}
   */
  public ObjectProperty<Callback<Side, Node>> rightBottomHeaderAreaProperty() {
    return rightBottomHeaderArea;
  }

  /**
   * Convenience setter for the {@code rightBottomHeaderArea} property.
   * <p>
   * See documentation of {@link #rightBottomHeaderAreaProperty() rightBottomHeaderArea} property
   * for details.
   *
   * @param rightBottomHeaderArea The new value
   */
  public void setRightBottomHeaderArea(Callback<Side, Node> rightBottomHeaderArea) {
    this.rightBottomHeaderArea.set(rightBottomHeaderArea);
  }

  /**
   * Convenience getter for the {@code tabMaxWidth} property.
   * <p>
   * See documentation of {@link #tabMaxWidthProperty() tabMaxWidth} property for details.
   *
   * @return The current value
   */
  public double getTabMaxWidth() {
    return tabMaxWidth.get();
  }

  /**
   * {@code tabMaxWidth} property.
   * <p>
   * This property controls the maximum width a tab may have. It is guaranteed that a tab never
   * exceeds this limit, independent from its own settings.
   * <p>
   * This property can be also set via the CSS style {@code -mvf4x-tab-max-width}
   *
   * @return The property
   */
  public DoubleProperty tabMaxWidthProperty() {
    return tabMaxWidth;
  }

  /**
   * Convenience setter for the {@code tabMaxWidth} property.
   * <p>
   * See documentation of {@link #tabMaxWidthProperty() tabMaxWidth} property for details.
   *
   * @param tabMaxWidth The new value
   */
  public void setTabMaxWidth(double tabMaxWidth) {
    this.tabMaxWidth.set(tabMaxWidth);
  }

  /**
   * Convenience getter for the {@code tabMinWidth} property.
   * <p>
   * See documentation of {@link #tabMinWidthProperty() tabMinWidth} property for details.
   *
   * @return The current value
   */
  public double getTabMinWidth() {
    return tabMinWidth.get();
  }

  /**
   * {@code tabMinWidth} property.
   * <p>
   * This property controls the minimum width a tab may have. It is guaranteed that a tab never
   * exceeds this limit, independent from its own settings.
   * <p>
   * This property can be also set via the CSS style {@code -mvf4x-tab-min-width}
   *
   * @return The property
   */
  public DoubleProperty tabMinWidthProperty() {
    return tabMinWidth;
  }

  /**
   * Convenience setter for the {@code tabMinWidth} property.
   * <p>
   * See documentation of {@link #tabMinWidthProperty() tabMinWidth} property for details.
   *
   * @param tabMinWidth The new value
   */
  public void setTabMinWidth(double tabMinWidth) {
    this.tabMinWidth.set(tabMinWidth);
  }

  /**
   * Convenience getter for the {@code viewSelectorControls} property.
   * <p>
   * See documentation of {@link #viewSelectorControlsProperty() viewSelectorControls} property for
   * details.
   *
   * @return The current value
   */
  public ViewSelectorControls getViewSelectorControls() {
    return viewSelectorControls.get();
  }

  /**
   * The {@code viewSelectorControls} property.
   * <p>
   * This property defines, which controls are shown in case that the header of the group overflows,
   * so that not all tabs can be made visible. The {@link ViewSelectorControls} provides the valid
   * values.
   * <p>
   * This property can be also set via the CSS style {@code -mvf4x-view-selector-controls}
   *
   * @return The property
   */
  public ObjectProperty<ViewSelectorControls> viewSelectorControlsProperty() {
    return viewSelectorControls;
  }

  /**
   * Convenience setter for the {@code viewSelectorControls} property.
   * <p>
   * See documentation of {@link #viewSelectorControlsProperty() viewSelectorControls} property for
   * details.
   *
   * @param viewSelectorControls The new value
   */
  public void setViewSelectorControls(ViewSelectorControls viewSelectorControls) {
    this.viewSelectorControls.set(viewSelectorControls);
  }

  /**
   * Convenience getter for the {@code dragTags} property.
   * <p>
   * See documentation of {@link #dragTagsProperty() dragTags} property for details.
   *
   * @return The current value.
   */
  @Override
  public Set<String> getDragTags() {
    return dragTags.getValue();
  }

  /**
   * The {@code dragTags} property.
   * <p>
   * This property contains the drag tags assigned to this instance.
   * <p>
   * These tags are evaluated when attempting to drop this object into another one. This is only
   * possible, if all the tags in this set can be found in the
   * {@link ViewGroup#getDropTags() drop tags} of the target or this set is empty.
   *
   * @return The property
   */
  public ObjectProperty<Set<String>> dragTagsProperty() {
    return dragTags;
  }

  /**
   * Convenience setter for the {@code dragTags} property.
   * <p>
   * See documentation of {@link #dragTagsProperty() dragTags} property for details.
   *
   * @param dragTags New value
   */
  public void setDragTags(Set<String> dragTags) {
    this.dragTags.setValue(
        Collections.unmodifiableSet(Objects.requireNonNullElseGet(dragTags, HashSet::new)));
  }

  /**
   * Convenience getter for the {@code dropTags} property.
   * <p>
   * See documentation of {@link #dropTagsProperty() dropTags} property for details.
   *
   * @return The current value.
   */
  public Set<String> getDropTags() {
    return dropTags.getValue();
  }

  /**
   * The {@code dropTags} property.
   * <p>
   * This property contains the drop tags assigned to this instance.
   * <p>
   * These tags are evaluated when attempting to drop an object into this one. This is only
   * possible, if the tags in this set are a super set of the tags found in the
   * {@link ViewOrGroup#getDragTags()} tags} of the source or this set is empty.
   *
   * @return The property
   */
  public ObjectProperty<Set<String>> dropTagsProperty() {
    return dropTags;
  }

  /**
   * Convenience setter for the {@code dropTags} property.
   * <p>
   * See documentation of {@link #dropTagsProperty() dropTags} property for details.
   *
   * @param dropTags New value
   */
  public void setDropTags(Set<String> dropTags) {
    this.dropTags.setValue(
        Collections.unmodifiableSet(Objects.requireNonNullElseGet(dropTags, HashSet::new)));
  }


  /**
   * Convenience getter for the {@code dropDropSplitSides} property.
   * <p>
   * See documentation of {@link #dropSplitSidesProperty() dropSplitSides} property for details.
   *
   * @return The current value
   */
  public Set<Side> getDropSplitSides() {
    return dropSplitSides.get();
  }

  /**
   * The {@code dropDropSplitSides} property.
   * <p>
   * This property controls, whether and how the container can be split when a drag and drop
   * operation is done.
   * <p>
   * When trying to drop an object into this container, the container is split vertically or
   * horizontally, so that two areas exist, one for the already existing content and one area for
   * the object being dropped. This property defines, on which sides the already existing content
   * can be placed.
   * <p>
   * By default, this property contains all possible sides, so that arbitrary splitting is possible;
   * setting it top an empty list forbids splitting.
   *
   * @return The property.
   */
  public ObjectProperty<Set<Side>> dropSplitSidesProperty() {
    return dropSplitSides;
  }

  /**
   * Convenience setter for the {@code dropDropSplitSides} property.
   * <p>
   * See documentation of {@link #dropSplitSidesProperty() dropSplitSides} property for details.
   *
   * @param value The new value
   */
  public void setDropSplitSides(Set<Side> value) {
    this.dropSplitSides.set(
        Collections.unmodifiableSet(Objects.requireNonNullElseGet(value, HashSet::new)));
  }

  /**
   * Convenience getter for the {@code dropTargetTypes} property.
   * <p>
   * See documentation of {@link #dropTargetTypesProperty() dropTargetTypes} property for details.
   *
   * @return The current value.
   */
  @Override
  public Set<DropTargetType> getDropTargetTypes() {
    return dropTargetTypes.getValue();
  }

  /**
   * The {@code dropTargetTypes} property.
   * <p>
   * Depending on these types it is decided, whether a drag and drop operation is supported. See the
   * enum {@link de.hipphampel.mv4fx.view.ViewOrGroup.DropTargetType} for details.
   *
   * @return The property
   */
  public ObjectProperty<Set<DropTargetType>> dropTargetTypesProperty() {
    return dropTargetTypes;
  }

  /**
   * Convenience setter for the {@code dropTargetTypes} property.
   * <p>
   * See documentation of {@link #dropTargetTypesProperty() dropTargetTypes} property for details.
   *
   * @param dropTargetTypes New value
   */
  public void setDropTargetTypes(Set<DropTargetType> dropTargetTypes) {
    this.dropTargetTypes.setValue(
        Collections.unmodifiableSet(Objects.requireNonNullElseGet(dropTargetTypes, HashSet::new)));
  }

  /**
   * Called during a drag and drop operation to determine the {@link DropTarget}.
   * <p>
   * It depends on the settings of this instance and the settings and kind of drag source, which
   * operations are possible in general, but at the bottom line the following possible two targets
   * might exist:
   * <ul>
   *   <li>The drag source might (a single {@code View} or all {@code Views} of a different
   *   {@code ViewGroup} might be added to the views of this instance</li>
   *   <li>This {@code ViewGroup} is split into two parts and the one part contains the old views
   *   and the other the drag source then.</li>
   * </ul>
   *
   * @param context The {@code DragAndDropContext}
   * @param pos     The position of the mouse pointer
   * @return A possible {@code DropTarget}
   */
  public Optional<DropTarget> findDropTarget(DragAndDropContext context, Point2D pos) {
    if (getSkin() instanceof ViewGroupSkin skin) {
      return skin.findDropTarget(context, pos);
    }
    return Optional.empty();
  }

  @Override
  public void setDragging(boolean dragging) {
    this.dragging.setValue(dragging);
  }

  @Override
  public ReadOnlyBooleanProperty draggingProperty() {
    return this.dragging;
  }

  public DropTarget getDropTarget() {
    return dropTarget.get();
  }

  public ObjectProperty<DropTarget> dropTargetProperty() {
    return dropTarget;
  }

  public void setDropTarget(DropTarget dropTarget) {
    this.dropTarget.set(dropTarget);
  }

  @Override
  protected Skin<?> createDefaultSkin() {
    return new ViewGroupSkin(this);
  }

  protected void onViewsChanged(Change<? extends View> change) {
    while (change.next()) {
      if (change.wasAdded()) {
        change.getAddedSubList().forEach(view -> view.setViewGroup(this));
      } else if (change.wasRemoved()) {
        change.getRemoved().forEach(view -> view.setViewGroup(null));
      }
    }
  }

  @Override
  public boolean isViewGroup() {
    return true;
  }

  @Override
  public ViewGroup asViewGroup() {
    return this;
  }

  @Override
  public ViewGroup asControl() {
    return this;
  }
}