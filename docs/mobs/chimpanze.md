# Chimpanzé

| | |
|---|---|
| ID | `mobmod:chimpanzee` |
| Nome | Chimpanzee / Chimpanzé |
| Inspiração | Chimpanzé-comum |
| Categoria | `CREATURE` (animal passivo, conta no limite de animais) |
| Classe base | `ModAnimal` → `Animal` |
| Temperamento | Neutro |
| Adicionado em | 0.1.0 |

![Variantes do chimpanzé](../imagens/chimpanze-variantes.png)

Um primata neutro das selvas, em tamanho real (cerca de 1 bloco de altura andando de quatro). Vive em bandos, anda apoiado nos nós dos dedos e escala paredes e troncos como uma aranha, sem cair sem querer da copa das árvores. Quando um membro do bando é atacado, os outros defendem.

## Arquivos

| O quê | Onde |
|---|---|
| Definição (tamanho, spawn, comida, nome) | [`mob/chimpanzee/ChimpanzeeMob.java`](../../src/main/java/com/otavi/mobmod/mob/chimpanzee/ChimpanzeeMob.java) |
| Entidade (atributos, IA, escalada, ataque, sons) | [`mob/chimpanzee/Chimpanzee.java`](../../src/main/java/com/otavi/mobmod/mob/chimpanzee/Chimpanzee.java) |
| Modelo e animações | [`client/mob/chimpanzee/ChimpanzeeModel.java`](../../src/client/java/com/otavi/mobmod/client/mob/chimpanzee/ChimpanzeeModel.java) |
| Renderer e render state | [`ChimpanzeeRenderer.java`](../../src/client/java/com/otavi/mobmod/client/mob/chimpanzee/ChimpanzeeRenderer.java), [`ChimpanzeeRenderState.java`](../../src/client/java/com/otavi/mobmod/client/mob/chimpanzee/ChimpanzeeRenderState.java) |
| Registro cliente | [`ChimpanzeeClient.java`](../../src/client/java/com/otavi/mobmod/client/mob/chimpanzee/ChimpanzeeClient.java) |
| Texturas | `src/client/resources/assets/mobmod/textures/entity/chimpanzee/` |
| Gerador das texturas e do ovo | [`tools/textures/ChimpanzeeTexture.java`](../../tools/textures/ChimpanzeeTexture.java) |
| JSON gerados (lang, tag, modelo do ovo) | `src/main/generated/` (via `./gradlew runDatagen`) |

## Atributos

| Atributo | Valor | Observação |
|---|---|---|
| Vida máxima | 24 | 12 corações |
| Velocidade de movimento | 0.3 | Mais rápido que a vaca (0.2), igual ao lobo |
| Dano de ataque | 4 | 2 corações por golpe, antes da armadura |
| Knockback do ataque | 0.5 | Empurrão leve |
| Altura de passo | 1.0 | Sobe um bloco inteiro sem pular |
| Escala | 1.0 fêmea / 1.15 macho | Bônus de macho aplicado por `ModAnimal` (`minecraft:scale`) |

Os demais atributos vêm de `Animal.createAnimalAttributes()`, com os valores padrão do jogo.

## Tamanho e hitbox

| | Largura × altura (blocos) | Altura dos olhos |
|---|---|---|
| Fêmea adulta | 0.8 × 1.05 | 0.9 |
| Macho adulto | 0.92 × 1.21 | 1.04 |
| Filhote fêmea | 0.4 × 0.53 | 0.45 |
| Filhote macho | 0.46 × 0.6 | 0.52 |

Escala da vida real: um chimpanzé andando de quatro tem cerca de 1 m até o topo da cabeça (1,2 a 1,7 m em pé). A distância de rastreamento do cliente é de 8 chunks.

## Comportamento e IA

### Goals (por prioridade, menor = mais importante)

| Prioridade | Goal | Detalhes |
|---|---|---|
| 0 | `FloatGoal` | Nada para não afundar |
| 1 | `AdultMeleeAttackGoal` | Só adultos. Persegue o alvo a 1.3× a velocidade e continua mesmo sem vê-lo |
| 1 | `BabyPanicGoal` | Só filhotes. Ao levar dano, foge a 1.8× a velocidade |
| 2 | `BreedGoal` | Procura parceiro quando está em love mode (1.0×) |
| 3 | `TemptGoal` | Segue o jogador segurando comida da tag `mobmod:chimpanzee_food` (1.2×). Não se assusta com movimentos bruscos |
| 4 | `FollowParentGoal` | Filhotes seguem um adulto (1.2×) |
| 5 | `WaterAvoidingRandomStrollGoal` | Passeia evitando a água (1.0×) |
| 6 | `LookAtPlayerGoal` | Olha para jogadores a até 8 blocos |
| 7 | `RandomLookAroundGoal` | Olha ao redor quando está parado |

### Alvos

| Prioridade | Goal | Detalhes |
|---|---|---|
| 1 | `HurtByTargetGoal` + `setAlertOthers()` | Quem ataca um chimpanzé vira alvo dele **e dos chimpanzés próximos** |

