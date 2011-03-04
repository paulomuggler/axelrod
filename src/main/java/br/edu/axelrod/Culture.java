package br.edu.axelrod;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.text.DecimalFormat;

public class Culture extends Applet implements Runnable {
	
	/***/
	private static final long serialVersionUID = -6579391838644972285L;
	
	/** max grid size */
	final int L_MAX = 128;
	
	/** max number of nodes */
	final int N_MAX = L_MAX * L_MAX;
	
	/** min grid size */
	final int L_MIN = 8;
	
	// final int MAXX=600;
	// final int MAXY=460;
	
	/** max features */
	final int F_MAX = 32;
	
	/** max traits */
	final int q_MAX = 250;
	
	/** maximum node degree */
	final int degree_max = 8;
	
	/** a thread */
	Thread t = null;

	/** network state representation */
	int s[][] = new int[N_MAX][F_MAX];
	
	/** adjacency matrix */
	int lt[][] = new int[N_MAX][degree_max];
	
	/** degree of each node */
	int degree[] = new int[N_MAX];
	
	/** indicates whether a node is active (a[i] == 1) or not (a[i] == 0) */
	int a[] = new int[N_MAX];
	
	/** which nodes are active */
	int ali[] = new int[N_MAX];
	
	/** active node count */
	int act;
	
	/** a single state representation? */
	int agrl[] = new int[F_MAX];
	
	/** grid size */
	int L = 64;
	
	/** number of nodes */
	int N = L * L;
	
	/** number of features */
	int F = 3;
	
	/** number of traits */
	int q = 15;
	
	/** probability that a random node will have its state randomized */
	double r = 0.0;
	
	/** probability than a random node will be randomly rewired */
	double p = 0.0;
	
	/** keeping track of time */
	double time, otime, nnext, slowmo = 1.0;
	
	/** width of a single node on screen */
	int k;
	
	/**  */
	int pause = 0;
	
	/** graphical renderer mode. 0 = Colors, 1 = Borders, 2 = Lattice */
	int gfx_mode = 1;
	
	/** partitioning of a single RGB component */
	int colour_code[] = new int[q_MAX];
	
	/**  */
	Object recent_target = null;
	
	/** lots of buttons */
	Button FP_B, FM_B, QP_B, QM_B, LP_B, LM_B, RP_B, RM_B, PP_B, PM_B,
			stop_go_B, INITH_B, INITR_B, INITB_B, PERT_B, CMODE_B, BMODE_B,
			NMODE_B, FAST_B, MEDIUM_B, SLOW_B;
	
	// TextField q_field,r_field;
	
	/** this is just to format the noise value output in the UI*/
	DecimalFormat noiseFormatter = new DecimalFormat("000000");

