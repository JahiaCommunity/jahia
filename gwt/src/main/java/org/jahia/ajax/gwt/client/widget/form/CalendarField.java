/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.form;

import java.util.Date;

import org.jahia.ajax.gwt.client.widget.calendar.CalendarPicker;
import org.jahia.ajax.gwt.client.widget.menu.CalendarMenu;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DomEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.menu.DateMenu;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;

/**
 * Provides an input field with a date and time picker.
 * 
 * @author Sergiy Shyrkov
 */
public class CalendarField extends TriggerField<Date> {

    public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm";

    /**
     * DateField error messages.
     */
    public class DateFieldMessages extends TextFieldMessages {

      private String minText;
      private String maxText;
      private String invalidText;

      /**
       * Returns the invalid text.
       * 
       * @return the invalid text
       */
      public String getInvalidText() {
        return invalidText;
      }

      /**
       * Returns the max error text.
       * 
       * @return the error text
       */
      public String getMaxText() {
        return maxText;
      }

      /**
       * Returns the min error text.
       * 
       * @return the error text
       */
      public String getMinText() {
        return minText;
      }

      /**
       * "The error text to display when the date in the field is invalid " +
       * "(defaults to '{value} is not a valid date - it must be in the format
       * {format}')."
       * 
       * @param invalidText the invalid text
       */
      public void setInvalidText(String invalidText) {
        this.invalidText = invalidText;
      }

      /**
       * Sets the error text to display when the date in the cell is after
       * maxValue (defaults to 'The date in this field must be before {
       * {@link #setMaxValue}').
       * 
       * @param maxText the max error text
       */
      public void setMaxText(String maxText) {
        this.maxText = maxText;
      }

      /**
       * The error text to display when the date in the cell is before minValue
       * (defaults to 'The date in this field must be after {@link #setMinValue} 
       * ').
       * 
       * @param minText the min text
       */
      public void setMinText(String minText) {
        this.minText = minText;
      }

    }

    private Date minValue;
    private Date maxValue;
    private DateMenu menu;
    private boolean formatValue;
    private boolean displayTime;

    /**
     * Initializes an instance of this class.
     */
    public CalendarField() {
        this(DEFAULT_DATE_FORMAT, true, false, null, false, null);
    }

