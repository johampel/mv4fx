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

import de.hipphampel.mv4fx.utils.Utils;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

/**
 * Represents a view to by shown in a {@link ViewGroup}.
 * <p>
 * A {@code View} is best compared with a {@code Tab} of a {@code TabPane}, regarding its functionality there are some similarities, but
 * also differences.
 * <p>
 * Similar to a {@code Tab}, a {@code View} has a {@link #getContent() content} to be display in a {@link ViewGroup}, when the {@code View}
 * is selected. It also has several to properties to influence the tab title and graphic that is used to represent a {@code View} in the
 * header area of the {@code ViewGroup}. All in all, a {@code View} offers more ways to customize the appearance then a {@code Tab} does.
 * <p>
 * The most important feature is - compare with a {@code Tab} - that a {@code View} can be dragged and dropped to another {@code ViewGroup},
 * a new stage or other places, so {@code Views} are not bound to a fixed {@code ViewGroup}.
 *
 * @see ViewGroup
 */
public class View implements ViewOrGroup {

  /**
   * Enum describing when a tab action is visible.
   * <p>
   * Tab actions are the "close" or "maximize" button in the tab controls of the {@link ViewGroup}. The enum describes, when such a button
   * is visible.
   */
  public enum TabActionVisibility {
    /**
     * Button is always visible.
     */
    ALWAYS,

    /**
     * Button is visible, when the mouse is over the tab.
     */
    MOUSE_OVER,

    /**
     * Button is visible, when the view is selected.
     */
    SELECTED,

    /**
     * Button is never visible.
     */
    NEVER
  }


  public static final String PROPERTY_VIEW_GROUP = "viewGroup";
  public static final String PROPERTY_CONTENT = "content";
  public static final String PROPERTY_TAB_LABEL = "tabLabel";
  public static final String PROPERTY_TAB_STYLE = "tabStyle";
  public static final String PROPERTY_TAB_TOOLTIP = "tabTooltip";
  public static final String PROPERTY_TAB_CONTEXT_MENU = "tabContextMenu";
  public static final String PROPERTY_TAB_GRAPHIC = "tabGraphic";
  public static final String PROPERTY_TAB_NODE_CALLBACK = "tabNode";
  public static final String PROPERTY_TAB_CLOSE_ACTION = "tabCloseAction";
  public static final String PROPERTY_TAB_MAXIMIZE_ACTION = "tabMaximizeAction";
  public static final String PROPERTY_DRAG_TAGS = "dragTags";
  public static final String PROPERTY_DROP_TARGET_TYPES = "dropTargetTypes";
  public static final String PROEPRTY_DRAGGING = "dragging";
  public static final String PROPERTY_MAXIMIZED = "maximized";
  public static final String PROPERTY_MAXIMIZABLE = "maximizable";

  private final ObjectProperty<ViewGroup> viewGroup;
  private final ObjectProperty<Node> content;
  private final StringPropertyBase tabLabel;
  private final StringPropertyBase tabStyle;
  private final ObjectProperty<Node> tabGraphic;
  private final ObjectProperty<Tooltip> tabTooltip;
  private final ObjectProperty<ContextMenu> tabContextMenu;
  private final ObjectProperty<Callback<Side, Node>> tabNode;
  private final ObjectProperty<TabActionVisibility> tabCloseActionVisibility;
  private final ObjectProperty<TabActionVisibility> tabMaximizeActionVisibility;
  private final SetProperty<String> dragTags;
  private final SetProperty<DropTargetType> dropTargetTypes;
  private final BooleanProperty dragging;
  private final BooleanProperty maximizable;
  private final BooleanProperty maximized;

  /**
   * Constructor.
   */
  public View() {
    this.viewGroup = new SimpleObjectProperty<>(this, PROPERTY_VIEW_GROUP);
    this.content = new SimpleObjectProperty<>(this, PROPERTY_CONTENT);
    this.tabLabel = new SimpleStringProperty(this, PROPERTY_TAB_LABEL);
    this.tabStyle = new SimpleStringProperty(this, PROPERTY_TAB_STYLE);
    this.tabGraphic = new SimpleObjectProperty<>(this, PROPERTY_TAB_GRAPHIC);
    this.tabNode = new SimpleObjectProperty<>(this, PROPERTY_TAB_NODE_CALLBACK);
    this.tabTooltip = new SimpleObjectProperty<>(this, PROPERTY_TAB_TOOLTIP, null);
    this.tabContextMenu = new SimpleObjectProperty<>(this, PROPERTY_TAB_CONTEXT_MENU, null);
    this.tabCloseActionVisibility = new SimpleObjectProperty<>(this, PROPERTY_TAB_CLOSE_ACTION, TabActionVisibility.MOUSE_OVER);
    this.tabMaximizeActionVisibility = new SimpleObjectProperty<>(this, PROPERTY_TAB_MAXIMIZE_ACTION, TabActionVisibility.NEVER);
    this.dragTags = new SimpleSetProperty<>(this, PROPERTY_DRAG_TAGS, FXCollections.observableSet());
    this.dropTargetTypes = new SimpleSetProperty<>(this, PROPERTY_DROP_TARGET_TYPES,
        FXCollections.observableSet(DropTargetType.REORDER, DropTargetType.CHANGE_GROUP, DropTargetType.NEW_WINDOW));
    this.dragging = new SimpleBooleanProperty(this, PROEPRTY_DRAGGING, false);
    this.maximizable = new SimpleBooleanProperty(this, PROPERTY_MAXIMIZABLE, true);
    this.maximized = new SimpleBooleanProperty(this, PROPERTY_MAXIMIZED, false);
  }

