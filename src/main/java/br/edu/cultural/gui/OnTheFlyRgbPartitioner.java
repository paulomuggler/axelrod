package br.edu.cultural.gui;


public class OnTheFlyRgbPartitioner implements RgbPartitioner {
	
	@SuppressWarnings("unused")
	private final int features;
	@SuppressWarnings("unused")
	private final int traits;
	@SuppressWarnings("unused")
	private final int usedFeatures;
	
	public OnTheFlyRgbPartitioner(int features, int traits){
		this.features = features;
		this.traits = traits;
		this.usedFeatures = calcUsedFeatures(features, traits);
	}

	@Override
	public int color(int[] state) {
		int ri, gi, bi;
		ri =  gi = bi = 1;
		int r, g, b;
		r =  g = b = 0;
		for (int i = 0; i < state.length; i++){
			switch (i%3){
				case 0: { 
					r+=state[i]; ri++; break;
				}
				case 1: {
					g+=state[i]; gi++; break;
				}
				case 2: {
					b+=state[i]; bi++; break;
				}
			}
		}
			
		r*=255;
		r/=ri;
		
		g*=255;
		g/=gi;
		
		b*=255;
		b/=bi;
		
		return r * 0x10000 + g * 0x100 + b;
	}
	
	private int calcUsedFeatures(int features, int traits) {
		// TODO Auto-generated method stub
		return 0;
	}

}
