# Plano de implementação da Etapa 3

> **Para quem for executar (pessoa ou agente):** siga as tarefas na ordem, uma de cada vez,
> marcando os checkboxes (`- [ ]`). Agentes: usar `superpowers:subagent-driven-development` ou
> `superpowers:executing-plans`.

**Objetivo:** implementar com TDD os 71 métodos da fase 1, os repositórios SQLite, o e-mail e as
telas Swing, e depois os 10 métodos da fase 2.

**Arquitetura:** camadas com portas e adaptadores. O domínio e os serviços são feitos ciclo a
ciclo, com fakes em memória. SQLite, e-mail e Swing entram por último, atrás de interfaces.

**Tecnologias:** Java 17, Maven Wrapper, JUnit 5.14.4 (com `junit-jupiter-params`), SQLite JDBC
3.53.1.0, Angus Mail 2.0.5, Swing.

**Especificação:** [`requisitos.md`](requisitos.md), que traz métodos M01–M81, regras RN01–RN33 e
cenários da seção 9, e [`arquitetura.md`](arquitetura.md), que traz camadas, portas, receita de
serviço e kit de testes. Leia os dois antes de começar. Este plano diz *em que ordem*, *em que
arquivo* e *com quais testes*; o comportamento detalhado de cada método está na seção 7 dos
requisitos.

## Restrições globais

- Pacote base `br.edu.ifmt.biblioteca`; código em `src/main/java`, testes em `src/test/java`.
- Java 17 (`maven.compiler.release` 17). Sem Spring, sem ORM, sem Mockito (arquitetura, seção 8).
- Tudo em português: classes, métodos, mensagens de exceção, commits e documentação.
- Nomes de teste no padrão `deve[Acao]Quando[Condicao]`; os nomes da seção 9 dos requisitos são
  usados exatamente como estão.
- Um ciclo RED → GREEN (→ REFACTOR) por método `Mxx`. Commit e push a cada etapa do ciclo.
- Commits só como **Gabriel Mallezan da Costa Ribeiro &lt;gabrielmallezan@gmail.com&gt;**, sem linha
  `Co-Authored-By` nem `Claude-Session`.
- O domínio só importa `java.*` e `br.edu.ifmt.biblioteca.excecao`. Os serviços só conhecem
  interfaces nas bordas. Só a `Aplicacao` cria implementações reais.
- Nenhuma regra lê a data ou a hora do sistema: datas chegam por parâmetro, e os serviços que
  contam minutos recebem um `Clock` (RNF05). Só a interface usa `LocalDate.now()`.
- Dinheiro em `BigDecimal` sempre com 2 casas (`setScale(2, RoundingMode.HALF_UP)`), mostrado como
  `R$ 3,00`. Datas mostradas como `dd/MM/yyyy`.
- O código de verificação nunca aparece em log, auditoria, tela ou `toString` (RNF10).
- CPF aparece mascarado (`***.982.247-**`) em mensagens, alvos da auditoria e telas (RN32).
- Exceções de regra herdam de `BibliotecaException extends RuntimeException`; dado inválido lança
  `IllegalArgumentException`, operação fora de hora lança `IllegalStateException`.
- Toda exceção tem só o construtor `(String mensagem)`; o teste confere o tipo e, quando o plano
  indica, a mensagem exata.
- A fase 2 só começa com a fase 1 inteira verde no `verify`.

## Pontos de atenção

Entradas que a especificação não testa diretamente, mas que vão aparecer no uso real. Cada uma
virou um teste na tarefa indicada.

1. **CPF em branco na tela** (nulo, vazio ou só espaços): `CpfInvalidoException` com a mensagem
   `Informe o CPF`, nunca `NullPointerException`. Tarefa 1.
2. **CPF ou ISBN com letras ou símbolos** (`529.982.247-2a`, `ISBN 9780306406157`) são recusados,
   e o ISBN-10 com `x` minúsculo no fim (`0-8044-2957-x`) é aceito como `X`. Tarefas 1 e 2.
3. **Código colado do e-mail com espaços nas pontas** (`" 482913 "`) é aceito. Tarefa 11.
4. **Dinheiro com escalas diferentes** (`3` contra `3.00`) é comparado pelo valor, e o saldo
   continua com 2 casas: pagar `3` de um saldo `3.00` zera a dívida. Tarefa 8.
5. **Ação registrada à noite em Cuiabá**, quando já é o dia seguinte em UTC (22:30 de 10/11 =
   02:30Z de 11/11): o filtro de auditoria por data usa o dia de `America/Cuiaba`. Tarefa 14.

---

## Como executar cada ciclo

Cada linha `Ciclo Mxx` das tarefas é um ciclo completo, nestes passos:

1. Escreva no arquivo de teste indicado os testes listados no ciclo, com os valores exatos.
2. Rode só a classe de teste: `./mvnw -q test -Dtest=NomeDaClasseTest` (no Windows,
   `.\mvnw.cmd -q test -Dtest=NomeDaClasseTest`). **Tem que falhar**: erro de compilação
   (`cannot find symbol`) quando a classe ou o método ainda não existe; senão, falha de asserção
   nos testes novos.
3. Commit e push **só do teste**:
   `git add <arquivo de teste>` → `git commit -m "RED: <mensagem do ciclo>"` → `git push`.
   O GitHub Actions fica ❌, de propósito.
4. Escreva o mínimo de código de produção para os testes passarem, nos arquivos da tarefa.
5. Rode **todos** os testes: `./mvnw -q test` → `BUILD SUCCESS`.
6. Commit e push: `git add <arquivos>` → `git commit -m "GREEN: <mensagem do ciclo>"` →
   `git push`. O Actions volta para ✅.
7. Se houver duplicação ou nome ruim, melhore com os testes verdes, rode `./mvnw -q test` e faça
   `git commit -m "REFACTOR: <o que melhorou>"` + push. Quando a tarefa marca um REFACTOR, ele é
   obrigatório.

A mensagem do ciclo aparece entre aspas em cada tarefa e termina com o ID, por exemplo:
`RED: CPF é validado pelos dígitos verificadores (M01)`.

Os testes de integração (`*IT`) rodam com `./mvnw -q verify` (uma classe:
`./mvnw -q verify -Dit.test=NomeIT`). Ao terminar cada passo (1 a 9), atualize a seção "Estado
atual" do [`CONTEXTO.md`](CONTEXTO.md) e faça o commit
`Atualiza o diário de bordo: passo N concluído`.

## Dados de teste padrão

Use sempre estes valores, para os testes lerem como uma história só.

| O quê | Valor |
| --- | --- |
| Usuária Ana (`UmUsuario.ana()`) | CPF `529.982.247-25`, `Ana Souza`, `ana@email.com` (mascarado: `a*a@email.com`) |
| Usuário Bruno (`UmUsuario.bruno()`) | CPF `123.456.789-09`, `Bruno Lima`, `bruno@email.com` |
| Administrador Gabriel (`UmAdministrador.gabriel()`) | CPF `111.444.777-35`, `Gabriel Mallezan`, `gabrielmallezan@gmail.com` (mascarado: `g*************n@gmail.com`) |
| Administradora Carla (`UmAdministrador.carla()`) | CPF `390.533.447-05`, `Carla Dias`, `carla@email.com` |
| CPF válido sem cadastro | `935.411.347-80` |
| Livro 1 | `Dom Casmurro`, Machado de Assis, ISBN `978-0-306-40615-7`, tombos `T-0001`… |
| Livro 2 | `Memórias Póstumas de Brás Cubas`, Machado de Assis, ISBN `978-85-359-0277-8`, tombos `M-0001`… |
| Livro 3 | `Vidas Secas`, Graciliano Ramos, ISBN `978-85-7164-011-5`, tombos `V-0001`… |
| Livro 4 | `Iracema`, José de Alencar, ISBN `978-65-4421-100-3`, tombos `I-0001`… |
| Processamento técnico padrão | `869.3`, `Estante 4, prateleira B`, reposição `R$ 80,00` |
| Data do empréstimo padrão | `10/11/2026` (devolução prevista `17/11/2026`) |
| Instante padrão do relógio | `2026-11-10T13:00:00Z` |
| Código fixo dos testes | `123456` |

## Mapa de arquivos da fase 1

```
src/main/java/br/edu/ifmt/biblioteca/
  Aplicacao.java
  dominio/      Cpf Isbn Formato Validacao CalculadoraMulta CalculadoraSuspensao
                Livro Exemplar DadosBibliograficos ProcessamentoTecnico
                SituacaoLivro EtapaCadastro EstadoConservacao SituacaoExemplar
                Usuario Administrador Emprestimo PoliticaCirculacao SituacaoEmprestimo
                ResultadoDevolucao CodigoVerificacao Finalidade Sessao
                RegistroAuditoria TipoAcao FiltroAuditoria
  verificacao/  GeradorCodigo GeradorCodigoAleatorio CanalEnvio EmailCanalEnvio ConfiguracaoEmail
  repositorio/  LivroRepository UsuarioRepository AdministradorRepository
                EmprestimoRepository AuditoriaRepository Transacao
    sqlite/     ConexaoSqlite + os 5 …RepositorySqlite
  servico/      ProvedorDeOperador Alteracoes AuditoriaService VerificacaoService
                AcessoService UsuarioService AcervoService EmprestimoService
  excecao/      BibliotecaException + 14 subclasses da fase 1
  ui/           Mensagens TelaEntrada TelaPrincipal PainelUsuarios PainelLivros
                PainelEmprestimos PainelAuditoria
src/main/resources/schema.sql
src/test/java/br/edu/ifmt/biblioteca/
  apoio/        UmLivro UmUsuario UmAdministrador RelogioDeTeste GeradorCodigoFixo
                CanalEnvioFalso OperadorFixo SemSessao TransacaoDireta
  dominio/      um …Test por classe de domínio, mais FormatoTest
  verificacao/  GeradorCodigoAleatorioTest
  servico/      um …ServiceTest por serviço e os …ServiceSqliteIT
  repositorio/  …RepositoryContrato (abstratas)
    memoria/    …RepositoryMemoria e …RepositoryMemoriaTest
    sqlite/     …RepositorySqliteIT
```

---

# Fase 1

## Passo 1 — Valores e cálculos

### Tarefa 1: `Cpf`

**Arquivos:** criar `dominio/Cpf.java`, `excecao/BibliotecaException.java`,
`excecao/CpfInvalidoException.java`; teste `dominio/CpfTest.java`.

**Interfaces:**
- Produz: `record Cpf(String valor)` (só os 11 dígitos); `String mascarado()`; `toString()` devolve
  `mascarado()`. `BibliotecaException(String mensagem) extends RuntimeException`;
  `CpfInvalidoException(String mensagem) extends BibliotecaException`.

Exemplo do formato dos testes (vale para o projeto todo):

```java
package br.edu.ifmt.biblioteca.dominio;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.ifmt.biblioteca.excecao.CpfInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    @Test
    void deveAceitarCpfQuandoDigitosVerificadoresEstiveremCorretos() {
        assertEquals("52998224725", new Cpf("529.982.247-25").valor());
    }

    @Test
    void deveLancarExcecaoQuandoDigitoVerificadorDoCpfForInvalido() {
        var e = assertThrows(CpfInvalidoException.class, () -> new Cpf("529.982.247-24"));
        assertEquals("CPF inválido", e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveLancarExcecaoQuandoCpfForNuloOuVazio(String valor) {
        var e = assertThrows(CpfInvalidoException.class, () -> new Cpf(valor));
        assertEquals("Informe o CPF", e.getMessage());
    }
}
```

- [ ] **Ciclo M01 — "CPF é validado pelos dígitos verificadores (M01)"**
  - `deveAceitarCpfQuandoDigitosVerificadoresEstiveremCorretos`: `"529.982.247-25"` → `valor()` = `"52998224725"`
  - `deveRemoverPontosTracoEEspacosQuandoCpfVierFormatado`: `" 529.982.247-25 "` e `"52998224725"` → `"52998224725"`
  - `deveLancarExcecaoQuandoDigitoVerificadorDoCpfForInvalido`: `"529.982.247-24"` → `CpfInvalidoException("CPF inválido")`
  - `deveRecusarCpfQuandoTodosOsDigitosForemIguais`: `"111.111.111-11"` → `CpfInvalidoException`
  - `deveRecusarCpfQuandoNaoTiverOnzeDigitos` (`@ValueSource`): `"5299822472"`, `"529982247250"` → `CpfInvalidoException`
  - `deveRecusarCpfQuandoTiverLetras` (ponto de atenção 2): `"529.982.247-2a"` → `CpfInvalidoException`
  - `deveLancarExcecaoQuandoCpfForNuloOuVazio` (ponto de atenção 1): `null`, `""`, `"   "` → `CpfInvalidoException("Informe o CPF")`
  - `deveConsiderarIguaisQuandoNumeroForOMesmo`: `new Cpf("529.982.247-25")` igual a `new Cpf("52998224725")`
  - Implementação: construtor compacto do record. Remova só `.`, `-` e espaços; o resto precisa ser
    11 dígitos. 1º dígito verificador = (Σ d[i] × (10 − i), i = 0..8) × 10 mod 11, com 10 → 0;
    2º = (Σ d[i] × (11 − i), i = 0..9) × 10 mod 11, com 10 → 0.
