package br.edu.axelrod.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jfree.chart.ChartPanel;

import br.edu.axelrod.plot.ActiveNodesScatterPlot;
import br.edu.axelrod.plot.Plot;

public class Plotter extends JFrame {
	private static final long serialVersionUID = 5182011718579471296L;
	
	private Plot<?> plot;
	private ChartPanel chartPanel;
	public JPanel content;
	
	public Plotter(String windowTitle, Plot<?> p){
		super(windowTitle);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		plot = p;
		
		chartPanel = new ChartPanel(p.chart());
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		
		content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(chartPanel);
		
		getContentPane().add(content);
	}
	
	public void addChartScaleSelectorX(){
		JPanel selectScale = new JPanel();
		ButtonGroup group = new ButtonGroup();

		JRadioButton linear = new JRadioButton("linear");
		linear.setMnemonic(KeyEvent.VK_L);
		linear.setActionCommand("linear");
		linear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ActiveNodesScatterPlot) plot).linearX("time");
			}
		});
		group.add(linear);
		selectScale.add(linear);

		JRadioButton logarithmic = new JRadioButton("logarithmic");
		logarithmic.setMnemonic(KeyEvent.VK_G);
		logarithmic.setActionCommand("logarithmic");
		logarithmic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ActiveNodesScatterPlot) plot).logarythmicX("time");
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
	
	public Plot<?> plot(){ return plot; }

}
