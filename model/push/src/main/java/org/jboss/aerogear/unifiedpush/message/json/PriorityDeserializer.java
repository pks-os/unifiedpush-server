/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.json;

import java.io.IOException;

import org.jboss.aerogear.unifiedpush.message.Priority;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Class for deserializing Priority enums.
 *
 * @author Summers Pittman
 */
public final class PriorityDeserializer extends JsonDeserializer<Priority> {

    @Override
    public Priority deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return Priority.valueOf(jp.getText().toUpperCase());
    }
}
