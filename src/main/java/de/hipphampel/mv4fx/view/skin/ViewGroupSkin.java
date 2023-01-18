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

import static de.hipphampel.mv4fx.view.Constants.CLASS_DROP_TARGET;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_CONTENT;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_HEADER;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_SELECTOR;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_SELECTOR_CONTROL;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_SELECTOR_CONTROL_DECORATION;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_SELECTOR_NEXT;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_SELECTOR_PREV;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_SELECTOR_SELECT;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_TAB;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_TAB_BUTTON;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_TAB_BUTTON_CLOSE;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_TAB_BUTTON_MAXIMIZE;
import static de.hipphampel.mv4fx.view.Constants.CLASS_VIEW_GROUP_TAB_CONTAINER;

import de.hipphampel.mv4fx.utils.SizeType;
import de.hipphampel.mv4fx.utils.Utils;
import de.hipphampel.mv4fx.view.DragAndDropContext;
import de.hipphampel.mv4fx.view.DropTarget;
import de.hipphampel.mv4fx.view.DropTarget.MoveToGroup;
import de.hipphampel.mv4fx.view.DropTarget.SplitViewGroup;
import de.hipphampel.mv4fx.view.View;
import de.hipphampel.mv4fx.view.View.TabActionVisibility;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.ViewGroup.ViewSelectorControls;
import de.hipphampel.mv4fx.view.ViewGroupContainer;
import de.hipphampel.mv4fx.view.ViewOrGroup.DropTargetType;
import java.util.List;
import java.util.Optional;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Callback;

/**
 * Default skin for {@link ViewGroup}
 */
public class ViewGroupSkin extends SkinBase<ViewGroup> {

  public static final PseudoClass PSEUDO_CLASS_TOP = PseudoClass.getPseudoClass("top");
  public static final PseudoClass PSEUDO_CLASS_RIGHT = PseudoClass.getPseudoClass("right");
  public static final PseudoClass PSEUDO_CLASS_BOTTOM = PseudoClass.getPseudoClass("bottom");
  public static final PseudoClass PSEUDO_CLASS_LEFT = PseudoClass.getPseudoClass("left");
  public static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");
  public static final PseudoClass PSEUDO_CLASS_HOVER = PseudoClass.getPseudoClass("hover");
  public static final PseudoClass PSEUDO_CLASS_DRAGGING = PseudoClass.getPseudoClass("dragging");

  private final ListChangeListener<View> viewListChangeListener;
  private final InvalidationListener sideChangeListener;
  private final InvalidationListener selectedViewListener;
  private final InvalidationListener draggingListener;
  private final InvalidationListener dropTargetListener;
  private final ContentContainer contentContainer;
  private final HeaderContainer headerContainer;
  private final DropTargetBox dropTargetBox;

  /**
   * Constructor.
   *
   * @param control The associated {@link ViewGroup}
   */
  public ViewGroupSkin(ViewGroup control) {
    super(control);
    this.contentContainer = newContentContainer();
    this.headerContainer = newHeaderContainer();
    this.dropTargetBox = newDropTargetBox();
    getChildren().addAll(headerContainer, contentContainer, dropTargetBox);

    this.viewListChangeListener = this::onViewsChanged;
    this.sideChangeListener = observable -> onSideChanged();
    this.selectedViewListener = observable -> onViewSelected();
    this.draggingListener = observable -> onDraggingChanged();
    this.dropTargetListener = observable -> onDropTargetChanged();
    control.getViews().addListener(viewListChangeListener);
    control.getSelection().selectedItemProperty().addListener(this.selectedViewListener);
    control.sideProperty().addListener(sideChangeListener);
    control.draggingProperty().addListener(draggingListener);
    control.dropTargetProperty().addListener(dropTargetListener);
    for (int i = 0; i < control.getViews().size(); i++) {
      onViewAdded(i, control.getViews().get(i));
    }

    if (!control.getViews().isEmpty() && control.getSelection().isEmpty()) {
      control.selectView(control.getViews().get(0));
    }

    onSideChanged();
    onViewSelected();
  }

  /**
   * Gets the {@link HeaderContainer}
   *
   * @return The {@code HeaderContainer}
   */
  protected HeaderContainer getHeaderContainer() {
    return headerContainer;
  }

  /**
   * Gets the {@link ContentContainer}
   *
   * @return The {@code ContentContainer}
   */
  protected ContentContainer getContentContainer() {
    return contentContainer;
  }

  @Override
  public void dispose() {
    contentContainer.setView(null);
    contentContainer.dispose();
    headerContainer.dispose();
    getSkinnable().dropTargetProperty().addListener(dropTargetListener);
    getSkinnable().draggingProperty().removeListener(draggingListener);
    getSkinnable().getSelection().selectedItemProperty().removeListener(selectedViewListener);
    getSkinnable().getViews().removeListener(viewListChangeListener);
    getSkinnable().sideProperty().removeListener(sideChangeListener);
    super.dispose();
  }

  //
  // Layout management
  //

  @Override
  protected double computeMinWidth(double height, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return computeWidth(height, topInset, rightInset, bottomInset, leftInset, SizeType.MIN);
  }

  @Override
  protected double computeMinHeight(double width, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return computeHeight(width, topInset, rightInset, bottomInset, leftInset, SizeType.MIN);
  }

  @Override
  protected double computeMaxWidth(double height, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return computeWidth(height, topInset, rightInset, bottomInset, leftInset, SizeType.MAX);
  }

  @Override
  protected double computeMaxHeight(double width, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return computeHeight(width, topInset, rightInset, bottomInset, leftInset, SizeType.MAX);
  }

  @Override
  protected double computePrefWidth(double height, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return computeWidth(height, topInset, rightInset, bottomInset, leftInset, SizeType.PREF);
  }

  @Override
  protected double computePrefHeight(double width, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return computeHeight(width, topInset, rightInset, bottomInset, leftInset, SizeType.PREF);
  }

  private double computeWidth(double height, double topInset, double rightInset,
      double bottomInset, double leftInset, SizeType type) {
    Side side = getEffectiveSide();
    double contentWidth = type.getWidth(contentContainer, height);
    double headerSize = side.isVertical() ? type.getWidth(headerContainer, height) : 0;
    return snapSizeX(contentWidth) + snapSizeX(headerSize) + leftInset + rightInset;
  }

  private double computeHeight(double width, double topInset, double rightInset,
      double bottomInset, double leftInset, SizeType type) {
    Side side = getEffectiveSide();
    double contentHeight = type.getHeight(contentContainer, width);
    double headerSize = side.isHorizontal() ? type.getHeight(headerContainer, width) : 0;
    return snapSizeY(contentHeight) + snapSizeY(headerSize) + topInset + bottomInset;
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    Side side = getEffectiveSide();

    headerContainer.getTransforms().clear();

    double hh = side.isHorizontal() ?
        snapSizeY(headerContainer.computePrefHeight(-1))
        : snapSizeX(headerContainer.computePrefHeight(-1));

    switch (side) {
      case TOP -> {
        layoutHeader(x, y, w, hh);
        layoutContent(x, y + hh, w, h - hh);
      }
      case BOTTOM -> {
        layoutHeader(x, h - hh, w, hh);
        layoutContent(x, y, w, h - hh);
      }
      case LEFT -> {
        headerContainer.getTransforms().add(new Rotate(-90, 0, hh));
        layoutHeader(x + hh, y + h - hh, h, hh);
        layoutContent(x + hh, y, w - hh, h);
      }
      case RIGHT -> {
        headerContainer.getTransforms().add(new Rotate(90, 0, hh));
        layoutHeader(x + w - hh, y - hh, h, hh);
        layoutContent(x, y, w - hh, h);
      }
    }
    layoutDropTargetBox(x, y, w, h);
  }

