/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.android.avd.manager.pojo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author arsi
 */
/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="device" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="orientation" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *                           &lt;attribute name="screenPos" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *                           &lt;attribute name="screenSize" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *                           &lt;attribute name="shadow" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="back" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="lights" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "device"
})
@XmlRootElement(name = "devices")
public class Devices {

    @XmlElement(required = true)
    protected List<Devices.Device> device;

    /**
     * Gets the value of the device property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the device property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDevice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Devices.Device }
     * 
     * 
     */
    public List<Devices.Device> getDevice() {
        if (device == null) {
            device = new ArrayList<Devices.Device>();
        }
        return this.device;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="orientation" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}decimal" />
     *                 &lt;attribute name="screenPos" type="{http://www.w3.org/2001/XMLSchema}decimal" />
     *                 &lt;attribute name="screenSize" type="{http://www.w3.org/2001/XMLSchema}decimal" />
     *                 &lt;attribute name="shadow" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="back" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="lights" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "orientation"
    })
    public static class Device {

        @XmlElement(required = true)
        protected List<Devices.Device.Orientation> orientation;
        @XmlAttribute(name = "id")
        protected String id;
        @XmlAttribute(name = "name")
        protected String name;

        /**
         * Gets the value of the orientation property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the orientation property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOrientation().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Devices.Device.Orientation }
         * 
         * 
         */
        public List<Devices.Device.Orientation> getOrientation() {
            if (orientation == null) {
                orientation = new ArrayList<Devices.Device.Orientation>();
            }
            return this.orientation;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setId(String value) {
            this.id = value;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}decimal" />
         *       &lt;attribute name="screenPos" type="{http://www.w3.org/2001/XMLSchema}decimal" />
         *       &lt;attribute name="screenSize" type="{http://www.w3.org/2001/XMLSchema}decimal" />
         *       &lt;attribute name="shadow" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="back" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="lights" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Orientation {

            @XmlAttribute(name = "name")
            protected String name;
            @XmlAttribute(name = "size")
            protected String size;
            @XmlAttribute(name = "screenPos")
            protected String screenPos;
            @XmlAttribute(name = "screenSize")
            protected String screenSize;
            @XmlAttribute(name = "shadow")
            protected String shadow;
            @XmlAttribute(name = "back")
            protected String back;
            @XmlAttribute(name = "lights")
            protected String lights;

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the size property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getSize() {
                return size;
            }

            /**
             * Sets the value of the size property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setSize(String value) {
                this.size = value;
            }

            /**
             * Gets the value of the screenPos property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getScreenPos() {
                return screenPos;
            }

            /**
             * Sets the value of the screenPos property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setScreenPos(String value) {
                this.screenPos = value;
            }

            /**
             * Gets the value of the screenSize property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getScreenSize() {
                return screenSize;
            }

            /**
             * Sets the value of the screenSize property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setScreenSize(String value) {
                this.screenSize = value;
            }

            /**
             * Gets the value of the shadow property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getShadow() {
                return shadow;
            }

            /**
             * Sets the value of the shadow property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setShadow(String value) {
                this.shadow = value;
            }

            /**
             * Gets the value of the back property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getBack() {
                return back;
            }

            /**
             * Sets the value of the back property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setBack(String value) {
                this.back = value;
            }

            /**
             * Gets the value of the lights property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLights() {
                return lights;
            }

            /**
             * Sets the value of the lights property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLights(String value) {
                this.lights = value;
            }

        }

    }

}

