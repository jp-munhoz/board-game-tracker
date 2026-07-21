package br.com.bg.ludopedia.client;

import br.com.bg.ludopedia.client.dto.LudopediaGameDetails;
import br.com.bg.ludopedia.client.dto.LudopediaGameListResponse;
import br.com.bg.ludopedia.exception.LudopediaGameNotFoundException;
import br.com.bg.ludopedia.exception.LudopediaIntegrationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class LudopediaClient {

    private final RestClient restClient;

    public LudopediaClient(RestClient ludopediaRestClient) {
        this.restClient = ludopediaRestClient;
    }

    public LudopediaGameListResponse search(String name) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/jogos")
                            .queryParam("search", name)
                            .build())
                    .retrieve()
                    .body(LudopediaGameListResponse.class);
        } catch (RestClientException e) {
            throw new LudopediaIntegrationException("Falha ao comunicar com a Ludopedia: " + describe(e), e);
        }
    }

    public LudopediaGameDetails getGame(long id) {
        try {
            return restClient.get()
                    .uri("/jogos/{id}", id)
                    .retrieve()
                    .body(LudopediaGameDetails.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new LudopediaGameNotFoundException("Jogo com id " + id + " nao encontrado na Ludopedia");
        } catch (RestClientException e) {
            throw new LudopediaIntegrationException("Falha ao comunicar com a Ludopedia: " + describe(e), e);
        }
    }

    private static String describe(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + " - " + root.getMessage();
    }
}
