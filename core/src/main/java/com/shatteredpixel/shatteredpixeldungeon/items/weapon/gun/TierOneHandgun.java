package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class TierOneHandgun extends FirearmWeapon {

    {
        defaultAction = AC_SHOOT;
        usesTargeting = true;

        image = ItemSpriteSheet.TIER_ONE_HANDGUN;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.8f;

        tier = 1;
        type = FirearmType.FirearmHandgun;
        max_round = 3;
        shot = 1;

        bullet_image = ItemSpriteSheet.SINGLE_BULLET;
    }

    @Override
    public void setMaxRound() {
        max_round = 3;// + 3 * Dungeon.hero.pointsInTalent(Talent.DEATH_MACHINE) + 3 * Dungeon.hero.pointsInTalent(Talent.QUANTITY_OVER_QUALITY);
    }

}
