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

import de.hipphampel.mv4fx.utils.Range;
import de.hipphampel.mv4fx.utils.Utils;
import de.hipphampel.mv4fx.view.Constants;
import de.hipphampel.mv4fx.view.GroupOrContainer;
import de.hipphampel.mv4fx.view.ViewGroupContainer;
import de.hipphampel.mv4fx.view.ViewGroupContainer.DividerDragMode;
import de.hipphampel.mv4fx.view.ViewGroupContainer.ResizePolicy;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Default skin of the {@link ViewGroupContainer}
 */
public class ViewGroupContainerSkin extends SkinBase<ViewGroupContainer> {

  private final ChangeListener<Number> sizeChanged;
  private final ChangeListener<Insets> insetsChanged;
  private final InvalidationListener childrenChanged;
  private final InvalidationListener orientationChanged;
  private final InvalidationListener positionChanged;
  private final InvalidationListener absolutePositionChanged;
  private Divider divider;
  private boolean updatingPosition;

  /**
   * Constructor
   *
   * @param control The control
   */
  public ViewGroupContainerSkin(ViewGroupContainer control) {
    super(control);
    this.childrenChanged = observable -> onChildrenChanged();
    this.sizeChanged = this::onSizeChanged;
    this.insetsChanged = this::onInsetsChanged;
    this.positionChanged = observable -> onPositionChanged();
    this.absolutePositionChanged = observable -> onAbsolutePositionChanged();
    this.orientationChanged = observable -> onOrientationChanged();
    control.insetsProperty().addListener(insetsChanged);
    control.widthProperty().addListener(sizeChanged);
    control.heightProperty().addListener(sizeChanged);
    control.dividerSizeProperty().addListener(sizeChanged);
    control.leftTopProperty().addListener(childrenChanged);
    control.rightBottomProperty().addListener(childrenChanged);
    control.positionProperty().addListener(positionChanged);
    control.absolutePositionProperty().addListener(absolutePositionChanged);
    control.orientationProperty().addListener(orientationChanged);
    control.maximizedViewProperty().addListener(childrenChanged);
    onChildrenChanged();
  }

  /**
   * Gets the associated {@link Divider}
   *
   * @return The {@code Divider}, might be {@code null}
   */
  protected Divider getDivider() {
    return divider;
  }

  @Override
  public void dispose() {
    ViewGroupContainer control = getSkinnable();
    control.maximizedViewProperty().removeListener(childrenChanged);
    control.orientationProperty().removeListener(orientationChanged);
    control.absolutePositionProperty().removeListener(absolutePositionChanged);
    control.positionProperty().removeListener(positionChanged);
    control.rightBottomProperty().removeListener(childrenChanged);
    control.leftTopProperty().removeListener(childrenChanged);
    control.dividerSizeProperty().removeListener(sizeChanged);
    control.heightProperty().removeListener(sizeChanged);
    control.widthProperty().removeListener(sizeChanged);
    control.insetsProperty().removeListener(insetsChanged);
    if (divider != null) {
      divider.dispose();
    }
    super.dispose();
  }

  /**
   * Called when the size changes.
   * <p>
   * Recalculates the position of the divider, depending on the resizeing policiy
   *
   * @param trigger  Obseverable triggering the event
   * @param oldValue The old value
   * @param newValue The new value
   */
  protected void onSizeChanged(Observable trigger, Number oldValue, Number newValue) {
    ViewGroupContainer container = getSkinnable();
    doIfNotUpdatingPosition(() -> {
      if (trigger == container.dividerSizeProperty()) {
        updateDividerPosition(
            getAbsolutePositionRange(null, null, oldValue.doubleValue()),
            oldValue.doubleValue());
      } else if (container.getOrientation() == Orientation.VERTICAL) {
        if (trigger == container.heightProperty()) {
          updateDividerPosition(
              getAbsolutePositionRange(null, oldValue.doubleValue(), null),
              container.getDividerSize());
        }
      } else {
        if (trigger == container.widthProperty()) {
          updateDividerPosition(
              getAbsolutePositionRange(null, oldValue.doubleValue(), null),
              container.getDividerSize());
        }
      }
    });
  }

  /**
   * Called when the padding/insets has been changed,
   * <p>
   * Bsically, relayouts the component.
   */
  protected void onInsetsChanged(Observable trigger, Insets oldValue, Insets newValue) {
    doIfNotUpdatingPosition(
        () -> updateDividerPosition(
            getAbsolutePositionRange(oldValue, null, null),
            getSkinnable().getDividerSize()));
  }

  /**
   * Called when the orientation has been changed,
   * <p>
   * Bsically, relayouts the component.
   */
  protected void onOrientationChanged() {
    doIfNotUpdatingPosition(() -> updateDividerPosition(
        getAbsolutePositionRange(null, null, null),
        getSkinnable().getDividerSize()));
  }