  private void layoutHeader(double x, double y, double w, double h) {
    headerContainer.resize(w, h);
    headerContainer.relocate(x, y);
  }

  private void layoutContent(double x, double y, double w, double h) {
    contentContainer.resize(w, h);
    contentContainer.relocate(x, y);
    contentContainer.setClip(new Rectangle(w, h));
  }

  private void layoutDropTargetBox(double x, double y, double w, double h) {
    if (!(getSkinnable().getDropTarget() instanceof SplitViewGroup dropTarget)) {
      dropTargetBox.relocate(0, 0);
      dropTargetBox.resize(0, 0);
      return;
    }

    switch (dropTarget.dropSide()) {
      case TOP -> {
        h /= 2;
      }
      case RIGHT -> {
        x += w / 2;
        w /= 2;
      }
      case BOTTOM -> {
        y += h / 2;
        h /= 2;
      }
      case LEFT -> {
        w /= 2;
      }
    }
    dropTargetBox.relocate(x, y);
    dropTargetBox.resize(w, h);
  }

  //
  // View management
  //

  /**
   * Called, when the side of the {@code ViewGroup} has been changed.
   * <p>
   * Basically sets the pseudo class
   */
  protected void onSideChanged() {
    ViewGroup viewGroup = getSkinnable();
    Side side = viewGroup.getSide();
    viewGroup.pseudoClassStateChanged(PSEUDO_CLASS_TOP, side == Side.TOP);
    viewGroup.pseudoClassStateChanged(PSEUDO_CLASS_RIGHT, side == Side.RIGHT);
    viewGroup.pseudoClassStateChanged(PSEUDO_CLASS_BOTTOM, side == Side.BOTTOM);
    viewGroup.pseudoClassStateChanged(PSEUDO_CLASS_LEFT, side == Side.LEFT);
  }

  /**
   * Called, when the list of views has been changed.
   *
   * @param change The change
   */
  protected void onViewsChanged(Change<? extends View> change) {
    while (change.next()) {
      if (change.wasAdded()) {
        for (int pos = change.getFrom(); pos < change.getTo(); pos++) {
          onViewAdded(pos, change.getList().get(pos));
        }
      } else if (change.wasRemoved()) {
        for (View view : change.getRemoved()) {
          onViewRemoved(view);
        }
      }
    }
  }

  /**
   * Called, when a {@link View} has been added.
   * <p>
   * Adds an according {@link TabControl}.
   *
   * @param pos  Position of the {@code View}
   * @param view The {@code View}
   */
  protected void onViewAdded(int pos, View view) {
    headerContainer.getTabContainer().addView(pos, view);
  }

  /**
   * Called, when a {@link View} has been removed.
   * <p>
   * Removes the according {@link TabControl}.
   *
   * @param view The {@code View}
   */
  protected void onViewRemoved(View view) {
    List<View> views = getSkinnable().getViews();
    int selection = getSkinnable().getSelection().getSelectedIndex();
    if (selection >= views.size() && selection > 0) {
      getSkinnable().selectView(views.get(selection - 1));
    } else {
      getSkinnable().selectView(null);
    }
    headerContainer.getTabContainer().removeView(view);
  }

  protected void onViewSelected() {
    View view = getSkinnable().getSelection().getSelectedItem();
    headerContainer.getTabContainer().selectView(view);
    contentContainer.setView(view);
  }

  //
  // Drag And Drop support
  //

  /**
   * Tries to find the drop target during a drag and drop position.
   * <p>
   * This simply delegates to the method of the tab container, so refer to
   * {@linkplain TabContainer#findDropTarget(DragAndDropContext, Point2D) there} for details
   *
   * @param context The {@link DragAndDropContext}
   * @param pos     The position
   * @return The target, if present.
   */
  public Optional<DropTarget> findDropTarget(DragAndDropContext context, Point2D pos) {
    Point2D posInTabContainer = headerContainer.tabContainer.parentToLocal(
        headerContainer.parentToLocal(pos));
    return headerContainer.tabContainer.findDropTarget(context, posInTabContainer)
        .or(() -> findSplittingDropTarget(context, pos));
  }

  private Optional<DropTarget> findSplittingDropTarget(DragAndDropContext context, Point2D pos) {
    if (getSkinnable() == null || !(getSkinnable().getParent() instanceof ViewGroupContainer)) {
      return Optional.of(DropTarget.none());
    }

    Side dropSide = getDropTargetSide(pos);
    if (!getSkinnable().getDropSplitSides().contains(dropSide)) {
      return Optional.of(DropTarget.none());
    }

    if (context.getDragSource() instanceof ViewGroup viewGroup) {
      return findSplittingDropTargetForViewGroup(viewGroup, dropSide);
    } else if (context.getDragSource() instanceof View view) {
      return findSplittingDropTargetForView(view, dropSide);
    }
    return Optional.empty();
  }

  private Optional<DropTarget> findSplittingDropTargetForView(View view, Side dropSide) {
    if (getSkinnable().getViews().size() == 1 && getSkinnable().getViews().get(0) == view) {
      return Optional.of(DropTarget.none());
    }
    // Valid drag target?
    if (view.getDropTargetTypes() == null ||
        !view.getDropTargetTypes().contains(DropTargetType.CHANGE_GROUP)) {
      return Optional.of(DropTarget.none());
    }

    // Do tags match?
    if (!Utils.isDragAndDropTagMatch(view.getDragTags(), getSkinnable().getDropTags())) {
      return Optional.of(DropTarget.none());
    }

    return Optional.of(DropTarget.splitViewGroup(getSkinnable(), dropSide));
  }

  private Optional<DropTarget> findSplittingDropTargetForViewGroup(ViewGroup source,
      Side dropSide) {
    // Dropping into self?
    if (source == getSkinnable()) {
      return Optional.of(DropTarget.none());
    }

    // Changing group allowed?
    if (source.getDropTargetTypes() == null ||
        !source.getDropTargetTypes().contains(DropTargetType.CHANGE_GROUP)) {
      return Optional.of(DropTarget.none());
    }

    // Do tags match?
    if (!Utils.isDragAndDropTagMatch(source.getDragTags(), getSkinnable().getDropTags())) {
      return Optional.of(DropTarget.none());
    }

    return Optional.of(DropTarget.splitViewGroup(getSkinnable(), dropSide));

  }

  private Side getDropTargetSide(Point2D pos) {
    double x = pos.getX();
    double y = pos.getY();
    double w = getSkinnable().getWidth();
    double h = getSkinnable().getHeight();

    boolean topOrRight = y * w <= x * h;
    boolean bottomOrLeft = (h - y) * w <= x * h;
    if (topOrRight) {
      return bottomOrLeft ? Side.RIGHT : Side.TOP;
    } else {
      return bottomOrLeft ? Side.BOTTOM : Side.LEFT;
    }
  }

  private void onDraggingChanged() {
    headerContainer.pseudoClassStateChanged(PSEUDO_CLASS_DRAGGING, getSkinnable().isDragging());
  }

  private void onDropTargetChanged() {
    getSkinnable().requestLayout();
    headerContainer.tabContainer.requestLayout();
  }

  //
  // Factory methods
  //

  /**
   * Factory method to create a {@link ContentContainer}.
   * <p>
   * Can be overwritten in case a alternative implementation is wanted to host the content.
   *
   * @return The {@code ContentContainer}
   */
  protected ContentContainer newContentContainer() {
    return new ContentContainer();
  }

  /**
   * Factory method to create a {@link HeaderContainer}.
   * <p>
   * Can be overwritten in case a alternative implementation is wanted to host the content.
   *
   * @return The {@code HeaderContainer}
   */
  protected HeaderContainer newHeaderContainer() {
    return new HeaderContainer();
  }

