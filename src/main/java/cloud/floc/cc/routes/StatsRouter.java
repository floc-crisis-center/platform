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
import cloud.floc.cc.service.BotsService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;

public class StatsRouter
  extends ApiRouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatsRouter.class);

  private final BotsService mService;

  public StatsRouter(Vertx pVertx, JWTAuth pJwtAuth, DB pDB, JsonObject pConfig)
    throws Exception {
    super(pVertx, LOGGER, pJwtAuth, null);

    mService = new BotsService(pDB, pVertx.fileSystem(), pConfig);
  }

  @Override
  protected String basePath() {
    return "/stats";
  }

  @Override
  protected void configureRoutes(String pCollectionPath, Vertx pVertx) {
    configureGetStatsRoute();
  }

  private void configureGetStatsRoute() {
    mRouter.route(HttpMethod.GET, basePath()).handler(routingContext -> {
      try {

        sendDocument(mService.getStats(), routingContext.response(), 200);

      } catch (Exception ex) {
        sendError(ex, routingContext.response());
      }
    });
  }
}
