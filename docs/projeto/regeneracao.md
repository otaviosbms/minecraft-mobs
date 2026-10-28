# Regeneração de vida

Sistema global: todo animal do mod recupera vida sozinho com o tempo e ao comer a sua comida, sem código extra. Vale para quem estende `ModAnimal` (`src/main/java/com/otavi/mobmod/entity/genetics/ModAnimal.java`) e para os [animais vanilla adaptados](animais-vanilla.md), que têm as curas próprias do jogo desligadas (a cura aleatória dos cavalos, no camelo). As regras ficam em `AnimalTraits` (`entity/traits/`).

## Regras

| Regra | Como funciona |
|---|---|
| Velocidade | Proporcional à vida máxima: todo mob vai de 0 à vida cheia em **2 minutos**, qualquer que seja o tamanho |
| Passos | A vida volta uma vez por segundo, então a barra de informações acima do mob sobe em passos visíveis |
| Atraso | Começa **10 segundos** depois do último dano recebido. Cada novo dano reinicia a contagem |
| Combate | Não regenera enquanto o mob tem um alvo (está lutando ou perseguindo alguém) |
| Comida | Dar ao mob ferido um item da comida dele cura **10% da vida máxima**: consome um item, toca o som de comer, solta corações e **não** o deixa em love mode |
| Vida cheia | Com a vida cheia, a comida funciona como antes: love mode para reproduzir e acelerar o crescimento dos filhotes |
| Persistência | O tempo desde o último dano é salvo no NBT (`TicksSinceDamage`), então recarregar o mundo não cura na hora |

Filhotes também regeneram. Um filhote ferido que recebe comida é curado em vez de crescer mais rápido.

## Valores por mob

| Mob | Vida | Regeneração | Vida cheia em | Cura por comida |
|---|---|---|---|---|
| [Elefante](../mobs/elefante.md) | 60 | 0,5 por segundo | 2 min | 6 (3 corações) |
| [Chimpanzé](../mobs/chimpanze.md) | 24 | 0,2 por segundo | 2 min | 2,4 (pouco mais de 1 coração) |
| [Suricato](../mobs/suricato.md) | 10 | 0,083 por segundo | 2 min | 1 (meio coração) |
| [Camelo](../mobs/camelo.md) (vanilla) | 32 | 0,27 por segundo | 2 min | 3,2 (pouco mais de 1,5 coração) |

O macho tem a mesma vida da fêmea; só o tamanho muda.

## Ajustar para um mob

Os valores padrão vêm de três métodos de `ModCreature`, que qualquer mob pode sobrescrever (são públicos, porque vêm de uma interface):

```java
@Override
public int regenFullHealthSeconds() {
    return 300; // regenera mais devagar: vida cheia em 5 min
}

@Override
public int regenDelaySeconds() {
    return 20; // espera 20 s depois do último dano
}

@Override
public float foodHealFraction() {
    return 0.25F; // cada item cura 25% da vida
}
```

## Testes automáticos

Percorrem todos os animais de `ModMobs.allAnimalTypes()` (os criados do zero e os vanilla adaptados):

| Teste | O que verifica |
|---|---|
| `injuredMobsRegenerateOutOfCombat` | Ferido até a metade da vida e sem o atraso, recupera 3 passos (1/40 da vida máxima) em 3 segundos |
| `mobsDoNotRegenerateRightAfterDamage` | Logo depois do dano, dentro do atraso de 10 s, a vida não sobe |
| `foodHealsInjuredMobs` | Ferido, recebe um item de comida de um jogador: cura 10% da vida, consome exatamente um item e não entra em love mode |

Para os testes, `setTicksSinceDamage(...)` pula o atraso.