  /**
   * Factory method to create a {@link TabContainer}.
   * <p>
   * Can be overwritten in case a alternative implementation is wanted to host the content.
   *
   * @return The {@code TabContainer}
   */
  protected TabContainer newTabContainer() {
    return new TabContainer();
  }

  /**
   * Factory method to create a {@link ViewSelector}.
   * <p>
   * Can be overwritten in case a alternative implementation is wanted to host the content.
   *
   * @return The {@code ViewSelector}
   */
  protected ViewSelector newViewSelector() {
    return new ViewSelector();
  }

  /**
   * Factory method to create a {@link TabControl}.
   * <p>
   * Can be overwritten in case a alternative implementation is wanted to host the content.
   *
   * @return The {@code TabControl}
   */
  protected TabControl newTabControl(View view) {
    return new TabControl(view);
  }

  /**
   * Factory method to create a {@link DropTargetBox}.
   * <p>
   * Can be overwritten in case a alternative implementation is wanted to host the drop target box.
   *
   * @return The {@code DropTargetBox}
   */
  protected DropTargetBox newDropTargetBox() {
    return new DropTargetBox();
  }

  //
  // Helpers
  //

  /**
   * Returns {@code true}, if tabs are layed out in reverse order.
   *
   * @return {@code true} if reverse layout
   */
  protected boolean isReverseHeaderLayout() {
    return getEffectiveSide() == Side.LEFT;
  }

  /**
   * Returns the effective side.
   * <p>
   * Returns {@link Side#TOP} in case the side is not set
   *
   * @return The side.
   */
  protected Side getEffectiveSide() {
    Side side = getSkinnable().getSide();
    return side == null ? Side.TOP : side;
  }

  //
  // Internal classes
  //

  /**
   * Container that manages the content area of a {@link View}.
   * <p>
   * It hosts the content of the current view.
   */
  protected static class ContentContainer extends Pane {

    private final InvalidationListener contentChangedListener;
    private View view;

    /**
     * Constructor.
     */
    protected ContentContainer() {
      this.getStyleClass().add(CLASS_VIEW_GROUP_CONTENT);
      this.contentChangedListener = observable -> onUpdateContent();
    }

    /**
     * Removes all listeners.
     */
    protected void dispose() {
      if (this.view != null) {
        this.view.contentProperty().removeListener(contentChangedListener);
        this.view.maximizedProperty().removeListener(contentChangedListener);
        this.view.maximizableProperty().removeListener(contentChangedListener);
      }
    }

    @Override
    protected double computeMinWidth(double height) {
      return computeWidth(height, SizeType.MIN);
    }

    @Override
    protected double computeMinHeight(double width) {
      return computeHeight(width, SizeType.MIN);
    }

    @Override
    protected double computeMaxWidth(double height) {
      return computeWidth(height, SizeType.MAX);
    }

    @Override
    protected double computeMaxHeight(double width) {
      return computeHeight(width, SizeType.MAX);
    }

    @Override
    protected double computePrefWidth(double height) {
      return computeWidth(height, SizeType.PREF);
    }

    @Override
    protected double computePrefHeight(double width) {
      return computeHeight(width, SizeType.PREF);
    }

    private double computeWidth(double height, SizeType type) {
      return getManagedChildren().stream()
          .mapToDouble(c -> type.getWidth(c, height))
          .max()
          .orElse(0)
          + snappedLeftInset()
          + snappedRightInset();
    }

    private double computeHeight(double width, SizeType type) {
      double h = getManagedChildren().stream()
          .mapToDouble(c -> type.getHeight(c, width))
          .max()
          .orElse(0);
      return h + snappedTopInset() + snappedBottomInset();
    }

    /**
     * Sets the current {@link View}.
     * <p>
     * The content of this view becomes the content of this container.
     *
     * @param newView The new view or {@code null}
     */
    protected void setView(View newView) {
      if (this.view != null && this.contentChangedListener != null) {
        this.view.contentProperty().removeListener(contentChangedListener);
        this.view.maximizedProperty().removeListener(contentChangedListener);
        this.view.maximizableProperty().removeListener(contentChangedListener);
      }
      this.view = newView;
      if (this.view != null) {
        this.view.contentProperty().addListener(contentChangedListener);
        this.view.maximizedProperty().addListener(contentChangedListener);
        this.view.maximizableProperty().addListener(contentChangedListener);
      }
      onUpdateContent();
    }

    /**
     * Called when the content of the current view has been changed.
     * <p>
     * Updates the content of the pane accordingly.
     */
    protected void onUpdateContent() {
      Node content = view == null ? null : view.getContent();
      if (content == null || (view.isMaximized() && view.isMaximizable())) {
        getChildren().clear();
      } else {
        getChildren().setAll(content);
      }
    }

    @Override
    protected void layoutChildren() {
      double ti = snappedTopInset();
      double li = snappedLeftInset();
      double w = getWidth() - li - snappedRightInset();
      double h = getHeight() - ti - snappedBottomInset();
      Node child = getChildren().isEmpty() ? null : getChildren().get(0);
      if (child != null) {
        layoutInArea(child, li, ti, w, h, getBaselineOffset(), HPos.CENTER, VPos.CENTER);
      }
    }
  }

  /**
   * Container for the header area.
   * <p>
   * It contains the {@link TabContainer} and - if  present - the {@link ViewGroup#getLeftTopHeaderArea() ViewGroup.leftTopHeaderArea} and
   * the {@link ViewGroup#getRightBottomHeaderArea()} ViewGroup.rightBottomArea}.
   */
  protected class HeaderContainer extends StackPane {

    private final TabContainer tabContainer;
    private final InvalidationListener sideListener;
    private final InvalidationListener leftTopAreaListener;
    private final InvalidationListener rightBottomAreaListener;
    private Node leftTopArea;
    private Node rightBottomArea;

    /**
     * Constructor.
     */
    protected HeaderContainer() {
      getStyleClass().add(CLASS_VIEW_GROUP_HEADER);
      this.tabContainer = newTabContainer();
      this.sideListener = observable -> onSideChanged();
      this.leftTopAreaListener = observable -> onLeftTopAreaChanged();
      this.rightBottomAreaListener = observable -> onRightBottomAreaChanged();
      getSkinnable().leftTopHeaderAreaProperty().addListener(leftTopAreaListener);
      getSkinnable().rightBottomHeaderAreaProperty().addListener(rightBottomAreaListener);
      onLeftTopAreaChanged();
      onRightBottomAreaChanged();

      setOnDragDetected(evt -> {
        DragAndDropContext context = DragAndDropContext.getInstance();
        context.start(getSkinnable(), evt.getScreenX(), evt.getScreenY());
      });
      setOnMouseReleased(evt -> {
        DragAndDropContext context = DragAndDropContext.getInstance();
        if (context.getDragSource() == getSkinnable()) {
          context.confirm(evt.getScreenX(), evt.getScreenY());
        }
      });
      setOnMouseDragged(evt -> {
        DragAndDropContext context = DragAndDropContext.getInstance();
        if (context.getDragSource() == getSkinnable()) {
          context.update(evt.getScreenX(), evt.getScreenY());
        }
      });

    }

    /**
     * Gets the associated {@link TabContainer}
     *
     * @return The {@code TabContainer}
     */
    protected TabContainer getTabContainer() {
      return tabContainer;
    }

    /**
     * Gets the left top area, if present
     *
     * @return The node
     */
    protected Node getLeftTopArea() {
      return leftTopArea;
    }

    /**
     * Gets the right/bottom area, if present
     *
     * @return The node
     */
    protected Node getRightBottomArea() {
      return rightBottomArea;
    }

