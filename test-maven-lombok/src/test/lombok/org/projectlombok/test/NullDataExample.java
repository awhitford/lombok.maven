package org.projectlombok.test;

import lombok.Data;
//import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
public class NullDataExample {

    @NotNull //@NonNull
    private String dataOne;

    @NotNull
    private String dataTwo;
}

