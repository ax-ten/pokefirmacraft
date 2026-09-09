# TFCobblemon

Mod di integrazione di **Cobblemon** nella progressione di **TerraFirmaCraft** e
GregTech, pensata per girare dentro Gregnautics Continued (MC 1.21.1, NeoForge).

Il progetto nasce come porting di
[TFCobblemon](https://github.com/kingtrapinch/tfcobblemon) di KingTrapinch, che
copriva TFC 3 + Cobblemon 1.5 su Forge 1.20.1. La storia git parte dal suo
repository, e la licenza resta la GPL-3.0 dell'originale.

Il documento di design del progetto e' in
[`pokefirmacraft-design-doc.md`](pokefirmacraft-design-doc.md).

## Cosa c'e' dentro (ereditato da TFCobblemon)

- 250 spawn di Pokemon con loot table dedicate
- Ricette per Poke Ball, PC, Healing Machine e oggetti di evoluzione
- Medicine naturali trovabili in natura e ottenibili in cucina
- Golett craftabile tramite il Blank Orb
- Forme custom di Steelix e Scizor legate agli acciai di TFC
- Preset di spawn per bioma, regione (caldo/freddo, secco/umido) e stagione
- Pokemon di acqua salata e dolce distinti (configurabili via KubeJS)

## Compilare

Serve un **JDK 21** (NeoForge 1.21.1 non gira su versioni piu' recenti):

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
