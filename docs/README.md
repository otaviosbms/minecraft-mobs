# Documentação do Minecraft Mobs

Índice de toda a documentação do projeto. Novos documentos devem ser colocados na pasta da categoria correspondente e listados aqui.

| Pasta | Conteúdo |
|---|---|
| [`projeto/`](projeto/) | Documentação do próprio mod: estrutura, build, arquitetura |
| [`mobs/`](mobs/) | Ficha completa de cada mob (atributos, IA, spawn, drops, modelo...) |
| [`imagens/`](imagens/) | Imagens usadas na documentação |

## Documentos

- [projeto/visao-geral.md](projeto/visao-geral.md) — arquitetura, estrutura de pastas, datagen e build
- [projeto/como-adicionar-um-mob.md](projeto/como-adicionar-um-mob.md) — passo a passo para criar um mob novo
- [mobs/README.md](mobs/README.md) — índice dos mobs, com uma ficha por mob ([Elefante](mobs/elefante.md), [Chimpanzé](mobs/chimpanze.md), [Suricato](mobs/suricato.md), [Camelo](mobs/camelo.md), [Tatu](mobs/tatu.md))
- [projeto/genetica.md](projeto/genetica.md) — sexo, reprodução, variantes de pele e barra de informações
- [projeto/regeneracao.md](projeto/regeneracao.md) — regeneração de vida por tempo e por comida
- [projeto/animais-vanilla.md](projeto/animais-vanilla.md) — adaptar animais do jogo (camelo, tatu) às regras do mod: mixins, texturas geradas em tempo real, checklist
- [projeto/sono.md](projeto/sono.md) — sono à noite de todos os animais (pose de cada um, olhos fechados, "z Z z", quando acordam)

## Convenções

- Nomes de arquivo em `kebab-case`, em português, extensão `.md`.
- Um documento por assunto; ao criar um novo, adicione-o à lista acima.
