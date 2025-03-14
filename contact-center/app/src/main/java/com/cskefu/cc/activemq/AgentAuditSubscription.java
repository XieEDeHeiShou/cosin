/* 
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2019-2022 Chatopera Inc, <https://www.chatopera.com>, 
 * Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.cskefu.cc.activemq;

import com.cskefu.cc.basic.Constants;
import com.cskefu.cc.cache.Cache;
import com.cskefu.cc.exception.CSKefuException;
import com.cskefu.cc.model.AgentUser;
import com.cskefu.cc.model.AgentUserAudit;
import com.cskefu.cc.persistence.repository.AgentUserRepository;
import com.cskefu.cc.proxy.AgentAuditProxy;
import com.cskefu.cc.socketio.client.NettyClients;
import com.cskefu.cc.util.SerializeUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * 会话监控
 */
@Component
public class AgentAuditSubscription {
    private final static Logger logger = LoggerFactory.getLogger(AgentAuditSubscription.class);

    @Autowired
    private Cache cache;

    @Autowired
    private AgentAuditProxy agentAuditProxy;

    @Autowired
    private AgentUserRepository agentUserRes;


    /**
     * 接收坐席会话监控消息
     *
     * @param msg
     */
    @JmsListener(destination = "${cskefu.activemq.destination.prefix}" + Constants.AUDIT_AGENT_MESSAGE + "${cskefu.activemq.destination.suffix}", containerFactory = "jmsListenerContainerTopic")
    public void onMessage(final String msg) {
        logger.info("[onMessage] payload {}", msg);
        try {
            final JsonObject json = new JsonParser().parse(msg).getAsJsonObject();

            if (json.has("data") &&
                    json.has("agentUserId") &&
                    json.has("event") && json.has("agentno")) {

                // 查找关联的会话监控信息
                final AgentUserAudit agentUserAudit = cache.findOneAgentUserAuditById(
                        json.get("agentUserId").getAsString()).orElseGet(() -> {
                    final AgentUser agentUser = agentUserRes.findById(json.get("agentUserId").getAsString()).orElse(null);
                    if (agentUser != null) {
                        return agentAuditProxy.updateAgentUserAudits(agentUser);
                    } else {
                        logger.warn(
                                "[onMessage] can not find agent user by id {}", json.get("agentUserId").getAsString());
                    }
                    return null;
                });

                if (agentUserAudit != null) {
                    final String agentno = json.get("agentno").getAsString();
                    logger.info(
                            "[onMessage] agentno {}, subscribers size {}, subscribers {}", agentno,
                            agentUserAudit.getSubscribers().size(),
                            StringUtils.join(agentUserAudit.getSubscribers().keySet(), "|"));

                    // 发送消息给坐席监控，不需要分布式，因为这条消息已经是从ActiveMQ使用Topic多机广播
                    for (final String subscriber : agentUserAudit.getSubscribers().keySet()) {
                        logger.info("[onMessage] process subscriber {}", subscriber);
                        if (!StringUtils.equals(subscriber, agentno)) {
                            logger.info("[onMessage] publish event to {}", subscriber);
                            NettyClients.getInstance().publishAuditEventMessage(
                                    subscriber,
                                    json.get("event").getAsString(),
                                    SerializeUtil.deserialize(json.get("data").getAsString()));
                        }
                    }
                } else {
                    logger.warn(
                            "[onMessage] can not resolve agent user audit object for agent user id {}",
                            json.get("agentUserId").getAsString());
                }
            } else {
                throw new CSKefuException("Invalid payload.");
            }
        } catch (Exception e) {
            logger.error("[onMessage] error", e);
        }
    }
}
