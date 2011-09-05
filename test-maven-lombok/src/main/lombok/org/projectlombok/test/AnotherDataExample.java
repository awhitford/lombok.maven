package org.projectlombok.test;

import lombok.Data;
// http://code.google.com/p/projectlombok/issues/detail?id=146
//import lombok.NonNull;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class AnotherDataExample implements Useful {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherDataExample.class);

    //@NonNull
    private final DataExample dataExample;

    //@NonNull
    private final String moreInformation;

    public void doSomething() {
        LOGGER.debug("Doing something useful...");
    }

    // Example related to http://code.google.com/p/projectlombok/issues/detail?id=269
    @XmlType
    @XmlEnum(Integer.class)
    public enum Coin { 
        @XmlEnumValue("1") PENNY(1),
        @XmlEnumValue("5") NICKEL(5),
        @XmlEnumValue("10") DIME(10),
        @XmlEnumValue("25") QUARTER(25);

        private final int cents;

        Coin (final int cents) {
            this.cents = cents;
        }
    }
}

