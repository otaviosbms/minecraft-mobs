# Nome do mob

> Modelo para a documentação de um mob. Copie para `docs/mobs/<nome-em-portugues>.md`, preencha todas as seções com os valores reais do código e adicione o mob no [índice](README.md). Use [elefante.md](elefante.md) como exemplo completo.

| | |
|---|---|
| ID | `mobmod:<id>` |
| Nome | English / Português |
| Inspiração | Animal real ou ideia |
| Categoria | `CREATURE` / `MONSTER` / `WATER_CREATURE` / ... |
| Classe base | `ModAnimal` → `Animal` |
| Temperamento | Passivo / Neutro / Hostil |
| Adicionado em | versão |

![Variantes](../imagens/<id>-variantes.png)

Descrição curta do mob e do seu papel no jogo.

## Arquivos

| O quê | Onde |
|---|---|
| Definição | `mob/<id>/<Nome>Mob.java` |
| Entidade | `mob/<id>/<Nome>.java` |
| Modelo e animações | `client/mob/<id>/<Nome>Model.java` |
| Renderer e render state | `client/mob/<id>/<Nome>Renderer.java`, `<Nome>RenderState.java` |
| Registro cliente | `client/mob/<id>/<Nome>Client.java` |
| Texturas | `src/client/resources/assets/mobmod/textures/entity/<id>/` |

## Atributos

| Atributo | Valor | Observação |
|---|---|---|
| Vida máxima | | |
| Velocidade de movimento | | |
| Dano de ataque | | |
| Outros | | |
| Escala | 1.0 fêmea / 1.15 macho | Bônus de macho de `ModAnimal` |

## Tamanho e hitbox

| | Largura × altura (blocos) | Altura dos olhos |
|---|---|---|
| Fêmea adulta | | |
| Macho adulto | | |
| Filhote | | |

## Comportamento e IA

### Goals (por prioridade)

| Prioridade | Goal | Detalhes |
|---|---|---|
| 0 | | |

### Alvos

| Prioridade | Goal | Detalhes |
|---|---|---|
| 1 | | |

### Ataque e habilidades especiais

## Reprodução

| | |
|---|---|
| Comida (tag `mobmod:<id>_food`) | |
| Quem pode acasalar | Somente macho + fêmea |
| Filhote | Sexo 50/50, variante herdada |

## Regeneração

Usa o sistema global de [regeneração](../projeto/regeneracao.md). Preencha com os valores do mob (vida máxima / 120 por segundo, a não ser que o mob sobrescreva `regenFullHealthSeconds()`):

| | |
|---|---|
| Por tempo | |
| Começa | 10 s depois do último dano, e só fora de combate (sem alvo) |
| Comida | Cada item da comida cura 10% da vida |

## Sexo e variantes

| | Fêmea | Macho |
|---|---|---|
| Tamanho | 100% | 115% |
| Diferenças visuais | | |

| Variante | Chance no spawn | Textura | Visual |
|---|---|---|---|
| Normal | 98% | `<id>.png` | |
| Albino | 1% | `<id>_albino.png` | |
| Melânico | 1% | `<id>_melanistic.png` | |

## Spawn natural

| | |
|---|---|
| Biomas | |
| Peso | |
| Tamanho do grupo | |
| Posicionamento | |
| Regras | |

## Drops

| Item | Quantidade | Pilhagem (Looting) |
|---|---|---|
| | | |

## Sons

| Evento | Som | Detalhe |
|---|---|---|
| Ambiente | | |
| Dano | | |
| Morte | | |
| Passos | | |

## Modelo

| Parte | Tamanho (L × A × C) | Observação |
|---|---|---|
| | | |

## Animações

| Animação | Como funciona |
|---|---|
| | |

## Itens e creative

| | |
|---|---|
| Ovo gerador | `mobmod:<id>_spawn_egg` |
| Aba criativa | |

## Traduções

| Chave | en_us | pt_br |
|---|---|---|
| `entity.mobmod.<id>` | | |

## Comandos úteis

```
/summon mobmod:<id>
```

## Testes automáticos

Coberto pelos gametests genéricos. Liste aqui testes específicos do mob, se houver.
