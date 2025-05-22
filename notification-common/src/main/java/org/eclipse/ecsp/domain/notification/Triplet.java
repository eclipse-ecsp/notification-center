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

package org.eclipse.ecsp.domain.notification;

/**
 * Triplet class.
 *
 * @param <A> generic
 *
 * @param <B> generic
 *
 * @param <C> generic
 */
@SuppressWarnings("checkstyle:MemberName")
public class Triplet<A, B, C> {


    private A a;
    private B b;
    private C c;

    /**
     * Triplet constructor.
     *
     * @param a of A
     *
     * @param b of B
     *
     * @param c of C
     */
    public Triplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Getter for A.
     *
     * @return a
     */
    public A getA() {
        return a;
    }

    /**
     * Setter for A.
     *
     * @param a the new value
     */
    public void setA(A a) {
        this.a = a;
    }

    /**
     * Getter for B.
     *
     * @return b
     */
    public B getB() {
        return b;
    }

    /**
     * Setter for B.
     *
     * @param b the new value
     */
    public void setB(B b) {
        this.b = b;
    }

    /**
     * Getter for C.
     *
     * @return c
     */
    public C getC() {
        return c;
    }

    /**
     * Setter for C.
     *
     * @param c the new value
     */
    public void setC(C c) {
        this.c = c;
    }

}
