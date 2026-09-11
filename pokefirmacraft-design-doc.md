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
3. **L'allineamento, nei momenti in cui qualcosa cambia.** Chi non e' su una
   ball addosso torna nel PC, chi lo e' va al posto della sua ball. E i momenti
   sono tre: la cintura che si mette o si toglie (`CurioChangeEvent` sullo slot
   `belt`), una ball che entra o esce, e l'apertura del PC. **Non a tempo**: un
   controllo al secondo su ogni giocatore e' il modo piu' sicuro di rovinare un
   server, e fra un gesto e l'altro non c'e' niente da controllare.

Senza il punto 3 i primi due si contraddicono: il cancello manda nel PC, e
niente riporterebbe indietro il Pokemon quando la ball torna sulla cintura.

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

### Quello che propongo io

Scelti perche' si appoggiano su qualcosa che c'e' gia', non perche' suonano
bene.

**Il frullatore di bacche** (Pokeblock e Poffin, da Rubino/Zaffiro e
Diamante/Perla). Un minigioco di tempismo su una manovella: piu' vai a ritmo,
piu' il blocco viene buono. E' l'idea che si incastra meglio con TFC di tutte,
perche' TFC ha **giа' l'albero motore** — windmill, water wheel, crankshaft — e
un frullatore a manovella e' esattamente il suo vocabolario. E da' un senso
alle bacche di Cobblemon, che adesso sono decorative.

**Voltorb Flip** (HeartGold/SoulSilver). Il campo minato logico del Game
Corner. Costa poco: e' tutto dentro una finestra, senza stato nel mondo, e la
macchina per le finestre custom l'abbiamo giа' scritta per lo scavo.

**La Zona Safari e la gara di scarafaggi** (Rosso/Blu, Oro/Argento). Un'area a
tempo con un numero fisso di ball. E risolve un problema che abbiamo: la
safari ball e la sport ball hanno effetti di nicchia e nessun posto dove
esistere, e questo glielo darebbe.

**Le fotografie alla Pokemon Snap.** Il pacchetto **ha giа' una mod di
fotografia**, `exposure-neoforge-1.21.1-1.9.18.jar`. Quindi la macchina
fotografica esiste: manca solo chi giudica lo scatto — inquadratura, distanza,
posa, rarita' — e un committente che paghi.

**Il Pokewalker.** Un oggetto che allena il Pokemon in base alla distanza
percorsa. In Minecraft si cammina sempre, quindi la meccanica si traduce da
sola, e il pacchetto ha **Curios** (`curios-neoforge-9.5.1`), che e' lo stesso
slot che serve alla Trainer Belt della sezione 6.

**La pesca a catena** (X/Y). Catture consecutive con la stessa canna alzano le
probabilita' di shiny. Cobblemon ha giа' canna, ami ed esche, e TFC ha i suoi
tag: e' quasi solo un contatore.

**Le basi segrete con le statue** (Rubino/Zaffiro, Diamante/Perla). Le statue
che alterano gli spawn intorno alla base sono la stessa cosa che l'Habitat
Block della tabella di progressione fa nell'era elettrica: valgono come la sua
versione artigianale, molto prima.

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
  Storage Component.** In AE2 ogni processore nasce da un circuito stampato su
  un materiale — il logic processor sull'oro, il calculation sul certus, l'engineering
  sul diamante. Il nostro si stampa sulla **tumblestone base**, ed e' l'unico
  pezzo che serve: lo vuole il PC e lo vuole il grado di cintura col collegamento
  remoto. Tier di AE2 richiesto: **quello base**, perche' il cancello e' il
  componente, non la tecnologia attorno.
- Ricollocazione di PC e Pasture Block secondo la sezione 4
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
