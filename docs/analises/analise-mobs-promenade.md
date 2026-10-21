# Análise técnica dos mobs do Promenade

> Fonte: `C:\Users\Otavi\Desktop\Promenade` (somente leitura). Caminhos relativos a essa raiz.
> Legenda: **[F]** fato confirmado no código · **[H]** hipótese · **[NV]** não verificável sem compilar/rodar.

---

## 1. Resumo da arquitetura

- **[F]** Projeto **multi-loader** em 3 subprojetos Gradle (`settings.gradle`):
  - `common/` — código e recursos independentes de loader, compilado só contra o Minecraft vanilla (NeoForm). Nunca gera jar próprio; suas fontes são compiladas dentro dos jars de cada loader.
  - `fabric/` — entrypoints Fabric, implementação da plataforma, **datagen** (`fabric/src/datagen`).
  - `neoforge/` — entrypoints NeoForge, implementação da plataforma, `PromenadeBiomeModifier`.
- **[F]** A ponte entre o código comum e o loader é a interface `fr.hugman.promenade.platform.PromenadePlatform`, carregada via `ServiceLoader` (`META-INF/services/fr.hugman.promenade.platform.PromenadePlatform` em cada loader). Ela expõe `registerAttributes`, `registerSpawnPlacement`, `addSpawn`, `registerEntityDataSerializer` etc.
- **[F]** Registros "simples" (tipos de entidade, itens, sons) são feitos diretamente com `Registry.register(BuiltInRegistries.X, …)` em classes com campos `static final`, inicializadas por `Reflection.initialize(...)` em `Promenade.init()`.
- **[F]** Registros que os loaders entregam por evento (renderizadores, camadas de modelo, partículas, cores) recebem um *registrar* funcional (ex.: `PromenadeEntityRenderers.Registrar`), passado pelo entrypoint cliente de cada loader.
- **[F]** Mapeamentos **Mojang** (nomes como `Animal`, `EntityType.Builder`, `ModelPart`, `Identifier`), Java **25**, Minecraft **26.3**.
- **[F]** Animações usam o sistema **vanilla** de keyframes (`AnimationDefinition`/`KeyframeAnimation`) — **sem GeckoLib** nem outra biblioteca de animação.
- **[F]** Modelos são definidos **em código Java** (`LayerDefinition`/`MeshDefinition`), não em JSON/Blockbench em runtime.
- **[F]** Arquivos de dados (lang, loot tables, tags, `sounds.json`, modelos de item) são **gerados por datagen Fabric** em `common/src/main/generated` (pasta não versionada — não existe no checkout).

## 2. Inventário de mobs

| Mob | Classe (common/src/main/java/fr/hugman/promenade/entity/) | Base | Categoria | IA | Renderer / modelo |
|---|---|---|---|---|---|
| Capybara | `Capybara.java` + `CapybaraAi.java` | `Animal` | `CREATURE` | **Brain** (sensores, atividades, behaviors próprios) + máquina de estados sincronizada | `CapybaraEntityRenderer` (`AgeableMobRenderer`), `AdultCapybaraModel`/`BabyCapybaraModel`, `CapybaraAnimations` (keyframes) |
| Duck | `Duck.java` | `Animal` | `CREATURE` | **Goals** clássicos | `DuckEntityRenderer` (`AgeableMobRenderer`), `AdultDuckModel`/`BabyDuckModel`, animação procedural em `setupAnim` |
| Lush Creeper | `LushCreeper.java` | `Creeper` (vanilla) | `MONSTER` | Herdada do Creeper | `LushCreeperRenderer` + `CreeperModel` vanilla + `CreeperOverlayRenderer` |
| Sunken | `Sunken.java` | `AbstractSkeleton` + `CrossbowAttackMob` | `MONSTER` | Goals + `MoveControl` aquático próprio | `SunkenEntityRenderer`, `SunkenEntityModel`, camadas de armadura |

Também há entidades não-mob: barcos (`EntityTypeFactory.boat/chestBoat`) que reutilizam `Boat`/`ChestBoat`/`BoatRenderer`/`BoatModel` vanilla.

