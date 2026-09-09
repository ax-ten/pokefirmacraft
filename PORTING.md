# Stato del porting

Base di partenza: TFCobblemon 1.1 (commit `271760a`), Forge 1.20.1 + TFC 3 +
Cobblemon 1.5. Destinazione: NeoForge 1.21.1 + TFC 4.2.10 + Cobblemon 1.8.0.

## Fatto

**Build e metadati**
- ForgeGradle -> ModDevGradle 2, toolchain Java 21, wrapper Gradle 8.14.3
- `mods.toml` -> `neoforge.mods.toml`, mixin config dichiarato li'
- il criterio JVM del daemon (`gradle/gradle-daemon-jvm.properties`) fa scaricare
  a Gradle un Temurin 21 da solo, cosi' il build non dipende dal JDK di sistema

**Codice**
- registri e eventi passati alle API NeoForge (`DeferredItem`, `NeoForge.EVENT_BUS`,
  `Item.TooltipContext` in `appendHoverText`)
- mixin: `tfc:farmland` non esiste piu', si usano `TFCTags.Blocks.FARMLANDS` e `MUD`
- script KubeJS aggiornati a KubeJS 7 (`event.server`, `damageValue`)

**Data pack**
- cartelle dei registri al singolare (1.21) e rinomini di TFC 4
  (`food_items` -> `food`, `rock_knapping` -> `knapping`, `sluicing` -> `deposit`)
- formati JSON: risultati con `id`, `tfc:and` + `tfc:not_rotten`, `fluid_ingredient`
  con `fluid`, ItemPredicate delle loot table con `items`/`predicates`
- tag rinominati: `forge:shears` -> `c:tools/shear`, `tfc:sharp_tools` ->
  `tfc:tools/sharp`, `tfc:foods/*` -> `c:foods/*`, `tfc:<tipo>_rock` ->
  `tfc:stones/loose/<tipo>`
- deposit di rooted dirt e muddy roots riscritti sugli otto ordini di suolo di TFC 4

**Cobblemon 1.8**
- `context` -> `spawnablePositionType`, preset `underwater`/`lava_surface` ->
  `water`/`lava`
- i dieci species override riscritti sui file della 1.8 tenendo solo drop ed
  evoluzioni volute
- asset: rimosse le copie di modelli, pose, versi e texture di dieci pokemon che
  la 1.5 non aveva ancora; restano forme d'acciaio e golett colorati, spostati
  sotto `bedrock/pokemon/resolvers`
- corretti dieci `species_additions` che per copia-incolla puntavano al pokemon
  sbagliato, piu' il refuso "gyrados"

**Biomi e id**
- i tag di bioma (`tfc:all`, `tfc:common`, ...) ora poggiano sui tag che TFC 4
  popola da solo: prima elencavano trenta biomi su 125 e gran parte del mondo
  restava senza spawn
- corretti gli id finiti fuori posto: `minecraft:scute` -> `turtle_scute`,
  `minecraft:rotten_compost` -> `tfc:rotten_compost`, `tfc:gran_feline` ->
  `tfc:food/gran_feline`, i native copper sotto `tfc:ore/`

## Verificato

`./gradlew runServer` arriva a `Done`: 8074 ricette caricate, nessun errore di
parsing e nessun tag che non si risolve. I due mixin risultano innestati nelle
classi vere di Cobblemon (`RevivalHerbBlock` e `VivichokeBlock`), e i preset di
spawn vengono letti.

Il primo avvio aveva tirato fuori tre cose, ora sistemate: le quattro ricette
"vuote" con dentro `{}`, le operazioni della soffiatura senza namespace e i
tag `#tfc:rock/*`, `#tfc:farmland`, `#tfc:fruit_tree_branch`.

## Da verificare in gioco

- **Script KubeJS**: `entity.fullNBT.Pokemon.Species` e `EntityEvents.death` su
  `cobblemon:pokemon` non sono stati provati contro KubeJS 7 e Cobblemon 1.8.
- **Deposit**: in TFC 4 sluice e battea condividono lo stesso sistema, quindi
  rooted dirt e muddy roots ora si possono anche setacciare a mano, non solo
  nella sluice come prima.
- **Braised Vivichoke**: l'item non esiste piu' in Cobblemon, la ricetta di
  cottura e' stata tolta. Se lo si vuole indietro va registrato come item nostro
  (serve anche la texture).
- **Ninfea**: `tfc:plant/water_lily` ora e' divisa per colore; ho scelto la
  bianca per lotad/lombre/ludicolo, ma e' una scelta arbitraria.
- **Biomi**: i gruppi di altitudine (`low/mid/high_altitude`) sono stati
  ricondotti a `#c:is_plains`, `#c:is_hill`, `#c:is_plateau` e `#c:is_mountain`,
  ma quale dei 125 biomi di TFC 4 debba stare in quale fascia e' una scelta di
  design da rivedere. Con Gregnautics Continued va poi controllato che i biomi
  del pack finiscano davvero in quei tag (vedi sezione 2 del design doc).

## Scelte da riportare al pack

**Visualizzatore ricette: EMI, non JEI.** La mod non spedisce integrazioni con
nessuno dei due, quindi in dev e' solo una riga in `build.gradle`. Gregnautics
Continued monta pero' JEI piu' `kubejei`, e ci appoggia
`kubejs/client_scripts/gregnautics_jei_material_hide.js`, che nasconde i
duplicati dei 35 materiali unificati fra TFC e GregTech. Quello script parla
con `KubeJEIEvents` e sotto EMI non fa piu' niente: EMI nasconde gli stack
dalla propria API, e un equivalente di `kubejei` non risulta esistere.

Chi passa a EMI si porta dietro quel lavoro. A favore: GTCEu ha integrazione
EMI nativa (`integration/recipeviewer/emi`) e EMI include JEMI, che legge i
plugin scritti per JEI. Da verificare invece se FTB Quests sappia aprire le
ricette tramite EMI, visto quanto conta nel pack.

## Non ancora iniziato

Dal design doc, tutto quello che va oltre il porting 1:1:

- riconciliazione dei tier metallo per le forme di Steelix e Scizor con i
  materiali unificati di Gregnautics Continued
- vincolo di cattura legato a Pasture Block / PC
- feature nuove della 1.8: Alpha Pokemon, Habitat Block, TM Machine
- held item introdotti tra la 1.6 e la 1.8
- estensione della progressione da Steam Age in avanti (parte GregTech)
- capitolo FTB Quests
- addon leggendari (Myths and Legends)
