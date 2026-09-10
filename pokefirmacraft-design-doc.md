# Pokefirmacraft — Documento di Design

Mod di integrazione di **Cobblemon** dentro **Gregnautics Continued** (porting 1:1 di TerraFirmaGreg-Modern su MC 1.21.1/NeoForge, con TFC 4 + GregTech CEu 8 + Create + Create: Aeronautics). Obiettivo: aggiungere Cobblemon alla progressione esistente senza affiancarla o stravolgerla.

## 0. Strategia: porting, non costruzione da zero

> **Stato (settembre 2026)**: il porting base è fatto e compila; il dettaglio di
> cosa è stato convertito e cosa resta da provare in gioco sta in `PORTING.md`.
> Il fork vive su github.com/ax-ten/tfcobblemon. Diverse assunzioni di questo
> documento si sono rivelate sbagliate alla prova dei fatti: sono corrette qui
> sotto, sezione per sezione.

**Decisione chiave**: invece di costruire Pokefirmacraft da zero, si parte dal **porting di TFCobblemon** (github.com/kingtrapinch/tfcobblemon, licenza GPL-3.0, quindi forkabile liberamente) — un mod esistente e funzionante di integrazione TerraFirmaCraft × Cobblemon — aggiornandolo a Cobblemon 1.8 e Minecraft 1.21.1/NeoForge.

**Cosa offre già TFCobblemon 1.1** (da portare):
- 250 spawn di Pokémon con loot table dedicate
- Ricette per Poké Ball, PC, Healing Machine, oggetti di evoluzione
- Medicine naturali trovabili in natura e tramite cucina
- Apricorn ottenuti rompendo le foglie degli alberi (non dall'Apricorn Tree di Cobblemon) — tipo di apricorn legato alla specie dell'albero/clima
- Golett craftabile con il nuovo item Blank Orb (caricato uccidendo Pokémon)
- Forme custom di Steelix e Scizor legate ai gradi di acciaio TFC
- Spawn per bioma, regione (caldo/freddo, secco/umido) e stagione
- Pokémon di acqua salata/dolce differenziati

**Cosa va portato/aggiornato tecnicamente**:
- Build system: ForgeGradle (1.20.1) → NeoGradle per NeoForge 1.21.1
- Dipendenza Cobblemon: ~1.5.x → 1.8.0 (nuovo sistema di spawn/Habitat Block, held item registry aggiornato — verificare breaking change)
- Dipendenza TerraFirmaCraft: TFC 3.x → TFC 4.x (versione usata da Gregnautics Continued)
- **Riconciliazione tier metallo**: le forme custom di Steelix/Scizor erano legate ai gradi di acciaio *TFC puro*; Gregnautics Continued unifica i metalli TFC e GregTech per eliminare i duplicati, quindi vanno riagganciate al sistema di materiali finale del pack, non al TFC originale
- ~~Script KubeJS: KubeJS 6 → KubeJS 7~~ — **assunzione sbagliata**: i tre script che
  TFCobblemon si portava in `data/tfcobblemon/kubejs/` non sono *mai* stati eseguiti,
  in nessuna versione. KubeJS legge solo dalla cartella `kubejs/` dell'istanza, mai
  dentro i jar. Erano codice morto, e con loro 16 dei 18 item della mod erano
  inottenibili. Ora sono ricette datapack + eventi Java, e la dipendenza da KubeJS
  non esiste più
- Licenza: il porting resta GPL-3.0 (obbligo della licenza originale)
- Bug ereditati corretti lungo la strada: dieci `species_additions` puntavano al
  Pokémon sbagliato per copia-incolla, i tag di bioma coprivano 30 biomi sui 125 di
  TFC 4, quattro ricette erano file `{}` che 1.21 rifiuta, e i resolver di Steelix
  e Golurk facevano piantare il client

**Cosa va aggiunto ex novo** (non presente in TFCobblemon, novità di Cobblemon 1.8 o del progetto più ampio):
- Alpha Pokémon (feature 1.8)
- Habitat Block (feature 1.8, controllo spawn)
- TM Machine aggiornata (feature 1.8)
- Held item introdotti tra Cobblemon 1.6 e 1.8 (Grip Claw, Booster Energy, Protective Pads, Punching Glove, Room Service, Scope Lens, Terrain Extender, Throat Spray, Utility Umbrella, Wide/Zoom Lens, ecc.)
- Estensione della progressione oltre l'Iron Age fino a Steam/Electrical/Nuclear Age (TFCobblemon copre solo la parte pretecnologica TFC; l'estensione GregTech è tutta da costruire)
- Vincolo di cattura Pasture Block/PC (non presente in TFCobblemon originale) —
  vedi sezione 6: **TFCobblemon lo contraddice attivamente**, non basta aggiungerlo