**Implementação própria vs. reuso:**
- Própria: Capybara, Duck (modelo, renderer, IA e lógica próprios).
- Reuso forte do vanilla: Lush Creeper (classe, IA e modelo do Creeper), Sunken (base de esqueleto, modelo de armadura humanoide).

**Padrões compartilhados [F]:**
- Variantes **data-driven** (registros dinâmicos `DuckVariant`, `CapybaraVariant`, `SunkenVariant`) sincronizadas via `EntityDataAccessor<Holder<…>>` com serializador registrado em `PromenadeTrackedData`, escolhidas em `finalizeSpawn` (`DuckVariants.select(...)`) e salvas com `VariantUtils.writeVariant/readVariant`.
- Atributos em método estático `createXAttributes()` partindo de `createAnimalAttributes()` / `createAttributes()`.
- Peso de spawn configurável via `PromenadeConfig` (0 desativa o spawn).
- Spawn egg via `ItemFactory.spawnEgg(type)` → `new SpawnEggItem(props.spawnEgg(type))`, com textura própria (`textures/item/<mob>_spawn_egg.png`).
- Sons próprios registrados em `PromenadeSoundEvents`.

## 3. Análise detalhada: **Duck** (mob representativo)

Escolhido por ser o mob próprio mais simples e completo (goals clássicos, filhotes, reprodução, sons, loot, spawn natural).

### 3.1 Entidade — `common/src/main/java/fr/hugman/promenade/entity/Duck.java`
- `public class Duck extends Animal` (Animal → AgeableMob → PathfinderMob → Mob → LivingEntity).
- Construtor: `setPathfindingMalus(PathType.WATER, 0.0F)` (pato não evita água).
- **Atributos:**
  ```java
  public static Builder createDuckAttributes() {
      return createAnimalAttributes()
              .add(Attributes.MAX_HEALTH, 4.0D)
              .add(Attributes.MOVEMENT_SPEED, 0.25D);
  }
  ```
- **Goals** (`registerGoals`): `FloatGoal`(0), `PanicGoal`(1), `BreedGoal`(2), `TemptGoal` com tag `DUCK_FOOD`(3), `FollowParentGoal`(4), `RandomSwimmingGoal`(5), `WaterAvoidingRandomStrollGoal`(6), `LookAtPlayerGoal`(7), `RandomLookAroundGoal`(8).
- **Sobrescritas relevantes:** `aiStep` (física do bater de asas e queda lenta), `isFood`, `getWaterSlowDown`, sons (`getAmbientSound`, `getHurtSound`, `getDeathSound`, `playStepSound`), `getBreedOffspring` (cria filhote via `PromenadeEntityTypes.DUCK.create(level, EntitySpawnReason.BREEDING)` e herda variante), `getDefaultDimensions` (dimensões menores para bebê), `finalizeSpawn` (sorteia variante), `defineSynchedData`/`addAdditionalSaveData`/`readAdditionalSaveData` (variante), `get`/`applyImplicitComponent(s)` (componente de variante, usado por spawn eggs/baldes).
- **Interações:** jogador alimenta (tag) → reprodução; segue jogador com comida; foge quando ferido; flutua.

### 3.2 Registro — `entity/PromenadeEntityTypes.java`
```java
public static final EntityType<Duck> DUCK = register("duck", EntityType.Builder.of(Duck::new, MobCategory.CREATURE)
        .sized(8f /16, 10f /16)
        .eyeHeight(9.75f /16)
        .clientTrackingRange(10)
        .updateInterval(3));
```
`register` cria `ResourceKey<EntityType<?>>` e chama `Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type.build(key))`.
Atributos: `platform.registerAttributes(DUCK, Duck::createDuckAttributes)`.
Spawn placement: `platform.registerSpawnPlacement(DUCK, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules)`.
Spawn natural (`appendWorldGen`): biomas que já têm vaca+ovelha+galinha+porco, peso configurável, grupo 4–4.

