# Animais vanilla adaptados

Além de criar animais do zero, o mod **adapta animais que já existem no Minecraft** para seguirem exatamente as mesmas regras dos seus próprios animais. O animal continua sendo o do jogo (`minecraft:camel`, por exemplo), com tudo o que o jogo já tem, e ganha os sistemas globais do mod. Todo exemplar no mundo é afetado: os que nascem naturalmente, os das vilas e os de mundos salvos antes do mod.

Animais adaptados:

| Animal | Pose de dormir (do jogo) | Ficha |
|---|---|---|
| Camelo (`minecraft:camel`) | Ajoelha (animação de sentar) | [camelo.md](../mobs/camelo.md) |
| Tatu (`minecraft:armadillo`) | Enrola na carapaça | [tatu.md](../mobs/tatu.md) |

## O que muda e o que fica

| Fica como no jogo (reaproveitado) | Passa a seguir o mod |
|---|---|
| Tipo de entidade, ID e ovo gerador | Sexo, com o macho 15% maior ([genética](genetica.md)) |
| Modelo, animações e texturas originais | Variantes de pele normal, albino e melânico, com herança |
| Sons, montaria, sela e habilidades (o dash do camelo) | Acasalamento só entre macho e fêmea |
| IA do jogo (Brain ou goals), comportamento e temperamento | Barra de informações no lugar do nome |
| Loot, spawn natural, estruturas e comida | [Regeneração](regeneracao.md) do mod (a cura aleatória do vanilla é desligada) |
| | [Sono](sono.md) à noite, com pose própria, olhos fechados e "z Z z" |
| | Coberto pelos gametests genéricos |

O comportamento do animal **não** é mudado: o camelo continua pacífico e foge quando é ferido, o tatu continua enrolando quando se assusta. Só entram os sistemas globais.

## Arquitetura

O animal vanilla não pode herdar de `ModAnimal`: o camelo, por exemplo, já herda de `AbstractHorse`. Por isso os sistemas globais ficam num componente, usado igualmente pelos animais do mod e pelos adaptados:

| Peça | Onde | Papel |
|---|---|---|
| `ModCreature` | `entity/traits/ModCreature.java` | Interface de todo animal do mod, criado ou adaptado: sexo, variante, sono, regeneração e os valores ajustáveis (com padrão). `ModCreature.of(entidade)` devolve `null` para quem não segue as regras |
| `AnimalTraits` | `entity/traits/AnimalTraits.java` | Estado e regras de todos os sistemas (genética, regeneração, cura por comida, sono, salvar/ler). O dono só chama os ganchos a partir dos seus métodos |
| `SleepSchedule` | `entity/traits/SleepSchedule.java` | Quando dormir e quando acordar, igual para IA de goals (`SleepGoal`) e de Brain |
| `ModAnimal` | `entity/genetics/ModAnimal.java` | Liga o `AnimalTraits` aos métodos de um animal criado do zero |
| Mixin do animal | `mixin/<Animal>Mixin.java` | Faz o mesmo num animal do jogo, e é a única diferença entre os dois casos |
| `AdaptedMob` / `ModMobs.ADAPTED` | `core/` | Lista dos adaptados, para os testes. Não registra nada: tipo, ovo, loot e spawn já existem |

Os dados (sexo, variante, dormindo) são sincronizados com o cliente como os de qualquer entidade: o mixin define os próprios `EntityDataAccessor` com a classe vanilla (`SynchedEntityData.defineId(Camel.class, …)`).

### Mixins do camelo

Ficam em `src/main/java/com/otavi/mobmod/mixin/`, registrados em `src/main/resources/mobmod.mixins.json`.

| Mixin | Alvo | O que faz |
|---|---|---|
| `CamelMixin` | `Camel` | Implementa `ModCreature` e injeta nos métodos vanilla: `defineSynchedData`, `addAdditionalSaveData`/`readAdditionalSaveData`, `tick`, `customServerAiStep`, `actuallyHurt`, `getAmbientSound` (silêncio dormindo), `finalizeSpawn`, `canMate`, `getBreedOffspring`, `mobInteract` (comida cura quando ferido) |
| `AbstractHorseMixin` | `AbstractHorse.aiStep` | Desliga a cura aleatória dos cavalos (1 ponto a cada ~45 s) para os adaptados, que só regeneram pelas regras do mod |
| `CamelRandomSittingMixin` | `CamelAi$RandomSitting` | O vanilla faz o camelo sentar e levantar ao acaso: dormindo, ele não levanta |

Todo gancho confere `CamelAdaptation.isAdapted(camel)`: o **camelo husk** (`minecraft:camel_husk`) herda da classe do camelo, mas fica como no jogo.

### Mixins do tatu

