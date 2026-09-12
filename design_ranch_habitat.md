# Ranch — il punteggio di habitat (bozza)

Bozza da discutere, non decisioni prese. Serve a fissare **cosa si può
misurare davvero** (verificato sui dati veri di Cobblemon 1.8), a mettere in
fila le forme possibili, e a isolare i numeri che restano da dettare.

Il resto del disegno delle zone sta in `pokefirmacraft-design-doc.md`, sezione
5 e dintorni. Qui si guarda solo il ranch.

## 1. Che numero è, e dove entra

Il ranch raccoglie in continuo quello che il Pokémon lascerebbe morendo. Quanto
in fretta lo decidono due fattori moltiplicativi:

| Fattore | Dove sta | Stato |
|---|---|---|
| **livello** | `ZoneWorld.resa` | fatto: 1.0 a livello 1, 1.5 a livello 100 |
| **stagione e clima** | `TfcZoneWorld.stagione` / `.clima` | seam pronto, numeri da dettare |
| **habitat** | `RanchHabitat.punteggio` | **questa bozza** |

È l'unico numero delle zone che la regola dei tre giorni non determina: la
palestra ha un tetto noto (252 EV) e l'ozio pure (255 di amicizia), quindi il
passo si ricava dividendo. Il raccolto non ha tetto, e cosa lo faccia rendere
di più è una scelta di gioco.

Oggi `punteggio()` risponde `PIENO` e il ranch si comporta come prima: si può
giocare, e quando i numeri arrivano si riempiono le costanti.

## 2. Cosa Cobblemon dà davvero

Verificato smontando `neoforge-1.8.0+1.21.1.jar`, non a memoria.

**L'API.** Ogni specie ha un file in `data/<ns>/spawn_pool_world/`, con dentro
una lista di *spawn*, ognuno con la sua `condition`. In codice sono
`SpawnDetail` con `getConditions()`, e ogni `SpawningCondition` sa rispondere
`isSatisfiedBy(SpawnablePosition)`. Quindi il controllo **tutto-o-niente esiste
già**; quello che non esiste è la frazione, che è esattamente quello che ci
serve.

Costruire una `SpawnablePosition` vera è possibile ma è roba loro (serve uno
`Spawner`, una `SpawnCause`, il contesto): per un punteggio parziale conviene
comunque leggere le clausole una per una, perché la frazione va costruita a
mano in ogni caso.

**Quante ce ne sono.** Su tutta la pool di Cobblemon (841 file, 2930 spawn) e
sulla nostra (250 file, 278 spawn):

| Clausola | Cobblemon | Nostra |
|---|---|---|
| `biomes` | 2923 su 2930 | 278 su 278 |
| `minSkyLight` / `maxSkyLight` | 1935 | 235 |
| `canSeeSky` | 840 | 22 |
| `isPokeSnack` | 529 | — |
| `structures` | 302 | — |
| `minY` / `maxY` | 270 / 205 | 1 |
| `timeRange` | 269 | 50 |
| `neededNearbyBlocks` | 206 | — |
| `isRaining` | 179 | — |
| `moonPhase`, `isSlimeChunk`, `neededBaseBlocks`, `minLureLevel`… | meno di 150 in tutto | — |

Media: **3.48 spawn per specie in Cobblemon, 1.11 nei nostri file**, e sempre
**una sola condizione per spawn**.

**Il fatto scomodo.** Una condizione tipica ha **tre clausole**: il bioma, e la
coppia di luce del cielo. Un conto del tipo "quante condizioni sono
soddisfatte" quindi non ha quasi niente da contare, e il bioma da solo pesa
quanto tutto il resto messo insieme. Qualunque forma scegliamo deve partire da
qui, altrimenti misura una cosa che nei dati non c'è.

**Chi non ha spawn.** 841 file di spawn contro oltre mille specie. Le
evoluzioni ce l'hanno (Charizard e Dragonite hanno il loro), i leggendari no
(Mewtwo non ha niente). Un ranch con dentro un leggendario, senza una regola
apposta, prenderebbe il punteggio minimo per sempre — il che magari è giusto,
ma va deciso, non subìto.

## 3. Le forme possibili

### A — binario sulla voce migliore
Si prende lo spawn che va meglio, e o è soddisfatto o no: due valori,
`PIENO` o `MINIMO`.

*Pro*: onesto, usa `isSatisfiedBy` così com'è, zero pesi da inventare, e il
giocatore capisce subito (o il posto va bene, o no).
*Contro*: niente sfumature, e il salto sulla soglia rende le fattorie una
questione di sì/no. In pratica il bioma decide tutto.

### B — frazione di clausole pesate
Si guardano le clausole una per una, si somma il peso di quelle soddisfatte e
si divide per il totale di quelle presenti. Sono i `PESO_*` già scritti in
`RanchHabitat`.

