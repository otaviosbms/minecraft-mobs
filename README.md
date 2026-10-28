# Minecraft Mobs

Mod de mobs para Minecraft feito com **Fabric**. Adiciona três animais novos, o **Elefante**, das savanas, o **Chimpanzé**, das selvas, e o **Suricato**, dos desertos. Também adapta animais que já existem no jogo, o **Camelo** e o **Tatu**, às mesmas regras: sexo, variantes de pele, barra de informações, regeneração e sono.

| | |
|---|---|
| Minecraft | 26.3 |
| Fabric Loader | ≥ 0.19.5 |
| Fabric API | 0.161.0+26.3 |
| Java | 25 |
| Versão do mod | 0.1.0 |
| Mod ID | `mobmod` |

## Conteúdo

### Elefante (`mobmod:elephant`)

- **Tamanho:** cerca de 3 blocos de altura, com proporções de um elefante africano. Machos são 15% maiores e têm presas mais longas.
- **Comportamento:** neutro. Vive em manadas e só ataca se for ferido ou se um filhote da manada for ferido. Filhotes não lutam: fogem em pânico e chamam os adultos.
- **Atributos:** 60 de vida, 8 de dano, resistência alta a knockback e sobe blocos de altura 1.
- **Spawn:** biomas de savana, em grupos de 2 a 4.
- **Reprodução:** alimentado com os itens da tag `mobmod:elephant_food`, que também o atraem. Só um macho e uma fêmea geram filhotes.
- **Sono:** deita de lado, com a tromba enrolada para o peito, as orelhas paradas e o flanco subindo e descendo com a respiração.
- **Extras:** ovo de spawn na aba criativa (Spawn Eggs), loot table própria e traduções em inglês e português (BR).

### Chimpanzé (`mobmod:chimpanzee`)

- **Tamanho:** real, cerca de 1 bloco de altura andando de quatro apoiado nos nós dos dedos. Machos são 15% maiores; fêmeas adultas têm o inchaço vermelho na traseira.
- **Comportamento:** neutro. Vive em bandos de 3 a 6, escala paredes e árvores como uma aranha sem cair sem querer da copa, e o bando defende quem for atacado.
- **Animações:** caminhada com balanço do corpo, respiração, cabeça curiosa, orelhas que mexem, boca que abre ao chamar e escalada com braços e pernas.
- **Atributos:** 24 de vida, 4 de dano, velocidade 0.3.
- **Spawn:** biomas de selva.
- **Reprodução:** maçã, fatia de melancia, bagas doces, bagas brilhantes e sementes de cacau. Só um macho e uma fêmea geram filhotes.
- **Árvores:** sobe com frequência nas árvores, atravessando a folhagem pelo tronco, fica de 30 a 90 s na copa e depois desce sem se machucar.
- **Fezes:** chance baixa de jogar fezes num jogador próximo (em média uma vez a cada 2 a 4 minutos). Funcionam como a bola de neve (empurram, sem dano) e o jogador também pode arremessá-las. Os adultos deixam fezes no chão a cada 5 a 10 minutos, como a galinha bota ovos.
- **Sono:** encolhido de lado, como no ninho, com os joelhos no peito e os braços abraçados na frente. Se estiver na copa, dorme lá em cima.
- **Drops:** nenhum item.

### Suricato (`mobmod:meerkat`)

- **Tamanho:** cerca de 0,6 bloco em pé (o dobro do real, para ficar visível). Machos são 15% maiores.
- **Comportamento:** neutro. Vive em grupos de 3 a 6, anda de quatro e com frequência fica em pé nas patas de trás para vigiar, encarando o jogador que estiver perto. O grupo ataca junto quem machucar um deles, e os adultos caçam traças e endermites.
- **Toca:** entra na terra (areia, terra, grama...) para se esconder. Foge assim de monstros e de jogadores correndo, com um grito de alarme que manda o grupo inteiro para baixo. Também descansa lá de vez em quando. Escondido, não pode ser atingido nem ferido, e volta para a superfície quando o perigo passa.
- **Sono:** dorme na toca à noite. Onde o chão é duro demais para cavar, dorme de barriga para baixo, com o queixo apoiado nas patas.
- **Animações:** transição suave entre de quatro e em pé, passinhos rápidos, cabeça que varre os arredores, cauda e orelhas que mexem e bote ao morder.
- **Atributos:** 10 de vida, 2 de dano, velocidade 0.3.
- **Spawn:** desertos (`#c:is_desert`), nascendo na areia.
- **Reprodução:** olho de aranha e ovo. Só um macho e uma fêmea geram filhotes.
- **Drops:** nenhum item.

Fichas completas: [Elefante](docs/mobs/elefante.md), [Chimpanzé](docs/mobs/chimpanze.md) e [Suricato](docs/mobs/suricato.md).

### Animais do jogo adaptados

O animal continua sendo o do jogo, com o modelo, as animações, os sons, a IA, o loot, o spawn e o ovo originais. Ele ganha os sistemas do mod listados abaixo. Vale para todos os exemplares, inclusive os de mundos criados antes do mod. As variantes de pele são geradas em tempo real a partir da textura do jogo, então nenhuma arte da Mojang fica no repositório.

