# TFCobblemon

Mod di integrazione di **Cobblemon** nella progressione di **TerraFirmaCraft**,
portata a MC 1.21.1 / NeoForge.

E' un fork della [TFCobblemon](https://github.com/kingtrapinch/tfcobblemon) di
KingTrapinch, che si fermava a TFC 3 + Cobblemon 1.5 su Forge 1.20.1. Nome,
modid e package restano i suoi; la storia git parte dal suo repository e la
licenza resta la GPL-3.0 dell'originale.

Da qui parte Pokefirmacraft, l'integrazione con Gregnautics Continued descritta
in [`pokefirmacraft-design-doc.md`](pokefirmacraft-design-doc.md).

## Cosa c'e' dentro (ereditato da TFCobblemon)

- 250 spawn di Pokemon con loot table dedicate
- Ricette per Poke Ball, PC, Healing Machine e oggetti di evoluzione
- Medicine naturali trovabili in natura e ottenibili in cucina
- Golett craftabile tramite il Blank Orb
- Forme custom di Steelix e Scizor legate agli acciai di TFC
- Preset di spawn per bioma, regione (caldo/freddo, secco/umido) e stagione
- Pokemon di acqua salata e dolce distinti (configurabili via KubeJS)

## Compilare

NeoForge 1.21.1 vuole Java 21. Non serve averlo installato: Gradle se lo
procura da solo grazie a `gradle/gradle-daemon-jvm.properties`.

```
./gradlew build
```

Il jar finisce in `build/libs/`. Per provare in gioco:

```
./gradlew runClient
```

Cobblemon, TerraFirmaCraft e Kotlin for Forge vengono scaricati automaticamente
dai maven dichiarati in `build.gradle`.

## Stato del porting

Vedi [`PORTING.md`](PORTING.md) per cosa e' gia' stato convertito e cosa resta
da verificare in gioco.
