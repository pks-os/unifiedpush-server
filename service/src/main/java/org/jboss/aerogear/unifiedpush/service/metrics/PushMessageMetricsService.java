/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.service.metrics;

import java.util.Date;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantErrorStatus;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.jboss.aerogear.unifiedpush.utils.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to handle different aspects of the Push Message Information metadata for the "Push Message History" view
 * on the Admin UI.
 */
@Service
@Transactional
public class PushMessageMetricsService implements IPushMessageMetricsService {

    // system property name used as the configurable maximum days the message information objects are stored
    public static final String AEROGEAR_METRICS_STORAGE_MAX_DAYS = "aerogear.metrics.storage.days";

    @Inject
    private FlatPushMessageInformationDao flatPushMessageInformationDao;

    /* (non-Javadoc)
	 * @see org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService#storeNewRequestFrom(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
    @Override
	public FlatPushMessageInformation storeNewRequestFrom(String pushAppId, String json, String ipAddress, String clientIdentifier) {
        final FlatPushMessageInformation information = new FlatPushMessageInformation();

        information.setRawJsonMessage(json);
        information.setIpAddress(ipAddress);
        information.setPushApplicationId(pushAppId);
        information.setClientIdentifier(clientIdentifier);

        flatPushMessageInformationDao.create(information);
        flatPushMessageInformationDao.flushAndClear();

        return information;
    }

    /* (non-Javadoc)
	 * @see org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService#updatePushMessageInformation(org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation)
	 */
    @Override
	public void updatePushMessageInformation(FlatPushMessageInformation pushMessageInformation) {
        flatPushMessageInformationDao.update(pushMessageInformation);
    }

    /* (non-Javadoc)
	 * @see org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService#appendError(org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation, org.jboss.aerogear.unifiedpush.api.Variant, java.lang.String)
	 */
    @Override
	public void appendError(final FlatPushMessageInformation pushMessageInformation, final Variant variant, final String errorMessage) {
        final VariantErrorStatus ves = new VariantErrorStatus(pushMessageInformation, variant, errorMessage);
        pushMessageInformation.getErrors().add(ves);
        flatPushMessageInformationDao.update(pushMessageInformation);
    }

    /* (non-Javadoc)
	 * @see org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService#findAllFlatsForPushApplication(java.lang.String, java.lang.String, boolean, java.lang.Integer, java.lang.Integer)
	 */
    @Override
	public PageResult<FlatPushMessageInformation, MessageMetrics> findAllFlatsForPushApplication(String pushApplicationID, String search, boolean sorting, Integer page, Integer pageSize) {
        return flatPushMessageInformationDao.findAllForPushApplication(pushApplicationID, search, sorting, page, pageSize);
    }

    /* (non-Javadoc)
	 * @see org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService#countMessagesForPushApplication(java.lang.String)
	 */
    @Override
	public long countMessagesForPushApplication(String pushApplicationId) {
        return flatPushMessageInformationDao.getNumberOfPushMessagesForPushApplication(pushApplicationId);
    }

    /* (non-Javadoc)
	 * @see org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService#deleteOutdatedFlatPushInformationData()
	 */
    @Override
	public void deleteOutdatedFlatPushInformationData() {
        final Date historyDate = DateUtils.calculatePastDate(ConfigurationUtils.tryGetIntegerProperty(AEROGEAR_METRICS_STORAGE_MAX_DAYS, 30));
        flatPushMessageInformationDao.deletePushInformationOlderThan(historyDate);
    }

    public FlatPushMessageInformation getPushMessageInformation(String id) {
        return flatPushMessageInformationDao.find(id);
    }

    /* (non-Javadoc)
	 * @see org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService#updateAnalytics(java.lang.String)
	 */
    @Override
	public void updateAnalytics(String aerogearPushId) {
        FlatPushMessageInformation pushMessageInformation = this.getPushMessageInformation(aerogearPushId);

        if (pushMessageInformation != null) { //if we are here, app has been opened due to a push message

            //if the firstOpenDate is not null that means it's no the first one, let's update the lastDateOpen
            if (pushMessageInformation.getFirstOpenDate() != null) {
                pushMessageInformation.setLastOpenDate(new Date());
            } else {
                pushMessageInformation.setFirstOpenDate(new Date());
                pushMessageInformation.setLastOpenDate(new Date());
            }
            //update the general counter
            pushMessageInformation.incrementAppOpenCounter();
        }

    }
}