  /**
   * Convenience getter for {@code viewGroup} property.
   * <p>
   * See documentation of {@link #viewGroupProperty() viewGroup} property for details.
   *
   * @return The property value
   */
  public ViewGroup getViewGroup() {
    return viewGroup.get();
  }

  /**
   * The {@code viewGroup} property.
   * <p>
   * This property is automatically computed and contains the {@link ViewGroup} the {@code View} is currently member of.
   *
   * @return The property.
   */
  public ReadOnlyObjectProperty<ViewGroup> viewGroupProperty() {
    return viewGroup;
  }

  // Internal use only
  void setViewGroup(ViewGroup viewGroup) {
    this.viewGroup.set(viewGroup);
  }

  /**
   * Convenience getter for {@code content} property.
   * <p>
   * See documentation of {@link #contentProperty() content} property for details.
   *
   * @return The property value
   */
  public Node getContent() {
    return content.get();
  }

  /**
   * {@code content} property.
   * <p>
   * Represents the content of the view.
   *
   * @return The property
   */
  public ObjectProperty<Node> contentProperty() {
    return content;
  }

  /**
   * Convenience setter for {@code content} property.
   * <p>
   * See documentation of {@link #contentProperty() content} property for details.
   *
   * @param content The property value
   */
  public void setContent(Node content) {
    this.content.set(content);
  }

  /**
   * Convenience getter for {@code tabLabel} property.
   * <p>
   * See documentation of {@link #tabLabelProperty() tabLabel} property for details.
   *
   * @return The property value
   */
  public String getTabLabel() {
    return tabLabel.get();
  }

  /**
   * {@code tabLabel} property.
   * <p>
   * This property allows to specify the text of the label representing the {@code View}. Such labels are used to render the tab of the view
   * (unless a {@link #tabNodeProperty() tabNode} is specified), or in other labels, e.g. when representing the view in a list.
   *
   * @return The property
   */
  public StringPropertyBase tabLabelProperty() {
    return tabLabel;
  }

  /**
   * Convenience setter for {@code tabLabel} property.
   * <p>
   * See documentation of {@link #tabLabelProperty() tabLabel} property for details.
   *
   * @param tabLabel The property value
   */
  public void setTabLabel(String tabLabel) {
    this.tabLabel.set(tabLabel);
  }

  /**
   * Convenience getter for {@code tabStyle} property.
   * <p>
   * See documentation of {@link #tabStyleProperty() tabStyle} property for details.
   *
   * @return The property value
   */
  public String getTabStyle() {
    return tabStyle.get();
  }

  /**
   * {@code tabStyle} property.
   * <p>
   * This property allows to specify the CSS style of the label representing the {@code View}. Such labels are used to render the tab of the
   * view (unless a {@link #tabNodeProperty() tabNode} is specified), or in other labels, e.g. when representing the view in a list.
   *
   * @return The property
   */
  public StringPropertyBase tabStyleProperty() {
    return tabStyle;
  }

  /**
   * Convenience setter for {@code tabGraphic} property.
   * <p>
   * See documentation of {@link #tabGraphicProperty() tabGraphic} property for details.
   *
   * @param tabStyle The property value
   */
  public void setTabStyle(String tabStyle) {
    this.tabStyle.set(tabStyle);
  }

  /**
   * Convenience getter for {@code tabGraphic} property.
   * <p>
   * See documentation of {@link #tabGraphicProperty() tabGraphic} property for details.
   *
   * @return The property value
   */
  public Node getTabGraphic() {
    return tabGraphic.get();
  }

