package radWorld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class RadEvents implements Listener {
	
public Main main;
	
	public RadEvents(Main m) {
		this.main = m;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		try {
			Player p = e.getPlayer();
			RadPlayer rp = main.getRadPlayer(p);
			if(rp != null) {
				if(rp.getDim() != 0) {
					main.removeRad(p);
					return;
				}
				Location l = p.getLocation();
				int rads = main.maxPen;
				FileConfiguration config = main.getWorldConfig();
				ConfigurationSection cS = null;
				if(config.contains("changedblocks")) {
					cS = config.getConfigurationSection("changedblocks");
				}
				for(int i = l.getBlockY() + 2; i < l.getBlockY() + 100; i++) {
					Location l2 = new Location(l.getWorld(), l.getX(), i, l.getZ());
					Block b = l2.getBlock();
					Material t = b.getType();
					if(cS != null) {
						if(cS.contains(t + "")) {
							rads -= cS.getInt(t + "");
						} else {
							main.debugLog(t + "");
							if(!b.isPassable()) {
								rads--;
							}
						}
					} else {
						if(!b.isPassable()) {
							rads--;
						}
					}
				}
		
				float sunlight_level = l.getBlock().getLightFromSky();
				sunlight_level /= 15;
				sunlight_level *= main.maxPen;
				
				rads = Math.max(rads, (int)sunlight_level);
				
				if(rads > 0) {
					main.addRad(p, rads);
				} else {
					main.removeRad(p);
				}
			}
		} catch (Exception er) {
			File f = new File(main.dataFolder, "RadEventsErrorLog.log");
			PrintStream ps;
			try {
				ps = new PrintStream(f);
				er.printStackTrace(ps);
				ps.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
    }
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		main.setEnb(p, false);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		main.resetRads(p, true);
	}
	
}