	/** java.applet.Applet#init() */
	public void init() {
		int i, j, l;
		this.setSize(640, 480);
		setLayout(null);
		setBackground(Color.white);
		LP_B = new Button("+");
		LP_B.setBounds(400, 10, 20, 20);
		add(LP_B);
		LM_B = new Button("-");
		LM_B.setBounds(425, 10, 20, 20);
		add(LM_B);
		FP_B = new Button("+");
		FP_B.setBounds(400, 40, 20, 20);
		add(FP_B);
		FM_B = new Button("-");
		FM_B.setBounds(425, 40, 20, 20);
		add(FM_B);
		QP_B = new Button("+");
		QP_B.setBounds(400, 70, 20, 20);
		add(QP_B);
		QM_B = new Button("-");
		QM_B.setBounds(425, 70, 20, 20);
		add(QM_B);
		RP_B = new Button("+");
		RP_B.setBounds(400, 100, 20, 20);
		add(RP_B);
		RM_B = new Button("-");
		RM_B.setBounds(425, 100, 20, 20);
		add(RM_B);
		PP_B = new Button("+");
		PP_B.setBounds(400, 130, 20, 20);
		add(PP_B);
		PM_B = new Button("-");
		PM_B.setBounds(425, 130, 20, 20);
		add(PM_B);
		stop_go_B = new Button("Stop/Go");
		stop_go_B.setBounds(400, 200, 90, 20);
		add(stop_go_B);
		PERT_B = new Button("Perturbation");
		PERT_B.setBounds(500, 200, 90, 20);
		add(PERT_B);
		INITH_B = new Button("homogeneous");
		INITH_B.setBounds(400, 260, 90, 20);
		add(INITH_B);
		INITR_B = new Button("random");
		INITR_B.setBounds(500, 260, 90, 20);
		add(INITR_B);
		INITB_B = new Button("bubble");
		INITB_B.setBounds(400, 290, 90, 20);
		add(INITB_B);
		CMODE_B = new Button("Colours");
		CMODE_B.setBounds(400, 338, 58, 20);
		add(CMODE_B);
		BMODE_B = new Button("Borders");
		BMODE_B.setBounds(466, 338, 58, 20);
		add(BMODE_B);
		NMODE_B = new Button("Lattice");
		NMODE_B.setBounds(532, 338, 58, 20);
		add(NMODE_B);
		FAST_B = new Button("Fast");
		FAST_B.setBounds(400, 368, 58, 20);
		add(FAST_B);
		MEDIUM_B = new Button("Medium");
		MEDIUM_B.setBounds(466, 368, 58, 20);
		add(MEDIUM_B);
		SLOW_B = new Button("Slow");
		SLOW_B.setBounds(532, 368, 58, 20);
		add(SLOW_B);

		colour_code[0] = 0;
		colour_code[1] = 255;
		colour_code[2] = 128;
		j = 64;
		l = 32;
		for (i = 3; i < q_MAX; i++) {
			colour_code[i] = l;
			l += j;
			if (l > 255) {
				j /= 2;
				l = j / 2;
			}
		}

		for (i = 0; i < N_MAX; i++)
			for (j = 0; j < F_MAX; j++)
				s[i][j] = 0;

		make_lattice();
		init_random();
	}

