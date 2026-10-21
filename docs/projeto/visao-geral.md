# Visão geral do projeto

Mod de mobs para Minecraft **26.3** usando **Fabric** (Loom `1.18-SNAPSHOT`, Fabric Loader `0.19.5`, Fabric API `0.161.0+26.3`), **Java 25**, mapeamentos Mojang. Hoje o mod adiciona um mob: o **Elefante** (`mobmod:elephant`), com ovo de spawn.

## Estrutura de pastas

```
minecraft-mobs/
├── build.gradle            Script de build (Loom, source sets, runs, Java 25)
├── gradle.properties       Versões e metadados (mod_id, version, Minecraft, Loader, Fabric API)
├── settings.gradle         Repositório de plugins (Fabric maven) e nome do projeto (mob-mod)
├── gradlew / gradlew.bat   Gradle Wrapper (usa Gradle 9.8.1)
├── gradle/wrapper/         Jar e propriedades do wrapper
├── docs/                   Esta documentação
├── src/
│   ├── main/               Código e recursos comuns (cliente + servidor)
│   └── client/             Código e recursos exclusivos do cliente
├── build/                  GERADO – classes, resources, libs/mobmod-0.1.0.jar, gametest
└── .gradle/                GERADO – cache do Gradle/Loom
```

`build/`, `.gradle/`, `run/`, `.idea/` etc. estão no `.gitignore`.

### `src/main` (lado comum)

| Caminho | Função |
|---|---|
| `java/com/otavi/mobmod/MobMod.java` | Entrypoint `main` (`ModInitializer`): inicializa registros, atributos, spawn placement, spawn em biomas e aba criativa |
| `java/.../entity/Elephant.java` | Classe do mob (`Animal`): atributos, goals de IA, ataque, sons |
| `java/.../entity/ModEntityTypes.java` | Registro do `EntityType`, atributos, regras de spawn e spawn em savanas (peso 5, grupo 2–4) |
| `java/.../item/ModItems.java` | Registro do ovo de spawn e inserção na aba de ovos criativa |
| `resources/fabric.mod.json` | Metadados do mod, entrypoints e dependências (`${version}` é substituído no build) |
| `resources/mobmod.accesswidener` | Abre `SpawnPlacements.register` (Fabric não tem evento para isso) |
| `resources/data/mobmod/loot_table/entities/elephant.json` | Drops do elefante |
| `resources/data/mobmod/tags/item/elephant_food.json` | Itens que atraem/alimentam o elefante |

### `src/client` (somente cliente)

Separado por `splitEnvironmentSourceSets()` para que código de cliente nunca vaze para o servidor.

| Caminho | Função |
|---|---|
| `java/.../client/MobModClient.java` | Entrypoint `client`: registra camadas de modelo e renderer |
| `java/.../render/entity/ElephantRenderer.java` | Renderer do elefante |
| `java/.../render/entity/model/ElephantModel.java` | Modelo 3D definido em código Java (sem Blockbench/GeckoLib) |
| `java/.../render/entity/model/ModModelLayers.java` | Registro das camadas de modelo |
| `java/.../render/entity/state/ElephantRenderState.java` | Estado de renderização (ex.: animação de ataque) |
| `resources/assets/mobmod/textures/` | Texturas do elefante e do ovo de spawn |
| `resources/assets/mobmod/items/`, `models/item/` | Definição/modelo do item do ovo |
| `resources/assets/mobmod/lang/` | Traduções `en_us.json` e `pt_br.json` |

## Build

Tudo roda pelo Gradle Wrapper (primeira execução baixa Gradle 9.8.1, Minecraft e dependências; precisa de JDK 25):

```bash
./gradlew build            # compila e gera build/libs/mobmod-0.1.0.jar
./gradlew runClient        # abre o Minecraft com o mod (pasta run/)
./gradlew runServer        # servidor de desenvolvimento
./gradlew runGameTestServer   # sobe servidor, roda gametests e sai (smoke test de carga do mod)
```

Fluxo do build:

1. Loom baixa e mapeia o Minecraft `26.3` (Mojang mappings) e aplica o access widener.
2. `processResources` expande `${version}` em `fabric.mod.json` com a `version` do `gradle.properties`.
3. Compila `main` e `client` (UTF-8, `--release 25`); ambos entram no mesmo mod `mobmod`.
4. O jar final sai em `build/libs/mobmod-<version>.jar` (nome vem de `base.archivesName = mod_id`).

Para trocar de versão do Minecraft/Fabric, edite apenas `gradle.properties` (as versões foram copiadas do projeto de referência Promenade).

## Como adicionar um novo mob

1. Classe da entidade em `entity/` + registro em `ModEntityTypes` (tipo, atributos, spawn placement, biomas).
2. Ovo de spawn em `ModItems`.
3. Cliente: modelo, render state, renderer, camada em `ModModelLayers` e registro em `MobModClient`.
4. Recursos: textura, lang (en/pt), loot table, tags, modelo do item.
