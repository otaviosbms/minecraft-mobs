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

Um primata neutro das selvas. Vive em bandos, anda apoiado nos nós dos dedos e escala paredes e troncos como uma aranha. Quando um membro do bando é atacado, os outros defendem.

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
| Fêmea adulta | 1.0 × 1.4 | 1.15 |
| Macho adulto | 1.15 × 1.61 | 1.32 |
| Filhote fêmea | 0.5 × 0.7 | 0.58 |
| Filhote macho | 0.58 × 0.81 | 0.66 |

A altura é a do animal andando de quatro, apoiado nos nós dos dedos. A distância de rastreamento do cliente é de 8 chunks.

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

Feito em código Java, sem Blockbench nem GeckoLib. Textura de 128×128. Medidas em pixels do modelo (16 px = 1 bloco), para a fêmea adulta:

| Parte | Tamanho (L × A × C) | Observação |
|---|---|---|
| Corpo | 12 × 10 × 14 | Inclinado: ombros altos, quadril baixo (`xRot -0.35`) |
| Cabeça | 9 × 8 × 7 | Sobre os ombros |
| Arco da sobrancelha | 8 × 2 × 1 | Parte da cabeça, saliente na testa |
| Focinho | 6 × 4 × 3 | Claro, com narinas e boca |
| Orelhas (2) | 1 × 4 × 4 | Redondas, nas laterais da cabeça, com dobra escura |
| Braços (2) | 5 × 15 × 5 | Longos, com mãos claras (apoio nos nós dos dedos) |
| Pernas (2) | 5 × 9 × 5 | Curtas, com pés claros |
| Inchaço | 10 × 6 × 3 | Vermelho, na traseira. Só fêmeas adultas |

O filhote é o mesmo modelo reduzido à metade (`MeshTransformer.scaling(0.5F)`). O layout UV de cada peça está documentado no topo de `tools/textures/ChimpanzeeTexture.java`.

## Animações

Todas procedurais, calculadas em `ChimpanzeeModel.setupAnim`:

| Animação | Como funciona |
|---|---|
| Cabeça | Acompanha o olhar, até 60° para os lados, com um leve balanço parado |
| Caminhada | Membros diagonais juntos; os braços balançam mais que as pernas curtas |
| Escalada | Os dois braços esticados para cima, alternando como se agarrasse a parede |
| Ataque | Levanta os dois braços e bate para baixo, com a cabeça para trás (0,5 s) |

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