    /**
     * Disposes the instance.
     * <p>
     * Removes all listeners
     */
    protected void dispose() {
      getSkinnable().rightBottomHeaderAreaProperty().removeListener(rightBottomAreaListener);
      getSkinnable().leftTopHeaderAreaProperty().removeListener(leftTopAreaListener);
      getSkinnable().sideProperty().removeListener(sideListener);
      tabContainer.dispose();
    }

    /**
     * Called when the side has been changed.
     * <p>
     * Updates the controls accordingly.
     */
    protected void onSideChanged() {
      onLeftTopAreaChanged();
      onRightBottomAreaChanged();
    }

    /**
     * Called when the control for the left/top area has been changed.
     * <p>
     * If present, calculates the new value for the control and calls {@link #updateChildren()}
     */
    protected void onLeftTopAreaChanged() {
      Callback<Side, Node> callback = getSkinnable().getLeftTopHeaderArea();
      this.leftTopArea = callback == null ? null : callback.call(getEffectiveSide());
      updateChildren();
    }

    /**
     * Called when the control for the right/bottom area has been changed.
     * <p>
     * If present, calculates the new value for the control and calls {@link #updateChildren()}
     */
    protected void onRightBottomAreaChanged() {
      Callback<Side, Node> callback = getSkinnable().getRightBottomHeaderArea();
      this.rightBottomArea = callback == null ? null : callback.call(getEffectiveSide());
      updateChildren();
    }

    /**
     * Updates the list of available children.
     */
    protected void updateChildren() {
      ObservableList<Node> children = getChildren();
      children.clear();
      children.add(getTabContainer());
      if (getLeftTopArea() != null) {
        children.add(getLeftTopArea());
      }
      if (getRightBottomArea() != null) {
        children.add(getRightBottomArea());
      }
    }

    @Override
    protected double computeMinWidth(double height) {
      return computeWidth(height, SizeType.MIN);
    }

    @Override
    protected double computeMinHeight(double width) {
      return computeHeight(width, SizeType.MIN);
    }

    @Override
    protected double computeMaxWidth(double height) {
      return computeWidth(height, SizeType.MAX);
    }

    @Override
    protected double computeMaxHeight(double width) {
      return computeHeight(width, SizeType.MAX);
    }

    @Override
    protected double computePrefWidth(double height) {
      return computeWidth(height, SizeType.PREF);
    }

    @Override
    protected double computePrefHeight(double width) {
      return computeHeight(width, SizeType.PREF);
    }

    private double computeWidth(double height, SizeType type) {
      return getManagedChildren().stream()
          .mapToDouble(c -> type.getWidth(c, height))
          .sum()
          + snappedLeftInset()
          + snappedRightInset();
    }

    private double computeHeight(double width, SizeType type) {
      double h = getManagedChildren().stream()
          .mapToDouble(c -> type.getHeight(c, width))
          .max()
          .orElse(0);
      return h + snappedTopInset() + snappedBottomInset();
    }

    @Override
    protected void layoutChildren() {
      double ti = snappedTopInset();
      double li = snappedLeftInset();
      double w = getWidth() - li - snappedRightInset();
      double h = getHeight() - ti - snappedBottomInset();
      Node left = isReverseHeaderLayout() ? getRightBottomArea() : getLeftTopArea();
      Node right = isReverseHeaderLayout() ? getLeftTopArea() : getRightBottomArea();
      double x = li;
      if (left != null) {
        double lw = left.prefWidth(h);
        layoutInArea(left, x, ti, lw, h, getBaselineOffset(), HPos.CENTER, VPos.CENTER);
        x += lw;
        w -= lw;
      }

      if (right != null) {
        double rw = right.prefWidth(h);
        layoutInArea(right, x + w - rw, ti, rw, h, getBaselineOffset(),
            HPos.CENTER,
            VPos.CENTER);
        w -= rw;
      }

      layoutInArea(tabContainer, x, ti, w, h, getBaselineOffset(),
          HPos.CENTER, getEffectiveSide() == Side.BOTTOM ? VPos.TOP : VPos.BOTTOM);
    }
  }

  /**
   * Container for the tab controls of the {@link View Views}.
   * <p>
   * This is a child of the {@link HeaderContainer} and hosts all the tab controls for the different views. If there are too many tabs so
   * that they don't fit in the visible area, a {@link ViewSelector} is shown.
   */
  protected class TabContainer extends StackPane {

    private ViewSelector viewSelector;
    private DropTargetBox dropTargetBox;
    private double scrollOffset = 0;

    /**
     * Constructor.
     */
    protected TabContainer() {
      getStyleClass().add(CLASS_VIEW_GROUP_TAB_CONTAINER);
      this.viewSelector = newViewSelector();
      this.dropTargetBox = newDropTargetBox();
      getChildren().addAll(viewSelector, dropTargetBox);
    }

    /**
     * Gets the associated {@link ViewSelector}.
     *
     * @return The {@code ViewSelector}
     */
    protected ViewSelector getViewSelector() {
      return viewSelector;
    }

    /**
     * Disposes the instance.
     * <p>
     * Basically, removes all listeners
     */
    protected void dispose() {
      getViewSelector().dispose();
      for (TabControl tab : getTabControls()) {
        tab.dispose();
      }
    }

    /**
     * Called when a {@link View} is added to the group.
     * <p>
     * Creates an according {@link TabControl} and adds it to the container.
     *
     * @param pos  Insert position.
     * @param view {@code View} to add.
     */
    protected void addView(int pos, View view) {
      if (pos >= getManagedChildren().size()) {
        throw new ArrayIndexOutOfBoundsException(pos);
      }
      TabControl tabControl = newTabControl(view);
      getChildren().add(pos, tabControl);
    }

    /**
     * Called when a {@link View} is removed from the group.
     * <p>
     * Removes the according {@link TabControl}from the container.
     *
     * @param view The view to remove.
     */
    protected void removeView(View view) {
      TabControl tabControl = (TabControl) getChildren().stream()
          .filter(n -> n instanceof TabControl)
          .filter(n -> ((TabControl) n).view == view)
          .findFirst()
          .orElse(null);
      if (tabControl == null) {
        return;
      }
      tabControl.dispose();
      getChildren().remove(tabControl);
      layoutChildren();
    }

    /**
     * Called when a view is selected.
     * <p>
     * Applies the {@code selected} pseudo class to the {@link TabControl} of the selected {@link View}.
     *
     * @param view The new selected {@code View}
     */
    protected void selectView(View view) {
      TabControl[] tabControls = getTabControls();
      for (TabControl tabControl : tabControls) {
        tabControl.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, view == tabControl.view);
      }
    }

    /**
     * Finds the {@link DropTarget} for the given position.
     * <p>
     * In case that {@code pos} is out of bounds, {@code empty} is returned. Otherwise the exact behaviour depends on the source of the drag
     * and drop operation and the position:
     *
     * <ul>
     *   <li>If the source is this {@code ViewGroup}, a {@linkplain DropTarget#none() undroppable}
     *   target is returned</li>
     *   <li>If the source is a different {@code ViewGroup}, the {@link ViewGroup#getDropTargetTypes()
     *   dropTargetTypes} of it allow movement to a different view group and all of the
     *   {@link ViewGroup#getDragTags() dragTags} are in the {@link ViewGroup#getDropTags() dropTags}
     *   then a {@linkplain DropTarget#moveToGroup(ViewGroup, int) move-to-group} target is returned,
     *   otherwise an undroppable</li>
     *   <li>If the source is a {@code View} of this group, a move-to-group target is returned, if
     *   the position would really change and {@code dropTargetTypes} allow reordering</li>
     *   <li>If the source is a {@code View} of a different group, the same rules apply as for a
     *   a different {@code ViewGroup}</li>
     * </ul>
     *
     * @param context The context for the drag and drop operation
     * @param pos     The position where to drop
     * @return The drop target
     */
    protected Optional<DropTarget> findDropTarget(DragAndDropContext context, Point2D pos) {
      if (!getBoundsInLocal().contains(pos)) {
        return Optional.empty();
      }

      if (context.getDragSource() instanceof View view) {
        return findDropTargetForView(view, pos);
      } else if (context.getDragSource() instanceof ViewGroup viewGroup) {
        return findDropTargetForViewGroup(viewGroup, pos);
      } else {
        return Optional.of(DropTarget.none());
      }
    }

