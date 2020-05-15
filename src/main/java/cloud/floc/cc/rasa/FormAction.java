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

package cloud.floc.cc.rasa;

import cloud.floc.cc.common.DocumentException;
import cloud.floc.cc.common.DocumentExistsException;
import cloud.floc.cc.common.DocumentNotFoundException;
import cloud.floc.cc.rasa.event.Event;

import java.util.List;
import java.util.Map;

public interface FormAction
  extends Action {

  List<String> requiredSlots(Tracker pTracker);

  Map<String, List<SlotExtractor>> slotExtractorsMap();

  List<Event> submit(Tracker pTracker, ActionResult pResult)
    throws DocumentException, DocumentNotFoundException, DocumentExistsException;
}