    /**
     * Creates a new date field.
     */
    public CalendarField(Date date) {
        this(DEFAULT_DATE_FORMAT, true, false, null, false, date);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param datePattern
     * @param displayTime
     * @param readOnly
     * @param fieldName
     * @param shadow
     * @param value
     */
    public CalendarField(String datePattern, boolean displayTime,
            boolean readOnly, final String fieldName, boolean shadow, Date value) {
        super();
        autoValidate = false;
        propertyEditor = new DateTimePropertyEditor(datePattern);
        messages = new DateFieldMessages();
        setTriggerStyle("x-form-date-trigger");
        this.displayTime = displayTime; 

        if (value != null) {
            // dateField.setValue(value);
            setValue(value);
        }

        if (fieldName != null && fieldName.length() > 0) {
            setName(fieldName);
            setItemId(fieldName);
        }

        setReadOnly(readOnly);
        setHideTrigger(readOnly);
        setShadow(shadow);
    }

    /**
     * Returns the field's date picker.
     * 
     * @return the date picker
     */
    public DatePicker getDatePicker() {
      if (menu == null) {
        menu = displayTime ? new CalendarMenu() : new DateMenu();

        menu.getDatePicker().addListener(Events.Select, new Listener<ComponentEvent>() {
          public void handleEvent(ComponentEvent ce) {
            focusValue = getValue();
            Date date = menu.getDate();
            if (displayTime) {
                date.setHours(((CalendarPicker) menu.getDatePicker()).getSelectedHour());
                date.setMinutes(((CalendarPicker) menu.getDatePicker()).getSelectedMinute());
            }
            setValue(date);
            menu.hide();
            el().blur();
          }
        });
        menu.addListener(Events.Hide, new Listener<ComponentEvent>() {
          public void handleEvent(ComponentEvent be) {
            focus();
          }
        });
      }
      return menu.getDatePicker();
    }

    /**
     * Returns the field's max value.
     * 
     * @return the max value
     */
    public Date getMaxValue() {
      return maxValue;
    }

    @Override
    public DateFieldMessages getMessages() {
      return (DateFieldMessages) messages;
    }

    /**
     * Returns the field's min value.
     * 
     * @return the min value
     */
    public Date getMinValue() {
      return minValue;
    }
    
    @Override
    public DateTimePropertyEditor getPropertyEditor() {
      return (DateTimePropertyEditor) propertyEditor;
    }

    /**
     * Returns true if formatting is enabled.
     * 
     * @return the format value state
     */
    public boolean isFormatValue() {
      return formatValue;
    }

    /**
     * True to format the user entered value using the field's property editor
     * after passing validation (defaults to false). Format value should not be
     * enabled when auto validating.
     * 
     * @param formatValue true to format the user value
     */
    public void setFormatValue(boolean formatValue) {
      this.formatValue = formatValue;
    }

    /**
     * Sets the field's max value.
     * 
     * @param maxValue the max value
     */
    public void setMaxValue(Date maxValue) {
      if (maxValue != null) {
        maxValue = new DateWrapper(maxValue).clearTime().asDate();
      }
      this.maxValue = maxValue;
    }

    /**
     * The maximum date allowed.
     * 
     * @param minValue the max value
     */
    public void setMinValue(Date minValue) {
      if (minValue != null) {
        minValue = new DateWrapper(minValue).clearTime().asDate();
      }
      this.minValue = minValue;
    }
    
    @Override
    public void setRawValue(String value) {
      super.setRawValue(value);
    }

    protected void expand() {
      DatePicker picker = getDatePicker();

      Object v = getValue();
      Date d = null;
      if (v instanceof Date) {
        d = (Date) v;
      } else {
        d = new Date();
      }
      picker.setValue(d, true);
      picker.setMinDate(minValue);
      picker.setMaxDate(maxValue);

      menu.show(wrap.dom, "tl-bl?");
      menu.focus();
    }

    protected void onDown(FieldEvent fe) {
      fe.cancelBubble();
      if (menu == null || !menu.isAttached()) {
        expand();
      }
    }

    @Override
    protected void onKeyPress(FieldEvent fe) {
      super.onKeyPress(fe);
      int code = fe.getKeyCode();
      if (code == 8 || code == 9) {
        if (menu != null && menu.isAttached()) {
          menu.hide();
        }
      }
    }

    @Override
    protected void onRender(Element target, int index) {
      super.onRender(target, index);

      new KeyNav<FieldEvent>(this) {
        public void onDown(FieldEvent fe) {
          CalendarField.this.onDown(fe);
        }
      };
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) {
      super.onTriggerClick(ce);
      if (isReadOnly()) {
        return;
      }

      expand();
      
      getInputEl().focus();
    }

    protected boolean validateBlur(DomEvent e,Element target){
        return menu == null|| (menu != null && !menu.isVisible());
    }

    @Override
    protected boolean validateValue(String value) {
      if (!super.validateValue(value)) {
        return false;
      }
      if (value.length() < 1) { // if it's blank and textfield didn't flag it then
        // it's valid
        return true;
      }

      DateTimeFormat format = getPropertyEditor().getFormat();

      Date date = null;

      try {
        date = getPropertyEditor().convertStringValue(value);
      } catch (Exception e) {

      }

      if (date == null) {
        String error = null;
        if (getMessages().getInvalidText() != null) {
          error = Format.substitute(getMessages().getInvalidText(), 0);
        } else {
          error = GXT.MESSAGES.dateField_invalidText(value, format.getPattern().toUpperCase());
        }
        markInvalid(error);
        return false;
      }

      if (minValue != null && date.before(minValue)) {
        String error = null;
        if (getMessages().getMinText() != null) {
          error = Format.substitute(getMessages().getMinText(), format.format(minValue));
        } else {
          error = GXT.MESSAGES.dateField_minText(format.format(minValue));
        }
        markInvalid(error);
        return false;
      }
      if (maxValue != null && date.after(maxValue)) {
        String error = null;
        if (getMessages().getMaxText() != null) {
          error = Format.substitute(getMessages().getMaxText(), format.format(maxValue));
        } else {
          error = GXT.MESSAGES.dateField_maxText(format.format(maxValue));
        }
        markInvalid(error);
        return false;
      }

      if (formatValue && getPropertyEditor().getFormat() != null) {
        setRawValue(getPropertyEditor().getFormat().format(date));
      }

      return true;
    }

}
