# Genética: sexo, reprodução e variantes de pele

Sistema global: todo animal do mod ganha sexo, variante de pele e a barra de informações, sem código extra. Vale para quem estende `ModAnimal` (`src/main/java/com/otavi/mobmod/entity/genetics/`) e para os [animais vanilla adaptados](animais-vanilla.md) (o camelo). As regras ficam em `AnimalTraits` (`entity/traits/`), usadas pelos dois através da interface `ModCreature`.

![Variantes do elefante](../imagens/elefante-variantes.png)

## Sexo

| Regra | Como funciona |
|---|---|
| Sorteio | 50% macho, 50% fêmea, no spawn natural, por ovo, por comando e no nascimento |
| Tamanho | Machos ficam **15% maiores** pelo atributo vanilla `minecraft:scale` (hitbox e modelo crescem juntos). Ajustável por mob sobrescrevendo `maleScaleBonus()` (de `ModCreature`) |
| Visual | Cada mob decide no modelo. No elefante, os machos têm presas longas e as fêmeas presas curtas |
| Persistência | Salvo no NBT como `Sex: "male"/"female"` e sincronizado com o cliente |

## Reprodução

Igual à vanilla: alimentar com a comida da tag do mob, love mode, cooldown de 5 minutos, XP e filhote que cresce. A única diferença é que **só um macho e uma fêmea podem acasalar** (`AnimalTraits.isOppositeSex`, chamado no `canMate`). Dois machos ou duas fêmeas apaixonados ficam lado a lado sem gerar filhote.

O filhote é criado no `getBreedOffspring` de cada animal (o do `ModAnimal` ou o vanilla, no caso dos adaptados), e os genes vêm de `AnimalTraits.giveGenesTo`:
- sexo sorteado (50/50);
- variante herdada de um dos pais (50% de cada), com mutação (veja abaixo).

## Variantes de pele

| Variante | Spawn natural | Textura |
|---|---|---|
| Normal | 98% | `<mob>.png` |
| Albino | 1% | `<mob>_albino.png` |
| Melânico | 1% | `<mob>_melanistic.png` |

**Herança:** o filhote pega a variante do pai ou da mãe. Além disso, todo filhote tem 1% de chance de nascer albino e 1% de nascer melânico, independente dos pais (mutação). Então dois albinos sempre têm filhotes raros, e dois normais raramente têm um.

As chances estão em `SkinVariant.NATURAL_CHANCE` e `SkinVariant.MUTATION_CHANCE`. A variante é salva no NBT como `SkinVariant: "normal"/"albino"/"melanistic"`.

O renderer escolhe a textura sozinho pela convenção de nome (`ModMobRenderer.getTextureLocation`). Cada mob novo só precisa dos três PNGs.

### Forçar uma variante para testar

```
/summon mobmod:elephant ~ ~ ~ {Sex:"male",SkinVariant:"albino"}
```
O `finalizeSpawn` sorteia de novo quando o mob nasce por comando. Se o valor não ficar, use `/data merge entity @e[type=mobmod:elephant,limit=1,sort=nearest] {SkinVariant:"melanistic"}`.

## Barra de informações (estilo Mo'Creatures)

`MobInfoBar` desenha acima de cada mob do mod, a até 16 blocos:
- linha de cima: nome (ou o nome dado com nametag), a variante entre parênteses se for rara, e ♂ azul ou ♀ rosa;
- linha de baixo: barra de vida com 20 segmentos, verde acima de 50%, amarela até 25% e vermelha abaixo disso.

Ela substitui o nametag vanilla nesses mobs e usa o mesmo pipeline de renderização.

## Mobs que não são animais

`Sex`, `SkinVariant` e a interface `GeneticMob` não dependem de `Animal`. Um mob hostil futuro pode ter uma base própria (ex.: `ModMonster`) que implemente `GeneticMob` com os mesmos dados sincronizados. O `ModMobRenderer` e a barra funcionam com qualquer `ModCreature`, inclusive com render states vanilla (veja [animais vanilla](animais-vanilla.md#lado-cliente)).
