package org.golde.bukkit.vanish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {

	public List<UUID> vanishedPlayers = new ArrayList<UUID>();
	public static HashMap<UUID, Integer> foodlevel = new HashMap<UUID, Integer>(); //store food for unvanish

	@Override
	public void onEnable() {
		getCommand("vanish").setExecutor(this);
		Bukkit.getPluginManager().registerEvents(this, this);


		new BukkitRunnable() {
			@Override
			public void run() {
				for (final UUID vanished : vanishedPlayers) {
					final Player all = Bukkit.getPlayer(vanished);
					if (all != null) {
						all.sendMessage(ChatColor.GREEN + "You are currently invisable for other players!");
					}
				}
			}
		}.runTaskTimer(this, 0, 10);

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("vanish")){

			Player p = (Player) sender;
			if(p.hasPermission("vanish.vanish")) {
				toggleVanish(p);
			}
			return true;
		} 

		return true;
	}
	
	private void toggleVanish(Player target) {
		if(isVanished(target)) {
			unvanishPlayer(target);
		}
		else {
			vanishPlayer(target);
		}
	}
	
	private boolean isVanished(Player target) {
		return vanishedPlayers.contains(target.getUniqueId());
	}

	private void vanishPlayer(final Player target) {
		if (!this.vanishedPlayers.contains(target.getUniqueId())) {
			this.vanishedPlayers.add(target.getUniqueId());
		}
		if (foodlevel.containsKey(target.getUniqueId())) {
			foodlevel.remove(target.getUniqueId());
		}
		foodlevel.put(target.getUniqueId(), target.getFoodLevel());
		for (final Player all : Bukkit.getOnlinePlayers()) {
			all.hidePlayer(target);
		}
	}

	private void unvanishPlayer(final Player target) {
		if (this.vanishedPlayers.contains(target.getUniqueId())) {
			this.vanishedPlayers.remove(target.getUniqueId());
		}
		if (foodlevel.containsKey(target.getUniqueId())) {
			foodlevel.remove(target.getUniqueId());
		}
		for (final Player all : Bukkit.getOnlinePlayers()) {
			all.showPlayer(target);
		}
	}

	@EventHandler
	public void onFoodLevelChange(final FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player) {
			final Player p = (Player)e.getEntity();
			if (vanishedPlayers.contains(p.getUniqueId())) {
				if (e.getFoodLevel() > foodlevel.get(p.getUniqueId())) {
					foodlevel.put(p.getUniqueId(), e.getFoodLevel());
				}
				if (foodlevel.containsKey(p.getUniqueId())) {
					e.setFoodLevel(foodlevel.get(p.getUniqueId()));
				}
			}
			e.setCancelled(false);
		}
	}
	
	@EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            final Player p = (Player)e.getDamager();
            if (vanishedPlayers.contains(p.getUniqueId())) {
                e.setDamage(0.0);
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "You can't damage people while vanished!");
            }
        }
        if (e.getEntity() instanceof Player) {
            final Player p = (Player)e.getEntity();
            if (vanishedPlayers.contains(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player p = (Player)e.getEntity();
            
            //could be optimised but eclipse auto filled this if for me essentally
            if (vanishedPlayers.contains(p.getUniqueId()) && (e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.LIGHTNING || e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.CUSTOM || e.getCause() == EntityDamageEvent.DamageCause.DROWNING || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK || e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || e.getCause() == EntityDamageEvent.DamageCause.LAVA || e.getCause() == EntityDamageEvent.DamageCause.MAGIC || e.getCause() == EntityDamageEvent.DamageCause.MELTING || e.getCause() == EntityDamageEvent.DamageCause.POISON || e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE || e.getCause() == EntityDamageEvent.DamageCause.STARVATION || e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION || e.getCause() == EntityDamageEvent.DamageCause.SUICIDE || e.getCause() == EntityDamageEvent.DamageCause.THORNS)) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (vanishedPlayers.contains(p.getUniqueId())) {
            vanishPlayer(p);
            e.setJoinMessage(null);
            for (final Player all : Bukkit.getOnlinePlayers()) {
                if (all.hasPermission("vanish.alert")) {
                    all.sendMessage(color("&7" + p.getName() + " &eJoined in vanish!"));
                }
            }
        }
        else {
            for (final UUID vanished : vanishedPlayers) {
                final Player all2 = Bukkit.getPlayer(vanished);
                if (all2 != null) {
                    this.vanishPlayer(all2);
                }
            }
        }
    }
    
    @EventHandler
    public void PlayerQuit(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        if (vanishedPlayers.contains(p.getUniqueId())) {
            e.setQuitMessage(null);
            for (final Player all : Bukkit.getOnlinePlayers()) {
                if (all.hasPermission("vanish.alert")) {
                    all.sendMessage(color("&7" + p.getName() + " &eLeft in vanish!"));
                }
            }
        }
    }
    
    private String color(String in) {
    	return ChatColor.translateAlternateColorCodes('&', in);
    }

}