package de.rcblum.overcollect.ui.utils;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class ComboBoxRenderer extends BasicComboBoxRenderer {

	/**
	 * A unique serial version identifier
	 * 
	 * @see Serializable#serialVersionUID
	 */
	private static final long serialVersionUID = -7406594603853060557L;

	private Color myBackground;
	private Color selectionBackground;

	public ComboBoxRenderer() {
		super();

		myBackground = UIManager.getColor("ComboBox.background");
		selectionBackground = UIManager.getColor("ComboBox.selectionBackground");
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		setText((String) value);

		if (isSelected)
			setBackground(selectionBackground);
		else
			setBackground(myBackground);

		return this;
	}

}