# Visão geral do projeto

Mod de mobs para Minecraft **26.3** usando **Fabric** (Loom `1.18-SNAPSHOT`, Fabric Loader `0.19.5`, Fabric API `0.161.0+26.3`), **Java 25**, mapeamentos Mojang. Hoje o mod adiciona dois mobs, cada um com ovo de spawn: o **Elefante** (`mobmod:elephant`) e o **Chimpanzé** (`mobmod:chimpanzee`). As fichas completas estão em [docs/mobs](../mobs/README.md). Todos os animais têm sexo, variantes de pele (normal, albino, melânico) e uma barra de informações.

A arquitetura foi pensada para **dezenas de mobs**: cada mob vive no seu próprio pacote e é declarado uma única vez numa `MobDefinition`; o resto (registro, spawn, aba criativa, traduções, loot, tags, modelos de item e testes) é derivado dessa definição. Para o passo a passo, veja [como-adicionar-um-mob.md](como-adicionar-um-mob.md).

## Estrutura de pastas

```
minecraft-mobs/
├── build.gradle            Script de build (Loom, source sets, datagen, runs, Java 25)
├── gradle.properties       Versões e metadados (mod_id, version, Minecraft, Loader, Fabric API)
├── settings.gradle         Repositório de plugins (Fabric maven) e nome do projeto (mob-mod)
├── gradlew / gradlew.bat   Gradle Wrapper (usa Gradle 9.8.1)
├── gradle/wrapper/         Jar e propriedades do wrapper
├── docs/                   Esta documentação
├── src/
│   ├── main/
│   │   ├── java/           Código comum (cliente + servidor)
│   │   ├── resources/      fabric.mod.json e access widener (escritos à mão)
│   │   └── generated/      GERADO PELO DATAGEN e versionado: lang, loot tables, tags, modelos de item
│   └── client/
│       ├── java/           Código só do cliente (renderers, modelos, datagen)
│       └── resources/      Texturas (únicos assets escritos à mão)
├── tools/textures/         Geradores das texturas (MobTexture + um gerador por mob)
├── build/                  GERADO – classes, libs/mobmod-<versão>.jar, gametest
└── .gradle/                GERADO – cache do Gradle/Loom
```

## Código comum (`src/main/java/com/otavi/mobmod`)

| Caminho | Função |
|---|---|
| `MobMod.java` | Entrypoint `main`. Só chama `ModMobs.init()` |
| `core/MobDefinition.java` | Descreve um mob: tipo de entidade, atributos, regra de spawn, ovo, nome, comida e loot. O `build()` já registra o `EntityType` e o ovo |
| `core/ModMobs.java` | **Lista de todos os mobs.** `init()` registra atributos, spawn placements, spawn em biomas e os ovos na aba criativa, em loop |
| `core/SpawnRule.java` | Onde e como o mob nasce (biomas, peso, grupo, placement). `SpawnRule.animal(...)` cobre animais terrestres |
| `core/Translation.java` | Texto em todos os idiomas do mod (`en_us`, `pt_br`) |
| `core/MobLoot.java` | Função que monta a loot table do mob (usada só pelo datagen) |
| `core/ModItems.java` | Itens que não são ovos (ex.: coco), com nome, aba criativa e modelo gerados pelo datagen |
| `core/ModEntityTypes.java` | Entidades que não são mobs (ex.: o projétil do coco) |
| `item/<nome>/` | Um pacote por item com comportamento próprio (ex.: `item/coconut/`: item e projétil) |
| `entity/genetics/` | Sistema global de genética: `Sex`, `SkinVariant`, `GeneticMob` e `ModAnimal` (base dos animais, com reprodução macho + fêmea). Veja [genetica.md](genetica.md) |
| `entity/goal/` | Goals de IA reaproveitáveis por vários mobs (`AdultMeleeAttackGoal`, `BabyPanicGoal`) |
| `mob/<nome>/` | Um pacote por mob: a classe da entidade e o `<Nome>Mob` com a `MobDefinition` |
| `gametest/MobGameTests.java` | Testes que percorrem `ModMobs.ALL`: spawn, loot table, tamanho do macho, acasalamento só entre sexos opostos e herança de variante |

## Código do cliente (`src/client/java/com/otavi/mobmod/client`)

Separado por `splitEnvironmentSourceSets()` para que código de cliente nunca vaze para o servidor.

