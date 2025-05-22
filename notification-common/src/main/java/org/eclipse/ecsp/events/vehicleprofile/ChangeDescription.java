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

package org.eclipse.ecsp.events.vehicleprofile;

import org.eclipse.ecsp.entities.EventData;

/**
 *ChangeDescription class.
 *
 * @author schunchu
 */
public class ChangeDescription implements EventData {

    private String key;
    private String path;
    private Object old;
    private Object changed;

    /**
     * getPath method.
     *
     * @return the hierarchyKey
     */
    public String getPath() {
        return path;
    }

    /**
     * setPath method.
     *
     * @param path the hierarchyKey to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * getKey.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * setKey.
     *
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * getOld.
     *
     * @return the old
     */
    public Object getOld() {
        return old;
    }

    /**
     * setOld.
     *
     * @param old the old to set
     */
    public void setOld(Object old) {
        this.old = old;
    }

    /**
     * getChanged.
     *
     * @return the changed
     */
    public Object getChanged() {
        return changed;
    }

    /**
     * set changed.
     *
     * @param changed the changed to set
     */
    public void setChanged(Object changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return "ChangeDescription [key=" + key + ", hierarchyKey=" + path
            + ", old=" + old + ", changed=" + changed + "]";
    }

}