  /**
   * {@code tabGraphic} property.
   * <p>
   * This property allows to specify the graphic of the label representing the {@code View}. Such labels are used to render the tab of the
   * view (unless a {@link #tabNodeProperty() tabNode} is specified), or in other labels, e.g. when representing the view in a list.
   * <p>
   * When used in tabs, the graphic is automatically rotate according to the side, so that it is always shown upright.
   *
   * @return The property
   */
  public ObjectProperty<Node> tabGraphicProperty() {
    return tabGraphic;
  }

  /**
   * Convenience setter for {@code tabGraphic} property.
   * <p>
   * See documentation of {@link #tabGraphicProperty() tabGraphic} property for details.
   *
   * @param tabGraphic The new value
   */
  public void setTabGraphic(Node tabGraphic) {
    this.tabGraphic.set(tabGraphic);
  }

  /**
   * Convenience getter for the {@code tabNode} property.
   * <p>
   * See documentation of {@link #tabNodeProperty() tabNode} property for details.
   *
   * @return The current value.
   */
  public Callback<Side, Node> getTabNode() {
    return tabNode.get();
  }

  /**
   * {@code tabNode} property.
   * <p>
   * This property allows to specify a {@link Callback} that is called to create the inner control of the tab for the view. If there are
   * action buttons (see {@link #tabCloseActionVisibilityProperty() tabCloseAction} and
   * {@link #tabMaximizeActionVisibilityProperty() tabMaximizeAction}), these are rendered separately from the inner tab control. By
   * default, this is {@code null}.
   * <p>
   * In case this property is set, the {@link #tabLabelProperty() tabLabel}, {@link #tabStyleProperty() tabStyle}, and
   * {@link #tabGraphicProperty()} tabGraphic} property are only used for rendering labels for representing the view (e.g. in lists)
   * <p>
   * By default, this property is {@code null}
   *
   * @return The property.
   */
  public ObjectProperty<Callback<Side, Node>> tabNodeProperty() {
    return tabNode;
  }

  /**
   * Convenience setter for the {@code tabNode} property.
   * <p>
   * See documentation of {@link #tabNodeProperty() tabNode} property for details.
   *
   * @param tabNode New value
   */
  public void setTabNode(Callback<Side, Node> tabNode) {
    this.tabNode.set(tabNode);
  }

  /**
   * Convenience getter for the {@code tabCloseActionVisibility} property.
   * <p>
   * See documentation of {@link #tabCloseActionVisibilityProperty() tabCloseAction} property for details.
   *
   * @return The current value.
   */
  public TabActionVisibility getTabCloseActionVisibility() {
    return tabCloseActionVisibility.get();
  }

  /**
   * {@code tabCloseActionVisibility} property.
   * <p>
   * This property controls, under which conditions a button is shown in the tab to close the {@code View}.
   * <p>
   * By default, the button is visible only if the mouse is over the tab
   *
   * @return The property.
   */
  public ObjectProperty<TabActionVisibility> tabCloseActionVisibilityProperty() {
    return tabCloseActionVisibility;
  }

  /**
   * Convenience setter for the {@code tabCloseActionVisibility} property.
   * <p>
   * See documentation of {@link #tabCloseActionVisibilityProperty() tabCloseActionVisibility} property for details.
   *
   * @param tabCloseActionVisibility New value
   */
  public void setTabCloseActionVisibility(TabActionVisibility tabCloseActionVisibility) {
    this.tabCloseActionVisibility.set(tabCloseActionVisibility);
  }

  /**
   * Convenience getter for the {@code tabMaximizeActionVisibility} property.
   * <p>
   * See documentation of {@link #tabMaximizeActionVisibilityProperty() tabMaximizeActionVisibility} property for details.
   *
   * @return The current value.
   */
  public TabActionVisibility getTabMaximizeActionVisibility() {
    return tabMaximizeActionVisibility.get();
  }

  /**
   * {@code tabMaximizeActionVisibility} property.
   * <p>
   * This property controls, under which conditions a button is shown in the tab to maximize the {@code View}.
   * <p>
   * By default, the button is not visible.
   *
   * @return The property.
   */
  public ObjectProperty<TabActionVisibility> tabMaximizeActionVisibilityProperty() {
    return tabMaximizeActionVisibility;
  }

  /**
   * Convenience setter for the {@code tabMaximizeActionVisibility} property.
   * <p>
   * See documentation of {@link #tabMaximizeActionVisibilityProperty() tabMaximizeActionVisibility} property for details.
   *
   * @param tabMaximizeActionVisibility New value
   */
  public void setTabMaximizeActionVisibility(TabActionVisibility tabMaximizeActionVisibility) {
    this.tabMaximizeActionVisibility.set(tabMaximizeActionVisibility);
  }

  /**
   * Convenience getter for the {@code tabTooltip} property.
   * <p>
   * See documentation of {@link #tabTooltipProperty() tabTooltip} property for details.
   *
   * @return The current value.
   */
  public Tooltip getTabTooltip() {
    return tabTooltip.get();
  }

