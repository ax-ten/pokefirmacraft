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
| Core | tumblestone + carta vetrata, uno per volta | **impasto di ender e tumblestone** colato in teglia, otto per volta |
| Coperchio | mezza apricorn | il reagente che porta l'effetto |

La tin sheet e' la scelta giusta per la produzione in serie: lo stagno e' un
metallo di tier -1 in TFC (si salda e si batte sull'incudine di rame, la prima
disponibile) e la lamiera e' l'unica forma da cui abbia senso ricavare piu'
gusci in un colpo — quattro basi per foglio, cinquanta millibucket di stagno
l'una contro i cento della colata rustic, perche' e' un guscio stampato
sottile e non una semisfera piena.

Il core in serie passa dal fuso. Una ender pearl macinata alla mola piu'
quattro tumblestone grezze fanno un impasto; l'impasto fonde a **1200 gradi**,
che la legna non raggiunge (arriva a 757) mentre la carbonella si (1350), e
rende duecento millibucket di fuso; il fuso si cola in una teglia di ceramica
da otto cavita' e da' otto capture core in una volta. Lo stampo si knappa
dall'argilla e si cuoce come tutte le ceramiche di TFC.

Il core singolo invece si fa con la **carta vetrata**, esattamente come TFC
lucida le gemme grezze (`tfc:advanced_shapeless_crafting` con
`tfc:damage_crafting_remainder`, la carta torna consumata): una ricetta da
banco normale, che si legge in EMI, al posto dello scraping di prima che
finiva sotto World Interaction e non si trovava.

**Il coperchio porta l'effetto.** Ogni ball prende un reagente caratteristico
oltre alla lamiera — il peso di piombo per la heavy, la lenza per la lure, la
rete per la net, l'argento per la moon, lo scappamento per la timer. Cosi' la
ricetta si legge da sola e non serve nessun dye. La lista reagente-per-ball e'
il prossimo punto da fissare, una ball per volta.

**Le due scale non sono la stessa cosa.** Il metallo decide *cosa* si puo'
fare, la macchina decide *quante*. In TFC lo si vede dal trip hammer, che
`workRemotely` con `HIT_LIGHT` sull'incudine accanto: toglie i click, non
cambia la resa. In Greg il moltiplicatore arriva dalla forming press con lo
stampo, non da una lega migliore. Una lamiera di red steel sono gli stessi
duecento millibucket di una di stagno, quindi non puo' rendere otto volte
tanti gusci — se cambia qualcosa, ne rende meno, perche' il red steel costa.

| Come si fa il guscio | Resa per lamiera | Era |
|---|---|---|
| colata a semisfera | 1 | rustic |
| lamiera sull'incudine | 4 | Iron |
| trip hammer su incudine, mosso da acqua o vento | 4, senza cliccare | Iron |
| steam forge hammer | 8–16 | Steam |
| forming press con stampo | 32–64 | Electrical |

Il metallo invece fa il **grado** della ball, che e' la scala che Cobblemon ha
gia' (i suoi `tier_N_poke_ball_materials` sono rame, ferro, oro, diamante):
stagno per la base, wrought iron per la great, acciaio per la ultra, gli
acciai esotici per le ultime. Cosi' le due scale restano ortogonali: il
metallo da' il moltiplicatore fisso, il reagente del coperchio da' l'effetto.

**Il collo di bottiglia si sposta.** Una ball e' core piu' base piu'
coperchio: se la lamiera rende trentadue gusci ma i core e i coperchi vengono
uno per volta, il moltiplicatore e' decorativo. Le tre linee devono salire
insieme — la teglia dei core rende otto, quindi il passo naturale dell'era del
ferro e' otto ball per ciclo, e il coperchio ha bisogno di una via in blocco
che regga lo stesso numero.

**Ripartizione per era:**

