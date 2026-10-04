package cz.redry.nightfall.item;

import cz.redry.nightfall.audio.RedrySounds;
import cz.redry.nightfall.world.NightDirector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class FieldTapeItem extends Item {
    private static final String[] FRAGMENTS = {
            "§8[REDRY / ZÁZNAM 01] §7„Stream jsem vypnul. Pak jsem z reproduktoru slyšel vlastní hlas, jak říká: ještě nechoď.“",
            "§8[REDRY / ZÁZNAM 02] §7„V chatu nebyl nikdo. Přesto se tam objevilo: otoč se. A pak znovu.“",
            "§8[REDRY / ZÁZNAM 03] §7„Stál přesně tam, kde předtím. Když jsem mrkl, byl na druhé straně místnosti.“",
            "§8[REDRY / ZÁZNAM 04] §7„Tohle není starý záznam. Slyším, jak teď dýcháš.“"
    };

    public FieldTapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            String fragment = FRAGMENTS[level.getRandom().nextInt(FRAGMENTS.length)];
            serverPlayer.sendSystemMessage(Component.literal(fragment), false);
            level.playSound(null, player.blockPosition(), RedrySounds.TAPE_PLAY,
                    SoundSource.PLAYERS, 0.85F, 0.88F + level.getRandom().nextFloat() * 0.12F);
            NightDirector.relieveFear(serverPlayer, 12);

            if (level instanceof ServerLevel serverLevel && NightDirector.isNight(serverLevel)
                    && level.getRandom().nextFloat() < 0.28F) {
                level.playSound(null, player.blockPosition().relative(player.getDirection().getOpposite(), 5),
                        RedrySounds.BREATH, SoundSource.AMBIENT, 0.54F, 0.62F);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
