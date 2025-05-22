/*
 *
 *  *
 *  * ******************************************************************************
 *  *
 *  *  Copyright (c) 2023-24 Harman International
 *  *
 *  *
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *
 *  *  you may not use this file except in compliance with the License.
 *  *
 *  *  You may obtain a copy of the License at
 *  *
 *  *
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  **
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *
 *  *  See the License for the specific language governing permissions and
 *  *
 *  *  limitations under the License.
 *  *
 *  *
 *  *
 *  *  SPDX-License-Identifier: Apache-2.0
 *  *
 *  *  *******************************************************************************
 *  *
 *
 */

package org.eclipse.ecsp.notification.dao;

import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SecondaryContactDAOImpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class SecondaryContactDAOImpl extends IgniteBaseDAOMongoImpl<String, SecondaryContact>
    implements SecondaryContactDAO {

    @Autowired
    private EncryptDecryptInterface encryptDecryptInterface;

    /**
     * save method.
     *
     * @param secondaryContact secondaryContact
     * @return SecondaryContact
     */
    @Override
    public SecondaryContact save(SecondaryContact secondaryContact) {
        encryptSecondaryContact(secondaryContact);
        return decryptSecondaryContact(super.save(secondaryContact));
    }

    /**
     * Find by id method.
     *
     * @param id id
     * @return SecondaryContact
     */
    @Override
    public SecondaryContact findById(String id) {
        SecondaryContact secondaryContact = super.findById(id);
        if (null != secondaryContact) {
            return decryptSecondaryContact(secondaryContact);
        }
        return null;
    }

    /**
     * Find by ids method.
     *
     * @param ids ids
     * @return List of SecondaryContact
     */
    @Override
    public List<SecondaryContact> findByIds(String[] ids) {
        List<SecondaryContact> decryptedSecondaryContacts = new ArrayList<SecondaryContact>();
        List<String> idList = null;

        if (null != ids && ids.length != 0) {
            idList = Arrays.asList(ids);
        }

        if (null != idList && !idList.isEmpty()) {
            idList.parallelStream().forEach(id -> decryptedSecondaryContacts.add(findById(id)));
        }

        return decryptedSecondaryContacts;
    }

    /**
     * encryptSecondaryContact method.
     *
     * @param secondaryContact secondaryContact
     */
    private void encryptSecondaryContact(SecondaryContact secondaryContact) {
        if (null != secondaryContact.getEmail()) {
            secondaryContact.setEmail(encryptDecryptInterface.encrypt(secondaryContact.getEmail()));
        }
        if (null != secondaryContact.getPhoneNumber()) {
            secondaryContact.setPhoneNumber(encryptDecryptInterface.encrypt(secondaryContact.getPhoneNumber()));
        }
    }

    /**
     * decryptSecondaryContact method.
     *
     * @param secondaryContact secondaryContact
     * @return SecondaryContact
     */
    private SecondaryContact decryptSecondaryContact(SecondaryContact secondaryContact) {
        if (null != secondaryContact.getEmail()) {
            secondaryContact.setEmail(encryptDecryptInterface.decrypt(secondaryContact.getEmail()));
        }
        if (null != secondaryContact.getPhoneNumber()) {
            secondaryContact.setPhoneNumber(encryptDecryptInterface.decrypt(secondaryContact.getPhoneNumber()));
        }
        return secondaryContact;
    }

    /**
     * getContactIds method.
     *
     * @param userId userId
     * @param vehicleId vehicleId
     * @return List of String
     */
    @Override
    public List<String> getContactIds(String userId, String vehicleId) {
        IgniteCriteria userCriteria = new IgniteCriteria("userId", Operator.EQ, userId);
        IgniteCriteria vehicleCriteria = new IgniteCriteria("vehicleId", Operator.EQ, vehicleId);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(userCriteria).and(vehicleCriteria);
        IgniteQuery query = new IgniteQuery(cg);
        return find(query).stream().map(SecondaryContact::getContactId).collect(Collectors.toList());

    }

    /**
     * getContacts method.
     *
     * @param userId userId
     * @param vehicleId vehicleId
     * @return List of SecondaryContact
     */
    @Override
    public List<SecondaryContact> getContacts(String userId, String vehicleId) {
        IgniteCriteria userCriteria = new IgniteCriteria("userId", Operator.EQ, userId);
        IgniteCriteria vehicleCriteria = new IgniteCriteria("vehicleId", Operator.EQ, vehicleId);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(userCriteria).and(vehicleCriteria);
        IgniteQuery query = new IgniteQuery(cg);
        return find(query);
    }
}