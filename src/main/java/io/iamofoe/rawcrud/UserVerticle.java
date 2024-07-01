package io.iamofoe.rawcrud;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserVerticle extends AbstractVerticle {

  private final Map<String, Item> items = new HashMap<>();

  @Override
  public void start(Promise<Void> startPromise) {
    Router mainRouter = Router.router(vertx);
    Router apiRouter = Router.router(vertx);

    mainRouter.route().handler(BodyHandler.create());
    mainRouter.get("/").handler(this::handleGetItems);
    mainRouter.post("/").handler(this::handleCreateItem);
    mainRouter.get("/:id").handler(this::handleGetItem);
    mainRouter.put("/:id").handler(this::handleUpdateItem);
    mainRouter.delete("/:id").handler(this::handleDeleteItem);

    mainRouter.route("/api/v1/items/*")
        .subRouter(apiRouter);

    vertx.createHttpServer().requestHandler(mainRouter)
      .listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void handleGetItems(RoutingContext routingContext) {
    routingContext.response()
      .putHeader("content-type", "application/json")
      .end(Json.encodePrettily(items.values()));
  }

  private void handleCreateItem(RoutingContext routingContext) {
    JsonObject requestBody = routingContext.body().asJsonObject();
    String name = requestBody.getString("name");
    String description = requestBody.getString("description");

    Item newItem = new Item(UUID.randomUUID().toString(), name, description);
    items.put(newItem.getId(), newItem);

    routingContext.response()
      .setStatusCode(201)
      .putHeader("content-type", "application/json")
      .end(Json.encodePrettily(newItem));
  }

  private void handleGetItem(RoutingContext routingContext) {
    String id = routingContext.pathParam("id");
    Item item = items.get(id);

    if (item == null) {
      routingContext.response()
        .setStatusCode(404)
        .end();
    } else {
      routingContext.response()
        .putHeader("content-type", "application/json")
        .end(Json.encodePrettily(item));
    }
  }

  private void handleUpdateItem(RoutingContext routingContext) {
    String id = routingContext.pathParam("id");
    Item existingItem = items.get(id);

    if (existingItem == null) {
      routingContext.response()
        .setStatusCode(404)
        .end();
    } else {
      JsonObject requestBody = routingContext.body().asJsonObject();
      String name = requestBody.getString("name");
      String description = requestBody.getString("description");

      Item updatedItem = new Item(id, name, description);
      items.put(id, updatedItem);

      routingContext.response()
        .putHeader("content-type", "application/json")
        .end(Json.encodePrettily(updatedItem));
    }
  }

  private void handleDeleteItem(RoutingContext routingContext) {
    String id = routingContext.pathParam("id");
    Item deletedItem = items.remove(id);

    if (deletedItem == null) {
      routingContext.response()
        .setStatusCode(404)
        .end();
      return;
    }

    routingContext.response()
      .setStatusCode(204)
      .end();
  }

}