	public void start() {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	public void stop() {
		t = null;
	}

	public void paint(Graphics g) {
		draw_all();
		print();
		g.setColor(Color.black);
		g.fillRect(400, 235, 190, 2);
		g.drawString("Set initial configuration", 400, 250);
		g.fillRect(400, 313, 190, 2);
		g.drawString("Display", 400, 328);
	}

	void make_lattice() {
		int i, j, l, d, x, y, flag;

		k = 3 * L_MAX / L;
		N = L * L;

		// initialize adjacency matrix
		for (i = 0; i < N; i++) {
			
			degree[i] = 0;
			x = i % L; // iterate columns
			y = i / L; // iterate lines
			
			if (x > 0) // not on column 0
				lt[i][degree[i]++] = x - 1 + y * L;
			
			if (x < L - 1) // not on last column
				lt[i][degree[i]++] = x + 1 + y * L;
			
			if (y > 0) // not on line 0
				lt[i][degree[i]++] = x + (y - 1) * L;
			
			if (y < L - 1) // not on last line
				lt[i][degree[i]++] = x + (y + 1) * L;
			
		}

		// initial rewiring
		for (i = 0; i < N; i++)
			for (j = 0; j < degree[i]; j++)
				if (Math.random() < p) {
					d = lt[i][j]; // i's neighbor
					l = 0;
					while (lt[d][l] != i)
						l++;
					// d gets one less neighbor
					lt[d][l] = lt[d][--degree[d]];
					do {
						// now we choose at random a different node to connect to node i
						d = (int) (Math.random() * N);
						for (l = flag = 0; l < degree[d]; l++)
							if (lt[d][l] == i)
								flag = 1;
					} while ((d == i) || flag != 0); // has to be different from i and not already i's neighbor
					lt[i][j] = d; // assign a new neighbor to i
					lt[d][degree[d++]] = i; // assign a new neighbor (i) to d
				}

	}

	
	/**
	 * Random initial state distribution
	 */
	public void init_random() {
		int i, f;

		for (i = 0; i < N; i++) {
			for (f = 0; f < F; f++)
				s[i][f] = (int) ((double) q * Math.random());
		}

		update_alist(true);

		otime = time = nnext = 0.0;
	}

	/**
	 * Homogeneous initial state distribution
	 */
	public void init_homogeneous() {
		int i, f;

		for (i = 0; i < N; i++) {
			for (f = 0; f < F; f++)
				s[i][f] = 0;
		}
		update_alist(true);

		otime = time = nnext = 0.0;
	}

	/**
	 * Bubble state distribution
	 */
	public void init_bubble() {
		int i, f, x, y;

		for (i = 0; i < N; i++) {
			x = (i % L) - L / 2;
			y = i / L - L / 2;
			if (x * x + y * y < L * L / 16)
				for (f = 0; f < F; f++)
					s[i][f] = (f == 0) ? 0 : 1;
			else
				for (f = 0; f < F; f++)
					s[i][f] = 0;
		}
		update_alist(true);

		otime = time = nnext = 0.0;
	}

	/**
	 * Draw controls region
	 */
	public void print() {
		Graphics g = getGraphics();

		g.setColor(Color.white);
		g.fillRect(450, 10, 150, 170);
		g.setColor(Color.black);
		g.drawString("L = " + String.valueOf(L), 455, 25);
		g.drawString("F = " + String.valueOf(F), 455, 55);
		g.drawString("q = " + String.valueOf(q), 455, 85);
		g.drawString("r = 0." + noiseFormatter.format((int) (r * 1E6)), 455,
				115);
		g.drawString("p = 0." + noiseFormatter.format((int) (p * 1E6)), 455,
				145);
		if (pause == 0)
			g.drawString(
					"time = " + String.valueOf((long) time) + " (running)",
					455, 175);
		else
			g.drawString("time = " + String.valueOf((long) time)
					+ ((act == 0) ? " (frozen)" : " (paused)"), 455, 175);
	}

	/*
	 * legend(Graphics g) { g.setColor(Color.white) g.fillRect(0,400,590,50);
	 * 
	 * if (gfx_mode==0) { if (F==2) {
	 */

	/**
	 * Set border colours for lattice display
	 * 
	 * @param site
	 * @param neigh
	 * @param g
	 */
	public void set_border_colour(int site, int neigh, Graphics g) {
		int f, overlap;
		for (f = overlap = 0; f < F; f++)
			if (s[site][f] == s[neigh][f])
				overlap++;
		if (overlap == 0)
			g.setColor(Color.black);
		else {
			if (overlap != F)
				g.setColor(Color.red);
			else
				g.setColor(Color.white);
		}
	}

	/**
	 * Draws a single node on the network
	 * @param site
	 * @param g
	 */
	public void draw_site(int site, Graphics g) {
		int neigh, x, y;
		x = site % L;
		y = site / L;

		if (gfx_mode == 0) {
			if (F == 2)
				g.setColor(new Color(colour_code[s[site][0]],
						colour_code[s[site][1]], 128));
			else
				g.setColor(new Color(colour_code[s[site][0]],
						colour_code[s[site][1]], colour_code[s[site][2]]));
			g.fillRect(2 + x * k, 2 + y * k, k, k);
		} else {
			if (x > 0) {
				neigh = site - 1;
				set_border_colour(site, neigh, g);
				g.fillRect(2 + x * k - 1, 2 + y * k, 2, k);
			}
			if (y > 0) {
				neigh = site - L;
				set_border_colour(site, neigh, g);
				g.fillRect(2 + x * k, 2 + y * k - 1, k, 2);
			}
			if (x < L - 1) {
				neigh = site + 1;
				set_border_colour(site, neigh, g);
				g.fillRect(2 + (x + 1) * k - 1, 2 + y * k, 2, k);
			}
			if (y < L - 1) {
				neigh = site + L;
				set_border_colour(site, neigh, g);
				g.fillRect(2 + x * k, 2 + (y + 1) * k - 1, k, 2);
			}
		}
	}

	/**
	 * Draw all nodes
	 */
	public void draw_all() {
		Graphics g = getGraphics();
		int i, j, x1, y1, x2, y2, d;

		g.setColor(Color.white);
		g.fillRect(0, 0, k * L + 4, k * L + 4);
		g.setColor(Color.black);
		g.fillRect(0, 0, 2, k * L + 4);
		g.fillRect(0, 0, k * L + 4, 2);
		g.fillRect(2 + k * L, 0, 2, k * L + 4);
		g.fillRect(0, 2 + k * L, k * L + 4, 2);

		if (gfx_mode != 2)
			for (i = 0; i < N; i++)
				draw_site(i, g);
		else {
			g.setColor(Color.blue);
			for (i = 0; i < N; i++)
				for (j = 0; j < degree[i]; j++) {
					d = lt[i][j];
					x1 = i % L;
					y1 = i / L;
					x2 = d % L;
					y2 = d / L;
					g.drawLine(2 + k / 2 + k * x1, 2 + k / 2 + k * y1, 2 + k
							/ 2 + k * x2, 2 + k / 2 + k * y2);
				}
		}

		print();
	}

	public boolean action(Event evt, Object arg) {
		recent_target = evt.target;
		start();
		return true;
	}

	/**
	 * UI input handling
	 */
	public void handle_event() {
		int x, y, f;

		if ((recent_target == FP_B) && (F < F_MAX)) {
			F++;
			update_alist(true);
			draw_all();
		}

		if ((recent_target == FM_B) && (F > 2)) {
			F--;
			update_alist(true);
			draw_all();
		}

		if (recent_target == QP_B) {
			q++;
			print();
		}

		if ((recent_target == QM_B) && (q > 2)) {
			q--;
			print();
		}

		if ((recent_target == RP_B) && (r < 0.5)) {
			if (r == 0.0) {
				r = 0.000001;
				pause = 0;
			} else
				r *= 1.77827942;
			nnext = time;
			print();
		}

		if (recent_target == RM_B) {
			if ((r /= 1.77827942) < 0.000001)
				r = 0.0;
			print();
		}

		if ((recent_target == PP_B) && (p < 0.5)) {
			if (p == 0.0)
				p = 0.000001;
			else
				p *= 1.77827942;
			make_lattice();
			update_alist(true);
			draw_all();
		}

		if (recent_target == PM_B) {
			if ((p /= 1.77827942) < 0.000001)
				p = 0.0;
			make_lattice();
			update_alist(true);
			draw_all();
		}

		if ((recent_target == LP_B) && (L < L_MAX)) {
			L *= 2;
			make_lattice();

			for (y = L - 1; y >= 0; y--)
				for (x = L - 1; x >= 0; x--)
					for (f = 0; f < F; f++)
						s[y * L + x][f] = s[(y / 2) * (L / 2) + (x / 2)][f];

			update_alist(true);
			draw_all();
		}

		if ((recent_target == LM_B) && (L > L_MIN)) {
			L /= 2;
			make_lattice();

			for (y = 0; y < L; y++)
				for (x = 0; x < L; x++)
					for (f = 0; f < F; f++)
						s[y * L + x][f] = s[(y * L * 4) + x * 2][f];

			update_alist(true);
			draw_all();
		}

		if (recent_target == INITR_B) {
			init_random();
			draw_all();
		}

		if (recent_target == INITH_B) {
			init_homogeneous();
			draw_all();
		}

		if (recent_target == INITB_B) {
			init_bubble();
			draw_all();
		}

		if (recent_target == stop_go_B) {
			pause = 1 - pause;
			print();
		}

		if (recent_target == PERT_B) {
			perturbation();
			update_alist(false);
			draw_all();
		}

		if (recent_target == CMODE_B) {
			gfx_mode = 0;
			draw_all();
		}

		if (recent_target == BMODE_B) {
			gfx_mode = 1;
			draw_all();
		}

		if (recent_target == NMODE_B) {
			gfx_mode = 2;
			draw_all();
		}

		if (recent_target == FAST_B)
			slowmo = 1.0;
		if (recent_target == MEDIUM_B) {
			slowmo = 0.01;
			otime = time;
		}
		if (recent_target == SLOW_B) {
			slowmo = 0.001;
			otime = time;
		}

		recent_target = null;
	}

	/** updates the active nodes representations (count, list, and status)
	 * 
	 * @param complete
	 */
	public void update_alist(boolean complete) {
		int i, j, f, overlap, neigh;

		print();
		act = 0;
		for (i = 0; i < N; i++) {
			if (complete || (a[i] != 0)) {
				a[i] = 0;
				for (j = 0; j < degree[i]; j++) {
					overlap = 0;
					neigh = lt[i][j];
					for (f = 0; f < F; f++)
						if (s[i][f] == s[neigh][f])
							overlap++;
					if ((overlap != 0) && (overlap < F)) {
						a[i] = 1;
						break;
					}
				}
			}
			if (a[i] != 0)
				ali[act++] = i;
		}

	}

	/**
	 * Introduces a random state perturbation on a randomly selected node.
	 * Must call update_alist() after calling this method.
	 * 
	 * @return the altered node
	 */
	public int perturbation() {
		int site, j, neigh;

		site = (int) (Math.random() * N);
		j = (int) (Math.random() * F);

		s[site][j] = (int) (Math.random() * q);

		for (j = 0; j < degree[site]; j++)
			if (a[neigh = lt[site][j]] == 0) {
				a[neigh] = 1;
				ali[act++] = neigh;
			}
		
		if (a[site] == 0) {
			a[site] = 1;
			ali[act++] = site;
		}

		return site;
	}

	public void run() {
		int i, j, f;
		int site, neigh;
		int waitsteps, waittol;

		Graphics g = getGraphics();
		print();
		waitsteps = 0;
		waittol = 10 * F;
		while (t != null) {
			if (recent_target != null) // user has clicked somewhere
				handle_event();

			if (pause == 0) {
				while ((nnext <= time) && (r != 0.0)) {
					if (gfx_mode != 2)
						draw_site(perturbation(), g);
					else
						perturbation();
					nnext -= Math.log(Math.random()) / r / N;
				}

				if (act != 0) {
					time += 1.0 / (double) act;
					i = (int) (Math.random() * act);
					site = ali[i];
					j = (int) (Math.random() * degree[site]);
					f = (int) (Math.random() * F);

					if (s[site][f] == s[neigh = lt[site][j]][f]) {
						for (f = j = 0; f < F; f++)
							if (s[site][f] != s[neigh][f])
								agrl[j++] = f;

						if (j != 0) {
							j = (int) (Math.random() * j);
							f = agrl[j];
							s[site][f] = s[neigh][f];
							if (gfx_mode != 2)
								draw_site(site, g);

							for (j = 0; j < degree[site]; j++)
								if (a[neigh = lt[site][j]] == 0) {
									a[neigh] = 1;
									ali[act++] = neigh;
								}
							waitsteps = 0;
						}
					}
					if ((waitsteps++) > waittol) {
						update_alist(false);
						waitsteps = 0;
					}
				}
			} else
				t = null;

			if (act == 0) {
				if (r > 0.0) {
					time = (nnext - time > 1.0) ? time + 1.0 : nnext;
				} else
					pause = 1;
				print();
			}

			if (otime < time - slowmo) {
				if ((int) otime != (int) time)
					print();
				if (slowmo < 1.0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				otime += slowmo;
			}
		}
	}
}