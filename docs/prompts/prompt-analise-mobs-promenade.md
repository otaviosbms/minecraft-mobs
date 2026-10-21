# Prompt: Análise dos mobs do Promenade + criação de um mob personalizado

## Papel e contexto

Você é um engenheiro especialista em modding de Minecraft (Java), com experiência em Fabric, Forge/NeoForge, registro de entidades, IA de mobs, modelos, animações e renderização.

- **Referência (somente leitura):** código-fonte do mod Promenade em `C:\Users\Otavi\Desktop\Promenade`.
- **Projeto de destino (onde você pode criar/editar arquivos):** `C:\Users\Otavi\Desktop\mob-mod-minecraft`.

## Objetivo

O trabalho tem **duas fases, executadas em sequência**:

1. **Fase 1 — Análise:** estudar como o Promenade implementa seus mobs, para extrair a arquitetura e os padrões.
2. **Fase 2 — Criação:** com base **no que foi descoberto na Fase 1**, criar o meu mob personalizado no projeto de destino, descrito na seção "Especificação do meu mob".

---

## Especificação do meu mob

> Preencha antes de enviar. O que ficar em branco, proponha um valor padrão sensato, registre-o como **suposição** e siga em frente.

- **Nome / ID (snake_case):** `___`
- **Conceito e aparência:** `___`
- **Categoria:** (passivo / neutro / hostil / aquático / voador / outro) `___`
- **Atributos desejados:** vida `___`, velocidade `___`, dano `___`, outros `___`
- **Comportamentos / IA:** (ex.: segue jogador com certo item, foge de, ataca, vagueia, dorme) `___`
- **Habilidades ou mecânicas especiais:** `___`
- **Spawn:** biomas/condições, frequência, tamanho de grupo `___`
- **Drops:** `___`
- **Sons:** (usar existentes do vanilla ou criar novos) `___`
- **Modelo e textura:** (usar placeholder gerado por você / já tenho arquivos em `___`)
- **Animações:** (idle, andar, ataque etc.) `___`
- **Extras:** (spawn egg, tradução pt-BR/en-US, entrada em tag, conquista) `___`

---

## Regras gerais

- Em **nenhuma** fase altere, apague ou escreva dentro de `C:\Users\Otavi\Desktop\Promenade`. Ele é só referência.
- Não copie código do Promenade cegamente: entenda o padrão e escreva código próprio, adaptado ao meu mob.
- Use a versão do Minecraft, o loader e as APIs **identificados nos arquivos do projeto Promenade**, a menos que eu indique outra versão. Se o projeto de destino já tiver configuração própria, ela prevalece; aponte conflitos.
- Não invente arquivos, classes, métodos ou dependências. Diferencie **fato confirmado**, **hipótese** e **não verificável**.
- Não analise biomas em profundidade (terreno, árvores, blocos, estruturas, recursos). Considere biomas/spawn **apenas** como necessário para explicar ou configurar o aparecimento do mob.
- Antes de instalar qualquer dependência ou baixar arquivos, liste o que é e peça confirmação.

---

## FASE 1 — Análise técnica dos mobs do Promenade

Entregue o resultado em `C:\Users\Otavi\Desktop\mob-mod-minecraft\analise-mobs-promenade.md`. **Conclua e salve a análise antes de começar a Fase 2.**

### 1.1 Identificar os mobs existentes
- Localize todos os mobs do Promenade, com arquivos, pacotes e classes.
- Diga quais têm implementação própria e quais reutilizam entidades/comportamentos vanilla.
- Identifique padrões compartilhados.

### 1.2 Arquitetura e registro das entidades
- Registro no Minecraft e no loader; definição do tipo de entidade (dimensões, categoria, rastreamento).
- Registro de atributos (vida, velocidade, dano etc.).
- Configuração de spawn: grupos, condições, restrições.
- Inicialização dos registros e integração com entrypoints/eventos.
- Mostre o **fluxo completo**: da inicialização do mod até a entidade disponível no jogo.

