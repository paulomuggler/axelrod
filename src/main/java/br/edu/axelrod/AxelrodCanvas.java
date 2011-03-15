/**
 * 
 */
package br.edu.axelrod;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author muggler
 * 
 */

public class AxelrodCanvas extends JPanel {

	private static final long serialVersionUID = -5262909296764901151L;

	private final int siteWidth;
	private final int canvasWidth;
	private final RgbPartitioner rp; // O(335.5) MB - see RgbPartitioner.java
	private final AxelrodNetwork nw;

	protected Insets thickness;

	protected Color lineColor;

	protected Insets gap;

	private final boolean borders;

	public AxelrodCanvas(int canvasWidth, AxelrodNetwork nw, boolean borders) {
		super();
		this.nw = nw;
		this.canvasWidth = canvasWidth - (canvasWidth % this.nw.size);
		this.siteWidth = (int) canvasWidth / this.nw.size;
		this.rp = new RgbPartitioner(nw.features, nw.traits);
		this.borders = borders;
		
		this.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				int mouseX = e.getX();
				int mouseY = e.getY();
				int i = (mouseY / AxelrodCanvas.this.siteWidth);
				int j = (mouseX / AxelrodCanvas.this.siteWidth);
				int node = i*AxelrodCanvas.this.nw.size+ j;
				String out = String.format("node: %d, %d, state %s", i, j, State.toString(AxelrodCanvas.this.nw.states[node]));
				System.out.println(out);
//				AxelrodCanvas.this.setToolTipText(out);
			}

			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		
		this.setPreferredSize(new Dimension(this.canvasWidth, this.canvasWidth));
		JFrame frame = new JFrame("Axelrod Simulation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
		
		lineColor = new Color(0,0,0);
		this.thickness = new Insets(1, 1, 1, 1);
		this.gap = new Insets(1, 1, 1, 1);

		this.initCanvas(nw);

	}

	/**
	 * @param nw
	 */
	public void initCanvas(AxelrodNetwork nw) {
		for (int nd = 0; nd < nw.n_nodes; nd++) {
			this.updateSite(nd / nw.size, nd % nw.size, nw.states[nd]);
		}
	}

	public void updateSite(int i, int j, int[] state) {
		Graphics g = getGraphics();
		int y = i * this.siteWidth;
		int x = j * this.siteWidth;
		int color = this.rp.color(state);
		g.setColor(new Color(color));
		g.fillRect(x, y, this.siteWidth, this.siteWidth);
		if(borders){
			this.paintBorder(g, x, y, this.siteWidth, this.siteWidth);
		}
	}

	public void paintBorder( Graphics g, int x, int y, int width,
			int height) {
		Color oldColor = g.getColor();

		g.setColor(lineColor);
		// top
		for (int i = 0; i < thickness.top; i++) {
			g.drawLine(x, y + i, x + width, y + i);
		}
		// bottom
		for (int i = 0; i < thickness.bottom; i++) {
			g.drawLine(x, y + height - i - 1, x + width, y + height - i - 1);
		}
		// right
		for (int i = 0; i < thickness.right; i++) {
			g.drawLine(x + width - i - 1, y, x + width - i - 1, y + height);
		}
		// left
		for (int i = 0; i < thickness.left; i++) {
			g.drawLine(x + i, y, x + i, y + height);
		}
		g.setColor(oldColor);
	}

	/**
	 * @return the canvasWidth
	 */
	public int getCanvasWidth() {
		return canvasWidth;
	}
}
