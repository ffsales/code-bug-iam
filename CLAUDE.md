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
- ✅ **Exercício 5 (Fase 2) — CONCLUÍDO**: `EntityRepository<T>` generalizado para
  `Repository<T, ID>` (`save`, `findById`, `getById`, `findAll`, `saveAll` com bounded wildcard
  `Collection<? extends T>` subindo para a interface) + `UserRepository implements
  Repository<User, UUID>` + `RoleRepository implements Repository<Role, String>` como segundo
  repositório, provando que a abstração não fica amarrada a `UUID` (usa o nome da `Role` como
  identidade). Exceção de "não encontrado" no mundo genérico resolvida com
  `EntityNotFoundException` como nova raiz entre `DomainException` e as exceções específicas
  (`UserNotFoundException`, `RoleNotFoundException`), preservando o vocabulário de domínio sem
  duplicar a decisão checked/unchecked do Exercício 3. Revisão passou por 4 rodadas corrigindo:
  `saveAll` de `UserRepository` reimplementava a inserção em vez de reusar `save`, bypassando a
  validação de `username` duplicado e reintroduzindo o bug de drift do índice secundário do
  Exercício 3 — corrigido delegando para `this::save` no `forEach`, com checagem extra de
  duplicidade **dentro do próprio lote** via `Collectors.toMap` (usando `User.equals` por `id` no
  merge function para não confundir "duplicata real" com "mesma entidade duas vezes no lote");
  um marker interface `Entity` criado sem uso real (nenhum bound, aplicado só em `Role` e não em
  `User`) foi removido em vez de mantido pela metade; bound `ID extends Comparable<ID>` foi
  proposto sem nenhum método da interface usar `compareTo`, e removido; um teste
  (`shouldSaveListOfUsers`) que assumia ordem de iteração de `HashMap.values()` — flaky, chegou a
  falhar em uma rodada — corrigido para buscar por `username` via `filter`/`findFirst` em vez de
  indexar a lista. Enunciado completo abaixo.
- ✅ **Exercício 6 (Fase 2) — CONCLUÍDO**: uso idiomático de `Optional` nos métodos de busca.
  `UserRepository.getById`/`RoleRepository.getById` passaram a reaproveitar `findById` via
  `orElseThrow` em vez de reimplementar o acesso ao `Map`. `getByUsername` e `findByUsername`
  passaram a encadear `Optional.ofNullable(mapUsersByName.get(username)).flatMap(this::findById)`
  em vez de checagem manual de `null` + `.get()` cru. `AuthenticationService.tryAuthenticate`
  virou uma cadeia única (`findByUsername(...).map(user -> {checagens de status/senha}).orElse(new
  UserNotFound(username))`), eliminando o padrão `isEmpty()` + `.get()`; `orElse` (não `orElseGet`)
  escolhido corretamente por `UserNotFound` ser barato de construir. `Role`/`User` ganharam Javadoc
  documentando que nenhum campo é opcional por design (item "Optional fora de lugar" do
  enunciado). Revisão corrigiu: uma tentativa inicial de `getByUsername` com stream
  (`filter`/`flatMap`/`collect`) que não compilava e reimplementava por varredura o que o índice
  `mapUsersByName` já resolvia em O(1); confirmado por grep que não sobra `Optional.get()` cru fora
  de teste. Ponto registrado como polimento futuro, não bloqueante: `getByUsername` ainda duplica a
  expressão de busca de `findByUsername` em vez de delegar a ele (`findByUsername(username)
  .orElseThrow(...)`). Enunciado completo abaixo.
- ✅ **Exercício 7 (Fase 2) — CONCLUÍDO**: persistência do estado do repositório em arquivo JSON via
  `java.nio.file` (NIO.2), com parsing manual (sem biblioteca). `UserRepository.saveToFile`/
  `loadFromFile` reaproveitam `findAll`/`saveAll` (não bypassam a validação de `username` duplicado).
  Serialização isolada em `UserSerializer` (monta/lê o JSON campo a campo via um `ObjectSerializer`/
  `JsonSerializer` injetado) e `RepositoryPersistenceException` encapsulando falhas de I/O e de
  parsing, preservando a causa original (`super(msg, cause)`). Passou por várias rodadas de revisão
  corrigindo dois bugs reais e um problema estrutural nos testes:
  - **Double-hash de senha no round-trip**: `UserSerializer` gravava `user.getPasswordHash()` (já
    hasheado) e `deserializeUser` reconstruía o `User` pelo construtor que hasheia de novo — depois
    de salvar e carregar, a senha original deixava de autenticar. Resolvido separando duas formas de
    criar um `User`: `User.newUser(...)` (dado novo, em texto plano, hasheia) e
    `User.reconstructUser(...)` (dado já persistido, atribui o hash direto) — os dois construtores
    ficaram `private`, só acessíveis pelas factories, pra não dar pra confundir qual aceita o quê.
  - **Repositório vazio quebrava ao carregar** (`StringIndexOutOfBoundsException` numa linha vazia
    gerada pelo próprio serializer) — corrigido tratando linha vazia/em branco no parsing.
  - **Testes não provavam o round-trip**: as primeiras versões salvavam e "carregavam" na mesma
    instância de `UserRepository`, então um `deserializeUser` quebrado (ex: devolvendo 0 usuários)
    passaria do mesmo jeito, porque os usuários originais nunca saíam da memória. Corrigido usando
    duas instâncias (uma escreve, outra lê) e comparando campo a campo (incluindo `getPasswordHash`,
    que `User.equals` — por `id` — não cobre). Testes de arquivo também migraram de paths fixos em
    `resources/` (que geravam dependência de ordem entre métodos de teste — mesma causa-raiz do bug
    de `HashMap.values()` do Exercício 5) para `@TempDir` do JUnit 5.
  - **Validação de campo malformado na desserialização**: capturar `NullPointerException` genérica
    pra sinalizar "arquivo corrompido" não cobria todos os casos (`UUID.fromString`/`UserStatus.valueOf`
    lançam `IllegalArgumentException` pra valor presente mas malformado, não capturada). Resolvido
    com `reconstructUser` validando formato explicitamente (UUID, enum) antes de construir, lançando
    `UserFieldInvalidException` (nova, extends `DomainException`) com mensagem por campo;
    `deserializeUser` encapsula qualquer `DomainException` em `RepositoryPersistenceException`
    preservando a causa. `RepositoryPersistenceException` ficou fora da hierarquia de `DomainException`
    por decisão documentada (falha de I/O/parsing não é violação de regra de negócio, é infraestrutura).
  Enunciado completo abaixo.