### 3.3 Renderização (cliente) — `common/src/client/java/fr/hugman/promenade/client/render/entity/`
- `state/DuckRenderState extends LivingEntityRenderState` — campos `variant`, `flapProgress`, `maxWingDeviation` (padrão "render state" do MC 1.21.2+: o renderer copia dados da entidade para um objeto de estado; o modelo só lê o estado).
- `DuckEntityRenderer extends AgeableMobRenderer<Duck, DuckRenderState, DuckModel>` — recebe modelo adulto e bebê (`context.bakeLayer(...)`), raio de sombra 0.3; `createRenderState`, `extractRenderState` (interpola com `Mth.lerp`), `getTextureLocation` (textura da variante, ou `MissingTextureAtlasSprite`).
- `model/duck/DuckModel extends EntityModel<DuckRenderState>` — cabeça segue `state.xRot/yRot`, pernas com `Mth.cos(walkAnimationPos * 0.6662F) * 1.4F * walkAnimationSpeed`.
- `AdultDuckModel`/`BabyDuckModel` — `static LayerDefinition getTexturedModelData()` com `MeshDefinition`, `PartDefinition.addOrReplaceChild(PartNames.X, CubeListBuilder.create().texOffs(u,v).addBox(...), PartPose.offset(...))`, `LayerDefinition.create(mesh, 64, 64)`.
- `model/PromenadeEntityModelLayers` — `ModelLayerLocation DUCK = new ModelLayerLocation(Promenade.id("duck"), "main")` e `register(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>>)`.
- `PromenadeEntityRenderers.register(Registrar)` — `registrar.register(PromenadeEntityTypes.DUCK, DuckEntityRenderer::new)`.

### 3.4 Recursos
- Texturas: `common/src/client/resources/assets/promenade/textures/entity/duck/{pekin,mallard,duckling}.png`; spawn egg `textures/item/duck_spawn_egg.png`.
- Sons: `assets/promenade/sounds/entity/duck/...ogg`; eventos em `sound/PromenadeSoundEvents.java` (`of("entity.duck.ambient")`); `sounds.json` gerado por `PromenadeSoundsProvider`.
- Loot table: `PromenadeEntityLootTableProvider` (penas 0–2 + pato cru, cozido se em chamas, looting).
- Tags: `PromenadeEntityTypeTagProvider` (`FALL_DAMAGE_IMMUNE`, `FOLLOWABLE_FRIENDLY_MOBS`, tags convencionais `ANIMALS`, `BIRDS`).
- Lang: `PromenadeEnglishLangProvider` (nome da entidade, legendas `subtitles.promenade.entity.duck.*`).
- Modelo do spawn egg: `PromenadeModelProvider` → `generateFlatItem(DUCK_SPAWN_EGG, ModelTemplates.FLAT_ITEM)`.

## 4. Fluxo de inicialização

```
[Fabric] PromenadeFabric.onInitialize()            [NeoForge] new PromenadeNeoForge(modBus)
   │                                                   │  platform.subscribe(modBus)  ← guarda listeners de
   │                                                   │  EntityAttributeCreationEvent / RegisterSpawnPlacementsEvent
   │                                                   │  RegisterEvent (1ª vez) ──┐
   └──────────────► Promenade.init() ◄─────────────────┘───────────────────────────┘
                      ├─ Reflection.initialize(PromenadeSoundEvents)   → registra SoundEvents
                      ├─ Reflection.initialize(PromenadeItems)         → spawn eggs (referenciam EntityTypes → classe carrega)
                      ├─ Reflection.initialize(PromenadeTrackedData)   → serializadores de dados sincronizados
                      ├─ Reflection.initialize(PromenadeEntityTypes)   → Registry.register(ENTITY_TYPE, …)
                      ├─ PromenadeEntityTypes.registerAttributes()     → Fabric: FabricDefaultAttributeRegistry
                      │                                                  NeoForge: EntityAttributeCreationEvent
                      ├─ PromenadeEntityTypes.registerSpawnPlacements()→ Fabric: SpawnPlacements.register
                      │                                                  NeoForge: RegisterSpawnPlacementsEvent
                      └─ PromenadeEntityTypes.appendWorldGen()         → Fabric: BiomeModifications.addSpawn
                                                                         NeoForge: PromenadeBiomeModifier
[Cliente Fabric] PromenadeFabricClient.onInitializeClient()
   ├─ PromenadeEntityModelLayers.register(ModelLayerRegistry::registerModelLayer)
   └─ PromenadeEntityRenderers.register(EntityRenderers::register)
[Cliente NeoForge] PromenadeNeoForgeClient (Dist.CLIENT)
   ├─ EntityRenderersEvent.RegisterLayerDefinitions → PromenadeEntityModelLayers.register
   └─ EntityRenderersEvent.RegisterRenderers        → PromenadeEntityRenderers.register
```
Resultado: tipo registrado → atributos padrão → regras de spawn → spawn em biomas → modelo/renderer no cliente → `/summon promenade:duck` funciona.

