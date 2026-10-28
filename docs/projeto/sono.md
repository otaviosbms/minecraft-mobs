# Sono

Sistema global: todo mob que estende `ModAnimal` (`src/main/java/com/otavi/mobmod/entity/genetics/ModAnimal.java`) dorme à noite, sem código extra. O `ModAnimal` adiciona o `SleepGoal` (`entity/goal/SleepGoal.java`) sozinho no construtor, então um mob novo não precisa registrá-lo em `registerGoals()`. Quando dormir e acordar fica no `SleepSchedule` (`entity/traits/`), também usado pelos [animais vanilla adaptados](animais-vanilla.md#sono-em-ia-de-brain) cuja IA é Brain (o [camelo](../mobs/camelo.md) ajoelha para dormir).

## Regras

| Regra | Como funciona |
|---|---|
| Quando dorme | Quando escurece (`isDarkOutside()`), cada animal deita num momento diferente: em média 5 s depois de anoitecer |
| Onde | Onde estiver: no chão, fora da água. O chimpanzé que estiver na copa de uma árvore dorme lá em cima |
| Quando acorda | De manhã: confere a cada 10 s se o dia já chegou, então o bando levanta aos poucos |
| Acorda na hora | Ao levar dano, ao ganhar um alvo (um companheiro pediu ajuda), ao cair na água, ao sair do chão, ao ser preso numa guia, ao entrar em love mode ou montar/ser montado |
| Não dorme | Até 5 s depois de levar dano, nem com um alvo ou nas outras situações acima |
| Prioridade | Goal de prioridade 2: acima de comer, seguir e passear (não anda nem olha em volta dormindo), abaixo de lutar e fugir, que o acordam |
| Silêncio | Dormindo, não faz o som ambiente (nem o chamado do chimpanzé). De vez em quando ronca baixinho (`entity.fox.sleep`, volume 0.3, no tom de voz do animal) |
| Vida | Continua regenerando normalmente |
| Persistência | O estado é salvo no NBT (`Asleep`), então um animal dormindo continua dormindo ao recarregar o mundo |

## Visual

O estado é sincronizado com o cliente (`DATA_ASLEEP`) e suavizado entre ticks em `getSleepAmount(partialTick)` (0 acordado, 1 deitado), que vai para `ModMobRenderState.sleepAmount`. Deitar e levantar levam 1 s.

### Comum a todos (renderer base, `ModMobRenderer`)

| O quê | Como |
|---|---|
| Pose padrão | `applySleepPose` rola o animal 85° para o lado em volta do meio do corpo e apoia a lateral no chão. Um mob novo sem nada próprio já dorme assim |
| Cabeça e pernas | A cabeça para de seguir o olhar e as pernas param de andar |
| Barra de informações | Desce junto com o animal deitado e ganha uma linha azul-clara "z Z z" animada |
| Olhos | `state.eyesClosed()` fica verdadeiro na metade do caminho para deitar; cada modelo mostra suas pálpebras nesse momento |

### Por animal

| Mob | Pose | Detalhes |
|---|---|---|
| [Elefante](../mobs/elefante.md) | Deitado de lado (pose padrão) | Patas da frente um pouco para a frente e as de trás para trás, tromba enrolada para o peito, queixo baixo, orelhas coladas na cabeça, cauda mole e o flanco subindo e descendo devagar com a respiração |
| [Chimpanzé](../mobs/chimpanze.md) | Encolhido de lado, como no ninho (pose padrão, apoiado no ombro) | Joelhos puxados até o peito, braços abraçados na frente, costas arredondadas, cabeça encolhida para baixo, boca fechada e orelhas paradas |
| [Suricato](../mobs/suricato.md) | De barriga para baixo, esticado no chão (sem rolar: `applySleepPose` vazio no renderer) | Corpo deitado e rebaixado até a barriga encostar no chão, patas da frente esticadas para a frente com o queixo apoiado nelas, patas de trás esticadas para trás e a cauda enrolada para o lado. Só aparece onde ele não consegue cavar; nos outros lugares dorme na toca |
| [Camelo](../mobs/camelo.md) (vanilla adaptado) | Ajoelhado: a animação vanilla de sentar (sem rolar: `applySleepPose` vazio) | Pescoço baixo para a frente, olhos fechados. A IA vanilla não o levanta enquanto dorme; ao acordar, levanta com a animação do jogo |
| [Tatu](../mobs/tatu.md) (vanilla adaptado) | Enrolado na carapaça: a animação vanilla de enrolar (sem rolar: `applySleepPose` vazio) | Fica enrolado mesmo com perigo por perto (a IA vanilla não espia nem desenrola); ao acordar, desenrola com a animação do jogo |

### Pálpebras

Os olhos são pintados na textura, então cada modelo tem duas pálpebras: placas planas logo para fora de cada olho (0,05 px, para não piscar contra a textura), filhas da cabeça, visíveis só quando `eyesClosed()`. A textura delas fica numa área própria de cada PNG, pintada pelos geradores em `tools/textures/`:

| Mob | Tamanho | UV | Desenho |
|---|---|---|---|
| Elefante | 0x1x2 (nas laterais da cabeça) | (200, 70) | Pele com um vinco escuro no lugar do olho |
| Chimpanzé | 4x2x0 (na frente, sob a sobrancelha) | (132, 52) | Pálpebra de pele com a linha escura dos cílios embaixo |
| Suricato | 0x2x2 (nas laterais, dentro da máscara) | (90, 24) | Máscara escura com a linha preta dos cílios embaixo |
| Camelo (vanilla) | 1x1x2 (+0.05) sobre os olhos do jogo, adulto e filhote | (0, 96) / (8, 96); filhote (0, 54) / (8, 54) | Pintadas em tempo real com a cor do pelo, veja [animais vanilla](animais-vanilla.md#pálpebras-sobre-modelos-vanilla) |
| Tatu (vanilla) | 0x1x1 nas laterais da cabeça, adulto e filhote | (0, 60) / (4, 60) | Pintadas em tempo real com a cor do pelo; aparecem enquanto ele enrola e desenrola |

## Suricato

O [suricato](../mobs/suricato.md) dorme na toca: à noite o `MeerkatBurrowGoal` o leva para baixo da terra, e enquanto ele está escondido por esse motivo `isAsleep()` é verdadeiro. O `SleepGoal` só assume onde o chão é duro demais para cavar (pedra, por exemplo): aí ele deita na superfície como os outros.

## Ajustar para um mob

Métodos de `ModCreature` (e `createSleepGoal()` de `ModAnimal`) que qualquer mob pode sobrescrever:

| Método | Para quê |
|---|---|
| `createSleepGoal()` | Trocar o goal de sono, ou devolver `null` para um mob que cuida disso sozinho |
| `canSleep()` | Restringir onde/quando dorme (o suricato acrescenta "não pode cavar aqui") |
| `fallAsleep()` | Outra forma de dormir (o suricato cava a toca) |
| `isAsleep()` | Contar outro estado como sono (o suricato na toca à noite) |
| `getSleepSound()` | Outro som de ronco |

No cliente, para dar ao mob a sua própria animação de dormir:

| Onde | Para quê |
|---|---|
| Renderer: `applySleepPose(state, poseStack, amount)` | Trocar a pose do modelo inteiro (deitar de lado, de barriga, sentado...) ou não fazer nada e deixar o modelo deitar sozinho, como o suricato |
| Renderer: `getSleepingHalfWidth(state)` | Na pose de lado, a que altura fica o meio do corpo (padrão: 30% da largura do hitbox; elefante 32%, chimpanzé 50%). Aumente se afundar no chão, diminua se flutuar |
| Modelo: `setupAnim` | Dobrar patas, cabeça e cauda misturando com `state.sleepAmount` (`Mth.lerp(sleep, pose acordado, pose dormindo)`), parar movimentos ociosos e mostrar as pálpebras com `state.eyesClosed()` |

Para fechar os olhos de um mob novo: adicione as duas pálpebras na cabeça do modelo, cobrindo os olhos pintados, e pinte a área delas no gerador de textura (veja `eyelid(...)` em qualquer gerador).

## Testes automáticos

| Teste | O que verifica |
|---|---|
| `everyAnimalSleepsAndWakesUpWhenHurt` | Todos os animais de `ModMobs.allAnimalTypes()` conseguem dormir (o suricato na toca, em areia; o camelo ajoelhado; o tatu enrolado) e quem pode levar dano acorda ao ser ferido |

Para os testes, `fallAsleep()` faz o animal dormir na hora.
