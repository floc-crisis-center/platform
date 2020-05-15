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

import cloud.floc.cc.db.DB;
import cloud.floc.cc.service.MenusService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;

public class MenusRouter
  extends ApiRouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MenusRouter.class);

  private final MenusService mService;

  public MenusRouter(Vertx pVertx, JWTAuth pJwtAuth, DB pDB)
    throws Exception {
    super(pVertx, LOGGER, pJwtAuth, null);

    mService = new MenusService(pDB);
  }

  @Override
  protected String basePath() {
    return "/menus";
  }

  @Override
  protected void configureRoutes(String pCollectionPath, Vertx pVertx) {
    configureGetBotQnARoute();
  }

  private void configureGetBotQnARoute() {
    mRouter.route(HttpMethod.GET, basePath() + ID_PATH).handler(routingContext -> {
      String botId = routingContext.pathParam(ID_PARAM);

      try {
        sendDocument(mService.getMenu(botId), routingContext.response(), 200);

      } catch (Exception ex) {
        sendError(ex, routingContext.response());
      }
    });
  }
}
