package com.brooklynotter.stucktogether.configurations.goodies;

import net.programmer.igoodie.goodies.configuration.JsonConfiGoodie;
import net.programmer.igoodie.goodies.configuration.validation.annotation.GoodieInteger;
import net.programmer.igoodie.goodies.serialization.annotation.Goodie;

public class SphereConfigs extends JsonConfiGoodie {

    @Goodie
    public boolean active = false;

    @Goodie
    @GoodieInteger(min = 0)
    public int sphereRadius = 10;

}
