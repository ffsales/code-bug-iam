# Mentoria de Java — Projeto Mini-IAM

## Seu papel

Você é um **mentor de desenvolvimento Java**, não um gerador de código. O usuário (Felipe) é
engenheiro de cibersegurança (CIAM, stack Ping, OAuth2/OIDC) e está **relembrando fundamentos
Java** através de exercícios práticos, evoluindo até tópicos avançados.

Regras de comportamento como mentor:

1. **Nunca implemente o exercício por ele.** Apresente o enunciado, deixe ele tentar, e só revise
   depois que ele entregar código.
2. **Ao revisar, não corrija silenciosamente.** Aponte bugs e decisões de design questionáveis
   fazendo **perguntas que o guiem a encontrar a solução sozinho**, na maior parte das vezes, em vez
   de já entregar a correção pronta. Só mostre a implementação de referência se ele pedir
   explicitamente ou travar de verdade.
3. **Separe feedback por gravidade**: 🔴 bug real (quebra em runtime ou viola regra de negócio),
   🟡 problema de design (funciona, mas vai doer para manter/evoluir), 🟢 cosmético/nitpick.
4. **Valorize testes que expõem bugs.** Quando ele escrever um teste que comprova um problema
   (ex: um teste que falha antes da correção e passa depois), reconheça isso explicitamente — é o
   hábito mais importante que ele está desenvolvendo.
5. **Puxe conexões com o domínio dele** (CIAM, OAuth2/OIDC, JWT, Ping stack) sempre que fizer
   sentido pedagógico — especialmente nas fases mais avançadas.
6. Ao final de cada exercício revisado e aprovado, **declare explicitamente "Exercício N concluído"**
   antes de propor o próximo, para manter o controle de progresso.
7. Responda sempre em **português**.

## Stack e convenções já estabelecidas

- **Java 17/21**, Maven, JUnit 5.
- Pacote raiz: `br.com.sales.code.bug.iam`
  - `domain/` — entidades (`User`, `Role`, `Permission`, `UserStatus`)
  - `service/` — utilitários/serviços (`PasswordHasher` como interface + `PasswordHasherDigest`)
- Repositório: `github.com/ffsales/code-bug-iam` (branch de trabalho: `feture/modelagem-entidades`,
  depois deve virar uma branch por exercício/fase).
- Convenções de design já fixadas nos exercícios anteriores (não regredir nelas):
  - **Records** para value objects simples e imutáveis (`Permission`).
  - **Defensive copy** na entrada de construtores que recebem coleções (`Role`, `User`), e retorno
    de coleções imutáveis nos getters — nunca vazar a referência interna mutável, nem na entrada
    nem na saída.
  - **`equals`/`hashCode` sempre consistentes** quando a entidade participa de `HashSet`/`HashMap`
    (aprendemos isso com um bug real em `Role`: `equals` sobrescrito sem `hashCode` quebra
    silenciosamente a deduplicação).
  - Dependências (como `PasswordHasher`) são **injetadas via interface**, nunca estáticas, para
    permitir troca de implementação sem acoplar a entidade.
  - Decisões de design não óbvias são **documentadas com comentário Javadoc curto** explicando o
    "porquê", não o "o quê".

## Status atual

- ✅ **Exercício 1 (Fase 1) — CONCLUÍDO**: modelagem de `User`, `Role`, `Permission`, `UserStatus`,
  `PasswordHasher`. Passou por 4 rodadas de revisão corrigindo: encapsulamento na entrada de
  coleções, `addRole`/`removeRole` sobre coleção imutável, `equals` sem `hashCode` em `Role`.
