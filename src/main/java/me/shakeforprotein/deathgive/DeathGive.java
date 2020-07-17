package me.shakeforprotein.deathgive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public final class DeathGive extends JavaPlugin implements Listener {

    private HashMap<Player, Long> cooldownHash = new HashMap<>();
    private List<Player> deadPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        if(getConfig().getString("version") != null && !getConfig().getString("version").equalsIgnoreCase(this.getDescription().getVersion())){
            File oldConfig = new File(getDataFolder(), "config-" + this.getDescription().getVersion() + "-" + LocalDateTime.now().toString().replace(":", "_").replace("T", "__") +".yml");
            try{getConfig().save(oldConfig);}
            catch(IOException e){
            }
            getConfig().options().copyDefaults(true);
            getConfig().set("version", this.getDescription().getVersion());
            saveConfig();
        }
        else{
            getConfig().set("version", this.getDescription().getVersion());
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void playerDieEvent(PlayerDeathEvent e){
        deadPlayers.add(e.getEntity());
    }

    @EventHandler
    public void playerRespawnEvent(PlayerRespawnEvent e) {
        if(deadPlayers != null && deadPlayers.size() > 0 && deadPlayers.contains(e.getPlayer())) {
            if (!cooldownHash.containsKey(e.getPlayer()) || (cooldownHash.containsKey(e.getPlayer()) && cooldownHash.get(e.getPlayer()) < (System.currentTimeMillis() - (getConfig().getInt("CooldownInSeconds") * 1000)))) {
                for (String group : getConfig().getConfigurationSection("Rewards").getKeys(false)) {
                    if (getConfig().getString("Rewards." + group + ".permission") != null) {
                        if (e.getPlayer().hasPermission(getConfig().getString("Rewards." + group + ".permission"))) {
                            if (getConfig().getStringList("Rewards." + group + ".items") != null) {
                                List<String> itemRewards = getConfig().getStringList("Rewards." + group + ".items");
                                for (String reward : itemRewards) {
                                    ItemStack rewardStack = new ItemStack(Material.valueOf(reward.split(",")[0].toUpperCase()), Integer.parseInt(reward.split(",")[1]));
                                    e.getPlayer().getInventory().addItem(rewardStack);
                                }
                                cooldownHash.putIfAbsent(e.getPlayer(), System.currentTimeMillis());
                            }
                        }
                    }
                }
            }
        }

        if(deadPlayers != null && deadPlayers.size() > 0 && deadPlayers.contains(e.getPlayer())){
            deadPlayers.remove(e.getPlayer());
        }
    }

}
