//=== File Prolog =============================================================
//	This code was developed by NASA, Goddard Space Flight Center, Code 588
//	for the Scientist's Expert Assistant (SEA) project.
//
//--- Contents ----------------------------------------------------------------
//	TimeLineNode
//
//--- Description -------------------------------------------------------------
//	A class for a single node on a time line
//
//--- Notes -------------------------------------------------------------------
//
//--- Development History -----------------------------------------------------
//
//	05/19/99	M. Fishman
//
//		Original implementation.
//
//
//--- DISCLAIMER---------------------------------------------------------------
//
//	This software is provided "as is" without any warranty of any kind, either
//	express, implied, or statutory, including, but not limited to, any
//	warranty that the software will conform to specification, any implied
//	warranties of merchantability, fitness for a particular purpose, and
//	freedom from infringement, and any warranty that the documentation will
//	conform to the program, or any warranty that the software will be error
//	free.
//
//	In no event shall NASA be liable for any damages, including, but not
//	limited to direct, indirect, special or consequential damages, arising out
//	of, resulting from, or in any way connected with this software, whether or
//	not based upon warranty, contract, tort or otherwise, whether or not
//	injury was sustained by persons or property or otherwise, and whether or
//	not loss was sustained from or arose out of the results of, or use of,
//	their software or services provided hereunder.
//
//=== End File Prolog =========================================================
//package gov.nasa.gsfc.util.gui;

package jsky.timeline;

import jsky.science.Time;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.text.DecimalFormat;

/**
 *
 * An interface for a single node on the time line.
 *
 * <P>This code was developed by NASA, Goddard Space Flight Center, Code 588
 * for the Scientist's Expert Assistant (SEA) project.
 *
 * @version     04/27/99
 * @author      M. Fishman
 **/
public class DefaultTimeLineNode implements TimeLineNode {

    protected final float fThumbHeight = 8;
    protected float fHandleHeight = 12;
    protected float fHandleWidth = 6;

    protected Color fSelectedColor = new Color(102, 204, 255, 160);
    protected Color fUnselectedColor = Color.blue;

    protected VetoableTimeLineNodeModel fModel;
    protected int fMode = TimeLineNode.UNSELECTED;
    protected int fDragMode = TimeLineNode.UNSELECTED;
    protected VetoableChangeSupport fChangeSupport = null;

    protected float fThumbBegin = 0.0f;
    protected float fThumbEnd = 0.0f;

    protected final BasicStroke fDefaultStroke = new BasicStroke();
    protected final BasicStroke fShadowStroke = new BasicStroke(1);

    protected final Line2D.Float fThumbShadowLine = new Line2D.Float();
    protected final Line2D.Float fThumbTopShadowLine = new Line2D.Float();
    protected final Line2D.Float fLHandleTopShadowLine = new Line2D.Float();
    protected final Line2D.Float fLHandleBottomShadowLine = new Line2D.Float();
    protected final Line2D.Float fLHandleRightShadowLine = new Line2D.Float();
    protected final Line2D.Float fLHandleLeftShadowLine = new Line2D.Float();
    protected final Line2D.Float fRHandleTopShadowLine = new Line2D.Float();
    protected final Line2D.Float fRHandleBottomShadowLine = new Line2D.Float();
    protected final Line2D.Float fRHandleRightShadowLine = new Line2D.Float();
    protected final Line2D.Float fRHandleLeftShadowLine = new Line2D.Float();

    protected final Rectangle2D.Float fThumb = new Rectangle2D.Float();
    protected final Rectangle2D.Float fLeftHandle = new Rectangle2D.Float();
    protected final Rectangle2D.Float fRightHandle = new Rectangle2D.Float();

    protected TimeLine fTimeLine = null;

    protected boolean fForceRecalculation = false;

    protected final VetoableChangeListener fMyVetoListener = evt -> fChangeSupport.fireVetoableChange(evt.getPropertyName(),
                                      evt.getOldValue(),
                                      evt.getNewValue());

    public DefaultTimeLineNode(final Time startTime, final Time endTime, final String name) {
        fModel = new DefaultVetoableTimeLineNodeModel(startTime, endTime, name);
        init();
    }

    /**
     *
     * initializes the object.  Override at your own risk.
     *
     **/
    protected void init() {
        fChangeSupport = new VetoableChangeSupport(this);
        fModel.addVetoableChangeListener(fMyVetoListener);
    }

