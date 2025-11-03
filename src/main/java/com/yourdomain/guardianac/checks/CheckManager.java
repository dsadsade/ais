package com.yourdomain.guardianac.checks;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.movement.FlyCheck;
import com.yourdomain.guardianac.checks.movement.SpeedCheck;

import java.util.ArrayList;
import java.util.List;

public class CheckManager {

    private final GuardianAC plugin;
    private final List<Check> checks;

    public CheckManager(GuardianAC plugin) {
        this.plugin = plugin;
        this.checks = new ArrayList<>();
        registerChecks();
    }

    private void registerChecks() {
        checks.add(new FlyCheck(plugin));
        checks.add(new SpeedCheck(plugin));

        for (Check check : checks) {
            plugin.getServer().getPluginManager().registerEvents(check, plugin);
        }
    }

    public List<Check> getChecks() {
        return checks;
    }

    public Check getCheckByName(String name) {
        for (Check check : checks) {
            if (check.getName().equalsIgnoreCase(name)) {
                return check;
            }
        }
        return null;
    }
}
