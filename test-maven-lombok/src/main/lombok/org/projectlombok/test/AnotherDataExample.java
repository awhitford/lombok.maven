package org.projectlombok.test;

import lombok.Data;
//import lombok.NonNull;

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
}

