/*
 * Copyright (c) 2020, Floc Technologies LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.floc.cc;

import cloud.floc.cc.common.Constants;
import cloud.floc.cc.db.DB;
import cloud.floc.cc.routes.ActionsRouter;
import cloud.floc.cc.routes.BotsRouter;
import cloud.floc.cc.routes.MenusRouter;
import cloud.floc.cc.routes.ResponsesRouter;
import cloud.floc.cc.routes.RouterUtils;
import cloud.floc.cc.routes.StatsRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class CrisisCenterServer
  extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(CrisisCenterServer.class);

  @Override
  public void start(Future<Void> pStartFuture) {
    try {
      JWTAuth jwtAuth = null; //new FirebaseJwtAuth(config());
      Router mainRouter = Router.router(vertx);
      DB db = DB.newDB(config());

      RouterUtils.configureBody(mainRouter, config());
      RouterUtils.configureCORS(mainRouter, config());

      mainRouter.mountSubRouter("/api/v1", new ActionsRouter(vertx, jwtAuth, db).getRouter());
      mainRouter.mountSubRouter("/api/v1", new BotsRouter(vertx, jwtAuth, db, config()).getRouter());
      mainRouter.mountSubRouter("/api/v1", new ResponsesRouter(vertx, jwtAuth, db).getRouter());
      mainRouter.mountSubRouter("/api/v1", new StatsRouter(vertx, jwtAuth, db, config()).getRouter());
      mainRouter.mountSubRouter("/api/v1", new MenusRouter(vertx, jwtAuth, db).getRouter());

      mainRouter.route("/starter/zip/*")
        .handler(StaticHandler
          .create()
          .setCachingEnabled(false)
          .setWebRoot(config().getString(Constants.ZIPS_ROOT_KEY, Constants.DEFAULT_ZIPS_ROOT_VALUE)));

      serve(pStartFuture, mainRouter);

    } catch (Exception ex) {
      LOGGER.error("Unable to start crisis center server", ex);
      pStartFuture.fail(ex);
    }
  }

  private void serve(Future<Void> pStartFuture, Router pRouter) {
    HttpServer httpServer = vertx.createHttpServer()
      .requestHandler(pRouter::accept);

    httpServer.listen(8080, lh -> {
      if (lh.succeeded()) {
        LOGGER.info(String.format("Crisis center server now serving requests on %d", lh.result().actualPort()));

        pStartFuture.complete();

      } else {
        pStartFuture.fail(lh.cause());
      }
    });
  }
}