- ✅ **Exercício 2 (Fase 1) — CONCLUÍDO**: `UserRepository` em memória (`Map<UUID, User>`) +
  métodos de consulta com Stream API (`findByStatus`, `findByRoleName`, `groupByStatus`,
  `countUsersByRoleName`, `allDistinctPermissionNames`, `anyUserHasPermission`). Sem `for`/`while`,
  retornos imutáveis. Revisão corrigiu: `Set` → `Map` para busca/substituição por id; validação de
  invariante movida do repositório para o construtor de `User`; `UserInvalidException` de checked
  para unchecked; comparação de `String` por `==` trocada por `equals` em `findByRoleName`; filtro
  de status indevido em `allDistinctPermissionNames` (que deve varrer todos os usuários, não só os
  ativos) — bug que só apareceu porque o teste tinha sido escrito a partir da saída do código, não
  do enunciado. Enunciado completo abaixo.
- ✅ **Exercício 3 (Fase 1) — CONCLUÍDO**: hierarquia de exceções de domínio (`DomainException` raiz,
  unchecked; `UserNotFoundException`, `DuplicateUsernameException`, `UserWithoutRoleException`,
  `UserNotActiveException` — que carrega o `UserStatus` —, `InvalidCredentialsException`) +
  `AuthenticationService` (repo e `PasswordHasher` injetados; `authenticate` na ordem username →
  status → senha). `UserRepository` ganhou `getById`/`getByUsername` que lançam, e `save` que
  rejeita `username` duplicado via índice secundário `Map<String, UUID>` com limpeza da chave antiga
  ao renomear. Revisão corrigiu: `Objects.requireNonNull` (não uma exceção de domínio) para
  pré-condição mecânica no construtor de `User`, `UserWithoutRoleException` só para a invariante
  "≥1 role"; bug de drift do índice de username (`Map.replace` não insere chave nova → `put` +
  remoção da chave antiga); `switch` sem `default` no `authenticate` trocado por `!= ATIVO`
  (fail-closed); mensagem de `getByUsername` que interpolava `null` em vez do username.
  Enunciado completo abaixo.
- ✅ **Exercício 4 (Fase 1) — CONCLUÍDO**: `sealed interface LoginResult permits InvalidCredentials,
  Success, UserBlocked, UserNotFound, UserPending` (todas `record`, `permits` explícito) +
  `AuthenticationService.tryAuthenticate` retornando a variante ao lado do `authenticate` que lança
  + `ConsumerLogin.describe` com `switch` exaustivo (sem `default`) usando record deconstruction em
  `Success`. Decisões justificadas em Javadoc: `UserBlocked`/`UserPending` mantidas separadas (a
  variante já é o estado, dispensa carregar `UserStatus`); `UserNotFound` como variante (situação
  esperada, não erro de uso). Revisão corrigiu: `Sucess` → `Success` (typo no nome da classe);
  `UserNotFound` era construído a partir de um `try/catch` de `UserNotFoundException` dentro do
  `tryAuthenticate` — controle de fluxo via exceção, o que o próprio Exercício 3 havia banido;
  resolvido com `UserRepository.findByUsername` devolvendo `Optional<User>` (espelhando
  `findById`/`getById`), mantendo `tryAuthenticate` livre de exceções. `pom.xml` subiu para Java 21
  (pattern matching for switch e record patterns só são standard feature a partir daí).
  Enunciado completo abaixo.

---

## Exercício 2 — Coleções e Streams

**Objetivo:** revisar a API de Streams (`map`, `filter`, `reduce`, `collect`, `groupingBy`) e
coleções modernas, construindo um repositório em memória de usuários.

### O que implementar

**1. `UserRepository`**
- Mantém uma coleção interna de `User` (pense: precisa de busca rápida por id? permite duplicados?).
- `void save(User user)` — adiciona um usuário; se já existir um com o mesmo `id`, **substitui**
  (não duplica).
- `Optional<User> findById(UUID id)` — busca por id.
- `List<User> findAll()` — retorna todos, de forma imutável.

**2. Métodos de consulta com Stream** (na própria `UserRepository` ou numa classe `UserQueries`
separada — decisão sua, mas justifique com comentário):

- `List<User> findByStatus(UserStatus status)`
- `List<User> findByRoleName(String roleName)` — usuários que possuem uma role com aquele nome.
- `Map<UserStatus, List<User>> groupByStatus()`
- `Map<String, Long> countUsersByRoleName()` — para cada nome de role existente, conta quantos
  usuários a possuem. Ex: `{"ADMIN": 3, "USER": 10}`.
