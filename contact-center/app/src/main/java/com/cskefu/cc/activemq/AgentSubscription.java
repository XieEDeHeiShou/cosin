/* 
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2019-2022 Chatopera Inc, All rights reserved. 
 * <https://www.chatopera.com>
 */
package com.cskefu.cc.activemq;

import com.cskefu.cc.basic.Constants;
import com.cskefu.cc.socketio.client.NettyClients;
import com.cskefu.cc.util.SerializeUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * WebIM Agent
 */
@Component
public class AgentSubscription {

    private final static Logger logger = LoggerFactory.getLogger(AgentSubscription.class);

    @Value("${application.node.id}")
    private String appNodeId;

    @Autowired
    private BrokerPublisher brokerPublisher;

    /**
     * Publish Message into ActiveMQ
     *
     * @param j
     */
    public void publish(JsonObject j) {
        j.addProperty("node", appNodeId);
        brokerPublisher.send(Constants.INSTANT_MESSAGING_MQ_TOPIC_AGENT, j.toString(), true);
    }

    @JmsListener(destination = "${cskefu.activemq.destination.prefix}" + Constants.INSTANT_MESSAGING_MQ_TOPIC_AGENT + "${cskefu.activemq.destination.suffix}", containerFactory = "jmsListenerContainerTopic")
    public void onMessage(final String payload) {
        logger.info("[onMessage] payload {}", payload);
        JsonParser parser = new JsonParser();
        JsonObject j = parser.parse(payload).getAsJsonObject();
        logger.debug("[onMessage] message body {}", j.toString());
        try {
            if (!j.has("id")) {
                logger.warn("[onMessage] Invalid payload, id is null");
                return;
            }

            NettyClients.getInstance().sendAgentEventMessage(
                    j.get("id").getAsString(),
                    j.get("event").getAsString(),
                    SerializeUtil.deserialize(j.get("data").getAsString()));
        } catch (Exception e) {
            logger.error("onMessage", e);
        }
    }
}
