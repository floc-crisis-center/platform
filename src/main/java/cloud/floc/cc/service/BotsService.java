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
import cloud.floc.cc.common.Constants;
import cloud.floc.cc.common.Document;
import cloud.floc.cc.common.DocumentException;
import cloud.floc.cc.common.DocumentExistsException;
import cloud.floc.cc.common.DocumentNotFoundException;
import cloud.floc.cc.common.Utils;
import cloud.floc.cc.db.DB;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class BotsService {

  public static final String BOTS_COLLECTION_ID = "bots";

  private static final String WELCOME_MESSAGE = "welcome_message";
  private static final String UTTER_WELCOME_MESSAGE = "utter_welcome_message";
  private static final String MAIN_MENU_MESSAGE = "main_menu_message";
  private static final String UTTER_MAIN_MENU_MESSAGE = "utter_main_menu_message";
  private static final String MAIN_MENU_OPTIONS_COUNT = "main_menu_options_count";
  private static final String MAIN_MENU_OPTION_PREFIX = "main_menu_option_";
  private static final String DETAIL_SUFFIX = "_d";
  private static final String MAX_OPTIONS = "maxOptions";

  private static final String INFO_BOT_TEMPLATE = "infobot-template";
  private static final String DOMAIN_YAML = "domain.yml";

  private static final ObjectMapper OBJECT_MAPPER = new YAMLMapper();

  private final DB mDB;
  private final FileSystem mFS;
  private final File mStateFolder;
  private final File mZipsFolder;

  public BotsService(DB pDB, FileSystem pFS, JsonObject pConfig)
    throws DocumentExistsException, DocumentException {
    this.mDB = Objects.requireNonNull(pDB, "DB should be provided");
    this.mFS = Objects.requireNonNull(pFS, "FS should be provided");

    this.mStateFolder = new File(pConfig.getString(Constants.STATE_FOLDER_KEY, Constants.DEFAULT_STATE_FOLDER_VALUE));
    this.mZipsFolder = new File(pConfig.getString(Constants.ZIPS_ROOT_KEY, Constants.DEFAULT_ZIPS_ROOT_VALUE));

    if (!mDB.hasCollection(BOTS_COLLECTION_ID)) {
      mDB.createCollection(Collection
        .newBuilder()
        .withId(BOTS_COLLECTION_ID)
        .build());
    }
  }

  public Collection getBots()
    throws DocumentNotFoundException, DocumentException {
    return mDB.getCollection(BOTS_COLLECTION_ID);
  }

  public Document getBot(String pId)
    throws DocumentNotFoundException, DocumentException {
    return mDB.getDocument(BOTS_COLLECTION_ID, pId);
  }

  public Document createBot(JsonObject pPayload)
    throws DocumentExistsException, DocumentException {
    Document newDoc = Document
      .newBuilder()
      .withId(Utils.generateId())
      .underCollection(BOTS_COLLECTION_ID)
      .withPayload(pPayload)
      .build();

    return mDB.createDocument(newDoc);
  }

  public Document updateBot(String pId, JsonObject pPayload)
    throws DocumentNotFoundException, DocumentException {
    Document updatedDoc = Document
      .newBuilder()
      .withId(pId)
      .underCollection(BOTS_COLLECTION_ID)
      .withPayload(pPayload)
      .build();

    return mDB.updateDocument(updatedDoc);
  }

  public Document deleteBot(String pId)
    throws DocumentNotFoundException, DocumentException {
    return mDB.deleteDocument(BOTS_COLLECTION_ID, pId);
  }

  public Document getStats()
    throws DocumentNotFoundException, DocumentException {
    var collection = mDB.getCollection(BOTS_COLLECTION_ID);
    JsonObject stats = new JsonObject();
    stats.put("botsCreated", collection.documents().size());
    // TODO: Gather and report real stats
    stats.put("reqsPerDay", 0);

    return Document
      .newBuilder()
      .withId("stats")
      .withPayload(stats)
      .build();
  }

  public void testBot(String pId) {
    // TODO: Interact with docker over REST or unix socket
    //       to build bot image and run a test container.
  }

  public Document zipBot(String pId)
    throws DocumentNotFoundException, DocumentException, IOException {
    File templateFolder = new File(mStateFolder, INFO_BOT_TEMPLATE);
    File botFolder = new File(mZipsFolder, pId);
    if (botFolder.exists()) {
      mFS.deleteRecursiveBlocking(botFolder.getCanonicalPath(), true);
    }
    botFolder.mkdirs();
    mFS.copyRecursiveBlocking(templateFolder.getCanonicalPath(), botFolder.getCanonicalPath(), true);

    File domainFile = new File(botFolder, DOMAIN_YAML);
    var reader = OBJECT_MAPPER.reader();
    JsonNode domainJson = reader.readTree(new FileInputStream(domainFile));

    Document botDoc = mDB.getDocument(MenusService.MENUS_COLLECTION_ID, pId);
    JsonObject menuJsonObject = botDoc.payload();
    prepareDomain(domainJson, menuJsonObject);
    var writer = OBJECT_MAPPER.writer();
    writer.writeValue(domainFile, domainJson);

    Utils.zip(botFolder, new File(botFolder.getParent(), pId + ".zip"));
    mFS.deleteRecursiveBlocking(botFolder.getCanonicalPath(), true);

    return botDoc;
  }

  private void prepareDomain(JsonNode pDomainJson, JsonObject pMenuJsonObject) {
    ObjectNode responsesJson = (ObjectNode) pDomainJson.get("responses");
    responsesJson.set(UTTER_WELCOME_MESSAGE,
      createResponseArray(pMenuJsonObject.getString(WELCOME_MESSAGE)));
    responsesJson.set(UTTER_MAIN_MENU_MESSAGE,
      createResponseArray(pMenuJsonObject.getString(MAIN_MENU_MESSAGE)));

    int maxOptions = pMenuJsonObject.getInteger(MAIN_MENU_OPTIONS_COUNT);
    for (int o = 1; o <= maxOptions; o++) {
      var k1 = MAIN_MENU_OPTION_PREFIX + o;
      var k2 = k1 + DETAIL_SUFFIX;
      responsesJson.set("utter_" + k1, createResponseArray(pMenuJsonObject.getString(k1)));
      responsesJson.set("utter_" + k2, createResponseArray(pMenuJsonObject.getString(k2)));
    }

    ObjectNode slotsJson = (ObjectNode) pDomainJson.get("slots");
    slotsJson.set(MAX_OPTIONS, createMaxOptionsJson(maxOptions));
  }

  private ArrayNode createResponseArray(String pText) {
    ArrayNode response = OBJECT_MAPPER.createArrayNode();
    ObjectNode json = OBJECT_MAPPER.createObjectNode();
    json.put("text", pText);
    response.add(json);
    return response;
  }

  private ObjectNode createMaxOptionsJson(int pMaxOptions) {
    ObjectNode json = OBJECT_MAPPER.createObjectNode();
    json.put("type", "text");
    json.put("initial_value", pMaxOptions);
    return json;
  }
}
