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
./gradlew runClient           # abre o Minecraft com o mod (pasta run/)
./gradlew runServer           # servidor de desenvolvimento
./gradlew runGameTestServer   # sobe um servidor, roda os gametests e sai
```

As versões do Minecraft, Loader e Fabric API ficam em [`gradle.properties`](gradle.properties).

## Estrutura

```
src/main/     código e recursos comuns (entidade, registros, loot table, tags, fabric.mod.json)
src/client/   código e recursos só do cliente (renderer, modelo, texturas, lang)
docs/         documentação do projeto
```

A explicação detalhada de cada pasta, do build e de como adicionar um novo mob está em [`docs/`](docs/README.md).

## Documentação

- [Índice da documentação](docs/README.md)
- [Visão geral do projeto](docs/projeto/visao-geral.md)
- [Análise dos mobs do Promenade](docs/analises/analise-mobs-promenade.md) (referência usada na criação do mod)

## Licença

All Rights Reserved. Autor: Otavi.
