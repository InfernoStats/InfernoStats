package com.infernostats.specialweapon;

import com.infernostats.InfernoStatsPlugin;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.infobox.Counter;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SpecialWeaponStats extends Counter {
    private final SpecialWeapon weapon;
    private int hitAttempts;
    private int hitSuccesses;
    private int healthRestored;
    private int prayerRestored;

    private static Client client;

    public SpecialWeaponStats(final Client client, BufferedImage image, InfernoStatsPlugin plugin, SpecialWeapon weapon)
    {
        super(image, plugin, 0);
        this.client = client;
        this.weapon = weapon;
        this.hitAttempts = 0;
        this.hitSuccesses = 0;
        this.healthRestored = 0;
        this.prayerRestored = 0;
    }

    public void addHits(int hit, int lastCurrentHealth, int currentHealth, int lastCurrentPrayer, int currentPrayer, boolean effectiveRestoration)
    {
        this.hitAttempts += 1;
        this.hitSuccesses += (hit > 0) ? 1 : 0;

        if (effectiveRestoration)
        {
            this.healthRestored += currentHealth - lastCurrentHealth;
            this.prayerRestored += currentPrayer - lastCurrentPrayer;
        }
        else
        {
            final int healthLevel = client.getRealSkillLevel(Skill.HITPOINTS);
            final int prayerLevel = client.getRealSkillLevel(Skill.PRAYER);
            final int maxHealthRestoration = healthLevel - currentHealth;
            final int maxPrayerRestoration; // Determined by SpecialWeapon

            switch (this.weapon){
                case SARADOMIN_GODSWORD:
                case SARADOMIN_GODSWORD_OR:
                    maxPrayerRestoration = prayerLevel - currentPrayer;

                    if (hit == 0)
                    {
                        // We have no way of knowing whether this was a
                        // successful hit 0 or a miss 0. We will always assume
                        // that the hit was a miss 0. This branch is a no-op.
                        //
                        // https://oldschool.runescape.wiki/w/Successful_hit
                        this.healthRestored += 0;
                        this.prayerRestored += 0;
                    }
                    else if (hit < 22)
                    {
                        this.healthRestored += Math.min(10, maxHealthRestoration);
                        this.prayerRestored += Math.min(5, maxPrayerRestoration);
                    }
                    else if (hit >= 22)
                    {
                        this.healthRestored += Math.min((int)(hit / 2), maxHealthRestoration);
                        this.prayerRestored += Math.min((int)(hit / 4), maxPrayerRestoration);
                    }
                    break;
                case TOXIC_BLOWPIPE:
                    if (hit != 0)
                    {
                        this.healthRestored += Math.min((int)(hit / 2), maxHealthRestoration);
                    }
                    break;
                case ELDRITCH_NIGHTMARE_STAFF:
                    maxPrayerRestoration = SpecialWeapon.ELDRITCH_PRAYER_CAP - currentPrayer;
                    if (hit != 0)
                    {
                        this.prayerRestored += Math.min((int)(hit / 2), maxPrayerRestoration);
                    }
                    break;
                default:
                    break;
            }
        }

        if (this.weapon.isPrayer())
        {
            setCount(this.prayerRestored);
        }
        else
        {
            setCount(this.healthRestored);
        }
    }

    @Override
    public String getTooltip()
    {
        if (weapon.isHealth() && weapon.isPrayer())
        {
            return weapon.getName() + " special has hit "
                    + hitSuccesses + "/" + hitAttempts + " time(s) for "
                    + healthRestored + " health and " + prayerRestored + " prayer restored.";
        }
        else if (weapon.isHealth())
        {
            return weapon.getName() + " special has hit "
                    + hitSuccesses + "/" + hitAttempts + " time(s) for "
                    + healthRestored + " health restored.";
        }
        else
        {
            return weapon.getName() + " special has hit "
                    + hitSuccesses + "/" + hitAttempts + " time(s) for "
                    + prayerRestored + " prayer restored.";
        }
    }

    @Override
    public Color getTextColor()
    {
        if (this.weapon.isPrayer())
        {
            final int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
            return prayerRestored >= superRestore(maxPrayer) ? Color.GREEN : Color.RED;
        }
        return Color.WHITE;
    }

    // Formula for points restored per dose of a super restore potion.
    // Result is multiplied by 4 to account for a full potion.
    private int superRestore(int prayerLevel)
    {
        return 4 * ((int)Math.floor(prayerLevel * 27/100) + 8);
    }
}