/**
 * 
 */
package br.edu.cultural.gui;

import java.awt.Color;
import java.awt.Graphics;

import br.edu.cultural.network.CulturalNetwork;

/**
 * @author muggler
 * 
 */

public class CultureColorsCanvas extends CultureCanvas {
	private static final long serialVersionUID = -5262909296764901151L;

	public CultureColorsCanvas(int canvasWidth, CulturalNetwork nw) {
		super(canvasWidth, nw);
	}
	
	public void paintComponent(Graphics g){
		for (int nd = 0; nd < nw.n_nodes; nd++) {
			int[] site = {nd / nw.size, nd % nw.size};
			int node = site[0] * nw.size + site[1];
			int y = site[0] * this.siteWidth;
			int x = site[1] * this.siteWidth;
			int color = this.rp.color(this.nw.states[node]);
			g.setColor(new Color(color));
			g.fillRect(x, y, this.siteWidth, this.siteWidth);
		}
	}
}
