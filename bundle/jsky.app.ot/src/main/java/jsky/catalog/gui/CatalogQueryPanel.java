package jsky.catalog.gui;

import jsky.catalog.*;
import jsky.util.Preferences;
import jsky.util.Storeable;
import jsky.util.gui.GridBagUtil;
import jsky.util.gui.MultiSelectComboBox;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Displays a panel containing widgets to use for searching a given
 * catalog. Based on the catalog configuration information, different
 * search fields are presented.
 */
public class CatalogQueryPanel extends JPanel
        implements ActionListener, Storeable {

    /** Isets used for labels in the GridBagLayout */
    protected static final Insets LABEL_INSETS = new Insets(6, 6, 0, 0);

    /** Isets used for values in the GridBagLayout */
    protected static final Insets VALUE_INSETS = new Insets(6, 6, 0, 3);

    /** Catalog we are accesing */
    protected Catalog _catalog;

    /** Array of labels displayed */
    private JLabel[] _labels;

    /** Array of components displayed next to the labels */
    protected JComponent[] _components;

    /** List of listeners for action events (called when <Enter> is typed in a text box). */
    private EventListenerList _actionListenerList = new EventListenerList();

    /** list of listeners for change events (called when an item is selected from a JComboBox) */
    private EventListenerList _changeListenerList = new EventListenerList();

    /** Used to format floating point values */
    protected NumberFormat nf = NumberFormat.getInstance(Locale.US);

    /** Layout utility class */
    private GridBagUtil _layout = new GridBagUtil(this);

    /**
     * The number of columns to use for the display
     * (should be an even number: each label/value pair counts as 2 columns)
     */
    private int _numCols;

    /** Used to keep the combo boxes from getting too wide */
    protected static final Dimension MIN_COMPONENT_SIZE = new Dimension(80, 21);


    /**
     * Create a CatalogQueryPanel containing catalog specific labels and
     * components for the search parameters specified in the catalog configuration
     * file.
     *
     * @param catalog the catalog to use
     * @param numCols the number of columns to use for the display (should be an even number)
     */
    public CatalogQueryPanel(Catalog catalog, int numCols) {
        _catalog = catalog;
        _numCols = numCols;
        nf.setGroupingUsed(false);

        // create label and entry widgets
        makePanelItems();
        doGridBagLayout(_layout);
    }

    /** Return the number of columns to use for the display (should be an even number) */
    protected int getNumCols() {
        return _numCols;
    }

    /** Update the display to reflect any changes in the catalog's query parameters */
    public void update() {
        QueryArgs queryArgs = getQueryArgs();
        removePanelItems();
        makePanelItems();
        doGridBagLayout(_layout);
        setQueryArgs(queryArgs);
        revalidate();
        repaint();
    }

    /**
     * Register to receive action events from this object whenever
     * a query parameter is changed.
     */
    public void addActionListener(ActionListener l) {
        _actionListenerList.add(ActionListener.class, l);
    }

    /**
     * Stop receiving action events from this object.
     */
    public void removeActionListener(ActionListener l) {
        _actionListenerList.remove(ActionListener.class, l);
    }

    /**
     * Notify any action listeners.
     */
    protected void fireActionEvent() {
        Object[] listeners = _actionListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(new ActionEvent(this, 0, null));
            }
        }
    }

    /** Called when a query parameter is changed to start the search.*/
    public void actionPerformed(ActionEvent e) {
        fireActionEvent();
    }

    /**
     * Register to receive change events from this object whenever
     * an item is selected from a JComboBox.
     */
    public void addChangeListener(ChangeListener l) {
        _changeListenerList.add(ChangeListener.class, l);
    }

    /**
     * Stop receiving change events from this object.
     */
    public void removeChangeListener(ChangeListener l) {
        _changeListenerList.remove(ChangeListener.class, l);
    }

    /**
     * Notify any listeners that a query parameter (from a JComboBox) has changed.
     */
    protected void fireChange(JComponent c) {
        Object[] listeners = _changeListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(new ChangeEvent(c));
            }
        }
    }

    /**
     * Make panel label with the given text
     */
    protected JLabel makeLabel(String s) {
        if (s == null)
            return null;
        return new JLabel(s, JLabel.LEFT);
    }

    /** Return the catalog for this object */
    public Catalog getCatalog() {
        return _catalog;
    }

    /**
     * Make and return the component for entering the value of the
     * given query parameter.
     */
    protected JComponent makeComponent(FieldDesc p) {
        if (p.getNumOptions() > 0) {
            return makeComboBox(p);
        }

        JTextField tf = makeTextField(10);
        String s = p.getDescription();
        if (s != null)
            tf.setToolTipText(s);
        Object o = p.getDefaultValue();
        if (o != null)
          tf.setText(p.getDisplayString(o));
        return tf;
    }

    /**
     * Make and return a text field with the given width.
     */
    protected JTextField makeTextField(int width) {
        JTextField tf = new JTextField(width);
        tf.addActionListener(this);
        return tf;
    }

    /**
     * Make and return a combo box with the values that the given field may have.
     */
    protected JComponent makeComboBox(FieldDesc p) {
        int n = p.getNumOptions();
        final JComboBox<String> cb = new JComboBox<>();
        String s = p.getDescription();
        if (s != null)
            cb.setToolTipText(s);

        // Check is there is a default option
        int def = p.getDefaultOptionIndex();
        if (def == -1)
            cb.addItem(""); // no default, empty means no option was specified

        for (int i = 0; i < n; i++) {
            cb.addItem(p.getOptionName(i));
        }

        cb.addActionListener(e -> fireChange(cb));

        cb.setPreferredSize(MIN_COMPONENT_SIZE);
        return cb;
    }

    /**
     * Make the display panel items
     */
    protected void makePanelItems() {
        int n = _catalog.getNumParams();
        if (n > 0) {
            _labels = new JLabel[n];
            _components = new JComponent[n];

            for (int i = 0; i < n; i++) {
                FieldDesc p = _catalog.getParamDesc(i);
                if (p == null || "hidden".equals(p.getType())) {
                    _labels[i] = null;
                    _components[i] = null;
                } else {
                    // add a label and component
                    String s = p.getName();
                    if (s == null)
                        s = "Field" + (i + 1);
                    _labels[i] = makeLabel(s);
                    _components[i] = makeComponent(p);
                }
            }
        }
    }

    /**
     * Remove the panel items.
     */
    protected void removePanelItems() {
        for (int i = 0; i < _labels.length; i++) {
            if (_labels[i] != null)
                remove(_labels[i]);
            if (_components[i] != null)
                remove(_components[i]);
        }
    }

    /** Return the text of the label corresponding to the given display component, or null if not found. */
    public String getLabelForComponent(JComponent c) {
        for (int i = 0; i < _labels.length; i++) {
            if (_components[i] == c)
                return _labels[i].getText();
        }
        return null;
    }

    /** Return the display component corresponding to the given label text, or null if not found. */
    public JComponent getComponentForLabel(String s) {
        for (int i = 0; i < _labels.length; i++) {
            if (_labels[i].getText().equals(s))
                return _components[i];
        }
        return null;
    }

    /**
     * Combine the panel items in the correct layout.
     *
     * @param layout utility object used for the layout
     * @return the number of rows in the layout
     */
    protected int doGridBagLayout(GridBagUtil layout) {
        int row = 0;
        int col = 0;
        int n = _catalog.getNumParams();
        for (int i = 0; i < n; i++) {
            if (_labels[i] != null || _components[i] != null) {
                if (_labels[i] != null)
                    layout.add(_labels[i], col, row, 1, 1, 0.0, 0.0,
                               GridBagConstraints.NONE, GridBagConstraints.EAST, LABEL_INSETS);
                col++;

                if (_components[i] != null)
                    layout.add(_components[i], col, row, 1, 1, 1.0, 0.0,
                               GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, VALUE_INSETS);
                col++;

                if (col >= _numCols) {
                    col = 0;
                    row++;
                }
            }
        }

        if (col != 0) {
            col = 0;
            row++;
        }

        // add the catalog description text
        String desc = _catalog.getDescription();
        if (desc != null) {
            JLabel label = new JLabel(desc);
            layout.add(label, col, row, _numCols, 1, 1.0, 0.0,
                       GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, LABEL_INSETS);
            row++;
        }
        return row;
    }

    /**
     * Initialize a QueryArgs object based on the current panel settings
     * that can be passed to the Catalog.query() method.
     */
    public void initQueryArgs(QueryArgs queryArgs) {
        int n = Math.min(_components.length, _catalog.getNumParams());
        for (int i = 0; i < n; i++) {
            if (_components[i] != null) {
                queryArgs.setParamValue(i, getValue(i));
            }
        }
    }

    public void storeUserSelection() {
        //Save this catalog, if it's an image server, to the user preferences
        if (_catalog.isImageServer()) {
            StringBuilder prop = new StringBuilder(_catalog.getName());
            //if this is an image server, it should have at least 7 items..
            //we need the last two.
            //And yes, this is lame...
            if (_catalog.getNumParams() >=7 ) {
                prop.append("*");
                prop.append(getValue(5));
                prop.append("*");
                prop.append(getValue(6));
            }
            Preferences.set(Catalog.SKY_USER_CATALOG, prop.toString());
        }
    }

    /**
     * Return a QueryArgs object based on the current panel settings
     * that can be passed to the Catalog.query() method.
     *
     * @return the QueryArgs object to use for a catalog query.
     */
    public QueryArgs getQueryArgs() {
        QueryArgs queryArgs = _catalog.getQueryArgs();
        initQueryArgs(queryArgs);
        return queryArgs;
    }

    /**
     * Set the values displayed in the query panel from the given object.
     *
     * @param queryArgs QueryArgs object describing the values (null values are ignored)
     *
     */
    public void setQueryArgs(QueryArgs queryArgs) {
        int n = Math.min(_components.length, _catalog.getNumParams());
        for (int i = 0; i < n; i++) {
            Object value = queryArgs.getParamValue(i);
            if (value != null)
                setValue(i, value);
        }
    }

    /** Return the value in the given component, or null if there is no value there. */
    protected Serializable getValue(FieldDesc p, JComponent c) {
        if (p.getNumOptions() > 0) {
            // must be a combo box
            JComboBox cb = (JComboBox) c;
            String s = (String) cb.getSelectedItem();
            if (s != null && s.length() != 0) {
                int n = p.getNumOptions();
                for (int j = 0; j < n; j++) {
                    if (p.getOptionName(j).equals(s))
                        return p.getOptionValue(j);
                }
            }
            return null;
        } else {
            // must be a text field
            String s = ((JTextField) c).getText();

            if (s == null || s.length() == 0)
                return null;

            // If the field describes a table column, look for a range of values,
            // possibly mixed with symbols, such as ">", "<", "<=", ">=".
            if (_catalog instanceof TableQueryResult)
                return FieldFormat.getValueRange(p, s);

            // Otherwise, the field must describe a parameter, so just get the value
            return p.parse(s);
        }
    }

    /** Return the value in the ith component, or null if there is no value there. */
    protected Serializable getValue(int i) {
        return getValue(_catalog.getParamDesc(i), _components[i]);
    }

    /** Set the value in the named component. */
    protected void setValue(String label, Object value) {
        int n = _catalog.getNumParams();
        for (int i = 0; i < n; i++) {
            FieldDesc param = _catalog.getParamDesc(i);
            if (param != null) {
                String name = param.getName();
                String id = param.getId();
                if ((id != null && id.equalsIgnoreCase(label)) || (name != null && name.equalsIgnoreCase(label))) {
                    setValue(i, value);
                    return;
                }
            }
        }
    }

    /** Set the value in the given component. */
    protected void setValue(int i, Object value) {
        JComponent c = _components[i];
        FieldDesc param = _catalog.getParamDesc(i);
        if (c instanceof JTextField) {
            String s;
            if (value instanceof Double)
                s = nf.format(((Double) value).doubleValue());
            else
                s = param.getDisplayString(value);
            ((JTextField) c).setText(s);
        } else if (c instanceof JComboBox) {
            ((JComboBox) c).setSelectedItem(value);
        } else if (c instanceof MultiSelectComboBox && value instanceof Object[]) {
            ((MultiSelectComboBox) c).setSelectedObjects((Object[])value);
        }
    }

    /** Store the current settings in a serializable object and return the object. */
    @Override
    public Object storeSettings() {
        Hashtable<String, Object> map = new Hashtable<>();
        int n = Math.min(_components.length, _catalog.getNumParams());
        for (int i = 0; i < n; i++) {
            if (_components[i] != null) {
                String name = _catalog.getParamDesc(i).getName();
                Object value = getValue(i);
                if (value == null) value = "";
                if (name != null)
                    map.put(name, value);
            }
        }
        return map;
    }

    /** Restore the settings previously stored. */
    @Override
    public boolean restoreSettings(Object obj) {
        if (obj instanceof Hashtable) {
            Hashtable map = (Hashtable) obj;
            for (Enumeration e = map.keys(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                Object value = map.get(name);
                setValue(name, value);
            }
            return true;
        }
        return false;
    }
}

