package radWorld;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
		Player p = e.getPlayer();
		Location l = p.getLocation();
		int rads = main.maxPen;
		for(int i = l.getBlockY() + 2; i < 100; i++) {
			Location l2 = new Location(l.getWorld(), l.getX(), i, l.getZ());
			Block b = l2.getBlock();
			Material t = b.getType();
			if(!b.isPassable()) {
				if(t == Material.WATER) {
					rads--;
				} else if(t == Material.NETHERITE_BLOCK) {
					rads -= 4;
				} else if(t == Material.IRON_BLOCK) {
					rads -= 2;
				}
				rads--;
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