- [ ] **Ciclo M02 — "CPF mostra só os dígitos do meio (M02)"**
  - `deveMostrarSoOsSeisDigitosDoMeioQuandoMascarar`: `mascarado()` = `"***.982.247-**"`
  - `deveUsarFormaMascaradaQuandoConverterParaTexto`: `toString()` = `"***.982.247-**"`

### Tarefa 2: `Isbn`

**Arquivos:** criar `dominio/Isbn.java`, `excecao/IsbnInvalidoException.java`; teste `dominio/IsbnTest.java`.

**Interfaces:** Produz `record Isbn(String valor)` (sem hífens e espaços, `X` maiúsculo);
`toString()` devolve `valor`; `IsbnInvalidoException(String)`.

- [ ] **Ciclo M03 — "ISBN é validado pelo dígito verificador (M03)"**
  - `deveAceitarIsbnQuandoDigitoVerificadorEstiverCorreto` (`@CsvSource` entrada, valor):
    `978-0-306-40615-7` → `9780306406157`; `0-306-40615-2` → `0306406152`;
    `0-8044-2957-x` → `080442957X` (ponto de atenção 2)
  - `deveLancarExcecaoQuandoDigitoVerificadorDoIsbnForInvalido`: `"978-0-306-40615-8"` → `IsbnInvalidoException("ISBN inválido")`
  - `deveRecusarIsbnQuandoFormatoForInvalido` (`@ValueSource`): `"978-0-306-40615-X"` (X fora da 10ª
    posição), `"12345"`, `"ISBN 9780306406157"` → `IsbnInvalidoException`
  - `deveLancarExcecaoQuandoIsbnForNuloOuVazio`: `null`, `""` → `IsbnInvalidoException("Informe o ISBN")`
  - `deveConsiderarIguaisQuandoValorForOMesmo`: `Isbn("978-0-306-40615-7")` igual a `Isbn("9780306406157")`
  - Implementação: ISBN-10 vale se Σ v[i] × (10 − i) mod 11 = 0, com `X` = 10 só na última
    posição; ISBN-13 vale se Σ d[i] × (1 se i par, 3 se ímpar) mod 10 = 0.

### Tarefa 3: `CalculadoraMulta`

**Arquivos:** criar `dominio/CalculadoraMulta.java`; teste `dominio/CalculadoraMultaTest.java`.

**Interfaces:** Produz `CalculadoraMulta(BigDecimal valorDiario, BigDecimal teto)`,
`CalculadoraMulta()` (R$ 1,00 e R$ 50,00), `valorDiario()`, `teto()`,
`BigDecimal calcular(int diasAtraso)` com 2 casas.

- [ ] **Ciclo M34 — "Multa tem valor diário e teto configuráveis (M34)"**
  - `deveUsarUmRealPorDiaETetoDeCinquentaQuandoCriadaSemArgumentos`: `valorDiario()` = `1.00`, `teto()` = `50.00`
  - `deveLancarExcecaoQuandoParametrosForemInvalidos` (`@CsvSource` valorDiario, teto; vazio = `null`):
    `(vazio, 50.00)`, `(0.00, 50.00)`, `(-1.00, 50.00)`, `(2.00, 1.00)` → `IllegalArgumentException`
- [ ] **Ciclo M35 — "Multa é o menor entre dias vezes valor diário e o teto (M35)"**
  - `deveCalcularMultaQuandoHouverAtraso` (`@CsvSource` dias, esperado): `0 → 0.00`, `3 → 3.00`, `50 → 50.00`, `60 → 50.00`
  - `deveUsarParametrosQuandoForemInformados`: `new CalculadoraMulta(2.50, 10.00).calcular(3)` = `7.50`
  - `deveLancarExcecaoQuandoDiasDeAtrasoForemNegativos`: `calcular(-1)` → `IllegalArgumentException`

### Tarefa 4: `CalculadoraSuspensao`

**Arquivos:** criar `dominio/CalculadoraSuspensao.java`; teste `dominio/CalculadoraSuspensaoTest.java`.

**Interfaces:** Produz `CalculadoraSuspensao(int diasPorDiaDeAtraso)`, `CalculadoraSuspensao()`
(fator 2), `diasPorDiaDeAtraso()`, `int calcularDias(int diasAtraso)`.

- [ ] **Ciclo M36 — "Suspensão tem fator configurável (M36)"**
  - `deveUsarFatorDoisQuandoCriadaSemArgumentos`: `diasPorDiaDeAtraso()` = `2`
  - `deveLancarExcecaoQuandoFatorForZero`: fatores `0` e `-1` → `IllegalArgumentException`
- [ ] **Ciclo M37 — "Suspensão multiplica os dias de atraso pelo fator (M37)"**
  - `deveCalcularDiasDeSuspensaoQuandoHouverAtraso` (`@CsvSource`): `0 → 0`, `3 → 6`, `60 → 120`
  - `deveLancarExcecaoQuandoDiasDeAtrasoForemNegativos`: `calcularDias(-1)` → `IllegalArgumentException`

## Passo 2 — Acervo

### Tarefa 5: `Livro`, cadastro em etapas

**Arquivos:** criar `dominio/Livro.java`, `SituacaoLivro.java`, `EtapaCadastro.java`,
`DadosBibliograficos.java`, `ProcessamentoTecnico.java`; teste `dominio/LivroTest.java`.

**Interfaces:**
- Produz `enum SituacaoLivro { EM_CADASTRO, LIBERADO }`.
- Produz `enum EtapaCadastro { DADOS_BIBLIOGRAFICOS("dados bibliográficos"),
  PROCESSAMENTO_TECNICO("processamento técnico"), EXEMPLARES("exemplares") }` com `String descricao()`.
- Produz `record DadosBibliograficos(String titulo, String autor, String editora, int anoPublicacao,
  int edicao, int numeroPaginas, String genero)` e
  `record ProcessamentoTecnico(String classificacao, String localizacao, BigDecimal valorReposicao)`,
  sem validação. Quem valida é o `Livro`.
- Produz `Livro(Isbn isbn, String titulo)`, `void registrarDadosBibliograficos(DadosBibliograficos)`,
  `void registrarProcessamentoTecnico(ProcessamentoTecnico)` e `Set<EtapaCadastro> listarPendencias()`.
- Produz os getters `isbn()`, `titulo()`, `autor()`, `editora()`, `anoPublicacao()`, `edicao()`,
  `numeroPaginas()`, `genero()`, `classificacao()`, `localizacao()`, `valorReposicao()` e
  `situacao()`. Os campos ainda não registrados valem `null` ou `0`.

Dados válidos usados nos testes: `new DadosBibliograficos("Dom Casmurro", "Machado de Assis",
"Garnier", 1899, 1, 256, "Romance")` e `new ProcessamentoTecnico("869.3", "Estante 4, prateleira B",
new BigDecimal("80.00"))`.

- [ ] **Ciclo M04 — "Livro começa em cadastro (M04)"**
  - `deveIniciarEmCadastroQuandoCriarLivro`: `situacao()` = `EM_CADASTRO`, `titulo()` = `"Dom Casmurro"`
  - `deveLancarExcecaoQuandoTituloEstiverEmBranco` (`@NullAndEmptySource` + `"  "`) → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoIsbnForNulo` → `IllegalArgumentException`
- [ ] **Ciclo M08 — "Livro lista as etapas pendentes (M08)"**
  - `deveListarAsTresEtapasQuandoLivroForRecemIniciado`: `{DADOS_BIBLIOGRAFICOS, PROCESSAMENTO_TECNICO, EXEMPLARES}`
  - Implementação: `EnumSet`. Os dados contam como registrados quando `autor()` não é nulo; o
    processamento, quando `classificacao()` não é nula; os exemplares, quando há pelo menos um.
- [ ] **Ciclo M05 — "Livro registra os dados bibliográficos (M05)"**
  - `deveGravarDadosQuandoForemValidos`: `autor()` = `"Machado de Assis"`, `edicao()` = `1`, pendências sem `DADOS_BIBLIOGRAFICOS`
  - `deveAceitarSemEditoraEGeneroQuandoForemOpcionais`: editora e gênero `null` → sem exceção
  - `deveLancarExcecaoQuandoDadoObrigatorioForInvalido` (`@CsvSource` titulo, autor, ano, edicao,
    paginas): título `''`, autor `''`, ano `0`, edição `0`, páginas `0`, um de cada vez →
    `IllegalArgumentException`, e depois `autor()` continua `null`
  - `deveCorrigirDadosQuandoRegistrarDeNovo`: segunda chamada com edição `2` → `edicao()` = `2`
- [ ] **Ciclo M06 — "Livro registra o processamento técnico (M06)"**
  - `deveGravarProcessamentoQuandoForValido`: `classificacao()` = `"869.3"`, `valorReposicao()` = `80.00`, pendências sem `PROCESSAMENTO_TECNICO`
  - `deveAceitarClassificacaoQuandoTiverSoTresDigitos`: `"869"`
  - `deveLancarExcecaoQuandoClassificacaoNaoSeguirCdd` (`@ValueSource`): `"abc"`, `"86"`, `"869."`, `"8693"` → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoLocalizacaoEstiverEmBranco` → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoValorDeReposicaoNaoForPositivo`: `null`, `0.00`, `-1.00` → `IllegalArgumentException`
  - Implementação: classificação casa com `\d{3}(\.\d+)?`.

### Tarefa 6: `Livro`, exemplares e liberação

**Arquivos:** criar `dominio/Exemplar.java`, `EstadoConservacao.java`, `SituacaoExemplar.java`,
`excecao/RegistroDuplicadoException.java`, `excecao/CadastroIncompletoException.java`; alterar
`Livro.java`; teste `dominio/LivroTest.java`.

**Interfaces:**
- Produz `enum EstadoConservacao { NOVO, CONSERVADO, VELHO, DANIFICADO }` e
  `enum SituacaoExemplar { DISPONIVEL, EMPRESTADO, EXTRAVIADO }`.
- Produz `Exemplar` com construtor **package-private**
  `Exemplar(Livro livro, String codigoTombo, EstadoConservacao estado, boolean consultaLocal)` e os
  getters `livro()`, `codigoTombo()`, `estado()`, `consultaLocal()`, `situacao()`.
- Produz em `Livro`: `Exemplar adicionarExemplar(String codigoTombo, EstadoConservacao estado)`,
  `List<Exemplar> exemplares()` (lista imutável), `Optional<Exemplar> buscarExemplar(String codigoTombo)`,
  `void liberar(LocalDate data)` e `LocalDate dataLiberacao()`.

- [ ] **Ciclo M07 — "Primeiro exemplar fica só para consulta local (M07)"**
  - `deveCriarPrimeiroExemplarComoConsultaLocal`: `"T-0001"`, `NOVO` → `consultaLocal()` true, `DISPONIVEL`, `livro()` é o próprio livro
  - `deveCriarExemplaresSeguintesParaEmprestimo`: `"T-0002"` → `consultaLocal()` false
  - `deveLancarExcecaoQuandoTomboJaExistirNoLivro`: `"T-0001"` duas vezes → `RegistroDuplicadoException`
  - `deveLancarExcecaoQuandoTomboOuEstadoForInvalido`: `("", NOVO)`, `("T-0003", null)` → `IllegalArgumentException`
  - `deveZerarPendenciasQuandoTodasAsEtapasEstiveremCompletas`: dados + processamento + 1 exemplar → `listarPendencias()` vazio
- [ ] **Ciclo M09 — "Livro só é liberado com o cadastro completo (M09)"**
  - `deveLiberarLivroQuandoNaoHouverPendencias`: `liberar(10/11/2026)` → `LIBERADO`, `dataLiberacao()` = `2026-11-10`
  - `deveImpedirLiberacaoQuandoHouverEtapaPendente`: sem processamento → `CadastroIncompletoException`
    com mensagem `"Cadastro incompleto. Falta: processamento técnico."`
  - `deveLancarExcecaoQuandoLivroJaEstiverLiberado` → `IllegalStateException`
  - Implementação: a mensagem junta as `descricao()` pendentes com `", "`, na ordem do enum.

### Tarefa 7: `Exemplar` e contagem de disponíveis

**Arquivos:** criar `excecao/ExemplarIndisponivelException.java`, `src/test/.../apoio/UmLivro.java`;
alterar `Exemplar.java`, `Livro.java`; testes `dominio/ExemplarTest.java`, `dominio/LivroTest.java`.

**Interfaces:**
- Produz em `Exemplar`: `boolean podeSerEmprestado()`, `void verificarDisponivel()` (lança
  `ExemplarIndisponivelException` com o motivo), `void registrarSaida()`,
  `void registrarRetorno(EstadoConservacao)`, `void atualizarEstado(EstadoConservacao)` e
  `void registrarExtravio()`.
- Produz em `Livro`: `int contarExemplaresDisponiveis()`.
- Produz (teste) `UmLivro.emCadastro()`, que devolve Dom Casmurro sem nenhuma etapa.
- Produz (teste) `UmLivro.liberadoComExemplares(int quantidade)`: Dom Casmurro com dados,
  processamento padrão e tombos `T-0001`…, estado `NOVO`, liberado em `01/11/2026`.
- Produz (teste) `UmLivro.liberadoComExemplares(String isbn, String titulo, String autor,
  String prefixoTombo, int quantidade)`. O método anterior chama este.

Mensagens, verificadas nesta ordem: `"Exemplar T-0002 indisponível: livro em cadastro"`,
`…: consulta local`, `…: já emprestado`, `…: extraviado`, `…: danificado`.

- [ ] **Ciclo M14 — "Exemplar muda de estado de conservação (M14)"**
  - `deveAtualizarEstadoQuandoInformado`: `VELHO` → `estado()` = `VELHO`
  - `deveLancarExcecaoQuandoEstadoForNulo` → `IllegalArgumentException`
- [ ] **Ciclo M11 — "Exemplar diz se pode ser emprestado (M11)"**
  - `deveSerEmprestavelQuandoLivroEstiverLiberadoEExemplarDisponivel`: `liberadoComExemplares(2)`, `T-0002` → true
  - `deveNaoSerEmprestavelQuandoForDeConsultaLocal`: `T-0001` → false
  - `deveNaoSerEmprestavelQuandoLivroEstiverEmCadastro`: livro completo mas não liberado → false
  - `deveNaoSerEmprestavelQuandoEstiverDanificado`: `atualizarEstado(DANIFICADO)` → false
- [ ] **Ciclo M12 — "Saída do exemplar respeita as regras de circulação (M12)"**
  - `deveMarcarComoEmprestadoQuandoRegistrarSaida`: `situacao()` = `EMPRESTADO`
  - `deveImpedirSaidaQuandoLivroEstiverEmCadastro`: mensagem termina em `livro em cadastro`
  - `deveLancarExcecaoQuandoExemplarForDeConsultaLocal`: mensagem termina em `consulta local`
  - `deveImpedirSaidaQuandoJaEstiverEmprestado`: mensagem termina em `já emprestado`
  - `deveImpedirSaidaQuandoEstiverDanificado`: mensagem termina em `danificado`
  - Implementação: `registrarSaida()` chama `verificarDisponivel()` e muda a situação;
    `podeSerEmprestado()` usa a mesma checagem, sem lançar exceção.
- [ ] **Ciclo M13 — "Exemplar volta para a estante na devolução (M13)"**
  - `deveVoltarADisponivelQuandoRegistrarRetorno`: depois da saída, `registrarRetorno(VELHO)` → `DISPONIVEL`, `estado()` = `VELHO`
  - `deveLancarExcecaoQuandoRetornoForDeExemplarNaoEmprestado` → `IllegalStateException`
  - `deveLancarExcecaoQuandoEstadoDoRetornoForNulo` → `IllegalArgumentException`
- [ ] **Ciclo M15 — "Exemplar extraviado sai de circulação (M15)"**
  - `deveTirarDeCirculacaoQuandoRegistrarExtravio`: depois da saída → `EXTRAVIADO`, `podeSerEmprestado()` false, nova saída com mensagem terminando em `extraviado`
  - `deveLancarExcecaoQuandoRegistrarExtravioDeExemplarDisponivel` → `IllegalStateException`
- [ ] **Ciclo M10 — "Livro conta os exemplares que podem sair (M10)"** (em `LivroTest`)
  - `deveContarSoExemplaresEmprestaveisQuandoLivroEstiverLiberado`: `liberadoComExemplares(3)` → `2`; depois de uma saída → `1`
  - `deveContarZeroQuandoLivroEstiverEmCadastro` → `0`
  - `deveContarZeroQuandoHouverExemplarUnico`: `liberadoComExemplares(1)` → `0`

## Passo 3 — Pessoas

### Tarefa 8: `Usuario`

**Arquivos:** criar `dominio/Usuario.java`, `dominio/Formato.java`,
`excecao/UsuarioBloqueadoException.java`, `src/test/.../apoio/UmUsuario.java`; testes
`dominio/UsuarioTest.java`, `dominio/FormatoTest.java`.

**Interfaces:**
- Produz `Usuario(Cpf cpf, String nome, String email)` com os getters `cpf()`, `nome()`,
  `email()`, `estaBanido()`, `motivoBanimento()`, `dataBanimento()`, `suspensoAte()` (`null` se
  nunca foi suspenso) e `saldoDevedor()`.
- Produz em `Usuario`: `atualizarDados(String nome, String email)`, `banir(String motivo, LocalDate data)`,
  `aplicarSuspensao(int dias, LocalDate aPartirDe)`, `boolean estaSuspenso(LocalDate)`,
  `adicionarDebito(BigDecimal)`, `abaterDebito(BigDecimal)` e `verificarPodeEmprestar(LocalDate)`.
- Produz `Formato.dinheiro(BigDecimal)` → `"R$ 3,00"` e `Formato.data(LocalDate)` → `"26/11/2026"`.
- Produz (teste) `UmUsuario.ana()` e `UmUsuario.bruno()`, sempre com objetos novos.

E-mail válido: casa com `^[^@\s]+@[^@\s]+\.[^@\s]+$`.

- [ ] **Ciclo M17 — "Usuário nasce liberado e sem débito (M17)"**
  - `deveCriarUsuarioLiberadoESemDebitoQuandoDadosForemValidos`: `estaBanido()` false, `suspensoAte()` null, `saldoDevedor()` = `0.00`
  - `deveLancarExcecaoQuandoEmailForInvalido` (`@NullAndEmptySource` + `"ana.email.com"`, `"ana@email"`, `"@email.com"`) → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoCpfOuNomeFaltarem`: CPF `null`; nome `" "` → `IllegalArgumentException`