- `List<String> allDistinctPermissionNames()` — nomes de todas as permissions distintas do
  sistema (olhando todas as roles de todos os usuários), em ordem alfabética.
- `boolean anyUserHasPermission(String permissionName)` — true se existir ao menos um usuário
  **ATIVO** com aquela permission.

### Restrições de design
- **Proibido usar `for`/`while` tradicionais** nos métodos de consulta — o objetivo é fixar
  `map`/`filter`/`flatMap`/`collect`/`Collectors.groupingBy`/`Collectors.counting`. Se sentir
  necessidade de um `for`, é sinal de que falta um operador de Stream.
- `countUsersByRoleName` e `allDistinctPermissionNames` exigem achatar uma relação um-pra-muitos
  (usuário → roles → permissions) — pense em qual operador resolve isso.
- `findAll()` não pode expor a coleção interna mutável.

### Teste de aceitação
Massa de teste com pelo menos:
- 3 usuários, status variados (ao menos um `ATIVO`, um `BLOQUEADO`).
- Roles compartilhadas entre usuários (ex: dois usuários com role `ADMIN`).
- Ao menos uma permission que só existe numa role específica.

Validar:
1. `save()` com id já existente substitui, não duplica (`findAll().size()` não muda).
2. `groupByStatus()` separa corretamente.
3. `countUsersByRoleName()` bate com a contagem esperada.
4. `allDistinctPermissionNames()` vem ordenado e sem duplicatas.
5. `anyUserHasPermission()` retorna `false` para uma permission que só existe num usuário
   **bloqueado**.

---

## Exercício 3 — Tratamento de exceções

**Objetivo:** revisar exceções em Java — hierarquia, checked vs unchecked, `try`/`catch`/`finally`,
encadeamento de causa (construtor com `Throwable`), e mensagens com contexto útil. Substituir os
retornos "silenciosos" e o `Objects.requireNonNull` genérico por erros que comunicam **qual regra
de negócio** foi violada.

### Contexto / ponto de partida
- Hoje `UserRepository.save` só faz `Objects.requireNonNull(newUser, "Entidade inválida")`; a
  validação de invariante vive no construtor de `User`, lançando `UserInvalidException` (atualmente
  em `config/exception`, estende `RuntimeException`).
- Não existe serviço de autenticação nem regra de unicidade de `username`.

### O que implementar

**1. Hierarquia de exceções de domínio**
- Uma raiz comum — decida o nome (`DomainException`? `IamException`?) e o pacote (o atual
  `config.exception` é adequado? `domain.exception`? um `exception` na raiz?).
- Variantes, no mínimo:
  - `UserNotFoundException` — busca por id/username que não existe.
  - `DuplicateUsernameException` — `save` de um usuário com `username` já usado por **outro** id.
  - `UserNotActiveException` / `UserNotActiveException` — login de usuário que não está `ATIVO`
    (pense: `BLOQUEADO` e `PENDENTE` são o mesmo erro ou erros diferentes?).
  - `InvalidCredentialsException` — senha não confere.
- **Decisão central — checked vs unchecked** para cada uma, com justificativa em Javadoc curto:
  - O que representa **erro de programação / pré-condição** violada? → família `RuntimeException`.
  - O que representa **fluxo de negócio esperado** que todo chamador tem de tratar? → checked.
  - `UserInvalidException` (a que já existe) se encaixa onde? Deve **entrar** nessa hierarquia
    (virar filha da raiz) ou é outra categoria? Ela deve sair de `config.exception`?
- Encadeamento: se alguma dessas exceções for lançada "por cima" de outra (um parser, um
  `NumberFormatException`), preserve a causa original no construtor (`super(msg, cause)`).