- ✅ **Exercício 8 (Fase 2) — CONCLUÍDO**: `LoginAttemptTrackerImpl` (contador de tentativas falhas por
  `UUID`, via `ConcurrentHashMap<UUID, AtomicInteger>`) integrado em `AuthenticationService.authenticate`/
  `tryAuthenticate`, bloqueando após 5 falhas — reaproveitando `UserStatus.BLOQUEADO` (decisão
  documentada em Javadoc: representa tanto bloqueio administrativo quanto por tentativas, sem
  distinção de variante nova em `LoginResult`). Validado por um teste que dispara 10 tentativas
  concorrentes via `ExecutorService` contra a mesma conta. Esse teste exigiu várias rodadas de
  depuração de concorrência de verdade (bugs só reproduzíveis rodando, não lendo o código):
  - `tryAuthenticate` sempre devolvia `InvalidCredentials` pra senha errada, mesmo na tentativa que
    estourava o limite — corrigido fazendo `treatLoginFail` devolver o `LoginResult` certo
    (`UserBlocked`/`InvalidCredentials`) em vez de decidir isso em outro lugar.
  - TOCTOU no contador: incrementar (`addAttemptFail`) e checar o limite (`isReachedMaxAttempts`)
    eram duas operações atômicas **separadas**, com uma brecha entre elas onde outra thread podia
    incrementar primeiro — corrigido fazendo `addAttemptFail` devolver o valor do próprio
    `incrementAndGet()`, e decidindo em cima desse valor, sem reconsulta.
  - `attempts == MAX_ATTEMPTS` só reconhecia a tentativa que acertava exatamente 5; as seguintes (6,
    7...) escapavam e voltavam a contar como `InvalidCredentials` — trocado para `>=`, o que também
    tornou o resultado 100% determinístico (confirmado por 30+ execuções diretas e via `mvn test`:
    sempre 4 `InvalidCredentials` / 6 `UserBlocked` em 10 tentativas concorrentes, nunca flaky).
  - Bug pré-existente em `UserRepository.save()`, exposto só pela concorrência real: salvar o mesmo
    usuário concorrentemente removia e recolocava a entrada do índice secundário por `username`
    (mesmo sem o username ter mudado), abrindo uma janela em que `findByUsername` via "ninguém ali"
    — causava um `UserNotFound` esporádico (~1 em 15 execuções). Corrigido evitando tocar no índice
    de nome quando o `username` não muda (`mapUsersById`/`mapUsersByName` migraram para
    `ConcurrentHashMap`, mas isso só garante atomicidade de operação isolada, não da sequência
    remove-depois-put como um todo).
  - Teste sequencial com off-by-one (`if (i == 5)` dentro de um loop `i < 5`, nunca satisfeito) que
    deixava de verificar o caso de fronteira (a 5ª tentativa); corrigido para `i == 4`.
  Enunciado completo abaixo.
- ✅ **Exercício 9 (Fase 2) — CONCLUÍDO**: `AuthenticateServiceTest` reorganizado com `@Nested`
  (classes não-estáticas agrupando `authenticate`/`tryAuthenticate`/concorrência, setup
  compartilhado via `@BeforeEach` da classe externa) — confirmado que nenhum dos 14 cenários
  anteriores se perdeu na reorganização (contagem de testes antes/depois batendo). Dois
  `@ParameterizedTest` substituindo métodos quase-idênticos: status inativo via `@EnumSource`
  (`BLOQUEADO`/`PENDENTE`) e reset de contagem via `@ValueSource` com um valor "fraco" e um "forte"
  (a conta que importa: `2 × times` comparado ao limite de 5 — só valores que cruzam o limite sem
  reset provam que o reset está fazendo trabalho real). `LoginAttemptTracker` extraído como
  interface (`LoginAttemptTrackerImpl` = implementação real) pra viabilizar os doubles. Dois test
  doubles manuais, sem Mockito:
  - `PasswordHasherStub` — stub determinístico (função identidade: `hash(x) = x`) isolando
    `AuthenticationService` do algoritmo real de hash. Passou por três tentativas até acertar:
    primeiro usava hasher real na criação do `user` e stub na autenticação (garantia mismatch pra
    **qualquer** senha, inclusive a correta — teste vazio de sinal); depois um "stub" com
    `Random.from(...)` (não determinístico — mesma entrada, saídas diferentes a cada chamada,
    quebrando a premissa básica de um stub); resolvido com a função identidade, que é determinística
    **e** sensível ao argumento — a mesma propriedade que faltava nas duas tentativas anteriores.
  - `LoginAttemptTrackerFake` — fake (não stub: reimplementa a lógica de contagem de verdade, só
    sem `ConcurrentHashMap`/`AtomicInteger`) com um método explícito pra forçar um usuário direto
    pro limite (`goToLimitAttempts(UUID)`), evitando repetir o loop de 5 tentativas reais num teste.
    Passou por rodadas corrigindo: primeiro uso trocava o double sem ganho nenhum sobre a
    implementação real (testes que não precisavam de nada que o fake oferecesse); depois um
    "atalho" que alterava o comportamento normal de `addAttemptFail` (toda falha contando como 5) e
    quebrava o incremento pra qualquer outro uso; depois um campo de atalho global
    (`attemptsFake`) que vazava entre usuários diferentes do mesmo fake — resolvido escrevendo o
    forçamento direto em `counterAttempts` por `UUID`, igual aos outros métodos da interface, sem
    nenhum estado solto da classe. Confirmado com dois usuários no mesmo fake (um forçado, outro
    incrementando normal) que não há mais contaminação cruzada.
  Enunciado completo abaixo.
