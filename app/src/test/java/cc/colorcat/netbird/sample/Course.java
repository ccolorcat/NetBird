/*
 * Copyright 2018 cxx
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

package cc.colorcat.netbird.sample;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * Author: cxx
 * Date: 2018-10-09
 * GitHub: https://github.com/ccolorcat
 */
public class Course {

    /**
     * id : 30
     * name : CSS未来
     * picSmall : http://img.mukewang.com/54c87c73000150cf06000338-300-170.jpg
     * picBig : http://img.mukewang.com/54c87c73000150cf06000338.jpg
     * description : CSS之父Bert Bos带来的分享。
     * learner : 13436
     */

    @JSONField(name = "id")
    @JsonProperty("id")
    @SerializedName("id")
    private int id;
    @JSONField(name = "name")
    @JsonProperty("name")
    @SerializedName("name")
    private String name;
    @JSONField(name = "picSmall")
    @JsonProperty("picSmall")
    @SerializedName("picSmall")
    private String picSmall;
    @JSONField(name = "picBig")
    @JsonProperty("picBig")
    @SerializedName("picBig")
    private String picBig;
    @JSONField(name = "description")
    @JsonProperty("description")
    @SerializedName("description")
    private String description;
    @JSONField(name = "learner")
    @JsonProperty("learner")
    @SerializedName("learner")
    private int learner;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicSmall() {
        return picSmall;
    }

    public void setPicSmall(String picSmall) {
        this.picSmall = picSmall;
    }

    public String getPicBig() {
        return picBig;
    }

    public void setPicBig(String picBig) {
        this.picBig = picBig;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLearner() {
        return learner;
    }

    public void setLearner(int learner) {
        this.learner = learner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Course course = (Course) o;

        if (id != course.id) return false;
        if (learner != course.learner) return false;
        if (name != null ? !name.equals(course.name) : course.name != null) return false;
        if (picSmall != null ? !picSmall.equals(course.picSmall) : course.picSmall != null)
            return false;
        if (picBig != null ? !picBig.equals(course.picBig) : course.picBig != null) return false;
        return description != null ? description.equals(course.description) : course.description == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (picSmall != null ? picSmall.hashCode() : 0);
        result = 31 * result + (picBig != null ? picBig.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + learner;
        return result;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", picSmall='" + picSmall + '\'' +
                ", picBig='" + picBig + '\'' +
                ", description='" + description + '\'' +
                ", learner=" + learner +
                '}';
    }
}
