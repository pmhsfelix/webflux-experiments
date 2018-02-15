package org.pedrofelix.webflux.experiments;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WebClientTests {

    private Logger log = LoggerFactory.getLogger(WebClientTests.class);

    @Test
    public void simple_GET() {
        WebClient client = WebClient.create();
        ClientResponse resp = client.get()
                .uri("http://httpbin.org/get")
                .exchange()
                .block();
        assertEquals(200, resp.statusCode().value());
    }

    @Test
    public void simple_GET_with_retrieve() throws InterruptedException {
        ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(config -> {
                    config.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder());
                }).build();


        WebClient client = WebClient.builder().exchangeStrategies(strategies).build();
        HttpBinResponse model = client.get()
                .uri("http://httpbin.org/get")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(HttpBinResponse.class)
                .block();

        assertEquals("http://httpbin.org/get", model.url);

        Thread.sleep(2000);

    }

    @Test
    public void mono_is_hot() throws InterruptedException {
        WebClient client = WebClient.builder()
        .filter((req, next) -> {
                log.info("filter");
                return next.exchange(req);
                })
        .build();
        Mono<ClientResponse> resp = client.get()
                .uri("http://httpbin.org/uuid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange();

        for(int i = 0 ; i<2 ; ++i) {
            resp.subscribe(res -> {
                log.info("status {}", res.statusCode().value());
                res.bodyToMono(HttpBinResponse.class).subscribe(m -> {
                    log.info("body {}", m.uuid);
                });

            });
        }

        Thread.sleep(5000);
    }


    public static class HttpBinResponse {
        public Map<String, String> headers;
        public String url;
        public String uuid;
    }
}