- 🔄 **Exercício 10 (Fase 3) — EM ANDAMENTO**: virtual threads (Project Loom) — reescrever o
  cenário de concorrência do Exercício 8/9 com `Executors.newVirtualThreadPerTaskExecutor()` e
  comparar com threads de plataforma. Enunciado completo abaixo.

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

## Exercício 5 — Generics

**Objetivo:** revisar generics — bounded type parameters, e projetar uma abstração reutilizável
(`Repository<T, ID>`) generalizando o que já existe, em vez de criar do zero.

### Contexto / ponto de partida
- `EntityRepository<T>` hoje é genérico só na entidade; o id está hardcoded em `UUID`
  (`Optional<T> findById(UUID id)`).
- `UserRepository` mistura, na mesma classe, métodos que são de repositório genérico (`save`,
  `findById`, `findAll`) com métodos específicos de `User` (`getById`, `getByUsername`,
  `findByUsername`, `findByStatus`, `findByRoleName`, `groupByStatus`, `countUsersByRoleName`,
  `allDistinctPermissionNames`, `anyUserHasPermission`).

### O que implementar

**1. Generalizar `EntityRepository<T>` → `Repository<T, ID>`**
- `void save(T entity)`
- `Optional<T> findById(ID id)`
- `T getById(ID id)` — hoje só existe em `UserRepository`; decida se sobe para a interface (e o
  que isso implica na exceção — ver abaixo).
- `List<T> findAll()`
- `ID` precisa de algum *bound*? Java não deixa declarar "requer `equals`/`hashCode` consistentes"
  como bound — mas pense se faz sentido um `ID extends Comparable<ID>`, ou se `ID` solto (só
  `Object` implícito) é suficiente para o que a interface realmente usa.