*Pro*: continua, si legge bene in una GUI ("habitat 70%"), premia il giocatore
che cura il posto.
*Contro*: con tre clausole medie di cui una sempre il bioma, la frazione ha
pochi gradini veri; e ogni peso è un numero da inventare, quindi da difendere.

### C — il bioma è un cancello, il resto è bonus
Bioma giusto o si sta al minimo. Sopra al minimo, ogni altra clausola
soddisfatta (luce, ora, blocchi vicini, meteo, struttura) aggiunge un gradino
fino a `PIENO`.

*Pro*: rispecchia i dati invece di combatterli — nei file il bioma **è** la
condizione. Insegna una cosa vera del gioco: dove costruisci conta più di come
arredi. Pochi numeri da dettare: il minimo, e quanto vale un gradino.
*Contro*: un bioma sbagliato non si recupera in nessun modo. Se vogliamo che
una serra possa rimediare, questa forma dice di no.

### D — vicinanza al bioma giusto
Come C, ma invece di sì/no sul bioma si misura **quanto ci si è vicini** — TFC
dà temperatura e piovosità medie per ogni coordinata, quindi la distanza
climatica fra dove sei e dove quel Pokémon nasce è calcolabile.

*Pro*: è la forma più TFC di tutte, e rende il mondo leggibile: spostarsi di
mille blocchi a sud cambia la resa.
*Contro*: dipende da TFC, e le zone devono funzionare anche senza. Sarebbe un
override in `TfcZoneWorld`, cioè lavoro doppio: una forma base più una
migliore. Da tenere per dopo, non per adesso.

**La mia proposta: C adesso, D come miglioria dentro il ponte TFC.** C sta in
piedi da sola, usa i dati come sono, e chiede pochi numeri. D è la cosa giusta
da fare dopo, quando il ponte con TFC ha già stagione e clima dentro.

## 4. Domande aperte

1. **Quanto vale il posto sbagliato?** Zero significa un ranch che non fa
   niente e un giocatore che non sa perché. Una frazione bassa (un quarto? un
   decimo?) punisce senza rompere.
2. **Il bioma è un cancello o un peso?** È la differenza fra la forma C e la B,
   ed è la decisione grossa.
3. **Chi non ha spawn** — leggendari, e chiunque altro: minimo secco, oppure si
   guarda la specie da cui si evolve? La seconda è più gentile e costa poco.
4. **L'habitat cambia solo la velocità, o anche cosa cade?** Oggi il pezzo
   raccolto è pescato dalla tabella dei drop del Pokémon. Un habitat pieno
   potrebbe dare accesso ai drop rari, che è un premio più interessante della
   sola fretta.
5. **Si vede?** Nella finestra della zona un "habitat 70%" accanto al Pokémon
   renderebbe il numero giocabile invece che occulto. L'overlay dell'area c'è
   già, quindi il posto dove metterlo esiste.
6. **Emisfero.** TFC sa fare le stagioni opposte a nord e a sud. Vale la pena,
   o è una complicazione che nessuno noterà?

## 5. I numeri da dettare

I nomi sono già in `RanchHabitat`, così quando sono decisi si riempiono e
basta.

| Nome | Cos'è | Forma che lo usa |
|---|---|---|
| `MINIMO` | quanto rende il posto sbagliato | tutte |
| `PIENO` | il posto giusto — fissato a 1.0 | tutte |
| `PESO_BIOMA` | quanto pesa il bioma | B |
| `PESO_LUCE`, `PESO_CIELO` | luce del blocco, luce del cielo | B, C |
| `PESO_ALTEZZA` | `minY` / `maxY` | B, C |
| `PESO_BLOCCHI_VICINI`, `PESO_BLOCCHI_SOTTO` | l'arredo e il pavimento | B, C |
| `PESO_FLUIDO` | acqua, profondità | B, C |
| `PESO_ORA` | `timeRange` | B, C |
| `PESO_METEO` | pioggia e temporale | B, C |
| `PESO_LUNA`, `PESO_STRUTTURA`, `PESO_DIMENSIONE` | i rari | B, C |

E, nel ponte con TFC (`TfcZoneWorld`), gli stessi conti per l'anno:

| Nome | Cos'è |
|---|---|
| `IN_STAGIONE` / `FUORI_STAGIONE` | quanto rende un tipo nella sua stagione e in quella opposta |
| `CLIMA_GIUSTO` / `CLIMA_SBAGLIATO` | quanto conta il clima del posto |
| `NEUTRO` | il valore che hanno tutti finché non sono decisi |

## 6. Cosa c'è già in codice

- `zone/RanchHabitat.java` — le costanti qui sopra e `punteggio()`, che oggi
  risponde `PIENO`.
- `zone/ZoneKind.java` — il ranch moltiplica la resa per il punteggio, quindi
  quando il conto si accende non c'è altro da collegare.
- `zone/tfc/TfcZoneWorld.java` — il ponte: il calendario **è già acceso** (i
  tick di calendario al posto di quelli del mondo), stagione e clima sono
  metodi che rispondono `NEUTRO`.
