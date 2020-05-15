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

package cloud.floc.cc.routes;

import cloud.floc.cc.bot.MainMenuForm;
import cloud.floc.cc.bot.MainMenuOptionsForm;
import cloud.floc.cc.db.DB;
import cloud.floc.cc.rasa.ActionsRunner;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;

public class ActionsRouter
  extends ApiRouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActionsRouter.class);

  private final ActionsRunner mActionsRunner;

  public ActionsRouter(Vertx pVertx, JWTAuth pJwtAuth, DB pDB)
    throws Exception {
    super(pVertx, LOGGER, pJwtAuth, null);

    mActionsRunner = new ActionsRunner(new MainMenuForm(pDB), new MainMenuOptionsForm(pDB));
  }

  @Override
  protected String basePath() {
    return "/actions";
  }

  @Override
  protected void configureRoutes(String pCollectionPath, Vertx pVertx) {
    configureActionRoute();
  }

  private void configureActionRoute() {
    mRouter.route(HttpMethod.POST, basePath()).handler(routingContext -> {
      try {
        JsonObject payload = routingContext.getBodyAsJson();
        JsonObject result = mActionsRunner.run(payload);

        routingContext.response().setStatusCode(200);
        routingContext.response().putHeader(CONTENT_TYPE, CONTENT_JSON);
        routingContext.response().setChunked(true);
        routingContext.response().write(result.toBuffer()).end();

      } catch (Exception ex) {
        sendError(ex, routingContext.response());
      }
    });
  }
}
