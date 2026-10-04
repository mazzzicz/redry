package cz.redry.nightfall.item;

import cz.redry.nightfall.RedryMod;
import net.fabricmc.fabric.api.itemgroup.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class RedryItems {
    private static final ResourceKey<Item> FIELD_TAPE_KEY = key("field_tape");
    private static final ResourceKey<Item> SIGNAL_RECEIVER_KEY = key("signal_receiver");

    public static final Item FIELD_TAPE = Registry.register(
            BuiltInRegistries.ITEM,
            FIELD_TAPE_KEY,
            new FieldTapeItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .setId(FIELD_TAPE_KEY))
    );

    public static final Item SIGNAL_RECEIVER = Registry.register(
            BuiltInRegistries.ITEM,
            SIGNAL_RECEIVER_KEY,
            new SignalReceiverItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)
                    .setId(SIGNAL_RECEIVER_KEY))
    );

    private RedryItems() {
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM, RedryMod.id(path));
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> {
                    entries.accept(FIELD_TAPE);
                    entries.accept(SIGNAL_RECEIVER);
                });
    }
}