  /**
   * Called when the absolute position has been changed.
   * <p>
   * Recaluclates the (relative) position and relayouts.
   */
  protected void onAbsolutePositionChanged() {
    ViewGroupContainer container = getSkinnable();
    doIfNotUpdatingPosition(() ->
        container.setPosition(computePosition(container.getAbsolutePosition()))
    );
  }

  /**
   * Called when the (relative) position has been changed.
   * <p>
   * Recaluclates the absolute position and relayouts.
   */
  protected void onPositionChanged() {
    ViewGroupContainer container = getSkinnable();
    doIfNotUpdatingPosition(() ->
        container.setAbsolutePosition(computeAbsolutePosition(container.getPosition()))
    );
  }

  /**
   * Called when the children have been changed.
   * <p>
   * This is called whenever then left/top or right/bottom component has been changed. Depending on the change, a divider is displayed or
   * not. It relayouts ths container
   */
  protected void onChildrenChanged() {
    ViewGroupContainer control = getSkinnable();
    ObservableList<Node> children = getChildren();

    children.clear();

    if (control.getMaximizedView() == null) {
      onChildrenChangedNormal(control, children);
    } else {
      onChildrenChangedMaximized(control, children);
    }

    control.requestLayout();
  }

  private void onChildrenChangedMaximized(ViewGroupContainer control, ObservableList<Node> children) {
    children.add(control.getMaximizedView().getContent());
  }

  private void onChildrenChangedNormal(ViewGroupContainer control, ObservableList<Node> children) {

    Control leftTop = getControl(control.getLeftTop());
    if (leftTop != null) {
      children.add(leftTop);
    }

    Control rightBottom = getControl(control.getRightBottom());
    if (rightBottom != null) {
      children.add(rightBottom);
    }

    if (leftTop != null && rightBottom != null) {
      if (divider == null) {
        divider = newDivider();
      }
      children.add(divider);
    } else if (divider != null) {
      divider.dispose();
      divider = null;
    }
  }

  /**
   * Computes the  position.
   * <p>
   * The method is called whenever the absolute position has been changed, based on this information the  position is recalculated.
   *
   * @param absolutePosition The current absolute position
   * @return The relative position
   */
  protected double computePosition(double absolutePosition) {
    Range range = getAbsolutePositionRange(null, null, null);
    return Math.max(absolutePosition - range.lower(), 0) / Math.max(1, range.size());
  }

  /**
   * Computes the absolute position.
   * <p>
   * The method is called whenever the position has been changed, based on this information the absolute position is recalculated.
   *
   * @param position The current (relative) position
   * @return The absolute position
   */
  protected double computeAbsolutePosition(double position) {
    Range range = getAbsolutePositionRange(null, null, null);
    return Math.max(0,
        range.lower() + Math.max(0, range.size()) * Utils.ensureValueIsBetween(position, 0, 1));
  }

  @Override
  protected double computeMinWidth(double height, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return 0;
  }

  @Override
  protected double computeMinHeight(double width, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return 0;
  }

  @Override
  protected double computePrefWidth(double height, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return 600;
  }

  @Override
  protected double computePrefHeight(double width, double topInset, double rightInset,
      double bottomInset, double leftInset) {
    return 600;
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    ViewGroupContainer container = getSkinnable();
    if (container == null) {
      return;
    }
    if (container.getMaximizedView() == null) {
      layoutChildrenNormal(container, x, y, w, h);
    } else {
      layoutChildrenMaximized(container, x, y, w, h);
    }
  }

  private void layoutChildrenMaximized(ViewGroupContainer container, double x, double y, double w, double h) {
    Node control = container.getMaximizedView().getContent();
    control.relocate(x, y);
    control.resize(w, h);
  }

  private void layoutChildrenNormal(ViewGroupContainer container, double x, double y, double w, double h) {
    double position = container.getAbsolutePosition();
    if (position == Region.USE_COMPUTED_SIZE) {
      position = computeAbsolutePosition(container.getPosition());
    }

    Control leftTop = getControl(container.getLeftTop());
    Control rightBottom = getControl(container.getRightBottom());

    if (leftTop == null && rightBottom == null) {
      return;
    }
    if (leftTop == null) {
      // Only rightBottom is visible
      rightBottom.relocate(x, y);
      rightBottom.resize(w, h);
      return;
    }

    if (rightBottom == null) {
      // Only leftTop is visible
      leftTop.relocate(x, y);
      leftTop.resize(w, h);
      return;
    }

    Orientation orientation = container.getOrientation();
    double dividerSize = container.getDividerSize();
    if (orientation == Orientation.VERTICAL) {
      leftTop.relocate(x, y);
      leftTop.resize(w, position - y);
      divider.relocate(x, position);
      divider.resize(w, dividerSize);
      rightBottom.relocate(x, position + dividerSize);
      rightBottom.resize(w, h + y - position - dividerSize);
    } else {
      leftTop.relocate(x, y);
      leftTop.resize(position - x, h);
      divider.relocate(position, y);
      divider.resize(dividerSize, h);
      rightBottom.relocate(position + dividerSize, y);
      rightBottom.resize(w + x - position - dividerSize, h);
    }
  }

