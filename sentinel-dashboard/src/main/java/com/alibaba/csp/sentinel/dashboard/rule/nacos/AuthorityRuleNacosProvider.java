/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleCorrectEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jasonma
 * @date 2023/5/8 4:10 下午
 * @description
 */
@Component("authorityRuleNacosProvider")
public class AuthorityRuleNacosProvider implements DynamicRuleProvider<List<AuthorityRuleEntity>> {

    @Autowired
    private ConfigService configService;
    @Autowired
    private Converter<String, List<AuthorityRuleCorrectEntity>> entityDecoder;

    @Override
    public List<AuthorityRuleEntity> getRules(String appName) throws Exception {
        String rules = configService.getConfig(appName + NacosConfigUtil.AUTHORITY_DATA_ID_POSTFIX,
                NacosConfigUtil.GROUP_ID, 3000);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        List<AuthorityRuleCorrectEntity> entityList = entityDecoder.convert(rules);
        entityList.forEach(e -> e.setApp(appName));
        return entityList.stream().map(rule -> {
            AuthorityRule authorityRule = new AuthorityRule();
            BeanUtils.copyProperties(rule, authorityRule);

            AuthorityRuleEntity entity = AuthorityRuleEntity.fromAuthorityRule(rule.getApp(), rule.getIp(), rule.getPort(), authorityRule);

            entity.setId(rule.getId());
            entity.setGmtCreate(rule.getGmtCreate());
            entity.setGmtModified(rule.getGmtModified());

            return entity;
        }).collect(Collectors.toList());
    }
}