    private Optional<DropTarget> findDropTargetForView(View source, Point2D pos) {
      // Valid drag target?
      DropTargetType dragTargetType =
          source.getViewGroup() == getSkinnable() ? DropTargetType.REORDER
              : DropTargetType.CHANGE_GROUP;
      if (source.getDropTargetTypes() == null ||
          !source.getDropTargetTypes().contains(dragTargetType)) {
        return Optional.of(DropTarget.none());
      }

      int dropIndex = calculateDropIndex(pos);
      if (dragTargetType == DropTargetType.REORDER) {
        // Do we really reorder?
        int index = getSkinnable().getViews().indexOf(source);
        if (dropIndex == index || dropIndex == index + 1) {
          return Optional.of(DropTarget.none());
        }
      } else {
        // Do tags match?
        if (!Utils.isDragAndDropTagMatch(source.getDragTags(), getSkinnable().getDropTags())) {
          return Optional.of(DropTarget.none());
        }
      }

      return Optional.of(DropTarget.moveToGroup(getSkinnable(), dropIndex));
    }

    private Optional<DropTarget> findDropTargetForViewGroup(ViewGroup source, Point2D pos) {

      // Dropping into self?
      if (source == getSkinnable()) {
        return Optional.of(DropTarget.none());
      }

      // Changing group allowed?
      if (source.getDropTargetTypes() == null ||
          !source.getDropTargetTypes().contains(DropTargetType.CHANGE_GROUP)) {
        return Optional.of(DropTarget.none());
      }

      // Do tags match?
      if (!Utils.isDragAndDropTagMatch(source.getDragTags(), getSkinnable().getDropTags())) {
        return Optional.of(DropTarget.none());
      }

      return Optional.of(DropTarget.moveToGroup(getSkinnable(), calculateDropIndex(pos)));
    }

    private int calculateDropIndex(Point2D pos) {
      TabControl[] tabControls = getTabControls();
      boolean reverse = isReverseHeaderLayout();
      for (int index = 0; index < tabControls.length; index++) {
        TabControl tabControl = tabControls[index];
        double middle = tabControl.getLayoutX() + tabControl.getWidth() / 2;
        if ((reverse && middle <= pos.getX()) || (!reverse && middle > pos.getX())) {
          return index;
        }
      }
      return tabControls.length;
    }

    @Override
    protected double computeMinWidth(double height) {
      return 0;
    }

    @Override
    protected double computeMinHeight(double width) {
      return computeHeight(width, SizeType.MIN);
    }

    @Override
    protected double computeMaxWidth(double height) {
      return computeWidth(height, SizeType.MAX);
    }

    @Override
    protected double computeMaxHeight(double width) {
      return computeHeight(width, SizeType.MAX);
    }

    @Override
    protected double computePrefWidth(double height) {
      return computeWidth(height, SizeType.PREF);
    }

    @Override
    protected double computePrefHeight(double width) {
      return computeHeight(width, SizeType.PREF);
    }

    private double computeWidth(double height, SizeType type) {
      // max(controlButton, sum(tabControls))
      ViewSelector viewSelector = getViewSelector();
      double[] widths = getTabWidths(getTabControls(), height, type);
      double width = Math.max(Utils.sum(widths), type.getWidth(viewSelector, height));
      return width + snappedLeftInset() + snappedRightInset();
    }

    private double computeHeight(double width, SizeType type) {
      // max(controlButton, max(tabControls))
      double height = type.getHeight(getViewSelector(), width);
      for (TabControl tab : getTabControls()) {
        height = Math.max(height, type.getHeight(tab, width));
      }
      return height + snappedTopInset() + snappedBottomInset();
    }

    /**
     * Layout the {@link TabControl TabControls}.
     * <p>
     * The available tab controls are layed out one after another in a horizontal fashion
     */
    @Override
    protected void layoutChildren() {
      boolean reverse = isReverseHeaderLayout();
      TabControl[] tabs = getTabControls();
      Side side = getSkinnable().getSide();
      double ti = snappedTopInset();
      double ri = snappedRightInset();
      double bi = snappedBottomInset();
      double li = snappedLeftInset();
      double width = getWidth() - li - ri;
      double height = getHeight() - ti - bi;

      setClip(new Rectangle(li, ti, width, height));
      if (tabs.length == 0) {
        hideViewSelector();
        return;
      }

      double[] tabWidths = getTabWidths(tabs, height, SizeType.PREF);
      double tabTotalWidth = Utils.sum(tabWidths);
      boolean requiresViewSelector = width < tabTotalWidth;

      double x = li;

      // Layout view selector, if required
      ViewSelector viewSelector = getViewSelector();
      if (requiresViewSelector) {
        double vsw = viewSelector.prefWidth(height);
        double vsx = reverse ? x : x + width - vsw;
        layoutInArea(viewSelector, vsx, ti, vsw, height, -1, HPos.CENTER,
            side == Side.BOTTOM ? VPos.TOP : VPos.BOTTOM);
        viewSelector.setVisible(true);
        width -= vsw;
        x += reverse ? vsw : 0;
      } else {
        hideViewSelector();
      }

      // Calculate the offset to the selected view, if any
      double[] tabPoss = getTabPositions(tabWidths, width);
      double scrollOffset = updateScrollOffset(width, tabPoss, tabWidths);

      // Layout the tabs
      for (int i = 0; i < tabs.length; i++) {
        layoutInArea(
            tabs[i],
            tabPoss[i] + scrollOffset + x,
            ti,
            tabWidths[i],
            height,
            -1,
            HPos.CENTER,
            side == Side.BOTTOM ? VPos.TOP : VPos.BOTTOM);
      }
      layoutDropTargetBox(li, ti, height, tabPoss, tabWidths);
    }

    private void layoutDropTargetBox(double x, double y, double h, double[] tabPoss,
        double[] tabWidths) {
      DropTarget target = getSkinnable().getDropTarget();
      if (!(target instanceof MoveToGroup moveToGroup)
          || moveToGroup.viewGroup() != getSkinnable()) {
        dropTargetBox.setVisible(false);
        return;
      }
      int index = moveToGroup.index();
      double w;
      if (index == 0) {
        x += scrollOffset;
        w = tabWidths[0] / 2;
      } else if (index == tabWidths.length) {
        x += tabPoss[index - 1] + tabWidths[index - 1] / 2 + scrollOffset;
        w = tabWidths[index - 1] / 2;
      } else {
        x += tabPoss[index - 1] + tabWidths[index - 1] / 2 + scrollOffset;
        w = (tabWidths[index] + tabWidths[index - 1]) / 2;
      }
      dropTargetBox.relocate(x, y);
      dropTargetBox.resize(w, h);
      dropTargetBox.setVisible(true);
    }

    private void hideViewSelector() {
      ViewSelector viewSelector = getViewSelector();
      layoutInArea(viewSelector, 0, 0, 0, 0,
          -1, HPos.CENTER, VPos.CENTER);
      viewSelector.setVisible(false);
    }