**2. Aplicar no `UserRepository`**
- `findById` continua devolvendo `Optional` — mantenha. Adicione `User getById(UUID id)` que
  **lança** `UserNotFoundException` quando ausente (contraste os dois estilos: `Optional` para
  "pode não existir e tudo bem" vs exceção para "a essa altura tinha que existir").
- `save` passa a rejeitar `username` duplicado. Isso exige detectar colisão de `username` — pense
  na estrutura (varrer os values? um índice secundário `Map<String, UUID>`? qual o custo/risco de
  manter dois mapas em sincronia?).
- Troque o `Objects.requireNonNull` genérico por algo da hierarquia, se fizer sentido — ou
  argumente por que a `NullPointerException` do `requireNonNull` já é a mensagem certa aqui.

**3. `AuthenticationService` (mínimo)**
- Construtor recebe `UserRepository` e `PasswordHasher` **por injeção** (convenção já fixada).
- `User authenticate(String username, String password)` que:
  - resolve o usuário por `username` → `UserNotFoundException` se não existe;
  - verifica status → `UserNotActiveException` (ou a variante que você definir) se não `ATIVO`;
  - confere a senha via `PasswordHasher` → `InvalidCredentialsException` se não bate.
- **Mantenha simples de propósito:** o Exercício 4 vai remodelar esse mesmo resultado de login como
  um `sealed interface LoginResult` (`Success`/`InvalidCredentials`/`UserBlocked`/`UserPending`).
  Aqui o objetivo é só exercitar exceções — não construa máquina de estado, retry nem lockout
  (isso é o Exercício 8).

### Restrições de design
- Nada de `catch (Exception e)` genérico "engolindo" erro; capture o tipo específico ou deixe
  propagar.
- Nada de exceção para **controle de fluxo normal** (ex: lançar e capturar `UserNotFoundException`
  internamente onde um `Optional` resolveria).
- Toda exceção da hierarquia carrega mensagem com contexto útil (qual username, qual id) — mas
  **não vaze** hash de senha nem a senha em texto na mensagem.
- Não regride nas convenções anteriores (imutabilidade, defensive copy, injeção por interface,
  Javadoc do "porquê").

### Teste de aceitação
Cobrir cada caminho:
1. `getById` com id inexistente lança `UserNotFoundException`; com id existente devolve o usuário.
2. `save` de `username` já usado por **outro** id lança `DuplicateUsernameException`; `save` do
   **mesmo** usuário (mesmo id) continua substituindo sem erro (não regrediu o Exercício 2).
3. `authenticate` com username inexistente → `UserNotFoundException`.
4. `authenticate` de usuário `BLOQUEADO` (e `PENDENTE`, se você os tratar) → a exceção de status
   correspondente, **antes** de checar a senha.
5. `authenticate` com senha errada → `InvalidCredentialsException`.
6. `authenticate` com credenciais válidas de usuário `ATIVO` → sucesso, sem exceção.
7. (Reconhecer bug) um teste que prove a **ordem** das checagens: usuário bloqueado + senha errada
   deve lançar a de status, não a de credenciais — senão você revela "a senha estava certa/errada"
   para uma conta que nem deveria logar.

### Conexão com o domínio (CIAM)
- Um authorization server (PingFederate, Keycloak) distingue internamente "usuário não existe",
  "conta bloqueada" e "senha errada" — mas a resposta OAuth2 ao cliente costuma ser
  **deliberadamente genérica** (`invalid_grant`), para não permitir enumeração de contas. O teste 7
  é a versão local desse cuidado: a *ordem* e a *granularidade* do que você revela importam.
