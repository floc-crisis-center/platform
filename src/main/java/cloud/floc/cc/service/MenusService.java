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

package cloud.floc.cc.service;

import cloud.floc.cc.common.Collection;
import cloud.floc.cc.common.Document;
import cloud.floc.cc.common.DocumentException;
import cloud.floc.cc.common.DocumentExistsException;
import cloud.floc.cc.common.DocumentNotFoundException;
import cloud.floc.cc.db.DB;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class MenusService {

  public static final String MENUS_COLLECTION_ID = "menus";

  private final DB mDB;

  public MenusService(DB pDB)
    throws DocumentExistsException, DocumentException {
    this.mDB = Objects.requireNonNull(pDB, "DB should be provided");

    if (!mDB.hasCollection(MENUS_COLLECTION_ID)) {
      mDB.createCollection(Collection
        .newBuilder()
        .withId(MENUS_COLLECTION_ID)
        .build());
    }
  }

  public void upsertMenu(String pBotId, JsonObject pPayload)
    throws DocumentExistsException, DocumentNotFoundException, DocumentException {
    if (mDB.hasDocument(MENUS_COLLECTION_ID, pBotId)) {
      mDB.updateDocument(Document
        .newBuilder()
        .underCollection(MENUS_COLLECTION_ID)
        .withId(pBotId)
        .withPayload(pPayload)
        .build());
    } else {
      mDB.createDocument(Document
        .newBuilder()
        .underCollection(MENUS_COLLECTION_ID)
        .withId(pBotId)
        .withPayload(pPayload)
        .build());
    }
  }

  public void updateMenu(String pBotId, JsonObject pPayload)
    throws DocumentNotFoundException, DocumentException {
    mDB.updateDocument(Document
      .newBuilder()
      .underCollection(MENUS_COLLECTION_ID)
      .withId(pBotId)
      .withPayload(pPayload)
      .build());

    var botDoc = mDB.getDocument(BotsService.BOTS_COLLECTION_ID, pBotId);
    JsonObject botPayload = new JsonObject(botDoc.payload().getMap());
    botPayload.put("generated", true);

    mDB.updateDocument(Document
      .newBuilder()
      .underCollection(BotsService.BOTS_COLLECTION_ID)
      .withId(pBotId)
      .withPayload(botPayload)
      .build());
  }

  public Document getMenu(String pBotId)
    throws DocumentNotFoundException, DocumentException {
    return mDB.getDocument(MENUS_COLLECTION_ID, pBotId);
  }
}
