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
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents a target during a drag and drop operation.
 * <p>
 * The main aim of this type is to move the object being dragged ({@link View} or {@link ViewGroup}) to its drop destination.
 * <p>
 * Depending on the mouse position, different {@code DropTarget} implementations are instantiated:
 * <ol>
 *   <li>If the mouse is over the header of an existing {@code ViewGroup}, a {@link MoveToGroup} instance is created</li>
 *   <li>If the mouse is over an existing {@code ViewGroup}, but not over the header area, a {@link SplitViewGroup} is created</li>
 *   <li>If the mouse is not over an existing window, a {@link NewWindow} is created</li>
 *   <li>If there is no actual drop destination, a {@link None} is created</li>
 * </ol>
 * <p>
 * Note that these rules only apply, if dropping is actually possible; depending on the settings of the object being dragged and its
 * potential drop target a drag and drop operation might be forbidden any way.
 */
public interface DropTarget {

  /**
   * Called by the {@link DragAndDropContext} when a {@link View} or {@link ViewGroup} is dropped.
   *
   * @param context    The {@code DragAndDropContext}
   * @param dragSource The object being dragged.
   */
  default void drop(DragAndDropContext context, ViewOrGroup dragSource) {
  }

  /**
   * Factory method to create a {@link NewWindow} instance
   *
   * @param screenPos The screen position
   * @return The instance
   */
  static DropTarget newWindow(Point2D screenPos) {
    return new NewWindow(screenPos);
  }

  /**
   * Factory method to create a {@link MoveToGroup} instance.
   *
   * @param viewGroup The {@link ViewGroup} where to drop
   * @param index     The index where to drop
   * @return The instance
   */
  static DropTarget moveToGroup(ViewGroup viewGroup, int index) {
    return new MoveToGroup(viewGroup, index);
  }

  /**
   * Factory method to create a {@link SplitViewGroup} instance.
   *
   * @param viewGroup The target {@link ViewGroup}
   * @param dropSide  The {@link Side} where to drop
   * @return The instance
   */
  static DropTarget splitViewGroup(ViewGroup viewGroup, Side dropSide) {
    return new SplitViewGroup(viewGroup, dropSide);
  }

  /**
   * Factory method to create a {@link None} instance.
   *
   * @return The instance
   */
  static DropTarget none() {
    return new None();
  }


  /**
   * {@link DropTarget} for moving a {@link ViewOrGroup} to an existing {@link ViewGroup}.
   * <p>
   * If the {@code ViewOrGroup} is actually a {@code ViewGroup}, the {@link View Views} of the group are moved and the source
   * {@code ViewGroup} is removed after wards, if possible.
   *
   * @param viewGroup The target  {@code ViewGroup}
   * @param index     The index where to insert the {@code ViewOrGroup}
   */
  record MoveToGroup(ViewGroup viewGroup, int index) implements DropTarget {

    @Override
    public void drop(DragAndDropContext context, ViewOrGroup dragSource) {
      if (dragSource instanceof View view && view.getViewGroup() != null) {
        dropView(view);
      } else if (dragSource instanceof ViewGroup group) {
        int pos = index;
        while (!group.getViews().isEmpty()) {
          View view = group.getViews().remove(0);
          viewGroup.addView(pos++, view);
        }
        viewGroup.selectView(viewGroup.getViews().get(Math.min(index, viewGroup.getViews().size() - 1)));
      }
    }

    private void dropView(View view) {
      int pos = index;
      if (view.getViewGroup() == viewGroup && index > viewGroup.getViews().indexOf(view)) {
        pos--;
      }
      view.getViewGroup().removeView(view);
      viewGroup.addAndSelectView(pos, view);
    }
  }

  /**
   * A {@link DropTarget} to drop a {@link View} or {@link ViewGroup} to a new window.
   * <p>
   * This is created, when the current drop position is outside any existing window.
   *
   * @param screenPos The screen position
   */
  record NewWindow(Point2D screenPos) implements DropTarget {

    @Override
    public void drop(DragAndDropContext context, ViewOrGroup dragSource) {
      GroupOrContainer dragSourceParent = dragSource.getParentGroupOrContainer();
      if (dragSourceParent == null ||
          (dragSource.isView() && !dragSourceParent.isViewGroup()) ||
          (dragSource.isViewGroup() && !dragSourceParent.isViewGroupContainer())) {
        return;
      }

      Bounds bounds = dragSource.asControl().getBoundsInLocal();
      ViewGroupContainer container = context.newViewGroupContainer(dragSourceParent, null);

      if (dragSource instanceof ViewGroup viewGroup) {
        container.setLeftTop(viewGroup);
      } else {
        ViewGroup viewGroup = context.newViewGroup(null);
        dragSourceParent.asViewGroup().removeView(dragSource.asView());
        viewGroup.addView(dragSource.asView());
        container.setLeftTop(viewGroup);
      }

      Stage stage = context.newStage();
      stage.setScene(new Scene(container));
      stage.setX(screenPos.getX() - bounds.getWidth() / 2);
      stage.setY(screenPos.getY() - bounds.getHeight() / 2);
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());
      stage.show();
    }
  }

  /**
   * A {@link DropTarget} representing that nothing is dropped.
   * <p>
   * This is typically used in situation where the drag and drop operation has no valid target.
   */
  record None() implements DropTarget {

  }

  /**
   * A {@link DropTarget} that splits an existing {@link ViewGroup} into two.
   */
  record SplitViewGroup(ViewGroup viewGroup, Side dropSide) implements DropTarget {

    @Override
    public void drop(DragAndDropContext context, ViewOrGroup dragSource) {
      // Some checks before...
      if (dragSource == null || viewGroup == null) {
        return;
      }
      GroupOrContainer dragSourceParent = dragSource.getParentGroupOrContainer();
      if (dragSourceParent == null ||
          (dragSource.isView() && !dragSourceParent.isViewGroup()) ||
          (dragSource.isViewGroup() && !dragSourceParent.isViewGroupContainer())) {
        return;
      }
      if (!(viewGroup.getParentGroupOrContainer() instanceof ViewGroupContainer dropTargetParent)) {
        return;
      }

      // Remove the source from its current parent
      ViewGroup targetGroup;
      if (dragSource.isViewGroup()) {
        targetGroup = dragSource.asViewGroup();
        dragSourceParent.asViewGroupContainer().remove(dragSource.asViewGroup());
      } else {
        targetGroup = context.newViewGroup(dragSourceParent.asViewGroup());
        dragSourceParent.asViewGroup().removeView(dragSource.asView());
        targetGroup.addView(dragSource.asView());
      }

      // Add the source to its new place
      if (dropTargetParent.getLeftTop() == viewGroup) {
        dropTargetParent = dropTargetParent.splitLeftTop(Utils.getOppositeSide(dropSide),
            () -> context.newViewGroupContainer(dragSourceParent, viewGroup.getParentGroupOrContainer().asViewGroupContainer()));
      } else if (dropTargetParent.getRightBottom() == viewGroup) {
        dropTargetParent = dropTargetParent.splitRightBottom(Utils.getOppositeSide(dropSide),
            () -> context.newViewGroupContainer(dragSourceParent, viewGroup.getParentGroupOrContainer().asViewGroupContainer()));
      }
      if (dropTargetParent.getLeftTop() == null) {
        dropTargetParent.setLeftTop(targetGroup);
      } else {
        dropTargetParent.setRightBottom(targetGroup);
      }
    }
  }
}