    private double updateScrollOffset(double width, double[] tabPoss, double[] tabWidths) {
      int selectedIndex = getSkinnable().getSelection().getSelectedIndex();
      if (selectedIndex == -1) {
        scrollOffset = 0;
        return scrollOffset;
      }

      double totalTabWidth = Utils.sum(tabWidths);
      if (totalTabWidth <= width) {
        scrollOffset = 0;
        return scrollOffset;
      }

      double tabPos = tabPoss[selectedIndex];
      double tabWidth = tabWidths[selectedIndex];

      if (isReverseHeaderLayout()) {
        if (scrollOffset + tabPos < 0) {
          // offset to low
          scrollOffset = Math.max(0, -tabPos);
        } else if (scrollOffset + tabPos + tabWidth > width) {
          // offset to high
          scrollOffset = -tabPos + Math.max(0, width - tabWidth);
        } else {
          // offset fits
          scrollOffset = Math.min(scrollOffset, totalTabWidth - width);
        }
      } else {
        if (scrollOffset + tabPos < 0) {
          // offset to low
          scrollOffset = -tabPos;
        } else if (scrollOffset + tabPos + tabWidth > width) {
          // offset to high
          scrollOffset = Math.min(0, -tabPos + Math.max(0, width - tabWidth));
        } else {
          // offset ok
          scrollOffset = Math.min(0, Math.max(scrollOffset, width - totalTabWidth));
        }
      }
      return scrollOffset;
    }

    private double[] getTabPositions(double[] widths, double width) {
      double[] positions = new double[widths.length];
      if (isReverseHeaderLayout()) {
        double pos = width;
        for (int i = 0; i < positions.length; i++) {
          pos -= widths[i];
          positions[i] = pos;
        }

      } else {
        double pos = 0;
        for (int i = 0; i < positions.length; i++) {
          positions[i] = pos;
          pos += widths[i];
        }
      }
      return positions;
    }

    private double[] getTabWidths(TabControl[] tabControls, double height, SizeType type) {
      double[] widths = new double[tabControls.length];
      for (int i = 0; i < tabControls.length; i++) {
        widths[i] = type.getWidth(tabControls[i], height);
      }
      return widths;
    }