- [ ] **Ciclo M18 — "Usuário atualiza nome e e-mail (M18)"**
  - `deveAtualizarNomeEEmailQuandoForemValidos`: `("Ana Souza Lima", "ana.lima@email.com")`, CPF igual
  - `deveLancarExcecaoQuandoNovoEmailForInvalido`: depois da exceção, `nome()` continua `"Ana Souza"`
  - `deveAtualizarDadosQuandoUsuarioEstiverBanido`: sem exceção
- [ ] **Ciclo M19 — "Banimento é para sempre (M19)"**
  - `deveBanirParaSempreQuandoInformarMotivoEData`: `estaBanido()` true, motivo `"Danificou livros de propósito"`, data `10/11/2026`
  - `deveLancarExcecaoQuandoBanirSemMotivo` (`@NullAndEmptySource` + `" "`) → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoDataDoBanimentoForNula` → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoUsuarioJaEstiverBanido` → `IllegalStateException`
- [ ] **Ciclo M20 — "Suspensão conta da data ou soma à atual (M20)"**
  - `deveContarSuspensaoAPartirDaDataQuandoNaoEstiverSuspenso`: `(6, 20/11/2026)` → `suspensoAte()` = `26/11/2026`
  - `deveSomarAoFimQuandoJaEstiverSuspenso`: depois, `(4, 22/11/2026)` → `30/11/2026`
  - `deveContarDeNovoQuandoSuspensaoAnteriorJaTerminou`: suspenso até `26/11`, `(4, 01/12/2026)` → `05/12/2026`
  - `deveLancarExcecaoQuandoDiasNaoForemPositivos`: `0`, `-1` → `IllegalArgumentException`
- [ ] **Ciclo M21 — "Suspensão vale até a véspera do fim (M21)"**
  - `deveEstarSuspensoQuandoDataForAnteriorAoFim`: até `26/11`, `estaSuspenso(25/11)` → true
  - `deveLiberarNoProprioDiaDoFimDaSuspensao`: `estaSuspenso(26/11)` → false
  - `deveNaoEstarSuspensoQuandoNuncaFoiSuspenso` → false
- [ ] **Ciclo M22 — "Débitos somam no saldo (M22)"**
  - `deveSomarAoSaldoQuandoAdicionarDebito`: `3.00` + `80.00` → `83.00`
  - `deveManterSaldoQuandoDebitoForZero`
  - `deveLancarExcecaoQuandoDebitoForNuloOuNegativo` → `IllegalArgumentException`
- [ ] **Ciclo M23 — "Pagamento abate o saldo (M23)"**
  - `deveAbaterDoSaldoQuandoPagar`: saldo `83.00`, abate `80.00` → `3.00`
  - `deveLancarExcecaoQuandoAbatimentoForMaiorQueSaldo`: saldo `3.00`, abate `10.00` → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoAbatimentoNaoForPositivo`: `0`, `-1` → `IllegalArgumentException`
  - `deveAbaterQuandoValorTiverEscalaDiferenteDoSaldo` (ponto de atenção 4): saldo `3.00`, abate `new BigDecimal("3")` → `saldoDevedor()` = `0.00`
  - Implementação: compare com `compareTo`, nunca com `equals`.
- [ ] **Ciclo M24 — "Usuário bloqueado não pega livro (M24)"** (com `FormatoTest`)
  - `deveFormatarEmReaisQuandoValorTiverCentavos`: `dinheiro(3)` = `"R$ 3,00"`, `dinheiro(1234.5)` = `"R$ 1.234,50"`
  - `deveFormatarDataNoPadraoBrasileiro`: `data(2026-11-26)` = `"26/11/2026"`
  - `deveLiberarEmprestimoQuandoNaoHouverBloqueio`: `assertDoesNotThrow`
  - `deveBloquearQuandoUsuarioEstiverBanido`: mensagem `"Usuário banido: Danificou livros de propósito"`
  - `deveBloquearEmprestimoQuandoUsuarioEstiverSuspenso`: até `26/11`, data `25/11` → `"Usuário suspenso até 26/11/2026"`
  - `deveBloquearQuandoHouverSaldoDevedor`: saldo `3.00` → `"Usuário com débito de R$ 3,00"`
  - Implementação: `Formato.dinheiro` monta `"R$ "` + `String.format(Locale.forLanguageTag("pt-BR"), "%,.2f", valor)`.
    Não use `NumberFormat.getCurrencyInstance`, que põe um espaço não separável.

### Tarefa 9: `Administrador`

**Arquivos:** criar `dominio/Administrador.java`, `dominio/Validacao.java`,
`src/test/.../apoio/UmAdministrador.java`; alterar `Usuario.java`, `Livro.java`; teste
`dominio/AdministradorTest.java`.

**Interfaces:**
- Produz `Administrador(Cpf cpf, String nome, String email)` com os getters `cpf()`, `nome()`,
  `email()` e `estaAtivo()`. Produz (teste) `UmAdministrador.gabriel()` e `UmAdministrador.carla()`.
- Produz `final class Validacao` (package-private, criada no REFACTOR) com
  `static <T> T exigir(T valor, String campo)`, `static String exigirTexto(String valor, String campo)`
  e `static String exigirEmail(String email)`. As mensagens são `"Informe o <campo>"` e
  `"E-mail inválido"`.

- [ ] **Ciclo M25 — "Administrador nasce ativo e sem senha (M25)"**
  - `deveCriarAtivoQuandoDadosForemValidos`: `estaAtivo()` true
  - `deveLancarExcecaoQuandoEmailDoAdministradorForInvalido`: `"gabriel@"` → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoNomeEstiverEmBranco` → `IllegalArgumentException`
- [ ] **REFACTOR obrigatório — "validações comuns do domínio numa classe só"**: leve para
  `Validacao` as checagens repetidas de `Usuario`, `Administrador` e `Livro`; todos os testes
  continuam verdes.

## Passo 4 — Circulação

### Tarefa 10: `Emprestimo`

**Arquivos:** criar `dominio/Emprestimo.java`, `PoliticaCirculacao.java`, `SituacaoEmprestimo.java`,
`excecao/RenovacaoNaoPermitidaException.java`; teste `dominio/EmprestimoTest.java`.

**Interfaces:**
- Produz `record PoliticaCirculacao(int limiteEmprestimos, int prazoDias, int maxRenovacoes,
  int prazoReposicaoDias)` com `static PoliticaCirculacao padrao()` → `(3, 7, 2, 30)`.
- Produz `enum SituacaoEmprestimo { ATIVO, DEVOLVIDO, PERDIDO, REPOSTO }`.
- Produz `Emprestimo(Usuario usuario, Exemplar exemplar, LocalDate dataEmprestimo, PoliticaCirculacao politica)`
  com os getters `usuario()`, `exemplar()`, `dataEmprestimo()`, `dataPrevistaDevolucao()`,
  `dataDevolucao()`, `situacao()`, `quantidadeRenovacoes()`, `dataPerda()` e `politica()`.
- Produz em `Emprestimo`: `boolean estaAtrasado(LocalDate)`, `int calcularDiasAtraso(LocalDate)`,
  `void registrarDevolucao(LocalDate, EstadoConservacao)`, `void verificarPodeRenovar(LocalDate)`
  (lança sem alterar nada), `void renovar(LocalDate)` e `void registrarPerda(LocalDate)`.
- O construtor não mexe no exemplar: quem chama `registrarSaida()` é o serviço. Os testes criam
  o empréstimo com um método auxiliar `novoEmprestimo(LocalDate)`, que faz a saída do `T-0002` de
  `liberadoComExemplares(2)` e cria o empréstimo para `UmUsuario.ana()` com `padrao()`.

