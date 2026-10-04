package cz.redry.nightfall.command;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class RedryCommands {
    private static final List<String> GUIDE = List.of(
            "§4§lREDRY: NOČNÍ ARCHIV §8/ §7VERZE 26.3",
            "§fPŘES DEN §8— §7svět je klidnější. Připrav jídlo, světlo a únikovou trasu; východ slunce anomálie rozpustí.",
            "§fPO SETMĚNÍ §8— §7nejdřív uslyšíš šum. Potom můžeš zahlédnout postavu, která nebyla na předchozím snímku.",
            "§fSTRACH §8— §7roste ve tmě, v blízkosti anomálií a při nízkém zdraví. Jasné světlo nebo přijímač RX-04 ho tlumí.",
            "§fHEROBRINE / POZOROVATEL §8— §7při přímém pohledu znehybní. Jakmile uhneš, zkrátí vzdálenost. Zaklepání za tebou není náhodné.",
            "§fRED-173 / ZASTAVENÝ §8— §7v podzemí se pohybuje, jen když na něj nedohlíží žádný živý hráč. V multiplayeru hlídá všechny svědky.",
            "§fRED-404 / CHAT BEZ DIVÁKŮ §8— §7po několika nocích se může připojit neznámý divák. Jeho zprávy nejsou od skutečného hráče.",
            "§fRX-04 §8— §7vyrob přijímač z kompasu, redstonu, ametystu a mědi. Pravým kliknutím změří směr; držený v ruce tlumí strach.",
            "§fKAZETA §8— §7papír, redstone, ametyst a měď. Každé přehrání odhalí jiný útržek. Někdy odpoví něco z druhé strany.",
            "§cDŮLEŽITÉ §8— §7světlo není ochrana před pohledem. Peaceful vypíná režiséra; při testování použij /navod entity.",
            "§8Příkazy: §7/navod lore · /navod signal · /navod entity"
    );

    private RedryCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("navod")
                        .executes(context -> sendGuide(context.getSource()))
                        .then(Commands.literal("lore").executes(context -> sendLore(context.getSource())))
                        .then(Commands.literal("signal").executes(context -> sendSignal(context.getSource())))
                        .then(Commands.literal("entity").executes(context -> sendEntityIds(context.getSource())))));
    }

    private static int sendGuide(CommandSourceStack source) {
        send(source, GUIDE);
        return Command.SINGLE_SUCCESS;
    }

    private static int sendLore(CommandSourceStack source) {
        send(source, List.of(
                "§4§lSPI S-00 / NÁVRAT SIGNÁLU",
                "§7Poslední stream skončil ve 23:17. Záznam obrazovky běžel dál, i když byl počítač odpojený od sítě.",
                "§7První noc: v odrazu monitoru se objevila postava se světlýma očima. Na původním obrazu místnost zela prázdnotou.",
                "§7Druhá noc: kroky za kamerou se zastavily vždy ve chvíli, kdy se Redry otočil. Záznam přesto ukazuje, že se postava přiblížila.",
                "§7Třetí noc: divák s prázdným profilem napsal ‚neotáčej se‘. Stream neměl připojení. Zpráva se objevila i na kazetě.",
                "§7Archiv dnes eviduje tři projevy: Pozorovatele, RED-173 a Chat bez diváků. Nevíme, zda jde o tři entity, nebo o jediný signál.",
                "§8Poslední přepis: §7„Tohle jsem neřekl. Proč to říká můj hlas?“",
                "§4Dodatek: §7Na zadní straně pásky je čerstvý otisk prstu. Kazeta nebyla od včerejška vyndána z inventáře."
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static int sendSignal(CommandSourceStack source) {
        send(source, List.of(
                "§4§lSYSTÉM STRACHU / RX-04",
                "§7Strach se zvyšuje postupně, ne skokem: tma, zranění a blízkost anomálie zhoršují rušení.",
                "§7Při silném rušení hrozí krátký výpadek obrazu, zrychlený tep a falešné zprávy v chatu.",
                "§7Světlé místo pomáhá. Přijímač RX-04 je účinnější: drž ho v ruce nebo pravým kliknutím zaměř signál.",
                "§8Vysoká úzkost neznamená, že se všechno, co slyšíš, skutečně stalo."
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static int sendEntityIds(CommandSourceStack source) {
        send(source, List.of(
                "§8TESTOVACÍ SUMMON ID / pouze v noci",
                "§7/summon redry:herobrine",
                "§7/summon redry:still_one",
                "§7/summon redry:chat_echo",
                "§8Entita se za dne odstraní. Creative a Spectator nejsou cílem nočního režiséra."
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static void send(CommandSourceStack source, List<String> lines) {
        for (String line : lines) {
            source.sendSuccess(() -> Component.literal(line), false);
        }
    }
}