| Caminho | Função |
|---|---|
| `MobModClient.java` | Entrypoint `client`. Só chama `ModMobsClient.init()` |
| `core/ClientMob.java` | Interface do lado cliente de um mob: registra camadas de modelo e renderer |
| `core/ModMobsClient.java` | **Lista do lado cliente de todos os mobs**, registrada em loop |
| `core/ModMobRenderer.java`, `ModMobRenderState.java` | Base de todos os renderers: textura por variante de pele e dados de sexo/vida |
| `core/MobInfoBar.java` | Barra estilo Mo'Creatures acima do mob: nome, sexo e vida |
| `mob/<nome>/` | Modelo (em código Java), render state, renderer e `<Nome>Client` |
| `datagen/` | Entrypoint `fabric-datagen` e providers que geram JSON a partir de `ModMobs.ALL` |

## Datagen

`./gradlew runDatagen` abre o jogo em modo de geração de dados e escreve em `src/main/generated`:

| Provider | Gera | A partir de |
|---|---|---|
| `ModLanguageProvider` | `assets/mobmod/lang/en_us.json`, `pt_br.json` | nome do mob, nome do ovo (automático), nome da tag de comida, nomes dos itens de `ModItems` |
| `ModEntityLootProvider` | `data/mobmod/loot_table/entities/<mob>.json` | `MobDefinition.loot(...)` |
| `ModItemTagProvider` | `data/mobmod/tags/item/<mob>_food.json` | `MobDefinition.food(...)` |
| `ModModelProvider` | `assets/mobmod/items/` e `models/item/` dos ovos e dos itens | ovo de cada mob e `ModItems.ALL` |

A pasta `generated` é **versionada** (só o `.cache/` é ignorado), então o projeto compila sem rodar o datagen. Mesmo assim, **rode `runDatagen` sempre que mudar uma `MobDefinition`** e commite o resultado. Arquivos de mobs removidos são apagados automaticamente.

Não edite nada dentro de `src/main/generated` à mão: será sobrescrito.

## Build

Tudo roda pelo Gradle Wrapper (a primeira execução baixa Gradle 9.8.1, Minecraft e dependências; precisa de JDK 25):

```bash
./gradlew build               # compila e gera build/libs/mobmod-0.1.0.jar
./gradlew runDatagen          # regenera src/main/generated
./gradlew runClient           # abre o Minecraft com o mod (pasta run/)
./gradlew runServer           # servidor de desenvolvimento
./gradlew runGameTestServer   # sobe um servidor, roda os gametests e sai
```

Fluxo do build:

1. Loom baixa e mapeia o Minecraft `26.3` (Mojang mappings) e aplica o access widener.
2. `processResources` junta `src/main/resources` e `src/main/generated`, e expande `${version}` em `fabric.mod.json`.
3. Compila `main` e `client` (UTF-8, `--release 25`); ambos entram no mesmo mod `mobmod`.
4. O jar final sai em `build/libs/mobmod-<version>.jar`.

O jar inclui as classes de datagen e de gametest, mas elas só rodam quando o Fabric é iniciado nesses modos (`runDatagen` / `runGameTestServer`).

Para trocar de versão do Minecraft/Fabric, edite apenas `gradle.properties`.

## Texturas

As texturas são geradas por código em `tools/textures/`, todas no mesmo estilo artístico:

| Arquivo | Função |
|---|---|
| `MobTexture.java` | Base comum: ruído em duas camadas, estrias, rugas em espiral, faces de cima mais claras e de baixo mais escuras, uma paleta por variante e o ovo de spawn |
| `ElephantTexture.java` | Elefante (256×256) |
| `ChimpanzeeTexture.java` | Chimpanzé (256×256, modelo em detalhe dobrado) e o ovo dele |
| `CoconutTexture.java` | Item coco (16×16) |

Cada gerador documenta no topo o layout UV do modelo e grava `<mob>.png`, `<mob>_albino.png` e `<mob>_melanistic.png` com o mesmo desenho e paletas diferentes. Rode da raiz do projeto, passando a pasta de texturas:

```bash
java tools/textures/ChimpanzeeTexture.java src/client/resources/assets/mobmod/textures
```

Qualquer PNG pode ser trocado por arte feita à mão (ex.: Blockbench), desde que mantenha o layout UV.