  private void updateDividerPosition(Range oldPositionRange, double oldDividerSize) {
    ViewGroupContainer container = getSkinnable();
    if (container.getAbsolutePosition() == Region.USE_COMPUTED_SIZE) {
      container.setAbsolutePosition(computeAbsolutePosition(container.getPosition()));
      return;
    }

    ResizePolicy policy = container.getResizePolicy() == null ? ResizePolicy.KEEP_RATIO
        : container.getResizePolicy();
    Range positionRange = getAbsolutePositionRange(null, null, null);

    switch (policy) {
      case KEEP_LEFT_TOP_SIZE: {
        double leftTopSize = container.getAbsolutePosition() - oldPositionRange.lower();
        double ratio = Utils.ensureValueIsBetween(
            leftTopSize / Math.max(1, positionRange.size()),
            0,
            1);
        container.setPosition(ratio);
        container.setAbsolutePosition(
            positionRange.lower() + Math.max(0, positionRange.size()) * ratio);
        break;
      }
      case KEEP_RIGHT_BOTTOM_SIZE: {
        double rightBottomSize =
            oldPositionRange.size() - oldDividerSize - container.getAbsolutePosition();
        double ratio = Utils.ensureValueIsBetween(
            (positionRange.size() - rightBottomSize - container.getDividerSize()) / Math.max(1,
                positionRange.size()),
            0,
            1);
        container.setPosition(ratio);
        container.setAbsolutePosition(
            positionRange.lower() + Math.max(0, positionRange.size()) * ratio);
      }
      case KEEP_RATIO: {
        double ratio = Utils.ensureValueIsBetween(container.getPosition(), 0, 1);
        container.setAbsolutePosition(
            positionRange.lower() + Math.max(0, positionRange.size()) * ratio);
        break;
      }
      default:
        throw new IllegalArgumentException("Unsupported policy!");
    }
    container.requestLayout();
  }

  private void doIfNotUpdatingPosition(Runnable effect) {
    if (updatingPosition) {
      return;
    }
    try {
      updatingPosition = true;
      effect.run();
    } finally {
      updatingPosition = false;
    }
  }

  private Range getAbsolutePositionRange(Insets presetInsets, Double presetSize,
      Double presetDividerSize) {
    ViewGroupContainer container = getSkinnable();
    double lt;
    double rb;
    double size;
    double dividerSize = presetDividerSize == null ? container.getDividerSize() : presetDividerSize;
    if (container.getOrientation() == Orientation.VERTICAL) {
      lt = presetInsets == null ? container.snappedTopInset() :
          container.snapPositionX(presetInsets.getTop());
      rb = presetInsets == null ? container.snappedBottomInset() :
          container.snapPositionX(presetInsets.getBottom());
      size = presetSize == null ? container.getHeight() : presetSize;
    } else {
      lt = presetInsets == null ? container.snappedLeftInset() :
          container.snapPositionY(presetInsets.getLeft());
      rb = presetInsets == null ? container.snappedRightInset() :
          container.snapPositionY(presetInsets.getRight());
      size = presetSize == null ? container.getWidth() : presetSize;
    }
    return new Range(lt, size - rb - dividerSize);
  }

  private Control getControl(GroupOrContainer goc) {
    if (goc == null) {
      return null;
    }
    return goc.asControl();
  }

  /**
   * Factory method to create a new {@link Divider}
   *
   * @return The {@code Divider}
   */
  protected Divider newDivider() {
    return new Divider();
  }

  /**
   * The divider.
   * <p>
   * The divider appears if the container hosts two children. The divider allows to change the size of the children by dragging it with the
   * mouse
   */
  protected class Divider extends Pane {

    private Double dragStart;

    /**
     * Constructor.
     */
    public Divider() {
      getStyleClass().add(Constants.CLASS_VIEW_GROUP_CONTAINER_DIVIDER);
      setOnMouseEntered(this::onMouseEntered);
      setOnMouseExited(this::onMouseExited);
      setOnMouseDragged(this::onMouseDragged);
      setOnMousePressed(this::onMousePressed);
      setOnMouseReleased(this::onMouseReleased);
    }

