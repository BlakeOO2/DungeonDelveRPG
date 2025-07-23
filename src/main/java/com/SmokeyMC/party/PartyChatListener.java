package com.SmokeyMC.party;

import com.SmokeyMC.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PartyChatListener implements Listener {
    private final Main plugin;

    public PartyChatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event){
        Player player = event.getPlayer();

        if (plugin.getPartyManager().isPartyChatToggled(player.getUniqueId())){
            Component message = event.message();
            String plainText = PlainTextComponentSerializer.plainText().serialize(message);
            plugin.debug("Party chat message received from " + player.getName() + ": " + plainText);
            event.setCancelled(true);
            plugin.getPartyManager().sendPartyMessage(player.getUniqueId(), plainText);
        }
    }


}
