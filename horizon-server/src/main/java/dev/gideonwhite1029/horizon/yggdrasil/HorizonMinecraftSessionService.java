package dev.gideonwhite1029.horizon.yggdrasil;

import com.destroystokyo.paper.profile.PaperMinecraftSessionService;
import com.mojang.authlib.Environment;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import com.mojang.authlib.yggdrasil.response.ProfileAction;
import dev.gideonwhite1029.horizon.HorizonConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class HorizonMinecraftSessionService extends PaperMinecraftSessionService {

    protected HorizonMinecraftSessionService(ServicesKeySet keySet, Proxy authenticationService, Environment environment) {
        super(keySet, authenticationService, environment);
    }

    private static List<URL> extraYggdrasilList = List.of();

    public static void initExtraYggdrasilList(List<String> extraYggdrasilServiceList) {
        List<URL> list = new ArrayList<>();
        for (String str : extraYggdrasilServiceList) {
            list.add(HttpAuthenticationService.constantURL(str + "/sessionserver/session/minecraft/hasJoined"));
        }
        extraYggdrasilList = Collections.unmodifiableList(list);
    }

    @Nullable
    @Override
    public ProfileResult hasJoinedServer(String profileName, String serverId, @Nullable InetAddress address) throws AuthenticationUnavailableException {
        ProfileResult result = super.hasJoinedServer(profileName, serverId, address); // mojang

        ServerPlayer player = MinecraftServer.getServer().getPlayerList().getPlayerByName(profileName);

        if (HorizonConfig.extraYggdrasilService && result == null) {
            final Map<String, Object> arguments = new HashMap<>();
            arguments.put("username", profileName);
            arguments.put("serverId", serverId);

            if (address != null) {
                arguments.put("ip", address.getHostAddress());
            }

            NameAndId cache = null;
            if (HorizonConfig.loginProtect) {
                cache = MinecraftServer.getServer().services.nameToIdCache().getIfCached(profileName);
            }

            for (URL checkUrl : extraYggdrasilList) {
                URL url = HttpAuthenticationService.concatenateURL(checkUrl, HttpAuthenticationService.buildQuery(arguments));
                try {
                    final HasJoinedMinecraftServerResponse response = client.get(url, HasJoinedMinecraftServerResponse.class);
                    if (response != null && response.id() != null) {
                        if (HorizonConfig.loginProtect && cache != null) {
                            if (!response.id().equals(cache.id())) {
                                continue;
                            }
                        }

                        final GameProfile result1 = new GameProfile(response.id(), profileName);
                        if (response.properties() != null) {
                            result1.properties().putAll(response.properties());
                        }

                        final Set<ProfileActionType> profileActions = response.profileActions().stream()
                                .map(ProfileAction::type)
                                .collect(Collectors.toSet());
                        return new ProfileResult(result1, profileActions);
                    }
                } catch (final MinecraftClientException e) {
                    if (e.toAuthenticationException() instanceof final AuthenticationUnavailableException unavailable) {
                        throw unavailable;
                    }
                }
            }
        }
        return result;
    }
}
