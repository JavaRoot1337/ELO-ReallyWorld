package ru.javaroot.elo.plat.txt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TxtTest {

    @Test
    void convertsLegacyAmpHexAndPercentPlaceholders() {
        assertEquals("<#ffb703><bold>elo <gray>▸ <white>1000", Txt.toMini("&#ffb703&lelo &7▸ &f1000"));
        assertEquals("<#FFFFFF>test <gray>ok", Txt.toMini("&FFFFFFtest &7ok"));

        Component comp = Txt.parse("&7рейтинг: &f%elo% %rank% &7(%player%)",
                Txt.text("%elo%", 1250),
                Txt.color("%rank%", "&aновичок"),
                Txt.text("%player%", "<red>Steve"));
        String plain = PlainTextComponentSerializer.plainText().serialize(comp);
        assertEquals("рейтинг: 1250 новичок (<red>Steve)", plain);
    }
}
