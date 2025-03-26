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

import java.util.Set;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Control;

/**
 * Supplemental interface implemented by {@link ViewGroup ViewGroups} and {@link View Views}.
 * <p>
 * Can be used in all situations, where an object might be either a {@code ViewGroup} or a
 * {@code View}
 * <p>
 * It provides those method that are required for drag and drop, more exactly those helper methods
 * that are required to the source of the drag and drop operation.
 */
public interface ViewOrGroup {

  /**
   * Enum for the allowed drop targets when it comes to drag and drop.
   * <p>
   * This enum describes, to which destinations the {@link ViewOrGroup} can be dropped in general.
   * This enum is used by the {@link #getDropTargetTypes()} methods to tell, which targets are
   * supported.
   */
  enum DropTargetType {
    /**
     * Indicates that reordering is allowed.
     * <p>
     * This is only relevant for {@link View Views} - if the {@code REORDER} target type is set, it
     * is allowed to change the index of the view by dragging it in the same {@link ViewGroup}.
     */
    REORDER,

    /**
     * Indicates that reparenting in a different {@link ViewGroup} is allowed.
     * <p>
     * This means that the {@link ViewOrGroup} might be moved into a different {@code ViewGroup}. In
     * general, a drag and drop operation with reparenting is only possible, if all
     * {@link #getDragTags() drag tags} are available in the
     * {@linkplain ViewGroup#getDropTags() drop tags} of the target.
     */
    CHANGE_GROUP,

    /**
     * Indicates that the object might be dropped into a new window.
     * <p>
     * If set and the drop target is not inside a known window, a new window is created then.
     */
    NEW_WINDOW
  }

  /**
   * Gets the tags for dragging.
   * <p>
   * These tags are evaluated when attempting to drop this object into another one. This is only
   * possible, if all the tags in this set can be found in the
   * {@linkplain ViewGroup#getDropTags() drop tags} of the target.
   *
   * @return The drag tags
   */
  Set<String> getDragTags();

  /**
   * Gets the supported {@link DropTargetType DropTargetTypes}.
   * <p>
   * Depending on these types it is decided, whether a drag and drop operation is supported.
   *
   * @return The supported {@code DropTargetTypes}
   */
  Set<DropTargetType> getDropTargetTypes();

  /**
   * Indicates, whether this is currently being dragged.
   *
   * @return {@code true}, if being dragged.
   */
  default boolean isDragging() {
    return draggingProperty().get();
  }

  /**
   * Sets this object being dragged.
   * <p>
   * Normally, you never call this method directly
   *
   * @param dragging Dragging flag
   */
  void setDragging(boolean dragging);

  /**
   * The {@code dragging} property.
   * <p>
   * Contains the information, whether this is being dragged.
   *
   * @return {@code Property}
   */
  ReadOnlyBooleanProperty draggingProperty();

  /**
   * Returns this as a {@link Control} instance.
   * <p>
   * For {@link ViewGroup ViewGroups} this is the instance itself, for {@link View Views} this is
   * the owning group
   *
   * @return This instance
   */
  default Control asControl() {
    return (Control) this;
  }

  /**
   * Returns, whether this is a {@link ViewGroup}
   *
   * @return {@code true}, if a {@code ViewGroup}
   */
  default boolean isViewGroup() {
    return this instanceof ViewGroup;
  }

  /**
   * Returns this as a {@link ViewGroup} instance.
   * <p>
   * Fails, if a not a {@link ViewGroup}
   *
   * @return This instance
   */
  default ViewGroup asViewGroup() {
    return (ViewGroup) this;
  }

  /**
   * Returns, whether this is a {@link View}
   *
   * @return {@code true}, if a {@code View}
   */
  default boolean isView() {
    return this instanceof View;
  }

  /**
   * Returns this as a {@link View} instance.
   * <p>
   * Fails, if a not a {@link View}
   *
   * @return This instance
   */
  default View asView() {
    return (View) this;
  }

  /**
   * Gets the parent {@link GroupOrContainer}, if any.
   *
   * @return The {@code GroupOrContainer}
   */
  default GroupOrContainer getParentGroupOrContainer() {
    if (this instanceof View view) {
      return view.getViewGroup();
    } else if (this instanceof ViewGroup group) {
      return group.getParent() instanceof ViewGroupContainer container ? container : null;
    }
    return null;
  }
}
