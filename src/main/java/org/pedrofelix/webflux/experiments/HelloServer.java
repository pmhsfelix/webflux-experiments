package org.pedrofelix.webflux.experiments;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.ipc.netty.http.server.HttpServer;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

public class HelloServer {

    public static void main(String[] args) throws IOException {
        RouterFunction<ServerResponse> route = routingFunction();
        HttpHandler httpHandler = toHttpHandler(route);

        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create("localhost", 8081);
        server.newHandler(adapter).block();
        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

    public static RouterFunction<ServerResponse> routingFunction() {

        return nest(path("/hello"),
                nest(accept(APPLICATION_JSON),
                        route(GET("/{id}"), req -> ServerResponse.ok().syncBody(req.pathVariable("id")))))
                .filter((req, next) -> {
                    System.out.println("here");
                    return next.handle(req);
                });
    }
}
