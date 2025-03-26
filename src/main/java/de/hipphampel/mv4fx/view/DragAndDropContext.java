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

import java.util.Objects;

/**
 * Helper to keep the state of a drag and drop operation.
 * <p>
 * Since there could be only one drag and drop operation at a point in time, there exists only one
 * instance, which can be accessed via the {@link #getInstance() getInstance()} method, which also
 * exists, if no actual drag and drop operation is in progress.
 * <p>
 * To allow an alternative implementation, there is a
 * {@link #setInstance(DragAndDropContext) setInstance()} method, which allows to replace the
 * default implementation, namely the {@link DefaultDragAndDropContext}
 */
public interface DragAndDropContext {

  /**
   * Sets the one and only instance.
   * <p>
   * By default, this is an instance of the {@link DefaultDragAndDropContext}
   *
   * @param context The {@code DragAndDropContext}
   */
  static void setInstance(DragAndDropContext context) {
    DragAndDropContextHolder.instance = Objects.requireNonNull(context);
  }

  /**
   * Gets the one and only instance.
   *
   * @return The {@code DragAndDropContext}
   */
  static DragAndDropContext getInstance() {
    return DragAndDropContextHolder.instance;
  }

  /**
   * Gets the object being dragged.
   * <p>
   * If no drag and drop operation is in progess, {@code null} is returned.
   *
   * @return The object being dragged.
   */
  ViewOrGroup getDragSource();

  /**
   * Gets the current {@link DropTarget}
   *
   * @return The {@code DropTarget}
   */
  DropTarget getDropTarget();

  /**
   * Starts a drag and drop operation with the given parameters.
   * <p>
   * The operation can be updated by calling  {@link #update(double, double) update} or terminated
   * by {@link #cancel() cancel} or {@link #confirm(double, double) confirm}
   *
   * @param dragSource Object being dragged
   * @param screenX    The screen X coordinate where the operation starts
   * @param screenY    The screen Y coordinate where the operation starts
   */
  void start(ViewOrGroup dragSource, double screenX, double screenY);

  /**
   * Confirms a drag and drop informaton with the given position.
   * <p>
   * The
   *
   * @param screenX The screen X coordinate where the operation ends
   * @param screenY The screen Y coordinate where the operation ends
   */
  void confirm(double screenX, double screenY);

  /**
   * Cancels the current drag and drop operation.
   * <p>
   * Any highlighting is removed and the operation is stopped.
   */
  void cancel();

  /**
   * Called to update the current context.
   * <p>
   * This method is called when the mouse is moved to the given screen position. The basic aim is to
   * update the {@link #getDropTarget() drop target} according to the new position.
   *
   * @param screenX The X screen position of the mouse
   * @param screenY The Y screen position of the mouse
   */
  void update(double screenX, double screenY);

  /**
   * Creates a new {@link ViewStage}.
   * <p>
   * The method is called when the drop target indicates that a new stages has to be created.
   *
   * @return The new {@code ViewStage}
   */
  default ViewStage newStage() {
    return new ViewStage();
  }

  /**
   * Creates a new {@link ViewGroupContainer} during a drag and drop operation.
   * <p>
   * This is called in two situations:
   * <ol>
   *   <li>When a {@code ViewGroupContainer} is split and a new one needs to be created as left/top or
   *   right/bottom child of it. In this case the {@code dropTargetParent} is not {@code null}</li>
   *   <li>When the {@code dragSource} is moved to a new windows and therefor a new {@code ViewGroupContainer}
   *   needs to be created to host the {@code dragSource}. In this case the {@code dropTargetParent} is
   *   {@code null}. </li>
   * </ol>
   *
   * @param dragSourceParent The parent of the {@link #getDragSource() dragSource}
   * @param dropTargetParent The target {@code ViewGroupContainer}, might be {@code null}
   * @return The newly created {@code ViewGroupContainer}
   */
  default ViewGroupContainer newViewGroupContainer(GroupOrContainer dragSourceParent,
      ViewGroupContainer dropTargetParent) {
    ViewGroupContainer container = new ViewGroupContainer();
    if (dropTargetParent != null) {
      container.setOrientation(dropTargetParent.getOrientation());
      container.setDividerSize(dropTargetParent.getDividerSize());
      container.setResizePolicy(dropTargetParent.getResizePolicy());
    }
    return container;
  }

  /**
   * Creates a new {@link ViewGroup} during a drag and drop operation.
   * <p>
   * This method is called in two situations:
   * <ol>
   * <li>When a {@link ViewGroupContainer} is split and a new {@code ViewGroup} needs to be created</li>
   * </ol>
   *
   * @param template The template for the new {@code ViewGroup}
   * @return The new {@code ViewGroup}
   */
  default ViewGroup newViewGroup(ViewGroup template) {
    ViewGroup copy = new ViewGroup();
    if (template != null) {
      copy.setTabMaxWidth(template.getTabMaxWidth());
      copy.setTabMinWidth(template.getTabMinWidth());
      copy.setSide(template.getSide());
      copy.setViewSelectorControls(template.getViewSelectorControls());
      copy.setDragTags(template.getDragTags());
      copy.setDropTags(template.getDropTags());
      copy.setDropTargetTypes(template.getDropTargetTypes());
      copy.setDropSplitSides(template.getDropSplitSides());
    }
    return copy;

  }
}


class DragAndDropContextHolder {

  static DragAndDropContext instance = new DefaultDragAndDropContext();
}