**2. A exceção de "não encontrado" no mundo genérico**
- `UserNotFoundException` é específica de `User` e carrega significado de domínio (username, id).
  Se `getById` genérico for para a interface, o que ela lança quando não encontra? Opções: uma
  `EntityNotFoundException` genérica (perde o vocabulário de domínio); cada implementação continua
  lançando a sua própria (a interface só documenta "lança unchecked, tipo definido pela
  implementação"); ou outra que você imaginar. Justifique em Javadoc — é a mesma discussão
  checked/unchecked do Exercício 3, agora sob o ângulo de genéricos.

**3. `UserRepository implements Repository<User, UUID>`**
- Os métodos genéricos migram para a assinatura da interface; os específicos de `User` continuam
  como métodos próprios da classe, fora da interface.

**4. Um segundo repositório genérico, para provar que a abstração generaliza**
- Não precisa ser elaborado. Ideia: um `RoleRepository implements Repository<Role, String>`, usando
  o nome da role como id (`Role.equals`/`hashCode` já são por nome — ele serve como identidade?).
  O ponto é ter evidência de que `Repository<T, ID>` funciona com um `ID` que não é `UUID`.

**5. (opcional) Bounded wildcard**
- Um `saveAll(Collection<? extends T> entities)` na interface ou em `UserRepository`, pensando em
  variância: por que `? extends T` e não `T` puro faz sentido para um parâmetro de entrada?

### Restrições de design
- Sem vazamento de type erasure: nada de `Object` bruto, cast manual ou
  `@SuppressWarnings("unchecked")` para contornar o compilador — se sentir necessidade de um cast,
  o bound provavelmente está errado.
- Não duplique contrato: um método que já existe na interface genérica não deve ser redeclarado na
  implementação com assinatura diferente.
- **Não regride**: os testes de `UserRepository`/`AuthenticationService`/`ConsumerLogin` dos
  Exercícios 2–4 continuam verdes sem mudar asserção — isso é refatoração de estrutura, não de
  regra de negócio.

### Teste de aceitação
1. `UserRepository implements Repository<User, UUID>` compila e toda a suíte existente (Ex2–Ex4)
   continua passando sem alteração de asserção.
2. Um segundo tipo de repositório com `ID` diferente de `UUID`, com teste de
   `save`/`findById`/`getById`/`findAll` provando que a abstração não está amarrada a `UUID`.
3. (verificação manual, registre) tente forçar um cast indevido ou um `ID` incompatível e confirme
   que o compilador recusa — não precisa de teste automatizado, só de você ver o bound funcionando.

### Conexão com o domínio (CIAM)
- `Repository<T, ID>` é o mesmo formato do `JpaRepository<T, ID>` do Spring Data, que você vai usar
  de verdade no Exercício 16. Parametrizar o `ID` à parte (em vez de assumir sempre `UUID` ou
  `Long`) evita acoplar a camada de persistência ao tipo de chave primária de um banco específico.
- Em identity providers reais, um usuário pode ser identificado por `sub` (string opaca do OIDC),
  um `UUID` interno, ou um `Long` de banco legado — a mesma abstração de repositório genérico é o
  que permite trocar isso sem reescrever a camada de acesso a dados.

---

## Exercício 6 — `Optional`

**Objetivo:** revisar o uso idiomático de `Optional` — encadear (`map`, `filter`, `orElseThrow`,
`orElseGet`) em vez de checar com `isPresent`/`isEmpty` e acessar com `.get()` cru, e reconhecer
onde `Optional` **não** deveria ser usado.

### Contexto / ponto de partida
Pontos concretos no código atual que usam `Optional` de forma manual, não encadeada:
- `UserRepository.getById` e `RoleRepository.getById` **reimplementam** a busca — acessam o `Map`
  diretamente e fazem `Objects.isNull(x)` + `throw` — em vez de reaproveitar `findById(id)`, que já
  devolve `Optional<T>` e já existe na própria classe.
- `UserRepository.getByUsername`:
  ```java
  return this.findById(id).get();
  ```
  um `.get()` sem checagem — funciona porque `id` veio do índice `mapUsersByName` e "sempre" existe
  em `mapUsersById`, mas é exatamente o tipo de suposição que, se a invariante entre os dois mapas
  quebrar um dia (ex: um bug futuro no `saveAll`), vira `NoSuchElementException` sem contexto
  nenhum de domínio, em vez de uma exceção que diga qual username falhou.
- `AuthenticationService.tryAuthenticate`:
  ```java
  var optUser = userRepository.findByUsername(username);
  if (optUser.isEmpty())
      return new UserNotFound(username);
  var user = optUser.get();
  ```
  o padrão clássico `isEmpty()` + `.get()` que o `Optional` foi desenhado para substituir.
- (bônus do Exercício 5) `orElseGet(null)` num teste — `orElseGet` espera um `Supplier`, não um
  valor pronto; `null` aí é o método errado, não só um valor perigoso.

### O que implementar
1. **`getById` sem duplicar a busca**: refatore `UserRepository.getById` e `RoleRepository.getById`
   para chamar `findById(id)` e encadear até a exceção (`orElseThrow`), em vez de acessar o `Map`
   de novo. Pense: isso é natural de subir como método `default` na interface `Repository<T, ID>`
   (recebendo um `Supplier<? extends RuntimeException>`, por exemplo) ou cada implementação deve
   continuar decidindo sozinha qual exceção lançar? Argumente a escolha.
2. **`getByUsername` sem `.get()` cru**: troque por um encadeamento que não dependa de "eu sei que
   está presente" — pense em `Optional<UUID>` → `flatMap`/`map` até `Optional<User>` →
   `orElseThrow`, sem nunca chamar `.get()`.
3. **`tryAuthenticate` sem `isEmpty()` + `.get()`**: refatore para não extrair o valor manualmente
   antes de saber que ele existe. Cuidado: as checagens seguintes (`BLOQUEADO`, `PENDENTE`, senha)
   dependem do `User` já desembrulhado e continuam precisando rodar **na mesma ordem** — pense se
   dá para fazer tudo em uma cadeia de `Optional` ou se, nesse caso específico, um
   `if (optUser.isEmpty()) return ...; var user = optUser.get();` bem no início (só para
   "desembrulhar e seguir com múltiplos ifs depois") já é aceitável, e por quê.
4. **Onde `Optional` não deveria aparecer**: confira o restante do código (domain, service) — campo
   de entidade, parâmetro de método, elemento de `List`/`Set` são os três lugares clássicos onde
   `Optional` é usado errado. Se não encontrar nenhum caso desses no projeto atual, documente por
   que não (ex: "nenhum campo de `User`/`Role` é opcional por design").

### Restrições de design
- Proibido `Optional.get()` sem checagem prévia — e mesmo com checagem, prefira `orElseThrow`/`map`
  a `isPresent`/`isEmpty` + acesso direto sempre que o fluxo permitir.
- `orElse` para valor pronto (barato); `orElseGet` só quando o valor default é caro de computar
  (lazy, só roda se precisar).
- `Optional` nunca como campo de entidade, parâmetro de método ou elemento de coleção.
- Não regride: as suítes de `UserRepository`/`RoleRepository`/`AuthenticationService`/
  `ConsumerLogin` continuam verdes sem mudar asserção — é refatoração de estilo, não de regra de
  negócio.

### Teste de aceitação
1. `getById` (`User` e `Role`) continua lançando a exceção certa (`UserNotFoundException` /
   `RoleNotFoundException`) para id/nome inexistente, e devolvendo a entidade certa quando existe —
   sem alteração de asserção nos testes já existentes.
2. `getByUsername` continua lançando `UserNotFoundException` para username inexistente.
3. `tryAuthenticate` continua com os 6 cenários do Exercício 4 passando (`UserNotFound`,
   `UserBlocked`, `UserPending`, `InvalidCredentials`, `Success`, e a ordem
   bloqueado-antes-de-senha-errada) sem alteração de asserção.
4. (verificação manual, registre) grep por `.get()` em cima de `Optional` no projeto — deve sobrar
   zero fora de teste.

### Conexão com o domínio (CIAM)
- SDKs OAuth2/OIDC devolvem estruturas parecidas com `Optional` para claims opcionais de um
  `id_token` (`nickname`, `picture`, etc.) — tratar isso com `.get()` cru é a mesma classe de bug
  que causa `NullPointerException` em produção quando um IdP simplesmente não popula um claim
  opcional que o código assumia sempre presente.
- A diferença entre "isso pode não existir e tudo bem" (`Optional`, lookup) e "isso não deveria
  faltar aqui" (exceção, `orElseThrow`) é a mesma discussão do Exercício 3 sobre `Optional` vs
  exceção — aqui ela aparece dentro do próprio código de busca, não só na fronteira do serviço.

---

## Exercício 7 — I/O e NIO.2

**Objetivo:** revisar E/S de arquivos com a API moderna (`java.nio.file`: `Path`, `Files`), incluindo
leitura/escrita de texto, tratamento de `IOException` (checked) e serialização manual para um
formato simples (JSON "à mão", sem biblioteca) — fixando a diferença entre a `java.io` clássica e
o NIO.2, e revisitando a discussão checked/unchecked do Exercício 3 sob o ângulo de falhas de
sistema de arquivos.

### Contexto / ponto de partida
- `UserRepository`/`RoleRepository` são hoje 100% em memória; todo estado se perde ao encerrar a
  aplicação.
- Não há dependência de parsing JSON no `pom.xml` — o exercício é deliberadamente "sem framework"
  (nada de Jackson/Gson), pra forçar entender o que uma lib faz por baixo.

### O que implementar
1. Um método de escrita (ex: `void saveToFile(Path path)`, no próprio repositório ou numa classe
   separada tipo `UserRepositorySerializer` — decisão sua, justifique) que serializa o estado atual
   (`findAll()`) para um arquivo JSON simples, usando a API `Files` (`writeString` ou
   `newBufferedWriter` — pense no trade-off entre escrever tudo de uma vez vs. streaming, para um
   volume de dados pequeno como esse).
2. Um método de leitura complementar (`void loadFromFile(Path path)` ou similar) que lê o JSON de
   volta e repovoa o repositório **reaproveitando `save`/`saveAll`** (não reimplementar a
   inserção nem bypassar a validação de `username` duplicado).
3. Tratamento de `IOException`: ela é checked — decida se deve subir crua para o chamador, ser
   encapsulada numa exceção de domínio unchecked (ex: `RepositoryPersistenceException`, preservando
   a causa original via `super(msg, cause)`), ou outra estratégia. Justifique com a mesma régua
   checked/unchecked do Exercício 3.
4. Usar `Path`/`Files` (NIO.2) — não `java.io.File`/`FileReader`/`FileWriter`. A diferença de API é
   o ponto do exercício.
5. Parsing "na mão": não precisa ser um parser JSON robusto e completo, mas escrita e leitura
   precisam ser consistentes entre si (round-trip). Documente explicitamente o que **não** é
   tratado (ex: escaping de caracteres especiais em strings), em vez de fingir que é um parser
   completo.

### Restrições de design
- Nada de bibliotecas de JSON externas — parsing/serialização manual, mesmo que rudimentar.
- Não regride nas convenções anteriores (imutabilidade, `Optional` idiomático do Exercício 6,
  hierarquia de exceções do Exercício 3, defensive copy).
- O chamador de `saveToFile`/`loadFromFile` não deveria precisar saber que o formato interno é
  JSON — não vaze detalhe de formato na assinatura pública além do necessário (ex: `Path` é
  suficiente, não precisa expor `String` de JSON cru na API).

### Teste de aceitação
1. Salvar um repositório com 2-3 usuários num arquivo temporário (`Files.createTempFile`) e
   carregar de volta num repositório novo — os usuários carregados devem ser iguais (`equals`) aos
   originais.
2. Salvar um repositório vazio e carregar de volta — não lança exceção, resultado é uma coleção
   vazia.
3. Carregar de um arquivo inexistente ou corrompido tem comportamento explícito e testado (a
   exceção que você decidiu lançar), não uma `IOException`/`RuntimeException` genérica não tratada.
4. (opcional, mas recomendado) round-trip de um usuário com role/permission — não só campos
   escalares, provando que a relação aninhada (usuário → roles → permissions) sobrevive à
   serialização.

### Conexão com o domínio (CIAM)
- Identity providers de teste/dev (um Keycloak local, um PingFederate em modo standalone)
  costumam persistir configuração e usuários em arquivos JSON/XML no disco antes de escalar para um
  banco real — entender o "por baixo do capô" de uma serialização manual ajuda a debugar quando
  esse arquivo fica corrompido ou incompatível entre versões do produto.
- `IOException` como checked exception é o exemplo clássico de "o compilador te obriga a decidir o
  que fazer" — a mesma discussão de "fluxo de negócio esperado (arquivo pode não existir) vs. erro
  de programação" do Exercício 3, agora aplicada a falhas de sistema de arquivos em vez de regra de
  negócio.

---

## Exercício 8 — Concorrência básica

**Objetivo:** revisar primitivas de concorrência em Java (`ExecutorService`, `Runnable`/`Callable`,
`Future`, `AtomicInteger`, `ConcurrentHashMap`, `synchronized`) implementando uma regra de bloqueio
por tentativas falhas de login, e escrevendo um teste que force concorrência real — não só provar
que a regra funciona em sequência, mas que o contador por trás dela não perde nem duplica incrementos
quando várias threads batem nele ao mesmo tempo.

### Contexto / ponto de partida
- Hoje `AuthenticationService.authenticate`/`tryAuthenticate` resolvem um login numa tentativa
  isolada: username → status → senha. Não existe memória de tentativas anteriores entre chamadas.
- `LoginResult` (Exercício 4) já modela `UserBlocked`/`UserPending` como estados de **conta**
  (vêm de `UserStatus`, uma coluna/campo persistido). A regra deste exercício é de outra natureza:
  bloqueio por **comportamento** (N tentativas falhas consecutivas), tipicamente temporário — pense
  se isso é um novo valor de `UserStatus`, uma variante nova de `LoginResult`, ou um estado que vive
  só no serviço, fora do `User` (o próprio enunciado não força uma resposta — justifique a sua).
- Nenhum componente hoje guarda estado entre chamadas de `authenticate` (o `UserRepository` guarda
  o `User` atual, não histórico de tentativas).

### O que implementar

1. **Um componente que rastreia tentativas falhas por usuário** (nome e localização são decisão
   sua — ex: `LoginAttemptTrackerImpl`, injetado em `AuthenticationService`, ou outra forma). Ele precisa:
   - Registrar uma falha para um usuário (pense na chave: `username` ou `UUID` — qual identifica
     melhor "a mesma conta" através de tentativas diferentes, inclusive se o `username` puder mudar?).
   - Resetar a contagem num login bem-sucedido.
   - Responder se aquela conta está bloqueada por tentativas (limite: **5** falhas consecutivas desde
     o último sucesso ou reset).
2. **Integrar a regra em `authenticate`/`tryAuthenticate`**: uma tentativa contra uma conta já
   bloqueada por tentativas deve falhar **antes** de checar a senha (mesma lição de ordem de
   checagem do Exercício 3 — não revelar "a senha estava certa" pra uma conta já bloqueada).
3. **Decidir o desfecho de `tryAuthenticate` para esse bloqueio**: reaproveita `UserBlocked` (ou a
   variante equivalente que você já tem) ou é um desfecho novo do `LoginResult`? Se criar um novo,
   lembre que o `switch` exaustivo do Exercício 4 (`ConsumerLogin.describe`) vai **quebrar a
   compilação** até você tratar o caso novo — isso é o recurso, não um bug, mas precisa ser feito.
4. **Uma simulação com `ExecutorService`**: disparar N tentativas de login *concorrentes* contra a
   mesma conta (senha errada), esperar todas terminarem (`invokeAll`/`Future.get`/`CountDownLatch` —
   decisão sua) e então verificar o estado final — contagem de falhas e bloqueio exatamente como
   esperado, nem mais nem menos tentativas contadas do que threads que efetivamente rodaram.

### Restrições de design
- O contador de tentativas falhas **precisa ser seguro sob concorrência real**. Nada de um campo
  `int`/`Integer` incrementado sem sincronização, nem um `Map.get(...)` seguido de `Map.put(...)`
  como duas operações separadas — isso é exatamente a race condition que o exercício quer expor
  (ler, incrementar e decidir bloquear precisam ser atômicos de ponta a ponta, não três passos onde
  outra thread pode "entrar no meio"). Pense em `AtomicInteger`, `ConcurrentHashMap.compute`/`merge`,
  ou um bloco `synchronized` — e esteja pronto pra explicar por que a opção escolhida fecha a brecha.
- Esse rastreamento é um segundo estado mutável compartilhado, além do `Map` do próprio
  `UserRepository`. Pergunta pra você: o `UserRepository` de hoje (um `HashMap` comum, sem nenhuma
  sincronização) é thread-safe? Precisa ser, pra esse exercício funcionar corretamente sob o teste
  concorrente do item 4? Se não for, o que isso significa pro resultado do teste?
- Não regride nas convenções anteriores (hierarquia de `LoginResult`/exceções já fixada, injeção por
  interface onde fizer sentido, Javadoc do "porquê" em decisões não óbvias).

### Teste de aceitação
1. **Sequencial — bloqueio liga**: 5 tentativas com senha errada bloqueiam a conta; uma 6ª tentativa
   — mesmo com a senha **certa** — é rejeitada por bloqueio, não autenticada.
2. **Sequencial — reset funciona**: 3 falhas, depois 1 sucesso, depois mais 3 falhas **não** bloqueia
   (o sucesso no meio zera a contagem).
3. **Concorrente (o teste que prova o exercício de fato)**: disparar via `ExecutorService` um número
   de tentativas simultâneas maior que o limite (ex: 20 threads, mesma conta, senha errada), esperar
   todas terminarem, e verificar que a conta ficou bloqueada e a contagem não "vazou" por race
   condition. Se você chegou a rodar essa simulação contra uma versão não sincronizada do contador
   antes de corrigir, documente o que observou (contagem menor que o número de falhas reais é o
   sintoma clássico de incremento não atômico) — é exatamente o tipo de teste que expõe bug que vale
   reconhecer.
4. (opcional, mas recomendado) repetir o teste concorrente várias vezes em sequência (`@RepeatedTest`,
   se quiser adiantar um pouco do Exercício 9) — race conditions são intermitentes; um único green
   não prova ausência de bug.

### Conexão com o domínio (CIAM)
- Lockout por tentativas falhas é defesa padrão contra brute-force em qualquer authorization server
  — PingFederate, Keycloak, Auth0 implementam alguma forma disso, geralmente com contagem
  **distribuída** entre múltiplas instâncias/nós (Redis, banco com lock otimista). "Contador
  thread-safe numa única JVM" é o primeiro degrau de um problema que, em produção, vira "contador
  consistente entre processos" — a dificuldade não desaparece, só muda de lugar.
- A diferença entre bloqueio por `UserStatus.BLOQUEADO` (ação administrativa, estado persistido) e
  bloqueio por tentativas (comportamental, geralmente com expiração automática) é uma distinção real
  em produtos de IAM — decidir consigo mesmo se o `LoginResult` trata os dois como o mesmo desfecho,
  e por quê, é o mesmo tipo de escolha que um authorization server real precisa tomar ao desenhar sua
  resposta de erro.

---

## Exercício 9 — Testes com JUnit 5

**Objetivo:** revisar recursos do próprio JUnit 5 que a suíte atual não usa —
`@ParameterizedTest`/fontes de parâmetro (`@ValueSource`, `@EnumSource`, `@CsvSource`),
`@Nested` para organizar cenários com setup compartilhado, e test doubles escritos à mão (sem
Mockito) para fixar os conceitos de dummy/stub/fake/spy/mock antes de trocar por uma lib de
mocking de verdade.

### Contexto / ponto de partida
- A suíte hoje (Exercícios 1–8) tem bastante duplicação estrutural — em particular
  `AuthenticateServiceTest` repete, em quase todo método, a mesma sequência de "criar
  `UserRepository`, `PasswordHasherDigest`, `LoginAttemptTrackerImpl`, `AuthenticationService`" antes
  de montar o cenário.
- Vários testes cobrem a mesma lógica pra valores de entrada diferentes com métodos **separados**
  (ex: um teste pra `UserStatus.BLOQUEADO` e outro, quase idêntico, pra `UserStatus.PENDENTE`
  lançando `UserNotActiveException`) — candidatos naturais a `@ParameterizedTest`.
- Não existe nenhum test double hoje: todo teste usa a implementação real de `PasswordHasher`
  (`PasswordHasherDigest`) e o `UserRepository` real (em memória). Isso funciona porque as duas são
  rápidas e determinísticas — mas o exercício pede pra você escrever um double manual mesmo assim,
  como exercício de API, antes de isso deixar de ser viável (quando a dependência for um serviço
  externo de verdade, no Exercício 17/18).
- Sem Mockito no `pom.xml` ainda — é deliberado, o ponto é entender o que a lib faz por baixo antes
  de usá-la.

### O que implementar

1. **`@Nested` em pelo menos uma classe de teste existente** (candidata natural:
   `AuthenticateServiceTest`) — agrupe os cenários por contexto (ex: uma classe aninhada para
   `authenticate`, outra para `tryAuthenticate`, outra para a regra de bloqueio por tentativas) e
   mova a criação repetida de `UserRepository`/`PasswordHasher`/`LoginAttemptTrackerImpl`/
   `AuthenticationService` para um `@BeforeEach` da classe aninhada (lembre: `@Nested` em JUnit 5
   exige classe interna **não-estática** — pense no porquê, e no que isso implica pra compartilhar
   estado entre testes do mesmo grupo sem compartilhar entre grupos diferentes).

2. **Pelo menos um `@ParameterizedTest`** substituindo métodos de teste repetidos que só variam o
   valor de entrada. Candidatos:
   - `UserStatus.BLOQUEADO`/`PENDENTE` → `UserNotActiveException` (via `@EnumSource` ou
     `@ValueSource`), hoje provavelmente dois métodos quase idênticos.
   - A sequência "N tentativas falhas, depois 1 sucesso, depois M tentativas falhas" do Exercício 8
     — pense se dá pra parametrizar `N`/`M`/resultado esperado em vez de fixar `3` e `3`.
   - Decida: ao parametrizar, você perde granularidade de mensagem de falha (qual caso exatamente
     quebrou)? Como o JUnit 5 nomeia cada invocação de um `@ParameterizedTest` no relatório, e isso
     já resolve essa preocupação?

3. **Um test double escrito à mão para `PasswordHasher`** (ex: `FakePasswordHasher` ou
   `StubPasswordHasher` — o nome importa: decida qual categoria ele é e **documente por quê** em
   Javadoc):
   - Um **stub** simples (sempre devolve um hash fixo/previsível, sem lógica de hashing real) usado
     pra testar `AuthenticationService` sem depender do algoritmo real de `PasswordHasherDigest`.
   - Pense se, em algum teste, você precisa também de um **spy** (grava quantas vezes foi chamado,
     com quais argumentos) — por exemplo, pra provar que `AuthenticationService` chama o hasher
     exatamente uma vez por tentativa, nem mais nem menos. Se construir isso, documente a diferença
     entre o que você fez aqui e o stub do item anterior.

4. **(opcional) Um double manual para `UserRepository` ou `LoginAttemptTrackerImpl`** — pense num
   cenário que seria mais fácil de provar com um double do que com a implementação real (ex: "o
   `save()` foi chamado com o usuário já bloqueado" sem precisar inspecionar o `Map` interno).

### Restrições de design
- **Nada de Mockito** (nem outra lib de mocking) — os doubles são classes/objetos escritos à mão,
  implementando a mesma interface que a dependência real.
- `@Nested` não pode esconder regressão: os cenários que já existiam continuam cobertos, com a
  mesma asserção — isso é reorganização de estrutura de teste, não mudança de regra de negócio
  (mesma régua do Exercício 5).
- Ao parametrizar, não perca cobertura: cada caso que tinha um método próprio antes precisa
  continuar coberto como uma entrada da fonte de parâmetros.
- Nomeie os test doubles pela categoria certa (dummy/stub/fake/spy/mock) — se não tiver certeza de
  qual é qual, isso é parte do exercício: pesquise a diferença antes de nomear.

### Teste de aceitação
1. A suíte inteira continua verde depois da reorganização com `@Nested` — sem perder nenhum cenário
   que já existia.
2. Pelo menos um `@ParameterizedTest` rodando múltiplos casos que antes eram métodos separados,
   com o relatório do JUnit mostrando cada invocação nomeada individualmente (verificação manual:
   rode com `mvn test` ou na IDE e confira os nomes).
3. Pelo menos um teste usando o double manual de `PasswordHasher` em vez da implementação real,
   provando que `AuthenticationService` não depende do algoritmo de hash concreto pra ser testado.
4. (se implementar o spy do item 3) um teste que verifica quantas vezes o hasher foi chamado numa
   tentativa de login — e falha se você, de propósito, fizer `AuthenticationService` chamar o
   hasher duas vezes por engano (teste que prova que o double pega esse tipo de bug).

### Conexão com o domínio (CIAM)
- Testar um `AuthenticationService` sem bater no algoritmo de hash real é o mesmo princípio de
  testar um fluxo OAuth2/OIDC sem bater num authorization server real — você troca a dependência
  por um double (aqui manual; mais pra frente, algo como WireMock pra simular um PingFederate) pra
  isolar a lógica que você quer validar.
- Entender a diferença entre stub, fake, spy e mock "na mão" é o que torna óbvio, mais tarde, o que
  o Mockito/WireMock estão fazendo por você — em vez de tratar a lib como mágica, você já sabe o
  contrato que ela está implementando.

---

## Exercício 10 — Virtual Threads (Project Loom)

**Objetivo:** revisar virtual threads (estabilizadas no Java 21 — `pom.xml` já está nessa versão
desde o Exercício 4) — criar e usar virtual threads, entender a diferença delas pra threads de
plataforma, e reaproveitar o cenário de concorrência do Exercício 8/9 pra comparar as duas
abordagens, sem alterar a regra de negócio.

### Contexto / ponto de partida
- `LoginAttemptAuthenticateConcurrentTest` (dentro de `AuthenticateServiceTest`, Exercício 9) usa
  `Executors.newFixedThreadPool(10)` — um pool de **threads de plataforma**, limitado a 10 threads
  de SO, pra simular 10 tentativas de login concorrentes.
- `LoginAttemptTracker`/`UserRepository` já são thread-safe por construção (`ConcurrentHashMap`,
  `AtomicInteger`, decisões atômicas de ponta a ponta) — correção de concorrência que o Exercício 8
  já validou sob threads de plataforma. A pergunta deste exercício é: essa correção depende de
  **como** as threads são criadas, ou é agnóstica a isso?
- Nenhum código de produção (domínio/serviço/repositório) precisa saber se está sendo chamado por
  uma thread de plataforma ou virtual — a troca deveria ficar inteiramente na camada de teste/
  simulação.

### O que implementar

1. **Reescreva o cenário de concorrência usando virtual threads** — troque
   `Executors.newFixedThreadPool(10)` por `Executors.newVirtualThreadPerTaskExecutor()` (ou
   `Thread.ofVirtual()` diretamente, se quiser explorar a API de mais baixo nível). Decida: cria um
   teste novo ao lado do existente, ou parametriza o mesmo teste pra rodar com os dois tipos de
   executor (lembra do Exercício 9 — `@ParameterizedTest` serviria bem aqui)?
2. **Explore a diferença de escala.** Threads de plataforma têm custo de memória/contexto que torna
   inviável criar milhares delas; virtual threads são desenhadas pra isso. Aumente bastante o número
   de tentativas simultâneas (ex: 1.000 ou 10.000, não só 10) usando virtual threads, e pense se o
   mesmo número seria razoável com um `newFixedThreadPool`.
3. **Confirme, de alguma forma simples (sem JMH, não precisa ser rigoroso), que o número de threads
   do sistema operacional não cresce proporcionalmente ao número de tarefas** quando usando virtual
   threads — ao contrário de um pool de plataforma do mesmo tamanho da carga. Uma forma simples:
   logar `Thread.currentThread()` (seu nome/tipo) de dentro de uma tarefa, ou comparar
   `Thread.getAllStackTraces().size()` antes e depois de disparar a carga.
4. **Documente (não precisa ser código):** o que muda na forma de "pensar" sobre concorrência usando
   virtual threads? Pools de tamanho fixo (como `newFixedThreadPool`) fazem sentido pra virtual
   threads, ou a recomendação de uso é outra (thread-per-task sem limitar um pool)? Isso muda algo
   sobre a *correção* que você já garantiu no Exercício 8, ou só sobre a *escala*?

### Restrições de design
- **Reaproveite a lógica de bloqueio por tentativas do Exercício 8/9 sem alterá-la** — isso é um
  exercício sobre a ferramenta de concorrência, não sobre a regra de negócio. Se você sentir
  necessidade de mudar `AuthenticationService`/`LoginAttemptTracker`/`UserRepository` pra "funcionar
  com virtual threads", isso é um sinal de que algo na implementação atual não era realmente
  thread-safe — o que seria, em si, um achado válido (mas não esperado).
- Fique atento a **pinning**: código que usa `synchronized` em blocos longos, ou operações de I/O
  bloqueante mal geridas, pode "prender" (pin) uma virtual thread à carrier thread, anulando parte
  do benefício. Verifique se algo no código atual usa `synchronized` (hoje a resposta deveria ser
  não — `ConcurrentHashMap`/`AtomicInteger` não dependem disso — mas confirme lendo o código, não
  assumindo).
- Não precisa (e não deve) reescrever o código de produção pra "usar virtual threads" — a mudança
  fica inteiramente na camada de teste/simulação, como prova de que um design concorrente correto
  (primitivas certas, atomicidade de ponta a ponta) funciona igual independente de como as threads
  são criadas.

### Teste de aceitação
1. O cenário reescrito com `newVirtualThreadPerTaskExecutor()` passa com os mesmos resultados do
   Exercício 9 (ex: 4 `InvalidCredentials` / 6 `UserBlocked` com 10 tentativas), repetido várias
   vezes sem flakiness — igual já foi validado com threads de plataforma.
2. Uma demonstração de que uma carga que seria cara/inviável com um pool fixo de threads de
   plataforma (ordem de 1.000+ tentativas simultâneas) roda sem problema com virtual threads.
3. (verificação manual, registre o que observou) alguma evidência de que o número de threads de SO
   não escala proporcionalmente à carga com virtual threads, ao contrário de um pool de plataforma
   equivalente.

### Conexão com o domínio (CIAM)
- Um authorization server real lida com um volume de requisições de autenticação concorrentes
  ordens de magnitude maior que os 10 (ou mesmo 10.000) deste exercício — virtual threads são
  exatamente a ferramenta que torna viável o modelo "uma thread por requisição" (o mais simples de
  programar e debugar) em escala, sem o custo de memória de milhares de threads de plataforma.
- A prova de que a correção do Exercício 8 é independente da estratégia de threading é um lembrete
  importante: concorrência correta vem de primitivas certas (`ConcurrentHashMap`, `AtomicInteger`,
  decisões atômicas de ponta a ponta) — não do tipo de thread usada para chamar o código. Trocar
  pra virtual threads sem essas primitivas não corrigiria nada; a vantagem delas é escala e
  simplicidade de programação, não correção por si só.

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
  (✅ concluído)
- Exercício 6: `Optional` — revisar uso correto (evitar `Optional.get()` sem checagem, encadear
  `map`/`orElseThrow`) refatorando os métodos de busca. (✅ concluído)
- Exercício 7: I/O e NIO.2 — persistir/carregar o estado do repositório em um arquivo JSON simples
  (sem framework, usando `java.nio.file`). (✅ concluído)
- Exercício 8: Concorrência básica — simular tentativas de login concorrentes com
  `ExecutorService`, garantindo que o contador de tentativas falhas (regra de bloqueio após 5
  falhas) seja thread-safe. (✅ concluído)
- Exercício 9: Testes com JUnit 5 — parametrização (`@ParameterizedTest`), `@Nested`, mocks simples
  (sem Mockito ainda, só para fixar a API do JUnit). (✅ concluído)

### Fase 3 — Avançado
- Exercício 10: Virtual Threads (Project Loom) — reescrever o cenário de login concorrente do
  Exercício 8 usando virtual threads e comparar. (🔄 em andamento — enunciado detalhado acima)
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
