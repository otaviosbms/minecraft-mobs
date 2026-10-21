# Minecraft Mobs

Mod de mobs para Minecraft feito com **Fabric**. Atualmente adiciona o **Elefante**, um animal neutro das savanas.

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

- **Comportamento:** neutro. Vive em manadas e só ataca se for ferido ou se um filhote da manada for ferido. Filhotes não lutam: fogem em pânico e chamam os adultos.
- **Atributos:** 60 de vida, 8 de dano, resistência alta a knockback e sobe blocos de altura 1.
- **Spawn:** biomas de savana, em grupos de 2 a 4.
- **Reprodução:** alimentado com os itens da tag `mobmod:elephant_food`, que também o atraem.
- **Extras:** ovo de spawn na aba criativa (Spawn Eggs), loot table própria e traduções em inglês e português (BR).

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

Cada mob vive no próprio pacote e é declarado uma vez numa `MobDefinition`. Registro, spawn, ovo, traduções, loot, tags e testes saem dessa definição.

```
src/main/java/.../core/          MobDefinition, SpawnRule e ModMobs (lista de todos os mobs)
src/main/java/.../mob/<nome>/    entidade e definição de cada mob
src/client/java/.../core/        ClientMob e ModMobsClient (lado cliente de todos os mobs)
src/client/java/.../mob/<nome>/  modelo, renderer e registro cliente de cada mob
src/client/java/.../datagen/     providers do datagen
src/main/generated/              JSONs gerados pelo datagen (não editar à mão)
docs/                            documentação do projeto
```

Adicionar um mob é criar as duas pastas dele, colocar uma linha em `ModMobs` e outra em `ModMobsClient`, e rodar `runDatagen`. O passo a passo está em [como adicionar um mob](docs/projeto/como-adicionar-um-mob.md).

## Documentação

- [Índice da documentação](docs/README.md)
- [Visão geral do projeto](docs/projeto/visao-geral.md)
- [Como adicionar um mob](docs/projeto/como-adicionar-um-mob.md)
- [Análise dos mobs do Promenade](docs/analises/analise-mobs-promenade.md) (referência usada na criação do mod)

## Licença

All Rights Reserved. Autor: Otavi.