- [ ] **Ciclo M27 — "Empréstimo calcula a data prevista (M27)"**
  - `deveCalcularDataPrevistaComPrazoDaPoliticaQuandoCriar`: `10/11/2026` → `17/11/2026`, `ATIVO`, `0` renovações
  - `deveCalcularDataPrevistaQuandoPrazoVirarOAno`: `28/12/2026` → `04/01/2027`
  - `deveLancarExcecaoQuandoArgumentoForNulo`: cada um dos 4 argumentos nulo → `IllegalArgumentException` (`assertAll`)
- [ ] **Ciclo M28 — "Empréstimo diz se está atrasado (M28)"**
  - `deveEstarEmDiaNaDataPrevista`: `17/11` → false; `deveEstarAtrasadoNoDiaSeguinteAoPrazo`: `18/11` → true
  - `deveNaoEstarAtrasadoQuandoJaFoiDevolvido`: devolvido em `20/11`, consulta em `25/11` → false
- [ ] **Ciclo M29 — "Empréstimo conta os dias de atraso (M29)"**
  - `deveContarDiasDeAtrasoQuandoPassarDoPrazo` (`@CsvSource` data, dias): `2026-11-20 → 3`, `2026-11-15 → 0`, `2026-11-17 → 0`, `2027-01-16 → 60`