| Era | Cosa deve sapere la ball | Ball |
|---|---|---|
| **Iron** | niente, moltiplicatore fisso | poke, citrine, verdant, azure, roseate, slate, premier, great |
| **Iron** | una proprieta' fisica del bersaglio o del posto | heavy (peso), dive (sommerso), lure (durante la pesca), net (Acqua/Coleottero), nest (livello basso), safari, park, sport |
| **Iron** | un numero da confrontare a occhio | level, friend |
| **Iron** | erboristeria | heal |
| **Steam** | moltiplicatore fisso, lega migliore | ultra |
| **Steam** | tempo e memoria | timer (conta i turni), repeat (specie gia' catturata) |
| **Steam** | un numero da leggere su un quadrante | moon (fase lunare), fast (velocita' base) |
| **Electrical** | fra le piu' forti | dusk, quick |
| **Electrical** | puro comfort | luxury, love |
| **Electrical** | stati di coscienza e altre dimensioni | dream, beast |
| **Nuclear** | — | master, rustic origin |
| mai craftabile | dono | cherish |

Diciannove ball in Iron, cinque in Steam, sei in Electrical, due in Nuclear.

### 5.5 Le tre vie per assemblare una ball, e cosa costano

| Via | Fluido per ball | Cosa serve | Cosa da' |
|---|---|---|---|
| banco a mano | 12,5 mB (via stampo) | niente | 2 ball per craft |
| catena di Create | **10 mB** | depot, deployer, spout, pressa | continua, e si vede cosa passa |
| Assembler di GregTech | **10 mB** | Assembler + energia | scala col tier e col parallelo |

**Dieci millibucket sono il fondo**, e resta tale anche nelle ere successive:
la macchina migliore da' velocita' e scala, non un fluido piu' economico.
Altrimenti a fine gioco una ball costerebbe quasi niente, e il capture core —
che e' il pezzo che rende una ball una ball — smetterebbe di pesare.

La colata rende otto core da duecento millibucket, e ogni core fa due ball:
dodici e mezzo per ball. Le due vie automatiche saltano il core e prendono il
fuso direttamente, e costano meno perche' non passano dallo stampo.

**In cima alla scala il core non c'e' piu'.** Le ball di ultimo tier prendono al
suo posto il **logic processor di Applied Energistics** (`ae2:logic_processor`):
non un core migliore, un altro pezzo. E' la sostituzione giusta perche' dice la
cosa giusta — il core e' il fermo che tiene dentro un Pokemon, e a fine gioco
quel fermo diventa un calcolatore. Il resto di quelle ricette e' da decidere.

**Come cresce la resa, e dove mi ero sbagliato.** Avevo scritto che il rapporto
della ricetta non cambia mai e che la scala viene solo dal tier della macchina.
E' falso, e il pacchetto lo dimostra riga per riga. In
`kubejs/server_scripts/tfg_port/tfg.server.machines.recipes.components.js` il
tubo a elettroni ha **tre ricette da banco** che convivono:

| Ricetta | Ingredienti | Resa |
|---|---|---|
| `electron_tube` | tubo di vetro, 2 bulloni d'acciaio, piastra di legno, 2 fili di lega rossa, lamiera di ferro battuto (+ cacciavite e tronchese) | **1** |
| `electron_tube2` | tubo di vetro, **circuito stampato in plastica**, 2 fili di lega rossa | **4** |
| `electron_tube3` | **chip NAND**, circuito stampato in plastica | **4** |

Quindi la convenzione vera e' un **terzo asse**: avanzando di era non si
raffina la stessa ricetta, se ne aggiunge una nuova con **meno pezzi, piu'
avanzati, e resa maggiore**. La vecchia resta valida, semplicemente non
conviene piu'. Gli stessi tre gradini ci sono anche sull'Assembler: 2, 4, 4.

Quello che invece resta vero e' il perche' entro *una* ricetta non si fa "tre
dentro quattro fuori": li' la scala e' il **tier della macchina** — ogni tier
sopra raddoppia la velocita' per quadrupla energia — e gli **hatch in
parallelo** dei multiblocco, che macinano N ricette identiche con rapporto uno
a uno. Sono due cose diverse e le avevo messe nello stesso sacco.

**Conseguenza per le nostre ball**: le ricette di un'era piu' avanzata non
devono limitarsi ad andare piu' veloci, devono chiedere meno pezzi e migliori e
darne di piu'. Il fondo di dieci millibucket di fluido per ball resta — quello
e' una decisione, non una convenzione — ma la resta in item puo' e deve
salire.

### 5.6 Le ball che mancano

Coperte: **ventitre**, cioe' tutta l'era del ferro tranne la sport, e tutta
quella del vapore. Ognuna ha ricetta a mano, catena di Create e Assembler.

Restano da progettare:

- **Electrical**: dusk, quick, luxury, love, dream, beast — sei, e sono quelle
  con gli effetti piu' astratti, quindi anche quelle con i reagenti meno ovvi
- **Nuclear**: master
- **mai craftabili**: cherish e sport, che si trovano

### 5.7 Il Pokemon Storage Component — la spina dorsale industriale

Nell'era industriale avanzata le ball non si fanno piu' un pezzo alla volta: si
fanno attorno a un componente, e quel componente e' lo stesso che regge il PC e
i gradi alti della cintura. E' il pezzo che dice "adesso i Pokemon si gestiscono
su scala".

**Tre gradi, e sono tre perche' AE2 ne ha gia' tre.** Non stiamo inventando una
scala: stiamo prendendo in prestito i pioli di una che il giocatore conosce
gia', quella delle celle — 1k, 4k, 16k — dove **ogni piolo costa quattro del
piolo sotto** piu' un processore migliore. La progressione viene gratis dalla
forma della ricetta, senza che serva spiegarla.

| Grado | Forma della ricetta (verificata in AE2 19.2.17) | A cosa serve |
|---|---|---|
| **PSC-1** | come `cell_component_1k`: polvere di redstone + **tumblestone** + il nostro processore su tumblestone | ball di tier 1 e 2, **e la cintura** |
| **PSC-2** | come `cell_component_4k`: redstone + calculation processor + **4x PSC-1** + vetro al quarzo | tutte le altre ball, **tranne la master** |
| **PSC-3** | come `cell_component_16k`: polvere di glowstone + calculation processor + **4x PSC-2** + vetro al quarzo | **il PC e la master ball** |

Tre e non due: due pioli metterebbero la master ball e la cintura a due passi di
distanza, e sono i due estremi della progressione. Se in gioco il terzo piolo
risultasse aria, si fondono PSC-2 e PSC-3 e la master resta l'unica cosa che
chiede quattro componenti invece di uno.

**La tumblestone su scala industriale, con le macchine di AE2 e zero codice.**
Il collo di bottiglia e' la tumblestone: la vuole il capture core, la vuole il
fluido di cattura, e ora la vuole il componente. In AE2 il certus si moltiplica
da se' e non si mina, e la stessa strada e' aperta a noi perche' i due tipi di
ricetta che servono sono **data-driven**:

- `ae2:charger` — tumblestone dentro il charger diventa **tumblestone carica**,
  come `charged_certus_quartz_crystal`
- `ae2:transform` — tumblestone carica **+ polvere di tumblestone** gettate in
  acqua danno **due** tumblestone, esattamente come
  `data/ae2/recipe/transform/certus_quartz_crystals.json`

Da cui: la tumblestone raddoppia, e i growth accelerator di AE2 accelerano quello
che cresce in acqua senza che noi tocchiamo niente. Ci manca solo la **polvere di
tumblestone base**: oggi esiste solo `tfcobblemon:sky_tumblestone_powder`.

**Cosa NON cambia, e va detto.** La catena pre-industriale resta valida per
intero — il knapping, le tre linee rustiche, la base in stagno, i coperchi. Vale
la convenzione del tubo a elettroni (sezione 5.5): la via industriale chiede
**meno pezzi, migliori, e ne da' di piu'**, e quella vecchia continua a
funzionare, semplicemente smette di convenire. Se invece il componente diventasse
obbligatorio, l'intera eta' del ferro andrebbe buttata, e non e' quello che
vogliamo.

### 5.8 Dove si trova la tumblestone

Cobblemon la mette nei suoi geodi, che sotto il generatore di TFC non nascono
mai: la worldgen e' nostra, e segue la maniera di TFC.

**Vene nella roccia, non cristalli nel vuoto.** Il geode e' basalto indurito
fuori, quarzite dentro, e lo strato alternativo e' un minerale nostro —
`tumblestone_quartzite` e le due varianti — sul modello `tfc:block/ore`, con la
forma dello strato di ametista di TFC ritinta coi colori veri di Cobblemon. La
cavita' e' vuota. Un cluster sarebbe un blocco appoggiato, e ogni grattacapo
che abbiamo avuto coi geodi veniva da li'.

**Tre varieta', due rarita'.** La tumblestone normale a `rarity_filter` 16 su
tutta la fascia -32..48; sky e black a 40 ciascuna, sbilanciate verso il basso
(-56..24, `very_biased_to_bottom`), percio' in profondita' i geodi sono piu'
densi e piu' spesso delle due varianti pregiate. Dentro la fodera, il 4% dei
blocchi di minerale e' un sito da scavare.

**Il guscio e' crepato, e dentro resta asciutto.** La crepa e' quella di
vanilla — un buco nel guscio, che e' anche il modo di accorgersi di un geode
scavandogli accanto — ma un buco vuol dire che da fuori puo' entrare acqua, e
un geode pieno d'acqua non ha senso. Si chiude a monte invece che a valle:
`invalid_blocks_threshold` a **zero**, cioe' il geode rinuncia a nascere se
trova una sola goccia d'acqua nel suo ingombro, dove vanilla ne tollera una —
e quella una, se capita sulla crepa, e' la falla. Non costa niente: su un mondo
di prova il conteggio dei minerali e' rimasto identico a prima, e di 2650
blocchi di minerale **nessuno** aveva acqua entro due blocchi. Gia' erano
asciutti; ora lo sono per costruzione.

**I sassi in superficie sono un indizio, non decorazione.** Sopra ogni geode
che nasce, sulla sua colonna, cadono da tre a sei sassi sciolti dello stesso
cristallo — la feature `tfcobblemon:geode_trail`, che piazza il geode di
vanilla e poi, solo se ha attecchito, sparge i sassi chiedendo l'altezza del
terreno uno per uno (su un pendio un'altezza sola li lascerebbe a mezz'aria).
Lo spargimento sparso e indipendente che c'era prima e' stato **tolto**: se
meta' dei sassi non avesse niente sotto, nessuno scaverebbe piu' sotto
nessuno. E' lo stesso patto che TFC fa coi suoi sassi sciolti, che dicono
sempre la verita' su che roccia hai sotto i piedi.

## 6. Trasporto, cattura e inventario

Questa sezione sostituisce il vincolo "niente Pasture Block, niente lancio" della
prima stesura. Quel gate era **negativo**: il gioco ti negava un'azione con un
messaggio. Quello che segue è **positivo** — hai una cintura, e la cintura ha dei
posti. Il giocatore capisce da sé perché non può ancora catturare, senza che
glielo si debba dire.

### 6.1 Trainer Belt

Lo slot **`belt`** esiste dentro Curios, ma **non e' dato a nessuno.** Sono due
cose separate, e vanno tenute distinte:

- il **tipo** di slot lo spedisce Curios, in
  `data/curios/curios/slots/belt.json` del suo jar: ordine 180, icona
  `curios:slot/empty_belt_slot`, validatore `curios:tag`. Nessuna dimensione.
- la **dimensione** e **chi lo riceve** li deve mettere una mod. Nel pacchetto
  lo fa ToolBelt, con `data/toolbelt/curios/slots/belt.json` che contiene solo
  `{"size": 1}` e `data/toolbelt/curios/entities/curio_slots.json` che lo
  concede a `minecraft:player`.

Quindi facciamo gli stessi due file sotto il nostro namespace, con lo stesso
contenuto. Il percorso e' `data/<namespace>/curios/slots/…`, con `curios`
**dentro** il namespace e non come namespace: `data/curios/slots/belt.json` non
lo legge nessuno. La dimensione va dichiarata e non sommata — l'operazione
predefinita e' `SET` — perche' lo slot e' uno ed e' condiviso: se ognuno
aggiungesse il suo, chi ha ToolBelt si troverebbe due posti alla cintura.

La dimensione uno decide l'architettura.

**Come si portano le ball.**

| Cosa indossi | Ball a portata |
|---|---|
| niente, o una ball nuda nello slot belt | 1 |
| Trainer Belt di cuoio | 3 |
| Trainer Belt completa | 6 |

Lo slot e' uno, quindi la cintura non allarga lo slot: la cintura **e'**
l'oggetto indossato, e tiene lei le ball in un contenitore suo. Allargare
`belt` a sei darebbe sei slot anche a ToolBelt e a qualunque altra cosa lo usi,
che non e' nostro diritto.

**La squadra e' la cintura.** Le ball sulla cintura sono, nell'ordine, gli slot
della squadra, e sono quelle che compaiono nell'elenco a sinistra. Non c'e' una
"squadra" separata da gestire: se la ball non e' sulla cintura, il Pokemon non
e' a portata.

**La cintura da sei e il suo cancello.** Quella di cuoio da tre si fa con tre
ami, uno per posto, a cui appendere le ball. Quella da sei si **fucina sulla
prima**: lamiera di **acciaio rosso** come template, la cintura di cuoio come
base, del cuoio come aggiunta. E le ball restano dentro, perche' la fucinatura
ricopia i componenti del pezzo base.

L'acciaio rosso e' il cancello, e non serve altro: e' uno dei due metalli di
fine corsa di TFC, vuole l'altoforno e una catena di leghe che non esiste prima.
Un oggetto fatto col fluido di cattura sarebbe stato un passaggio in piu' per
dire la stessa cosa.

**Si riusa la cintura vecchia, e il motivo e' che dentro ci sono i Pokemon.**
Rifarla da zero vuol dire o perdere quello che ha dentro o dover svuotarla prima
— un passaggio in piu' che si dimentica esattamente quando costa di piu'. Ed e'
anche la convenzione di tutto quello che contiene roba: gli zaini delle mod si
migliorano col vecchio zaino nella ricetta, e vanilla passa da diamante a
netherite tenendosi l'oggetto.

Tecnicamente si fa in due modi, e quello che non funziona e' **il crafting
vanilla cosi' com'e'**: `crafting_shaped` e `crafting_shapeless` costruiscono il
risultato dalla ricetta e i componenti del pezzo vecchio non arrivano. Gli zaini
delle mod tengono il contenuto perche' si scrivono un serializzatore di ricetta
proprio, che legge il vecchio e lo ricopia — e' una strada aperta anche a noi.

L'altra e' gratis: il **tavolo da fucina**.
`SmithingTransformRecipe.assemble` fa `transmuteCopy`, che porta sul risultato
l'intera patch di componenti del pezzo base, quindi la cintura passa a sei con
le ball ancora dentro senza che scriviamo una riga. E il tavolo c'e': TFC lo
tiene e ne spedisce la ricetta.

Fra le due, la fucinatura: lo stesso risultato senza codice da mantenere.

**Usare una ball evoca il Pokemon che ha dentro.** Non e' "seleziona nella
lista, poi premi R": la ball stessa e' la maniglia del suo Pokemon, e il tasto
destro su di essa fa quello che farebbe R su quel Pokemon nell'elenco. Da cui
segue che in combattimento **non si evoca da una ball tenuta in mano** — si
manda in campo dalla cintura. Chi si e' preparato male resta con quello che ha
addosso.

Il blocco e' di parte server, che e' l'unica a sapere delle battaglie: il client
muove il braccio e non succede niente. Brutto ma innocuo, perche' il lancio di
una ball lo fa il server.

**E le ball della cintura si usano con l'interfaccia che Cobblemon ha gia':**
frecce su e giu' per scegliere nell'elenco a sinistra, R per mandare in campo.
Non c'e' niente da costruire — dato che la squadra e' la cintura, quelle frecce
scorrono esattamente le ball che si hanno addosso. Chi va in campo esce
momentaneamente dalla sua ball, che resta sua e **non lascia la cintura**: il
posto e' occupato da lui anche mentre e' fuori. Quando si ispeziona la cintura,
la sua icona dovrebbe mostrarsi aperta.

**Catturare non c'entra e funziona come sempre.** Una ball vuota si lancia col
tasto destro anche in mezzo a uno scontro: il blocco riguarda solo le ball
piene, cioe' l'evocare. Mandare in campo e catturare sono due gesti diversi, e
solo il primo deve passare dalla cintura.

**La ball fisica e' il magazzino, e prima del PC e' l'unico.** Questo e' il
cuore del sistema, non un dettaglio: in TFC il PC non esiste per meta' partita —
arriva nell'era elettrica e vuole il Pokemon Storage Component (sezione 5.7) —
quindi finche' non c'e' **l'unico posto dove un Pokemon puo' stare e' una ball
che tieni in mano.** Si immagazzinano i Pokemon tenendo le loro ball in una
cassa, ed e' il sistema grezzo e rustico che il pacchetto chiede.

Da cui una conseguenza tecnica: **spostare un Pokemon dentro e fuori dalla
squadra e' mettere e togliere la sua ball dalla cintura**, e non serve nessuna
macchina per farlo. Il deposito interno di Cobblemon fa da magazzino invisibile
per i Pokemon nelle ball che non si portano addosso — il giocatore non lo vede
mai come un PC, vede delle ball. Il **blocco** PC diventa quello che deve
essere: una macchina per la gestione in blocco e per l'accesso a distanza, non
il presupposto per avere piu' di sei Pokemon.

**Dove stanno i dati, e sono tre posti.** La squadra, il PC vero, e un **box
invisibile** che tiene tutti gli altri. Nient'altro: le ball sono **maniglie**
verso il box invisibile, non contenitori. Un Pokemon esiste in uno di quei tre
posti e in nessun altro, e la ball e' il modo di maneggiarlo da fuori senza
mescolare i dati.

Il box invisibile si prende con `getCustomStore(PCStore.class, chiave,
registryAccess)`, dove la chiave e' un UUID derivato da quello del giocatore: il
PC cerca col suo UUID e questo non lo trova mai. Cobblemon lo tiene in cache e
lo salva su file come gli altri. Nel PC vero finisce solo quello che il giocatore
vi deposita di proposito, ed e' quello che tiene il PC un traguardo invece di un
indice gratuito di ogni Pokemon che si ha in una cassa.

**Perche' non serializzare il Pokemon dentro la ball**, che sarebbe piu'
intuitivo. Un'obiezione che sembrava decisiva non lo e': i data fixer di
Cobblemon stanno dentro `Pokemon.CODEC`
(`CobblemonSchemas.wrapCodec(CODEC, POKEMON)` nell'inizializzatore statico),
quindi un Pokemon dentro un item migrerebbe fra versioni come uno in un
deposito. Quella che resta invece e' pesante: oggi una ball copiata e' una
**maniglia morta**, perche' il Pokemon ricorda quale handle lo possiede e rifiuta
le altre; con i dati dentro l'item, una copia e' un **secondo Pokemon vero**, e
in creativa si copia col tasto centrale. Si passerebbe da "impossibile per
costruzione" a "un clic". E le battaglie vorrebbero comunque un deposito, quindi
la materializzazione cintura-squadra resterebbe: lo stesso lavoro, con in piu' il
rischio.

**Ma la ball non deve contenere il Pokemon: deve puntarlo.** Se il Pokemon vive
dentro l'item, perdere l'item e' perderlo — in lava, in una morte in un posto
irraggiungibile, in una mod che cancella oggetti — ed e' esattamente il motivo
per cui Cobblemon tiene party e PC fuori dal mondo. La stessa esperienza si
ottiene senza il rischio facendo portare alla ball **l'UUID** del Pokemon: il
tasto destro evoca quello puntato, l'elenco a sinistra mostra le ball della
cintura nell'ordine, e il Pokemon continua a vivere nel deposito del giocatore.
Se la ball si perde, si perde una ball.

Per i Pokemon in eccesso, quelli che oggi finiscono nel PC, vale lo stesso: la
ball in tasca punta a un deposito personale in overflow, non se lo porta dentro.

**Il passaggio di mano col PC.** Il PC tiene il Pokemon, la ball lo tiene in
mano. Depositare vuol dire consegnarlo, quindi la sua ball sparisce — dalla
cintura e dall'inventario, ogni copia. Ritirarlo vuol dire riprenderlo in mano,
e la ball torna: quella vera, perche' Cobblemon ricorda con che ball l'hai
preso. Sopra i posti disponibili si esce comunque, come oggetto, ed e' il modo
di portarsi via piu' Pokemon di quanti se ne possano usare.

Senza la seconda meta' la prima sarebbe una trappola: depositi, ritiri, e ti
ritrovi un Pokemon in squadra che non hai modo di far uscire.

Il gancio sta **sull'azione e non sullo stato**, e sono i tre handler di
Cobblemon che portano l'UUID nel pacchetto: `MovePartyPokemonToPCHandler`,
`MovePCPokemonToPartyHandler` e `SwapPCPartyPokemonHandler`. Cosi' un deposito
voluto si distingue da un Pokemon che finisce nel PC per altre strade — una
cattura a cintura piena, dove la ball invece te la tieni.

Ritirando, il Pokemon riceve un **handle nuovo**: la ball vecchia e' stata
distrutta col deposito, e se una sua copia girasse ancora non deve rispondere
alla nuova.

**La ball di un Pokemon e' la sua, e nessun'altra.** Cobblemon si ricorda con
che ball l'hai preso (`Pokemon.getCaughtBall()`): quella e' la sua per sempre,
e non si richiama un Pokemon dentro una ball diversa. Quindi non esiste il
gesto "ball vuota addosso al mio Pokemon": la ball giusta nasce alla cattura, e
se la perdi la riprendi ritirandolo dal PC.

**Appena catturato, il Pokemon e' un oggetto da raccogliere.** La ball cade dove
e' caduto il lancio, ed e' il giocatore ad andarsela a prendere: e' il gesto dei
giochi, e non c'e' fretta, perche' una ball piena non scade e non la intacca
niente. Raccogliendola finisce sulla cintura da se' se c'e' posto, altrimenti
nell'inventario. Non esiste il caso in cui un Pokemon viva solo dentro una
struttura invisibile.

**Portarsi un Pokemon in tasca invece che nel PC.** Oggi, a squadra piena, il
Pokemon catturato finisce nel PC. Con la cintura piena vorremmo che restasse
**dentro la ball, nell'inventario**, e che il PC diventasse una scelta e non un
automatismo.

**Da fare, in coda alla cintura.**

- **L'icona della cintura**, disegnata a mano: quelle di adesso sono segnaposto,
  le borchie si pestano con la fibbia e la fascia si legge come un mattone.
- **I cerchietti colorati** sull'icona, uno per ball portata e nell'ordine in cui
  stanno — great, poke, gigaton da' blu, rosso, nero. **Aspettano l'icona**: i
  cerchietti si disegnano sopra, e sopra un segnaposto non si capisce se stanno
  bene. Il colore non lo scriviamo a mano, lo si ricava dalla texture di ogni
  ball, cosi' vale anche per quelle che aggiungeremo.
- **L'elenco a sinistra a squadra vuota**: Cobblemon esce subito dal disegno se
  non c'e' nessun Pokemon (`PartyOverlay.render`, offset 150), quindi il posto
  vuoto non si vede. Si puo' forzare, ma vuol dire tenere in piedi un pezzo di
  HUD che il suo autore ha deciso di non mostrare.

**Il collegamento remoto al PC**, era elettrica LV: la cintura avanzata apre il
PC con una scorciatoia, configurabile, e da li' si fanno le stesse cose che si
farebbero davanti al PC — depositare e ritirare passando dalla cintura. Niente
di piu' esotico: e' il PC di Cobblemon, aperto da lontano. E' anche la valvola
di sfogo per il punto sopra: prima di averla, i Pokemon in eccesso te li porti
addosso; dopo, li spedisci da qualunque posto.

Si costruisce coi componenti di Applied Energistics (sezione 9): un terminale
senza fili e' esattamente quello che è.

**Cosa ho verificato essere agganciabile**

- `PartyStore` tiene una lista fissa di slot con `size()`, `get(int)`,
  `set(int, Pokemon)`, `occupied()` e `swap(int, int)`. **La dimensione non va
  toccata**: resta sei internamente, altrimenti i salvataggi esistenti si
  rompono. Il limite della cintura va messo come cancello su `add` e sul
  disegno dell'overlay, non sulla struttura.

**Come il cancello e' fatto davvero.** Tre pezzi, e il terzo e' quello che tiene
in piedi gli altri due.

1. `PlayerPartyStore.add` risponde no oltre quanto la cintura concede, e chi
   chiama fa quello che ha sempre fatto a squadra piena: manda nel PC. A
   giocatore offline non ci mettiamo in mezzo — uno scambio che arriva mentre
   non c'e' nessuno non ha una cintura da guardare.
2. L'elenco a sinistra mostra solo i posti concessi. L'overlay chiede a
   `ClientParty.getSlots()` la lista degli slot piu' volte — una per l'altezza
   complessiva, una per disegnarli — e rispondendo a tutte con la lista
   accorciata il riquadro si stringe da se' e resta centrato. Mai piu' corta di
   quanti Pokemon ci sono davvero: nel momento in cui ti togli la cintura e' il
   caso di vederli.
3. **L'allineamento, nei momenti in cui qualcosa cambia.** E' il pezzo che
   regge tutto — senza di lui non c'e' modo di spostare un Pokemon dentro e
   fuori dalla squadra prima del PC — quindi va scritto perche' sia sicuro, non
   perche' sia breve. Le tre operazioni di Cobblemon non sono intercambiabili:
   `set` su un posto **occupato** toglie il precedente occupante *da questo
   store e da nessun altro*, cioe' lo lascia senza coordinate e quindi in nessun
   deposito; `remove` lascia senza coordinate e va sempre seguito da un `add`
   che riesca; `swap` e' l'unico che non distrugge niente. Da cui i tre passaggi
   in quest'ordine: **prima escono** quelli di cui non si porta la ball (con
   rientro immediato se il deposito rifiuta), **poi entrano** quelli di cui la
   si porta, sempre in un posto libero, **poi si ordina** solo con `swap`. Cosi'
   `set` non ha mai niente da distruggere.
 Chi non e' su una
   ball addosso torna nel PC, chi lo e' va al posto della sua ball. E i momenti
   sono tre: la cintura che si mette o si toglie (`CurioChangeEvent` sullo slot
   `belt`), una ball che entra o esce, e l'apertura del PC. **Non a tempo**: un
   controllo al secondo su ogni giocatore e' il modo piu' sicuro di rovinare un
   server, e fra un gesto e l'altro non c'e' niente da controllare.

Senza il punto 3 i primi due si contraddicono: il cancello manda nel PC, e
niente riporterebbe indietro il Pokemon quando la ball torna sulla cintura.

**Due trappole nei depositi di Cobblemon, trovate a caro prezzo.** Nessuna delle
due si scopre leggendo il codice, perche' sono presupposti e non righe.

1. **Un `PCStore` appena costruito non ha box**, e `getFirstAvailablePosition()`
   le scorre per trovare un posto libero: con zero box risponde sempre "nessun
   posto", quindi `add` **falisce sempre**. Il PC vero non ne soffre perche' la
   fabbrica lo costruisce con una funzione propria (`pcConstructor`) che le
   crea; un deposito chiesto con `getCustomStore` passa invece dal costruttore
   riflessivo — `getConstructor(UUID.class).newInstance(uuid)` — e quella
   funzione non viene chiamata. **Un deposito custom non e' un deposito
   pronto**: le box se le deve creare da se', e conviene farle crescere anche a
   richiesta, perche' un salvataggio fatto mentre il deposito era vuoto le
   riporta a zero.

   Il sintomo era irriconoscibile: la cintura sembrava non contare niente. In
   realta' l'allineamento contava e sfrattava giusto, ma lo sfrattato non aveva
   dove andare e rimbalzava in squadra. L'unica ragione per cui non si e' perso
   nessun Pokemon e' il ripiego che rimette in squadra chi il deposito rifiuta,
   scritto per un caso che sembrava teorico.

2. **`PokemonStore.get(UUID)` passa da un indice interno**, e quell'indice puo'
   restare indietro rispetto al contenuto. Per cercare un Pokemon in un
   deposito conviene **scorrerlo**: una risposta sbagliata li' significa
   duplicare un Pokemon o perderlo, e non vale il tempo che si risparmia.

**E una lezione sul come si cercano questi difetti.** Il primo l'ho inseguito
per quattro tornate correggendo sintomi — ogni correzione ne mascherava un
altro — e si e' arreso solo a tre righe di log messe nei punti giusti: quanti
Pokemon sono addosso, quanti in squadra, quanti nel deposito. La riga
"addosso 3, in squadra 4, nel box 0" seguita da uno sfratto, e alla riga dopo
la squadra ancora 4 e il box ancora 0, dice in due secondi quello che nessuna
rilettura del codice avrebbe detto — perche' il difetto non era nel nostro
codice.

**Un Pokemon fuori dalla sua ball non si tocca.** Se sta nel mondo ce l'hai
messo tu, e spedirlo nel PC perche' la sua ball ha cambiato posto svuoterebbe di
senso l'averlo fuori. L'unico posto dove si richiama d'ufficio e' il PC: aprendo
il deposito prima rientrano tutti nelle proprie ball, poi si allinea — ed e'
anche il solo momento in cui serve sapere con certezza dove sta ciascuno.

**In combattimento la squadra e' quella registrata all'inizio.** Togliersi la
cintura a meta' scontro non cambia le carte in tavola: finche' la battaglia e'
in corso l'allineamento non fa niente.

**Quanti posti, esattamente.** Una cintura concede i suoi: tre col cuoio, sei
con quella completa. Senza cintura ne vale **uno**, e quell'uno non si toglie
mai — che tu indossi la ball o niente.

Il pavimento a uno non e' una gentilezza, e' quello che tiene il sistema
leggibile. A zero posti chi comincia la partita non puo' tenere il primo Pokemon
che prende, e l'elenco a sinistra vuoto non spiega perche': si legge come un
difetto, non come una regola. Con un posto sempre disponibile la progressione
resta la stessa — uno, tre, sei — e il primo Pokemon ce l'hai.

Conseguenza sull'allineamento: senza niente addosso non c'e' nulla a cui
allinearsi, e la squadra non si tocca. Il limite lo tiene il cancello su `add`,
che e' il posto giusto.
- `PlayerPartyStore.add(Pokemon)` restituisce un booleano e c'e'
  `getOverflowPC(RegistryAccess)`: **e' li' che vive l'automatismo del PC**, ed
  e' il punto in cui inserirsi per far restare il Pokemon nella ball.
- `PartyOverlay` e' la classe dell'elenco a sinistra, quindi e' quella da
  mixinare per mostrare solo gli spazi che la cintura concede invece di sei
  segnaposto da subito.
- `PokeBallItem` non gestisce i tooltip ne' l'uso in battaglia da se': il
  rifiuto in combattimento va messo sull'uso dell'item controllando se il
  giocatore e' in battaglia e da dove viene lo stack.

**La scritta "Slot: belt" si toglie alle ball, non alle cinture.** Curios la
aggiunge da se' a qualunque oggetto indossabile: sotto una cintura ci sta, e'
la sua unica ragione di esistere, ma sotto cinquanta ball e' rumore.
`ICurio.getSlotsTooltip(List, TooltipContext)` e' un metodo default:
restituendo la lista invariata la riga non compare. La capability si attacca
agli item di Cobblemon con `CuriosCapability.ITEM`, che e' una `ItemCapability`
di NeoForge, quindi si registra sui loro item senza toccare la loro mod.

**Come si maneggia nell'inventario: come un sacco.** Cintura sul cursore, tasto
destro su una ball e la infila; tasto destro su uno slot vuoto e ne esce
l'ultima. Con la cintura addosso, shift + tasto destro con una ball in mano la
infila senza aprire niente — e se la cintura e' piena la ball si lancia, come
sempre. Un posto una ball: i posti sono gli slot della squadra, non un
magazzino.

**Una ball piena non si perde.** A terra non scade (`setUnlimitedLifetime`) e
non la intacca niente (`setInvulnerable`). Il Pokemon vive comunque nel
deposito, ma la ball e' l'unico modo di richiamarlo senza passare dal PC, e
vederla bruciare in una colata di lava sarebbe una punizione sproporzionata.
Resta la rete di sicurezza: ritirando dal PC la ball si rimaterializza, perche'
Cobblemon salva in `Pokemon.getCaughtBall()` con che ball l'hai preso.

**Contro i doppioni: la ball ha un'identita'.** Se una ball piena venisse
copiata — creativa, una mod che duplica oggetti — ci sarebbero due maniglie per
una creatura. Quindi la ball porta, oltre all'UUID del Pokemon, un `handle`
suo, e il Pokemon si ricorda quale handle lo possiede nei suoi dati
persistenti: una copia non risponde piu'. La verifica passa se il Pokemon non
ha padrone scritto, cosi' i Pokemon presi prima di questa modifica continuano a
funzionare.

**Il tooltip: cache per tutto, tranne il livello.** Specie, soprannome, sesso e
shiny non cambiano da soli, quindi stanno sulla ball e si disegnano senza
chiedere niente a nessuno. Il livello invece cresce mentre il Pokemon e' fuori,
e una ball nello zaino non se ne accorge: quello in cache e' l'ultimo visto, e
quando il cursore si posa sulla ball parte una domanda al server che risponde
col livello vero. Una richiesta ogni due secondi per Pokemon, un intero per
risposta.

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

**Cosa serve**: `MenuType`, storage con filtri per slot, schermata client con
linguette, sincronizzazione, persistenza sull'oggetto Curios, più la grafica
dell'interfaccia. Il limite per categoria, che è ciò che la rende una borsa Pokémon, è
invece la parte facile.

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

### 6.6 Il pascolo, a ball

Il pasture block di Cobblemon e' una **finestra sul PC**: al click apre la
schermata del PC in modalita' pascolo, e da li' si trascinano dentro i Pokemon,
che intanto continuano ad abitare nel PC. Questo era il terzo posto — dopo la
squadra e il PC — in cui una ball fisica non contava niente, e per la stessa
ragione degli altri due: senza PC non lo usi.

**Da noi e' una cesta.** Tasto destro sul blocco e si aprono **sedici caselle**,
due file da otto, che accettano solo ball con un Pokemon dentro. Ci si mettono
le ball che si hanno addosso, e chi ha la sua ball appesa qui sta al pascolo; si
ritira la ball e il Pokemon rientra, sempre sotto forma di ball, nell'inventario.
Nessun PC, nessun trascinamento da una box. Sedici e' il limite di Cobblemon
(`defaultPasturedPokemonLimit`), che e' anche quanti Pokemon il pascolo sa
tenere legati.

**Chi pascola non si decide al clic, si riconcilia dopo.** A fine clic si
confronta la cesta coi legami del pascolo e si sistema la differenza: chi ha
perso la sua ball rientra, chi l'ha appena appesa esce. E' la stessa scelta
fatta per la cintura e per la stessa ragione: un clic in una finestra e' tre
mosse — prendi, sposta, deponi — e provare a reagire a ognuna vuol dire
inseguire stati di mezzo.

**Sotto, quello che si sposta e' la residenza.** Il Pokemon passa dal box
invisibile a un terzo deposito, il **PC del pascolo**, che porta l'UUID del
giocatore come gli altri due — deve, altrimenti i Pokemon che ci vivono non
risultano di nessuno e non si possono nemmeno accarezzare — ed e' separato
perche' la fabbrica di Cobblemon tiene file e cache per *classe* di deposito.
Tolta la ball dalla cesta si torna nel box.

**Rotto il blocco**, le ball cadono dove stava e tutti i Pokemon rientrano con
l'effetto del pascolo, quello con cui ne escono: non il richiamo verso il
giocatore, perche' non sono tornati in tasca a nessuno — e' il recinto che e'
sparito. Il padrone a cui tornano e' quello del legame e non quello del blocco:
a un pascolo ci puo' appendere una ball chiunque passi.

**Perche' si cambia il pascolo che c'e' invece di farne uno nostro.** La block
entity di Cobblemon e' `final` e non si estende; NeoForge permetterebbe a un
blocco nostro di ospitarla (`BlockEntityTypeAddBlocksEvent`), ma il suo tick
chiama `togglePastureOn`, che fa un cast secco a `PastureBlock` — un blocco
nostro esploderebbe venti volte al secondo. Quello che si riusa, e vale la
pena, e' tutto il resto: il vagabondaggio dentro i confini, il controllo
periodico dei legami, il rilascio alla rottura. Il cardine e' uno solo: il
legame ritrova il Pokemon con `getPC(pcId).get(pokemonId)`, e quella ricerca
deve ripiegare sul deposito del pascolo, o entro due secondi ogni Pokemon
appeso risulta scomparso.

**Il pascolo si accende quando ha ball appese.** Cobblemon usa quello stato per
dire "c'e' una schermata aperta"; a cesta chiusa non vorrebbe dire niente, e
acceso-con-qualcuno-dentro e' l'unico segno che il modello ha gia'.

**Resta aperta la specializzazione**, che e' la cosa che rende un pascolo
diverso da un parcheggio: allenare EV, alzare l'amicizia, raccogliere quello
che il Pokemon droppa. Il disegno sta nella sezione 6.7.

**Resta aperto il grado industriale.** Il pascolo collegato al PC — quello che
ti fa sfogliare tutte le box invece delle sedici ball che ti porti dietro — e'
esattamente il pascolo di Cobblemon come era: non c'e' da costruirlo, c'e' da
rimetterlo come secondo blocco, dietro al PC.

### 6.7 Pascoli specializzati — un blocco che dichiara un'area

Un pascolo che tiene i Pokemon parcheggiati e' un magazzino con l'erba. Quello
che lo rende un posto e' che li' dentro succeda qualcosa, e le tre cose che
vogliamo far succedere — allenare, affezionare, raccogliere — hanno tutte la
stessa forma: **un blocco dichiara un'area, e quello che c'e' nell'area decide
quanto va bene**. Uno scheletro solo, tre letture diverse del contenuto. Non
tre macchine con tre interfacce: tre modi di leggere una stanza che il
giocatore ha costruito.

**Allenamento EV: i sacchi da boxe, sei.** Uno per statistica, e non c'e' un
sacco generico: allenare vuol dire scegliere, e gli EV hanno un tetto che si
spende una volta sola — **252** per statistica, **510** in totale. Ogni sacco
porta scritta addosso la sua, con la fascia colorata che i giochi usano per
disegnare quella statistica. Il riferimento e' il Fantallenamento, che pero' e'
di **X e Y** e non di Bianco e Nero.

Sono blocchi che si **consumano con l'uso**: ad ogni tick una percentuale che
si deteriorino, e il deterioramento si vede a stadi, come l'incudine di vanilla
che passa per scheggiata e rovinata prima di rompersi. Da una parte un budget
che si esaurisce, dall'altra attrezzi che si consumano: due cose finite che si
guardano, che e' il contrario di una macchina che gira per sempre.

**Cosa si brucia per allenare, e cosa si paga per costruire.** Ci sono tre
famiglie di oggetti candidate, e guardando cosa sono davvero in Cobblemon si
dispongono da se' su tre ruoli diversi invece di litigare per lo stesso:

| Famiglia | Cosa sono in Cobblemon | Ruolo |
|---|---|---|
| **Power item** — weight, bracer, belt, lens, band, anklet | **craftabili** (l'anklet: foglia di menta verde, calcestruzzo, diamanti) e sono **esattamente sei, uno per statistica** | il **cancello**: e' l'oggetto che va nella ricetta del sacco, uno per sacco. Nei giochi sono oggetti da tenere addosso che aumentano gli EV guadagnati, quindi e' anche il ruolo giusto |
| **Succo e polvere di bacca** | `berry_juice` si fa con **due bacche e una scodella** | il **carburante ordinario**: costa poco, rende poco. La *polvere* di bacca non esiste ancora: la fa il Berry Crush, che sta nel catalogo dei minigiochi (sezione 8.2) — una dipendenza vera fra le due mod |
| **Oggetti X** — attack, defence, special attack, special defence, speed | **craftabili nella pentola**, quattro per volta, da un fiore e una bacca | il **carburante buono**, che accelera il tick. Attenzione: sono **cinque**, per gli HP non esiste un oggetto X, quindi quel sacco va alimentato d'altro |

Le **vitamine** (HP Up, Protein, Iron, Calcium, Zinc, Carbos) restano fuori, e
per un motivo misurato: in Cobblemon **non hanno ricetta**, si trovano soltanto.
Gatare sei sacchi dietro sei oggetti che non si possono fabbricare vorrebbe dire
che la palestra si costruisce se si e' fortunati.

**Dei sacchi ci sono gia' i blocchi**, diciotto: sei statistiche per tre stadi.
Sono **alti due metri** e si piazzano **solo appesi** — vogliono un soffitto
sopra e il vuoto sotto, e il pezzo che si piazza e' quello alto, dove clicchi
e' dove sta l'attacco, mentre il basso lo mette lui e pende due pixel sopra il
pavimento. Rompendo l'uno cade l'altro, e la tabella di loot e' solo del pezzo
basso, cosi' da qualunque parte lo si rompa ne cade uno e uno solo. La tela e'
la **juta di TFC** (`tfc:burlap_cloth`), riempita di sabbia e appesa a una
corda.

**Il colore non sta nelle texture.** Diciotto sacchi con la fascia colorata
cotta dentro volevano ventuna texture; cosi' invece ne bastano **quattro** —
tre basi, una per stadio d'usura, piu' una fascia sola in bianco e nero — e la
statistica la mette il **tint** a schermo, moltiplicando il colore sulla trama.
Moltiplicare e non coprire e' la parte che conta: la tela dipinta resta tela
invece di diventare vernice. I modelli scendono da trentasei a **sei**, perche'
tutte e sei le statistiche dello stesso stadio usano lo stesso modello.

Gli stadi di usura, quelli, vanno portati **nella forma** e non solo nella
texture: un sacco sfondato deve sgonfiarsi, non solo mostrare i buchi. I tre
modelli sono nella todolist, in Blockbench.

**Amicizia: la scenografia.****Amicizia: la scenografia.** Non attrezzi ma arredo, da costruire: sale da te',
terme, un giardino. Item nostri, dove contano **quantita' e posizione** dentro
l'area, e l'amicizia sale ad ogni tick di quanto la stanza se lo merita. E' il
modo rustico di fare quello che nei giochi si fa camminando col Pokemon
appresso: qui non cammini tu, gli costruisci un posto dove stare bene.

**Raccolto: l'habitat.** La resa dei drop cresce con **quante condizioni di
spawn di quel Pokemon l'area soddisfa**. Per un Magmar: che sia in un bioma
caldo, che ci sia lava, che ci sia basalto, che ci siano dispenser di snack. E
questa non e' una scala da inventare, perche' **Cobblemon la parla gia'**: ogni
spawn ha `condition` e `anticondition`, e su millecinquecentoquarantaquattro
file di spawn il vocabolario e' questo — `biomes`, `neededNearbyBlocks`,
`neededBaseBlocks`, `fluid`, `minSkyLight`/`maxSkyLight`, `canSeeSky`,
`minY`/`maxY`, `structures`, `timeRange`, `isRaining`, `moonPhase`,
`isSlimeChunk`, `isPokeSnack`.

Due cose da notare la' dentro. La prima: **`isPokeSnack` esiste gia'** — e
Magnemite ha persino un `weightMultiplier` che gli raddoppia il peso quando c'e'
uno snack in giro. Quindi il dispenser di pokemelle non e' una nostra
invenzione, e' una condizione che Cobblemon conosce, e `weightMultiplier` e' il
precedente di "condizione soddisfatta, resa migliore" scritto da loro.

La seconda: quelle condizioni non sono tutte della stessa pasta, e vanno lette
in tre famiglie.

| Famiglia | Condizioni | Che ruolo ha nel conteggio |
|---|---|---|
| **Si costruiscono** | `neededNearbyBlocks`, `neededBaseBlocks`, `fluid`, luce e `canSeeSky`, `isPokeSnack` | il grosso del punteggio: e' il lavoro del giocatore |
| **Si scelgono** | `biomes`, `minY`/`maxY`, `structures`, `isSlimeChunk` | dove metti il pascolo, deciso una volta e per sempre |
| **Passano** | `timeRange`, `isRaining`, `isThundering`, `moonPhase` | non si costruiscono: finestre in cui la resa sale da se' |

Le prime due famiglie fanno il punteggio dell'area, la terza e' il momento
buono. Da cui, gratis, un habitat perfetto non e' costruibile per ogni specie
nello stesso posto: chi vuole mungere un Magmar si scava una fornace, chi vuole
un Magnemite gli fa una miniera.

**Cosa resta da decidere:** i numeri di tutto — quanto grande e' l'area, quanto
costa un tick, la probabilita' di rottura dei sacchi, quanti EV per sacco, di
quanto sale l'amicizia e quanto rende un habitat pieno contro uno vuoto — e se
i tre pascoli sono tre blocchi o lo stesso blocco che legge l'area e capisce da
se' cosa gli e' stato costruito attorno.

## 7. Alpha Pokémon e leggendari

- Gli **Alpha Pokémon non sono legati al tier tecnologico** del giocatore — spawnano secondo le meccaniche native di Cobblemon, senza sistemi di drop/materiali ibridi custom
- I **leggendari/mitici** sono l'eccezione: gestiti tramite l'addon **Myths and Legends**, con i key item che ne attivano lo spawn posizionati in **Nuclear Age**
- Trading e PvP tra giocatori restano **invariati** rispetto a Cobblemon vanilla

## 8. Quest (FTB Quests)

TerraFirmaGreg – New Horizons usa FTB Quests come sistema di quest. Le quest Pokefirmacraft vanno pensate come un **capitolo dedicato** agganciato ai traguardi già esistenti nel quest book di TFG (es. "hai raggiunto Bronze Age" sblocca il capitolo Cobblemon corrispondente).

**Struttura del capitolo: non ancora definita — da progettare in una fase successiva.**

## 8.0 Megapietre — appunti per quando ci arriveremo

Niente di implementato, sono note da non perdere.

**I geodi si fanno con un file.** TFC non ha una sua feature: usa
`minecraft:geode` di vanilla con i provider tutti aperti — guscio, strato
intermedio, strato interno, quello alternativo, i riempimenti. Il suo geode e'
basalto indurito fuori, quarzite dentro e ametista come strato alternativo.
Quindi un geode di megapietre e' un `configured_feature` e un `placed_feature`,
zero codice: strato interno il minerale di megapietre, guscio la roccia
indurita del posto.

**Il minerale di megapietre non ha tier.** Un blocco rende **tre o quattro
megapietre pescate a caso fra tutte**, senza distinzione: la scarsita' e' il
gate, non la profondita' o il metallo del piccone. Cosi' una megapietra non si
"punta", si trova.

**L'Arma Suprema di AZ, era nucleare.** L'idea e' trattarla come un reattore:
la si carica, si innesca la reazione e ne esce **ogni singola megapietra** in
una volta. E' il modo di chiudere il capitolo megapietre senza farne un
grinding infinito, e sta bene nell'era nucleare perche' e' letteralmente la
stessa meccanica di un reattore — combustibile, innesco, e qualcosa che puo'
andare male.

## 8.1 Scavo archeologico — numeri da tarare in gioco

Il minigioco funziona, i numeri sono messi a caso e vanno provati con le mani.

- [ ] `DigSite.DURABILITY` = 60. Con la griglia 9x9 e il martello a 7 non si
      arriva a ripulire tutto: e' voluto, il sito va letto e non arato.
- [ ] `DigSiteBlockEntity.TREASURES` = 2 tesori per sito, pescati due volte
      dalla stessa loot table. Tirato fuori l'ultimo, il sito si sfalda.
- [ ] `DigSiteBlockEntity.DEFAULT_LOOT`: una tabella sola per tutti i siti.
      Va scelta dalla struttura che piazza il blocco, che ne ha diciotto fra
      common, uncommon e rare.
- [ ] Lo sconto del tier: `DigTool.siteCost` legge la durabilita' massima e la
      divide per 700, fermandosi a due. Era proporzionale e senza tetto, e un
      martello d'acciaio scendeva da sette a uno: apriva un geode in trenta
      colpi invece di quattro. Resta un proxy, se i metalli di TFC non scalano
      lineari serve una tabella esplicita.
- [ ] Il piccone: croce di cinque celle, tre di sito a colpo, e sui bracci
      sfonda sette volte su dieci.
- [ ] **Rendere i tipi di sito data-driven.** Oggi `SiteKind` ha l'ordine degli
      strati e la penalita' del martello scritti nell'enum, `DigSkin` ha i path
      delle texture di TFC scritti a mano, e `ModDig` genera i cinquantaquattro
      blocchi in un ciclo Java. Un data registry `site_type` — id, varianti,
      tre texture di strato, ordine, penalita', loot table — li tirerebbe fuori
      tutti. Serve a noi (aggiungere un sito smette di essere una modifica
      Java) e rende banale, se un giorno lo vogliamo, staccare il minigioco
      come addon a se': il pacchetto `dig` sono 1942 righe in sedici classi e
      **non importa una sola classe di TFC o di Cobblemon**, l'accoppiamento e'
      tutto nei dati. TFC diventerebbe un datapack e vanilla un altro.
- [ ] Le soglie del rumore stanno su `SiteKind`: 0.55 / 0.78 per sedimento e
      pietra, 0.5 / 1.01 per il cristallo. Da vedere in mano.
- [ ] L'ordine degli strati e' sul tipo di sito: sciolto va pulviscolo, calce,
      roccia; la pietra viva al contrario; il cristallo sono due strati di
      cristallo e basta, per cui la spazzola non ci trova niente da fare per
      costruzione e non per una regola scritta a parte.
- [ ] Sette tipi di cristallo (ametista, diamante, smeraldo, lapis, e le tre
      tumblestone) con la stessa loot table. Se vale la pena differenziarle,
      basta una tabella per tipo.
- [ ] Le probabilita' dei colpi: martello 80% di sfondare le otto celle
      intorno (20% le incrina), 60% sulle quattro punte del diamante;
      scalpello 30% di prendere anche la cella sotto. Sul pulviscolo il ferro
      non fa presa: il martello dimezza, lo scalpello riesce solo a incrinare,
      e per la sabbia c'e' la spazzola.
- [ ] `SiteKind.CRYSTAL.hammerPenalty` = 2: nel cristallo il martello consuma
      il doppio, quattordici a colpo, quindi conviene lo scalpello ma il
      martello resta usabile.
- [ ] `SiteKind.rollCrystal` = 4% — quanto raro e' un sito di cristallo.

**La worldgen dei siti: frequenti come in Cobblemon base.** E' il riferimento
giusto, perche' e' quello con cui la loot table archeologica e' stata bilanciata.
Ma c'e' un problema piu' grosso sotto: **i monumenti di Cobblemon non si
generano**, il generatore di terreno di TerraFirmaCraft li ha rimossi. Sono loro
che piazzano i blocchi sospetti, quindi finche' non tornano non c'e' niente da
scavare. Rimetterli in piedi viene prima di tarare qualunque numero.
- [ ] La griglia e' 9x9 in una costante sola (`DigSite.SIZE`), quindi cambiarla
      e' una riga; la finestra pero' e' dimensionata a mano in `DigLayout`.

## 8.2 Minigiochi e meccaniche dai giochi — futuro prossimo

Niente di implementato. Qui sta la lista, con quello che ho verificato esistere
come appiglio, perche' la differenza fra un'idea e un lavoro fattibile e'
sapere su cosa si appoggia.

### Il catalogo sta in un file a parte

L'elenco largo — trecentosessanta righe — e'
`pokemon_minigiochi_mod_minecraft.md`, che raccoglie le attivita' dell'universo
Pokemon reinterpretabili come minigiochi. **Quella non e' roba di TFCobblemon**:
e' una mod compagna, che si appoggia su questa senza starci dentro. Qui restano
i verdetti e gli appigli; li' sta il catalogo.

Il file porta con se' anche i criteri, e sono la parte che vale piu' della
lista: dentro gare, puzzle, precisione, tempismo, raccolta a tempo, sport,
cucina interattiva, percorsi, arcade e multiplayer non da combattimento; fuori
i combattimenti Pokemon, le Battle Facility, le basi segrete e tutto quello che
non ha un vero giro da minigioco. E una regola sopra tutte: si reinterpretano
le **meccaniche**, senza riprodurre asset originali.

Quello che il catalogo aggiunge e che qui non c'era: il **Pokeathlon** coi suoi
dieci eventi, i **Kids Club** di Stadium e i minigiochi di Stadium 2, il
**Pokemon-Amie**, il **Dream World**, le attrazioni dei due **PokePark**, il
**Musical/PokeStar**, il **Game Corner**, e il **Poke Transfer**.

Due cose da segnare, perche' non sono minigiochi e sono piu' grosse di cosi'.

**Il Poke Transfer ha una feature maggiore attaccata.** Il minigioco — bersagli
che escono dall'erba e si catturano a tempo — e' il rito di passaggio di due
cose molto piu' ambiziose: **importare Pokemon da un salvataggio 3DS reale**
(moddato), copiandoli e non spostandoli, e **spostare Pokemon fra due mondi
Minecraft** attraverso un server centrale o peer to peer. Sono progetti a se',
e vanno valutati come tali.

**Il Berry Crush fa la polvere di bacca**, che e' il carburante ordinario dei
sacchi da boxe (sezione 6.7) e che in Cobblemon non esiste. E' l'unico punto in
cui le due mod si toccano davvero: se la palestra vuole quella polvere, la
polvere la fa un minigioco che sta di la'.

Un solo disaccordo da tenere a mente: il catalogo elenca il **Voltorb Flip**
sotto Game Corner, e qui sotto e' bocciato. Vale la bocciatura finche' non la
si cambia — il catalogo raccoglie i candidati, i verdetti stanno qui.

### Quello che vogliamo

**Le passeggiate tra gli ultravarchi.** Una dimensione infinita in cui il
Pokemon cavalcabile corre dritto e non si puo' fermare, con anelli da
attraversare e percorsi a ostacoli, come in Ultrasole e Ultraluna, e in fondo
gli ultravarchi e le ultracreature.

L'appiglio c'e' e non e' poco: **Cobblemon 1.8 ha un sistema di cavalcature
completo**. `data/cobblemon/ride_settings/` contiene **tredici stili di guida**
— bird, boat, burst, dolphin, glider, helicopter, horse, hover, jet, minekart,
rocket, submarine, vehicle — ognuno con espressioni Molang su velocita',
accelerazione, maneggevolezza e stamina legate alle **ride stats** del
Pokemon, che si alzano dandogli da mangiare. Ci sono la stamina, la quota
massima, lo smontaggio in volo. Quindi la corsa negli ultravarchi non parte da
zero: parte da uno stile di guida nostro (`rocket` e' il candidato) su una
dimensione nostra.

**Le coccole.** Accarezzare i Pokemon, lanciargli la palla e farsela riportare,
il picnic insieme, come in Scarlatto e Violetto. E' la parte che da' un motivo
a tenersi un Pokemon fuori dalla ball che non sia combattere.

**Le battaglie impersonando il Pokemon**, o comunque nello stile di Legends
Z-A: tempo reale, schivate, posizionamento, invece dei turni.

**Il pinball.** Pokemon Pinball, con la ball che rimbalza e cattura.

### Quello che propongo io, e cosa e' sopravvissuto

Li avevo scelti perche' si appoggiano su qualcosa che c'e' gia'. Tre sono
passati, quattro no, e le bocciature dicono qualcosa che vale tenere scritto.

**PASSA — La forza cinetica di Create dentro Cobblemon.** Era nato come
"frullatore di bacche" (Pokeblock e Poffin, da Rubino/Zaffiro e Diamante/Perla):
un minigioco di tempismo su una manovella, piu' vai a ritmo piu' il blocco viene
buono. L'idea vera pero' e' piu' larga di un frullatore: **oggetti di Cobblemon
mossi dall'albero motore di TFC e di Create**. Windmill, water wheel,
crankshaft, e le macchine di Create esistono e girano gia': quello che manca
sono i pezzi di Cobblemon che sappiano prendere quel movimento.

Il minigioco resta un'opzione, non la via: **tutto deve essere automatizzabile
senza**. Il tempismo a mano da' il risultato migliore, la macchina lo da'
costante — che e' lo stesso patto di tutto il resto del pacchetto.

(Correzione di un mio errore: avevo scritto che le bacche di Cobblemon sono
decorative. Non lo sono — ci si fanno gia' gli snack.)

**PASSA — Le fotografie alla Pokemon Snap.** Il pacchetto **ha gia' una mod di
fotografia**, `exposure-neoforge-1.21.1-1.9.18.jar`, quindi la macchina
fotografica esiste. Manca chi giudica lo scatto — inquadratura, distanza, posa,
rarita' — e **un NPC o un villager dedicato con cui scambiarle**: le foto
diventano merce, non un punteggio in una finestra.

**PASSA — Il Pokewalker.** Un oggetto che allena il Pokemon in base alla
distanza percorsa. In Minecraft si cammina sempre, quindi la meccanica si traduce
da se', e Curios e' gia' li' — lo stesso slot su cui e' costruita la cintura.

**RIPRESO — Voltorb Flip.** Era bocciato con la motivazione giusta per allora:
costava poco, e costare poco non e' un motivo per fare una cosa. Quello che e'
cambiato non e' il minigioco, e' la cornice — **gli arcade sono in gioco**. Il
catalogo (sezione 8.2) ha una sezione intera di Poke-Arcade e una di Game
Corner, dove il Voltorb Flip sta accanto a slot e roulette, e un puzzle che
vive dentro un mobile non deve giustificarsi come meccanica del mondo: deve
solo essere un buon puzzle dentro un mobile. E lo e' — campo minato e picross
insieme, coi moltiplicatori che ti fanno decidere quando smettere.

**BOCCIATO — La Zona Safari e la gara di scarafaggi.** Un'area a tempo con un
numero fisso di ball e' **un evento da server**, non una meccanica di gioco
singolo. Resta aperto il problema che l'aveva fatta proporre: la safari ball e
la sport ball hanno effetti di nicchia e nessun posto dove esistere. Va risolto
altrove.

**BOCCIATA — La pesca a catena.** Esiste gia' come mod a se': non si rifa'.

**BOCCIATE — Le basi segrete.** In Minecraft non hanno senso: la base e' la
casa che ti costruisci, e non serve un sistema che te ne dia una finta. Quello
che l'aveva fatta proporre — le statue che alterano gli spawn intorno — resta
valido e vive gia' nell'Habitat Block dell'era elettrica.

## 9. Punti aperti / da decidere durante lo sviluppo

**Decisioni di fondo**

- ~~Su quale pack poggiare~~ — **deciso, e non e' un problema.** Gregnautics
  Continued e' stato ritirato, ma noi non dipendiamo dal pacchetto: dipendiamo
  da un elenco di mod. Quell'elenco va tenuto scritto, e con quello ci si
  appoggia a qualunque pacchetto con la stessa filosofia — ce ne sono diversi.
  Il pacchetto e' un posto dove giocare, non una base su cui costruire.
- ~~Separazione da TFCobblemon~~ — **deciso: resta un fork, e va bene cosi'.**
  E' stata una base grossa e diverse delle sue ricette sono ancora in uso. Nome,
  modid e licenza restano i suoi.

**Progettazione**

- Redesign completo delle ricette Poké Ball in chiave Greg (sezione 5.1), con
  due cose gia' decise: l'**alluminio** e' il materiale dell'ultimo tier, e i
  **coperchi** possono andare di circuiti come tutto il resto di quell'era
- Le tre vie di produzione danno tutte due ball, quindi **non seguono la
  convenzione** scoperta col tubo a elettroni (sezione 5.5): era piu' avanzata =
  meno pezzi, migliori, resa maggiore. Da rifare coi numeri
- **Il PC e la Trainer Belt avanzata si costruiscono con i componenti di Applied
  Energistics**: celle, ME controller, terminali, wireless access point. È il
  vocabolario giusto — un armadio in rete e un terminale senza fili — e dice
  molto più della pila di lamiere di ora. **Ma solo la ricetta.** Lo storage
  resta quello di Cobblemon: le box non diventano celle, non si ripartisce
  niente, e il PC non va rifatto meccanicamente.

  **Il cancello e' un pezzo nostro, fatto col vocabolario di AE2: il Pokemon
  Storage Component**, che nel frattempo e' diventato la spina dorsale di tutta
  l'era industriale — tre gradi, e le ball si fanno attorno a lui. Il disegno
  sta in **sezione 5.7**. Tier di AE2 richiesto: **quello base**, perche' il
  cancello e' il componente, non la tecnologia attorno.
- **Rivedere le ricette degli oggetti di Cobblemon e allinearle a TFC.** Non
  le ball — quelle hanno la loro sezione — ma tutto il resto: macchine,
  held item, targhette, mensole, analizzatore di fossili. Sono **986 ricette**,
  di cui **70 chiedono metalli di vanilla** (`c:ingots/iron`, `c:ingots/gold`,
  `minecraft:iron_ingot`, il blocco di ferro, il rame). E TFC **non fornisce
  `c:ingots/iron`**: ha `c:ingots/wrought_iron`, `cast_iron` e `pig_iron`, e
  nient'altro. Quindi quelle ricette stanno in piedi solo se le riempie un'altra
  mod, e va verificato in gioco quali di loro sono davvero craftabili e quali
  sono ricette morte. Esempio vivo trovato scrivendo la healing machine:
  l'**Electirizer** vuole 4 lingotti d'oro, 4 di ferro e un blocco di redstone,
  e ce lo siamo messo come gate senza sapere se il ferro lo tagga qualcuno.
  Da tenere presente che ne stiamo gia' sovrascrivendo 66
- Ricollocazione di PC e Pasture Block secondo la sezione 4
- ~~Cosa fanno i pascoli specializzati~~ — **deciso, il disegno sta in sezione
  6.7**: un blocco dichiara un'area e quello che c'e' dentro decide la resa —
  sacchi da boxe che si consumano per gli EV, arredo da costruire per
  l'amicizia, condizioni di spawn soddisfatte per il raccolto dei drop. Restano
  aperti **i numeri** (dimensione dell'area, costo per tick, rottura dei
  sacchi, EV per sacco, resa di un habitat pieno) e se sono **tre blocchi o
  uno** che capisce da se' cosa gli hanno costruito attorno
- **Le vitamine vanno rese craftabili, in era elettrica.** In Cobblemon HP Up,
  Protein, Iron, Calcium, Zinc e Carbos **non hanno ricetta**: si trovano
  soltanto. Sono gli oggetti EV dei giochi, quindi e' giusto che esistano come
  cosa che si produce — ma tardi, perche' un integratore e' chimica, non
  cucina. Finche' non ci sono, i sacchi da boxe restano gatati sui power item
  (sezione 6.7)
- **Le ricette dei power item vanno rifatte.** Quelle di Cobblemon sono
  calcestruzzo, diamanti e foglia di menta: niente che parli la lingua di TFC.
  E adesso contano il doppio, perche' il power item e' il cancello di un sacco
  da boxe — cambiare la sua ricetta vuol dire cambiare quando si apre la
  palestra
- **I modelli dei tre stadi dei sacchi, in Blockbench.** Oggi i tre stadi
  cambiano solo texture e la forma e' la stessa: la rottura si deve vedere
  nella geometria, un sacco sfondato che si sgonfia. Il colore non c'entra, ci
  pensa il tint (sezione 6.7)
- Materiali delle due cinture (la capienza è decisa: 1 a mani nude, 3, 6)
- ~~Quali categorie ha la borsa~~ — **decise.** Si rifanno a quelle di
  Cobblemon, tenendo solo le quattro che hanno senso come tasche: **ball**,
  **bacche + mente + snack**, **medicine**, **oggetti che cambiano le statistiche**.
  Tutto il resto resta oggetto normale nell'inventario. Si sblocca **quando si
  sbloccano borse e tool belt**, con una ricetta della stessa famiglia
- Struttura dettagliata del capitolo di quest FTB (nomi, ordine, traguardi di sblocco)
- Held item non ancora assegnati singolarmente su tutti i 101 esistenti in Cobblemon (la sezione 4 copre le categorie principali per rappresentanza, non ogni singolo item)
- Dettaglio tecnico dei processi Greg di intaglio/taglio gemme da riusare per le Evolution Stone Ore (va verificato quale macchina/processo Greg esatto si applica)
- Fascia di altitudine dei 96 biomi nuovi di TFC 4 (sezione 2)

**Tecnica**

- La feature `dye` dei Golett non applica l'aspetto in Cobblemon 1.8
- Se aggiungere GregTech al runtime di sviluppo, senza il quale il lavoro delle
  sezioni 4 e 5 non è verificabile
- Verifica in gioco di quanto elencato in `PORTING.md`
