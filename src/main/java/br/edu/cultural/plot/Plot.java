package br.edu.cultural.plot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import br.edu.cultural.simulation.CultureDisseminationSimulation;
import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationEventListener;

public abstract class Plot<S extends CultureDisseminationSimulation, D extends XYDataset> implements SimulationEventListener {
	
	protected static final int DEFAULT_SERIES_KEY = 0;
	protected JFreeChart chart;
	protected S sim;
	protected D dataset;
	
	public abstract JFreeChart createPlot(S simulation);
	public abstract void plot();
	
	public void link(S simulation) {
		createPlot(simulation);
		sim.addListener(this);
	}
	public void unlink() {
		sim.removeListener(this);
	}
	
	public JFreeChart chart(){ return chart; }
	
	public String simInfo(){
		return String.format("L = %d, F = %d, q = %d", sim.nw.size, 
							 sim.nw.features, sim.nw.traits);
	}
	
	public void iteration() {}
	public void interaction(int i, int j) {}
	public void epoch() {}
	public void started() {}
	public void finished() {}
	
	public void linearX(String axisLabel){
		((XYPlot) chart.getPlot()).setDomainAxis(new NumberAxis(axisLabel));
	}
	public void logarythmicX(String axisLabel){
		try {
			((XYPlot) chart.getPlot()).setDomainAxis(new LogarithmicAxis(axisLabel));
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
			if(!e.getMessage().equals("Values less than or equal to zero not allowed with logarithmic axis")) throw e;
			System.err.println("Error: attempt to use log scale on an axis with values less than or equal to zero.");
		}
	}
	
	public void linearY(String axisLabel) {
		((XYPlot) chart.getPlot()).setRangeAxis(new NumberAxis(axisLabel));
	}
	public void logarythmicY(String axisLabel) {
		try {
			((XYPlot) chart.getPlot()).setRangeAxis(new LogarithmicAxis(axisLabel));
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
			if(!e.getMessage().equals("Values less than or equal to zero not allowed with logarithmic axis")) throw e;
			System.err.println("Error: attempt to use log scale on an axis with values less than or equal to zero.");
		}
	}
	
	public void export_series_to_csv(File f) throws IOException {
		if(!f.exists()) f.createNewFile();
		if(f.canWrite()){
			FileWriter fw = new FileWriter(f);
			fw.write(chart.getTitle().getText());
			fw.write('\n');
			fw.write(((XYPlot) chart.getPlot()).getDomainAxis().getLabel());
			fw.write(',');
			fw.write(((XYPlot) chart.getPlot()).getRangeAxis().getLabel());
			fw.write('\n');
			for (int i = 0; i < dataset.getItemCount(DEFAULT_SERIES_KEY); i++) {
				fw.write(Double.toString(dataset.getXValue(DEFAULT_SERIES_KEY, i)));
				fw.write(',');
				fw.write(Double.toString(dataset.getYValue(DEFAULT_SERIES_KEY, i)));
				fw.write('\n');
			}
			fw.close();
		}
	}
}
