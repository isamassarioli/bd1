import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class TrabalhoBD {
    
    private static final String URL = "jdbc:postgresql://localhost:5432/loc004";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgre";
    
    private Connection connection;
    private Scanner scanner;
    
    public TrabalhoBD() {
        this.scanner = new Scanner(System.in);
    }
    
    // ===================== CONNECTION METHODS =====================
    
    public boolean conectarAoBancoDados() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Conectado ao banco de dados com sucesso!");
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("✗ Driver PostgreSQL não encontrado: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.out.println("✗ Erro ao conectar ao banco de dados: " + e.getMessage());
            return false;
        }
    }
    
    public void desconectar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Desconectado do banco de dados.");
            }
        } catch (SQLException e) {
            System.out.println("✗ Erro ao desconectar: " + e.getMessage());
        }
    }
    
    // ===================== FILME OPERATIONS =====================
    
    public void incluirFilme() {
        try {
            System.out.println("\n--- Incluir Novo Filme ---");
            System.out.print("Nome do filme: ");
            String nomFilme = scanner.nextLine();
            
            System.out.print("Código da cor (ex: AZ, VM, etc): ");
            String codCor = scanner.nextLine().toUpperCase();
            
            System.out.println("Gêneros disponíveis:");
            listarGeneros();
            System.out.print("Código do gênero: ");
            int codGenero = scanner.nextInt();
            scanner.nextLine();
            
            if (!validarCor(codCor)) {
                System.out.println("✗ Cor inválida!");
                return;
            }
            
            if (!validarGenero(codGenero)) {
                System.out.println("✗ Gênero inválido!");
                return;
            }
            
            String sql = "INSERT INTO filme (nom_filme, cod_cor, cod_genero) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nomFilme);
            stmt.setString(2, codCor);
            stmt.setInt(3, codGenero);
            
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("✓ Filme incluído com sucesso! Código: " + rs.getInt(1));
                }
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao incluir filme: " + e.getMessage());
        }
    }
    
    public void alterarFilme() {
        try {
            System.out.println("\n--- Alterar Filme ---");
            System.out.print("Código do filme: ");
            int codFilme = scanner.nextInt();
            scanner.nextLine();
            
            if (!verificarFilmeExiste(codFilme)) {
                System.out.println("✗ Filme não encontrado!");
                return;
            }
            
            System.out.println("\nO que deseja alterar?");
            System.out.println("1. Nome do filme");
            System.out.println("2. Cor");
            System.out.println("3. Gênero");
            System.out.print("Escolha: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();
            
            String sql = "";
            PreparedStatement stmt = null;
            
            switch (opcao) {
                case 1:
                    System.out.print("Novo nome: ");
                    String novoNome = scanner.nextLine();
                    sql = "UPDATE filme SET nom_filme = ? WHERE cod_filme = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, novoNome);
                    stmt.setInt(2, codFilme);
                    break;
                    
                case 2:
                    System.out.print("Nova cor: ");
                    String novaCor = scanner.nextLine().toUpperCase();
                    if (!validarCor(novaCor)) {
                        System.out.println("✗ Cor inválida!");
                        return;
                    }
                    sql = "UPDATE filme SET cod_cor = ? WHERE cod_filme = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setString(1, novaCor);
                    stmt.setInt(2, codFilme);
                    break;
                    
                case 3:
                    listarGeneros();
                    System.out.print("Novo gênero: ");
                    int novoGenero = scanner.nextInt();
                    scanner.nextLine();
                    if (!validarGenero(novoGenero)) {
                        System.out.println("✗ Gênero inválido!");
                        return;
                    }
                    sql = "UPDATE filme SET cod_genero = ? WHERE cod_filme = ?";
                    stmt = connection.prepareStatement(sql);
                    stmt.setInt(1, novoGenero);
                    stmt.setInt(2, codFilme);
                    break;
                    
                default:
                    System.out.println("✗ Opção inválida!");
                    return;
            }
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("✓ Filme alterado com sucesso!");
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao alterar filme: " + e.getMessage());
        }
    }
    
    public void excluirFilme() {
        try {
            System.out.println("\n--- Excluir Filme ---");
            System.out.print("Código do filme: ");
            int codFilme = scanner.nextInt();
            scanner.nextLine();
            
            if (!verificarFilmeExiste(codFilme)) {
                System.out.println("✗ Filme não encontrado!");
                return;
            }
            
            // Verificar se existem fitas do filme
            if (verificarFitasExistemParaFilme(codFilme)) {
                System.out.println("✗ Não é possível excluir filme com fitas cadastradas!");
                return;
            }
            
            System.out.print("Tem certeza que deseja excluir? (S/N): ");
            String confirmacao = scanner.nextLine();
            
            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println("✓ Operação cancelada.");
                return;
            }
            
            String sql = "DELETE FROM filme WHERE cod_filme = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, codFilme);
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("✓ Filme excluído com sucesso!");
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao excluir filme: " + e.getMessage());
        }
    }
    
    public void listarFilmes() {
        try {
            String sql = "SELECT f.cod_filme, f.nom_filme, f.cod_cor, g.nom_genero " +
                        "FROM filme f " +
                        "JOIN genero g ON f.cod_genero = g.cod_genero " +
                        "ORDER BY f.cod_filme";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\n--- Lista de Filmes ---");
            System.out.println(String.format("%-5s | %-40s | %-4s | %-15s", 
                             "Cod", "Nome", "Cor", "Gênero"));
            System.out.println("-".repeat(75));
            
            boolean temDados = false;
            while (rs.next()) {
                temDados = true;
                System.out.println(String.format("%-5d | %-40s | %-4s | %-15s",
                    rs.getInt("cod_filme"),
                    rs.getString("nom_filme"),
                    rs.getString("cod_cor"),
                    rs.getString("nom_genero")));
            }
            
            if (!temDados) {
                System.out.println("Nenhum filme cadastrado.");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao listar filmes: " + e.getMessage());
        }
    }
    
    // ===================== FITA OPERATIONS =====================
    
    public void alterarSituacaoFita() {
        try {
            System.out.println("\n--- Alterar Situação da Fita ---");
            listarFitas();
            System.out.print("Código da fita: ");
            int codFita = scanner.nextInt();
            scanner.nextLine();
            
            if (!verificarFitaExiste(codFita)) {
                System.out.println("✗ Fita não encontrada!");
                return;
            }
            
            System.out.println("\nSituações disponíveis:");
            System.out.println("1. Disponível");
            System.out.println("2. Alugada");
            System.out.println("3. Danificada");
            System.out.print("Nova situação: ");
            int situacao = scanner.nextInt();
            scanner.nextLine();
            
            if (situacao < 1 || situacao > 3) {
                System.out.println("✗ Situação inválida!");
                return;
            }
            
            String sql = "UPDATE fita SET sit_fita = ? WHERE cod_fita = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, situacao);
            stmt.setInt(2, codFita);
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("✓ Situação da fita alterada com sucesso!");
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao alterar situação da fita: " + e.getMessage());
        }
    }
    
    public void incluirFita() {
        try {
            System.out.println("\n--- Incluir Nova Fita ---");
            listarFilmes();
            System.out.print("Código do filme: ");
            int codFilme = scanner.nextInt();
            scanner.nextLine();
            
            if (!verificarFilmeExiste(codFilme)) {
                System.out.println("✗ Filme não encontrado!");
                return;
            }
            
            System.out.print("Data de aquisição (yyyy-MM-dd): ");
            String dataAquisicao = scanner.nextLine();
            
            String sql = "INSERT INTO fita (cod_filme, sit_fita, dat_aquisicao) VALUES (?, 1, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, codFilme);
            stmt.setDate(2, java.sql.Date.valueOf(dataAquisicao));
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("✓ Fita incluída com sucesso! Código: " + rs.getInt(1));
                }
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao incluir fita: " + e.getMessage());
        }
    }
    
    public void listarFitas() {
        try {
            String sql = "SELECT f.cod_fita, f.cod_filme, fl.nom_filme, f.sit_fita, f.dat_aquisicao " +
                        "FROM fita f " +
                        "JOIN filme fl ON f.cod_filme = fl.cod_filme " +
                        "ORDER BY f.cod_fita";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\n--- Lista de Fitas ---");
            System.out.println(String.format("%-6s | %-6s | %-30s | %-15s | %-12s", 
                             "Cod", "Filme", "Nome Filme", "Situação", "Data Aquisição"));
            System.out.println("-".repeat(90));
            
            boolean temDados = false;
            while (rs.next()) {
                temDados = true;
                String situacao = rs.getInt("sit_fita") == 1 ? "Disponível" : 
                                rs.getInt("sit_fita") == 2 ? "Alugada" : "Danificada";
                System.out.println(String.format("%-6d | %-6d | %-30s | %-15s | %-12s",
                    rs.getInt("cod_fita"),
                    rs.getInt("cod_filme"),
                    rs.getString("nom_filme"),
                    situacao,
                    rs.getDate("dat_aquisicao")));
            }
            
            if (!temDados) {
                System.out.println("Nenhuma fita cadastrada.");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao listar fitas: " + e.getMessage());
        }
    }
    
    // ===================== LOCAÇÃO OPERATIONS =====================
    
    public void realizarLocacao() {
        try {
            System.out.println("\n--- Realizar Locação ---");
            
            // Verificar cliente
            listarClientes();
            System.out.print("Código do cliente: ");
            int codCliente = scanner.nextInt();
            scanner.nextLine();
            
            if (!verificarClienteExiste(codCliente)) {
                System.out.println("✗ Cliente não encontrado!");
                return;
            }
            
            // Escolher fita disponível
            System.out.println("\n--- Fitas Disponíveis ---");
            listarFitasDisponiveis();
            System.out.print("Código da fita: ");
            int codFita = scanner.nextInt();
            scanner.nextLine();
            
            if (!verificarFitaExiste(codFita)) {
                System.out.println("✗ Fita não encontrada!");
                return;
            }
            
            if (!verificarFitaDisponivel(codFita)) {
                System.out.println("✗ Fita não está disponível!");
                return;
            }
            
            // Definir valor da locação
            System.out.print("Valor da locação: R$ ");
            double valorLocacao = scanner.nextDouble();
            scanner.nextLine();
            
            // Definir dias de aluguel
            System.out.print("Quantidade de dias (padrão 3): ");
            String diasStr = scanner.nextLine();
            int dias = diasStr.isEmpty() ? 3 : Integer.parseInt(diasStr);
            
            // Calcular datas
            LocalDateTime dataLocacao = LocalDateTime.now();
            LocalDateTime dataPrevista = dataLocacao.plusDays(dias);
            
            String sql = "INSERT INTO locacao (cod_cliente, cod_fita, dat_locacao, " +
                        "dat_prevista_devolucao, val_locacao) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, codCliente);
            stmt.setInt(2, codFita);
            stmt.setTimestamp(3, Timestamp.valueOf(dataLocacao));
            stmt.setTimestamp(4, Timestamp.valueOf(dataPrevista));
            stmt.setDouble(5, valorLocacao);
            
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas > 0) {
                // Atualizar status da fita para "Alugada"
                String updateFita = "UPDATE fita SET sit_fita = 2 WHERE cod_fita = ?";
                PreparedStatement stmtFita = connection.prepareStatement(updateFita);
                stmtFita.setInt(1, codFita);
                stmtFita.executeUpdate();
                stmtFita.close();
                
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("✓ Locação realizada com sucesso!");
                    System.out.println("   Código da locação: " + rs.getInt(1));
                    System.out.println("   Data de devolução prevista: " + dataPrevista);
                }
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao realizar locação: " + e.getMessage());
        }
    }
    
    public void listarLocacoes() {
        try {
            String sql = "SELECT l.cod_locacao, l.cod_cliente, c.nom_cliente, l.cod_fita, " +
                        "fl.nom_filme, l.dat_locacao, l.dat_prevista_devolucao, " +
                        "l.dat_devolucao, l.val_locacao " +
                        "FROM locacao l " +
                        "JOIN cliente c ON l.cod_cliente = c.cod_cliente " +
                        "JOIN fita f ON l.cod_fita = f.cod_fita " +
                        "JOIN filme fl ON f.cod_filme = fl.cod_filme " +
                        "ORDER BY l.cod_locacao DESC";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\n--- Lista de Locações ---");
            System.out.println(String.format("%-6s | %-6s | %-20s | %-30s | %-20s | %-10s", 
                             "Loc", "Cli", "Cliente", "Filme", "Status", "Valor"));
            System.out.println("-".repeat(120));
            
            boolean temDados = false;
            while (rs.next()) {
                temDados = true;
                String status = rs.getTimestamp("dat_devolucao") == null ? "Ativa" : "Devolvida";
                System.out.println(String.format("%-6d | %-6d | %-20s | %-30s | %-20s | R$ %.2f",
                    rs.getInt("cod_locacao"),
                    rs.getInt("cod_cliente"),
                    rs.getString("nom_cliente"),
                    rs.getString("nom_filme"),
                    status,
                    rs.getDouble("val_locacao")));
            }
            
            if (!temDados) {
                System.out.println("Nenhuma locação cadastrada.");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao listar locações: " + e.getMessage());
        }
    }
    
    public void devolverFita() {
        try {
            System.out.println("\n--- Devolver Fita ---");
            listarLocacoes();
            System.out.print("Código da locação: ");
            int codLocacao = scanner.nextInt();
            scanner.nextLine();
            
            if (!verificarLocacaoExiste(codLocacao)) {
                System.out.println("✗ Locação não encontrada!");
                return;
            }
            
            // Obter informações da locação
            String sql = "SELECT cod_fita FROM locacao WHERE cod_locacao = ? AND dat_devolucao IS NULL";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, codLocacao);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("✗ Locação já foi devolvida ou não existe!");
                rs.close();
                stmt.close();
                return;
            }
            
            int codFita = rs.getInt("cod_fita");
            rs.close();
            stmt.close();
            
            // Atualizar devolução
            LocalDateTime dataDevolucao = LocalDateTime.now();
            String updateLocacao = "UPDATE locacao SET dat_devolucao = ? WHERE cod_locacao = ?";
            PreparedStatement stmtUpdate = connection.prepareStatement(updateLocacao);
            stmtUpdate.setTimestamp(1, Timestamp.valueOf(dataDevolucao));
            stmtUpdate.setInt(2, codLocacao);
            stmtUpdate.executeUpdate();
            stmtUpdate.close();
            
            // Atualizar status da fita para "Disponível"
            String updateFita = "UPDATE fita SET sit_fita = 1 WHERE cod_fita = ?";
            PreparedStatement stmtFita = connection.prepareStatement(updateFita);
            stmtFita.setInt(1, codFita);
            stmtFita.executeUpdate();
            stmtFita.close();
            
            System.out.println("✓ Fita devolvida com sucesso!");
            System.out.println("   Data de devolução: " + dataDevolucao);
            
        } catch (SQLException e) {
            System.out.println("✗ Erro ao devolver fita: " + e.getMessage());
        }
    }
    
    // ===================== HELPER METHODS =====================
    
    private void listarClientes() {
        try {
            String sql = "SELECT cod_cliente, nom_cliente FROM cliente ORDER BY cod_cliente";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\n--- Clientes ---");
            while (rs.next()) {
                System.out.println(rs.getInt("cod_cliente") + " - " + rs.getString("nom_cliente"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao listar clientes: " + e.getMessage());
        }
    }
    
    private void listarGeneros() {
        try {
            String sql = "SELECT cod_genero, nom_genero FROM genero ORDER BY cod_genero";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                System.out.println(rs.getInt("cod_genero") + " - " + rs.getString("nom_genero"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao listar gêneros: " + e.getMessage());
        }
    }
    
    private void listarFitasDisponiveis() {
        try {
            String sql = "SELECT f.cod_fita, f.cod_filme, fl.nom_filme, f.dat_aquisicao " +
                        "FROM fita f " +
                        "JOIN filme fl ON f.cod_filme = fl.cod_filme " +
                        "WHERE f.sit_fita = 1 " +
                        "ORDER BY f.cod_fita";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println(String.format("%-6s | %-6s | %-40s", 
                             "Cod", "Filme", "Nome Filme"));
            System.out.println("-".repeat(60));
            
            boolean temDados = false;
            while (rs.next()) {
                temDados = true;
                System.out.println(String.format("%-6d | %-6d | %-40s",
                    rs.getInt("cod_fita"),
                    rs.getInt("cod_filme"),
                    rs.getString("nom_filme")));
            }
            
            if (!temDados) {
                System.out.println("Nenhuma fita disponível.");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("✗ Erro ao listar fitas disponíveis: " + e.getMessage());
        }
    }
    
    private boolean verificarFilmeExiste(int codFilme) throws SQLException {
        String sql = "SELECT COUNT(*) FROM filme WHERE cod_filme = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, codFilme);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean existe = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return existe;
    }
    
    private boolean verificarFitaExiste(int codFita) throws SQLException {
        String sql = "SELECT COUNT(*) FROM fita WHERE cod_fita = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, codFita);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean existe = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return existe;
    }
    
    private boolean verificarFitaDisponivel(int codFita) throws SQLException {
        String sql = "SELECT sit_fita FROM fita WHERE cod_fita = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, codFita);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean disponivel = rs.getInt(1) == 1;
        rs.close();
        stmt.close();
        return disponivel;
    }
    
    private boolean verificarFitasExistemParaFilme(int codFilme) throws SQLException {
        String sql = "SELECT COUNT(*) FROM fita WHERE cod_filme = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, codFilme);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean existe = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return existe;
    }
    
    private boolean verificarClienteExiste(int codCliente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cliente WHERE cod_cliente = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, codCliente);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean existe = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return existe;
    }
    
    private boolean verificarLocacaoExiste(int codLocacao) throws SQLException {
        String sql = "SELECT COUNT(*) FROM locacao WHERE cod_locacao = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, codLocacao);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean existe = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return existe;
    }
    
    private boolean validarCor(String codCor) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cor WHERE cod_cor = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, codCor);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean valida = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return valida;
    }
    
    private boolean validarGenero(int codGenero) throws SQLException {
        String sql = "SELECT COUNT(*) FROM genero WHERE cod_genero = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, codGenero);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        boolean valida = rs.getInt(1) > 0;
        rs.close();
        stmt.close();
        return valida;
    }
    
    // ===================== MENU SYSTEM =====================
    
    public void exibirMenu() {
        boolean continuar = true;
        
        while (continuar) {
            System.out.println("\n===============================================");
            System.out.println("         SISTEMA DE LOCADORA - MENU PRINCIPAL");
            System.out.println("===============================================");
            System.out.println("--- FILMES ---");
            System.out.println("1. Incluir novo filme");
            System.out.println("2. Alterar filme");
            System.out.println("3. Excluir filme");
            System.out.println("4. Listar filmes");
            System.out.println("--- FITAS ---");
            System.out.println("5. Incluir nova fita");
            System.out.println("6. Alterar situação da fita");
            System.out.println("7. Listar fitas");
            System.out.println("--- LOCAÇÕES ---");
            System.out.println("8. Realizar locação");
            System.out.println("9. Devolver fita");
            System.out.println("10. Listar locações");
            System.out.println("0. Sair");
            System.out.println("===============================================");
            System.out.print("Escolha uma opção: ");
            
            int opcao = scanner.nextInt();
            scanner.nextLine();
            
            switch (opcao) {
                case 1:
                    incluirFilme();
                    break;
                case 2:
                    alterarFilme();
                    break;
                case 3:
                    excluirFilme();
                    break;
                case 4:
                    listarFilmes();
                    break;
                case 5:
                    incluirFita();
                    break;
                case 6:
                    alterarSituacaoFita();
                    break;
                case 7:
                    listarFitas();
                    break;
                case 8:
                    realizarLocacao();
                    break;
                case 9:
                    devolverFita();
                    break;
                case 10:
                    listarLocacoes();
                    break;
                case 0:
                    continuar = false;
                    System.out.println("\n✓ Encerrando sistema...");
                    break;
                default:
                    System.out.println("✗ Opção inválida!");
            }
        }
    }
    
    // ===================== MAIN METHOD =====================
    
    public static void main(String[] args) {
        TrabalhoBD sistema = new TrabalhoBD();
        
        if (sistema.conectarAoBancoDados()) {
            sistema.exibirMenu();
        } else {
            System.out.println("✗ Não foi possível conectar ao banco de dados.");
        }
        
        sistema.desconectar();
    }
}
