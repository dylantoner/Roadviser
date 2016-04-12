/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.nuig.trafficappbackend.models;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

/**
 * UserAccount entity.
 */
@Entity
public class UserAccount {

    /**
     * Unique identifier of this Entity in the database.
     */
    @Id
    private Long ID;

    @Index
    private String email;
    private String displayName;
    private int reputation;
    private Date dateJoined;
    private int numIncidents;

    public UserAccount(){
        this.dateJoined = new Date();
        this.reputation =0;
        this.numIncidents =0;
    }

    public Date getDateJoined() {
        return dateJoined;
    }

    public String getEmail() {return email;}

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public Long getID() {return ID;}

    public void setID(Long ID) {this.ID = ID;}

    public int getNumIncidents() {return numIncidents;}

    public void setNumIncidents(int numIncidents) {this.numIncidents = numIncidents;}
}