- [ ] **Ciclo M30 — "Devolução acontece uma vez só (M30)"**
  - `deveRegistrarDevolucaoQuandoDataForValida`: `(20/11, VELHO)` → `DEVOLVIDO`, `dataDevolucao()` `20/11`, exemplar `DISPONIVEL` e `VELHO`
  - `deveAceitarDevolucaoNoMesmoDiaDoEmprestimo`: `10/11` → sem exceção
  - `deveLancarExcecaoQuandoDataDevolucaoForAnteriorAoEmprestimo`: `09/11` → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoEmprestimoJaFoiDevolvido` → `IllegalStateException`
- [ ] **Ciclo M31 — "Renovação só em dia e até duas vezes (M31)"**
  - `deveRenovarPorSeteDiasAPartirDaDataQuandoEstiverEmDia`: `renovar(15/11)` → `22/11`, 1 renovação; `renovar(20/11)` → `27/11`, 2
  - `deveImpedirRenovacaoQuandoLimiteDeRenovacoesForAtingido`: 3ª em `25/11` → `RenovacaoNaoPermitidaException("Limite de 2 renovações atingido")`
  - `deveImpedirRenovacaoQuandoEmprestimoEstiverAtrasado`: `renovar(18/11)` sem renovar antes → `RenovacaoNaoPermitidaException("Empréstimo atrasado não pode ser renovado")`
  - `deveLancarExcecaoQuandoRenovarEmprestimoDevolvido` → `IllegalStateException`
  - `deveLancarExcecaoQuandoDataDaRenovacaoForAnteriorAoEmprestimo` → `IllegalArgumentException`
  - Implementação: `renovar` chama `verificarPodeRenovar`. A ordem das checagens é argumento,
    depois situação, depois regra.
- [ ] **Ciclo M32 — "Perda marca empréstimo e exemplar (M32)"**
  - `deveMarcarComoPerdidoQuandoRegistrarPerda`: `20/11` → `PERDIDO`, `dataPerda()` `20/11`, exemplar `EXTRAVIADO`
  - `deveLancarExcecaoQuandoPerdaForDeEmprestimoDevolvido` → `IllegalStateException`
  - `deveLancarExcecaoQuandoDataDaPerdaForAnteriorAoEmprestimo` → `IllegalArgumentException`

## Passo 5 — Verificação

### Tarefa 11: `CodigoVerificacao`

**Arquivos:** criar `dominio/CodigoVerificacao.java`, `dominio/Finalidade.java`,
`excecao/CodigoVerificacaoInvalidoException.java`; teste `dominio/CodigoVerificacaoTest.java`.

**Interfaces:**
- Produz `enum Finalidade { ACESSO_ADMINISTRATIVO, EMPRESTIMO, RENOVACAO, PERDA }`.
- Produz `CodigoVerificacao(Cpf cpf, String codigo, Finalidade finalidade, Instant emitidoEm)` com os
  getters `cpf()`, `finalidade()`, `emitidoEm()`, `expiraEm()`, `tentativasErradas()` e
  `estaUsado()`. Não tem getter do código, e o `toString` não o mostra.
- Produz em `CodigoVerificacao`: `boolean estaExpirado(Instant agora)` e
  `void validar(String codigoDigitado, Finalidade finalidade, Instant agora)`.

Mensagens: errou com tentativas sobrando → `"Código incorreto. Restam 2 tentativas."` (com uma só:
`"Código incorreto. Resta 1 tentativa."`); terceira errada → `"Código incorreto. Peça um novo código."`;
expirado, usado ou invalidado → `"Código expirado ou já usado. Peça um novo código."`.

- [ ] **Ciclo M38 — "Código vale cinco minutos (M38)"**
  - `deveExpirarCincoMinutosDepoisQuandoEmitir`: emitido `2026-11-10T13:00:00Z` → `expiraEm()` = `13:05:00Z`
  - `deveLancarExcecaoQuandoCodigoNaoTiverSeisDigitos` (`@ValueSource`): `"12345"`, `"1234567"`, `"12a456"` → `IllegalArgumentException`
  - `deveLancarExcecaoQuandoArgumentoForNulo` → `IllegalArgumentException`
  - `deveEsconderCodigoQuandoConverterParaTexto`: `toString()` não contém `"482913"`
- [ ] **Ciclo M39 — "Código expira no quinto minuto (M39)"**
  - `deveContinuarValidoUmSegundoAntesDeExpirar`: `13:04:59Z` → false; `deveExpirarNoInstanteDeExpiracao`: `13:05:00Z` → true
- [ ] **Ciclo M40 — "Código só vale uma vez, para uma finalidade (M40)"**
  - `deveMarcarComoUsadoQuandoCodigoEstiverCerto`: `estaUsado()` true
  - `deveRecusarCodigoQuandoJaTiverSidoUsado`: segunda vez → `CodigoVerificacaoInvalidoException`
  - `deveRecusarCodigoQuandoFinalidadeForDiferente`: código de `EMPRESTIMO` em `RENOVACAO` → exceção, `tentativasErradas()` = 1
  - `deveInformarTentativasRestantesQuandoErrarCodigo`: 1º erro → `"Código incorreto. Restam 2 tentativas."`
  - `deveInvalidarCodigoQuandoErrarTresVezes`: 3 erros e depois o código certo → exceção
  - `deveRecusarCodigoQuandoEstiverExpirado`: certo às `13:05:00Z` → exceção
  - `deveAceitarCodigoQuandoVierComEspacosNasPontas` (ponto de atenção 3): `" 482913 "` → `estaUsado()` true

### Tarefa 12: `GeradorCodigoAleatorio`

**Arquivos:** criar `verificacao/GeradorCodigo.java`, `verificacao/GeradorCodigoAleatorio.java`;
teste `verificacao/GeradorCodigoAleatorioTest.java`.

**Interfaces:** Produz `interface GeradorCodigo { String gerar(); }`, `GeradorCodigoAleatorio()`
(usa `new SecureRandom()`) e `GeradorCodigoAleatorio(Random random)`.

- [ ] **Ciclo M46 — "Gerador cria códigos de seis dígitos (M46)"**
  - `deveGerarSeisDigitosQuandoChamarMilVezes`: 1.000 chamadas, todas casam com `\d{6}`
  - `deveCompletarComZerosAEsquerdaQuandoNumeroForPequeno`: `Random` cujo `nextInt(int)` devolve `4271` → `"004271"`

## Passo 6 — Acesso e auditoria

### Tarefa 13: `Sessao` e `RegistroAuditoria`

**Arquivos:** criar `dominio/Sessao.java`, `dominio/RegistroAuditoria.java`, `dominio/TipoAcao.java`;
testes `dominio/SessaoTest.java`, `dominio/RegistroAuditoriaTest.java`.

**Interfaces:**
- Produz `Sessao(Administrador administrador, Instant inicio)` com os getters `administrador()`,
  `iniciadaEm()` e `ultimoUsoEm()`.
- Produz `enum TipoAcao` com os 24 valores da seção 6 dos requisitos e `boolean ehDeSeguranca()`,
  que é true só para `SESSAO_EXPIRADA`, `ACESSO_NEGADO`, `CODIGO_INCORRETO` e `VERIFICACAO_BLOQUEADA`.
- Produz `RegistroAuditoria(long sequencia, Instant dataHora, Cpf cpfOperador, TipoAcao acao, String alvo,
  Cpf cpfUsuario, String detalhes, String hashAnterior)`, com getters de todos os campos mais
  `hash()`, e a constante `RegistroAuditoria.HASH_INICIAL` = `"0".repeat(64)`.

Registro de exemplo: `(1, 2026-11-10T13:00:00Z, gabriel, EMPRESTIMO, "T-0002", ana, "Dom Casmurro", HASH_INICIAL)`.

- [ ] **Ciclo M41 — "Sessão começa com último uso igual ao início (M41)"**
  - `deveAbrirSessaoComUltimoUsoIgualAoInicio`. A checagem de administrador inativo fica para a Tarefa 34, junto com M26.
- [ ] **Ciclo M44 — "Registro de auditoria calcula o próprio hash (M44)"**
  - `deveCalcularHashSha256DosCamposEncadeadoAoAnterior`: exemplo → `hash()` = `"ffbca534717db4313b083bcc7cd10415e17241b7f96a14c6bbb5a717814efa90"`
  - `deveMudarHashQuandoDetalhesMudarem`: detalhes `"Dom Casmurro!"` → `"56fdb1e2384e17de65ee2b812324b0f74b54868f90b4386ddec0d5f05212b325"`
  - `deveExigirOperadorQuandoAcaoNaoForDeSeguranca`: `EMPRESTIMO` sem operador → `IllegalArgumentException`
  - `deveAceitarSemOperadorQuandoAcaoForDeSeguranca`: `CODIGO_INCORRETO` sem operador → sem exceção
  - `deveLancarExcecaoQuandoCampoObrigatorioFaltar`: data, ação, alvo em branco, `hashAnterior` nulo → `IllegalArgumentException`
  - Implementação: SHA-256 em UTF-8, hex minúsculo, dos campos unidos por `|` nesta ordem:
    `sequencia | dataHora.toString() | cpfOperador.valor() ou "" | acao.name() | alvo |
    cpfUsuario.valor() ou "" | detalhes ou "" | hashAnterior`.

### Tarefa 14: `AuditoriaService`

**Arquivos:** criar `repositorio/AuditoriaRepository.java`, `dominio/FiltroAuditoria.java`,
`servico/AuditoriaService.java`, `src/test/.../repositorio/memoria/AuditoriaRepositoryMemoria.java`,
`src/test/.../apoio/RelogioDeTeste.java`; teste `servico/AuditoriaServiceTest.java`.

**Interfaces:**
- Produz `interface AuditoriaRepository { void adicionar(RegistroAuditoria r);
  Optional<RegistroAuditoria> ultimo(); List<RegistroAuditoria> listarTodos(); }`. `listarTodos`
  vem em ordem de sequência.
- Produz `record FiltroAuditoria(Cpf cpfOperador, Cpf cpfUsuario, TipoAcao acao, LocalDate de,
  LocalDate ate)` com `static FiltroAuditoria vazio()`. Qualquer campo pode ser `null`.
- Produz `AuditoriaService(AuditoriaRepository repositorio, Clock relogio)`, com
  `RegistroAuditoria registrar(Administrador operador, TipoAcao acao, String alvo, Cpf cpfUsuario,
  String detalhes)` e `List<RegistroAuditoria> consultar(FiltroAuditoria filtro)`.
- Produz (teste) `RelogioDeTeste(Instant inicio) extends Clock` com `instant()`, zona UTC e
  `void avancar(Duration)`.

- [ ] **Ciclo M53 — "Auditoria grava com a hora do relógio e encadeia (M53)"**
  - `deveGravarRegistroComHoraDoRelogioQuandoRegistrar`: relógio `2026-11-10T13:00:00Z` → `dataHora` igual, sequência `1`, `hashAnterior` = `HASH_INICIAL`, repositório com 1 registro
  - `deveEncadearAoUltimoQuandoJaHouverRegistros`: segundo registro → sequência `2`, `hashAnterior` = `hash()` do primeiro
  - `deveLancarExcecaoQuandoOperadorForNulo` → `IllegalArgumentException`
- [ ] **Ciclo M55 — "Auditoria filtra e ordena do mais recente (M55)"**
  - `deveListarDoMaisRecenteAoMaisAntigoQuandoFiltroForVazio`: 3 registros → sequências `3, 2, 1`
  - `deveFiltrarPorTodosOsCamposPreenchidos`: operador, usuário, ação e período (datas inclusivas), cada um sozinho e todos juntos
  - `deveFiltrarPeloDiaLocalQuandoHorarioUtcForDoDiaSeguinte` (ponto de atenção 5): registro em
    `2026-11-11T02:30:00Z`; filtro `de = ate = 10/11/2026` → aparece; `de = ate = 11/11/2026` → não aparece
  - Implementação: data local = `dataHora` em `ZoneId.of("America/Cuiaba")`.

### Tarefa 15: `VerificacaoService`

**Arquivos:** criar `verificacao/CanalEnvio.java`, `excecao/EnvioCodigoException.java`,
`servico/VerificacaoService.java`, `src/test/.../apoio/GeradorCodigoFixo.java`,
`src/test/.../apoio/CanalEnvioFalso.java`; teste `servico/VerificacaoServiceTest.java`.

**Interfaces:**
- Produz `interface CanalEnvio { void enviar(String email, String assunto, String mensagem); }`.
  Em caso de falha, lança `EnvioCodigoException`.
- Produz `VerificacaoService(GeradorCodigo gerador, CanalEnvio canal, Clock relogio)`, com
  `String enviarCodigo(Cpf cpf, String email, Finalidade finalidade, String descricaoOperacao)`
  (devolve o e-mail mascarado) e `void validarCodigo(Cpf cpf, Finalidade finalidade, String codigoDigitado)`.
- Produz (teste) `GeradorCodigoFixo(String... codigos)`: devolve os códigos em sequência e repete o
  último; sem argumentos, usa `"123456"`.
- Produz (teste) `CanalEnvioFalso` com `record Mensagem(String email, String assunto, String texto)`,
  `List<Mensagem> enviadas()`, `Mensagem ultima()` e `void falharNoProximoEnvio()`.

Texto do e-mail: assunto `"Código de verificação da biblioteca"`; corpo `"Código " + codigo +
" para confirmar " + descricaoOperacao + ". Vale 5 minutos."`. Máscara: parte antes do `@` com a
primeira e a última letra e `*` no meio (com até 2 letras: a primeira e um `*`); o domínio fica igual.

- [ ] **Ciclo M57 — "Código é enviado por e-mail com a descrição da operação (M57)"**
  - `deveEnviarCodigoComDescricaoDaOperacaoQuandoSolicitar`: descrição `"o empréstimo de Dom Casmurro (tombo T-0002)"` →
    texto `"Código 123456 para confirmar o empréstimo de Dom Casmurro (tombo T-0002). Vale 5 minutos."` para `ana@email.com`
  - `deveDevolverEmailMascaradoQuandoEnviar`: `ana@email.com` → `"a*a@email.com"`; `gabrielmallezan@gmail.com` → `"g*************n@gmail.com"`
  - `deveInvalidarCodigoAnteriorQuandoEnviarNovo`: gerador `"111111"`, `"222222"`; validar `111111` → `CodigoVerificacaoInvalidoException`; `222222` → aceito
  - `deveLancarExcecaoQuandoEnvioDoCodigoFalhar`: canal falha → `EnvioCodigoException`; depois, `validarCodigo` com `123456` → `CodigoVerificacaoInvalidoException`
  - Implementação: guarde o código num `Map<Cpf, CodigoVerificacao>` só depois que o envio der certo.
- [ ] **Ciclo M58 — "Código é conferido com o relógio (M58)"**
  - `deveAceitarCodigoQuandoEstiverCertoEDentroDaValidade`
  - `deveRecusarQuandoNaoHouverCodigoAtivo` → `CodigoVerificacaoInvalidoException("Nenhum código ativo. Peça um novo código.")`
  - `deveRecusarCodigoQuandoPassaremCincoMinutos`: relógio avança 5 min → exceção
  - `deveRecusarQuandoCodigoForDeOutroCpf`: enviado para Ana, conferido com Bruno → exceção

### Tarefa 16: `AcessoService`

**Arquivos:** criar `repositorio/AdministradorRepository.java`, `repositorio/Transacao.java`,
`servico/ProvedorDeOperador.java`, `servico/AcessoService.java`,
`excecao/SessaoInvalidaException.java`, `excecao/OperacaoNaoAutorizadaException.java`,
`src/test/.../repositorio/memoria/AdministradorRepositoryMemoria.java`,
`src/test/.../apoio/TransacaoDireta.java`; teste `servico/AcessoServiceTest.java`.

**Interfaces:**
- Produz `interface AdministradorRepository { void salvar(Administrador a);
  Optional<Administrador> buscarPorCpf(Cpf cpf); List<Administrador> listarTodos(); boolean estaVazio(); }`.
- Produz `interface Transacao { void executar(Runnable operacao); <T> T executar(Supplier<T> operacao); }`
  e `interface ProvedorDeOperador { Administrador operadorAtual(); }`.
- Produz `AcessoService(AdministradorRepository administradores, VerificacaoService verificacao,
  AuditoriaService auditoria, Transacao transacao, Clock relogio) implements ProvedorDeOperador`,
  com `Administrador cadastrarAdministrador(Cpf, String nome, String email)`,
  `String solicitarAcesso(Cpf)`, `Sessao entrar(Cpf, String codigo)`, `void sair()` e
  `Administrador operadorAtual()`.
- Os testes usam o `AuditoriaService` e o `VerificacaoService` de verdade, com repositório em
  memória, `GeradorCodigoFixo`, `CanalEnvioFalso` e `RelogioDeTeste`. O administrador inicial é
  gravado direto no repositório em memória.

Descrição do código de acesso: `"a entrada no sistema da biblioteca"`. Mensagens:
`"Este CPF não tem acesso administrativo"` (M49) e `"Entre no sistema para continuar"` (sem sessão).

- [ ] **Ciclo M49 — "Administrador pede o código de acesso (M49)"**
  - `deveEnviarCodigoDeAcessoQuandoCpfForDeAdministradorAtivo`: devolve `"g*************n@gmail.com"`; e-mail enviado para `gabrielmallezan@gmail.com` com a descrição
  - `deveNegarAcessoQuandoCpfNaoForDeAdministradorAtivo`: CPF `935.411.347-80` → `OperacaoNaoAutorizadaException`, nenhum e-mail
- [ ] **Ciclo M50 — "Administrador entra com o código (M50)"**
  - `deveAbrirSessaoQuandoCodigoEstiverCerto`: `administrador()` = Gabriel, `iniciadaEm()` = relógio; auditoria `ENTRADA` com alvo `"***.444.777-**"`
  - `deveRecusarEntradaQuandoCodigoEstiverErrado`: `"999999"` → `CodigoVerificacaoInvalidoException`, sem sessão e sem `ENTRADA`
  - `deveEncerrarSessaoAnteriorQuandoOutroAdministradorEntrar`: Carla entra depois de Gabriel → `operadorAtual()` = Carla
- [ ] **Ciclo M52 — "Serviços sabem quem está conectado (M52)"**
  - `deveRetornarOperadorQuandoHouverSessao`
  - `deveLancarExcecaoQuandoNaoHouverSessao` → `SessaoInvalidaException("Entre no sistema para continuar")`
  - A expiração por 30 minutos fica para a Tarefa 35.
- [ ] **Ciclo M51 — "Administrador sai do sistema (M51)"**
  - `deveRegistrarSaidaEEncerrarSessaoQuandoSair`: auditoria `SAIDA`; depois, `operadorAtual()` → `SessaoInvalidaException`
  - `deveIgnorarQuandoSairSemSessao`: nenhum registro novo
- [ ] **Ciclo M47 — "Primeiro administrador entra sem sessão, os outros não (M47)"**
  - `deveCriarPrimeiroAdministradorSemSessaoQuandoSistemaEstiverVazio`: gravado; auditoria `ADMINISTRADOR_CADASTRADO` com operador = ele mesmo
  - `deveExigirSessaoQuandoJaExistirAdministrador`: segundo sem sessão → `SessaoInvalidaException`, nada gravado
  - `deveCadastrarComSessaoQuandoAdministradorEstiverConectado`: Gabriel conectado cadastra Carla → operador do registro = Gabriel
  - `deveLancarExcecaoQuandoCpfDoAdministradorJaExistir` → `RegistroDuplicadoException`
  - Implementação: gravar e auditar dentro de `transacao.executar`.

## Passo 7 — Serviços de negócio

Todo teste de alteração confere três coisas: o retorno, o que ficou no repositório e o registro de
auditoria (ação, operador, alvo, `cpfUsuario`, detalhes). Todo serviço tem um teste
`naoDeveGravarNemAuditarQuandoNaoHouverSessao` para a primeira operação de alteração, usando
`SemSessao`. Siga a receita da seção 4 da arquitetura.

### Tarefa 17: `UsuarioService`

**Arquivos:** criar `repositorio/UsuarioRepository.java`, `excecao/RegistroNaoEncontradoException.java`,
`servico/UsuarioService.java`, `servico/Alteracoes.java`,
`src/test/.../repositorio/memoria/UsuarioRepositoryMemoria.java`, `src/test/.../apoio/OperadorFixo.java`,
`src/test/.../apoio/SemSessao.java`; teste `servico/UsuarioServiceTest.java`.

**Interfaces:**
- Produz `interface UsuarioRepository { void salvar(Usuario u); Optional<Usuario> buscarPorCpf(Cpf cpf);
  List<Usuario> listarTodos(); default Usuario obter(Cpf cpf) }`. O `obter` lança
  `RegistroNaoEncontradoException("Usuário não encontrado: " + cpf.mascarado())`.
- Produz `UsuarioService(UsuarioRepository usuarios, ProvedorDeOperador acesso, AuditoriaService auditoria,
  Transacao transacao)`, com `Usuario cadastrar(Usuario)`, `Usuario buscarPorCpf(Cpf)`,
  `void atualizarDados(Cpf, String nome, String email)`, `void banir(Cpf, String motivo, LocalDate data)`
  e `void registrarPagamento(Cpf, BigDecimal valor)`.
- Produz `final class Alteracoes` (package-private, criada no ciclo M61) com
  `Alteracoes campo(String nome, Object antes, Object depois)` e `String texto()`. O texto traz só
  os campos que mudaram, no formato `nome: antes → depois`, unidos por `"; "`. `null` vira `—`,
  `BigDecimal` passa por `Formato.dinheiro` e `LocalDate` por `Formato.data`.
- Produz (teste) `OperadorFixo(Administrador)` e `SemSessao`, que lança
  `SessaoInvalidaException("Entre no sistema para continuar")`.

Nos registros deste serviço, o alvo é `cpf.mascarado()` e o `cpfUsuario` é o CPF da pessoa.

- [ ] **Ciclo M59 — "Cadastro de usuário exige sessão e CPF novo (M59)"**
  - `deveGravarEAuditarQuandoCadastrarUsuario`: `USUARIO_CADASTRADO`, operador Gabriel, alvo `"***.982.247-**"`, detalhes `"Ana Souza"`
  - `deveLancarExcecaoQuandoCpfJaEstiverCadastrado` → `RegistroDuplicadoException`
  - `naoDeveGravarNemAuditarQuandoNaoHouverSessao` → `SessaoInvalidaException`, repositório e trilha vazios
- [ ] **Ciclo M60 — "Busca de usuário pelo CPF (M60)"**
  - `deveRetornarUsuarioQuandoCpfExistir`
  - `deveLancarExcecaoQuandoCpfNaoExistir`: `RegistroNaoEncontradoException` cuja mensagem contém `"***.982.247-**"` e não contém `"52998224725"`
- [ ] **Ciclo M61 — "Alteração de dados registra antes e depois (M61)"**
  - `deveAtualizarEAuditarAntesEDepoisQuandoAlterarDados`: detalhes = `"nome: Ana Souza → Ana Souza Lima; e-mail: ana@email.com → ana.lima@email.com"`
- [ ] **Ciclo M62 — "Banimento pelo serviço registra o motivo (M62)"**
  - `deveBanirEAuditarMotivoQuandoBanir`: `USUARIO_BANIDO`, detalhes `"Danificou livros de propósito"`; `estaBanido()` no repositório
- [ ] **Ciclo M63 — "Pagamento abate o saldo e fica na trilha (M63)"**
  - `deveAbaterEAuditarQuandoRegistrarPagamento`: saldo `3.00`, paga `3.00` → `0.00`; detalhes `"Pagamento de R$ 3,00; saldo restante R$ 0,00"`
  - `deveLancarExcecaoQuandoPagamentoForMaiorQueSaldo` → `IllegalArgumentException`, nada auditado

### Tarefa 18: `AcervoService`

**Arquivos:** criar `repositorio/LivroRepository.java`, `repositorio/EmprestimoRepository.java`,
`excecao/LivroComEmprestimosException.java`, `servico/AcervoService.java`,
`src/test/.../repositorio/memoria/LivroRepositoryMemoria.java`,
`src/test/.../repositorio/memoria/EmprestimoRepositoryMemoria.java`; teste `servico/AcervoServiceTest.java`.

**Interfaces:**
- Produz `interface LivroRepository`, com `void salvar(Livro l)`, `Optional<Livro> buscarPorIsbn(Isbn)`,
  `Optional<Exemplar> buscarExemplar(String codigoTombo)`, `List<Livro> listarTodos()`,
  `void remover(Isbn)` e os métodos default `Livro obter(Isbn)` e `Exemplar obterExemplar(String codigoTombo)`.
  Os dois `obter` lançam `RegistroNaoEncontradoException`, com as mensagens `"Livro não encontrado: <isbn>"`
  e `"Exemplar não encontrado: <tombo>"`.
- Produz `interface EmprestimoRepository`, com `void salvar(Emprestimo e)`,
  `List<Emprestimo> listarPorUsuario(Cpf)`, `Optional<Emprestimo> buscarAtivoPorTombo(String codigoTombo)`,
  `List<Emprestimo> listarAtivos()` e `boolean existeParaLivro(Isbn)`.
- Produz `AcervoService(LivroRepository livros, EmprestimoRepository emprestimos, ProvedorDeOperador acesso,
  AuditoriaService auditoria, Transacao transacao)` com M64–M72, nas assinaturas da seção 7 dos requisitos.
- Os repositórios em memória guardam os próprios objetos. O de empréstimos só inclui um objeto
  que ainda não está na lista; a comparação é por identidade.

Nos registros deste serviço, o alvo é `isbn.valor()` para livro e o tombo para exemplar. O
`cpfUsuario` fica nulo.

- [ ] **Ciclo M64 — "Cadastro de livro começa pelo ISBN (M64)"**
  - `deveIniciarEAuditarQuandoIsbnForNovo`: `LIVRO_INICIADO`, alvo `"9780306406157"`, detalhes `"Dom Casmurro"`
  - `deveLancarExcecaoQuandoIsbnJaEstiverCadastrado` → `RegistroDuplicadoException`
  - `naoDeveGravarNemAuditarQuandoNaoHouverSessao`
- [ ] **Ciclo M65 — "Dados bibliográficos pelo serviço registram antes e depois (M65)"**
  - `deveRegistrarDadosEAuditarAntesEDepois`: primeira vez → detalhes contém `"autor: — → Machado de Assis"`; corrigir só a edição → detalhes = `"edição: 1 → 2"`
  - Rótulos: `título`, `autor`, `editora`, `ano`, `edição`, `páginas`, `gênero`.
- [ ] **Ciclo M66 — "Processamento técnico pelo serviço (M66)"**
  - `deveRegistrarProcessamentoEAuditar`: `LIVRO_PROCESSAMENTO_ALTERADO`, detalhes `"classificação: — → 869.3; localização: — → Estante 4, prateleira B; valor de reposição: — → R$ 80,00"`
- [ ] **Ciclo M67 — "Tombo é único no acervo inteiro (M67)"**
  - `deveCadastrarExemplarEAuditar`: `EXEMPLAR_CADASTRADO`, alvo `"T-0001"`
  - `deveLancarExcecaoQuandoTomboJaExistirEmOutroLivro`: `T-0001` em Dom Casmurro e de novo em Vidas Secas → `RegistroDuplicadoException`
- [ ] **Ciclo M68 — "Mudança de estado do exemplar fica na trilha (M68)"**
  - `deveAlterarEstadoEAuditarEstadoAnterior`: detalhes `"estado: NOVO → DANIFICADO"`
  - `deveLancarExcecaoQuandoTomboNaoExistir` → `RegistroNaoEncontradoException`
- [ ] **Ciclo M69 — "Liberação pelo serviço (M69)"**
  - `deveLiberarEAuditarQuandoNaoHouverPendencias`: `LIVRO_LIBERADO`
  - `deveImpedirLiberacaoQuandoHouverPendencia` → `CadastroIncompletoException`, nada auditado
- [ ] **Ciclo M70 — "Pesquisa por título ou autor (M70)"**
  - Cenário: Dom Casmurro e Memórias Póstumas liberados; Vidas Secas em cadastro, só com ISBN e título.
  - `deveEncontrarPorTituloOuAutorSemDiferenciarMaiusculas`: `("machado", null)` → `[Dom Casmurro, Memórias Póstumas de Brás Cubas]`
  - `deveFiltrarPorSituacaoQuandoInformada`: `("", EM_CADASTRO)` → `[Vidas Secas]`
  - `deveListarTodosQuandoTermoForVazioESituacaoNula` → os 3, em ordem alfabética de título
  - Implementação: livro sem autor só casa pelo título.
- [ ] **Ciclo M71 — "Busca de livro pelo ISBN (M71)"**
  - `deveRetornarLivroQuandoIsbnExistir`; `deveLancarExcecaoQuandoIsbnNaoExistir` → `RegistroNaoEncontradoException`
- [ ] **Ciclo M72 — "Livro com histórico de empréstimo não sai do acervo (M72)"**
  - `deveRemoverLivroEExemplaresEAuditar`: `LIVRO_EXCLUIDO`, detalhes `"Dom Casmurro — Machado de Assis"`; `buscarExemplar("T-0002")` vazio
  - `deveImpedirExclusaoQuandoLivroTiverHistoricoDeEmprestimo`: um empréstimo do `T-0002` no repositório → `LivroComEmprestimosException`
  - `deveLancarExcecaoQuandoRemoverIsbnInexistente` → `RegistroNaoEncontradoException`

### Tarefa 19: `EmprestimoService`, empréstimo

**Arquivos:** criar `excecao/EmprestimoNaoPermitidoException.java`, `servico/EmprestimoService.java`;
teste `servico/EmprestimoServiceTest.java`.

**Interfaces:**
- Produz `EmprestimoService(EmprestimoRepository emprestimos, LivroRepository livros, UsuarioRepository usuarios,
  VerificacaoService verificacao, ProvedorDeOperador acesso, AuditoriaService auditoria, Transacao transacao,
  CalculadoraMulta multa, CalculadoraSuspensao suspensao, PoliticaCirculacao politica)`.
- Produz `String solicitarConfirmacao(Finalidade, Cpf, String codigoTombo, LocalDate data)` e
  `Emprestimo emprestar(Cpf, String codigoTombo, String codigo, LocalDate data)`.
- Cenário do `@BeforeEach`: Ana e Bruno gravados; os 4 livros dos dados padrão liberados, com 4
  exemplares cada; `OperadorFixo(gabriel)`; `VerificacaoService` real com `GeradorCodigoFixo()` e
  `CanalEnvioFalso`; data `10/11/2026`. O código certo é sempre `123456`, depois de
  `solicitarConfirmacao`.

Descrições: `"o empréstimo de <título> (tombo <tombo>)"`, `"a renovação de <título> (tombo <tombo>)"`
e `"a perda de <título> (tombo <tombo>), com cobrança de <valor>"`.

Mensagens: `"Usuário com empréstimo atrasado"` (`UsuarioBloqueadoException`),
`"Limite de 3 empréstimos atingido"` e `"Usuário já está com um exemplar de <título>"`
(`EmprestimoNaoPermitidoException`).

- [ ] **Ciclo M73a — "Código de empréstimo só sai depois das regras (M73)"**
  - `deveEnviarCodigoComDescricaoQuandoEmprestimoForPermitido`: devolve `"a*a@email.com"`; texto contém `"o empréstimo de Dom Casmurro (tombo T-0002)"`
  - `naoDeveEnviarCodigoQuandoUsuarioEstiverBanido` → `UsuarioBloqueadoException`, canal sem mensagens
  - `deveLancarExcecaoQuandoFinalidadeForAcessoAdministrativo` → `IllegalArgumentException`
  - `naoDeveEnviarCodigoQuandoNaoHouverSessao` → `SessaoInvalidaException`
  - Implementação: um método privado `validarEmprestimo(Usuario, Exemplar, LocalDate)` serve ao
    M73 e ao M74. A ordem é `verificarPodeEmprestar` (M24), atraso, limite, mesmo livro e, por
    último, `exemplar.verificarDisponivel()`.
- [ ] **Ciclo M74 — "Empréstimo confirmado pelo código do usuário (M74)"**
  - `deveEmprestarEAuditarQuandoTudoEstiverValido`: previsto `17/11/2026`, exemplar `EMPRESTADO`, empréstimo no repositório; `EMPRESTIMO` com alvo `"T-0002"`, `cpfUsuario` Ana, detalhes `"Dom Casmurro, devolução prevista para 17/11/2026"`
  - `deveBloquearEmprestimoQuandoUsuarioTiverAtraso`: Ana com empréstimo de `01/11` (previsto `08/11`) → `UsuarioBloqueadoException`
  - `deveImpedirEmprestimoQuandoLimiteForAtingido`: 3 ativos de livros diferentes, pede Iracema → `EmprestimoNaoPermitidoException`
  - `deveImpedirEmprestimoQuandoJaTiverExemplarDoMesmoLivro`: com `T-0002`, pede `T-0003` → `EmprestimoNaoPermitidoException`
  - `deveImpedirEmprestimoQuandoExemplarForDeConsultaLocal`: `T-0001` → `ExemplarIndisponivelException`
  - `deveImpedirEmprestimoQuandoCodigoForInvalido`: `"999999"` → `CodigoVerificacaoInvalidoException`; nada gravado, exemplar `DISPONIVEL`, trilha vazia
  - `naoDeveGastarCodigoQuandoEmprestimoForRecusado`: código pedido para `T-0002`; `emprestar` do `T-0001` com `123456` → `ExemplarIndisponivelException`; depois, `emprestar` do `T-0002` com `123456` → dá certo
  - Implementação: receita da arquitetura; dentro da transação, salve `exemplar.livro()` e o empréstimo.

### Tarefa 20: `EmprestimoService`, renovação, devolução e perda

**Arquivos:** criar `dominio/ResultadoDevolucao.java`; alterar `EmprestimoService.java`; teste
`servico/EmprestimoServiceTest.java`.

**Interfaces:**
- Produz `record ResultadoDevolucao(int diasAtraso, BigDecimal multa, BigDecimal valorReposicao,
  LocalDate suspensoAte)`. Sem atraso, `suspensoAte` é `null`; os valores em dinheiro são `0.00`
  quando não há cobrança.
- Produz `LocalDate renovar(Cpf, String codigoTombo, String codigo, LocalDate data)`,
  `ResultadoDevolucao devolver(String codigoTombo, LocalDate data, EstadoConservacao estado)` e
  `ResultadoDevolucao registrarPerda(Cpf, String codigoTombo, String codigo, LocalDate data)`.
- Auxiliar do teste: `emprestarDomCasmurro()` pede o código e empresta o `T-0002` para Ana em `10/11/2026`.
- Implementação: o empréstimo vem de `emprestimos.buscarAtivoPorTombo(...)`; se não houver, lança
  `RegistroNaoEncontradoException("Nenhum empréstimo ativo do exemplar <tombo>")`. Depois de mudar,
  salve `emprestimo.exemplar().livro()`, o empréstimo e o usuário. Não salve um livro buscado à
  parte (no SQLite, seria outro objeto).
- Na auditoria, o alvo é o tombo e o `cpfUsuario` é o do empréstimo. A mensagem de CPF errado é
  `"Este empréstimo é de outro usuário"`.

- [ ] **Ciclo M73b — "Código de renovação só para o dono do empréstimo (M73)"**
  - `naoDeveEnviarCodigoQuandoRenovacaoNaoForPermitida`: em `18/11`, atrasado → `RenovacaoNaoPermitidaException`, canal sem mensagens
  - `naoDeveEnviarCodigoDeRenovacaoQuandoCpfForDeOutroUsuario`: Bruno → `EmprestimoNaoPermitidoException`
- [ ] **Ciclo M75 — "Renovação confirmada pelo código (M75)"**
  - `deveRenovarEAuditarQuandoEstiverEmDia`: em `15/11` → devolve `22/11/2026`; `RENOVACAO` com detalhes `"nova devolução prevista para 22/11/2026"`
  - `deveImpedirRenovacaoQuandoCpfNaoForDoUsuarioDoEmprestimo` → `EmprestimoNaoPermitidoException`
  - `deveImpedirRenovacaoQuandoUsuarioEstiverBloqueado`: Ana com saldo `3.00` → `UsuarioBloqueadoException`
  - `deveLancarExcecaoQuandoNaoHouverEmprestimoAtivoDoTombo` → `RegistroNaoEncontradoException`
- [ ] **Ciclo M76 — "Devolução calcula multa e suspensão (M76)"**
  - `deveDevolverSemMultaQuandoEstiverNoPrazo`: `17/11` → `(0, 0.00, 0.00, null)`; exemplar `DISPONIVEL`; `DEVOLUCAO` com detalhes `"sem atraso"`
  - `deveCobrarMultaESuspenderQuandoHouverAtraso`: `20/11` → `(3, 3.00, 0.00, 26/11/2026)`; Ana com saldo `3.00`; detalhes `"3 dias de atraso, multa de R$ 3,00, suspenso até 26/11/2026"`
  - `deveSomarSuspensaoQuandoUsuarioJaEstiverSuspenso`: Ana com `T-0002` (previsto `17/11`) e `M-0002` (emprestado em `13/11`, previsto `20/11`); devolve `T-0002` em `20/11` e `M-0002` em `22/11` → `suspensoAte` = `30/11/2026`
  - `deveAceitarDevolucaoQuandoUsuarioEstiverBanido`: sem código e sem exceção
- [ ] **Ciclo M73c — "Código de perda mostra o valor cobrado (M73)"**
  - `deveDescreverValorCobradoQuandoSolicitarCodigoDePerda`: em `20/11` → texto contém `"a perda de Dom Casmurro (tombo T-0002), com cobrança de R$ 83,00"`
- [ ] **Ciclo M77 — "Perda cobra multa e reposição (M77)"**
  - `deveCobrarMultaEReposicaoQuandoRegistrarPerda`: `20/11` → `(3, 3.00, 80.00, 26/11/2026)`; saldo `83.00`; exemplar `EXTRAVIADO`; empréstimo `PERDIDO`; `PERDA` com detalhes `"3 dias de atraso, multa de R$ 3,00, reposição de R$ 80,00, suspenso até 26/11/2026"`
  - `deveImpedirPerdaQuandoCpfNaoForDoUsuarioDoEmprestimo` → `EmprestimoNaoPermitidoException`
  - `deveImpedirPerdaQuandoCodigoForInvalido` → `CodigoVerificacaoInvalidoException`, nada muda

### Tarefa 21: `EmprestimoService`, consultas

**Arquivos:** alterar `EmprestimoService.java`; teste `servico/EmprestimoServiceTest.java`.

**Interfaces:** Produz `List<Emprestimo> listarAtivos(Cpf)`, `List<Emprestimo> listarHistorico(Cpf)` e
`List<Emprestimo> listarAtrasados(LocalDate)`. Nestes testes, monte os empréstimos direto no
`EmprestimoRepositoryMemoria`, com os objetos de domínio, porque pelo serviço os débitos de um
empréstimo bloqueariam o seguinte.

- [ ] **Ciclo M79 — "Empréstimos ativos do usuário (M79)"**
  - `deveListarSoAtivosDoUsuario`: Ana com 1 ativo e 1 devolvido → só o ativo
  - `deveLancarExcecaoQuandoCpfNaoEstiverCadastrado`: `935.411.347-80` → `RegistroNaoEncontradoException`
- [ ] **Ciclo M80 — "Histórico do mais recente ao mais antigo (M80)"**
  - `deveListarHistoricoDoMaisRecenteAoMaisAntigo`: empréstimos de `01/11` (devolvido), `05/11` (perdido) e `10/11` (ativo) → `10/11, 05/11, 01/11`
- [ ] **Ciclo M81 — "Atrasados do vencimento mais antigo (M81)"**
  - `deveListarAtrasadosDoVencimentoMaisAntigoAoMaisRecente`: Ana `T-0002` previsto `17/11`; Bruno `M-0002` emprestado `08/11`, previsto `15/11`; em `20/11` → `[Bruno, Ana]`
  - `naoDeveListarDevolvidosQuandoBuscarAtrasados`

## Passo 8 — Infraestrutura

Os testes de contrato ficam em `src/test/.../repositorio/` como classes abstratas
`…RepositoryContrato`, com `protected abstract XRepository criarRepositorio();`. Cada uma tem duas
filhas: `…RepositoryMemoriaTest`, que roda no `test`, e `…RepositorySqliteIT`, que roda no
`verify`. Nos ITs, o `@BeforeEach` cria `new ConexaoSqlite("jdbc:sqlite::memory:")` e chama
`criarTabelas()`; o `@AfterEach` fecha a conexão.

O ciclo aqui é o mesmo: o RED tem o contrato, as duas filhas e o IT falhando (não compila), e o
GREEN tem o código SQLite. As mensagens levam o requisito no fim, por exemplo `(RNF07)`.

### Tarefa 22: `ConexaoSqlite`, schema e repositórios de pessoas

**Arquivos:**
- Criar `src/main/resources/schema.sql` e `repositorio/sqlite/ConexaoSqlite.java`.
- Criar `repositorio/sqlite/UsuarioRepositorySqlite.java` e `AdministradorRepositorySqlite.java`.
- Alterar `Usuario.java` e `Administrador.java`.
- Testes: `repositorio/UsuarioRepositoryContrato.java`, `AdministradorRepositoryContrato.java`, as
  duas filhas de cada contrato em `memoria/` e em `sqlite/`.

**Interfaces:**
- Produz `ConexaoSqlite(String url) implements Transacao, AutoCloseable`, com `Connection conexao()`
  e `void criarTabelas()`. O `criarTabelas` lê o `schema.sql` do classpath. Na fase 1, `executar`
  só roda a operação; a transação de verdade entra na Tarefa 40.
- Produz `static Usuario restaurar(Cpf, String nome, String email, boolean banido, String motivoBanimento,
  LocalDate dataBanimento, LocalDate suspensoAte, BigDecimal saldoDevedor)`, e no mesmo formato
  `static Administrador restaurar(Cpf, String nome, String email, boolean ativo, LocalDate dataDesativacao)`.
  O `Administrador` ganha o getter `dataDesativacao()`.
  Os dois são documentados como "uso exclusivo dos repositórios".
- Produz `UsuarioRepositorySqlite(ConexaoSqlite)` e `AdministradorRepositorySqlite(ConexaoSqlite)`.
- `schema.sql`: as 6 tabelas da seção 10 dos requisitos, com `CREATE TABLE IF NOT EXISTS`, os dois
  triggers da auditoria e `PRAGMA foreign_keys = ON` aplicado ao abrir a conexão. O `id` do
  empréstimo é `TEXT PRIMARY KEY` (ver Tarefa 24). Datas em texto ISO e dinheiro em centavos.

- [ ] **Ciclo "Usuários e administradores gravados no SQLite (RNF07)"**
  - `UsuarioRepositoryContrato.deveDevolverUsuarioIgualQuandoSalvarEBuscar`: todos os campos iguais, inclusive banido com motivo e data, `suspensoAte` e saldo `83.00`
  - `deveAtualizarQuandoSalvarDeNovo`: muda o nome e salva → `listarTodos()` tem 1 usuário, com o nome novo
  - `deveRetornarVazioQuandoCpfNaoExistir`; `deveListarTodosQuandoHouverVarios`
  - `AdministradorRepositoryContrato`: ida e volta, `estaVazio()` antes e depois, atualização

### Tarefa 23: `LivroRepositorySqlite`

**Arquivos:** criar `repositorio/sqlite/LivroRepositorySqlite.java`; alterar `Livro.java`,
`Exemplar.java`; testes `repositorio/LivroRepositoryContrato.java` e as duas filhas.

**Interfaces:**
- Produz `static Livro restaurar(Isbn, String titulo, DadosBibliograficos dadosOuNulo,
  ProcessamentoTecnico processamentoOuNulo, SituacaoLivro, LocalDate dataLiberacao)`.
- Produz o método de instância `Exemplar restaurarExemplar(String codigoTombo, EstadoConservacao,
  boolean consultaLocal, SituacaoExemplar)` em `Livro`.
- Produz `LivroRepositorySqlite(ConexaoSqlite)`. O `salvar` grava o livro e substitui os
  exemplares dele.

- [ ] **Ciclo "Livros e exemplares gravados no SQLite (RNF07)"**
  - `deveGuardarLivroComExemplaresQuandoSalvar`: pendências, situação, data de liberação e cada exemplar (estado, consulta local, situação `EMPRESTADO`) iguais depois de buscar
  - `deveEncontrarExemplarPeloTombo`: `buscarExemplar("T-0002")` → exemplar cujo `livro().isbn()` é o do livro
  - `deveAtualizarExemplaresQuandoSalvarDeNovo`: exemplar novo e estado mudado aparecem
  - `deveRemoverLivroEExemplaresQuandoRemover`

### Tarefa 24: `EmprestimoRepositorySqlite`

**Arquivos:** criar `repositorio/sqlite/EmprestimoRepositorySqlite.java`; alterar `Emprestimo.java`,
`docs/requisitos.md` (seção 10, tabela `emprestimo`: `id` passa a ser texto com UUID); testes
`repositorio/EmprestimoRepositoryContrato.java` e as duas filhas.

**Interfaces:**
- Produz `UUID id()` em `Emprestimo`, gerado no construtor, e
  `static Emprestimo restaurar(UUID id, Usuario, Exemplar, PoliticaCirculacao, LocalDate dataEmprestimo,
  LocalDate dataPrevista, LocalDate dataDevolucao, SituacaoEmprestimo, int quantidadeRenovacoes,
  LocalDate dataPerda)`.
- Produz `EmprestimoRepositorySqlite(ConexaoSqlite, UsuarioRepository, LivroRepository, PoliticaCirculacao)`.
  Ele remonta o usuário e o exemplar pelos outros dois repositórios. Por causa das chaves
  estrangeiras, o IT grava a usuária e o livro antes do empréstimo.

- [ ] **Ciclo "Empréstimos gravados no SQLite (RNF07)"**
  - `deveDevolverEmprestimoIgualQuandoSalvarEBuscar`: datas, situação e renovações iguais
  - `deveEncontrarAtivoPeloTombo`: depois da devolução e de salvar de novo → vazio
  - `deveListarPorUsuario`; `deveInformarSeLivroJaTeveEmprestimo`: true para Dom Casmurro, false para Vidas Secas

### Tarefa 25: `AuditoriaRepositorySqlite` e triggers

**Arquivos:** criar `repositorio/sqlite/AuditoriaRepositorySqlite.java`; alterar
`RegistroAuditoria.java`; testes `repositorio/AuditoriaRepositoryContrato.java`, as duas filhas e os
testes de trigger no próprio `AuditoriaRepositorySqliteIT`.

**Interfaces:**
- Produz `static RegistroAuditoria restaurar(long sequencia, Instant dataHora, Cpf cpfOperador,
  TipoAcao acao, String alvo, Cpf cpfUsuario, String detalhes, String hashAnterior, String hash)`.
  Ele mantém o hash gravado, sem recalcular.
- Produz `AuditoriaRepositorySqlite(ConexaoSqlite)`. O IT grava Gabriel antes, por causa da chave estrangeira.

- [ ] **Ciclo "Trilha de auditoria gravada no SQLite (RNF07)"**
  - Contrato: `deveGuardarRegistroQuandoAdicionar` (todos os campos e o hash iguais), `deveRetornarUltimoPelaSequencia`, `deveListarEmOrdemDeSequencia`
- [ ] **Ciclo "Banco bloqueia alteração e remoção da trilha (RN30)"** (só no IT)
  - `deveBloquearUpdateQuandoTentarAlterarRegistro`: `UPDATE auditoria SET detalhes = 'x'` → `SQLException` com `"A trilha de auditoria não pode ser alterada"`
  - `deveBloquearDeleteQuandoTentarApagarRegistro` → `SQLException` com `"A trilha de auditoria não pode ser apagada"`

### Tarefa 26: Testes de integração dos serviços

**Arquivos:** testes `servico/AcessoServiceSqliteIT.java`, `UsuarioServiceSqliteIT.java`,
`AcervoServiceSqliteIT.java`, `EmprestimoServiceSqliteIT.java`.

Cada IT monta o serviço com os repositórios SQLite num banco em memória e roda o caminho
principal. Depois, lê de novo com **repositórios novos** (objetos novos) e confere o que foi gravado.
O Gabriel é gravado no banco antes, por causa da chave estrangeira da auditoria.

- [ ] `AcessoServiceSqliteIT.deveGravarAdministradorEAuditoriaQuandoCadastrarPrimeiro`
- [ ] `UsuarioServiceSqliteIT.deveGravarBanimentoQuandoBanirUsuario`
- [ ] `AcervoServiceSqliteIT.deveGravarLivroLiberadoQuandoCompletarCadastro`
- [ ] `EmprestimoServiceSqliteIT.deveGravarEmprestimoEDevolucaoComMultaQuandoHouverAtraso`: depois de emprestar, o exemplar relido está `EMPRESTADO`; depois de devolver em `20/11`, a usuária relida tem saldo `3.00` e `suspensoAte` `26/11/2026`
- [ ] Rode `./mvnw -q verify`. Se todos passarem de primeira, faça um commit sem prefixo de ciclo,
  `Testes de integração dos serviços com SQLite`. Se algum falhar (por exemplo, um `salvar`
  esquecido), faça o par `RED:`/`GREEN:` com a correção.

### Tarefa 27: Canal de e-mail (fora do TDD, RNF04)

**Arquivos:** criar `verificacao/ConfiguracaoEmail.java` e `verificacao/EmailCanalEnvio.java`.

**Interfaces:**
- Produz `record ConfiguracaoEmail(String host, int porta, String remetente, String senhaDeApp)`
  com `static ConfiguracaoEmail carregar()`. O `carregar` lê `config.properties` do classpath,
  com as chaves de `config.properties.exemplo`. Sem o arquivo, usa as variáveis de ambiente
  `BIBLIOTECA_EMAIL_REMETENTE` e `BIBLIOTECA_EMAIL_SENHA`, com host `smtp.gmail.com` e porta `587`.
- Produz `EmailCanalEnvio(ConfiguracaoEmail) implements CanalEnvio`, com SMTP autenticado e STARTTLS.
  Uma `MessagingException` vira `EnvioCodigoException("Não foi possível enviar o código. Tente de novo.")`.

- [ ] Implementar. A senha de app e o código não aparecem em log nem em mensagem de erro.
- [ ] Conferir à mão na Tarefa 28: o código chega no e-mail do Gabriel.
- [ ] Commit sem prefixo de ciclo: `Canal de e-mail pelo Gmail (fora do TDD, RNF04)`.

## Passo 9 — Interface Swing

As telas ficam fora do TDD e da cobertura. Valem para todas:
- Cada ação chama um serviço dentro de `Mensagens.executar(Component pai, Runnable acao)`, que
  mostra o `getMessage()` de `BibliotecaException`, `IllegalArgumentException` e
  `IllegalStateException` num `JOptionPane` de aviso (RNF03).
- A data do dia é `LocalDate.now(ZoneId.of("America/Cuiaba"))`. O CPF aparece sempre por
  `mascarado()`.
- Cada tarefa termina com a lista de conferência feita à mão e um commit sem prefixo de ciclo.

### Tarefa 28: `Aplicacao`, `TelaEntrada` e `TelaPrincipal`

**Arquivos:** criar `Aplicacao.java`, `ui/Mensagens.java`, `ui/TelaEntrada.java`, `ui/TelaPrincipal.java`;
alterar `pom.xml` (excluir `br/edu/ifmt/biblioteca/Aplicacao*` no JaCoCo, ao lado de `ui/**`).

- `Aplicacao.main` faz, em ordem:
  - `ConexaoSqlite("jdbc:sqlite:biblioteca.db")` e `criarTabelas()`;
  - os repositórios SQLite e o `EmailCanalEnvio(ConfiguracaoEmail.carregar())`;
  - `Clock.systemUTC()` e os serviços;
  - `SwingUtilities.invokeLater` abrindo a `TelaEntrada`.
- `TelaEntrada`:
  - com o banco vazio, mostra o cadastro do primeiro administrador;
  - depois, mostra o campo do CPF, o botão Enviar código (que mostra o e-mail mascarado), o campo
    do código e o botão Entrar, que abre a `TelaPrincipal`.
- `TelaPrincipal`: janela com as abas Livros, Usuários, Empréstimos e Auditoria (preenchidas nas
  tarefas 29–32) e um botão Sair, que chama `sair()` e volta para a `TelaEntrada`.
- [ ] Conferência: banco novo → cadastro do primeiro administrador → código chega no e-mail → entra → sai → entra de novo.
- [ ] Commit: `Telas de entrada e principal em Swing`.

### Tarefa 29: `PainelUsuarios`

- [ ] Tabela com CPF mascarado, nome, situação (ativo, `suspenso até dd/MM/yyyy` ou banido) e saldo devedor.
- [ ] Formulário com nome, CPF e e-mail para cadastrar e alterar.
- [ ] Botões Banir (pede o motivo), Registrar pagamento e Histórico de empréstimos (abre a lista do M80). Não tem botão de excluir.
- [ ] Conferência: cadastrar, alterar, tentar CPF repetido (mensagem aparece), banir, pagar.
- [ ] Commit: `Aba de usuários`.

### Tarefa 30: `PainelLivros`

- [ ] Lista com filtro por situação e coluna de pendências (`listarPendencias`).
- [ ] O cadastro abre em 4 etapas, cada uma com seu Salvar: identificação, dados bibliográficos, processamento técnico e exemplares.
- [ ] Liberar fica ativo só sem pendências.
- [ ] Botão Excluir.
- [ ] Conferência: cadastrar em dias diferentes (fechar e abrir o programa no meio), liberar, tentar excluir livro já emprestado.
- [ ] Commit: `Aba de livros`.

### Tarefa 31: `PainelEmprestimos`

- [ ] Campos CPF e tombo.
- [ ] Um botão para cada operação que pede código: Emprestar, Renovar e Registrar perda. Cada um
  chama `solicitarConfirmacao` e mostra o e-mail mascarado; depois o campo do código e Confirmar.
- [ ] Devolver, com o estado do exemplar, sem código.
- [ ] Tabela de ativos com os atrasados em destaque (M81).
- [ ] Conferência: empréstimo com o código real, devolução com atraso (mude a data à mão no teste), renovação recusada no terceiro pedido.
- [ ] Commit: `Aba de empréstimos`.

### Tarefa 32: `PainelAuditoria` e menu Administradores

- [ ] Filtros por administrador, CPF do usuário, ação e período.
- [ ] Tabela com data e hora no fuso `America/Cuiaba` (`dd/MM/yyyy HH:mm`), administrador, ação, alvo e detalhes.
- [ ] Menu Administradores → Cadastrar.
- [ ] Conferência: as ações das tarefas 28–31 aparecem; nenhum código de verificação aparece na trilha.
- [ ] Commit: `Aba de auditoria e cadastro de administradores`.

### Tarefa 33: Fechamento da fase 1

- [ ] `./mvnw verify` verde; anote o total de testes (Surefire + Failsafe) e a cobertura de
  instruções de `target/site/jacoco/index.html`.
- [ ] Atualize o status e a tabela "Resultados" do README e o "Estado atual" do `CONTEXTO.md`.
- [ ] Commit: `Fase 1 concluída: resultados no README`.

---

# Fase 2

Só começa com a Tarefa 33 feita. Mesmo procedimento de ciclo. A ordem segue a dependência entre
os itens.

### Tarefa 34: Desativação de administrador (M26, M48, partes de M41 e M49)

**Interfaces:** Produz `void desativar(LocalDate data)` em `Administrador` (o getter
`dataDesativacao()` já existe desde a Tarefa 22) e `void desativarAdministrador(Cpf, LocalDate)` em
`AcessoService`.

- [ ] **Ciclo M26:**
  - `deveDesativarEGuardarDataQuandoDesativar`
  - `deveLancarExcecaoQuandoAdministradorJaEstiverInativo` → `IllegalStateException`
- [ ] **Ciclo M41 (inativo):** `deveImpedirSessaoQuandoAdministradorEstiverInativo` → `IllegalStateException`
- [ ] **Ciclo M49 (inativo):** `deveNegarAcessoQuandoAdministradorEstiverInativo` → mesma mensagem do CPF desconhecido
- [ ] **Ciclo M48:**
  - `deveDesativarEAuditarQuandoOutroAdministradorPedir`: Gabriel desativa Carla → `ADMINISTRADOR_DESATIVADO`
  - `deveImpedirQuandoAdministradorTentarDesativarASiMesmo` → `OperacaoNaoAutorizadaException`
- [ ] Menu Administradores → Desativar; conferência à mão; commit `Desativação de administrador na tela`.

### Tarefa 35: Expiração da sessão (M42, M43, parte de M52)

**Interfaces:** Produz `boolean estaExpirada(Instant agora)` e `void registrarUso(Instant agora)`
em `Sessao`. O `operadorAtual()` passa a chamar `registrarUso(relogio.instant())`.

- [ ] **Ciclo M42:** último uso `10:00` → `estaExpirada(10:29)` false e `estaExpirada(10:30)` true
- [ ] **Ciclo M43:**
  - `deveAtualizarUltimoUsoQuandoRegistrarUso`
  - `deveLancarExcecaoQuandoSessaoEstiverExpirada` → `SessaoInvalidaException`
- [ ] **Ciclo M52 (expiração):** `deveEncerrarSessaoQuandoPassarem30MinutosSemUso`: `RelogioDeTeste.avancar(30 min)` → `SessaoInvalidaException`; a chamada seguinte também falha
- [ ] Na tela, `SessaoInvalidaException` volta para a `TelaEntrada`; commit `Tela volta para a entrada quando a sessão expira`.

### Tarefa 36: Eventos de segurança na trilha (M54 e registros em M49, M52 e M58)

**Interfaces:**
- Produz `RegistroAuditoria registrarEventoSeguranca(TipoAcao, Cpf cpfEnvolvido, String detalhes)`
  em `AuditoriaService`.
- O `VerificacaoService` passa a receber `AuditoriaService`: o construtor fica
  `(GeradorCodigo, CanalEnvio, AuditoriaService, Clock)`. Atualize os testes e a `Aplicacao`.

- [ ] **Ciclo M54:**
  - `deveRegistrarEventoSemOperadorQuandoAcaoForDeSeguranca`
  - `deveRecusarEventoDeSegurancaQuandoAcaoForComum` → `IllegalArgumentException`
- [ ] **Ciclo M58 (evento):** `deveRegistrarCodigoIncorretoQuandoErrarCodigo`: registro `CODIGO_INCORRETO` com a finalidade nos detalhes e sem o código digitado; o registro fica gravado mesmo com a exceção
- [ ] **Ciclo M49 (evento):** `deveRegistrarAcessoNegadoQuandoCpfNaoTiverAcesso`
- [ ] **Ciclo M52 (evento):** `deveRegistrarSessaoExpiradaQuandoSessaoVencer`

### Tarefa 37: Bloqueio por falhas e intervalo entre envios (partes de M57 e M58)

**Arquivos:** criar `excecao/VerificacaoBloqueadaException.java`.

- [ ] **Ciclo M57 (intervalo):** `deveImpedirReenvioQuandoNaoPassarem60Segundos`: novo pedido em 30 s → `VerificacaoBloqueadaException`; em 60 s → enviado
- [ ] **Ciclo M58 (bloqueio):** `deveBloquearVerificacaoQuandoHouverCincoFalhasSeguidas`:
  - 5 falhas seguidas, pedindo código novo a cada 60 s → na 5ª, `VerificacaoBloqueadaException` e registro `VERIFICACAO_BLOQUEADA`;
  - por 15 minutos, `enviarCodigo` e `validarCodigo` → `VerificacaoBloqueadaException`;
  - depois disso, liberado.
- [ ] `deveZerarFalhasQuandoAcertarCodigo`: 4 falhas, 1 acerto, 1 falha → sem bloqueio

### Tarefa 38: Reposição física do exemplar perdido (M16, M33, M78)

**Arquivos:**
- Criar `excecao/ReposicaoNaoPermitidaException.java`.
- Alterar `EmprestimoRepository` (`Optional<Emprestimo> buscarPerdidoPorTombo(String)`), as duas
  implementações e o contrato.

- [ ] **Ciclo M16:**
  - `deveVoltarComoNovoQuandoRegistrarReposicao`: `EXTRAVIADO` → `DISPONIVEL`, `NOVO`, mesmo tombo
  - `deveLancarExcecaoQuandoExemplarNaoEstiverExtraviado`
- [ ] **Ciclo M33:**
  - `deveMarcarComoRepostoQuandoEstiverNoPrazo`: perda `20/11`, reposição `20/12` (30º dia) → `REPOSTO`
  - `deveImpedirReposicaoQuandoPassarem30DiasDaPerda`: `21/12` → `ReposicaoNaoPermitidaException`
- [ ] **Ciclo M78:**
  - `deveAbaterReposicaoEAuditarQuandoRepor`: saldo `83.00` → `3.00`; `REPOSICAO`
  - `deveImpedirReposicaoQuandoValorJaTiverSidoPago`: saldo `3.00` → `ReposicaoNaoPermitidaException`
- [ ] Botão Registrar reposição na aba Empréstimos; conferência; commit `Reposição de exemplar na tela`.

### Tarefa 39: Integridade da trilha (M45, M56)

**Interfaces:** Produz `boolean hashConfere()` em `RegistroAuditoria` e
`OptionalLong verificarIntegridade()` em `AuditoriaService`. Para simular a adulteração, o
`AuditoriaRepositoryMemoria` ganha dois métodos que só existem no teste:
`substituir(RegistroAuditoria)` e `remover(long sequencia)`.

- [ ] **Ciclo M45:**
  - `deveConferirHashQuandoRegistroNaoMudou`
  - `deveDetectarAlteracaoQuandoCampoMudarDepoisDeGravado`: `restaurar` com detalhes mudados e o hash original → false
- [ ] **Ciclo M56:**
  - `deveRetornarVazioQuandoTrilhaEstiverIntegra`
  - `deveApontarRegistroQuandoTrilhaForAdulterada`: registro 2 trocado no repositório em memória → `2`
  - `deveApontarQuandoHashAnteriorNaoBater`: registro 2 removido → `3`
- [ ] Botão Verificar integridade na aba Auditoria; conferência; commit `Verificação de integridade na tela`.

### Tarefa 40: Transação no SQLite (RN33)

- [ ] **Ciclo "Alteração e auditoria gravadas juntas (RN33)"** em `src/test/.../repositorio/sqlite/ConexaoSqliteIT.java`:
  - `deveDesfazerTudoQuandoOperacaoFalharNoMeio`: dentro de `executar`, salva um usuário e lança `RuntimeException` → o usuário não está no banco
  - `deveUsarATransacaoDeForaQuandoChamadasForemAninhadas`: `executar` dentro de `executar` → um só commit no fim
  - Implementação: `setAutoCommit(false)`, `commit` no fim e `rollback` com a exceção relançada. Um contador de profundidade faz as chamadas internas só rodarem.

### Tarefa 41: Fechamento da fase 2

- [ ] `./mvnw verify` verde; atualize os Resultados do README, a tabela da seção 12 dos requisitos
  (fase 2 feita) e o `CONTEXTO.md`.
- [ ] Commit: `Fase 2 concluída`.
