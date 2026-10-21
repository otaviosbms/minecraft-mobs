# Como adicionar um mob

Exemplo para um mob chamado `zebra`. Use o Elefante (`mob/elephant`) como referência completa.

## 1. Lado comum: `src/main/java/com/otavi/mobmod/mob/zebra/`

**`Zebra.java`**: a entidade. Animais estendem **`ModAnimal`**, que já traz sexo, variantes de pele e a reprodução macho + fêmea (veja [genetica.md](genetica.md)), além da regeneração de vida por tempo e por comida (veja [regeneracao.md](regeneracao.md)); não implemente `getBreedOffspring`. Defina `createZebraAttributes()` e `registerGoals()`. Goals reaproveitáveis ficam em `entity/goal/`.

**`ZebraMob.java`**: a definição, que é a única fonte de verdade do mob:

```java
public final class ZebraMob {
    public static final TagKey<Item> FOOD = TagKey.create(Registries.ITEM, MobMod.id("zebra_food"));

    public static final MobDefinition<Zebra> DEFINITION = MobDefinition.builder("zebra", Zebra::new, MobCategory.CREATURE)
            .type(type -> type.sized(1.4f, 1.6f).eyeHeight(1.4f))         // tamanho/hitbox
            .attributes(Zebra::createZebraAttributes)
            .spawn(SpawnRule.animal(BiomeTags.IS_SAVANNA, 8, 2, 6))         // biomas, peso, grupo min/max
            .name("Zebra", "Zebra")                                         // en_us, pt_br
            .food(FOOD, new Translation("Food for Zebras", "Comida para Zebras"), Items.WHEAT)
            .loot(context -> LootTable.lootTable() /* ... */)
            .build();

    public static final EntityType<Zebra> TYPE = DEFINITION.type();
}
```

Opcionais do builder: `spawnEggAfter(Items.X)` (posição na aba criativa; sem ele o ovo vai para o fim), `noSpawnEgg()`, e omitir `spawn`, `food` ou `loot` se o mob não tiver.

## 2. Lado cliente: `src/client/java/com/otavi/mobmod/client/mob/zebra/`

- `ZebraModel.java`: modelo em código (`createLayer()`), animações em `setupAnim`. O render state traz `sex` e `skinVariant` se o modelo precisar mudar algo por sexo (como as presas do elefante).
- `ZebraRenderState.java`: estende `ModMobRenderState`.
- `ZebraRenderer.java`: estende `ModMobRenderer`, que já escolhe a textura da variante e desenha a barra de informações (nome, vida e sexo).
- `ZebraClient.java`:

```java
public final class ZebraClient implements ClientMob {
    public static final ModelLayerLocation LAYER = ClientMob.layer("zebra");

    @Override
    public void register(Registrar registrar) {
        registrar.layer(LAYER, ZebraModel::createLayer);
        registrar.renderer(ZebraMob.TYPE, ZebraRenderer::new);
    }
}
```

## 3. Registrar nas duas listas

- `core/ModMobs.java`: adicionar `ZebraMob.DEFINITION` em `ALL`.
- `client/core/ModMobsClient.java`: adicionar `new ZebraClient()` em `ALL`.

São as **únicas** alterações fora das pastas do mob.

## 4. Texturas (`src/client/resources/assets/mobmod/textures/`)

- `entity/zebra/zebra.png`, `zebra_albino.png` e `zebra_melanistic.png` (as três variantes de pele são obrigatórias)
- `item/zebra_spawn_egg.png`

Para manter o estilo dos outros mobs, crie `tools/textures/ZebraTexture.java` estendendo `MobTexture` (veja `ChimpanzeeTexture.java`): ele gera as três variantes e o ovo.

## 5. Gerar dados e testar

```bash
./gradlew runDatagen
```
Gera lang, loot table, tag de comida e modelo do ovo em `src/main/generated`.

```bash
./gradlew runGameTestServer
```
Os testes já cobrem o mob novo: ele precisa nascer, sobreviver e ter loot table.

```bash
./gradlew runClient
```
Teste no jogo com `/summon mobmod:zebra` e o ovo na aba Spawn Eggs.

Commite também os arquivos gerados em `src/main/generated`.

## 6. Documentar

Copie [`docs/mobs/_modelo.md`](../mobs/_modelo.md) para `docs/mobs/<nome>.md`, preencha com os valores reais e adicione uma linha no [índice dos mobs](../mobs/README.md).