## 5. Mapa de arquivos (recorte relevante)

```
Promenade/
├─ build.gradle / settings.gradle / gradle.properties
├─ common/
│  ├─ build.gradle                      (moddev + NeoForm, source sets main + client)
│  └─ src/
│     ├─ main/java/fr/hugman/promenade/
│     │  ├─ Promenade.java              (init())
│     │  ├─ platform/PromenadePlatform.java
│     │  ├─ entity/{Capybara,CapybaraAi,Duck,LushCreeper,Sunken,PromenadeEntityTypes}.java
│     │  ├─ entity/{ai/brain,data,helper,spawn,variant}/…
│     │  ├─ item/PromenadeItems.java, item/helper/ItemFactory.java
│     │  ├─ sound/PromenadeSoundEvents.java
│     │  └─ references/PromenadeEntityTypeIds.java
│     ├─ client/java/fr/hugman/promenade/client/render/entity/
│     │  ├─ PromenadeEntityRenderers.java
│     │  ├─ {Capybara,Duck,Sunken}EntityRenderer.java, LushCreeperRenderer.java
│     │  ├─ animation/CapybaraAnimations.java
│     │  ├─ model/PromenadeEntityModelLayers.java, model/{capybara,duck}/…
│     │  └─ state/{Capybara,Duck,Sunken}RenderState.java
│     └─ client/resources/assets/promenade/{textures/entity,textures/item,sounds/entity}/…
├─ fabric/
│  ├─ src/main/java/…/fabric/{PromenadeFabric,FabricPromenadePlatform}.java
│  ├─ src/client/java/…/fabric/client/PromenadeFabricClient.java
│  ├─ src/main/resources/fabric.mod.json, promenade.accesswidener, META-INF/services/…
│  └─ src/datagen/java/…/data/provider/{EntityLootTable,EntityTypeTag,EnglishLang,Sounds,Model}Provider…
└─ neoforge/
   ├─ src/main/java/…/neoforge/{PromenadeNeoForge,NeoForgePromenadePlatform,PromenadeBiomeModifier}.java
   ├─ src/main/java/…/neoforge/client/PromenadeNeoForgeClient.java
   └─ src/main/resources/META-INF/{neoforge.mods.toml,accesstransformer.cfg,services/…}
```

## 6. Dependências e ferramentas

| Item | Valor | Fonte |
|---|---|---|
| Minecraft | **26.3** | `gradle.properties` `minecraft_version` |
| Java | **25** (`options.release = 25`) | `build.gradle` |
| Gradle | **9.8.1** | `gradle/wrapper/gradle-wrapper.properties` |
| Fabric Loader | 0.19.5 | `gradle.properties` |
| Fabric Loom | 1.18-SNAPSHOT (`net.fabricmc.fabric-loom`) | `gradle.properties` |
| Fabric API | 0.161.0+26.3 | `gradle.properties` |
| NeoForge | 26.3.0.31-beta (ModDevGradle 2.0.147) | `gradle.properties` |
| NeoForm (common) | 26.3-1 | `gradle.properties` |
| Biolith | 3.8.0-beta.1 (biomas — irrelevante para mob) | `gradle.properties` |
| Mappings | Mojang (oficiais) | nomes de classe no código |
| Modelos/animações | vanilla (`LayerDefinition`, `AnimationDefinition`) — **sem GeckoLib** | imports |