  /**
   * {@code tabTooltip} property.
   * <p>
   * This property controls the {@link Tooltip} that belongs to the view. The tooltip is shown in the tab area of the view.
   *
   * @return The property
   */
  public ObjectProperty<Tooltip> tabTooltipProperty() {
    return tabTooltip;
  }

  /**
   * Convenience setter for the {@code tabTooltip} property.
   * <p>
   * See documentation of {@link #tabTooltipProperty() tabTooltip} property for details.
   *
   * @param tabTooltip New value
   */
  public void setTabTooltip(Tooltip tabTooltip) {
    this.tabTooltip.set(tabTooltip);
  }

  /**
   * Convenience getter for the {@code tabContextMenu} property.
   * <p>
   * See documentation of {@link #tabContextMenuProperty() tabContextMenu} property for details.
   *
   * @return The current value.
   */
  public ContextMenu getTabContextMenu() {
    return tabContextMenu.get();
  }

  /**
   * {@code tabContextMenu} property.
   * <p>
   * This property controls the {@link ContextMenu} that belongs to the view. The context menu is shown in the tab area of the view.
   *
   * @return The property
   */
  public ObjectProperty<ContextMenu> tabContextMenuProperty() {
    return tabContextMenu;
  }

  /**
   * Convenience setter for the {@code tabContextMenu} property.
   * <p>
   * See documentation of {@link #tabContextMenuProperty() tabContextMenu} property for details.
   *
   * @param tabContextMenu New value
   */
  public void setTabContextMenu(ContextMenu tabContextMenu) {
    this.tabContextMenu.set(tabContextMenu);
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
    return dragTags.get();
  }

  /**
   * The {@code dragTags} property.
   * <p>
   * This property contains the drag tags assigned to this instance.
   * <p>
   * These tags are evaulated when attempting to drop this object into an other one. This is only possible, if all the tags in this set can
   * be found in the {@link ViewGroup#getDropTags() drop tags} of the target.
   *
   * @return The property
   */
  public SetProperty<String> dragTagsProperty() {
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
    this.dragTags.setValue(FXCollections.observableSet(Objects.requireNonNullElseGet(dragTags, HashSet::new)));
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
    return dropTargetTypes.get();
  }

  /**
   * The {@code dropTargetTypes} property.
   * <p>
   * Depending on these types it is decided, whether a drag and drop operation is supported.
   *
   * @return The property
   */
  public SetProperty<DropTargetType> dropTargetTypesProperty() {
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
    this.dropTargetTypes.set(FXCollections.observableSet(Objects.requireNonNullElseGet(dropTargetTypes, HashSet::new)));
  }

  /**
   * Check, whether closing this {@link View} is allowed.
   * <p>
   * This implementation return {@code true} by default, derived classes might implement a decision here to decide, whether it is safe to
   * delete the {@code View}.
   * <p>
   * This method is called internally always just before any attempt to close the {@code View}; however the {@link #close() close} method
   * itself does not call this method
   *
   * @return {@code true} if allowed
   */
  public boolean canClose() {
    return true;
  }

  /**
   * Closes this {@code View}.
   * <p>
   * Closing means that the view is removed from the {@link ViewGroup}. It also normalizes the entire hierarchy of the
   * {@link ViewGroupContainer ViewGroupContainers} and {@code ViewGroups} to close and remove all empty components that are allowed to be
   * auto closed.
   */
  public void close() {
    if (getViewGroup() != null) {
      ViewGroupContainer root = Utils.getRootViewGroupContainer(this.getViewGroup()).orElse(null);
      getViewGroup().removeView(this);
      Utils.normalizeOrCloseViewGroupContainer(root);
    }
  }

  public boolean isMaximizable() {
    return maximizable.get();
  }

  public BooleanProperty maximizableProperty() {
    return maximizable;
  }

  public void setMaximizable(boolean maximizable) {
    this.maximizable.set(maximizable);
  }

  public boolean isMaximized() {
    return maximized.get();
  }

  public BooleanProperty maximizedProperty() {
    return maximized;
  }


  /**
   * Convenience setter for the {@code maximizedProperty} property.
   * <p>
   * See documentation of {@link #maximizedProperty() maximizedProperty} property for details.
   *
   * @param maximized New value
   */
  public void setMaximized(boolean maximized) {
    this.maximized.set(maximized);
  }

  @Override
  public void setDragging(boolean dragging) {
    this.dragging.setValue(dragging);
  }

  @Override
  public ReadOnlyBooleanProperty draggingProperty() {
    return this.dragging;
  }

  @Override
  public Control asControl() {
    return this.getViewGroup();
  }
}