- Addon leggendari (Myths and Legends)
- **Trainer Belt, "siediti e aspetta" e borsa a categorie** (tutta la sezione 6):
  sostituiscono il vincolo Pasture Block/PC della prima stesura
- **Redesign delle ricette Poké Ball in chiave Greg** (vedi sezione 5): quelle
  ereditate da TFCobblemon sono crafting manuale a passo singolo e non rispettano
  il principio di profondità della sezione 1
- **Colori dei Golett**: la feature `dye` non applica l'aspetto in Cobblemon 1.8
  (`default: "none"` non è tra le scelte valide) — da sistemare o da ripensare
- **Traduzioni dei tag** introdotti dalla mod, altrimenti i visualizzatori di
  ricette mostrano l'id grezzo

La tabella di progressione per era alla sezione 4 resta valida come **roadmap per le aggiunte oltre TFCobblemon** (Steam Age in poi) e come riferimento per eventuali modifiche a ciò che TFCobblemon già implementa nelle ere pretecnologiche (Stone → Iron) — da verificare voce per voce contro l'implementazione reale del mod portato prima di decidere cosa mantenere invariato e cosa adattare ai principi della sezione 1.

## 1. Filosofia di design

- **Aggiungere, non affiancare**: nessuna progressione parallela. Ogni feature Cobblemon si aggancia a un'era TerraFirmaGreg esistente.
- **Niente downgrade di meccaniche**: se una macchina/meccanica Cobblemon esiste in una sola versione ufficiale (es. Healing Machine), non va scomposta in versioni "rudimentali" per riempire più tier.
- **Le meccaniche di battaglia Pokémon non sono legate alla progressione del mondo**: il tiering degli held item non riflette la loro potenza competitiva, ma quanto è sensato pensare di poterli ottenere in base alla loro natura (organica, minerale, meccanica, elettronica...).
- **Ricette allo stesso livello di complessità di TerraFirmaGreg**: nessuna scorciatoia. Le ricette Cobblemon-Greg devono avere la stessa profondità (step multipli, macchine, materiali intermedi) delle altre ricette dello stesso tier nel pack.
- **Riuso di processi esistenti** dove possibile (es. intaglio/taglio gemme di Greg per le Evolution Stone Ore) invece di inventare macchine dedicate.

## 2. Base tecnica