APIs específicas usadas para entidades: Fabric `FabricDefaultAttributeRegistry`, `BiomeModifications.addSpawn`, `ModelLayerRegistry`, `FabricEntityDataRegistry`; NeoForge `EntityAttributeCreationEvent`, `RegisterSpawnPlacementsEvent`, `EntityRenderersEvent.*`, `BiomeModifier`.
Convenções da versão: `EntityType.Builder.build(ResourceKey)`, `Item.Properties.setId(key)` e `.spawnEgg(type)`, `ValueInput`/`ValueOutput` para NBT, `Identifier` (antigo `ResourceLocation`), render states.

## 7. Guia de implementação para um mob novo

1. **Entidade** em `entity/<Mob>.java` estendendo `Animal` (passivo/neutro) ou `Monster` (hostil). Atributos em `static createXAttributes()`, goals em `registerGoals()`, sons, `isFood`, `getBreedOffspring`, dimensões de bebê.
2. **Tipo** em `PromenadeEntityTypes` com `EntityType.Builder.of(X::new, MobCategory.…).sized(w,h).eyeHeight(e)`.
3. **Atributos + spawn placement** pelo loader (Fabric: `FabricDefaultAttributeRegistry.register` + `SpawnPlacements.register`).
4. **Spawn natural** via `BiomeModifications.addSpawn` (Fabric) com seletor de bioma/tag.
5. **Sons** em `SoundEvents` registrados + `sounds.json` + `.ogg` (ou reutilizar vanilla).
6. **Cliente:** `RenderState` → `Model` (adulto/bebê) com `LayerDefinition` → `ModelLayerLocation` → `Renderer` (`AgeableMobRenderer`) → registrar camadas e renderer no entrypoint cliente.
7. **Animações:** procedurais em `setupAnim` (Duck) ou `AnimationDefinition` keyframes + `AnimationState` (Capybara).
8. **Recursos:** textura `textures/entity/<mob>/<mob>.png`, spawn egg (item + modelo `items/` + textura), lang, loot table, tags.

## 8. Checklist de criação

- [ ] Classe da entidade (+ atributos, goals, sons, comida, reprodução)
- [ ] Registro do `EntityType`
- [ ] Registro de atributos padrão
- [ ] Spawn placement + spawn em biomas
- [ ] Item spawn egg (+ aba criativa)
- [ ] RenderState, Model(s), ModelLayerLocation, Renderer
- [ ] Registro de camada de modelo e renderer no cliente
- [ ] Textura da entidade e do spawn egg
- [ ] `assets/<mod>/items/<egg>.json` + `models/item/<egg>.json`
- [ ] `lang/en_us.json` e `lang/pt_br.json`
- [ ] `data/<mod>/loot_table/entities/<mob>.json`
- [ ] Tags (`item` comida; opcionalmente entity type)
- [ ] Sons (vanilla ou próprios + `sounds.json`)
- [ ] Entrypoints em `fabric.mod.json` / `neoforge.mods.toml`

## 9. Recomendações

- **Referência principal: Duck** — `Animal` + goals clássicos é a forma mais simples e robusta; os padrões de registro, renderer adulto/bebê e render state são diretamente reaproveitáveis.
- **Referência secundária: Capybara** — usar quando quiser animações keyframe (`AnimationDefinition` + `AnimationState`) ou estados sincronizados; o sistema de *Brain* é bem mais complexo e não é necessário para um mob comum.
- **Lush Creeper** mostra como reaproveitar um mob vanilla inteiro com pouquíssimo código.
- Para um projeto novo e pequeno, a camada `PromenadePlatform` (multi-loader) é opcional: se o alvo for só Fabric, chamar as APIs do Fabric diretamente elimina ~1/3 dos arquivos. Variantes data-driven também são opcionais para um mob com aparência única.
- **[NV]** Nada foi compilado ou executado nesta fase; não há JDK instalado na máquina (`java: command not found`).
