
package login;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
 
/**
 * CÓDIGO ORIGINAL — mantido para fins de análise de Teste de Caixa Branca.
 *
 * PROBLEMAS IDENTIFICADOS:
 * 1. SQL Injection: query montada por concatenação de strings
 * 2. Credenciais expostas na URL de conexão em texto puro
 * 3. Driver com nome incorreto: "com.mysql.Driver.Manager" não existe
 * 4. Catch vazio: exceções silenciadas sem log nem relançamento
 * 5. NullPointerException: conn pode ser null se conectarBD() falhar
 * 6. Resource leak: Connection, Statement e ResultSet nunca fechados
 * 7. Variável "result" como atributo de instância (não thread-safe)
 * 8. Sem documentação Javadoc
 */
public class User {
 
    /**
     * PROBLEMA: catch vazio — qualquer falha de conexão é silenciada.
     * PROBLEMA: credenciais (user=lopes&password=123) expostas no código.
     * PROBLEMA: nome do driver "com.mysql.Driver.Manager" está incorreto.
     * PROBLEMA: Connection nunca é fechado após uso.
     */
    public Connection conectarBD() {
        Connection conn = null;
        try {
            // PROBLEMA: nome do driver incorreto
            Class.forName("com.mysql.Driver.Manager").newInstance();
 
            // PROBLEMA: credenciais em texto puro no código-fonte
            String url = "jdbc:mysql://127.0.0.1/test?user=lopes&password=123";
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {
            // PROBLEMA: catch vazio — falha ignorada silenciosamente
        }
        return conn; // pode retornar null sem aviso
    }
 
    // PROBLEMA: variáveis de instância mutáveis — não thread-safe
    public String nome = "";
    public boolean result = false;
 
    /**
     * PROBLEMA: SQL Injection — login e senha concatenados diretamente na query.
     * Exemplo de ataque: login = "' OR '1'='1" autentica qualquer usuário.
     *
     * PROBLEMA: conn pode ser null (se conectarBD falhou), causando NullPointerException
     * na linha conn.createStatement() — exceção capturada pelo catch vazio abaixo.
     *
     * PROBLEMA: Statement e ResultSet nunca fechados (resource leak).
     */
    public boolean verificarUsuario(String login, String senha) {
        String sql = "";
        Connection conn = conectarBD();
 
        // INSTRUÇÃO SQL — VULNERÁVEL A SQL INJECTION
        sql += "select nome from usuarios ";
        sql += "where login = " + "'" + login + "'";   // VULNERÁVEL
        sql += " and senha = " + "'" + senha + "';";   // VULNERÁVEL
 
        try {
            Statement st = conn.createStatement();     // RISCO: NullPointerException se conn=null
            ResultSet rs = st.executeQuery(sql);       // RISCO: SQL Injection
            if (rs.next()) {
                result = true;
                nome = rs.getString("nome");
                // PROBLEMA: rs, st e conn não são fechados após uso
            }
        } catch (Exception e) {
            // PROBLEMA: catch vazio — exceção ignorada, impossível depurar
        }
        return result;
    }
} // fim da class
 
