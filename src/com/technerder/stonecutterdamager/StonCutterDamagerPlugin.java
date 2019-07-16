package com.technerder.stonecutterdamager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StonCutterDamagerPlugin extends JavaPlugin implements Listener {

	private int damageDelay;
	private double damageAmount;
	private String stoneCutterDeathMessage;
	private boolean shouldDamagePlayers, shouldDamageEntities, shouldDestroyItems;

	public void onEnable() {
		loadConfigValues();
		runDamageTimer();
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	public void loadConfigValues() {
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		damageDelay = config.getInt("Damage-Delay");
		damageAmount = config.getDouble("Damage-Amount");
		shouldDestroyItems = config.getBoolean("Destroy-Items");
		shouldDamagePlayers = config.getBoolean("Damage-Players");
		shouldDamageEntities = config.getBoolean("Damage-Entities");
		stoneCutterDeathMessage = ChatColor.translateAlternateColorCodes('&', config.getString("Death-Message"));
	}

	public void runDamageTimer() {
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			if (shouldDamagePlayers) {
				Bukkit.getOnlinePlayers().forEach(player -> {
					if (isOnStoneCutter(player)) {
						player.damage(damageAmount);
					}
				});
			}
			if (shouldDamageEntities || shouldDestroyItems) {
				Bukkit.getWorlds().forEach(world -> {
					world.getEntities().stream().forEach(entity -> {

						if (!isOnStoneCutter(entity)) {
							return;
						}

						if (entity instanceof Item && shouldDestroyItems) {
							entity.remove();
							return;
						}

						if (entity instanceof Player) {
							return;
						}

						if (!(entity instanceof LivingEntity)) {
							return;
						}

						if (!shouldDamageEntities) {
							return;
						}

						((LivingEntity) entity).damage(damageAmount);

					});
				});
			}
		}, 0, damageDelay);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (isOnStoneCutter(player)) {
			event.setDeathMessage(stoneCutterDeathMessage.replace("%player_name%", player.getName()));
		}
	}

	public boolean isOnStoneCutter(Entity entity) {
		return entity.getLocation().getBlock().getType() == Material.STONECUTTER;
	}

}