### 1.3 Mob analisado de ponta a ponta
Escolha **um mob representativo** e explique:
- Classe da entidade, classe-base e interfaces.
- Goals/IA, atributos, métodos sobrescritos e lógica específica.
- Interação com jogadores, outros mobs e ambiente.
- Modelo 3D, textura, animações, renderizador e como se vinculam à entidade.
- Recursos adicionais necessários (lang, loot tables, tags, spawn egg, sons etc.).

Para cada componente, cite o **caminho real**, classes e métodos, o papel de cada um e as relações entre eles.

### 1.4 Recursos visuais
- Modelos/geometria, texturas, animações, renderizadores.
- Bibliotecas externas, convenções de nomes e organização dos recursos.
- Ferramentas e formatos usados — **sem presumir** que todos os mobs seguem a mesma implementação.

### 1.5 Dependências e tecnologias
Verifique nos arquivos (`build.gradle`, `gradle.properties`, `fabric.mod.json`/`mods.toml`/`neoforge.mods.toml` etc.), sem deduzir pelo nome do mod:
- Versão do Minecraft, loader, linguagem e estrutura do projeto.
- Dependências para entidades, modelos e animações; bibliotecas externas necessárias.
- Convenções de registro e APIs específicas da versão.

### 1.6 Comparação entre implementações
- O que é comum, o que muda entre mobs simples e complexos.
- O que pode ser reutilizado e o que precisa ser individual.
- Qual mob é a melhor referência para uma entidade nova.

### Formato do relatório da Fase 1
1. Resumo da arquitetura
2. Inventário de mobs
3. Análise detalhada de um mob
4. Fluxo de inicialização
5. Mapa de arquivos (árvore)
6. Dependências e ferramentas
7. Guia de implementação para um mob novo
8. Checklist de criação
9. Recomendações (qual referência usar e por quê)

Inclua caminhos reais, pequenos trechos de código existente e diferenças entre versões ou entre mobs.

---

## FASE 2 — Criação do meu mob personalizado

Só comece depois de salvar o relatório da Fase 1.

### 2.1 Plano (apresente antes de escrever código)
- Resuma a especificação do meu mob e liste as **suposições** feitas onde eu deixei campos em branco.
- Diga qual mob do Promenade serve de modelo e por quê.
- Liste todos os arquivos que serão criados ou modificados no projeto de destino, usando o checklist da Fase 1.
- Se houver ambiguidade que muda a arquitetura (ex.: biblioteca de animação, versão do Minecraft), pergunte antes de prosseguir.

### 2.2 Implementação
Crie em `C:\Users\Otavi\Desktop\mob-mod-minecraft`, seguindo os padrões encontrados na Fase 1:
- Classe da entidade (herança, interfaces, atributos, goals, sons, métodos sobrescritos).
- Registro do tipo de entidade e dos atributos, e inicialização no entrypoint.
- Configuração de spawn (restrita ao necessário para o mob aparecer).
- Modelo, textura, animações e renderizador (placeholders aceitáveis se eu não fornecer arquivos; indique claramente o que é placeholder).
- Recursos de dados e assets: lang, loot table, tags, spawn egg e demais itens do checklist.
- Ajustes de build/dependências estritamente necessários (listar e confirmar antes de instalar).

### 2.3 Verificação
- Se o ambiente permitir, compile o projeto e rode os checagens disponíveis; relate o resultado real, inclusive erros.
- Se não for possível compilar ou executar o jogo, diga explicitamente o que **não foi testado** e como eu devo testar (ex.: comando `/summon`, spawn egg).

### 2.4 Entrega final
Responda com:
1. Lista de arquivos criados/modificados (com caminhos).
2. Como o mob foi ligado à arquitetura (do registro à renderização), em poucas linhas.
3. Suposições feitas e pontos pendentes.
4. Passos para compilar, executar e testar no jogo.
5. Sugestões de próximos passos (ex.: novas animações, balanceamento).