| Animal | Como dorme | Ficha |
|---|---|---|
| **Camelo** (`minecraft:camel`) | Ajoelha com a animação de sentar do jogo e baixa o pescoço | [camelo.md](docs/mobs/camelo.md) |
| **Tatu** (`minecraft:armadillo`) | Enrola na carapaça e fica enrolado até de manhã, mesmo com perigo por perto | [tatu.md](docs/mobs/tatu.md) |

O camelo husk continua como no jogo. Como foi feita a adaptação: [animais vanilla](docs/projeto/animais-vanilla.md).

### Para todos os animais

- **Sexo:** macho ou fêmea, com reprodução igual à vanilla, mas só entre sexos opostos.
- **Variantes de pele:** albino e melânico (1% cada), herdadas dos pais.
- **Barra de informações:** nome, sexo (♂/♀) e vida acima do mob, no estilo Mo'Creatures.
- **Regeneração:** fora de combate, a vida volta sozinha (cheia em 2 minutos, começando 10 s depois do último dano). Dar a comida a um mob ferido cura 10% da vida.
- **Sono:** à noite cada animal dorme na sua pose, fecha os olhos, fica quieto (ronca baixinho) e mostra um "z Z z" na barra. Acorda de manhã, ou na hora se for ferido, chamado pelo bando, preso numa guia ou se cair na água.

![Variantes do elefante](docs/imagens/elefante-variantes.png)
![Variantes do chimpanzé](docs/imagens/chimpanze-variantes.png)
![Variantes do suricato](docs/imagens/suricato-variantes.png)

Detalhes em [genetica.md](docs/projeto/genetica.md), [regeneracao.md](docs/projeto/regeneracao.md) e [sono.md](docs/projeto/sono.md). Cada mob tem sua ficha completa em [docs/mobs](docs/mobs/README.md).

## Como usar

1. Instale o [Fabric Loader](https://fabricmc.net/use/) para o Minecraft 26.3.
2. Coloque o [Fabric API](https://modrinth.com/mod/fabric-api) na pasta `mods`.
3. Coloque o `mobmod-0.1.0.jar` na pasta `mods`.

## Desenvolvimento

Requer **JDK 25**. Tudo roda pelo Gradle Wrapper; a primeira execução baixa o Gradle, o Minecraft e as dependências.

```bash
./gradlew build               # compila e gera build/libs/mobmod-0.1.0.jar
./gradlew runDatagen          # regenera lang, loot tables, tags e modelos em src/main/generated
./gradlew runClient           # abre o Minecraft com o mod (pasta run/)
./gradlew runServer           # servidor de desenvolvimento
./gradlew runGameTestServer   # sobe um servidor, roda os gametests e sai
```

As versões do Minecraft, Loader e Fabric API ficam em [`gradle.properties`](gradle.properties).

## Estrutura

Cada mob vive no próprio pacote e é declarado uma vez numa `MobDefinition`. Registro, spawn, ovo, traduções, loot, tags e testes saem dessa definição. As regras comuns a todos os animais (genética, regeneração, sono) ficam escritas uma vez só em `entity/traits/`. Os animais criados do zero e os do jogo adaptados por mixins usam as mesmas regras.

```
src/main/java/.../core/          MobDefinition, SpawnRule, AdaptedMob e ModMobs (lista de todos os mobs)
src/main/java/.../entity/traits/ ModCreature e AnimalTraits: regras comuns a todos os animais
src/main/java/.../mob/<nome>/    entidade e definição de cada mob (ou a adaptação de um animal do jogo)
src/main/java/.../mixin/         mixins que adaptam animais do jogo (camelo, tatu)
src/client/java/.../core/        ClientMob e ModMobsClient (lado cliente de todos os mobs)
src/client/java/.../mob/<nome>/  modelo, renderer e registro cliente de cada mob
src/client/java/.../datagen/     providers do datagen
src/main/generated/              JSONs gerados pelo datagen (não editar à mão)
docs/                            documentação do projeto
```

Adicionar um mob é criar as duas pastas dele, colocar uma linha em `ModMobs` e outra em `ModMobsClient`, e rodar `runDatagen`. O passo a passo está em [como adicionar um mob](docs/projeto/como-adicionar-um-mob.md). Para adaptar um animal do jogo, veja [animais vanilla](docs/projeto/animais-vanilla.md).

## Documentação

- [Índice da documentação](docs/README.md)
- [Visão geral do projeto](docs/projeto/visao-geral.md)
- [Como adicionar um mob](docs/projeto/como-adicionar-um-mob.md)
- [Genética: sexo, reprodução e variantes](docs/projeto/genetica.md)
- [Regeneração de vida](docs/projeto/regeneracao.md)
- [Sono](docs/projeto/sono.md)
- [Animais do jogo adaptados](docs/projeto/animais-vanilla.md)
- [Fichas dos mobs](docs/mobs/README.md)

## Licença

All Rights Reserved. Autor: Otavi.
