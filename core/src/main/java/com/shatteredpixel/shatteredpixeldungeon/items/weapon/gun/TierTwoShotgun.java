package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class TierTwoShotgun extends FirearmWeapon {

    {
        defaultAction = AC_SHOOT;
        usesTargeting = true;

        image = ItemSpriteSheet.TIER_TWO_SHOTGUN;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.8f;

        tier = 2;
        type = FirearmType.FirearmShotgun;
        max_round = 5;
        shot = 5;

        bullet_image = ItemSpriteSheet.TRIPLE_BULLET;
    }

    @Override
    public void setMaxRound() {
        max_round = 5;// + 3 * Dungeon.hero.pointsInTalent(Talent.DEATH_MACHINE) + 3 * Dungeon.hero.pointsInTalent(Talent.QUANTITY_OVER_QUALITY);
    }

}
