package radWorld;

public class RadPlayer {
	
	public float inc;
	public float lvl;
	public boolean enb;
	public boolean prot;
	private int dim = 0;
	
	public RadPlayer() {
		inc = 0f;
		lvl = 0f;
		enb = true;
		prot = false;
	}
	public RadPlayer(float inc, float lvl, boolean enb, boolean prot) {
		this.inc = inc;
		this.lvl = lvl;
		this.enb = enb;
		this.prot = prot;
	}
	
	public void setDim(int d) {
		if(dim != d) {
			Main.log.info("Player moved to dim " + d);
		}
		dim = d;
	}
	public int getDim() {
		return dim;
	}
}
