package com.infernostats.tickcounter;

public class TickCounterUtils {
    public static int getTicksForAnimation(int animation, int weapon) {
        int delta;
        switch (animation)
        {
            case 7617: // rune knife
            case 8194: // dragon knife
            case 8291: // dragon knife spec
            case 5061: // blowpipe
                delta = 2;
                break;
            case 2323: // rpg
            case 7618: // chin
                delta = 3;
                break;
            case 426: // bow shoot
                if (weapon == 20997) // twisted bow
                    delta = 5;
                else // shortbow
                    delta = 3;
                break;
            case 376: // dds poke
            case 377: // dds slash
            case 422: // punch
            case 423: // kick
            case 386: // lunge
            case 390: // generic slash
                if (weapon == 24219) // swift blade
                {
                    delta = 3;
                    break;
                }
            case 1062: // dds spec
            case 1067: // claw stab
            case 1074: // msb spec
            case 1167: // trident cast
            case 1658: // whip
            case 2890: // arclight spec
            case 3294: // abby dagger slash
            case 3297: // abby dagger poke
            case 3298: // bludgeon attack
            case 3299: // bludgeon spec
            case 3300: // abby dagger spec
            case 7514: // claw spec
            case 7515: // d sword spec
            case 8145: // rapier stab
            case 8288: // dhl stab
                if (weapon == 24219) // swift blade
                {
                    delta = 3;
                    break;
                }
            case 8289: // dhl slash
            case 8290: // dhl crush
            case 4503: // inquisitor's mace crush
            case 1711: // zamorakian spear
                delta = 4;
                break;
            case 393: // staff bash
                if (weapon == 13652)
                { // claw scratch
                    delta = 4;
                    break;
                }
            case 395: // axe autos
            case 400: // pick smash
                if (weapon == 24417)
                {
                    // inquisitor's mace stab
                    delta = 4;
                    break;
                }
            case 1379: // burst or blitz
            case 1162: // strike/bolt spells
            case 7855: // surge spells
                if (weapon == 24423) // harmonised staff
                {
                    delta = 4;
                    break;
                }
            case 7552: // generic crossbow
            case 1979: // barrage spell cast
            case 8056: // scythe swing
                delta = 5;
                break;
            case 401:
                if (weapon == 13576) // dwh bop
                    delta = 6;
                else if (weapon == 23360) // ham joint
                    delta = 3;
                else // used by pickaxe and axe
                    delta = 5;
                break;
            case 1378:
            case 7045:
            case 7054:
            case 7055: // godsword autos
            case 7511: // dinh's attack
            case 7516: // maul attack
            case 7555: // ballista attack
            case 7638: // zgs spec
            case 7640: // sgs spec
            case 7642: // bgs spec
            case 7643: // bgs spec
            case 7644: // ags spec
                delta = 6;
                break;
            case 428: // chally swipe
            case 440: // chally jab
            case 1203: // chally spec
                delta = 7;
                break;
            default:
                delta = 0;
        }
        return delta;
    }
}