| Mixin | Alvo | O que faz |
|---|---|---|
| `ArmadilloMixin` | `Armadillo` | Implementa `ModCreature` e injeta em `defineSynchedData`, `addAdditionalSaveData`/`readAdditionalSaveData`, `tick`, `customServerAiStep`, `actuallyHurt`, `getAmbientSound`, `getBreedOffspring` e `mobInteract`. O tatu não sobrescreve `finalizeSpawn` nem `canMate`, então o mixin **acrescenta** esses dois métodos (chamando o vanilla por `super`) em vez de injetar neles |
| `ArmadilloAiMixin` | `ArmadilloAi` (o lambda que chama `rollOut`) | Sem perigo por perto, a IA vanilla desenrola na hora qualquer tatu enrolado: dormindo, ele fica na carapaça |
| `ArmadilloBallUpMixin` | `ArmadilloAi$ArmadilloBallUp.tick` | Com um perigo por perto, a IA vanilla espia e desenrola quando passa: dormindo, nada disso acontece |

O `ArmadilloAiMixin` mira um lambda (`lambda$static$2`), cujo nome é gerado pelo compilador. Se uma atualização do Minecraft mudar esse nome, o jogo para na inicialização com um erro de mixin, e o alvo precisa ser procurado de novo (é o lambda que chama `Armadillo.rollOut`).

### Sono em IA de Brain

Os animais do mod dormem pelo `SleepGoal`. O camelo usa Brain (`CamelAi`), então o goal não roda: o `CamelSleep` (`mob/camel/CamelSleep.java`) é chamado a cada tick do servidor depois da IA vanilla, com o mesmo `SleepSchedule`. A pose é a que o jogo já tem para o camelo descansar: ele **ajoelha** (`sitDown()`) e, ao acordar por qualquer motivo, **levanta** (`standUp()`).

O tatu faz o mesmo com o `ArmadilloSleep`. Para dormir, ele **enrola na carapaça** (`rollUp()`) e, ao acordar, desenrola (`rollOut()`). A exceção é quando um perigo o mantém enrolado: aí a IA vanilla cuida dele. Duas diferenças:
- No jogo, só o comportamento de se esconder termina de enrolar (de `ROLLING` para `SCARED`), e ele só roda com perigo. Por isso o `ArmadilloSleep` faz essa troca sozinho para quem está dormindo.
- Enrolar encolhe o hitbox, e o tatu sai do chão por um tick. Por isso, dormindo dentro da carapaça, ele não precisa estar no chão (`AnimalTraits.canSleep(false)`).

## Lado cliente

| Peça | Onde | Papel |
|---|---|---|
| `MobTraitsRenderData` / `HasMobTraits` | `client/core/` | Os dados do mod (sexo, variante, vida, sono) num objeto à parte, para caberem tanto no render state do mod quanto num render state vanilla estendido (`ModCamelRenderState extends CamelRenderState`) |
| `ModMobRenderer` | `client/core/` | Base de todos os renderers: barra de informações, dados de sono e pose. Aceita render states vanilla |
| `ModCamelRenderer` | `client/mob/camel/` | Substitui o renderer vanilla do camelo, reaproveitando a camada de sela (`CamelRenderer.createCamelSaddleLayer`) e os estados de animação (`CamelRenderer.extractAdditionalState`) |
| `ModAdultCamelModel` / `ModBabyCamelModel` | `client/mob/camel/` | Estendem os modelos vanilla: a malha do jogo (`AdultCamelModel.createBodyMesh()`, e a do filhote via access widener) com duas pálpebras a mais. As animações são as do jogo |
| `VanillaVariantTextures` / `VanillaSkinSpec` | `client/core/` | Texturas das variantes geradas em tempo real a partir das texturas do jogo |

O renderer vanilla é trocado pelo `EntityRenderers.register(EntityTypes.CAMEL, …)` do `Registrar` de sempre. O que o renderer reaproveita e que não é público fica aberto em `src/main/resources/mobmod.accesswidener`.

### Texturas geradas em tempo real

Nenhuma textura da Mojang é copiada para o repositório. A cada recarga de recursos, o `VanillaVariantTextures` lê a textura do jogo (`minecraft:textures/entity/camel/camel.png` e `camel_baby.png`) e, para cada variante:

1. recolore: o normal mantém as cores do jogo, o albino vira creme claro com olhos vermelhos e o melânico vira marrom escuro. O sombreado original é mantido;
2. pinta as pálpebras numa área livre da textura, com a cor do pelo ao lado dos olhos e um vinco um pouco mais escuro;
3. registra como textura dinâmica em `mobmod:textures/entity/camel/camel<sufixo>.png`.

Um resource pack que troque a textura do camelo muda as variantes também. Cada animal descreve a sua textura num `VanillaSkinSpec`, com os pixels dos olhos, um pixel de pelo para as pálpebras e as caixas UV das pálpebras.

### Pálpebras sobre modelos vanilla

