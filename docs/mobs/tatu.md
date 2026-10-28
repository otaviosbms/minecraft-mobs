# Tatu

| | |
|---|---|
| ID | `minecraft:armadillo` (animal do jogo, [adaptado](../projeto/animais-vanilla.md)) |
| Nome | Armadillo / Tatu (nomes do jogo) |
| Inspiração | Tatu |
| Categoria | `CREATURE` |
| Classe base | `Armadillo` → `Animal` (vanilla), com `ArmadilloMixin` implementando `ModCreature` |
| Temperamento | Passivo (enrola na carapaça quando assustado), como no jogo |
| Adaptado em | 0.1.0 |

O tatu do jogo, com tudo o que ele já tem (modelo, animações de enrolar, desenrolar e espiar, sons, escamas que caem e saem com o pincel, IA, loot e spawn), mais os sistemas globais do mod: sexo, variantes de pele, barra de informações, regeneração e sono. Todo tatu do mundo é afetado, inclusive os de mundos antigos.

## Arquivos

| O quê | Onde |
|---|---|
| Adaptação (lista) | `mob/armadillo/ArmadilloAdaptation.java` |
| Sono (IA de Brain) | `mob/armadillo/ArmadilloSleep.java` |
| Mixins | `mixin/ArmadilloMixin.java`, `ArmadilloAiMixin.java`, `ArmadilloBallUpMixin.java` |
| Modelos (vanilla + pálpebras) | `client/mob/armadillo/ModAdultArmadilloModel.java`, `ModBabyArmadilloModel.java`, `ArmadilloEyelids.java` |
| Renderer e render state | `client/mob/armadillo/ModArmadilloRenderer.java`, `ModArmadilloRenderState.java` |
| Registro cliente e texturas | `client/mob/armadillo/ArmadilloClient.java` |
| Texturas | Geradas em tempo real a partir de `minecraft:textures/entity/armadillo/armadillo.png` e `armadillo_baby.png` (veja [animais vanilla](../projeto/animais-vanilla.md#texturas-geradas-em-tempo-real)) |

## Atributos

Os do jogo (`Armadillo.createAttributes()`):

| Atributo | Valor | Observação |
|---|---|---|
| Vida máxima | 12 | |
| Velocidade de movimento | 0.14 | |
| Escala | 1.0 fêmea / 1.15 macho | Bônus de macho do `ModCreature` |

Enrolado, o tatu recebe menos dano, como no jogo.

## Comportamento e IA

A IA é a do jogo (`ArmadilloAi`, Brain):
- passeia e segue quem segura olho de aranha;
- enrola na carapaça quando vê uma ameaça (um jogador correndo, um morto-vivo) ou leva dano;
- espia de vez em quando e desenrola quando o perigo passa;
- solta escamas de tempos em tempos, e elas também saem com o pincel.

O mod só acrescenta o sono e impede que a IA o desenrole enquanto dorme.

## Reprodução

| | |
|---|---|
| Comida (tag do jogo `minecraft:armadillo_food`) | Olho de aranha |
| Quem pode acasalar | Somente macho + fêmea |
| Filhote | Sexo 50/50, variante herdada |

## Regeneração

Usa o sistema global de [regeneração](../projeto/regeneracao.md). O tatu do jogo não tem cura própria.

| | |
|---|---|
| Por tempo | 12 / 120 = 0,1 por segundo (vida cheia em 2 min) |
| Começa | 10 s depois do último dano, e só fora de combate |
| Comida | Cada olho de aranha cura 10% da vida (1,2) quando ferido, mesmo enrolado; com a vida cheia funciona como no jogo |

## Sono

Segue o [sono](../projeto/sono.md) global (mesmo horário, mesmos motivos para acordar). A pose é a que o jogo já tem: o tatu **enrola na carapaça**, como o suricato entra na toca.

| O quê | Como |
|---|---|
| Deitar | Enrola com a animação vanilla (`rollUp`), até ficar uma bola fechada |
| Olhos | Fecham na metade do caminho (pálpebras no adulto e no filhote), visíveis enquanto ele enrola e desenrola |
| Silêncio | Sem sons ambientes; ronca baixinho |
| Ameaça | Se aparece um perigo enquanto dorme, continua enrolado (a IA vanilla não espia nem desenrola) |
| Acordar | De manhã, ou se for montado, preso numa guia, cair na água ou entrar em love mode: desenrola com a animação vanilla |
| Ferido | Acorda e fica enrolado, como no jogo: a IA vanilla desenrola quando o perigo passa |

## Sexo e variantes

| | Fêmea | Macho |
|---|---|---|
| Tamanho | 100% | 115% |
| Diferenças visuais | Nenhuma além do tamanho | |

| Variante | Chance no spawn | Textura | Visual |
|---|---|---|---|
| Normal | 98% | `mobmod:textures/entity/armadillo/armadillo.png` | Cores do jogo |
| Albino | 1% | `mobmod:textures/entity/armadillo/armadillo_albino.png` | Creme claro, olhos vermelhos |
| Melânico | 1% | `mobmod:textures/entity/armadillo/armadillo_melanistic.png` | Marrom escuro |

O filhote usa as mesmas variantes, geradas a partir de `armadillo_baby.png` (`armadillo_baby<sufixo>.png`).

## Spawn natural, drops e sons

Os do jogo: nasce em savanas e badlands, solta escamas (`minecraft:gameplay/armadillo_shed`) e usa os sons de tatu do jogo.

## Modelo e animações

O modelo e as animações (andar, enrolar, desenrolar, espiar) são os do jogo. O mod acrescenta só as pálpebras, filhas do `head_cube`:

| Parte | Tamanho | Posição | UV |
|---|---|---|---|
| Pálpebra adulta (x2) | 0x1x1, 0,05 para fora da face | Laterais da cabeça, x=±1.5, y=1, z=-1 | (0, 60) / (4, 60) |
| Pálpebra do filhote (x2) | 0x1x1, 0,05 para fora da face | Laterais da cabeça, x=±1, y=-2, z=-3 | (0, 60) / (4, 60) |

## Comandos úteis

```
/summon minecraft:armadillo
/summon minecraft:armadillo ~ ~ ~ {SkinVariant:"melanistic",Sex:"female"}
/time set night
```

## Testes automáticos

Coberto pelos gametests genéricos (nasce e sobrevive, macho maior, só macho + fêmea, herança de variante, regeneração, cura por comida e sono). Teste específico:

| Teste | O que verifica |
|---|---|
| `armadilloRollsUpToSleepAndRollsOutWhenItWakes` | Dormindo, enrola e fica enrolado (a IA vanilla não o desenrola). Ao acordar, desenrola |
