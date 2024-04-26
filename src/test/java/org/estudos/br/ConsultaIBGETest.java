package org.estudos.br;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConsultaIBGETest {
    @Mock
    private HttpURLConnection connectionMock;

    private static final String ESTADOS_API_URL = "https://servicodados.ibge.gov.br/api/v1/localidades/estados/";
    private static final String DISTRITOS_API_URL = "https://servicodados.ibge.gov.br/api/v1/localidades/distritos/";
    private static final String JSON_RESPONSE = "{\"id\":33,\"sigla\":\"RJ\",\"nome\":\"Rio de Janeiro\",\"regiao\":{\"id\":3,\"sigla\":\"SE\",\"nome\":\"Sudeste\"}}";

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        InputStream inputStream = new ByteArrayInputStream(JSON_RESPONSE.getBytes());
        when(connectionMock.getInputStream()).thenReturn(inputStream);
    }

    @RepeatedTest(5)
    @DisplayName("Teste para consulta única de um estado")
    public void testConsultarEstado() throws IOException {
        // Arrange
        String estado = "SP";

        // Assert
        assertResponseNotEmptyAndStatusCodeOk(ESTADOS_API_URL + estado);
    }

    @ParameterizedTest
    @CsvSource({"RO", "AC", "AM", "RR", "PA", "AP", "TO", "MA", "PI", "CE", "RN", "PB", "PE", "AL", "SE", "BA", "MG", "ES"})
    @DisplayName("Teste para consulta de estados com CSV")
    public void testConsultarEstados(String sigla) throws IOException {
        // Act
        String resposta = ConsultaIBGE.consultarEstado(sigla);

        // Assert
        assertFalse(resposta.isEmpty(), "A resposta não deve estar vazia");
        assertResponseNotEmptyAndStatusCodeOk(ESTADOS_API_URL + sigla);
    }
    
    
    // Teste para verificar a consulta de distritos
    @ParameterizedTest
    @ValueSource(ints = {520005005, 310010405, 520010005})  // Identificadores de distritos
    @DisplayName("Teste de consulta de distrito")
    public void testConsultarDistrito(int identificador) throws IOException {
        // Consulta informações do distrito com o identificador fornecido
        String resposta = ConsultaIBGE.consultarDistrito(identificador);
        // Verifica se a resposta não está vazia
        assert !resposta.isEmpty();

        // Realiza uma conexão HTTP para verificar o status code da resposta
        int statusCode = obterStatusCode(identificador);
        // Verifica se o status code é 200, o que indica sucesso
        assertEquals(200, statusCode, "O status code da resposta da API deve ser 200 (OK)");
    }

    // Método auxiliar para obter o status code de uma URL
    private int obterStatusCode(int id) throws IOException {
        URL url = new URL(DISTRITOS_API_URL + id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        return connection.getResponseCode();
    }


    @Test
    @DisplayName("Consulta usando o Estado com Mock")
    public void testConsultarEstadoComMock() throws IOException {
        // Arrange
        String estado = "RJ";

        // Act
        String resposta = ConsultaIBGE.consultarEstado(estado);

        // Assert
        assertEquals(JSON_RESPONSE, resposta, "O JSON retornado deve corresponder ao esperado");
    }

    private void assertResponseNotEmptyAndStatusCodeOk(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        int statusCode = connection.getResponseCode();
        assertEquals(200, statusCode, "O status code da resposta da API deve ser 200 (OK)");
    }
}