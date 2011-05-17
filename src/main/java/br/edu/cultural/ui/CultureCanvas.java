package br.edu.cultural.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;

import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.State;

public abstract class CultureCanvas extends JPanel {
	private static final long serialVersionUID = 8613295340995177231L;
	
	public final RgbPartitioner rp;
	
	protected int nodeClicked;
	protected int[] clickNwCoords;
	
	public int[] stateStroke;
	JPopupMenu nodeContextMenu = new JPopupMenu("Node");
	
	protected final int siteWidth;
	protected final int canvasWidth;
	protected final CulturalNetwork nw;
	
	public CultureCanvas(int canvasWidth, CulturalNetwork nw) {
		super(true);
		this.nw = nw;
		this.canvasWidth = canvasWidth - (canvasWidth % this.nw.size);
		this.siteWidth = (int) canvasWidth / this.nw.size;
		
		if(Math.pow(this.nw.traits, this.nw.features) > 0x1000000){
			this.rp = new OnTheFlyRgbPartitioner(nw.features, nw.traits);
		}else{
			this.rp = new PreCalcRgbPartitioner(nw.features, nw.traits);
		}
		
		this.stateStroke = new int[nw.features];
		for (int i = 0; i < stateStroke.length; i++) {
			stateStroke[i] = 0;
		}
		
		this.addMouseListener(contextMenu);
		this.addMouseListener(statePen);
		this.addMouseListener(stateRect);
		
		nodeContextMenu.add(setStateStroke);
		nodeContextMenu.add(printNodeState);
		nodeContextMenu.add(monitorNode);
		
		this.setPreferredSize(new Dimension(this.canvasWidth, this.canvasWidth));
		
	}
	
	int n = 0;
	public void paintComponent(Graphics g){
		paintExtension(g);
		if(++n % 100 == 0)
			RepaintManager.currentManager(this).addDirtyRegion(this, this.getX(), this.getY(), this.getWidth(), this.getHeight());	
	}

	public abstract void paintExtension(Graphics g);
	

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
			boolean monitoring = ((MainApplicationFrame)parent).sim.toggle_listening(nodeClicked);
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