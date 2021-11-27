package radWorld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor {
	
	private Main main;
	
	public Commands(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command com, String lable, String[] args) {
		Player p = null;
		if(sender instanceof Player) {
			p = (Player)sender;
		}
		if(p != null && !p.hasPermission("rads.admin")) {
			sender.sendMessage(ChatColor.RED + "You are not alowed to use this command. If you belive this to be in error, contact your server administrator and ask to have permesion: 'rads.admin'");
			return false;
		}
		try {
			if(args.length > 0) {
				if(args[0].equals("dissable") ) {
					if(args.length == 1) {
						main.setDissabled(true);
						sender.sendMessage(ChatColor.YELLOW + "Rad World dissabled");
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments");
						return false;
					}
				} else if(args[0].equals("enable") ) {
					if(args.length == 1) {
						main.setDissabled(false);
						sender.sendMessage(ChatColor.GREEN + "Rad World enabled");
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments");
						return false;
					}
				} else if(args[0].equals("version") ) {
					if(args.length == 1) {
						sender.sendMessage("Rad World version: " + main.getVerison() );
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments");
						return false;
					}
				} else if(args[0].equals("reload") ) {
					if(args.length == 1) {
						sender.sendMessage(ChatColor.GREEN + "Reloading Configuration file");
						main.reloadConfigFile();
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments");
						return false;
					}
				} else if(args[0].equals("player") ) {
					if(args.length >= 3) {
						if(args[1].equals("add") ) {
							Player p2 = getPlayer(args[2]);
							if(p2 != null) {
								if(main.addPlayer(p2)) {
									sender.sendMessage("Player "  + args[2] + " added");
									return true;
								} else {
									sender.sendMessage(ChatColor.YELLOW + "Player " + args[2] + " already added");
									return false;
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Player " + args[2] + " dosen't exist");
								return false;
							}
						} else if(args[1].equals("remove") ) {
							Player p2 = getPlayer(args[2]);
							if(p2 != null) {
								if(main.removePlayer(p2)) {
									sender.sendMessage("Player "  + args[2] + " removed");
									return true;
								} else {
									sender.sendMessage(ChatColor.YELLOW + "Player " + args[2] + " already removed");
									return false;
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Player " + args[2] + " dosen't exist");
								return false;
							}
						} else if(args[1].equals("level") ) {
							Player p2 = getPlayer(args[2]);
							if(p2 != null) {
								sender.sendMessage("Player "  + args[2] + " is exposed to " + main.getRadInc(p2) + " r/s, " + main.getRadCum(p2) + " rads");
								return true;
							} else {
								sender.sendMessage(ChatColor.RED + "Player " + args[2] + " dosen't exist");
								return false;
							}
						} else if(args[1].equals("set") ) {
							Player p2 = getPlayer(args[2]);
							if(args.length == 4) {
								float v = 0;
								try {
									v = Float.parseFloat(args[3]);
								} catch(NumberFormatException e) {
									sender.sendMessage(ChatColor.RED + "Value must be a number");
									return false;
								}
								if(p2 != null) {
									main.setRads(p2, v);
									sender.sendMessage("Player "  + args[2] + " exposure set to " + v + " rads");
									return true;
								} else {
									sender.sendMessage(ChatColor.RED + "Player " + args[2] + " dosen't exist");
									return false;
								}
							} else {
								sender.sendMessage(ChatColor.RED + "No value included");
								return false;
							}
						} else if(args[1].equals("enabled") ) {
							Player p2 = getPlayer(args[2]);
							if(args.length == 4) {
								boolean v = Boolean.parseBoolean(args[3]);
								if(p2 != null) {
									main.setEnb(p2, v);
									sender.sendMessage("Player "  + args[2] + " enabled set to " + v);
									return true;
								} else {
									sender.sendMessage("Player " + args[2] + " dosen't exist");
									return false;
								}
							} else {
								if(p2 != null) {
									main.setEnb(p2, true);
									sender.sendMessage("Player "  + args[2] + " enabled");
									return true;
								} else {
									sender.sendMessage("Player " + args[2] + " dosen't exist");
									return false;
								}
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Invalid second argument | add, remove, level, set, enable, dissable");
							return false;
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments");
						return false;
					}
				} else if(args[0].equals("global")) {
					if(args.length == 3) {
						if(args[1].equals("damage")) {
							boolean v = Boolean.parseBoolean(args[2]);
							main.setDmgEnb(v);
							if(v) {
								sender.sendMessage("Damage enabled");
							} else {
								sender.sendMessage("Damage disabled");
							}
							return true;
						} else {
							float v = 0;
							try {
								v = Float.parseFloat(args[2]);
							} catch(NumberFormatException e) {
								sender.sendMessage(ChatColor.RED + "Value must be a number");
								return false;
							}
							String s = main.setParam(args[1], v);
							if(!s.equals("")) {
								sender.sendMessage(s);
								return true;
							} else {
								sender.sendMessage(ChatColor.RED + "Invalid second argument | damage, recovery, radMultip, armorProt");
								return false;
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments");
						return false;
					}
				} else if(args[0].equals("help")) {
					sender.sendMessage(ChatColor.GREEN + "Displaying help for Rad World");
					sender.sendMessage("To enable radiaiton, run:" + ChatColor.DARK_BLUE + " /rads enable" + ChatColor.RESET + ". To disable:" + ChatColor.DARK_BLUE + " /rads disable");
					sender.sendMessage("For players to recive radaiont they must be added. To add a player run:" + ChatColor.DARK_BLUE + " /rads player add [player name]" + ChatColor.RESET + ". To Remove:" + ChatColor.DARK_BLUE + " /rads player remove [player name]");
					sender.sendMessage("To disable radiaiton aclumation for a spesific player, run:" + ChatColor.DARK_BLUE + " /rads player enabled [player name] false" + ChatColor.RESET + ". To re-enable it:" + ChatColor.DARK_BLUE + " /rads player enabled [player name]");
					sender.sendMessage("To sent the total exposure for a player run:" + ChatColor.DARK_BLUE + " /rads player set [player name] [level]");
					sender.sendMessage("To veiw the current exposure and total exposure for a player run:" + ChatColor.DARK_BLUE + " /rads player level [player name]");
					sender.sendMessage("The global options are: ");
					sender.sendMessage(" - damage: damage enabled, defaults to false");
					sender.sendMessage(" - recovery: recovery rate, defaults 120 rads/s");
					sender.sendMessage(" - radMultip: radiation multiplyer, defaults to 1");
					sender.sendMessage(" - armorProt: amount of protection from armor, defaults to 0.8");
					sender.sendMessage("To set a global option run:" + ChatColor.DARK_BLUE + " /rads global [option] [value]");
					sender.sendMessage("To veiw the version of the plugin run:" + ChatColor.DARK_BLUE + " /rads version");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Invlaid first argument | help, player, global, version, reload");
					return false;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Add parameters | help, player, global, version, reload");
				return false;
			}
		} catch (Exception e) {
			File f = new File(main.dataFolder, "CommandErrorLog.log");
			PrintStream ps;
			try {
				ps = new PrintStream(f);
				e.printStackTrace(ps);
				ps.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		sender.sendMessage(ChatColor.RED + "Command exucution faild, see log");
		return false;
	}

	public static Player getPlayer(String name) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getName().equals(name) ) return p;
        }
        return null;
    }

}
