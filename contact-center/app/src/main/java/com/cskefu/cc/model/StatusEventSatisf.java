/*
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2018- Jun. 2023 Chatopera Inc, <https://www.chatopera.com>,  Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Copyright (C) 2017 优客服-多渠道客服系统,  Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.cskefu.cc.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Proxy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "uk_callcenter_event")
@Proxy(lazy = false)
public class StatusEventSatisf implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3169891408571074136L;
	private String id ;
	private boolean satisf ;	//是否记录满意度调查
	private String satisfaction 	;		//服务小结ID
	private Date satisfdate ;				//满意度调查提交时间
	
	@Id
	@Column(length = 32)
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "assigned")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isSatisf() {
		return satisf;
	}
	public void setSatisf(boolean satisf) {
		this.satisf = satisf;
	}
	public String getSatisfaction() {
		return satisfaction;
	}
	public void setSatisfaction(String satisfaction) {
		this.satisfaction = satisfaction;
	}
	public Date getSatisfdate() {
		return satisfdate;
	}
	public void setSatisfdate(Date satisfdate) {
		this.satisfdate = satisfdate;
	}
}