- **Minecraft**: 1.21.1
- **Mod loader**: NeoForge
- **Modpack di base**: Gregnautics Continued — porting artigianale 1:1 di TerraFirmaGreg-Modern, con TFC 4 + GregTech CEu 8 (build patchata) + Create + Create: Aeronautics, ~240 mod, 1.194 quest su 25 capitoli, worldgen con 89 vene minerarie/geodi
  - ⚠️ **Il pack è stato ritirato dal suo autore**: `ascorblack/Gregnautics-Continued`
    dà 404, e la pagina CurseForge non ha più uno slug pubblico (il progetto 1611173
    esiste solo nella cache dell'API, ultimo file 0.1.9 del 21 luglio 2026). L'unica
    fonte viva è il fork `drainstar/Gregnautics-Continued-Test`, 552 MB con i jar
    committati, aggiornato all'8 settembre 2026. **Va deciso se questa resta la base
    del progetto**: non c'è garanzia che venga mantenuta
  - Versioni verificate nel fork: TFC **4.2.5**, GTCEu **8.0.0 patchata a mano**,
    KubeJS **2101.7.2** (cioè KubeJS 7), Firmalife 3.0.11, Patchouli 1.21.1-93
- **Cobblemon**: v1.8.0 (NeoForge) — include Alpha Pokémon, Habitat Block, TM Machine aggiornata
- **Nota storica**: Cobblemon ha abbandonato il supporto Forge dopo la v1.3.2 (solo Fabric/NeoForge da allora); TerraFirmaGreg-Modern (Forge 1.20.1) era quindi incompatibile — da qui la scelta di Gregnautics Continued
- **Nota su Greate**: il mod Greate (ponte Create↔GregTech usato dalla TerraFirmaGreg originale) non è mai stato portato a 1.21.1/NeoForge (si ferma a 1.20.1/1.19.2). Gregnautics Continued lo sostituisce con un ponte custom scritto in KubeJS — non è un blocco, ma va tenuto presente se si cercano parti/ricette "Greate" testuali nel pack
- **Visualizzatore ricette**: il pack monta JEI più `kubejei`, ma la scelta per
  Pokefirmacraft è **EMI**. La mod non spedisce integrazioni per nessuno dei due, quindi
  è indifferente; lato pack invece il passaggio costa la riscrittura di
  `gregnautics_jei_material_hide.js`, che nasconde i duplicati dei 35 materiali
  unificati TFC↔GregTech e parla con `KubeJEIEvents`. GTCEu ha integrazione EMI nativa
  e EMI include JEMI per i plugin JEI; resta da verificare FTB Quests
- **Addon leggendari**: Myths and Legends (key item configurabili per attivare lo spawn) — scelto al posto di Legendary Monuments (rischio compatibilità worldgen con TFC) e Legendary Encounters (poco personalizzabile)
- **Compatibilità worldgen Cobblemon↔TFC**: Cobblemon assegna gli spawn tramite tag di bioma; i biomi custom di TFC/TFG non sono taggati di default (stesso problema noto con altri mod di worldgen come Terralith) — serve un datapack dedicato che mappi i biomi alle categorie di spawn di Cobblemon, altrimenti i Pokémon non compaiono in gran parte del mondo
  - **Parzialmente risolto**: i tag di TFCobblemon coprivano 30 biomi, TFC 4 ne ha 125.
    Ora i gruppi (`tfc:all`, `tfc:oceans`, `tfc:common`...) poggiano sui tag che TFC 4
    popola da sé (`#c:is_ocean`, `#c:is_mountain`, `#c:is_badlands`...) più i sette
    biomi che nessun tag copre. Resta da decidere a mano in quale fascia di altitudine
    stiano i 96 biomi nuovi, e va rifatto il controllo sui biomi aggiunti dal pack

## 3. Approccio implementativo

**Punto di partenza**: fork di TFCobblemon (vedi sezione 0) aggiornato a NeoForge 1.21.1 + Cobblemon 1.8.0 + TFC 4, innestato su Gregnautics Continued.

La maggior parte delle modifiche successive al porting base (spostamento materiali, gate di crafting, ricette per l'estensione GregTech) può essere fatta con **datapack**, ed eventualmente con **KubeJS lato pack**.

⚠️ **Correzione**: il documento dava per scontato che la mod potesse spedire script
KubeJS, "coerentemente con come TFCobblemon stesso è già strutturato". Non è così:
KubeJS carica gli script solo dalla cartella `kubejs/` dell'istanza, mai da dentro i
jar delle mod. Gli script di TFCobblemon non hanno mai girato. Quindi la regola è:

- **tutto ciò che è dentro la mod** va espresso come datapack o come codice Java
- **KubeJS resta uno strumento di pack**, per gli aggiustamenti che si fanno
  sull'istanza (l'unificazione dei materiali, il ponte Create↔Greg, e simili)

Questo sposta verso il codice più cose di quante il documento ne prevedesse.

Serve invece **codice reale** (Kotlin/Java, tramite gli eventi/API pubblici di Cobblemon) per:
- Il vincolo di cattura: bloccare il lancio della Poké Ball se il giocatore non ha un Pasture Block attivo o un PC (vedi sezione 6) — non presente in TFCobblemon originale, da aggiungere
- L'integrazione delle feature 1.8 (Alpha Pokémon, Habitat Block) che potrebbero richiedere hook più profondi dei semplici script KubeJS

## 4. Tabella di progressione per era

| Era TerraFirmaGreg | Feature Cobblemon sbloccate |
|---|---|
| **Stone Age** | Nessuna cattura (solo osservazione); Poké Rod (versione unica, non craftabile in tier "rudimentale"); agricoltura di base incluse le piante di Mint → Nature Mints; cura passiva (letto, bacche selvatiche) |
| **Copper Age** | Ball antiche (Apricorn + Tumblestone + Copper ingot, quasi invariate da Cobblemon vanilla); Apricorn Tree e Berry Tree coltivabili; Type Gems come loot in rovine/strutture o crescita naturale su Crystal Cluster/Deepslate Core (mai craftabili); Incensi (erboristeria) |
| **Bronze Age** | Pasture Block (fino a 8 Pokémon); Ball base con ingot Greg equivalenti; ibridazione bacche come meccanica unica e completa (non frammentata su più ere); Breeding completo (IV/nature/abilità incluse da subito, agganciato al Pasture Block); minerali/polveri grezze come held item (Bright Powder, Black Sludge, Charcoal Stick, Black Belt, Black Glasses, Silver/Gold Powder) |
| **Iron Age** | Ball evolute (Great/Ultra-equivalenti); Evolution Stone Ore lavorabili (riusando processi di intaglio/taglio gemme Greg già esistenti); Vitamins/Mochis; Plates dei tipi; accessori in cuoio/tessuto (Choice Band/Scarf, Binding Band, Grip Claw, Punching Glove, Protective Pads, Muscle Band) |
| **Steam Age** | Automazione delle linee di produzione Ball; Healing Machine (versione unica e completa, non prima); Mega Evolution (Mega Bracelet/Mega Stones); ottica di precisione come held item (Wide/Zoom/Scope Lens, Metronome, Choice Specs, Wise Glasses, Clear Amulet) |
| **Electrical Age (LV+)** | PC (storage dimensionale); TM Machine avanzata (materiali Cobblemon nativi + potenziatori/catalizzatori Greg); Habitat Block (controllo spawn); Fossil Analyzer + Restoration Tank; Data Monitor; held item "energetici/chimici" espliciti (Life Orb, Air Balloon, Eject Button/Pack, Absorb Bulb, Weakness/Blunder Policy, Red Card, Safety Goggles, Utility Umbrella, Room Service, Throat Spray) |
| **Nuclear Age** | Alpha Pokémon di fascia altissima; leggendari/mitici (via addon Myths and Legends, key item posizionati a questo tier); economia Relic Coin/Gimmighoul avanzata; Ability item (Ability Capsule/Patch/Shield); held item energetici più estremi (Cell Battery, Adrenaline Orb) |

**Nota su reliquie/parti di Pokémon** (King's Rock, Razor Fang/Claw, Deep Sea Tooth/Scale, Thick Club, Light Ball, Soul Dew, Metal/Quick Powder): non si craftano, si trovano/droppano da Pokémon specifici → disponibili già da **Copper Age**, appena si inizia a catturare/combattere.

## 5. Poké Ball — dettaglio

- **Ball antiche**: restano quasi identiche a Cobblemon vanilla (Apricorn + Tumblestone + Copper ingot) — già coerenti con l'early game
- **Ball base/evolute**: l'ingot vanilla (iron/gold) è sostituito dall'ingot Greg equivalente dello stesso tier
- **Automazione**: linee Greg dedicate per processare Apricorns/Tumblestone in serie, sbloccabile in Steam Age — macchine dedicate, non crafting manuale

### 5.1 Le ricette ereditate non vanno bene — da rifare

Le ricette Poké Ball che arrivano da TFCobblemon sono **crafting manuale a passo
singolo**: apricorn + tumblestone + lingotto in un banco da lavoro, e via. Funzionano,
ma contraddicono il principio di profondità della sezione 1 — nel resto del pack un
oggetto di quel livello richiede fusione, stampi, lavorazione all'incudine.

Il redesign è **da progettare da zero**, e va deciso almeno:

- quali passaggi intermedi introdurre (lamiera stampata? guscio superiore/inferiore
  da assemblare? meccanismo di scatto?), e con quali macchine per ciascuna era
- se le ball antiche restano l'eccezione artigianale — sono di apricorn, ha senso che
  non passino da una macchina — mentre solo quelle "moderne" diventano industriali
- come si aggancia ai materiali *unificati* del pack invece che ai lingotti TFC puri

Nota pratica: GregTech è stato aggiunto al runtime di sviluppo (bastano `gtceu` e
`ldlib2`, il resto è jarjar), quindi il lavoro è verificabile senza avviare l'istanza
completa del pack.

### 5.2 Struttura della catena — proposta

Le ball si smontano in tre pezzi, e ogni pezzo diventa un passaggio:

1. **Il guscio** — la parte metallica, che porta il tier del materiale
2. **Il nucleo** — l'apricorn, che porta il colore e quindi l'identità della ball
3. **L'assemblaggio** — a mano nelle ere pretecnologiche, in assembler dalla Steam Age

**Rustic** (ex "Ancient", Copper Age): guscio di tumblestone + rame → assemblato con
l'apricorn del colore giusto. Restano artigianali: sono di apricorn, non ha senso
industrializzarle. Ma due passaggi invece di uno.

**Ball moderne** (Bronze → Iron): guscio dalle lamiere del tier (i tag già
predisposti), assemblaggio con apricorn e tumblestone. La resa scende da **16 a 1**:
oggi quattro apricorn e una lamiera danno sedici ball, che è l'opposto di quanto
chiede la sezione 1.

**Dalla Steam Age**: gli stessi gusci escono da bender/estrusore e l'assemblaggio passa
in assembler — l'automazione della sezione 5, senza inventare macchine nuove.

**Costo da mettere in conto**: la catena richiede almeno un item intermedio per tier
(il guscio), quindi **nuove texture**. Senza quelle non è implementabile: è l'unico
motivo per cui questa sezione è ancora una proposta e non codice.

### 5.3 Dove vivono le ricette delle macchine Greg

GregTech **non spedisce ricette come file**: le genera a runtime dal registro dei
materiali. Il pack ci lavora sopra solo con KubeJS (569 chiamate a
`event.recipes.gtceu.assembler`). Per la mod restano tre strade — JSON datapack nel
formato di GTRecipe, KubeJS lato pack, o l'API di GTCEu da Java — e la scelta vincola
tutte le ere da Steam in poi. Vedi sezione 9.

### 5.4 Le ball moderne — ripartizione per effetto

Cobblemon ha gia' una scala a cinque tier (`tier_N_poke_balls` e
`tier_N_poke_ball_materials`, agganciati a copper / iron / gold / diamond). La
struttura ci serve, i contenuti no: il materiale lo rimappiamo su TFC e Greg, e
il tier di Cobblemon mette insieme effetti di natura molto diversa — la moon
ball, che legge una fase lunare, sta nello stesso scaglione della dive ball, che
sente solo se il bersaglio e' sott'acqua.

Il criterio qui e' un altro: **cosa deve sapere la ball per funzionare.** Un
colore non e' un effetto, quindi i dye escono dalla catena; l'unico posto dove
il colore conta e' il coperchio delle sei ball-ricolore, e li' arriva
dall'apricorn, che e' una pianta, non un pigmento.

**I tre pezzi restano gli stessi delle rustic, ma cambiano provenienza:**

| Pezzo | Rustic | Moderne |
|---|---|---|
| Base | colata a semisfera, una per volta | **tin sheet** battuta sull'incudine, piu' basi per foglio |
| Core | tumblestone scartavetrata, una per volta | **tumblestone + ender pearl** nel pentolone, piu' core per cottura |
| Coperchio | mezza apricorn | il reagente che porta l'effetto |

La tin sheet e' la scelta giusta per la produzione in serie: lo stagno e' un
metallo di tier -1 in TFC (si salda e si batte sull'incudine di rame, la prima
disponibile) e la lamiera e' l'unica forma da cui abbia senso ricavare piu'
gusci in un colpo. Il core in serie usa il pentolone (`tfc:pot`), che accetta
cinque ingredienti e restituisce cinque oggetti: una ender pearl semina quattro
tumblestone.

**Il coperchio porta l'effetto.** Ogni ball prende un reagente caratteristico
oltre alla lamiera — il peso di piombo per la heavy, la lenza per la lure, la
rete per la net, l'argento per la moon, lo scappamento per la timer. Cosi' la
ricetta si legge da sola e non serve nessun dye. La lista reagente-per-ball e'
il prossimo punto da fissare, una ball per volta.

**Ripartizione per era:**

| Era | Cosa deve sapere la ball | Ball |
|---|---|---|
| **Iron** | niente, moltiplicatore fisso | poke, citrine, verdant, azure, roseate, slate, premier, great |
| **Iron** | una proprieta' fisica del bersaglio o del posto | heavy (peso), dive (sommerso), lure (durante la pesca), net (Acqua/Coleottero), nest (livello basso), safari, park, sport |
| **Iron** | un numero da confrontare, con una scala o un almanacco | level, fast, friend, moon |
| **Iron** | erboristeria | heal |
| **Steam** | moltiplicatore fisso, lega migliore | ultra |
| **Steam** | tempo e memoria | timer (conta i turni), quick (primo istante), repeat (specie gia' catturata) |
| **Steam** | un sensore | dusk (livello di luce) |
| **Steam** | manifattura fine | luxury, love |
| **Electrical** | stati di coscienza e altre dimensioni | dream, beast |
| **Nuclear** | — | master, rustic origin |
| mai craftabile | dono | cherish |

Ventuno ball in Iron, sette in Steam, due in Electrical, due in Nuclear.
Timer, repeat, quick, ultra e dusk sono in Steam per decisione presa; luxury e
love le ho messe li' perche' sono le due che non leggono niente del mondo ma
chiedono una lavorazione fine del guscio, che a mano non viene.

## 6. Trasporto, cattura e inventario

Questa sezione sostituisce il vincolo "niente Pasture Block, niente lancio" della
prima stesura. Quel gate era **negativo**: il gioco ti negava un'azione con un
messaggio. Quello che segue è **positivo** — hai una cintura, e la cintura ha dei
posti. Il giocatore capisce da sé perché non può ancora catturare, senza che
glielo si debba dire.

### 6.1 Trainer Belt

Un oggetto in un singolo slot **Curios** (presente nel pack: `curios-neoforge 9.5.1`),
che è ciò che permette di portarsi dietro le ball.

- **Senza cintura**: una ball sola, quella dello starter
- **Prima cintura**: 2 ball
- **Un solo gradino intermedio**: 4 ball
- **Fino all'Iron Age**: 6 ball

La prima versione si fa **conciando la pelle**, che in TFC è già una catena vera:
pelle grezza, ammollo nella calce in botte, raschiatura sul tronco, secondo ammollo,
essiccazione. Cinque passaggi che non dobbiamo inventare.

Sulla tumblestone: TFC ha già `tfc:glue`, che è la colla del pack, quindi usare la
tumblestone *come resina* duplicherebbe una funzione esistente. Meglio come **fibbia**
— un cristallo incastonato, visibile, lo stesso materiale che poi si ritrova nei
meccanismi delle ball. Se invece si vuole la resina, si macina al quern e si mescola
alla colla.

Gli aggiornamenti di capienza salgono col metallo, in linea con le ere.

### 6.2 "Siediti e aspetta"

Trattare il Pokémon più come il cane di Minecraft che come un oggetto in tasca: gli si
dice di sedersi, e questo lo **toglie temporaneamente dalla squadra** lasciandolo nel
mondo.

Non va inventato: `PokemonEntity` ha già il campo **`tethering`**, il legame che usa il
Pasture Block per tenere un Pokémon fuori dal party. Il "siediti" è lo stesso
meccanismo legato a una posizione invece che a un blocco.

Da risolvere: persistenza del legame se il chunk si scarica, se il Pokémon muore, se il
giocatore va lontano.

### 6.3 Lo starter

Niente ball utilizzabili all'inizio. In Cobblemon ogni Pokémon porta una `caughtBall`,
ma è un dato di provenienza, non un oggetto nell'inventario — da verificare in gioco che
la schermata iniziale non ne regali una vera.

Esteticamente la **Rustic Origin Ball** è la provenienza giusta per lo starter: è solo
visivo, ma racconta che quel Pokémon non l'hai preso con una ball che ti sei costruito.

**Interfaccia**: nascondere gli slot vuoti della squadra finché la cintura non li
sblocca, così è evidente che ancora non si cattura invece di sembrare un bug.

### 6.4 La borsa

Una borsa in stile Pokémon — **categorie e limite duro di stack per categoria** — in un
secondo slot Curios. Non uno zaino generico: il punto è il limite, non la capienza.

Non esiste niente da integrare: su 1.21.1 nessuna mod fa una borsa così per Cobblemon
(Loot Bag è un contenitore di bottino, gli zaini del pack non hanno categorie).
Cobblemon offre però il registro **`bag_items`**, che definisce gli oggetti usabili in
battaglia ed è estendibile da datapack: il vocabolario per la categoria "medicine" c'è
già.

Le categorie possono aprirsi con le ere — medicine da subito, MT in Electrical Age, key
item alla fine — così la borsa diventa un altro modo di leggere la progressione.

**Costo**: è il pezzo di codice più grosso del progetto. `MenuType`, storage con filtri
per slot, schermata client con linguette, sincronizzazione, persistenza sull'oggetto
Curios, più la grafica dell'interfaccia. Il limite per categoria, che è ciò che la rende
una borsa Pokémon, è invece la parte facile.

**Ordine**: dopo la cintura e dopo il rifacimento della cattura. Una borsa che organizza
per categorie oggetti che ancora non esistono è un contenitore vuoto con delle etichette.

### 6.5 Cosa resta del vincolo originale, e perché il fork si è separato

Del gate "Pasture Block o PC" non resta quasi nulla: il limite è la cintura, e il
Pasture Block torna a essere ciò che è — un posto dove *lasciare* i Pokémon, non un
lasciapassare per catturarli.

Resta però il motivo per cui questo progetto ha smesso di essere un fork di TFCobblemon:
là il **PC è craftabile nella fase pretecnologica**, il che rompe la tabella della
sezione 4 (che lo colloca in Electrical Age) e svuota qualunque limite all'accumulo di
Pokémon. Non è un bug del porting, è una divergenza di design — e con la cintura la
distanza fra i due progetti si allarga ancora.

## 7. Alpha Pokémon e leggendari

- Gli **Alpha Pokémon non sono legati al tier tecnologico** del giocatore — spawnano secondo le meccaniche native di Cobblemon, senza sistemi di drop/materiali ibridi custom
- I **leggendari/mitici** sono l'eccezione: gestiti tramite l'addon **Myths and Legends**, con i key item che ne attivano lo spawn posizionati in **Nuclear Age**
- Trading e PvP tra giocatori restano **invariati** rispetto a Cobblemon vanilla

## 8. Quest (FTB Quests)

TerraFirmaGreg – New Horizons usa FTB Quests come sistema di quest. Le quest Pokefirmacraft vanno pensate come un **capitolo dedicato** agganciato ai traguardi già esistenti nel quest book di TFG (es. "hai raggiunto Bronze Age" sblocca il capitolo Cobblemon corrispondente).

**Struttura del capitolo: non ancora definita — da progettare in una fase successiva.**

## 9. Punti aperti / da decidere durante lo sviluppo

**Decisioni di fondo**

- **Su quale pack poggiare**: Gregnautics Continued è stato ritirato dall'autore e
  sopravvive solo come fork di terzi (vedi sezione 2). Va deciso se accettare il
  rischio, forkare a propria volta il pack, o agganciarsi ad altro
- **Separazione da TFCobblemon** (sezione 6.1): staccare il fork su GitHub e smettere
  di trattare l'upstream come base da seguire

**Progettazione**

- Redesign completo delle ricette Poké Ball in chiave Greg (sezione 5.1)
- Ricollocazione di PC e Pasture Block secondo la sezione 4
- Capienza e materiali degli aggiornamenti della Trainer Belt (sezione 6.1)
- Quali categorie ha la borsa e con che era si aprono (sezione 6.4)
- Struttura dettagliata del capitolo di quest FTB (nomi, ordine, traguardi di sblocco)
- Held item non ancora assegnati singolarmente su tutti i 101 esistenti in Cobblemon (la sezione 4 copre le categorie principali per rappresentanza, non ogni singolo item)
- Dettaglio tecnico dei processi Greg di intaglio/taglio gemme da riusare per le Evolution Stone Ore (va verificato quale macchina/processo Greg esatto si applica)
- Fascia di altitudine dei 96 biomi nuovi di TFC 4 (sezione 2)

**Tecnica**

- La feature `dye` dei Golett non applica l'aspetto in Cobblemon 1.8
- Se aggiungere GregTech al runtime di sviluppo, senza il quale il lavoro delle
  sezioni 4 e 5 non è verificabile
- Verifica in gioco di quanto elencato in `PORTING.md`
