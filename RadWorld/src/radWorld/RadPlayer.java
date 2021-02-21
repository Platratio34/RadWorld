package radWorld;

public class RadPlayer {
	
	public float inc;
	public float lvl;
	public boolean enb;
	public boolean prot;
	
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
}
