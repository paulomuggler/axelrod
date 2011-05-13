package br.edu.cultural.gui;

import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

	public class ClassNameComboBoxRenderer extends BasicComboBoxRenderer{
		private static final long serialVersionUID = 6965028171205010114L;
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setText(humanize(((Class<?>) value).getSimpleName()));
			return c;
		}
		
		public static String humanize(String camelized){
			Pattern upper = Pattern.compile("[A-Z][a-z]*");
			Matcher m = upper.matcher(camelized);
			StringBuilder sb = new StringBuilder();
			while(m.find())
			sb.append(m.group().toLowerCase()+" ");
			return sb.toString();
		}
	}