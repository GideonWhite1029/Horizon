package dev.gideonwhite1029.horizon.yggdrasil;

import com.destroystokyo.paper.profile.PaperAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import java.net.Proxy;

public class HorizonAuthenticationService extends PaperAuthenticationService {

    public HorizonAuthenticationService(Proxy proxy) {
        super(proxy);
    }

    @Override
    public MinecraftSessionService createMinecraftSessionService() {
        return new HorizonMinecraftSessionService(this.getServicesKeySet(), this.getProxy(), this.environment);
    }
}
