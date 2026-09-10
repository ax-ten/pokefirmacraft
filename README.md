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

### I tre modi di provare

```
./gradlew runClient              # snello: TFC, Cobblemon, Greg, EMI. Venti secondi.
./gradlew runClient -Pcreate     # snello piu' Create, per le catene di assemblaggio
./gradlew installToPack          # jar nelle mod di Gregnautics, poi si gioca da Prism
```

Il dev snello e' quello per iterare sulle ricette. Con `-Pcreate` entra Create
preso dal pacchetto, coi suoi tre jar annidati estratti a mano in `local-mods/`
perche' in dev il jar-in-jar non viene aperto.

C'e' anche `-Ppack`, che mette tutte le mod di Gregnautics sul classpath, ma non
arriva ad avviarsi: diverse mod si comportano diversamente quando stanno sul
classpath invece che in una cartella `mods` — leggono il proprio jar, o applicano
i mixin prima di quando FML se li aspetta — e si finisce a escluderle una per
una. Per provare col pacchetto intero conviene `installToPack` e Prism.

Cobblemon, TerraFirmaCraft e Kotlin for Forge vengono scaricati automaticamente
dai maven dichiarati in `build.gradle`. Il jar di Cobblemon sta su
maven.impactdev.net, pesa 143 MB e su connessioni lente il download va in
timeout: in quel caso conviene scaricarlo a mano (`curl -L -C -`, che riprende
da dove si era fermato) e metterlo in `~/.m2`, poi lanciare Gradle con un init
script che aggiunge `mavenLocal()`.

## Crediti e licenze

Vedi [`CREDITS.md`](CREDITS.md): il codice viene da TFCobblemon (GPL-3.0), le texture
dei componenti delle ball da Create: Cobblemon Balls Overhaul (MIT).

## Stato del porting

Vedi [`PORTING.md`](PORTING.md) per cosa e' gia' stato convertito e cosa resta
da verificare in gioco.