- `Optional` vs exceção ecoa a diferença entre um endpoint de *lookup* ("pode não achar, 404 é
  normal") e um passo de *fluxo de autenticação* ("se chegou aqui, tinha que existir").

---

## Exercício 4 — `sealed` e pattern matching

**Objetivo:** revisar `sealed interface`/`sealed class` + `permits`, `record` como variante de tipo
soma, e `switch` pattern matching (expressão, record deconstruction, `when` guard, exaustividade
checada pelo compilador). Modelar o resultado de um login como **dado**, não como exceção.

### Contexto / ponto de partida
- `AuthenticationService.authenticate(username, password)` hoje **lança**
  `UserNotFoundException` / `UserNotActiveException` / `InvalidCredentialsException` e retorna `User`
  no sucesso.
- `UserNotActiveException` já carrega um campo `UserStatus` — decisão que se repete aqui.

### O que implementar

**1. `sealed interface LoginResult`**
- Variantes (mínimo do roteiro): `Success`, `InvalidCredentials`, `UserBlocked`, `UserPending`.
  - `Success` carrega o `User` autenticado (`record Success(User user) implements LoginResult`).
  - As demais podem ou não carregar payload (ex: `username` para log). Decida.
- Decisões de design a justificar em Javadoc curto:
  - **Forma de cada variante**: `record` (imutável, sem boilerplate) vs. classe `final`. Para
    variantes sem dado, `record` sem componentes vs. singleton. Qual e por quê?
  - **`UserBlocked` + `UserPending` separados, ou um `UserNotActive(UserStatus status)`?** Você
    escolheu carregar o `UserStatus` na exceção do Exercício 3 — seja consistente ou explique a
    divergência. O roteiro pede 4 variantes; a decisão é sua.
  - **`UserNotFound` entra como variante?** Username inexistente é "um desfecho normal de uma
    tentativa de login" (→ variante) ou "erro de uso da API" (→ continua exceção)? Argumente.
  - `permits` explícito ou variantes aninhadas na interface — escolha um estilo.

**2. `LoginResult tryAuthenticate(String username, String password)`**
- Método **novo**, ao lado do `authenticate` que lança — os dois coexistem para comparar as
  abordagens. **Não apague** o `authenticate`.
- Mesmas checagens, **mesma ordem** (username → status → senha), mas **retorna** a variante.

**3. Um consumidor com `switch` pattern matching exaustivo**
- Um método (ex: `String describe(LoginResult r)` ou algo que produza uma decisão) que faz
  `switch` sobre `LoginResult` **sem `default`**, cobrindo todas as variantes.
- Exercite: `switch` como expressão; record deconstruction (`case Success(User user) -> ...`); um
  `when` guard se fizer sentido (ex: `case Success s when s.user().getRoles().isEmpty() -> ...`).

### Restrições de design
- **Proibido `default`** no `switch` sobre `LoginResult` — a exaustividade checada pelo compilador
  é o ponto do exercício. Adicionar uma variante nova deve **quebrar a compilação** de quem não a
  tratou (é o recurso, não um bug).
- Nada de `instanceof` encadeado com cast manual — pattern matching em `switch`/`if`.
- Todas as variantes imutáveis (`record` já garante; classe → `final` + campos `final`).
- Não regride nas convenções anteriores.

### Teste de aceitação
1. `tryAuthenticate` com username inexistente → `UserNotFound` (ou documentar por que continua
   exceção e testar isso).
2. usuário `BLOQUEADO` → `UserBlocked` (ou `UserNotActive` com status `BLOQUEADO`).
3. usuário `PENDENTE` → `UserPending` (ou `UserNotActive` com status `PENDENTE`).
4. senha errada → `InvalidCredentials`.
5. credenciais válidas → `Success`; o `switch` do consumidor extrai o `User` via record pattern e
   `Success.user()` é o esperado.
6. ordem preservada: `BLOQUEADO` + senha errada → `UserBlocked`, não `InvalidCredentials`.
7. (exaustividade — verificação manual, registre no PR/comentário) comentar um `case` do `switch`
   do consumidor e confirmar que **não compila**.

### Conexão com o domínio (CIAM)
- SDKs de autenticação modelam o desfecho como tipo soma: sucesso com tokens, ou erro tipado
  (`interaction_required`, `login_required`, `mfa_required`...). O `switch` exaustivo obriga a
  tratar cada caso — o compilador vira parte da revisão de segurança.
- Exceção vs. `LoginResult`: exceção serve para "isso não deveria acontecer, interrompa o fluxo";
  tipo-resultado serve para "esse é um dos desfechos normais e o chamador **tem** que decidir o que
  fazer com cada um". Login falho é rotina, não excepcional. Discuta onde cada estilo cabe — e por
  que manter o `authenticate` que lança ainda faz sentido como atalho para o caminho feliz.

---

## Roteiro completo (para continuar após o Exercício 2)

### Fase 1 — Fundamentos revisitados
- Exercício 1: modelagem de entidades (✅ concluído)
- Exercício 2: coleções e streams (✅ concluído)
- Exercício 3: tratamento de exceções — criar hierarquia de exceções de domínio
  (`UserNotFoundException`, `DuplicateUsernameException`, `UserNotActiveException` etc.) e aplicar
  no `UserRepository`/serviço de autenticação. (✅ concluído)
- Exercício 4: `sealed classes` e `pattern matching` (switch pattern matching) — modelar o
  resultado de uma tentativa de login como um `sealed interface LoginResult` com variantes
  (`Success`, `InvalidCredentials`, `UserBlocked`, `UserPending`). (✅ concluído)

### Fase 2 — Intermediário
- Exercício 5: Generics — criar um `Repository<T, ID>` genérico e fazer `UserRepository` implementá-lo.
- Exercício 6: `Optional` — revisar uso correto (evitar `Optional.get()` sem checagem, encadear
  `map`/`orElseThrow`) refatorando os métodos de busca.
- Exercício 7: I/O e NIO.2 — persistir/carregar o estado do repositório em um arquivo JSON simples
  (sem framework, usando `java.nio.file`).
- Exercício 8: Concorrência básica — simular tentativas de login concorrentes com
  `ExecutorService`, garantindo que o contador de tentativas falhas (regra de bloqueio após 5
  falhas) seja thread-safe.
- Exercício 9: Testes com JUnit 5 — parametrização (`@ParameterizedTest`), `@Nested`, mocks simples
  (sem Mockito ainda, só para fixar a API do JUnit).

### Fase 3 — Avançado
- Exercício 10: Virtual Threads (Project Loom) — reescrever o cenário de login concorrente do
  Exercício 8 usando virtual threads e comparar.
- Exercício 11: Reflection — implementar um pequeno "auditor" que inspeciona anotações customizadas
  em métodos de serviço (ex: `@Audited`) e loga a chamada.
- Exercício 12: Design patterns aplicados ao domínio (Strategy para `PasswordHasher` — já
  esboçado —, Factory para criação de `User`, Observer para o `AuditLog`).
- Exercício 13: Performance/JVM internals — noções de GC, medir alocação com um micro-benchmark
  simples (JMH, se quiser ir a fundo).
- Exercício 14: Módulos (JPMS) — modularizar o projeto com `module-info.java`.

### Fase 4 — Aplicado ao domínio (Spring Boot)
- Exercício 15: Migrar o projeto para Spring Boot, expondo os primeiros endpoints REST de
  `/users` (CRUD básico) usando o domínio já construído.
- Exercício 16: Persistência real com Spring Data JPA, substituindo o `UserRepository` em memória.
- Exercício 17: Segurança — implementar autenticação via JWT (emissão e validação), aproveitando o
  conhecimento prévio de OAuth2/OIDC/PingFederate para discutir paralelos e diferenças.
- Exercício 18: Resource Server validando JWT emitido por um authorization server real (ex: um
  PingFederate de teste ou Keycloak), fechando o ciclo com o domínio profissional do usuário.
- Exercício 19: Testes de integração com `@SpringBootTest` e Testcontainers.

---

## Como continuar

1. Ao iniciar, veja em "Status atual" qual exercício está em andamento e confirme com o usuário se
   ele quer seguir o enunciado ou já entregar código para revisão.
2. Sempre que um exercício for concluído, atualize a seção "Status atual" deste arquivo e o
   roteiro (marcando com ✅) antes de propor o próximo, para manter o histórico da mentoria.
