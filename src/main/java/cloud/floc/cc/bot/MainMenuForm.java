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

package cloud.floc.cc.bot;

import cloud.floc.cc.common.DocumentException;
import cloud.floc.cc.common.DocumentExistsException;
import cloud.floc.cc.common.DocumentNotFoundException;
import cloud.floc.cc.db.DB;
import cloud.floc.cc.rasa.ActionResult;
import cloud.floc.cc.rasa.BaseFormAction;
import cloud.floc.cc.rasa.SlotExtractor;
import cloud.floc.cc.rasa.Tracker;
import cloud.floc.cc.rasa.event.Event;
import cloud.floc.cc.service.MenusService;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MainMenuForm
  extends BaseFormAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuForm.class);

  private final MenusService mService;

  public MainMenuForm(DB pDB)
    throws Exception {
    super("main_menu_form", LOGGER, pDB);

    mService = new MenusService(pDB);
  }

  @Override
  public List<String> requiredSlots(Tracker pTracker) {
    return List.of(
      "welcome_message",
      "main_menu_message",
      "main_menu_options_count"
    );
  }

  @Override
  public Map<String, List<SlotExtractor>> slotExtractorsMap() {
    return Map.of(
      "welcome_message", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_message", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_options_count", List.of(new SlotExtractor("number"))
    );
  }

  @Override
  public List<Event> submit(Tracker pTracker, ActionResult pResult)
    throws DocumentExistsException, DocumentNotFoundException, DocumentException {
    // Save the collected slots.
    JsonObject payload = new JsonObject();
    payload.put("welcome_message", pTracker.slotValue("welcome_message"));
    payload.put("main_menu_message", pTracker.slotValue("main_menu_message"));
    payload.put("main_menu_options_count", pTracker.slotValue("main_menu_options_count"));

    String botId = pTracker.metadata().getString("botId", null);
    if (botId == null) throw new DocumentException("Unable to retrieve the bot ID");

    LOGGER.info("Saving menu for bot: {0}", botId);
    mService.upsertMenu(botId, payload);
    LOGGER.info("Saved menu for bot: {0}", botId);

    pResult.addTemplateMessage("utter_got_it_welcome_message");
    return List.of();
  }
}
