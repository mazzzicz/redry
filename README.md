# REDRY: Noční archiv — Signál 26.3

Český survival-horor pro **Minecraft Java 26.3** a Fabric. Ve dne sbíráš důkazy a ladíš přijímač. V noci se hra učí, čeho se bojíš: tma, zranění a blízkost anomálie postupně zvyšují rušení, až začne praskat obraz i zvuk. **Nightfall resource pack se aktivuje automaticky.**

> Fanouškovský hororový projekt inspirovaný Redrym, Herobrinem a formátem SCP spisů. RED-173, RED-404 a archivní lore jsou původní fikce vytvořená pro tento mod; nejde o oficiální obsah Redryho ani SCP Foundation.

## Stažení a instalace

**[Stáhnout nejnovější sestavení REDRY pro Minecraft 26.3](https://github.com/mazzzicz/redry/releases/latest)**

Potřebuješ Minecraft Java **26.3**, **JDK/Java 25**, Fabric Loader **0.19.5 nebo novější** a Fabric API **0.161.0+26.3**.

1. Vytvoř si Fabric instalaci Minecraftu 26.3 a spusť ji alespoň jednou.
2. Stáhni `redry-nightfall-0.2.0.jar` z vydání výše.
3. Vlož JAR do složky `mods` své herní instalace. Fabric API musí být také ve složce `mods`.
4. Spusť svět a napiš `/navod`.

Na multiplayeru musí být mod i Fabric API na serveru. Každý hráč potřebuje mod také na klientovi kvůli vlastním modelům, texturám a animovanému HUD rušení.

## Co je nového v Signálu

### Systém strachu

- Strach se mění během hry: narůstá v temnotě, při nízkém zdraví a v dosahu anomálie; ustupuje ve světle nebo když držíš přijímač RX-04.
- S rostoucím strachem se zkracují klidná období. Přicházejí prostorové šepoty, klepání zpoza hráče, tlukot, krátké výpadky zraku a klamné zprávy.
- Vlastní HUD vrstva Nightfall napodobuje poškozený archivní záznam: tmavé okraje, úzké červené výpadky a citlivý měřič rušení. Zesiluje se u anomálií a při efektech tmy.
- Den zůstává přípravným oknem. Za denního světla se archivní bytosti rozpadají; obtížnost Peaceful noční režii vypne.

### Tři anomálie

- **Herobrine — Pozorovatel:** objeví se na okraji zorného pole a prvních pár okamžiků nehybně stojí. Když se na něj díváš, ztuhne; když sklopíš oči, přiblíží se. Zblízka se zvuk může ozvat z místa, které je za tebou.
- **RED-173 — Zastavený:** loví hlavně v podzemí. V multiplayeru kontroluje pohled všech blízkých přeživších — otočí-li se jediný z nich, socha se může pohnout.
- **RED-404 — Chat bez diváků:** pozdější noční projev, který se přibližuje bez běžného útoku. Může zanechat zprávu připomínající skutečný chat a zmizí, jakmile si ho všimneš nebo se dostane příliš blízko.

### Důkazy a vybavení

- **Archivní přijímač RX-04:** kompas, dva redstony, ametystový střep a měděný ingot. Pravým kliknutím ukáže jen přibližný směr a vzdálenost nejbližší anomálie; žádné přesné souřadnice. Držení přijímače ve volné ruce tlumí strach.
- **Ztracená kazeta Redryho:** pravým kliknutím přehraje náhodný útržek ze čtyř záznamů. Každé použití trochu uklidní hráče — ne vždy ale uklidní to, co poslouchá.
- Obě vlastní položky jsou vyrobitelné a mají vlastní pixel-art ikony. Modely a textury všech tří anomálií jsou součástí automaticky aktivovaného packu.

## Příkazy

- `/navod` — pravidla přežití a ovládání.
- `/navod lore` — archivní spis S-00 a přepisy kazet.
- `/navod signal` — vysvětlení strachu a RX-04.
- `/navod entity` — testovací summon ID.

Testovací entity: `/summon redry:herobrine`, `/summon redry:still_one`, `/summon redry:chat_echo`. Testuj po setmění; za dne se odstraní. Creative a Spectator nejsou cílem režiséra.

## Atmosféra a zvuk

Vlastní pixel-art textury, modely a procedurální animace běží v odděleném klientském rendereru. Zvukové události mají vlastní názvy, titulky, vrstvení a různé hlasitosti/tóniny. **Zatím ale skládají zvukové zdroje Minecraftu — mod neobsahuje vlastní hlasové nahrávky ani syntetizované české dialogy.**

## Vývoj a sestavení

Požadavky: **JDK 25**, **Gradle 9.7+**, internetové připojení k repozitářům Fabric/Minecraft. Minecraft 26.3 je neobfuskovaný; projekt proto používá nový plugin Fabric Loom `net.fabricmc.fabric-loom` bez Yarn/official mapping remapu.

```sh
gradle build
```

Výsledný mod bude v `build/libs/redry-nightfall-0.2.0.jar`. Zdrojové JSON a PNG lze offline zkontrolovat:

```sh
python3 tools/validate_project.py
```

Vlastní pixel-art assety jsou reprodukovatelně generované bez externích Python knihoven:

```sh
python3 tools/generate_art.py
```

Kontinuální build pro Minecraft 26.3 běží přes GitHub Actions na JDK 25 a vytváří také stažitelný artefakt pro každé sestavení.
