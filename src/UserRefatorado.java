package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Classe de autenticação de usuários — versão revisada e corrigida.
 *
 * Correções aplicadas em relação ao código original:
 * - SQL Injection eliminado com uso de PreparedStatement
 * - NullPointerException tratado com verificação de conexão
 * - Recursos fechados com try-with-resources
 * - Exceções logadas adequadamente
 * - Driver corrigido para com.mysql.cj.jdbc.Driver
 * - Credenciais externalizadas como constantes (idealmente via env vars)
 * - Variável result tornada local (thread-safe)
 */
public class UserRefatorado {

    // MELHORIA: credenciais em constantes separadas do código de lógica
    // Em produção, usar variáveis de ambiente ou arquivo de configuração externo
    private static final String DB_URL      = "jdbc:mysql://127.0.0.1:3306/test";
    private static final String DB_USER     = "lopes";
    private static final String DB_PASSWORD = "123";
    private static final String DB_DRIVER   = "com.mysql.cj.jdbc.Driver";

    // MELHORIA: nome do usuário autenticado (opcional, sem efeito no resultado)
    public String nomeUsuario = "";

    /**
     * Estabelece conexão com o banco de dados MySQL.
     *
     * @return Connection válido ou null em caso de falha (com log do erro)
     */
    public Connection conectarBD() {
        Connection conn = null;
        try {
            // MELHORIA: nome correto do driver MySQL moderno
            Class.forName(DB_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            // MELHORIA: exceção logada para diagnóstico
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Verifica se o usuário com o login e senha informados existe no banco.
     *
     * Utiliza PreparedStatement para prevenir SQL Injection.
     * Recursos são fechados automaticamente com try-with-resources.
     *
     * @param login login do usuário
     * @param senha senha do usuário (idealmente já em hash antes de chegar aqui)
     * @return true se autenticado com sucesso, false caso contrário
     */
    public boolean verificarUsuario(String login, String senha) {

        // MELHORIA: result como variável local — thread-safe
        boolean result = false;

        // MELHORIA: verificação de null antes de usar a conexão
        Connection conn = conectarBD();
        if (conn == null) {
            System.err.println("Autenticação impossível: sem conexão com o banco.");
            return false;
        }

        // MELHORIA: PreparedStatement com parâmetros — elimina SQL Injection
        String sql = "SELECT nome FROM usuarios WHERE login = ? AND senha = ?";

        // MELHORIA: try-with-resources fecha conn, st e rs automaticamente
        try (
            Connection c = conn;
            PreparedStatement st = c.prepareStatement(sql)
        ) {
            // MELHORIA: parâmetros vinculados com segurança
            st.setString(1, login);
            st.setString(2, senha);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    result = true;
                    nomeUsuario = rs.getString("nome");
                }
            }

        } catch (Exception e) {
            // MELHORIA: exceção logada com stack trace para diagnóstico
            System.err.println("Erro durante autenticação: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}