Resultado:
- Atacar um adulto faz o bando inteiro contra-atacar.
- Atacar um filhote faz o filhote fugir e os adultos próximos atacarem.
- Sem provocação, o chimpanzé nunca ataca.

### Escalada

Igual à da aranha:
- usa `WallClimberNavigation`, então os caminhos podem subir paredes e troncos;
- quando encosta numa parede, fica "escalável" (`onClimbable`) e sobe por ela;
- o estado de escalada é sincronizado com o cliente, que anima os braços estendidos para cima.

Na selva, isso deixa o chimpanzé subir nas árvores e nos troncos grossos.

### Segurança contra quedas

Para não pular da copa das árvores sem querer:

| Regra | Como funciona |
|---|---|
| Borda | Ao andar sozinho (passeando, seguindo comida ou os pais, fugindo), o movimento que o levaria para fora de uma borda com queda **maior que 2 blocos** é cortado, como um jogador agachado (`maybeBackOffFromEdge`). Pelo menos 0,15 bloco da área interna da hitbox precisa continuar sobre chão |
| Caminhos | A navegação só planeja quedas de até 2 blocos (`getMaxFallDistance`), então os caminhos combinam com a regra da borda e ele não fica preso tentando segui-los |
| Exceção | Perseguindo um alvo (depois de ser provocado), pode pular de qualquer altura, como um animal vanilla |
| Já pendurado | Se for empurrado ou levar knockback para fora da borda, a regra não o trava: ele pode voltar ou cair |

O limite está em `Chimpanzee.SAFE_DROP`. Na copa, ele desce por degraus de até 2 blocos ou escalando os troncos.

### Ataque

- Corpo a corpo com 4 de dano e knockback 0.5.
- A cada golpe toca o som de mordida do panda e dispara a animação de ataque, que dura 10 ticks (0,5 s) e é sincronizada pelo evento de entidade `4`.

## Reprodução

Usa o sistema global de [genética](../projeto/genetica.md):

| | |
|---|---|
| Comida (tag `mobmod:chimpanzee_food`) | Maçã, fatia de melancia, bagas doces, bagas brilhantes e sementes de cacau |
| Quem pode acasalar | Somente macho + fêmea |
| Love mode, cooldown e XP | Iguais aos animais vanilla |
| Filhote | Sexo 50/50 e variante herdada de um dos pais (1% de mutação para cada variante rara) |
| Crescimento | Igual aos animais vanilla. A comida acelera o crescimento do filhote |

## Sexo e variantes

| | Fêmea | Macho |
|---|---|---|
| Tamanho | 100% | 115% (hitbox e modelo) |
| Inchaço vermelho na traseira | Sim (só adulta) | Não |
| Símbolo na barra | ♀ rosa | ♂ azul |

O inchaço é o inchaço sexual das fêmeas de chimpanzé, presente na referência.

| Variante | Chance no spawn | Textura | Visual |
|---|---|---|---|
| Normal | 98% | `chimpanzee.png` | Pelo marrom quase preto, rosto, mãos e pés claros, olhos castanhos |
| Albino | 1% | `chimpanzee_albino.png` | Pelo branco, pele rosada, olhos vermelhos |
| Melânico | 1% | `chimpanzee_melanistic.png` | Pelo preto retinto, pele escura |

Sexo e variante são salvos no NBT (`Sex`, `SkinVariant`) e aparecem na barra de informações acima do chimpanzé, a até 16 blocos.

## Spawn natural

| | |
|---|---|
| Biomas | Tag `#minecraft:is_jungle` (selva, selva esparsa, selva de bambu e biomas de outros mods que usem a tag) |
| Peso | 8 |
| Tamanho do grupo | 3 a 6 |
| Posicionamento | No chão (`ON_GROUND`), heightmap `MOTION_BLOCKING_NO_LEAVES` |
| Regras | `Animal.checkAnimalSpawnRules`: precisa de bloco de grama embaixo e luz suficiente, como os animais vanilla |

## Drops

Nenhum item: o chimpanzé não tem loot table (`loot` não é definido em `ChimpanzeeMob`). Solta só a XP padrão de animal ao ser morto por um jogador.

## Sons

Emprestados do panda, com o tom da voz em **1.35×** (mais agudo, mais perto de um chimpanzé):

| Evento | Som | Detalhe |
|---|---|---|
| Ambiente | `entity.panda.ambient` | A cada 160 ticks (8 s) em média |
| Dano | `entity.panda.hurt` | |
| Morte | `entity.panda.death` | |
| Passos | `entity.panda.step` | Volume 0.15, tom 1.3 (passos leves) |
| Ataque | `entity.panda.bite` | Só quando o golpe acerta |

## Modelo

Feito em código Java, sem Blockbench nem GeckoLib. Textura de 256×256.

O modelo é construído em **detalhe dobrado** (cada peça com o dobro de pixels) e depois reduzido para o tamanho real com `MeshTransformer.scaling(0.37F)` (filhote: `0.185F`). Assim a textura tem o dobro de pixels por bloco, com um nível de detalhe próximo ao do elefante. Medidas em pixels da geometria em detalhe dobrado:

| Parte | Tamanho (L × A × C) | Observação |
|---|---|---|
| Corpo | 24 × 20 × 28 | Inclinado: ombros altos, quadril baixo (`xRot -0.35`) |
| Cabeça | 18 × 16 × 14 | Sobre os ombros |
| Arco da sobrancelha | 16 × 4 × 2 | Parte da cabeça, saliente na testa |
| Focinho | 12 × 5 × 6 | Claro, com narinas, rugas e lábio superior |
| Mandíbula | 12 × 3 × 6 | Articulada atrás do focinho: abre mostrando dentes e língua |
| Orelhas (2) | 2 × 8 × 8 | Redondas, nas laterais da cabeça, com dobra escura |
| Braços (2) | 10 × 30 × 10 | Longos, com mãos claras, dedos e unhas (apoio nos nós dos dedos) |
| Pernas (2) | 10 × 18 × 10 | Curtas, com pés claros e artelhos |
| Inchaço | 20 × 12 × 6 | Vermelho, na traseira. Só fêmeas adultas |

O layout UV de cada peça está documentado no topo de `tools/textures/ChimpanzeeTexture.java`.

### Textura

Gerada no mesmo estilo do elefante (`MobTexture`), com detalhes pintados na resolução dobrada:
- pelo com fios curtos (verticais nas laterais, deitados nas costas), reflexo acinzentado nos ombros e faixa escura na espinha;
- rosto claro com bico de viúva, olheiras fundas, olhos com íris, pupila, brilho e pálpebra;
- narinas largas, sulco até o lábio, lábios rosados, dentes e língua (visíveis com a boca aberta);
- mãos e pés com nós dos dedos, dedos separados, unhas e palmas com vincos;
- orelhas com dobra em "C" e inchaço com dobras e brilho.

## Animações

Todas procedurais, calculadas em `ChimpanzeeModel.setupAnim`:

| Animação | Como funciona |
|---|---|
| Cabeça | Acompanha o olhar, até 60° para os lados |
| Caminhada | Membros diagonais juntos (os braços balançam mais que as pernas). O corpo sobe e desce duas vezes por passada e balança de lado; a cabeça compensa o movimento |
| Respiração (parado) | O corpo e a cabeça sobem e descem devagar |
| Cabeça curiosa (parado) | Inclina a cabeça de um lado para o outro |
| Orelhas | Mexem rapidamente de vez em quando, cada uma no seu ritmo |
| Chamado | Toda vez que toca o som ambiente, levanta a cabeça e abre a boca (1 s). O servidor avisa o cliente pelo evento de entidade `5` |
| Escalada | O corpo fica mais vertical contra a parede, os braços se alternam esticados para cima, cada perna empurra junto com o braço oposto, e a cabeça olha para cima |
| Ataque | Levanta os dois braços e bate para baixo, com a cabeça para trás e a boca aberta (0,5 s) |

Os deslocamentos (respiração e passos) são multiplicados pela escala da camada, para o filhote balançar na mesma proporção do adulto.

## Itens e creative

| | |
|---|---|
| Ovo gerador | `mobmod:chimpanzee_spawn_egg` ("Chimpanzee Spawn Egg" / "Ovo Gerador de Chimpanzé") |
| Aba criativa | Spawn Eggs, logo depois do ovo de Galinha |
| Textura do ovo | `textures/item/chimpanzee_spawn_egg.png` (marrom-escuro com manchas claras, gerada pelo mesmo gerador) |

## Traduções

| Chave | en_us | pt_br |
|---|---|---|
| `entity.mobmod.chimpanzee` | Chimpanzee | Chimpanzé |
| `item.mobmod.chimpanzee_spawn_egg` | Chimpanzee Spawn Egg | Ovo Gerador de Chimpanzé |
| `tag.item.mobmod.chimpanzee_food` | Food for Chimpanzees | Comida para Chimpanzés |

## Comandos úteis

```
/summon mobmod:chimpanzee
/summon mobmod:chimpanzee ~ ~ ~ {Sex:"female",SkinVariant:"albino"}
/summon mobmod:chimpanzee ~ ~ ~ {Age:-24000}
/data get entity @e[type=mobmod:chimpanzee,limit=1,sort=nearest]
```

`Age:-24000` cria um filhote. Ainda não foi verificado no jogo se o `/summon` com NBT mantém `Sex`/`SkinVariant`. Se não mantiver, use `/data merge entity` depois de invocar.

## Testes automáticos

Coberto pelos gametests genéricos (`./gradlew runGameTestServer`), que percorrem todos os mobs:
- nasce e sobrevive;
- macho maior que fêmea;
- só macho + fêmea acasalam;
- dois pais albinos nunca têm filhote normal.

O teste de loot table não se aplica, porque o chimpanzé não tem drops.

Teste específico, `chimpanzeeDoesNotWalkOffHighLedges`: põe o chimpanzé sobre um bloco a 5 de altura, tenta movê-lo 2 blocos para fora e confirma que ele continua lá em cima.
