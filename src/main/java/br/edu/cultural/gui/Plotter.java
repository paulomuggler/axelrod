package br.edu.cultural.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jfree.chart.ChartPanel;

import br.edu.cultural.plot.Plot;

public class Plotter extends JFrame {
	private static final long serialVersionUID = 5182011718579471296L;
	
	private Plot<?, ?> plot;
	private ChartPanel chartPanel;
	public JPanel content;
	
	public Plotter(String windowTitle, Plot<?, ?> p){
		super(windowTitle);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		plot = p;
		
		chartPanel = new ChartPanel(p.chart());
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		addExportToCsvOption();
		
		content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(chartPanel);
		
		getContentPane().add(content);
	}
	
	public void addChartScaleSelectorX(final String axisLabel){
		JPanel selectScale = new JPanel();
		ButtonGroup group = new ButtonGroup();

		JRadioButton linear = new JRadioButton("linear");
		linear.setMnemonic(KeyEvent.VK_L);
		linear.setActionCommand("linear");
		linear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plot.linearX(axisLabel);
			}
		});
		group.add(linear);
		selectScale.add(linear);

		JRadioButton logarithmic = new JRadioButton("logarithmic");
		logarithmic.setMnemonic(KeyEvent.VK_G);
		logarithmic.setActionCommand("logarithmic");
		logarithmic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plot.logarythmicX(axisLabel);
			}
		});
		group.add(logarithmic);
		selectScale.add(logarithmic);

		content.add(selectScale);
	}
	
	public void addChartScaleSelectorY(final String axisLabel) {
		JPanel selectScale = new JPanel();
		ButtonGroup group = new ButtonGroup();

		JRadioButton linear = new JRadioButton("linear");
		linear.setMnemonic(KeyEvent.VK_L);
		linear.setActionCommand("linear");
		linear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plot.linearY(axisLabel);
			}
		});
		group.add(linear);
		selectScale.add(linear);

		JRadioButton logarithmic = new JRadioButton("logarithmic");
		logarithmic.setMnemonic(KeyEvent.VK_G);
		logarithmic.setActionCommand("logarithmic");
		logarithmic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plot.logarythmicY(axisLabel);
			}
		});
		group.add(logarithmic);
		selectScale.add(logarithmic);

		content.add(selectScale);
	}
	
	public void mostra(){
		this.pack();
		this.setVisible(true);
	}
	
	public Plot<?, ?> plot(){ return plot; }

	private void addExportToCsvOption(){
		JMenuItem exportToCsv = new JMenuItem(new AbstractAction("Export to .csv...") {
			private static final long serialVersionUID = -7965277270950293468L;
			public void actionPerformed(ActionEvent e) {
				int select = MainApplicationFrame.getFileChooser().showSaveDialog(Plotter.this);
				if (select == JFileChooser.APPROVE_OPTION) {
					File f = MainApplicationFrame.getFileChooser().getSelectedFile();
					try {
						Plotter.this.plot.export_series_to_csv(f);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(Plotter.this,
								"Error saving to file: " + e1.getMessage(),
								"Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		this.chartPanel.getPopupMenu().add(exportToCsv, 3);
	}

}
