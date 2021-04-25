package radWorld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	
	private Main main;
	
	public Commands(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command com, String lable, String[] args) {
//		Player p = sender.
		try {
			if(args.length > 0) {
				if(args[0].equals("disable") ) {
					if(args.length == 2) {
						boolean v = Boolean.parseBoolean(args[1]);
						main.setDissabled(v);
						if(v) {
							sender.sendMessage("Rad World dissabled");
						} else {
							sender.sendMessage("Rad World enabled");
						}
						return true;
					} else {
						sender.sendMessage("Incorrect number of arguments");
						return false;
					}
				} else if(args[0].equals("version") ) {
					if(args.length == 1) {
						sender.sendMessage("Rad World version: " + main.getVerison() );
						return true;
					} else {
						sender.sendMessage("Incorrect number of arguments");
						return false;
					}
				} else if(args[0].equals("reload") ) {
					if(args.length == 1) {
						sender.sendMessage("Reloading Configuration file");
						main.reloadConfigFile();
						return true;
					} else {
						sender.sendMessage("Incorrect number of arguments");
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
									sender.sendMessage("Player " + args[2] + " already added");
									return false;
								}
							} else {
								sender.sendMessage("Player " + args[2] + " dosen't exist");
								return false;
							}
						} else if(args[1].equals("remove") ) {
							Player p2 = getPlayer(args[2]);
							if(p2 != null) {
								if(main.removePlayer(p2)) {
									sender.sendMessage("Player "  + args[2] + " removed");
									return true;
								} else {
									sender.sendMessage("Player " + args[2] + " already removed");
									return false;
								}
							} else {
								sender.sendMessage("Player " + args[2] + " dosen't exist");
								return false;
							}
						} else if(args[1].equals("level") ) {
							Player p2 = getPlayer(args[2]);
							if(p2 != null) {
								sender.sendMessage("Player "  + args[2] + " is exposed to " + main.getRadInc(p2) + " r/s, " + main.getRadCum(p2) + " rads");
								return true;
							} else {
								sender.sendMessage("Player " + args[2] + " dosen't exist");
								return false;
							}
						} else if(args[1].equals("set") ) {
							Player p2 = getPlayer(args[2]);
							if(args.length == 4) {
								float v = 0;
								try {
									v = Float.parseFloat(args[3]);
								} catch(NumberFormatException e) {
									sender.sendMessage("Value must be a number");
									return false;
								}
								if(p2 != null) {
									main.setRads(p2, v);
									sender.sendMessage("Player "  + args[2] + " exposure set to " + v + " rads");
									return true;
								} else {
									sender.sendMessage("Player " + args[2] + " dosen't exist");
									return false;
								}
							} else {
								sender.sendMessage("No value included");
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
								sender.sendMessage("No value included");
								return false;
							}
						} else {
							sender.sendMessage("Invalid second argument | add, remove, level, set, enalbed");
							return false;
						}
					} else {
						sender.sendMessage("Incorrect number of arguments");
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
								sender.sendMessage("Value must be a number");
								return false;
							}
							String s = main.setParam(args[1], v);
							if(!s.equals("")) {
								sender.sendMessage(s);
								return true;
							} else {
								sender.sendMessage("Invalid second argument | damage, recovery, radMultip, armorProt");
								return false;
							}
						}
					} else {
						sender.sendMessage("Incorrect number of arguments");
						return false;
					}
				} else {
					sender.sendMessage("Invlaid first argument | player, global, version, reload");
					return false;
				}
			} else {
				sender.sendMessage("Add parameters");
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
		sender.sendMessage("Command exucution faild, see log");
		return false;
	}

	public static Player getPlayer(String name) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getName().equals(name) ) return p;
        }
        return null;
    }

}
