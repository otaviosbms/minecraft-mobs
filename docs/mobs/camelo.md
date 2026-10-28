# Camelo

| | |
|---|---|
| ID | `minecraft:camel` (animal do jogo, [adaptado](../projeto/animais-vanilla.md)) |
| Nome | Camel / Camelo (nomes do jogo) |
| Inspiração | Dromedário |
| Categoria | `CREATURE` |
| Classe base | `Camel` → `AbstractHorse` → `Animal` (vanilla), com `CamelMixin` implementando `ModCreature` |
| Temperamento | Passivo (foge quando ferido), como no jogo |
| Adaptado em | 0.1.0 |

O camelo do jogo, com tudo o que ele já tem (modelo, animações, sons, montaria para dois, dash, sela, IA, loot e spawn nas vilas do deserto), mais os sistemas globais do mod: sexo, variantes de pele, barra de informações, regeneração e sono. Todo camelo do mundo é afetado, inclusive os de vilas e de mundos antigos. O camelo husk (`minecraft:camel_husk`) fica como no jogo.

## Arquivos

| O quê | Onde |
|---|---|
| Adaptação (lista) | `mob/camel/CamelAdaptation.java` |
| Sono (IA de Brain) | `mob/camel/CamelSleep.java` |
| Mixins | `mixin/CamelMixin.java`, `AbstractHorseMixin.java`, `CamelRandomSittingMixin.java` |
| Modelos (vanilla + pálpebras) | `client/mob/camel/ModAdultCamelModel.java`, `ModBabyCamelModel.java`, `CamelEyelids.java` |
| Renderer e render state | `client/mob/camel/ModCamelRenderer.java`, `ModCamelRenderState.java` |
| Registro cliente e texturas | `client/mob/camel/CamelClient.java` |
| Texturas | Geradas em tempo real a partir de `minecraft:textures/entity/camel/camel.png` e `camel_baby.png` (veja [animais vanilla](../projeto/animais-vanilla.md#texturas-geradas-em-tempo-real)) |

## Atributos

Os do jogo (`Camel.createAttributes()`):

| Atributo | Valor | Observação |
|---|---|---|
| Vida máxima | 32 | |
| Velocidade de movimento | 0.09 | Mais o bônus de corrida quando montado |
| Força de pulo | 0.42 | |
| Altura de passo | 1.5 | |
| Escala | 1.0 fêmea / 1.15 macho | Bônus de macho do `ModCreature` |

## Tamanho e hitbox

| | Largura × altura (blocos) |
|---|---|
| Fêmea adulta | 1.7 × 2.375 (do jogo) |
| Macho adulto | 1.96 × 2.73 |
| Filhote | Escala de filhote do jogo (0.45) |

## Comportamento e IA

A IA é a do jogo (`CamelAi`, Brain): passeia, segue quem segura cacto, foge quando ferido, senta e levanta ao acaso e pode ser montado e selado. O mod só acrescenta o sono e impede que a IA o levante enquanto dorme.

## Reprodução

| | |
|---|---|
| Comida (tag do jogo `minecraft:camel_food`) | Cacto |
| Quem pode acasalar | Somente macho + fêmea |
| Filhote | Sexo 50/50, variante herdada; os atributos do filhote seguem o jogo |

## Regeneração

Usa o sistema global de [regeneração](../projeto/regeneracao.md). A cura aleatória dos cavalos do jogo (1 ponto a cada ~45 s) é desligada para o camelo.

| | |
|---|---|
| Por tempo | 32 / 120 = 0,27 por segundo (vida cheia em 2 min) |
| Começa | 10 s depois do último dano, e só fora de combate |
| Comida | Cada cacto cura 10% da vida (3,2) quando ferido; com a vida cheia funciona como no jogo |

## Sono

Segue o [sono](../projeto/sono.md) global (mesmo horário, mesmos motivos para acordar). A pose é a que o jogo já tem para o camelo descansar:

| O quê | Como |
|---|---|
| Deitar | Ajoelha com a animação vanilla de sentar |
| Cabeça | Baixa o pescoço para a frente (até 0,55 rad), misturado pelo `sleepAmount` |
| Olhos | Fecham na metade do caminho (pálpebras no adulto e no filhote) |
| Silêncio | Sem sons ambientes; ronca baixinho |
| Acordar | De manhã, ao ser ferido, montado, preso numa guia, dando dash ou em love mode: levanta com a animação vanilla |

## Sexo e variantes

| | Fêmea | Macho |
|---|---|---|
| Tamanho | 100% | 115% |
| Diferenças visuais | Nenhuma além do tamanho | |

| Variante | Chance no spawn | Textura | Visual |
|---|---|---|---|
| Normal | 98% | `mobmod:textures/entity/camel/camel.png` | Cores do jogo |
| Albino | 1% | `mobmod:textures/entity/camel/camel_albino.png` | Creme claro, olhos vermelhos |
| Melânico | 1% | `mobmod:textures/entity/camel/camel_melanistic.png` | Marrom escuro |

O filhote usa as mesmas variantes geradas a partir de `camel_baby.png` (`camel_baby<sufixo>.png`).

## Spawn natural, drops e sons

Os do jogo: nasce nas vilas do deserto, não solta itens (`minecraft:entities/camel`) e usa os sons de camelo do jogo.

## Modelo e animações

O modelo e as animações (andar, sentar, levantar, idle, dash) são os do jogo. O mod acrescenta só as pálpebras:

| Parte | Tamanho | Posição | UV |
|---|---|---|---|
| Pálpebra adulta (x2) | 1x1x2 (+0.05) | Quina da frente da cabeça, x=±3.5, y=-20, z=-15 | (0, 96) / (8, 96) |
| Pálpebra do filhote (x2) | 1x1x2 (+0.05) | Laterais da cabeça, x=±2.5, y=-11, z=-7.5 | (0, 54) / (8, 54) |

## Comandos úteis

```
/summon minecraft:camel
/summon minecraft:camel ~ ~ ~ {SkinVariant:"albino",Sex:"male"}
/time set night
```

## Testes automáticos

Coberto pelos gametests genéricos (nasce e sobrevive, macho maior, só macho + fêmea, herança de variante, regeneração, cura por comida e sono). Testes específicos:

| Teste | O que verifica |
|---|---|
| `camelKneelsToSleepAndGetsUpWhenItWakes` | Dormindo, ajoelha e a IA vanilla não o levanta. Ao acordar, levanta |
| `camelHuskIsNotAdapted` | O camelo husk fica como no jogo |
