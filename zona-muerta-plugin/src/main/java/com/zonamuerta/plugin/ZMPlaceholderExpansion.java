package com.zonamuerta.plugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Placeholders de Zona Muerta para PlaceholderAPI.
 *
 *  %zm_infection%        → porcentaje de infección del jugador (0–100)
 *  %zm_dia%              → día actual de la apocalipsis
 *  %zm_luna_sangre%      → "SI" / "NO" según estado de Luna de Sangre
 *  %zm_mutacion_rate%    → tasa de mutación actual (0–100)
 *  %zm_infectado%        → "true" / "false" si el jugador está infectado
 */
public class ZMPlaceholderExpansion extends PlaceholderExpansion {

    private final ZonaMuerta plugin;
    private final Infected infected;

    public ZMPlaceholderExpansion(ZonaMuerta plugin, Infected infected) {
        this.plugin = plugin;
        this.infected = infected;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "zm";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ZonaMuerta";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // Mantener registrado aunque PlaceholderAPI se recargue
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        switch (identifier.toLowerCase()) {
            case "infection":
                return String.valueOf((int) infected.getInfectionLevel(player));

            case "dia":
                return String.valueOf(plugin.getCurrentDay());

            case "luna_sangre":
                return plugin.isBloodMoon() ? "§c☾ SÍ" : "§aNO";

            case "mutacion_rate":
                return String.format("%.1f", plugin.getCurrentMutationRate());

            case "infectado":
                return infected.isPlayerInfected(player) ? "true" : "false";

            default:
                return null;
        }
    }
}
