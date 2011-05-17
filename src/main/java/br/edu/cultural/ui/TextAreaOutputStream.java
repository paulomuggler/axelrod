package br.edu.cultural.ui;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class TextAreaOutputStream extends OutputStream {
	private JTextArea thisIsMyTextArea = null;
		
	public TextAreaOutputStream(final JTextArea tOut){
		thisIsMyTextArea = tOut;
	};
		
	public void write(int b) {
		byte[] bs = new byte[1];
		bs[0] = (byte) b;
		thisIsMyTextArea.append(new String(bs));
		thisIsMyTextArea.setCaretPosition(thisIsMyTextArea.getDocument().getLength());
	}
}