Os olhos vanilla são pintados na textura. Cada pálpebra é uma caixa pequena por cima dos pixels do olho, um pouco maior que eles (`CubeDeformation(0.05)`) para não piscar contra a textura. Ela é filha da `head` do modelo vanilla e só aparece quando `traits.eyesClosed()`. No camelo:

| | Olho na textura do jogo | Pálpebra |
|---|---|---|
| Adulto | Linha de 1 px na linha 8: 2 px em cada lateral da cabeça e 1 px na quina da frente | 1x1x2 em x=±3.5, y=-20, z=-15. UV (0, 96) e (8, 96) |
| Filhote | 2 px na linha 6 de cada lateral | 1x1x2 em x=±2.5, y=-11, z=-7.5. UV (0, 54) e (8, 54) |

## Testes automáticos

Os testes genéricos percorrem `ModMobs.allAnimalTypes()` (os criados e os adaptados), então o animal adaptado é testado como qualquer outro:
- nasce, sobrevive e segue as regras do mod;
- o macho é maior e só macho + fêmea acasalam;
- dois pais albinos nunca têm filhote normal;
- regenera fora de combate, mas não logo depois do dano;
- a comida cura 10% sem love mode;
- dorme e acorda ao ser ferido.

O teste de loot continua só para os mobs criados, porque a loot do adaptado é a do jogo. Testes específicos:

| Teste | O que verifica |
|---|---|
| `camelKneelsToSleepAndGetsUpWhenItWakes` | Dormindo, ele ajoelha e a IA vanilla não o levanta por 4 s. Ao acordar, levanta |
| `camelHuskIsNotAdapted` | O camelo husk fica como no jogo; o camelo é adaptado |
| `armadilloRollsUpToSleepAndRollsOutWhenItWakes` | Dormindo, o tatu enrola e fica enrolado. Ao acordar, desenrola |

## Como adaptar o próximo animal

Exemplo para a lhama (`minecraft:llama`). O camelo é o exemplo de animal que já sobrescreve os métodos do jogo; o tatu, de animal que não sobrescreve alguns deles.

1. **Ler o animal vanilla**:
   - a classe base (é um `Animal`?)
   - a IA: goals ou Brain
   - os métodos que ele sobrescreve: `finalizeSpawn`, `canMate`, `getBreedOffspring`, `mobInteract`, `getAmbientSound`, `tick`
   - curas próprias, como a do `AbstractHorse`
   - subclasses que devem ficar de fora
2. **Mixin** `mixin/LlamaMixin.java`, copiando o `CamelMixin`:
   - estende a superclasse vanilla, só para alcançar os métodos protegidos
   - implementa `ModCreature` e cria o `AnimalTraits` na primeira chamada de `mobmod$traits()`
   - define os três `EntityDataAccessor` com a classe vanilla
   - injeta nos mesmos métodos e confere o tipo para deixar subclasses de fora
   - método que a classe vanilla não declara (só herda) não aceita injeção: acrescente o override no mixin chamando `super`, como o `finalizeSpawn` e o `canMate` do tatu
   - se a classe sobrescreve `baseTick`, chame `beforeBaseTick()`; senão silencie o `getAmbientSound` dormindo, como no camelo
   - registre o mixin em `mobmod.mixins.json`
3. **Curas vanilla**: desligue as que não forem a regeneração do mod, como no `AbstractHorseMixin`.
4. **Sono**:
   - IA de goals: adicione o `SleepGoal`. Ele recebe um `ModAnimal`; generalize para `Mob & ModCreature` se precisar.
   - Brain: crie um `<Animal>Sleep` com o `SleepSchedule`, como o `CamelSleep`, e bloqueie os comportamentos vanilla que o acordariam. Para achá-los, procure no jar do jogo as classes que chamam o método que desfaz a pose (o tatu tinha dois).
   - Prefira uma pose que o jogo já tenha (sentar, deitar).
5. **Lista**: crie `mob/llama/LlamaAdaptation.java` com o `AdaptedMob` e adicione-o em `ModMobs.ADAPTED`.
6. **Cliente** (`client/mob/llama/`):
   - render state que estende o vanilla e implementa `HasMobTraits`
   - renderer que estende `ModMobRenderer`, reaproveitando camadas e extração do renderer vanilla (abra o que precisar no access widener)
   - modelos que estendem os vanilla e acrescentam as pálpebras
   - `VanillaSkinSpec` de cada textura: ache os pixels dos olhos e uma área livre
   - `LlamaClient` em `ModMobsClient.ALL`
7. **Testar**: `./gradlew runGameTestServer` (os genéricos já cobrem o animal), mais testes específicos do jeito de dormir e das subclasses deixadas de fora.
8. **Documentar**: ficha em `docs/mobs/` a partir do [modelo](../mobs/_modelo.md), marcando os campos vanilla, e uma linha no [índice dos mobs](../mobs/README.md).
