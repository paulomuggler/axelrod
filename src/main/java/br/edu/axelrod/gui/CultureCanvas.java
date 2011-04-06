/**
 * 
 */
package br.edu.axelrod.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import br.edu.axelrod.network.CulturalNetwork;
import br.edu.axelrod.network.State;

/**
 * @author muggler
 * 
 */

public class CultureCanvas extends JPanel {

	private static final long serialVersionUID = -5262909296764901151L;

	private final int siteWidth;
	private final int canvasWidth;
	public final RgbPartitioner rp;
	private final CulturalNetwork nw;

	protected Insets thickness;
	protected Color lineColor;
	protected Insets gap;
	private final boolean borders;
	
	private int nodeClicked;
	private int[] clickNwCoords;
	
	public int[] stateStroke;
	JPopupMenu nodeContextMenu = new JPopupMenu("Node");
	
	public final List<int[]> dirtySites = Collections.synchronizedList(new ArrayList<int[]>());

	public CultureCanvas(int canvasWidth, CulturalNetwork nw, boolean borders) {
		super();
		this.nw = nw;
		this.canvasWidth = canvasWidth - (canvasWidth % this.nw.size);
		this.siteWidth = (int) canvasWidth / this.nw.size;
		
		this.rp = new RgbPartitioner(nw.features, nw.traits);
		this.borders = borders;
		
		this.stateStroke = new int[nw.features];
		for (int i = 0; i < stateStroke.length; i++) {
			stateStroke[i] = 0;
		}
		
		this.addMouseListener(contextMenu);
//		this.addMouseListener(stateInspector);
		this.addMouseListener(statePen);
		this.addMouseListener(stateRect);
		
		nodeContextMenu.add(setStateStroke);
		nodeContextMenu.add(printNodeState);
		nodeContextMenu.add(monitorNode);
		
		this.setPreferredSize(new Dimension(this.canvasWidth, this.canvasWidth));
		
		lineColor = new Color(0,0,0);
		this.thickness = new Insets(1, 1, 1, 1);
		this.gap = new Insets(1, 1, 1, 1);

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
			if(borders){
				this.paintBorder(g, x, y, this.siteWidth, this.siteWidth);
			}
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
	
	Action setStateStroke = new AbstractAction("set paint color") {
		private static final long serialVersionUID = 4857404375859735556L;
		public void actionPerformed(ActionEvent e) {
			System.arraycopy(nw.states[nodeClicked], 0, stateStroke, 0, stateStroke.length);
			Component parent = getParent();
			do parent = parent.getParent(); while (!(parent instanceof MainApplicationFrame));
			((MainApplicationFrame)parent).updateStroke();
		}
	};
	
	Action printNodeState = new AbstractAction("print node state") {
		private static final long serialVersionUID = 4857404375859735556L;
		public void actionPerformed(ActionEvent e) {
			String out = String.format("node: %d, (%d, %d), state %s", (clickNwCoords[0]*nw.size + clickNwCoords[1]), clickNwCoords[0], clickNwCoords[1], State.toString(CultureCanvas.this.nw.states[nodeClicked]));
			System.out.println(out);
		}
	};

	Action monitorNode = new AbstractAction("toggle monitor for this node") {
		private static final long serialVersionUID = 4857404375859735556L;
		public void actionPerformed(ActionEvent e) {
			Component parent = getParent();
			do parent = parent.getParent(); while (!(parent instanceof MainApplicationFrame));
			boolean monitoring = ((MainApplicationFrame)parent).sim.toggleMonitor(nodeClicked);
			String out = String.format((monitoring ? "added node to monitoring" : "removed node from monitoring")+": %d, %d, %d, state %s", (clickNwCoords[1]*nw.size + clickNwCoords[0]), clickNwCoords[0], clickNwCoords[1], State.toString(CultureCanvas.this.nw.states[nodeClicked]));
			System.out.println(out);
		}
	};
	
	private void recordClickCoords(MouseEvent e){
		nodeClicked = getClickedNode(e);
		clickNwCoords = networkCoordsForClick(e);
	}
	
	private MouseListener contextMenu = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON3){
				recordClickCoords(e);
				nodeContextMenu.show(CultureCanvas.this, e.getX(), e.getY());
			}
		}
	};
	
	@SuppressWarnings("unused")
	private MouseListener stateInspector = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1){
				int node = getClickedNode(e);
				setStateStroke(CultureCanvas.this.nw.states[node]);
				int[] coords = networkCoordsForClick(e);
				String out = String.format("node: %d, %d, state %s", coords[0], coords[1], State.toString(CultureCanvas.this.nw.states[node]));
				System.out.println(out);
			}
		}
	};
	
	private MouseListener statePen = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1){
				int node = getClickedNode(e);
				setNwState(node, stateStroke);
			}
		}
	};
	
	private MouseListener stateRect = new MouseAdapter() {
		int[] dragStarted;
		int[] dragEnded;
		public void mousePressed(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1){
				dragStarted = networkCoordsForClick(e);
			}
		}
		public void mouseReleased(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1){
				dragEnded = networkCoordsForClick(e);
				int startX, startY, endX, endY;
				if(dragStarted[0] < dragEnded[0]){
					startX = dragStarted[0];
					endX = dragEnded[0];
				}else{
					endX = dragStarted[0];
					startX = dragEnded[0];
				}
				if(dragStarted[1] < dragEnded[1]){
					startY = dragStarted[1];
					endY = dragEnded[1];
				}else{
					endY = dragStarted[1];
					startY = dragEnded[1];
				}
				if(startX != endX || startY != endY){
					for (int i = startX; i <= endX; i++) {
						for (int j = startY; j <= endY; j++) {
							if(i < nw.size && j < nw.size){
								setNwState(i*nw.size + j, stateStroke);
							}
						}
					}
				}
			}
		}
	};
	
	private void setNwState(int node, int[] state){
		System.arraycopy(state, 0, nw.states[node], 0, nw.features);
		nw.update_representations(node);
		CultureCanvas.this.repaint();
	}
	
	private int getClickedNode(MouseEvent e) {
		int mouseX = e.getX();
		int mouseY = e.getY();
		int i = (mouseY / CultureCanvas.this.siteWidth);
		int j = (mouseX / CultureCanvas.this.siteWidth);
		int node = i*CultureCanvas.this.nw.size+ j;
		return node;
	}
	
	private int[] networkCoordsForClick(MouseEvent e){
		int[] coords = {e.getY() / CultureCanvas.this.siteWidth, e.getX() / CultureCanvas.this.siteWidth};
		return coords;
	}

	public void setStateStroke(int[] state) {
		System.arraycopy(state, 0, stateStroke, 0, nw.features);
	}
}