    /**
     *
     * set the selection mode of the time linenode
     *
     **/
    public void setSelectionMode(final int mode) {
        if (fMode != mode) {
            final int oldMode = fMode;
            try {
                fChangeSupport.fireVetoableChange(TimeLineNode.MODE, oldMode, mode);
                fMode = mode;
            } catch (DetailedPropertyVetoException ignored) {
                // ignored
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * get the selection mode of the time line node
     *
     **/
    public int getSelectionMode() {
        return fMode;
    }

    /**
     *
     * set the unselected color for the time line node
     *
     **/
    public void setUnselectedColor(final Color color) {
        if (color != fUnselectedColor) {
            final Color oldColor = fUnselectedColor;
            try {
                fUnselectedColor = color;
                fChangeSupport.fireVetoableChange(TimeLineNode.UNSELECTED_COLOR,
                                                  oldColor, color);

            } catch (DetailedPropertyVetoException ex) {
                fUnselectedColor = oldColor;
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * get the the unselected color the time line node
     *
     **/
    public Color getUnselectedColor() {
        return fUnselectedColor;
    }

    /**
     *
     * set the selected color for the time line node
     *
     **/
    public void setSelectedColor(final Color color) {
        if (color != fSelectedColor) {
            final Color oldColor = fSelectedColor;
            try {
                fSelectedColor = color;
                fChangeSupport.fireVetoableChange(TimeLineNode.UNSELECTED_COLOR,
                                                  oldColor, color);

            } catch (DetailedPropertyVetoException ex) {
                fSelectedColor = oldColor;
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * get the the selected color the time line node
     *
     **/
    public Color getSelectedColor() {
        return fSelectedColor;
    }

    /**
     *
     * get the time on the time line that this node starts
     *
     **/
    public Time getStartTime() {
        return fModel.getStartTime();
    }

    /**
     *
     * set the time that this node starts
     *
     **/
    public void setStartTime(final Time time) throws DetailedPropertyVetoException {
        if (time.getValue(Time.SECOND) != fModel.getStartTime().getValue(Time.SECOND)) {
            final Time oldTime = fModel.getStartTime();
            try {
                fModel.setValidStartTime(time);
                Time displayEdge = fTimeLine.getDisplayStart();
                if ((getStartTime().getValue() < displayEdge.getValue() &&
                        oldTime.getValue() >= displayEdge.getValue())) {
                    fChangeSupport.fireVetoableChange(TimeLineNode.HIT_LEFT_EDGE, oldTime, time);
                    fForceRecalculation = true;
                }
            } catch (DetailedPropertyVetoException ex) {
                fForceRecalculation = true;
                throw ex;
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * move node by specified amount
     *
     **/
    public synchronized void moveTimeLineNodeBy(final Time time) throws DetailedPropertyVetoException {
        if (time.getValue() != 0.0) {
            final Time oldStartTime = fModel.getStartTime();
            final Time oldEndTime = fModel.getEndTime();
            try {
                fModel.moveTimeLineNodeByValid(time);
                if (fTimeLine != null) {
                    if (time.getValue() < 0d) {
                        Time displayEdge = fTimeLine.getDisplayStart();
                        if ((getStartTime().getValue() < displayEdge.getValue() &&
                                oldStartTime.getValue() >= displayEdge.getValue())) {
                            fChangeSupport.fireVetoableChange(TimeLineNode.HIT_LEFT_EDGE,
                                                              oldStartTime, getStartTime());
                            fForceRecalculation = true;
                        }
                    } else {
                        Time displayEdge = fTimeLine.getDisplayEnd();
                        if ((getEndTime().getValue() > displayEdge.getValue() &&
                                oldEndTime.getValue() <= displayEdge.getValue())) {
                            fChangeSupport.fireVetoableChange(TimeLineNode.HIT_RIGHT_EDGE, oldEndTime, time);
                            fForceRecalculation = true;
                        }
                    }
                }

            } catch (DetailedPropertyVetoException ex) {
                fModel.revertToPrevious();
                fForceRecalculation = true;
                throw ex;
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * get the time on the time line that this node ends
     *
     **/
    public Time getEndTime() {
        return fModel.getEndTime();
    }

    /**
     *
     * set the time on the time line that this node ends
     *
     **/
    public void setEndTime(final Time time) throws DetailedPropertyVetoException {
        if (time.getValue(Time.SECOND) != fModel.getEndTime().getValue(Time.SECOND)) {
            final Time oldTime = fModel.getEndTime();
            try {
                if (time.getValue(Time.SECOND) > getStartTime().getValue(Time.SECOND)) {
                    fModel.setValidEndTime(time);
                    final Time displayEdge = fTimeLine.getDisplayEnd();
                    if ((fModel.getEndTime().getValue() > displayEdge.getValue() &&
                            oldTime.getValue() <= displayEdge.getValue())) {
                        fChangeSupport.fireVetoableChange(TimeLineNode.HIT_RIGHT_EDGE, oldTime, time);
                        fForceRecalculation = true;
                    }
                } else {
                    throw new DetailedPropertyVetoException(this, TimeLineNode.NODE_MIN_SIZE_EXCEEDED,
                                                            "invalid end time", null);
                }
            } catch (DetailedPropertyVetoException ex) {
                fModel.revertToPrevious();
                fForceRecalculation = true;
                throw ex;
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * get the duration of the time line node
     *
     **/
    public Time getDuration() {
        return fModel.getDuration();
    }

    /**
     *
     * set the duration of the time line node
     **/
    public void setDuration(final Time durationLength) throws DetailedPropertyVetoException {
        if (fTimeLine != null) {
            setEndTime(new Time(fModel.getStartTime().getValue(Time.SECOND) +
                                durationLength.getValue(Time.SECOND), Time.SECOND));
        }
    }

    /**
     *
     * add a property change listener to the node
     **/
    public void addVetoableChangeListener(final VetoableChangeListener listener) {
        fChangeSupport.addVetoableChangeListener(listener);

    }

    /**
     *
     * remove a propertyChangeListener to the node
     **/
    public void removeVetoableChangeListener(final VetoableChangeListener listener) {
        fChangeSupport.removeVetoableChangeListener(listener);
    }

    /**
     * paint the time line node
     *
     * @param graphics the graphics component to paint
     **/
    public void paintTimeLineNode(final Graphics2D graphics) {
        final Font origFont = graphics.getFont();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final Time startTime = fModel.getStartTime();
        final Time endTime = fModel.getEndTime();

        boolean addHandles = true;
        if (!((startTime.getValue(Time.SECOND) > fTimeLine.getDisplayEnd().getValue(Time.SECOND)) ||
                (endTime.getValue(Time.SECOND) < fTimeLine.getDisplayStart().getValue(Time.SECOND)))) {
            if ((fDragMode == TimeLineNode.UNSELECTED) || fForceRecalculation) {
                calculateNodeDimensions();
                fForceRecalculation = false;
            } else {
                final float thumbMin = fTimeLine.getPointForTime(fTimeLine.getDisplayStart());
                final float thumbMax = fTimeLine.getPointForTime(fTimeLine.getDisplayEnd());

                if (fThumbBegin < thumbMin) {
                    fThumbBegin = thumbMin;
                }

                if (fThumbEnd > thumbMax) {
                    fThumbEnd = thumbMax;
                }
            }
            final float thumbLengthMin = 2f * fHandleWidth;
            final float thumbWidth = (fThumbEnd - fThumbBegin);
            float thumbHeight = fThumbHeight;
            if (thumbWidth < thumbLengthMin) {
                thumbHeight = fHandleHeight;
                addHandles = false;
            }

            // draw the thumb
            fThumb.height = thumbHeight;
            fThumb.width = thumbWidth;
            fThumb.x = fThumbBegin;
            fThumb.y = (fTimeLine.getHeight() / 2f - thumbHeight / 2f);
            graphics.setStroke(fDefaultStroke);

            if (fMode == TimeLineNode.NODE_SELECTED) {
                graphics.setColor(fSelectedColor);
            } else {
                graphics.setColor(fUnselectedColor);
            }
            graphics.draw(fThumb);
            graphics.fill(fThumb);

            // this is the bottom shadow line of the thumb
            fThumbShadowLine.x1 = fThumb.x;
            fThumbShadowLine.y1 = fThumb.y + thumbHeight;
            fThumbShadowLine.x2 = fThumb.x + thumbWidth;
            fThumbShadowLine.y2 = fThumb.y + thumbHeight;

            graphics.setStroke(fShadowStroke);
            graphics.setColor(Color.black);
            graphics.draw(fThumbShadowLine);

            // this is the top shadow line of the thumb
            fThumbTopShadowLine.x1 = fThumb.x;
            fThumbTopShadowLine.y1 = fThumb.y;
            fThumbTopShadowLine.x2 = fThumb.x + thumbWidth;
            fThumbTopShadowLine.y2 = fThumb.y;

            graphics.setStroke(fShadowStroke);
            graphics.setColor(Color.white);
            graphics.draw(fThumbTopShadowLine);

            final double length = endTime.getValue(Time.MINUTE) - startTime.getValue(Time.MINUTE);

            if ((startTime.getValue(Time.SECOND) >= fTimeLine.getDisplayStart().getValue(Time.SECOND)) && addHandles) {
                // draw left handle
                fLeftHandle.height = fHandleHeight;
                fLeftHandle.width = fHandleWidth;
                fLeftHandle.x = fThumb.x;
                fLeftHandle.y = (float) fTimeLine.getHeight() / 2f - fHandleHeight / 2f;
                graphics.setStroke(fDefaultStroke);
                if ((fMode == TimeLineNode.LEFT_HANDLE_SELECTED) ||
                        (fMode == TimeLineNode.NODE_SELECTED)) {
                    graphics.setColor(fSelectedColor);
                } else {
                    graphics.setColor(fUnselectedColor);
                }
                graphics.draw(fLeftHandle);
                graphics.fill(fLeftHandle);
                graphics.setStroke(fShadowStroke);

                // these are shadow lines
                fLHandleBottomShadowLine.x1 = fLeftHandle.x;
                fLHandleBottomShadowLine.y1 = fLeftHandle.y + fHandleHeight;
                fLHandleBottomShadowLine.x2 = fLeftHandle.x + fHandleWidth;
                fLHandleBottomShadowLine.y2 = fLeftHandle.y + fHandleHeight;

                fLHandleRightShadowLine.x1 = fLeftHandle.x + fHandleWidth;
                fLHandleRightShadowLine.y1 = fLeftHandle.y;
                fLHandleRightShadowLine.x2 = fLeftHandle.x + fHandleWidth;
                fLHandleRightShadowLine.y2 = fLeftHandle.y + fHandleHeight;

                fLHandleLeftShadowLine.x1 = fLeftHandle.x;
                fLHandleLeftShadowLine.y1 = fLeftHandle.y;
                fLHandleLeftShadowLine.x2 = fLeftHandle.x;
                fLHandleLeftShadowLine.y2 = fLeftHandle.y + fHandleHeight;

                graphics.setColor(Color.black);
                graphics.draw(fLHandleBottomShadowLine);

                graphics.draw(fLHandleRightShadowLine);

                fLHandleTopShadowLine.x1 = fLeftHandle.x;
                fLHandleTopShadowLine.y1 = fLeftHandle.y;
                fLHandleTopShadowLine.x2 = fLeftHandle.x + fHandleWidth;
                fLHandleTopShadowLine.y2 = fLeftHandle.y;

                graphics.setColor(Color.white);
                graphics.draw(fLHandleTopShadowLine);
                graphics.draw(fLHandleLeftShadowLine);
            }

            if ((endTime.getValue(Time.SECOND) <= fTimeLine.getDisplayEnd().getValue(Time.SECOND)) && addHandles) {
                // draw Right handle
                fRightHandle.height = fHandleHeight;
                fRightHandle.width = fHandleWidth;
                fRightHandle.x = fThumb.x + thumbWidth - fHandleWidth;
                fRightHandle.y = (float) fTimeLine.getHeight() / 2f - fHandleHeight / 2f;
                graphics.setStroke(fDefaultStroke);
                if ((fMode == TimeLineNode.RIGHT_HANDLE_SELECTED) ||
                        (fMode == TimeLineNode.NODE_SELECTED)) {
                    graphics.setColor(fSelectedColor);
                } else {
                    graphics.setColor(fUnselectedColor);
                }
                graphics.draw(fRightHandle);
                graphics.fill(fRightHandle);

                // these are shadow lines
                fRHandleBottomShadowLine.x1 = fRightHandle.x;
                fRHandleBottomShadowLine.y1 = fRightHandle.y + fHandleHeight;
                fRHandleBottomShadowLine.x2 = fRightHandle.x + fHandleWidth;
                fRHandleBottomShadowLine.y2 = fRightHandle.y + fHandleHeight;

                fRHandleRightShadowLine.x1 = fRightHandle.x + fHandleWidth;
                fRHandleRightShadowLine.y1 = fRightHandle.y;
                fRHandleRightShadowLine.x2 = fRightHandle.x + fHandleWidth;
                fRHandleRightShadowLine.y2 = fRightHandle.y + fHandleHeight;

                fRHandleLeftShadowLine.x1 = fRightHandle.x;
                fRHandleLeftShadowLine.y1 = fRightHandle.y;
                fRHandleLeftShadowLine.x2 = fRightHandle.x;
                fRHandleLeftShadowLine.y2 = fRightHandle.y + fHandleHeight;

                fRHandleTopShadowLine.x1 = fRightHandle.x;
                fRHandleTopShadowLine.y1 = fRightHandle.y;
                fRHandleTopShadowLine.x2 = fRightHandle.x + fHandleWidth;
                fRHandleTopShadowLine.y2 = fRightHandle.y;

                graphics.setStroke(fShadowStroke);
                graphics.setColor(Color.black);
                graphics.draw(fRHandleBottomShadowLine);
                graphics.draw(fRHandleRightShadowLine);

                graphics.setColor(Color.white);
                graphics.setStroke(fDefaultStroke);
                graphics.draw(fRHandleTopShadowLine);
            }

            // draw name label
            graphics.setFont(TimeLineNode.DEFAULT_FONT);
            final NodeLabelData nlp = calc(getTimeLineNodeName(), graphics, addHandles);
            final AffineTransform oldTransform = graphics.getTransform();
            if (nlp.rotate) {
                graphics.rotate(TimeLineNode.LABEL_ROTATION, nlp.textX, nlp.textY);
            }
            graphics.setColor(Color.black);
            if (thumbWidth > 14.) // allan: quick fix to avoid overlapping strings
            {
                graphics.drawString(nlp.label, nlp.textX, nlp.textY);
            }
            graphics.setTransform(oldTransform);

            // draw duration
            graphics.setFont(TimeLineNode.DEFAULT_FONT);
            final DecimalFormat lengthForm = new DecimalFormat();
            lengthForm.setMaximumFractionDigits(2);
            final String lengthStr = lengthForm.format(length);
            final Rectangle2D lengthBounds = graphics.getFontMetrics().getStringBounds(lengthStr,
                                                                                 graphics);
            graphics.setColor(Color.black);
            float lengthX = fThumb.x + thumbWidth / 2f - (float) (lengthBounds.getWidth() / 2.0);
            float lengthY;
            if (!addHandles) {
                lengthY = fThumb.y + fHandleHeight + (float) (lengthBounds.getHeight() + TimeLineNode.DEFAULT_LABEL_SPACE);
            } else {
                lengthY = fLeftHandle.y + fHandleHeight + (float) (lengthBounds.getHeight() + TimeLineNode.DEFAULT_LABEL_SPACE);
            }
            if (lengthBounds.getWidth() > thumbWidth) {
                lengthX = fThumb.x + thumbWidth / 2f;
                graphics.rotate(TimeLineNode.LABEL_REVERSE_ROTATION, lengthX, lengthY);
            }
            if (thumbWidth > 14.) // allan: quick fix to avoid overlapping strings
            {
                graphics.drawString(lengthStr, lengthX, lengthY);
            }
            graphics.setFont(origFont);
            graphics.setTransform(oldTransform);
        }
    }

    private class NodeLabelData {
        final String label;
        final float textX;
        final float textY;
        final boolean rotate;

        NodeLabelData(final String label, final float x, final float y, final boolean rotate) {
            this.label  = label;
            this.textX  = x;
            this.textY  = y;
            this.rotate = rotate;
        }
    }

    private static final int LABEL_PADDING = 8;

    private NodeLabelData calc(final String startLabel, final Graphics2D graphics, final boolean addHandles) {
        final Rectangle2D nameBounds = graphics.getFontMetrics().getStringBounds(startLabel, graphics);
        final float thumbWidth = (fThumbEnd - fThumbBegin);

        float textY;
        if (!addHandles) {
            textY = fThumb.y - (float) (nameBounds.getHeight() - TimeLineNode.DEFAULT_LABEL_SPACE);
        } else {
            textY = fLeftHandle.y - (float) (nameBounds.getHeight() - TimeLineNode.DEFAULT_LABEL_SPACE);
        }

        if ((nameBounds.getWidth() + LABEL_PADDING) <= thumbWidth) {
            // Already fits, so just return the information.
            final float textX = fThumb.x + thumbWidth / 2f - (float) (nameBounds.getWidth() / 2.0);
            return new NodeLabelData(startLabel, textX, textY, false);
        }

        // Too big to fit, so try a shorter label if available.
        final String shorterLabel = shortenTimeLineNodeName(startLabel);
        if ((shorterLabel == null) || (shorterLabel.length() >= startLabel.length())) {
            // There isn't a shorter label, so rotate it.
            final float textX = fThumb.x + thumbWidth / 2f;
            return new NodeLabelData(startLabel, textX, textY, true);
        } else {
            // We got back a shorter alternative, so use it.
            return calc(shorterLabel, graphics, addHandles);
        }
    }

    /**
     *
     * handle mouse events
     *
     **/
    private float fTempMouseOffset = 0;
    private float fTempThumbWidth = 0;

    public synchronized void handleMouseEvent(final MouseEvent evt) {
        final Point pt = evt.getPoint();
        if (!evt.isPopupTrigger()) {
            if ((evt.getID() == MouseEvent.MOUSE_CLICKED) && (evt.getClickCount() == 1)) {
                if (fLeftHandle.contains(pt.x, pt.y)) {
                    if ((fMode == TimeLineNode.LEFT_HANDLE_SELECTED) ||
                            (fMode == TimeLineNode.NODE_SELECTED)) {
                        setSelectionMode(TimeLineNode.UNSELECTED);
                    } else {
                        setSelectionMode(TimeLineNode.LEFT_HANDLE_SELECTED);
                    }
                } else if (fRightHandle.contains(pt.x, pt.y)) {
                    if ((fMode == TimeLineNode.RIGHT_HANDLE_SELECTED) ||
                            (fMode == TimeLineNode.NODE_SELECTED)) {
                        setSelectionMode(TimeLineNode.UNSELECTED);
                    } else {
                        setSelectionMode(TimeLineNode.RIGHT_HANDLE_SELECTED);
                    }
                } else if (fThumb.contains(pt.x, pt.y)) {
                    if (fMode == TimeLineNode.NODE_SELECTED) {
                        setSelectionMode(TimeLineNode.UNSELECTED);
                    } else {
                        setSelectionMode(TimeLineNode.NODE_SELECTED);
                    }
                }

            } else if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
                if (fLeftHandle.contains(pt.x, pt.y) && !fRightHandle.contains(pt.x, pt.y)) {
                    fDragMode = TimeLineNode.LEFT_HANDLE_SELECTED;
                } else if (fRightHandle.contains(pt.x, pt.y) && !fLeftHandle.contains(pt.x, pt.y)) {
                    fDragMode = TimeLineNode.RIGHT_HANDLE_SELECTED;
                } else if (fThumb.contains(pt.x, pt.y)) {
                    fDragMode = TimeLineNode.NODE_SELECTED;
                    fTempMouseOffset = pt.x - fThumbBegin;
                    fTempThumbWidth = fThumbEnd - fThumbBegin;

                }

            } else if (evt.getID() == MouseEvent.MOUSE_RELEASED) {
                final int oldMode = fDragMode;
                fDragMode = TimeLineNode.UNSELECTED;
                try {
                    setDuration(getDuration());
                    fChangeSupport.fireVetoableChange(TimeLineNode.MODE, oldMode, fDragMode);
                } catch (Exception ex) {
                    // ignored
                }
            }
        }
    }

    /**
     *
     * handle mouse events
     *
     **/
    public synchronized void handleMouseDragEvent(final MouseEvent evt) {
        if (fDragMode != TimeLineNode.UNSELECTED) {
            final Point pt = evt.getPoint();

            if (fDragMode == TimeLineNode.LEFT_HANDLE_SELECTED) {
                fThumbBegin = pt.x;
            } else if (fDragMode == TimeLineNode.RIGHT_HANDLE_SELECTED) {
                fThumbEnd = pt.x;
            } else if (fDragMode == TimeLineNode.NODE_SELECTED) {
                fThumbBegin = pt.x - fTempMouseOffset;
                fThumbEnd = fThumbBegin + fTempThumbWidth;
            }

            final Time newStartTime = fTimeLine.getTimeForPoint(fThumbBegin);
            final Time newEndTime = fTimeLine.getTimeForPoint(fThumbEnd);
            final Time oldStartTime = getStartTime();

            try {
                if (fDragMode == TimeLineNode.NODE_SELECTED) {
                    if (newStartTime.getValue(Time.SECOND) != oldStartTime.getValue(Time.SECOND)) {
                        final double moveByVal = newStartTime.getValue(Time.SECOND)
                                - oldStartTime.getValue(Time.SECOND);
                        moveTimeLineNodeBy(new Time(moveByVal, Time.SECOND));
                    }
                } else if (fDragMode == TimeLineNode.LEFT_HANDLE_SELECTED) {
                    setStartTime(newStartTime);
                } else if (fDragMode == TimeLineNode.RIGHT_HANDLE_SELECTED) {
                    setEndTime(newEndTime);
                }

            } catch (DetailedPropertyVetoException ex) {
                fForceRecalculation = true;
            }
        }
    }


    /**
     *
     * handle mouse events
     *
     **/
    public void handleMouseMoveEvent(final MouseEvent evt) { }

    /**
     *
     * handle key event
     *
     **/
    public void handleKeyEvent(final KeyEvent evt) throws DetailedPropertyVetoException {
        if (evt.getID() == KeyEvent.KEY_PRESSED) {
            int interval = 10;

            if (evt.isControlDown()) {
                interval = 1;
            } else if (evt.isShiftDown()) {
                interval = 60;
            }
            if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                interval = -1 * interval;
            } else if (evt.getKeyCode() != KeyEvent.VK_RIGHT) {
                interval = 0;
            }

            if (interval != 0) {
                final Time anInterval = fTimeLine.getIntervalTime();
                final Time intervalTime = new Time(anInterval.getValue(Time.SECOND) * interval, Time.SECOND);
                if (getSelectionMode() == TimeLineNode.LEFT_HANDLE_SELECTED) {
                    setStartTime(new Time(getStartTime().getValue(Time.SECOND)
                                          + intervalTime.getValue(Time.SECOND),
                                          Time.SECOND));
                } else if (getSelectionMode() == TimeLineNode.RIGHT_HANDLE_SELECTED) {
                    setEndTime(new Time(getEndTime().getValue(Time.SECOND)
                                        + intervalTime.getValue(Time.SECOND),
                                        Time.SECOND));
                } else if (getSelectionMode() == TimeLineNode.NODE_SELECTED) {
                    moveTimeLineNodeBy(intervalTime);
                }
            }
        }
    }

    /**
     *
     * give the time line node a name
     *
     **/
    public void setTimeLineNodeName(final String name) {
        fModel.setTimeLineNodeName(name);
    }

    /**
     *
     * get the name of the time line node
     *
     **/
    public String getTimeLineNodeName() {
        return fModel.getTimeLineNodeName();
    }

    public String shortenTimeLineNodeName(final String name) { return null; }

    /**
     *
     * set the parent time line
     *
     **/
    public void setParent(final TimeLine timeLine) {
        fTimeLine = timeLine;
        TimeLineModel model = null;
        if (timeLine != null) {
            model = timeLine.getModel();
        }
        fModel.setParent(model);
        fDragMode = TimeLineNode.UNSELECTED;
        fMode = TimeLineNode.UNSELECTED;

        fForceRecalculation = true;
    }

    /**
     *
     * get the parent time line
     *
     **/
    public TimeLine getParent() {
        return fTimeLine;
    }

    /**
     *
     * calculate thumb's dimensions in pixels
     *
     **/
    protected void calculateNodeDimensions() {
        fThumbBegin = fTimeLine.getPointForTime(fModel.getStartTime());
        fThumbEnd = fTimeLine.getPointForTime(fModel.getEndTime());
    }

    /**
     *
     * returns whether the node is currently being dragged
     *
     **/
    public boolean isDragging() {
        return fDragMode != TimeLineNode.UNSELECTED;
    }

    /**
     *
     * returns whther the node is currently selected.  Note:  This method will only return true
     * if the whole node is selected.
     *
     **/
    public boolean isSelected() {
        return (fMode == TimeLineNode.NODE_SELECTED);
    }

    /**
     *
     * revert the time line node to its previous position
     *
     **/
    public void revertToPrevious() {
        fModel.revertToPrevious();
    }

    public void vetoableChange(final PropertyChangeEvent evt) throws DetailedPropertyVetoException {

        if (!TimeLine.NODE_REMOVED.equals(evt.getPropertyName()) && (evt.getSource() instanceof TimeLineNode)) {
            final TimeLineNode node = (TimeLineNode) evt.getSource();

            if ((node != this) && (intersects(node) || node.intersects(this))) {
                throw new DetailedPropertyVetoException(this, TimeLineNode.NODE_OVERLAP,
                                                        "node " + getTimeLineNodeName() + " overlaps "
                                                        + node.getTimeLineNodeName(), evt);
            }
        }
    }

    /**
     *
     * returns what area of a time line node a point exists in
     *
     **/
    public int getAreaForPoint(final Point pt) {
        int result = TimeLineNode.UNSELECTED;
        if (fLeftHandle.contains(pt.x, pt.y)) {
            result = TimeLineNode.LEFT_HANDLE_SELECTED;
        } else if (fRightHandle.contains(pt.x, pt.y)) {
            result = TimeLineNode.RIGHT_HANDLE_SELECTED;
        } else if (fThumb.contains(pt.x, pt.y)) {
            result = TimeLineNode.NODE_SELECTED;
        }
        return result;
    }

    /**
     *
     * returns whether the node intersects the passed in node
     *
     **/
    public boolean intersects(final TimeLineNode node) {
        return fModel.intersects(node.getModel());
    }

    /**
     *
     * returns the center point for the time line node
     *
     **/
    public Point getCenterPoint() {
        final double value = fModel.getStartTime().getValue() +
                (fModel.getEndTime().getValue() - fModel.getStartTime().getValue()) / 2.0;
        final Time centerTime = new Time(value);
        final float x = fTimeLine.getPointForTime(centerTime);
        final float y = fTimeLine.getHeight() / 2f;
        return new Point(Math.round(x), Math.round(y));
    }

    public Rectangle getBounds() {
        final double startTime = fModel.getStartTime().getValue();
        final double endTime   = fModel.getEndTime().getValue();
        final float  x         = fTimeLine.getPointForTime(new Time(startTime));
        final float  w         = fTimeLine.getPointForTime(new Time(endTime)) - x;
        final float  h         = fTimeLine.getHeight();
        return new Rectangle(Math.round(x), 0, Math.round(w), Math.round(h));
    }

    /**
     *
     * move node to a specified location
     *
     **/
    public synchronized void setTimeLineNode(final Time start, final Time end) throws DetailedPropertyVetoException {
        final Time oldStartTime = getStartTime();
        final Time oldEndTime = getEndTime();
        try {
            fModel.setValidTimeLineNode(start, end);

            if (fTimeLine != null) {
                Time displayEdge = fTimeLine.getDisplayStart();
                if ((fModel.getStartTime().getValue() < displayEdge.getValue() &&
                        oldStartTime.getValue() >= displayEdge.getValue())) {
                    fChangeSupport.fireVetoableChange(TimeLineNode.HIT_LEFT_EDGE,
                                                      oldStartTime, fModel.getStartTime());
                    fForceRecalculation = true;
                }

                displayEdge = fTimeLine.getDisplayEnd();
                if ((fModel.getEndTime().getValue() > displayEdge.getValue() &&
                        oldEndTime.getValue() <= displayEdge.getValue())) {
                    fChangeSupport.fireVetoableChange(TimeLineNode.HIT_RIGHT_EDGE, oldEndTime, fModel.getEndTime());
                    fForceRecalculation = true;
                }
            }

        } catch (DetailedPropertyVetoException ex) {
            fForceRecalculation = true;
            throw ex;
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * returns whether the specified point is in the node
     *
     **/
    public boolean containsPoint(final Point pt) {
        boolean result = false;
        final Time time = fTimeLine.getTimeForPoint(pt.x);
        if ((time.getValue() >= fModel.getStartTime().getValue()) &&
                (time.getValue() <= fModel.getEndTime().getValue())) {
            result = true;
        }
        return result;
    }


    /**
     *
     * returns a description for the area at the specified point
     *
     **/
    public String getDescription(final Point pt) {
        final String units = fTimeLine.getUnitsType();
        if (TimeLine.DATE_VIEW.equals(units) || TimeLine.TIME_VIEW.equals(units))
            return getTimeLineNodeName();

        String result = null;
        if (fLeftHandle.contains(pt.x, pt.y)) {
            final DecimalFormat form = new DecimalFormat();
            form.setMaximumFractionDigits(2);
            final String str = form.format(getStartTime().getValue(units));
            result = str + " " + Time.getUnitsAbbrev(units);
        } else if (fRightHandle.contains(pt.x, pt.y)) {
            final DecimalFormat form = new DecimalFormat();
            form.setMaximumFractionDigits(2);
            final String str = form.format(getEndTime().getValue(units));
            result = str + " " + Time.getUnitsAbbrev(units);
        } else if (fThumb.contains(pt.x, pt.y)) {
            result = getTimeLineNodeName();
        }

        return result;
    }

    /**
     *
     * get the node's underlying model
     *
     **/
    public TimeLineNodeModel getModel() {
        return fModel;
    }

    /**
     *
     * set the time line node's underlying model
     *
     **/
    public void setModel(final TimeLineNodeModel model) {
        if (model != fModel) {
            if (fModel != null) {
                fModel.removeVetoableChangeListener(fMyVetoListener);
            }
            fModel = (VetoableTimeLineNodeModel) model;
            init();
        }
    }

    /**
     *
     * get the cursor for the specified point
     *
     */
    public Cursor getCursor(final MouseEvent evt) {
        Cursor result = TimeLine.DEFAULT_CURSOR;
        final Point pt = evt.getPoint();
        if (fLeftHandle.contains(pt.x, pt.y) ||
                fRightHandle.contains(pt.x, pt.y) ||
                fThumb.contains(pt.x, pt.y)) {
            if (TimeLine.SELECTION_MODE.equals(getParent().getMode())) {
                result = MOVE_CURSOR;
            }
        }

        return result;
    }

    public String toString() {
        return fModel.toString();
    }


}

