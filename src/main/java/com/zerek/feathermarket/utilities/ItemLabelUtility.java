package com.zerek.feathermarket.utilities;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.zerek.feathermarket.FeatherMarket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ItemLabelUtility {

    private final FeatherMarket plugin;

    public ItemLabelUtility(FeatherMarket plugin) {
        this.plugin = plugin;
    }

    public Component formatExtraLabel(ItemStack itemStack){
        switch (itemStack.getType()){
            case PLAYER_HEAD:
                PlayerProfile playerProfile;
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                if (itemStack.getItemMeta().hasDisplayName()) return MiniMessage.miniMessage().deserialize("<red> ⓘ").hoverEvent(Component.text("Head has been renamed."));
                if (skullMeta.getOwningPlayer() != null) {
                    if (skullMeta.getOwningPlayer().getName() != null) return MiniMessage.miniMessage().deserialize("<green> ⓘ").hoverEvent(Component.text("Verified head: " + skullMeta.getOwningPlayer().getName()));
                    else return MiniMessage.miniMessage().deserialize("<red> ⓘ").hoverEvent(Component.text("Can't verify head"));
                }
            default:
                return Component.text("");
        }
    }


}
