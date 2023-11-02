package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class TierTwoMachinegun extends FirearmWeapon {

    {
        defaultAction = AC_SHOOT;
        usesTargeting = true;

        image = ItemSpriteSheet.TIER_TWO_MACHINEGUN;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.8f;

        tier = 2;
        type = FirearmType.FirearmAuto;
        max_round = 8;
        shot = 2;

        bullet_image = ItemSpriteSheet.DUAL_BULLET;
    }

    @Override
    public void setMaxRound() {
        max_round = 8;// + 3 * Dungeon.hero.pointsInTalent(Talent.DEATH_MACHINE) + 3 * Dungeon.hero.pointsInTalent(Talent.QUANTITY_OVER_QUALITY);
    }

}
