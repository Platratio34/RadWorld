package radWorld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin {
	
	public boolean dev = false;

	private BukkitScheduler scheduler;
	private FileConfiguration config;
	private PluginDescriptionFile pdf;
	
	public static Logger log;
	public PrintStream log2;
	
	private float radMultip = 1;
	private float recoveryRate = 60;
	public int maxPen = 30;
	private float protLvl = 0.6f;
	
	private HashMap<String, RadPlayer> radPlayers;
	
	private Commands com;

	private String[] radLvs;
	private String[] radLvs2;
	private String[] radLvsb;
	
	private boolean dmgEnb = false;
	private boolean dissabled = true;
	private boolean enableSave = false;
	private float tps = 20;
	
	public File dataFolder;
	
	private String worldName;
	File worldConfigFile;
	
	@Override
	public void onEnable() {
		dataFolder = getDataFolder();
		log = super.getLogger();
		try {
			log2 = new PrintStream(new File(dataFolder + "log.log"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		radPlayers = new HashMap<String, RadPlayer>();
		
		worldName = getServer().getWorlds().get(0).getName();
		dataFolder = getDataFolder();
//		log.info(worldName);
//		log.info(getServer().getWorlds().size() + "");
//		log.info(getServer().getWorlds().get(0).getName());
//		log.info(getServer().getWorlds().get(1).getName());
//		log.info(getServer().getWorlds().get(2).getName());
		
		reloadConfigFile();
		
		radMultip = (float) config.getDouble("radMultip");
		recoveryRate = (float) config.getDouble("recoveryRate");
		maxPen = config.getInt("maxPen");
		protLvl = (float) config.getDouble("protLvl");
		dmgEnb = config.getBoolean("dmgEnb");
		dissabled = config.getBoolean("dissabled");
		ConfigurationSection cS = config.getConfigurationSection("radPlayers");
		if(cS != null) {
			for(String k : cS.getKeys(false)) {
				ConfigurationSection cSp = cS.getConfigurationSection(k);
				radPlayers.put(k, new RadPlayer(0, (float)cSp.getDouble("lvl"), cSp.getBoolean("enb"), false));
			}
		}
		scheduler = this.getServer().getScheduler();
		pdf = this.getDescription();
		if(pdf.getVersion().contains("-Dev")) {
			log.warning("You are running a dev version of this plugin");
			dev = true;
		}
		PluginManager pm = Bukkit.getServer().getPluginManager();
		com = new Commands(this);
		pm.registerEvents(new RadEvents(this), this);
		getCommand("rads").setExecutor(com);
		getCommand("rads").setTabCompleter(new TabCompleter(this));
		
		initArrs();
		
		if(!dissabled) {
			updateRad(0);
		}
		
		log.info(ChatColor.GREEN + "Started");
		//if(!Bukkit.getServer().getClass().getPackage().getName().contains(pdf.getAPIVersion())) { log.warn("Incorrect MC Version");}
	}
	
	@Override
	public void onDisable() {
		log2.close();
		if(config != null) {
			config.set("radMultip", radMultip);
			config.set("recoveryRate", recoveryRate);
			config.set("maxPen", maxPen);
			config.set("protLvl", protLvl);
			config.set("dmgEnb", dmgEnb);
			config.set("dissabled", dissabled);
			ConfigurationSection cS = config.createSection("radPlayers");
	
			for (Entry<String, RadPlayer> entry : radPlayers.entrySet() ) {
				ConfigurationSection cSp = cS.createSection(entry.getKey());
				cSp.set("lvl", entry.getValue().lvl);
				cSp.set("enb", entry.getValue().enb);
			}
			if(enableSave) {
				try {
					config.save(worldConfigFile);
				} catch (IOException e) {
					log.warning("Couldn't save configuration file " + worldName + ".yml");
				}
			} else {
				log.info("RadWorld Dissabled, did not save configuration file");
			}
		} else {
			log.warning("Configuration file null, failed to save");
		}
//		saveConfig();
	}
	
	public void log(String msg) {
		log2.println(msg);
		log2.flush();
	}
	
	private void updateRad(int s) {
		s %= 8;
		final int s2 = s + 1;
		try {
			for (Entry<String, RadPlayer> entry : radPlayers.entrySet() ) {
				String id = entry.getKey();
				RadPlayer rp = entry.getValue();
	
				if(rp.enb) {
					float inc = rp.inc;
					if(rp.prot) {
						inc -= maxPen / (1/protLvl);
					}
					inc = Math.max(inc, 0f);
					if(inc > 0) {
						rp.lvl += (inc / 4) * radMultip;
					} else {
						rp.lvl -= recoveryRate / 4;
					}
				}
				rp.lvl = Math.max(0, rp.lvl);
				rp.lvl = Math.min(maxPen * maxPen * 2, rp.lvl);
				
				float dps = 0;
				
				if(rp.lvl > 8 * maxPen && rp.lvl <= 24 * maxPen) {
					dps = 0.5f;
				} else if(rp.lvl > 24 * maxPen && rp.lvl <= 48 * maxPen) {
					dps = 1;
				} else if(rp.lvl > 48 * maxPen && rp.lvl < maxPen * maxPen * 2) {
					dps = 2;
				} else if(rp.lvl >= maxPen * maxPen * 2){
					dps = 4;
				}
				Player p = getPlayer(id);
				if(p != null) {
					if(!(p.getWorld().getName().contains("_nether") || p.getWorld().getName().contains("_the_end") ) ) {
						rp.setDim(0);
						if(s == 0 || s == 4) {
							if(dps == 0.5) {
								if(s == 0) {
									damgePlayer(p,1f);
								}
							} else {
								damgePlayer(p,dps);
							}
						}
						if(!rp.prot) {
							p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText( radLvs[(int) rp.inc] + "  | " + dps + " |  " + radLvs2[(int) Math.min(Math.ceil(rp.lvl / (2 * maxPen) ), radLvs2.length-1 ) ] + " | " + (int)(rp.inc * radMultip) + " r/s | " + (int)rp.lvl + " rads" ) );
						} else {
							p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText( radLvsb[(int) rp.inc] + "  | " + dps + " |  " + radLvs2[(int) Math.min(Math.ceil(rp.lvl / (2 * maxPen) ), radLvs2.length-1 ) ] + " | " + (int)(Math.max(rp.inc - (maxPen / (1/protLvl)), 0f)  * radMultip) + " r/s | " + (int)rp.lvl + " rads" ) );
						}
					} else {
						if(p.getWorld().getName().contains("_nether")) {
							rp.setDim(-1);
						} else {
							rp.setDim(1);
						}
					}
					
					PlayerInventory pI = p.getInventory();
					boolean prot = true;
					if(pI.getBoots() != null) {
						if(pI.getBoots().getType() != Material.NETHERITE_BOOTS) {
							prot = false;
						}
					} else {
						prot = false;
					}
					if(pI.getLeggings() != null) {
						if(pI.getLeggings().getType() != Material.NETHERITE_LEGGINGS) {
							prot = false;
						}
					} else {
						prot = false;
					}
					if(pI.getChestplate() != null) {
						if(pI.getChestplate().getType() != Material.NETHERITE_CHESTPLATE) {
							prot = false;
						}
					} else {
						prot = false;
					}
					if(pI.getHelmet() != null) {
						if(pI.getHelmet().getType() != Material.NETHERITE_HELMET) {
							prot = false;
						}
					} else {
						prot = false;
					}
					rp.prot = prot;
					
				}
					
			}
		} catch (Exception er) {
			File f = new File(dataFolder, "MainErrorLog.log");
			PrintStream ps;
			try {
				ps = new PrintStream(f);
				er.printStackTrace(ps);
				ps.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		
		if(!dissabled) {
			scheduler.scheduleSyncDelayedTask(this, new Runnable() {
				 
				 @Override
				 public void run() {
					 updateRad(s2);
				 }
				 
			 }, (long) (tps/4));
		}
			
	}
	
	public void addRad(Player p, int l) {
		String id = p.getUniqueId() + "";
		if(radPlayers.containsKey(id)) {
			radPlayers.get(id).inc = l;
		}
	}
	public void removeRad(Player p) {
		String id = p.getUniqueId() + "";
		if(radPlayers.containsKey(id)) {
			radPlayers.get(id).inc = 0f;
		}
	}
	
	public boolean addPlayer(Player p) {
		String id = p.getUniqueId() + "";
		if(!radPlayers.containsKey(id)) {
			radPlayers.put(id, new RadPlayer());
			return true;
		} else {
			return false;
		}
	}
	public boolean removePlayer(Player p) {
		String id = p.getUniqueId() + "";
		if(radPlayers.containsKey(id)) {
			radPlayers.remove(id);
			return true;
		} else {
			return false;
		}
	}
	
	public static Player getPlayer(int entityID) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getEntityId() == entityID) return p;
        }
        return null;
    }
	public static Player getPlayer(String uUID) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if ((p.getUniqueId() + "").equals(uUID) ) return p;
        }
        return null;
    }
	
	public float getRadInc(Player p) {
		return radPlayers.get(p.getUniqueId() + "").inc;
	}
	public float getRadCum(Player p) {
		return radPlayers.get(p.getUniqueId() + "").lvl;
	}
	
	public List<String> getPlayers() {
		List<String> l =  new ArrayList<String>();
		for (Entry<String, RadPlayer> entry : radPlayers.entrySet() ) {
			Player p = getPlayer(entry.getKey());
			if(p != null) {
				l.add(p.getName());
			}
		}
		return l;
	}
	
	public void resetRads(Player p) {
		String id = p.getUniqueId() + "";
		if(radPlayers.containsKey(id)) {
			radPlayers.get(id).lvl = 0;
		}
	}
	public void resetRads(Player p, boolean enb) {
		String id = p.getUniqueId() + "";
		if(radPlayers.containsKey(id)) {
			radPlayers.get(id).lvl = 0;
			radPlayers.get(id).enb = enb;
		}
	}
	public void setRads(Player p, float v) {
		String id = p.getUniqueId() + "";
		if(radPlayers.containsKey(id)) {
			radPlayers.get(id).lvl = v;
		}
	}
	public void setEnb(Player p, boolean enb) {
		String id = p.getUniqueId() + "";
		if(radPlayers.containsKey(id)) {
			radPlayers.get(id).enb = enb;
		}
	}
	
	public void setDmgEnb(boolean enb) {
		dmgEnb = enb;
	}
	
	private void damgePlayer(Player p, double a) {
		if(dmgEnb) {
			p.damage(a);
		} else {
			for(int i = 0; i < a; i++) {
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
			}
		}
	}
	
	public String setParam(String p, float v) {
		if(p.equals("recovery")) {
			recoveryRate = v;
			return "Recovery rate set to " + v + " per second";
		} else if(p.equals("radMultip")) {
			radMultip = v;
			return "Rad multip set to " + v;
		} else if(p.equals("armorProt")) {
			protLvl = v;
			initArrs();
			return "Armor protection set to " + v;
		}
		return "";
	}
	
	public void initArrs() {
		radLvs = new String[maxPen+1];
		radLvs2 = new String[maxPen+1];
		radLvsb = new String[maxPen+1];
		for(int i = 0; i < radLvs.length; i++) {
			radLvs[i] = "";
			radLvs2[i] = "";
			radLvsb[i] = ChatColor.GREEN + "";
			for(int j = 0; j < maxPen/2; j++) {
				if(j*2 < (i-1)) {
					radLvs[i] += "X";
					radLvs2[i] += "X";
					radLvsb[i] += "X";
				} else if(j*2 == (i-1)) {
					if(i > 0 && (i-1)%2 == 0) {
						radLvs[i] += "+";
						radLvs2[i] += "+";
						radLvsb[i] += "+";
					} else {
						radLvs[i] += "-";
						radLvs2[i] += "-";
						radLvsb[i] += "-";
					}
				} else {
					radLvs[i] += "-";
					radLvs2[i] += "-";
					radLvsb[i] += "-";
				}
				if(j == 1) {
					radLvs2[i] += ChatColor.YELLOW;
				} else if(j == 5) {
					radLvs2[i] += ChatColor.RED;
					radLvs2[i] += ChatColor.RED;
				} else if(j == 11) {
					radLvs2[i] += ChatColor.DARK_RED;
				}
				if(j == (int) (maxPen / (2 * (1/protLvl)) ) ) {
					radLvsb[i] += ChatColor.RESET;
				}
			}
//			System.out.println(radLvs[i]);
			radLvs2[i] += ChatColor.RESET;
		}
	}
	
	public void setDissabled(boolean b) {
		dissabled = b;
		if(!b) {
			updateRad(0);
			enableSave = true;
		}
	}
	
	public String getVerison() {
		return pdf.getVersion();
	}
	
	public FileConfiguration getWorldConfig() {
		return config;
	}
	
	public void reloadConfigFile() {
		try {
			config = getConfig();
			
			worldConfigFile = new File(dataFolder, worldName + ".yml");
			if(worldConfigFile.exists()) {
				config = YamlConfiguration.loadConfiguration(worldConfigFile);
				enableSave = true;
			} else {
				log.info("Config for world " + worldName + " didn't exist, if enbaled, one will be created on plugin enable");
			}
			
			try {
				Reader defConfigStream = new InputStreamReader(this.getResource("config.yml"), "UTF8");
			    if (defConfigStream != null) {
			        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			        config.setDefaults(defConfig);
			    }
			} catch (UnsupportedEncodingException e) {
				log.warning("Failed to load default config");
			}
		} catch (Exception e) {
			File f = new File(dataFolder, "MainErrorLog.log");
			PrintStream ps;
			try {
				ps = new PrintStream(f);
				e.printStackTrace(ps);
				ps.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}
}
