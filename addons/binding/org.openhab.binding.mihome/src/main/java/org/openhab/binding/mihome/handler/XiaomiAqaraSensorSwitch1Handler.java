/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.CHANNEL_AQARA_CH0;

import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author Dimalo
 */
public class XiaomiAqaraSensorSwitch1Handler extends XiaomiSensorBaseHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public XiaomiAqaraSensorSwitch1Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected void parseReport(JsonObject data) {
        if (data.has("channel_0")) {
            triggerChannel(CHANNEL_AQARA_CH0, data.get("channel_0").getAsString().toUpperCase());
        }
    }
}
