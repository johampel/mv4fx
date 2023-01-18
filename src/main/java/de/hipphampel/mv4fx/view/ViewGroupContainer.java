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

import de.hipphampel.mv4fx.utils.LayoutRequestingStyleableObjectProperty;
import de.hipphampel.mv4fx.utils.StyleableSizeProperty;
import de.hipphampel.mv4fx.view.skin.ViewGroupContainerSkin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Container that holds one or two {@link ViewGroup ViewGroups} or {@code ViewGroupContainers}.
 * <p>
 * This control is similar to a {@code SplitPane}, witht the following exceptions:
 * <ul>
 * <li>It contains at most two sub components</li>
 * <li>The sub components are either {@code ViewGroups} or {@code ViewGroupContainers}</li>
 * <li>If there is only one child, this single child occupies the entire area</li>
 * <li>There are styles/properties to control the resizing and drag &amp; drop behaviour</li>
 * </ul>
 * <p>
 * The {@code ViewGroupContainer} provides several CSS styles to customize the behaviour and appearance:
 * <ul>
 *   <li>{@code -mv4fx-orientation}: defines the orientation of the children, if there are two of them
 *   possible values are {@code vertical} and {@code horizontal}, whereas {@code horizontal} is the
 *   default</li>
 *   <li>{@code -mv4fx-divider-drag-mode}: defines the mode how to drag the divider that sits between
 *   the left/top and right/bottom component. The modes affects only the position change of the divider
 *   by dragging it. It is always possible to change the position of the divider programmatically.
 *   The following modes are known:
 *   <ul>
 *     <li>{@code fixed} - the divider cannot be dragged by the user</li>
 *     <li>{@code free} - the divider can be positioned freely without any restriction</li>
 *     <li>{@code respect-min-size} - the divider cannot be dragged to a position that underflows
 *     the minimum size of a child</li>
 *   </ul>
 *   The default is {@code free}</li>
 *   <li>{@code -mv4fx-divider-size}: The size of the divider (height or width, depending on the
 *   orientation (default is 5)</li>
 *   <li>{@code -mv4fx-resize-policy}: Describes how resizes the components, if the container is
 *   resized. The following values are known:
 *   <ul>
 *     <li>{@code keep-ratio}: The children are resized so that the ratio of them stays the same</li>
 *     <li>{@code keep-left-top-size}: Only the right/bottom child is adapted</li>
 *     <li>{@code keep-right-bottom-size}: Only the left/top child is adapted</li>
 *   </ul>
 *   (default is {@code keep-ratio})</li>
 * </ul>
 */
public class ViewGroupContainer extends Control implements GroupOrContainer {

  /**
   * Enumeration describing how the divider might be dragged by the user.
   * <p>
   * These constants describe just the way how the divider can be dragged; in generaö it can be positioned programmatically without any
   * restriction.
   */
  public enum DividerDragMode {

    /**
     * The divider cannot be dragged/moved by the user
     */
    FIXED,

    /**
     * The divider can be dragged freely around, even so that one side is empty
     */
    FREE,

    /**
     * If possible, the divider cannot dragged so that the the min size of the components is not underflowed. In case that the container is
     * smaller than the sum of the groups, the divider is effectively fixed then.
     */
    RESPECT_MIN_SIZE
  }

  /**
   * Enumeration describing whats happens when the container is resized.
   */
  public enum ResizePolicy {

    /**
     * When resizing, try to keep the size of the left/top component. If the size of the container becomes less then this left/top
     * component, the component is shrinked as well.
     */
    KEEP_LEFT_TOP_SIZE,

    /**
     * When resizing, try to keep the size of the roght/bottom component. If the size of the container becomes less then this right/vbottom
     * component, the component is shrinked as well.
     */
    KEEP_RIGHT_BOTTOM_SIZE,

    /**
     * When resizing, keep the ratio between the left/top and right/bottom component.
     */
    KEEP_RATIO
  }

  // Public constants
  public static final String PROPERTY_LEFT_TOP = "leftTop";
  public static final String PROPERTY_RIGHT_BOTTOM = "rightBottom";
  public static final String PROPERTY_ORIENTATION = "orientation";
  public static final String PROPERTY_DIVIDER_DRAG_MODE = "dividerDragMode";
  public static final String PROPERTY_DIVIDER_SIZE = "dividerSize";
  public static final String PROPERTY_POSITION = "position";
  public static final String PROPERTY_ABSOLUTE_POSITION = "absolutePosition";
  public static final String PROPERTY_RESIZE_POLICY = "resizePolicy";
  public static final String PROPERTY_MAXIMIZED_VIEW = "maximizedView";

  // Style definition stuff
  private static final StyleablePropertyFactory<ViewGroupContainer> FACTORY = new StyleablePropertyFactory<>(
      Control.getClassCssMetaData());
  private static final CssMetaData<ViewGroupContainer, Orientation> ORIENTATION = FACTORY.createEnumCssMetaData(
      Orientation.class, "-mv4fx-orientation", s -> s.orientation,
      Orientation.HORIZONTAL, false);
  private static final CssMetaData<ViewGroupContainer, DividerDragMode> DIVIDER_DRAG_MODE = FACTORY.createEnumCssMetaData(
      DividerDragMode.class, "-mv4fx-divider-drag-mode", s -> s.dividerDragMode,
      DividerDragMode.FREE, false);
  private static final CssMetaData<ViewGroupContainer, Number> DIVIDER_SIZE = FACTORY.createSizeCssMetaData(
      "-mv4fx-divider-size", s -> s.dividerSize, 5);
  private static final CssMetaData<ViewGroupContainer, ResizePolicy> RESIZE_POLICY = FACTORY.createEnumCssMetaData(
      ResizePolicy.class, "-mv4fx-resize-policy", s -> s.resizePolicy,
      ResizePolicy.KEEP_RATIO, false);
  private final static List<CssMetaData<? extends Styleable, ?>> CLASS_CSS_META_DATA;

  static {
    List<CssMetaData<? extends Styleable, ?>> own = new ArrayList<>(Control.getClassCssMetaData());
    own.add(DIVIDER_DRAG_MODE);
    own.add(DIVIDER_SIZE);
    own.add(RESIZE_POLICY);
    CLASS_CSS_META_DATA = Collections.unmodifiableList(own);
  }

  /**
   * Gets the CSS meta data of this class.
   *
   * @return CSS meta data.
   */
  public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
    return CLASS_CSS_META_DATA;
  }

  private final ObjectProperty<GroupOrContainer> leftTop;
  private final ObjectProperty<GroupOrContainer> rightBottom;
  private final ObjectProperty<View> maximizedView;
  private final LayoutRequestingStyleableObjectProperty<Orientation> orientation;
  private final LayoutRequestingStyleableObjectProperty<DividerDragMode> dividerDragMode;
  private final StyleableSizeProperty dividerSize;
  private final DoubleProperty position;
  private final DoubleProperty absolutePosition;
  private final LayoutRequestingStyleableObjectProperty<ResizePolicy> resizePolicy;

  /**
   * Constructor.
   */
  public ViewGroupContainer() {
    this.getStylesheets().add(Constants.CSS_URL);
    this.getStyleClass().add("view-group-container");
    this.leftTop = new SimpleObjectProperty<>(this, PROPERTY_LEFT_TOP);
    this.rightBottom = new SimpleObjectProperty<>(this, PROPERTY_RIGHT_BOTTOM);
    this.orientation = new LayoutRequestingStyleableObjectProperty<>(this, PROPERTY_ORIENTATION,
        ORIENTATION);
    this.dividerDragMode = new LayoutRequestingStyleableObjectProperty<>(this,
        PROPERTY_DIVIDER_DRAG_MODE,
        DIVIDER_DRAG_MODE);
    this.dividerSize = new StyleableSizeProperty(this, PROPERTY_DIVIDER_SIZE, DIVIDER_SIZE);
    this.position = new SimpleDoubleProperty(this, PROPERTY_POSITION, 0.5);
    this.absolutePosition = new SimpleDoubleProperty(this, PROPERTY_ABSOLUTE_POSITION,
        USE_COMPUTED_SIZE);
    this.resizePolicy = new LayoutRequestingStyleableObjectProperty<>(this, PROPERTY_RESIZE_POLICY,
        RESIZE_POLICY);
    this.maximizedView = new SimpleObjectProperty<>(this, PROPERTY_MAXIMIZED_VIEW);
  }

  @Override
  public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
    return CLASS_CSS_META_DATA;
  }

  /**
   * Convenience getter for the {@code leftTop} property.
   * <p>
   * See documentation of {@link #leftTopProperty() leftTop} property for details.
   *
   * @return The current value
   */
  public GroupOrContainer getLeftTop() {
    return leftTop.get();
  }

  /**
   * The {@code leftTop} property.
   * <p>
   * This property contains the component to show in the left or top area of the container. In case it is the only one, it occupies the
   * entire space of the container
   *
   * @return The property
   */
  public ObjectProperty<GroupOrContainer> leftTopProperty() {
    return leftTop;
  }

  /**
   * Convenience setter for the {@code leftTop} property.
   * <p>
   * See documentation of {@link #leftTopProperty() leftTop} property for details.
   *
   * @param value The new value
   */
  public void setLeftTop(GroupOrContainer value) {
    this.leftTop.set(value);
  }

  /**
   * Convenience getter for the {@code rightBottom} property.
   * <p>
   * See documentation of {@link #rightBottomProperty() rightBottom} property for details.
   *
   * @return The current value
   */
  public GroupOrContainer getRightBottom() {
    return rightBottom.get();
  }

  /**
   * The {@code rightBottom} property.
   * <p>
   * This property contains the component to show in the right or bottom area of the container. In case it is the only one, it occupies the
   * entire space of the container
   *
   * @return The property
   */
  public ObjectProperty<GroupOrContainer> rightBottomProperty() {
    return rightBottom;
  }

  /**
   * Convenience setter for the {@code rightBottom} property.
   * <p>
   * See documentation of {@link #rightBottomProperty() rightBottom} property for details.
   *
   * @param value The new value
   */
  public void setRightBottom(GroupOrContainer value) {
    this.rightBottom.set(value);
  }


  /**
   * Convenience getter for the {@code orientation} property.
   * <p>
   * See documentation of {@link #orientationProperty() orientation} property for details.
   *
   * @return The current value
   */
  public Orientation getOrientation() {
    return orientation.get();
  }


  /**
   * The {@code orientation} property.
   * <p>
   * This property controls, whether to divide the two controls horizontal or vertical.
   *
   * @return The property
   */
  public ObjectProperty<Orientation> orientationProperty() {
    return orientation;
  }

  /**
   * Convenience setter for the {@code orientation} property.
   * <p>
   * See documentation of {@link #orientationProperty() orientation} property for details.
   *
   * @param value The new value
   */
  public void setOrientation(Orientation value) {
    this.orientation.set(value);
  }

  /**
   * Convenience getter for the {@code dividerDragMode} property.
   * <p>
   * See documentation of {@link #dividerDragModeProperty() dividerDragMode} property for details.
   *
   * @return The current value
   */
  public DividerDragMode getDividerDragMode() {
    return dividerDragMode.getValue();
  }

  /**
   * The {@code dividerDragMode} property.
   * <p>
   * This property controls, how the user might drag the divider. The {@link DividerDragMode} for the available values
   *
   * @return The property
   */
  public ObjectProperty<DividerDragMode> dividerDragModeProperty() {
    return dividerDragMode;
  }

  /**
   * Convenience setter for the {@code dividerDragMode} property.
   * <p>
   * See documentation of {@link #dividerDragModeProperty() dividerDragMode} property for details.
   *
   * @param value The new value
   */
  public void setDividerDragMode(DividerDragMode value) {
    dividerDragMode.setValue(value);
  }

  /**
   * Convenience getter for the {@code dividerSize} property.
   * <p>
   * See documentation of {@link #dividerSizeProperty() dividerSize} property for details.
   *
   * @return The current value
   */
  public double getDividerSize() {
    return dividerSize.get();
  }

  /**
   * The {@code dividerSize} property.
   * <p>
   * This property defines the size of the divider, so its height, when orientation is vertical or its width, if the orientation is
   * horizontal
   *
   * @return The property
   */
  public StyleableSizeProperty dividerSizeProperty() {
    return dividerSize;
  }

  /**
   * Convenience setter for the {@code dividerSize} property.
   * <p>
   * See documentation of {@link #dividerSizeProperty() dividerSize} property for details.
   *
   * @param value The new value
   */
  public void setDividerSize(double value) {
    this.dividerSize.set(value);
  }

  /**
   * Convenience getter for the {@code position} property.
   * <p>
   * See documentation of {@link #positionProperty() position} property for details.
   *
   * @return The current value
   */
  public double getPosition() {
    return position.get();
  }

  /**
   * The {@code position} property.
   * <p>
   * This property contains the relative position of the divider. If its value is {@code 0.0}, the left/top component occupies {@code 0%}
   * and the right/bottom {@code 100%} of the available space; a value of {@code 0.5} means that both components occupy the same amount.
   * <p>
   * In order to get or set the absolute position of the divider, use {@link #absolutePositionProperty()}. Note that {@code position} and
   * {@code absolutePosition} depend each other - modification on one of them leads to a modification of the other
   *
   * @return The property.
   */
  public DoubleProperty positionProperty() {
    return position;
  }

  /**
   * Convenience setter for the {@code position} property.
   * <p>
   * See documentation of {@link #positionProperty() position} property for details.
   *
   * @param value The new value
   */
  public void setPosition(double value) {
    this.position.set(value);
  }

  /**
   * Convenience getter for the {@code absolutePosition} property.
   * <p>
   * See documentation of {@link #absolutePositionProperty() absolutePosition} property for details.
   *
   * @return The current value
   */
  public double getAbsolutePosition() {
    return absolutePosition.get();
  }

  /**
   * The {@code absolutePosition} property.
   * <p>
   * This property contains the absolute position of the divider. Basically, it contains the effective width/height of tbe left/top
   * component.
   * <p>
   * In order to get or set the relative position of the divider, use {@link #positionProperty()}. Note that {@code position} and
   * {@code absolutePosition} depend each other - modification on one of them leads to a modification of the other
   *
   * @return The property.
   */
  public DoubleProperty absolutePositionProperty() {
    return absolutePosition;
  }

  /**
   * Convenience setter for the {@code absolutePosition} property.
   * <p>
   * See documentation of {@link #absolutePositionProperty() absolutePosition} property for details.
   *
   * @param value The new value
   */
  public void setAbsolutePosition(double value) {
    this.absolutePosition.set(value);
  }

  /**
   * Convenience getter for the {@code resizePolicy} property.
   * <p>
   * See documentation of {@link #resizePolicyProperty() resizePolicy} property for details.
   *
   * @return The current value
   */
  public ResizePolicy getResizePolicy() {
    return resizePolicy.get();
  }

  /**
   * The {@code resizePolicy} property.
   * <p>
   * This property defines how the divider is positioned when then container is resized. The exact sizing behaviour is described in the
   * documentation of the {@link ResizePolicy}
   *
   * @return The property.
   */
  public StyleableObjectProperty<ResizePolicy> resizePolicyProperty() {
    return resizePolicy;
  }

  /**
   * Convenience setter for the {@code resizePolicy} property.
   * <p>
   * See documentation of {@link #resizePolicyProperty() resizePolicy} property for details.
   *
   * @param value The new value
   */
  public void setResizePolicy(ResizePolicy value) {
    this.resizePolicy.set(value);
  }

  @Override
  protected Skin<?> createDefaultSkin() {
    return new ViewGroupContainerSkin(this);
  }

  /**
   * Removes {@code child} from this instance.
   *
   * @param child The child to remove
   * @return {@code true}, if it wqs as child
   */
  public boolean remove(GroupOrContainer child) {
    if (child == getLeftTop()) {
      setLeftTop(null);
      return true;
    } else if (child == getRightBottom()) {
      setRightBottom(null);
      return true;
    }
    return false;
  }

  /**
   * Normalizes the object graph.
   * <p>
   * Normalization tries to remove subordinated {@code ViewGroupContainers} as far as possible. In case the left-top or right-bottom child
   * is a {@code ViewGroupContainer} that has less than two children, it can be replaced with the single child or {@code null}; this is done
   * by this method. This method works recursively.
   */
  public void normalize() {
    GroupOrContainer leftTop = normalize(getLeftTop());
    GroupOrContainer rightBottom = normalize(getRightBottom());
    if ((leftTop == null) != (rightBottom == null)) {
      // Only one child
      leftTop = leftTop != null ? leftTop : rightBottom;
      rightBottom = null;
      if (leftTop instanceof ViewGroupContainer vgc) {
        leftTop = vgc.getLeftTop();
        rightBottom = vgc.getRightBottom();
        vgc.setLeftTop(null);
        vgc.setRightBottom(null);
        double position = vgc.getPosition();
        double absolutePosition = vgc.getAbsolutePosition();
        setOrientation(vgc.getOrientation());
        setDividerDragMode(vgc.getDividerDragMode());
        setDividerSize(getDividerSize());
        setResizePolicy(getResizePolicy());
        setAbsolutePosition(absolutePosition);
        setPosition(position);
      }
    }
    setLeftTop(null);
    setRightBottom(null);
    setLeftTop(leftTop);
    setRightBottom(rightBottom);
    requestParentLayout();
  }

  private static GroupOrContainer normalize(GroupOrContainer goc) {
    if (goc instanceof ViewGroup vg) {
      return vg.isAutoClose() && vg.getViews().isEmpty() ? null : vg;
    }
    if (goc instanceof ViewGroupContainer vgc) {
      GroupOrContainer leftTop = normalize(vgc.getLeftTop());
      GroupOrContainer rightBottom = normalize(vgc.getRightBottom());
      if (leftTop != null && rightBottom != null) {
        vgc.setLeftTop(leftTop);
        vgc.setRightBottom(rightBottom);
        return vgc;
      } else if (leftTop != null) {
        return leftTop;
      } else {
        return rightBottom;
      }
    }

    return goc;
  }

  /**
   * Splits the left/top side of the container so that the current content appears on {@code side}.
   * <p>
   * The exact result depends on the question, how many children there are currently present.
   * <ul>
   * <li>No children: In this case the {@linkplain #orientationProperty() orientation} of this
   * container is adapted according to the {@code side parameter}</li>
   * <li>One child: independent, whether the current child is left top/or right/bottom, the
   * orientation of this container is adapted according to the {@code side} and the child is moved
   * to this {@code side}</li>
   * <li>Two children: the method affects only the left/top child, which is replaced by a mew
   * {@code ViewGroupContainer} that is oriented according to the {@code side} and the previous
   * left/top child becomes an according child of the new container</li>
   * </ul>
   *
   * @param side                 The side where to move the existing child to, if any.
   * @param newContainerSupplier {@link Supplier} that creates a new {@link ViewGroupContainer} if required
   * @return The {@code ViewGroupContainer} containing the previous content now.
   */
  public ViewGroupContainer splitLeftTop(Side side, Supplier<ViewGroupContainer> newContainerSupplier) {
    if (hasTwoChildren()) {
      GroupOrContainer lt = getLeftTop();
      ViewGroupContainer newContainer = newContainerSupplier.get();
      setLeftTop(placeInContainer(newContainer, lt, side));
      return newContainer;
    } else {
      placeInContainer(this, getSingleChild(), side);
      return this;
    }
  }

  /**
   * Splits the right/bottom side of the container so that the current content appears on {@code side}.
   * <p>
   * The exact result depends on the question, how many children there are currently present.
   * <ul>
   * <li>No children: In this case the {@linkplain #orientationProperty() orientation} of this
   * container is adapted according to the {@code side parameter}</li>
   * <li>One child: independent, whether the current child is left top/or right/bottom, the
   * orientation of this container is adapted according to the {@code side} and the child is moved
   * to this {@code side}</li>
   * <li>Two children: the method affects only the right/bottom child, which is replaced by a mew
   * {@code ViewGroupContainer} that is oriented according to the {@code side} and the previous
   * right/bottom child becomes an according child of the new container</li>
   * </ul>
   *
   * @param side                 The side where to move the existing child to, if any.
   * @param newContainerSupplier {@link Supplier} that creates a new {@link ViewGroupContainer} if required
   * @return The {@code ViewGroupContainer} containing the previous content now.
   */
  public ViewGroupContainer splitRightBottom(Side side, Supplier<ViewGroupContainer> newContainerSupplier) {
    if (hasTwoChildren()) {
      GroupOrContainer rb = getRightBottom();
      ViewGroupContainer newContainer = newContainerSupplier.get();
      setRightBottom(placeInContainer(newContainer, rb, side));
      return newContainer;
    } else {
      placeInContainer(this, getSingleChild(), side);
      return this;
    }
  }

  private boolean hasTwoChildren() {
    return getLeftTop() != null && getRightBottom() != null;
  }

  private GroupOrContainer getSingleChild() {
    return getLeftTop() != null ? getLeftTop() : getRightBottom();
  }

  private static ViewGroupContainer placeInContainer(ViewGroupContainer container,
      GroupOrContainer child, Side side) {
    container.setOrientation(side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL);
    switch (side) {
      case TOP, LEFT -> {
        container.setRightBottom(null);
        container.setLeftTop(child);
      }
      case BOTTOM, RIGHT -> {
        container.setLeftTop(null);
        container.setRightBottom(child);
      }
    }
    return container;
  }

  public View getMaximizedView() {
    return maximizedView.get();
  }

  public ReadOnlyObjectProperty<View> maximizedViewProperty() {
    return maximizedView;
  }

  public void restoreView(View view) {
    if (view != null && getMaximizedView() == view) {
      maximizedView.set(null);
      requestLayout();
    }
  }

  public void maximizeView(View view) {
    if (view == getMaximizedView()) {
      return;
    }

    if (getMaximizedView() != null) {
      getMaximizedView().setMaximized(false);
      getMaximizedView().getViewGroup().requestLayout();
    }

    maximizedView.set(view);
    requestLayout();
  }

}
