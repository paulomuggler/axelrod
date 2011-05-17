package br.edu.cultural.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import br.edu.cultural.plot.Plot;
import br.edu.cultural.simulation.CultureDisseminationSimulation;

class PlotAction extends AbstractAction {
	private static final long serialVersionUID = 8874503720547085785L;
	protected Plotter plotter;
	protected Plot<CultureDisseminationSimulation, ?> plot;
	private String pTitle;
	private JMenuItem theJMenuItemForThisAction;

	@SuppressWarnings("unchecked")
	public PlotAction(String actionCaption, String plotTitle, Plot<?, ?> p) {
		super(actionCaption);
		plot = (Plot<CultureDisseminationSimulation, ?>) p;
		pTitle = plotTitle;
		theJMenuItemForThisAction = new JMenuItem(this);
	}
	
	public JMenuItem menuItemForThisThanksVeryMuchJavaForNotBeingTooMuchVerbose(){
		return theJMenuItemForThisAction;
	}

	public void actionPerformed(ActionEvent e) {
		if (plotter == null) {
			plotter = new Plotter(pTitle, plot);
			plotter.addChartScaleSelectorX("time");
			plotter.validate();
			plotter.repaint();
		}
		plotter.mostra();
	}

	public void link(CultureDisseminationSimulation sim) {
		plot.createPlot(sim);
		sim.addListener(plot);
	}

	public void unlink(CultureDisseminationSimulation sim) {
		sim.removeListener(plot);
	}

	public String toString() {
		return pTitle;
	}
}