    TabControl[] getTabControls() {
      List<Node> children = getManagedChildren();
      TabControl[] tabControls = new TabControl[children.size() - 2];
      for (int i = 0; i < tabControls.length; i++) {
        tabControls[i] = (TabControl) children.get(i);
      }
      return tabControls;
    }
  }

  /**
   * Panel containing controls to select a {@link View}.
   * <p>
   * The selector is visible, in case that not all {@link TabControl TabControls} fit into the {@link TabContainer}.
   * <p>
   * Which controls are present, depends on the {@link ViewGroup#viewSelectorControlsProperty() ViewGroup.viewSelectorControls} setting.
   */
  protected class ViewSelector extends HBox {

    private final InvalidationListener selectionListener;
    private final InvalidationListener controlsListener;
    ContextMenu menu;

    /**
     * Constructor.
     */
    protected ViewSelector() {
      getStyleClass().add(CLASS_VIEW_GROUP_SELECTOR);
      this.selectionListener = observable -> onSelectionChanged();
      this.controlsListener = observable -> onControlsChanged();

      getSkinnable().getSelection().selectedItemProperty().addListener(selectionListener);
      getSkinnable().viewSelectorControlsProperty().addListener(controlsListener);
      getSkinnable().sideProperty().addListener(controlsListener);
      onControlsChanged();
    }

    /**
     * Removes all listeners.
     */
    protected void dispose() {
      getSkinnable().sideProperty().removeListener(controlsListener);
      getSkinnable().viewSelectorControlsProperty().removeListener(controlsListener);
      getSkinnable().getSelection().selectedItemProperty().removeListener(selectionListener);
    }

    /**
     * Called when clicking on the "previous view" button.
     * <p>
     * This button selects the next left/top {@link View} relative to the current selection.
     */
    protected void onPrevView() {
      changeSelection(-1);
    }

    /**
     * Called when clicking on the "next view" button.
     * <p>
     * This button selects the next right/bottom {@link View} relative to the current selection.
     */
    protected void onNextView() {
      changeSelection(+1);
    }

    private void changeSelection(int incrementOrDecrement) {
      ViewGroup skinnable = getSkinnable();
      int index = getSkinnable().getSelection().getSelectedIndex() + incrementOrDecrement;
      int max = skinnable.getViews().size() - 1;
      skinnable.selectView(skinnable.getViews().get(Math.max(0, Math.min(max, index))));
    }

    /**
     * Called when clicking the "select view" button.
     * <p>
     * Opens a context menu to select the view.
     */
    protected void onSelectView() {
      if (menu != null) {
        menu.hide();
        menu = null;
        return;
      }

      Node control = getControlByStyle(CLASS_VIEW_GROUP_SELECTOR_SELECT).orElse(null);
      if (control == null) {
        return;
      }

      menu = createContextMenu();
      Side side = switch (getEffectiveSide()) {
        case TOP -> Side.BOTTOM;
        case RIGHT -> Side.LEFT;
        case BOTTOM -> Side.TOP;
        case LEFT -> Side.RIGHT;
      };
      menu.show(control, side, 0, 0);
    }

    private ContextMenu createContextMenu() {
      ContextMenu menu = new ContextMenu();
      menu.setOnHidden(evt -> this.menu = null);
      ToggleGroup group = new ToggleGroup();
      int id = 0;
      for (View view : getSkinnable().getViews()) {
        RadioMenuItem item = new RadioMenuItem();
        item.setId("view" + (id++));
        item.setText(view.getTabLabel());
        item.setGraphic(view.getTabGraphic());
        item.setStyle(view.getTabStyle());
        item.setToggleGroup(group);
        item.setSelected(view == getSkinnable().getSelection().getSelectedItem());
        item.setOnAction(evt -> getSkinnable().selectView(view));
        menu.getItems().add(item);
      }

      return menu;
    }

    /**
     * Called when the available controls are changed.
     * <p>
     * Depending on {@link ViewGroup#getViewSelectorControls() ViewGroup.viewGroupSelectorControls} controls are made available or not.
     */
    protected void onControlsChanged() {
      ViewSelectorControls setting = getSkinnable().getViewSelectorControls();
      if (setting == null) {
        setting = ViewSelectorControls.DROPDOWN;
      }

      List<Node> controls = List.of();
      switch (setting) {
        case ARROWS -> {
          if (isReverseHeaderLayout()) {
            controls = List.of(
                newControl(CLASS_VIEW_GROUP_SELECTOR_PREV, evt -> onNextView()),
                newControl(CLASS_VIEW_GROUP_SELECTOR_NEXT, evt -> onPrevView())
            );
          } else {
            controls = List.of(
                newControl(CLASS_VIEW_GROUP_SELECTOR_PREV, evt -> onPrevView()),
                newControl(CLASS_VIEW_GROUP_SELECTOR_NEXT, evt -> onNextView())
            );
          }
        }
        case DROPDOWN -> controls = List.of(
            newControl(CLASS_VIEW_GROUP_SELECTOR_SELECT, evt -> onSelectView())
        );
        case BOTH -> {
          if (isReverseHeaderLayout()) {
            controls = List.of(
                newControl(CLASS_VIEW_GROUP_SELECTOR_SELECT, evt -> onSelectView()),
                newControl(CLASS_VIEW_GROUP_SELECTOR_PREV, evt -> onNextView()),
                newControl(CLASS_VIEW_GROUP_SELECTOR_NEXT, evt -> onPrevView())
            );
          } else {
            controls = List.of(
                newControl(CLASS_VIEW_GROUP_SELECTOR_PREV, evt -> onPrevView()),
                newControl(CLASS_VIEW_GROUP_SELECTOR_NEXT, evt -> onNextView()),
                newControl(CLASS_VIEW_GROUP_SELECTOR_SELECT, evt -> onSelectView())
            );
          }
        }
      }
      getChildren().setAll(controls);
      onSelectionChanged();
    }

    /**
     * Called when the selected {@link View} changes.
     * <p>
     * Depending on the selection, controls are enabled or disabled.
     */
    protected void onSelectionChanged() {
      int index = getSkinnable().getSelection().getSelectedIndex();
      getControlByStyle(isReverseHeaderLayout() ? CLASS_VIEW_GROUP_SELECTOR_NEXT : CLASS_VIEW_GROUP_SELECTOR_PREV).ifPresent(
          n -> n.setDisable(index <= 0));
      getControlByStyle(isReverseHeaderLayout() ? CLASS_VIEW_GROUP_SELECTOR_PREV : CLASS_VIEW_GROUP_SELECTOR_NEXT).ifPresent(
          n -> n.setDisable(index == -1 || index + 1 >= getSkinnable().getViews().size()));
    }

    private Optional<Node> getControlByStyle(String style) {
      return getManagedChildren().stream()
          .filter(n -> n instanceof StackPane sp
              && sp.getChildren().size() == 1
              && sp.getChildren().get(0).getStyleClass().contains(style))
          .findFirst();
    }

    /**
     * Creates a new control.
     * <p>
     * This is basically a {@link StackPane} hosting a {@link Label}. The pane has the predefined style {@code view-selector-control} and
     * the label the {@code control-decoration} style.
     *
     * @param style   The additional style of the label,
     * @param handler The event hanler.
     * @return The pane.
     */
    protected Node newControl(String style, EventHandler<MouseEvent> handler) {
      StackPane pane = new StackPane();
      pane.getStyleClass().add(CLASS_VIEW_GROUP_SELECTOR_CONTROL);
      pane.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);
      Label label = new Label();
      label.getStyleClass().addAll(style, CLASS_VIEW_GROUP_SELECTOR_CONTROL_DECORATION);
      pane.getChildren().add(label);
      return pane;
    }
  }

  /**
   * Component representing a tab for a {@link View}.
   * <p>
   * There is one instance of this class per {@code View} in a {@link ViewGroup}. It basically contains a node representing the view (e.g. a
   * {@link Label}) and optionally one or more buttons for certain operations.
   */
  protected class TabControl extends Pane {

    private final int SPACING = 2;

    private final View view;
    private final InvalidationListener tabActionChangeListener;
    private final InvalidationListener tabControlChangeListener;
    private final InvalidationListener sizeChangeListener;
    private final InvalidationListener draggingChangeListener;
    private final InvalidationListener maximizeChangeListener;
    private Button closeButton;
    private Button maximizeButton;
    private Node control;
    private Label label;

    /**
     * Constructor.
     *
     * @param view The associated {@link View}
     */
    protected TabControl(View view) {
      getStyleClass().add(CLASS_VIEW_GROUP_TAB);
      this.view = view;
      this.tabActionChangeListener = observable -> onTabActionVisibilityChanged();
      this.view.tabCloseActionVisibilityProperty().addListener(tabActionChangeListener);
      this.view.tabMaximizeActionVisibilityProperty().addListener(tabActionChangeListener);
      this.getPseudoClassStates().addListener(tabActionChangeListener);

      this.tabControlChangeListener = observable -> onTabControlChanged();
      this.view.tabLabelProperty().addListener(tabControlChangeListener);
      this.view.tabStyleProperty().addListener(tabControlChangeListener);
      this.view.tabGraphicProperty().addListener(tabControlChangeListener);
      this.view.tabNodeProperty().addListener(tabControlChangeListener);
      this.view.tabTooltipProperty().addListener(tabControlChangeListener);
      this.view.tabContextMenuProperty().addListener(tabControlChangeListener);
      getSkinnable().sideProperty().addListener(tabControlChangeListener);

      this.sizeChangeListener = observable -> requestLayout();
      getSkinnable().tabMinWidthProperty().addListener(sizeChangeListener);
      getSkinnable().tabMaxWidthProperty().addListener(sizeChangeListener);

      this.draggingChangeListener = observable -> onDraggingChanged();
      this.view.draggingProperty().addListener(draggingChangeListener);

      this.maximizeChangeListener = observable -> onMaximizeChanged();
      this.view.maximizableProperty().addListener(maximizeChangeListener);
      this.view.maximizedProperty().addListener(maximizeChangeListener);

      setOnMouseClicked(this::onMouseClicked);
      setOnDragDetected(evt -> {
        DragAndDropContext context = DragAndDropContext.getInstance();
        context.start(view, evt.getScreenX(), evt.getScreenY());
      });
      setOnMouseReleased(evt -> {
        DragAndDropContext context = DragAndDropContext.getInstance();
        if (context.getDragSource() == view) {
          context.confirm(evt.getScreenX(), evt.getScreenY());
        }
      });
      setOnMouseDragged(evt -> {
        DragAndDropContext context = DragAndDropContext.getInstance();
        if (context.getDragSource() == view) {
          context.update(evt.getScreenX(), evt.getScreenY());
        }
      });
      onTabControlChanged();
      onTabActionVisibilityChanged();
    }

    /**
     * Disposes this instance.
     * <p>
     * This effectively means that all listeners are removed from the view.
     */
    protected void dispose() {
      setOnMouseClicked(null);

      getSkinnable().tabMinWidthProperty().removeListener(sizeChangeListener);
      getSkinnable().tabMaxWidthProperty().removeListener(sizeChangeListener);

      getSkinnable().sideProperty().removeListener(tabControlChangeListener);
      this.view.tabNodeProperty().removeListener(tabControlChangeListener);
      this.view.tabGraphicProperty().removeListener(tabControlChangeListener);
      this.view.tabStyleProperty().removeListener(tabControlChangeListener);
      this.view.tabLabelProperty().removeListener(tabControlChangeListener);
      this.view.tabTooltipProperty().removeListener(tabControlChangeListener);
      this.view.tabContextMenuProperty().removeListener(tabControlChangeListener);

      this.getPseudoClassStates().removeListener(tabActionChangeListener);
      this.view.tabMaximizeActionVisibilityProperty().removeListener(tabActionChangeListener);
      this.view.tabCloseActionVisibilityProperty().removeListener(tabActionChangeListener);

      this.view.draggingProperty().removeListener(this.draggingChangeListener);

      this.view.maximizedProperty().removeListener(this.maximizeChangeListener);
      this.view.maximizableProperty().removeListener(this.maximizeChangeListener);
    }

    /**
     * Called when the mouse is clicked.
     * <p>
     * Make the view to the active view
     *
     * @param mouseEvent The causing event
     */
    protected void onMouseClicked(MouseEvent mouseEvent) {
      getSkinnable().getSelection().select(this.view);
    }

    /**
     * Called, when a prooerty is changed that is related to the inner tab control.
     * <p>
     * The following rules exist:
     * <ul>
     *   <li>If {@link View#getTabNode() View.tabNodeCallback} is set and returns a {@link Node},
     *   this node is used.</li>
     *   <li>Otherwise, a {@link Label} is created that is decorated with the {@link
     *   View#getTabLabel() View.tabLabel}, {@link View#getTabStyle() View.tabStyle} and {@link
     *   View#getTabGraphic() View.tabGraphic}</li>
     * </ul>
     */
    protected void onTabControlChanged() {
      Node node =
          view.getTabNode() != null ? view.getTabNode().call(view.getViewGroup()
              .getSide()) : null;
      if (node != null) {
        control = node;
        label = null;
      } else {
        if (this.label == null) {
          this.label = new Label();
        }
        control = label;
        this.label.setText(view.getTabLabel());
        this.label.setStyle(view.getTabStyle());
        this.label.setGraphic(view.getTabGraphic());
        this.label.setTooltip(view.getTabTooltip());
        this.label.setContextMenu(view.getTabContextMenu());
        rotate(this.label.getGraphic());
      }
      HBox.setHgrow(control, Priority.ALWAYS);
      updateChildren();
    }

    /**
     * Called, when settings regarding the maximization are changed.
     */
    protected void onMaximizeChanged() {
      ViewGroupContainer root = Utils.getRootViewGroupContainer(view.getContent()).orElse(null);
      if (root == null) {
        return;
      }

      if (view.isMaximized() && view.isMaximizable()) {
        root.maximizeView(view);
      } else {
        root.restoreView(view);
      }
    }


    /**
     * Called when the visibility of the action buttons has been changed.
     * <p>
     * Note that this creates the buttons for the close and maximize action, if they are not generally disabled. It depends on the current
     * state of the view and the mouse pointer position, whether they are actually visible.
     */
    protected void onTabActionVisibilityChanged() {
      if (view.getTabCloseActionVisibility() == null
          || view.getTabCloseActionVisibility() == TabActionVisibility.NEVER) {
        closeButton = null;
      } else if (closeButton == null) {
        closeButton = createCloseButton();
      }

      if (view.getTabMaximizeActionVisibility() == null
          || view.getTabMaximizeActionVisibility() == TabActionVisibility.NEVER
          || !view.isMaximizable()) {
        maximizeButton = null;
      } else if (maximizeButton == null) {
        maximizeButton = createMaximizeButton();
      }
      updateChildren();
    }

    /**
     * Updates the children of this component.
     * <p>
     * It adds for sure the control (which is either a {@link Label} or the result of the {@link View#getTabNode() View.tabNodeCallback} and
     * depending one the current state the action buttons
     */
    protected void updateChildren() {
      List<Node> children = getChildren();
      children.clear();
      children.add(control);
      if (maximizeButton != null && isActionVisible(view.getTabMaximizeActionVisibility())) {
        children.add(maximizeButton);
      }
      if (closeButton != null && isActionVisible(view.getTabCloseActionVisibility())) {
        children.add(closeButton);
      }
      requestLayout();
    }

    private void onDraggingChanged() {
      pseudoClassStateChanged(PSEUDO_CLASS_DRAGGING, view.isDragging());
    }

    /**
     * Creates a button for the close action
     *
     * @return The button
     */
    protected Button createCloseButton() {
      Button btn = new Button();
      btn.getStyleClass().addAll(CLASS_VIEW_GROUP_TAB_BUTTON, CLASS_VIEW_GROUP_TAB_BUTTON_CLOSE);
      btn.setOnAction(event -> handleClose(view));
      btn.setPrefHeight(control.prefHeight(-1));
      btn.setMinHeight(control.prefHeight(-1));
      return btn;
    }

    private void handleClose(View view) {
      if (view.canClose()) {
        view.close();
      }
    }

    /**
     * Creates a button for the maximize action.
     *
     * @return The button
     */
    protected Button createMaximizeButton() {
      Button btn = new Button();
      btn.getStyleClass().addAll(CLASS_VIEW_GROUP_TAB_BUTTON, CLASS_VIEW_GROUP_TAB_BUTTON_MAXIMIZE);
      btn.setOnAction(event -> view.setMaximized(true));
      btn.setPrefHeight(control.prefHeight(-1));
      btn.setMinHeight(control.prefHeight(-1));
      return btn;
    }

    /**
     * Checks, whether the given {@code visibility} indicates that the action should be visible.
     *
     * @param visibility The {@link TabActionVisibility}
     * @return {@code true}, if the corresponding button should be visible.
     */
    protected boolean isActionVisible(TabActionVisibility visibility) {
      return switch (visibility) {
        case ALWAYS -> true;
        case NEVER -> false;
        case SELECTED -> getPseudoClassStates().contains(PSEUDO_CLASS_SELECTED);
        case MOUSE_OVER -> getPseudoClassStates().contains(PSEUDO_CLASS_HOVER);
      };
    }

    private void rotate(Node node) {
      if (node == null) {
        return;
      }
      Bounds bounds = node.getBoundsInLocal();
      node.getTransforms().clear();
      switch (getEffectiveSide()) {
        case LEFT -> node.getTransforms().addAll(
            new Translate(bounds.getMaxX(), -bounds.getMaxY()),
            new Rotate(90)
        );
        case RIGHT -> node.getTransforms().addAll(
            new Translate(bounds.getMaxY(), bounds.getMaxX()),
            new Rotate(-90)
        );
      }
    }

    @Override
    protected void layoutChildren() {
      double ti = snappedTopInset();
      double li = snappedLeftInset();
      double w = getWidth() - li - snappedRightInset();
      double h = getHeight() - ti - snappedBottomInset();
      double cw;

      // Close button is right and highest priority
      if (closeButton != null && isActionVisible(view.getTabCloseActionVisibility())) {
        cw = Utils.ensureValueIsBetween(closeButton.prefWidth(h), 0, Math.max(w, 0));
        w -= cw;
        layoutInArea(closeButton, li + w, ti, cw, h, -1, HPos.CENTER, VPos.CENTER);
        w -= SPACING;
      }

      // Control is left and next priority
      cw = Utils.ensureValueIsBetween(control.prefWidth(h), 0, Math.max(w, 0));
      layoutInArea(control, li, ti, cw, h, -1, HPos.CENTER, VPos.CENTER);
      w -= cw;
      li += cw;

      // Maximize button is middle, lowest priority
      if (maximizeButton != null && isActionVisible(view.getTabMaximizeActionVisibility())) {
        li += SPACING;
        w -= SPACING;
        cw = Utils.ensureValueIsBetween(maximizeButton.prefWidth(h), 0, Math.max(w, 0));
        w -= cw;
        layoutInArea(maximizeButton, li + w, ti, cw, h, -1, HPos.CENTER, VPos.CENTER);
      }
    }

    @Override
    protected double computePrefWidth(double height) {
      return computeWidth(height, SizeType.PREF);
    }

    @Override
    protected double computePrefHeight(double width) {
      return computeHeight(width, SizeType.PREF);
    }

    @Override
    protected double computeMinWidth(double height) {
      return computeWidth(height, SizeType.MIN);
    }

    @Override
    protected double computeMinHeight(double width) {
      return computeHeight(width, SizeType.MIN);
    }

    @Override
    protected double computeMaxWidth(double height) {
      return computeWidth(height, SizeType.MAX);
    }

    @Override
    protected double computeMaxHeight(double width) {
      return computeHeight(width, SizeType.MAX);
    }

    private double computeWidth(double height, SizeType type) {
      double childWidth = snapSizeY(getManagedChildren().stream()
          .mapToDouble(n -> type.getWidth(n, height))
          .sum());
      double computed = childWidth + snappedLeftInset() + snappedRightInset()
          + (getManagedChildren().size() - 1) * SPACING;
      return Utils.ensureValueIsBetween(
          computed,
          getSkinnable().getTabMinWidth(),
          getSkinnable().getTabMaxWidth());
    }

    private double computeHeight(double width, SizeType type) {
      // Calculated based and the max. height of the children
      double childHeight = snapSizeY(getManagedChildren().stream()
          .mapToDouble(n -> type.getHeight(n, width))
          .max()
          .orElse(0));
      return childHeight + snappedTopInset() + snappedBottomInset();
    }
  }

  /**
   * Component highlighting a {@link DropTarget} during a drag and drop operation.
   */
  protected static class DropTargetBox extends Pane {

    /**
     * Constructor
     */
    public DropTargetBox() {
      getStyleClass().add(CLASS_DROP_TARGET);
    }
  }
}
