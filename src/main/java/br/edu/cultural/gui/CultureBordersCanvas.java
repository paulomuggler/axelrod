/**
 * 
 */
package br.edu.cultural.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.edu.cultural.network.CulturalNetwork;

/**
 * @author muggler
 * 
 */

public class CultureBordersCanvas extends CultureCanvas {
	private static final long serialVersionUID = -5262909296764901151L;

	protected Insets thickness;
	protected Insets gap;
	
	
	public final List<int[]> dirtySites = Collections.synchronizedList(new ArrayList<int[]>());

	public CultureBordersCanvas(int canvasWidth, CulturalNetwork nw) {
		super(canvasWidth, nw);
		this.thickness = new Insets(1, 1, 1, 1);
		this.gap = new Insets(1, 1, 1, 1);
	}
	
	public void paintComponent(Graphics g){
		for (int nd = 0; nd < nw.n_nodes; nd++) {
			int[] site = {nd / nw.size, nd % nw.size};
			int node = site[0] * nw.size + site[1];
			int y = site[0] * this.siteWidth;
			int x = site[1] * this.siteWidth;
			int bColorUp, bColorRight, bColorDown, bColorLeft;
			bColorUp = bColorRight = bColorDown = bColorLeft = -1;
			for(int ni = 0; ni < nw.degree(node); ni++){
				int nbr = nw.node_neighbor(node, ni);
				int overlap = nw.overlap(node, nbr);
				int color = -1;
				double cos = 255 * Math.cos((overlap -1)*(Math.PI/this.nw.features-1));
				double bCos = cos > 0? cos : 0;
				double gSin = 255 * Math.sin((overlap -1)*(Math.PI/this.nw.features-1));
				double rCos = -cos > 0? -cos : 0;
				
				if(overlap == 0){
					color = 0;
				}else if(overlap == nw.features){
					color = 0xFFFFFF;
				}else{
					color = (int) (rCos * 0x10000 + gSin* 0x100 + bCos);
//					color = (int) (rCos * 0x10000 + bCos);
				}
				
				if(nbr == node - nw.size){
					bColorUp = color;
				}else if (nbr == node+1){
					bColorRight = color;
				}else if(nbr == node + nw.size){
					bColorDown = color;
				}else if(nbr == node - 1){
					bColorLeft = color;
				}
				
				// top
				g.setColor(new Color(bColorUp));
				for (int i = 0; i < thickness.top; i++) {
					g.drawLine(x, y + i, x + siteWidth, y + i);
				}
				// right
				g.setColor(new Color(bColorRight));
				for (int i = 0; i < thickness.right; i++) {
					g.drawLine(x + siteWidth - i - 1, y, x + siteWidth - i - 1, y + siteWidth);
				}
				// bottom
				g.setColor(new Color(bColorDown));
				for (int i = 0; i < thickness.bottom; i++) {
					g.drawLine(x, y + siteWidth - i - 1, x + siteWidth, y + siteWidth - i - 1);
				}
				// left
				g.setColor(new Color(bColorLeft));
				for (int i = 0; i < thickness.left; i++) {
					g.drawLine(x + i, y, x + i, y + siteWidth);
				}
				
			}
		}
	}

}
