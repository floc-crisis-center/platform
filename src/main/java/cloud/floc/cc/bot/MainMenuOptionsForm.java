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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainMenuOptionsForm
  extends BaseFormAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuOptionsForm.class);

  private final MenusService mService;

  public MainMenuOptionsForm(DB pDB)
    throws Exception {
    super("main_menu_options_form", LOGGER, pDB);

    mService = new MenusService(pDB);
  }

  @Override
  public List<String> requiredSlots(Tracker pTracker) {
    int optionsCount = Integer.parseInt(pTracker.slotValue("main_menu_options_count").toString());
    List<String> slots = new ArrayList<>();

    for (int o = 1; o <= optionsCount; o++) {
      slots.add("main_menu_option_" + o);
      slots.add("main_menu_option_" + o + "_d");
    }

    return slots;
  }

  @Override
  public Map<String, List<SlotExtractor>> slotExtractorsMap() {
    return Map.of(
      "main_menu_option_1", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_1_d", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_2", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_2_d", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_3", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_3_d", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_4", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_4_d", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_5", List.of(new SlotExtractor(SlotExtractor.Type.Text)),
      "main_menu_option_5_d", List.of(new SlotExtractor(SlotExtractor.Type.Text))
    );
  }

  @Override
  public List<Event> submit(Tracker pTracker, ActionResult pResult)
    throws DocumentException, DocumentNotFoundException {
    String botId = pTracker.metadata().getString("botId", null);
    if (botId == null) throw new DocumentException("Unable to retrieve the bot ID");

    LOGGER.info("Updating menu for bot: {0}", botId);

    JsonObject menuJson = mService.getMenu(botId).payload();
    int optionsCount = menuJson.getInteger("main_menu_options_count");
    JsonObject payload = new JsonObject(menuJson.getMap());

    for (int o = 1; o <= optionsCount; o++) {
      var q = "main_menu_option_" + o;
      var d = "main_menu_option_" + o + "_d";
      payload.put(q, pTracker.slotValue(q));
      payload.put(d, pTracker.slotValue(d));
    }

    mService.updateMenu(botId, payload);

    LOGGER.info("Updated menu for bot: {0}", botId);
    pResult.addTemplateMessage("utter_got_options_message");
    return List.of();
  }
}
