/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FirearmWeapon extends MeleeWeapon {

    public enum FirearmType {
        FirearmRifle,
        FirearmHandgun,
        FirearmSniper,
        FirearmAuto,
        FirearmShotgun,
        FirearmExplosive,
        FirearmLaser,
        FirearmHiLaser,
        FirearmEtc1,
        FirearmFlame;
    }

    public FirearmType type;

    public static final String AC_SHOOT		= "SHOOT";
    public static final String AC_RELOAD    = "RELOAD";

    public int max_round;
    public int round = 0;
    public int shot = 1;
    public float reload_time;
    public int bullet_image = ItemSpriteSheet.SHURIKEN;//ItemSpriteSheet.SINGLE_BULLET;
    public String bullet_sound = Assets.Sounds.PUFF;

    public int max_dist = 5;
    public int degree = 30;

    private static final String TXT_STATUS      = "%d/%d";
    private static final String ROUND           = "round";
    private static final String MAX_ROUND       = "max_round";
    private static final String RELOAD_TIME     = "reload_time";

    public int tier;

    //setting the emition of flamethrowers
    public void setEmitting() {
        max_dist = 5;
        degree = 30;
    }

    public void setMaxRound() {}

    public void setReloadTime() {
        float reloadMulti = 1f;
        switch (type) {
            case FirearmShotgun:
                reload_time = 1f * reloadMulti;
                break;
            case FirearmRifle:
            case FirearmHandgun:
            case FirearmSniper:
            case FirearmAuto:
            case FirearmExplosive:
            case FirearmHiLaser:
            case FirearmLaser:
            case FirearmEtc1:
            case FirearmFlame:
            default:
                reload_time = 2f * reloadMulti;
                break;
        }
    }

    @Override
    public int min(int lvl) {
        return  tier + //base
                lvl;    //level scaling
    }

    @Override
    public int max(int lvl) {
        return  3*(tier+1) +    //base
                lvl*(tier+1);   //level scaling
    }

    public int Bulletmin(int lvl) {

        double dmgReduce = 1;

        switch (type) {
            case FirearmSniper:
                return (int) ((3 * tier + lvl + RingOfSharpshooting.levelDamageBonus(Dungeon.hero))) ;
            case FirearmExplosive:
                return (tier + 4) + lvl + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) ;
            case FirearmHiLaser:
            case FirearmLaser:
                return tier + lvl ;
            case FirearmShotgun:
                return (tier - 1) + lvl + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) ;
            case FirearmAuto:
                return (int) ((tier + lvl + RingOfSharpshooting.levelDamageBonus(Dungeon.hero)) * dmgReduce) ;
            case FirearmRifle:
            case FirearmHandgun:
            case FirearmEtc1:
            case FirearmFlame:
            default:
                return tier + lvl + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) ;
        }
    }

    public int Bulletmax(int lvl) {

        double dmgReduce = 1;

        switch (type) {
            case FirearmSniper:
                return (int)((4 * (tier+2) + lvl * (tier+2) + RingOfSharpshooting.levelDamageBonus(Dungeon.hero))) ;
            case FirearmAuto:
                return (int)((2*tier-1) + (lvl*tier) + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) * dmgReduce) ;
            case FirearmShotgun:
                return (tier*2-1) + lvl + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) ;
            case FirearmExplosive:
                return 3*(tier+4) + lvl*(tier+3) + RingOfSharpshooting.levelDamageBonus(hero) ;
            case FirearmHiLaser:
                return Math.round((4 * (tier + 1) + (lvl*4) + RingOfSharpshooting.levelDamageBonus(Dungeon.hero))) ;
            case FirearmLaser:
                return Math.round((3 * (tier + 1) + (lvl*2) + RingOfSharpshooting.levelDamageBonus(Dungeon.hero))) ;
            case FirearmEtc1:
            case FirearmFlame:
                return (5*tier) + (lvl*3) + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) ;
            case FirearmRifle:
            case FirearmHandgun:
            default:
                return (5*tier-1) + (lvl*tier) + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) ;
        }
    }

    public int STRReq(int lvl){
        return STRReq(tier, lvl) + 1;
    }

    @Override
    public String defaultAction() {
        return AC_SHOOT;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MAX_ROUND, max_round);
        bundle.put(ROUND, round);
        bundle.put(RELOAD_TIME, reload_time);
        //GLog.p("Firearm store "+this.getClass().getSimpleName()+"\n");
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        max_round = bundle.getInt(MAX_ROUND);
        round = bundle.getInt(ROUND);
        reload_time = bundle.getFloat(RELOAD_TIME);
        //GLog.p("Firearm restore "+this.getClass().getSimpleName()+"\n");
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped( hero )) {
            actions.add(AC_SHOOT);
            actions.add(AC_RELOAD);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);

        if (action.equals(AC_SHOOT)) {

            if (!isEquipped( hero )) {
                usesTargeting = false;
                GLog.w(Messages.get(this, "not_equipped"));
            } else {
                setReloadTime();
                setMaxRound();
                switch (type) {
                    //flamethrowers can hit anyway, so they have strength restrictions
                    case FirearmFlame:
                        if (hero.STR() < STRReq()) {
                            usesTargeting = false;
                            GLog.w(Messages.get(this, "heavy_to_shoot"));
                        } else {
                            if (round <= 0) {
                                reload();
                            } else {
                                usesTargeting = true;
                                curUser = hero;
                                curItem = this;
                                GameScene.selectCell(shooter);
                            }
                        }
                        break;
                    case FirearmRifle:
                    case FirearmHandgun:
                    case FirearmSniper:
                    case FirearmAuto:
                    case FirearmShotgun:
                    case FirearmExplosive:
                    case FirearmHiLaser:
                    case FirearmLaser:
                    case FirearmEtc1:
                    default:
                        if (round <= 0) {
                            reload();
                        } else {
                            usesTargeting = true;
                            curUser = hero;
                            curItem = this;
                            GameScene.selectCell(shooter);
                        }
                        break;
                }
            }
        }
        if (action.equals(AC_RELOAD)) {

            if (round == max_round) {
                GLog.w(Messages.get(this, "already_loaded"));
            } else {
                switch (type) {
                    case FirearmRifle:
                    case FirearmHandgun:
                    case FirearmSniper:
                    case FirearmAuto:
                    case FirearmShotgun:
                    case FirearmExplosive:
                    case FirearmHiLaser:
                    case FirearmLaser:
                    case FirearmEtc1:
                    case FirearmFlame:
                    default:
                        reload();
                        break;
                }
            }
        }
    }

    public void reload() {
        curUser.spend(reload_time);
        curUser.busy();
        Sample.INSTANCE.play(Assets.Sounds.UNLOCK, 2, 1.1f);
        curUser.sprite.operate(curUser.pos);
        round = Math.max(max_round, round);

        GLog.i(Messages.get(this, "reloading"));

        updateQuickslot();
    }

    public int getRound() { return this.round; }

    @Override
    public String status() {
        return Messages.format(TXT_STATUS, round, max_round);
    }

    @Override
    public Item upgrade() {
        return upgrade(false);
    }

    @Override
    public Item upgrade(boolean enchant) {
        return super.upgrade(enchant);
    }

    @Override
    public int damageRoll(Char owner) {
        int damage = augment.damageFactor(super.damageRoll(owner));

        if (owner instanceof Hero) {
            int exStr = ((Hero)owner).STR() - STRReq();
            if (exStr > 0) {
                damage += Random.IntRange( 0, exStr );
            }
        }

        return damage;
    }

    @Override
    public String info() {
        setReloadTime();
        setMaxRound();
        String info = desc();
        int loadtime = (int)(100);

        if (levelKnown) {
            info += "\n\n" + Messages.get(MeleeWeapon.class, "stats_known", tier, augment.damageFactor(min()), augment.damageFactor(max()), STRReq());
            if (STRReq() > Dungeon.hero.STR()) {
                info += " " + Messages.get(Weapon.class, "too_heavy");
            } else if (Dungeon.hero.STR() > STRReq()){
                info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq());
            }
            info += "\n\n" + Messages.get(FirearmWeapon.class, "stats_known",
                    Bulletmin(FirearmWeapon.this.buffedLvl()),
                    Bulletmax(FirearmWeapon.this.buffedLvl()),
                    round, max_round,
                    new DecimalFormat("#.##").format(reload_time));
        } else {
            info += "\n\n" + Messages.get(MeleeWeapon.class, "stats_unknown", tier, min(0), max(0), STRReq(0));
            if (STRReq(0) > Dungeon.hero.STR()) {
                info += " " + Messages.get(MeleeWeapon.class, "probably_too_heavy");
            }
            info += "\n\n" + Messages.get(FirearmWeapon.class, "stats_unknown",
                    Bulletmin(0),
                    Bulletmax(0),
                    round, max_round, new DecimalFormat("#.##").format(reload_time));
        }

        String statsInfo = statsInfo();
        if (!statsInfo.equals("")) info += "\n\n" + statsInfo;

        switch (augment) {
            case SPEED:
                info += " " + Messages.get(Weapon.class, "faster");
                break;
            case DAMAGE:
                info += " " + Messages.get(Weapon.class, "stronger");
                break;
            case NONE:
        }

        if (enchantment != null && (cursedKnown || !enchantment.curse())){
            info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment.name());
            info += " " + Messages.get(enchantment, "desc");
        }

        if (cursed && isEquipped( Dungeon.hero )) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
        } else if (cursedKnown && cursed) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed");
        } else if (!isIdentified() && cursedKnown){
            info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
        }

        if (Dungeon.hero.heroClass == HeroClass.DUELIST){
            info += "\n\n" + Messages.get(this, "ability_desc");
        }

        return info;
    }

    public String statsInfo(){
        return Messages.get(this, "stats_desc");
    }

    @Override
    public Emitter emitter() {
        Emitter emitter = new Emitter();
        emitter.pos(ItemSpriteSheet.film.width(image)/2f + 2f, ItemSpriteSheet.film.height(image)/3f);
        emitter.fillTarget = false;
        emitter.pour(Speck.factory( Speck.RED_LIGHT ), 0.6f);
        return emitter;
    }

    @Override
    protected float baseDelay(Char owner) {
        float delay = augment.delayFactor(this.DLY);
        if (owner instanceof Hero) {
            int encumbrance = STRReq() - ((Hero)owner).STR();
            if (encumbrance > 0){
                delay *= Math.pow( 1.2, encumbrance );
            }
        }

        return delay;
    }

    public FirearmWeapon.Bullet knockBullet(){
        return new FirearmWeapon.Bullet();
    }

    public float accuracyFactorBullet(Char owner, Char target) {
        float acc = 1f;

        /*
        if (Dungeon.level.adjacent( owner.pos, target.pos ) && Dungeon.hero.subClass == HeroSubClass.SPECOPS) {
            acc += 0.4f;
        }

        if (Dungeon.level.adjacent( owner.pos, target.pos ) && Dungeon.hero.hasTalent(Talent.PIN_POINT_ATTACK)) {
            acc += 0.2f * Dungeon.hero.pointsInTalent(Talent.PIN_POINT_ATTACK);
        }
        */

        return acc;
    }

    //explosion at 3x3 grid
    public void onThrowBulletFirearmExplosive( int cell ) {
        CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
        CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
        ArrayList<Char> affected = new ArrayList<>();
        for (int n : PathFinder.NEIGHBOURS9) {
            int c = cell + n;
            if (c >= 0 && c < Dungeon.level.length()) {
                if (Dungeon.level.heroFOV[c]) {
                    CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
                    CellEmitter.center(cell).burst(BlastParticle.FACTORY, 4);
                }
                if (Dungeon.level.flamable[c]) {
                    Dungeon.level.destroy(c);
                    GameScene.updateMap(c);
                }
                Char ch = Actor.findChar(c);
                if (ch != null) {
                    affected.add(ch);
                }
            }
        }
        Sample.INSTANCE.play( Assets.Sounds.BLAST );
    }


    //fires invisible bullet and draws laser effect
    public void onThrowBulletFirearmLaser( int cell ) {
        Ballistica beam = new Ballistica(hero.pos, cell, Ballistica.PROJECTILE);
        for (int c : beam.subPath(0, beam.dist))
            CellEmitter.center(c).burst( PurpleParticle.BURST, 1 );

        curUser.sprite.parent.add(
                new Beam.DeathRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(beam.collisionPos)));

        for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
            if (mob.paralysed <= 0
                    && Dungeon.level.distance(curUser.pos, mob.pos) <= 4
                    && mob.state != mob.HUNTING) {
                mob.beckon( curUser.pos );
            }
        }

        Invisibility.dispel();
        updateQuickslot();
    }

    //fires invisible bullet and and draws flames
    public void onThrowBulletFirearmFlame( int cell ) {
        Ballistica aim = new Ballistica(hero.pos, cell, Ballistica.WONT_STOP); //Always Projecting and no distance limit, see MissileWeapon.throwPos
        setEmitting();
        int dist = Math.min(aim.dist, max_dist);
        ConeAOE cone = new ConeAOE(aim,
                dist,
                degree,
                Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);
        //cast to cells at the tip, rather than all cells, better performance.
        for (Ballistica ray : cone.outerRays){
            ((MagicMissile)hero.sprite.parent.recycle( MagicMissile.class )).reset(
                    MagicMissile.FIRE_CONE,
                    hero.sprite,
                    ray.path.get(ray.dist),
                    null
            );
        }
        for (int cells : cone.cells){
            //knock doors open
            if (Dungeon.level.map[cells] == Terrain.DOOR){
                Level.set(cells, Terrain.OPEN_DOOR);
                GameScene.updateMap(cells);
            }

            //only ignite cells directly near caster if they are flammable
            if (!(Dungeon.level.adjacent(hero.pos, cells) && !Dungeon.level.flamable[cells])) {
                GameScene.add(Blob.seed(cells, 2, Fire.class));
            }

            Char ch = Actor.findChar(cells);
            if (ch != null && ch.alignment != hero.alignment){
                int damage = damageRoll(hero);
                damage -= ch.drRoll();
                ch.damage(damage, hero);
            }
        }
        Sample.INSTANCE.play(Assets.Sounds.BURNING, 1f);
        //final zap at 2/3 distance, for timing of the actual effect
        MagicMissile.boltFromChar(hero.sprite.parent,
                MagicMissile.FIRE_CONE,
                hero.sprite,
                cone.coreRay.path.get(dist * 2 / 3),
                new Callback() {
                    @Override
                    public void call() {
                    }
                });
        Invisibility.dispel();
        updateQuickslot();
    }

    public void cooldown() {}

    public class Bullet extends MissileWeapon {

        {
            image = bullet_image;
            hitSound = bullet_sound;

            //bullet = true;
        }

        @Override
        public int buffedLvl() {
            return FirearmWeapon.this.buffedLvl();
        }

        @Override
        public int damageRoll(Char owner) {
            //Hero hero = (Hero)owner;
            Char enemy = hero.enemy();
            int bulletdamage = Random.NormalIntRange(Bulletmin(FirearmWeapon.this.buffedLvl()),
                    Bulletmax(FirearmWeapon.this.buffedLvl()));

            if (owner.buff(Momentum.class) != null && owner.buff(Momentum.class).freerunning()) {
                bulletdamage = Math.round(bulletdamage * (1f + 0.15f * ((Hero) owner).pointsInTalent(Talent.PROJECTILE_MOMENTUM)));
            }

            switch (type) {
                case FirearmHiLaser:
                case FirearmLaser:
                    if (hero.buff(Recharging.class) != null) {
                        bulletdamage *= 1.2f;
                    }
                    if (hero.buff(ArtifactRecharge.class) != null) {
                        bulletdamage *= 1.2f;
                    }
                    if (enemy instanceof Eye) {
                        bulletdamage *= 0.75f;
                    }
                    break;
                case FirearmRifle:
                case FirearmHandgun:
                case FirearmSniper:
                case FirearmAuto:
                case FirearmShotgun:
                case FirearmExplosive:
                case FirearmEtc1:
                case FirearmFlame:
                default:
                    break;
            }

            return bulletdamage;
        }

        @Override
        public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
            return FirearmWeapon.this.hasEnchant(type, owner);
        }

        @Override
        public int proc(Char attacker, Char defender, int damage) {
            SpiritBow bow = hero.belongings.getItem(SpiritBow.class);
            if (FirearmWeapon.this.enchantment == null
                    && Random.Int(3) < hero.pointsInTalent(Talent.SHARED_ENCHANTMENT)
                    && hero.buff(MagicImmune.class) == null
                    && bow != null
                    && bow.enchantment != null) {
                return bow.enchantment.proc(this, attacker, defender, damage);
            } else {
                return FirearmWeapon.this.proc(attacker, defender, damage);
            }
        }

        @Override
        public float delayFactor(Char user) {
            return FirearmWeapon.this.delayFactor(user);
        }

        @Override
        public float accuracyFactor(Char owner, Char target) {
            return accuracyFactorBullet(owner, target);
        }

        @Override
        public int STRReq(int lvl) {
            return FirearmWeapon.this.STRReq();
        }

        @Override
        protected void onThrow( int cell ) {
            ArrayList<Char> targets;
            Char findChar = Actor.findChar(cell);
            switch (type) {
                case FirearmExplosive:
                    for (int j = 0; j < shot; j++) {
                        targets = new ArrayList<>();
                        if (Actor.findChar(cell) != null) targets.add(Actor.findChar(cell));
                        for (int i : PathFinder.NEIGHBOURS8) {
                            if (Actor.findChar(cell + i) != null)
                                targets.add(Actor.findChar(cell + i));
                        }
                        for (Char target : targets) {
                            curUser.shoot(target, this);
                            if (target == hero) {
                                /*
                                if (hero.hasTalent(Talent.SHAPED_WARHEAD)) {
                                    int damage = damageRoll(hero);
                                    damage -= target.drRoll();
                                    damage *= (1 - 0.3 * hero.pointsInTalent(Talent.SHAPED_WARHEAD));
                                    target.damage(damage, hero);
                                }
                                */
                                if (!target.isAlive()) {
                                    Dungeon.fail(getClass());
                                    GLog.n(Messages.get(FirearmWeapon.class, "ondeath"));
                                }
                            }
                        }
                        onThrowBulletFirearmExplosive(cell);
                        /*
                        if (hero.hasTalent(Talent.DEATH_MACHINE) && Random.Int(10) <= hero.pointsInTalent(Talent.DEATH_MACHINE)) {
                            //round preserves
                        } else if (hero.buff(Bunker.class) != null && Random.Int(10) <= 1 + hero.pointsInTalent(Talent.SHRIKE_TURRET)) {

                        } else if (hero.buff(InfiniteBullet.class) != null) {

                        } else {
                            round--;
                        }
                        */
                        round--;
                    }
                    cooldown();
                    break;
                case FirearmLaser:
                    for (int i = 0; i < shot; i++) {
                        targets = new ArrayList<>();
                        if (Actor.findChar(cell) != null) targets.add(Actor.findChar(cell));
                        for (Char target : targets) {
                            curUser.shoot(target, this);
                        }
                        onThrowBulletFirearmLaser(cell);
                        /*
                        if (hero.hasTalent(Talent.ONE_MORE_ROUND) && Random.Int(10) < hero.pointsInTalent(Talent.ONE_MORE_ROUND)) {
                            //round preserves
                        } else if (hero.buff(Bunker.class) != null && Random.Int(10) <= 1 + hero.pointsInTalent(Talent.SHRIKE_TURRET)) {

                        } else if (hero.buff(InfiniteBullet.class) != null) {

                        } else {
                            round--;
                        }
                        */
                        round--;
                    }
                    cooldown();
                    break;
                case FirearmFlame:
                    for (int i = 0; i < shot; i++) {
                        targets = new ArrayList<>();
                        if (Actor.findChar(cell) != null) targets.add(Actor.findChar(cell));
                        for (Char target : targets) {
                            curUser.shoot(target, this);
                        }
                        onThrowBulletFirearmFlame(cell);
                        /*
                        if (hero.buff(Bunker.class) != null && Random.Int(10) <= 1 + hero.pointsInTalent(Talent.SHRIKE_TURRET)) {

                        } else if (hero.buff(InfiniteBullet.class) != null) {

                        } else {
                            round--;
                        }
                        */
                        round--;
                    }
                    cooldown();
                    break;
                case FirearmHiLaser:
                    for (int i = 0; i < shot; i++) {
                        if (round <= 0) break;
                        /*
                        if (hero.hasTalent(Talent.DEATH_MACHINE) && Random.Int(10) <= hero.pointsInTalent(Talent.DEATH_MACHINE)) {
                            //round preserves
                        } else if (hero.buff(Bunker.class) != null && Random.Int(10) <= 1 + hero.pointsInTalent(Talent.SHRIKE_TURRET)) {

                        } else if (hero.buff(InfiniteBullet.class) != null) {

                        } else {
                            round --;
                        }
                        */
                        round --;

                        Char enemy = Actor.findChar(cell);
                        if (enemy == null || enemy == curUser) {
                            parent = null;
                            CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                            CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                        } /*else {
                            if (hero.hasTalent(Talent.GRID_EXPOSURE)) {
                                int dur = 1;
                                if (hero.pointsInTalent(Talent.GRID_EXPOSURE) >= 3) {
                                    dur = 2;
                                }
                                if (hero.pointsInTalent(Talent.GRID_EXPOSURE) >= 1) {
                                    Buff.affect(hero, Recharging.class, (float)dur);
                                    if (hero.pointsInTalent(Talent.GRID_EXPOSURE) >= 2) {
                                        Buff.affect( hero, ArtifactRecharge.class).set(dur);
                                    }
                                }
                            }
                            if (hero.hasTalent(Talent.MALICIOUS_FUEL) && Random.Int(5) < hero.pointsInTalent(Talent.MALICIOUS_FUEL)) {
                                ArrayList<Lightning.Arc> arcs = new ArrayList<>();
                                ArrayList<Char> affected = new ArrayList<>();
                                affected.clear();
                                arcs.clear();

                                Shocking.arc(hero, enemy, 2, affected, arcs);

                                affected.remove(enemy); //defender isn't hurt by lightning
                                for (Char ch : affected) {
                                    if (ch.alignment != hero.alignment) {
                                        ch.damage(Math.round(damageRoll(hero)*0.2f), this);
                                    }
                                }

                                hero.sprite.parent.addToFront( new Lightning( arcs, null ) );
                                Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
                            }
                            if (!curUser.shoot(enemy, this)) {
                                CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                                CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                            }
                        }*/
                    }
                    cooldown();
                    break;
                case FirearmRifle:
                case FirearmHandgun:
                case FirearmAuto:
                case FirearmEtc1:
                    //fires designated rounds per shot
                    for (int i = 0; i < shot; i++) {
                        if (round <= 0) break;
                        /*
                        if (hero.hasTalent(Talent.ONE_MORE_ROUND) && Random.Int(10) < hero.pointsInTalent(Talent.ONE_MORE_ROUND)) {
                            //round preserves
                        } else if (hero.buff(Bunker.class) != null && Random.Int(10) <= 1 + hero.pointsInTalent(Talent.SHRIKE_TURRET)) {

                        } else if (hero.buff(InfiniteBullet.class) != null) {

                        } else {
                            round--;
                        }
                        */
                        round--;

                        Char enemy = Actor.findChar(cell);
                        if (enemy == null || enemy == curUser) {
                            parent = null;
                            CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                            CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                        } else {
                            if (!curUser.shoot(enemy, this)) {
                                CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                                CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                            }
                        }
                    }
                    cooldown();
                    break;
                case FirearmSniper:
                case FirearmShotgun:
                default:
                    //fires all designated rounds at once
                    for (int i = 0; i < shot; i++) {
                        if (round <= 0) break;
                        /*
                        if (hero.buff(Bunker.class) != null && Random.Int(10) <= 1 + hero.pointsInTalent(Talent.SHRIKE_TURRET)) {

                        } else if (hero.buff(InfiniteBullet.class) != null) {

                        } else {
                            round--;
                        }
                        */
                        round--;

                        Char enemy = Actor.findChar(cell);
                        if (enemy == null || enemy == curUser) {
                            parent = null;
                            CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                            CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                        } else {
                            if (!curUser.shoot(enemy, this)) {
                                CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                                CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                            }
                        }
                    }
                    cooldown();
                    break;
            }

            /*
            if (Dungeon.hero.hasTalent(Talent.SUPPRESSION_FIRE) &&
                    Random.Int(10) < Dungeon.hero.pointsInTalent(Talent.SUPPRESSION_FIRE) &&
                    !Dungeon.level.flamable[cell] && findChar != null) {
                Buff.affect(findChar, Roots.class, 1f);
            }

            if (Dungeon.hero.hasTalent(Talent.URBAN_WARFARE) &&
                    Random.Int(5) < Dungeon.hero.pointsInTalent(Talent.URBAN_WARFARE) &&
                    !Dungeon.level.flamable[cell] && findChar != null) {
                Buff.affect(findChar, Blindness.class, 1f);
                Buff.affect(findChar, Cripple.class, 1f);
            }
            */

            switch (type) {
                case FirearmSniper:
                    /*
                    if (hero.hasTalent(Talent.IN_THE_DARKNESS) && Random.Int(5) < hero.pointsInTalent(Talent.IN_THE_DARKNESS)) {
                        GameScene.updateMap(hero.pos);
                    } else {
                        for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                            if (mob.paralysed <= 0
                                    && Dungeon.level.distance(curUser.pos, mob.pos) <= 4
                                    && mob.state != mob.HUNTING) {
                                mob.beckon( curUser.pos );
                            }
                        }
                    }
                    break;
                    */
                case FirearmRifle:
                case FirearmHandgun:
                case FirearmAuto:
                case FirearmShotgun:
                case FirearmExplosive:
                case FirearmHiLaser:
                case FirearmLaser:
                case FirearmEtc1:
                case FirearmFlame:
                default:
                    for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                        if (mob.paralysed <= 0
                                && Dungeon.level.distance(curUser.pos, mob.pos) <= 4
                                && mob.state != mob.HUNTING) {
                            mob.beckon( curUser.pos );
                        }
                    }
                    break;
            }
            updateQuickslot();
        }

        @Override
        public void throwSound() {
            switch (type) {
                case FirearmExplosive:
                    Sample.INSTANCE.play( Assets.Sounds.PUFF, 1, Random.Float(0.33f, 0.66f) );
                    break;
                case FirearmHiLaser:
                case FirearmLaser:
                    Sample.INSTANCE.play( Assets.Sounds.LIGHTNING, 1, Random.Float(0.33f, 0.66f) );
                    break;
                case FirearmFlame:
                    Sample.INSTANCE.play(Assets.Sounds.BURNING, 1f);
                    break;
                case FirearmRifle:
                case FirearmHandgun:
                case FirearmSniper:
                case FirearmAuto:
                case FirearmShotgun:
                case FirearmEtc1:
                default:
                    Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 1, Random.Float(0.33f, 0.66f) );
                    break;
            }
        }

        @Override
        public void cast(final Hero user, final int dst) {
            super.cast(user, dst);

            /*
            if (user.hasTalent(Talent.DISCHARGE_SHOT)
                    && user.buff(Talent.DischargeCooldown.class) == null) {
                int throwPos = throwPos(user, dst);
                if (Actor.findChar(throwPos) == null) {
                    for (Mob mob : (Mob[]) Dungeon.level.mobs.toArray(new Mob[0])) {
                        if (Dungeon.level.adjacent(mob.pos, throwPos) && mob.alignment != Char.Alignment.ALLY) {
                            Buff.affect(mob, NoEnergy.class).set(2, 2);
                            CellEmitter.center(mob.pos).burst(BlastParticle.FACTORY, 10);
                        }
                    }
                    Buff.affect(user, Talent.DischargeCooldown.class, (float)(80 - (user.pointsInTalent(Talent.DISCHARGE_SHOT)*20)));
                }
            }
            */
        }
    }

    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null) {
                if (target == curUser.pos) {
                    reload();
                } else {
                    knockBullet().cast(curUser, target);
                }
            }
        }
        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };

    @Override
    public int value() {
        int price = 20 * tier;
        if (hasGoodEnchant()) {
            price *= 1.5;
        }
        if (cursedKnown && (cursed || hasCurseEnchant())) {
            price /= 2;
        }
        if (levelKnown && level() > 0) {
            price *= (level() + 1);
        }
        if (price < 1) {
            price = 1;
        }
        return price;
    }

}