    /**
     * Dispose the instance.
     * <p>
     * Removes all listeners
     */
    public void dispose() {
      setOnMouseEntered(null);
      setOnMouseExited(null);
      setOnMouseDragged(null);
      setOnMousePressed(null);
      setOnMouseReleased(null);
    }

    /**
     * Called when the mouse enters the divider.
     * <p>
     * The pointer is changed to drag indicator in case dragging is possible.
     *
     * @param event The causing event
     */
    protected void onMouseEntered(MouseEvent event) {
      Range dragRange = getDragRange();
      setDragCursor(dragRange != null);
    }

    /**
     * Called when the mouse exits the divider.
     * <p>
     * In case that no drag operation is in progress, the pointer is reset to normal.
     *
     * @param event The causing event
     */
    protected void onMouseExited(MouseEvent event) {
      setDragCursor(dragStart == null);
    }

    /**
     * Called when the mouse is dragged.
     * <p>
     * Basically updates the position of the divider, if possible.
     *
     * @param event The causing event
     */
    protected void onMouseDragged(MouseEvent event) {
      ViewGroupContainer container = getSkinnable();
      setDragCursor(dragStart != null);
      if (dragStart == null) {
        return;
      }
      Range dragRange = getDragRange();
      if (dragRange == null || dragRange.isEmpty()) {
        return;
      }
      double newPos = getRelevantMousePosition(event, true) - dragStart;
      if (container.getDividerDragMode() == DividerDragMode.RESPECT_MIN_SIZE && !dragRange.contains(newPos)) {
        return;
      }
      getSkinnable().setAbsolutePosition(
          Utils.ensureValueIsBetween(newPos, dragRange.lower(), dragRange.upper()));
      requestLayout();
    }

    /**
     * Called when the mouse button is pressed.
     * <p>
     * If the primary button is pressen, it starts the drag mode, the cursor is set to drag mode anyway.
     *
     * @param event The event
     */
    protected void onMousePressed(MouseEvent event) {
      Range dragRange = getDragRange();
      setDragCursor(dragRange != null);
      if (!event.isPrimaryButtonDown() || dragRange == null) {
        return;
      }
      dragStart = getRelevantMousePosition(event, false);
    }

    /**
     * Called when the mouse button is released.
     * <p>
     * Resets the cursor to the default, if the mouse pointer is not longer inside the divider and stops any kind of dragging.
     *
     * @param event The event
     */
    protected void onMouseReleased(MouseEvent event) {
      setDragCursor(getBoundsInLocal().contains(event.getX(), event.getY()));
      if (!event.isPrimaryButtonDown()) {
        return;
      }
      dragStart = null;
    }

    private double getRelevantMousePosition(MouseEvent event, boolean translateToParent) {
      Point2D pt = new Point2D(event.getX(), event.getY());
      if (translateToParent) {
        pt = getLocalToParentTransform().transform(pt);
      }
      return getSkinnable().getOrientation() == Orientation.VERTICAL ? pt.getY() : pt.getX();
    }

    /**
     * Helper to set the cursor to indicate dragging or not.
     *
     * @param indicateDragging {@code true}, if indicate dragging
     */
    protected void setDragCursor(boolean indicateDragging) {
      Cursor cursor = Cursor.DEFAULT;
      if (indicateDragging) {
        Orientation orientation = getSkinnable().getOrientation();
        cursor = orientation == Orientation.VERTICAL ? Cursor.V_RESIZE : Cursor.H_RESIZE;
      }
      setCursor(cursor);
    }

    /**
     * Calculates the range of the possible drag positions.
     * <p>
     * This is calculated based on the current side of the container and the
     * {@link ViewGroupContainer#dividerDragModeProperty() dividerDragMode}. The method returns {@code null} if the divider is not
     * draggable.
     *
     * @return The range
     */
    protected Range getDragRange() {
      ViewGroupContainer container = getSkinnable();
      DividerDragMode mode = container.getDividerDragMode();
      if (mode == DividerDragMode.FIXED) {
        return null;
      }

      Range range = getAbsolutePositionRange(null, null, null);
      if (mode == DividerDragMode.FREE) {
        return range;
      }

      if (container.getOrientation() == Orientation.VERTICAL) {
        double w = container.getWidth();
        return new Range(
            range.lower() + Math.max(0, container.getLeftTop().asControl().minHeight(w)),
            range.upper() - Math.max(0, container.getRightBottom().asControl().minHeight(w)));
      } else {
        double h = container.getHeight();
        return new Range(
            range.lower() + Math.max(0, container.getLeftTop().asControl().minWidth(h)),
            range.upper() - Math.max(0, container.getRightBottom().asControl().minWidth(h)));
      }
    }
  }
}
