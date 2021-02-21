package radWorld;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TabCompleter implements org.bukkit.command.TabCompleter {
	
	private Main main;
	
	public TabCompleter(Main main) {
		this.main = main;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command com, String lable, String[] args) {
		List<String> l = new ArrayList<String>();
		if(args.length == 1) {
			l.add("player");
			l.add("global");
			l.add("disable");
			return fix(l, args[args.length - 1]);
		} else if(args.length == 2 && args[0].equals("player")) {
			l.add("add");
			l.add("remove");
			l.add("level");
			l.add("set");
			l.add("enabled");
			return fix(l, args[args.length - 1]);
		} else if(args.length == 2 && args[0].equals("global")) {
			l.add("damage");
			l.add("recovery");
			l.add("radMultip");
			l.add("armorProt");
			return fix(l, args[args.length - 1]);
		} else if(args.length == 3 && args[0].equals("player")) {
			if(args[1].equals("add") ) {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
		            l.add(p.getName());
		        }
			} else {
				for (String str : main.getPlayers()) {
					l.add(str);
				}
			}
			return fix(l, args[args.length - 1]);
		} else if(args.length == 3 && args[1].equals("damage") && args[0].equals("global")) {
			l.add("true");
			l.add("false");
			return fix(l, args[args.length - 1]);
		} else if(args.length == 2 && args[0].equals("disable")) {
			l.add("true");
			l.add("false");
			return fix(l, args[args.length - 1]);
		}
		return l;
	}
	
	private static List<String> fix(List<String> in, String str) {
		List<String> l = new ArrayList<String>();
		if(in.size() > 0) {
			for(int i = 0; i < in.size(); i++) {
				if(in.get(i) != null) {
					if(in.get(i).substring(0,Math.min(str.length(), in.get(i).length())).equals(str)) {
						l.add(in.get(i));
					}
				}
			}
		} else {
			return l;
		}
		if(l.size()==0) {
			return l;
		}
		return l;
	}
	
	
}
