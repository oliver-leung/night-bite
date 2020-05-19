package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import edu.cornell.gdiac.nightbite.entity.*;
import edu.cornell.gdiac.util.SoundController;


public class CollisionController implements ContactListener {
    /** Impulse for players pushing */
    protected static final float PUSH_IMPULSE = 200f;

    public static final int ITEMS_TO_WIN = 3;
    private final String FX_FALL_FILE = "audio/whistle.wav";

    private WorldModel worldModel;

    public CollisionController(WorldModel worldModel) {
        this.worldModel = worldModel;
    }

    /**
     * Called when two fixtures begin to touch
     */
    public void beginContact(Contact contact) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();

        // Player-Thief Contact
        if (a instanceof PlayerModel && b instanceof ThiefEnemyModel) {
            handlePlayerToThiefContact((PlayerModel) a, (ThiefEnemyModel) b);
        } else if (b instanceof PlayerModel && a instanceof ThiefEnemyModel) {
            handlePlayerToThiefContact((PlayerModel) b, (ThiefEnemyModel) a);
        }

        // Player-Object Contact
        if (a instanceof PlayerModel) {
            handlePlayerToObjectContact((PlayerModel) a, b);
        } else if (b instanceof PlayerModel) {
            handlePlayerToObjectContact((PlayerModel) b, a);
        }

        // Enemy-Object Contact
        if (a instanceof EnemyModel) { // || a instanceof CrowdUnitModel
            handleEnemyToObjectContact((HumanoidModel) a, b);
        } else if (b instanceof EnemyModel) { // || b instanceof CrowdUnitModel
            handleEnemyToObjectContact((HumanoidModel) b, a);
        }

        // Item-Object Contact
        if (a instanceof ItemModel) {
            handleItemToObjectContact((ItemModel) a, b);
        } else if (b instanceof ItemModel) {
            handleItemToObjectContact((ItemModel) b, a);
        }

        // Firecracker-Object Contact
        if (a instanceof FirecrackerModel) {
            handleFirecrackerToObjectContact((FirecrackerModel) a, b);
        } else if (b instanceof FirecrackerModel) {
            handleFirecrackerToObjectContact((FirecrackerModel) b, a);
        }
    }

    /**
     * Called when two fixtures cease to touch
     */
    public void endContact(Contact contact) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();
        if ((a instanceof PlayerModel || a instanceof ThiefEnemyModel) && b instanceof ItemModel) {
            int itemId = ((ItemModel) b).getId();
            worldModel.setOverlapItem(itemId, false);
        } else if ((b instanceof PlayerModel || b instanceof ThiefEnemyModel) && a instanceof ItemModel) {
            int itemId = ((ItemModel) a).getId();
            worldModel.setOverlapItem(itemId, false);
        } else if (a instanceof HoleModel && b instanceof FirecrackerModel) {
            ((FirecrackerModel) b).removeContactHole((HoleModel) a);
        } else if (b instanceof HoleModel && a instanceof FirecrackerModel) {
            ((FirecrackerModel) a).removeContactHole((HoleModel) b);
        }
    }

    /**
     * Called after a contact is updated
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();

        // Humanoids walk through items and firecrackers
        if ((a instanceof HumanoidModel && b instanceof ItemModel) || (b instanceof HumanoidModel && a instanceof ItemModel)) {
            contact.setEnabled(false);
        } else if ((a instanceof ItemModel && ((ItemModel) a).holdingPlayer != null) || (b instanceof ItemModel && ((ItemModel) b).holdingPlayer != null)) {
            contact.setEnabled(false);
        } else if ((a instanceof HumanoidModel && b instanceof FirecrackerModel) || (b instanceof HumanoidModel && a instanceof FirecrackerModel)) {
            contact.setEnabled(false);
        }
    }

    /**
     * Inspect a contact after solver is finished
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();

        // Player-Player Contact
        if (a instanceof PlayerModel && b instanceof PlayerModel) {
            PlayerModel playerA = (PlayerModel) a;
            PlayerModel playerB = (PlayerModel) b;

            Vector2 flyDirection;
            if (playerA.state == PlayerModel.MoveState.RUN && playerB.state != PlayerModel.MoveState.RUN) {
                flyDirection = playerB.getLinearVelocity().nor();
                playerA.resetBoosting();
                playerB.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerB.getPosition(), true);
            } else if (playerB.state == PlayerModel.MoveState.RUN && playerA.state != PlayerModel.MoveState.RUN) {
                flyDirection = playerA.getLinearVelocity().nor();
                playerA.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerA.getPosition(), true);
                playerB.resetBoosting();
            } else if (playerB.state == PlayerModel.MoveState.RUN && playerA.state == PlayerModel.MoveState.RUN) {
                flyDirection = playerA.getLinearVelocity().nor();
                playerA.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerA.getPosition(), true);
                flyDirection = playerB.getLinearVelocity().nor();
                playerA.resetBoosting();
                playerB.resetBoosting();
                playerB.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerB.getPosition(), true);
            }
        }
    }

    public void handlePlayerToThiefContact(PlayerModel player, ThiefEnemyModel thief) {
        if (thief.contactCooldown <= 0) {
            if (player.hasItem()) {
                // Thief takes the item
                for (ItemModel item_obj : player.getItems()) {
                    item_obj.setHeld(thief);
                }
                player.clearInventory();
            } else if (thief.hasItem()) {
                // Player takes the item
                for (ItemModel item_obj : thief.getItems()) {
                    item_obj.setHeld(player);
                }
                thief.clearInventory();

                thief.resetThief(); // Reset attack status
            }
            thief.resetContactcooldown();

        }
    }

    public void handlePlayerToObjectContact(PlayerModel player, Object object) {
        if (object instanceof HoleModel) {

            // Player-Hole collision
            player.setDead();

            if (player.hasItem()) { // TODO fix jank implementation
                for (ItemModel item_obj : player.getItems()) {
                    item_obj.startRespawn();
                }
                player.clearInventory();
            }

            SoundController.getInstance().play(FX_FALL_FILE, FX_FALL_FILE, false, Assets.VOLUME);

        } else if (object instanceof ItemModel) {

            // Player-Item
            if (! ((ItemModel) object).isDead()) {
                int id = ((ItemModel) object).getId();
                worldModel.setOverlapItem(id, true);
            }
        } else if (object instanceof HomeModel) {

            // Player-Home
            HomeModel homeObject = (HomeModel) object;
            // If players went to their own home, drop off item and increment score
            if (player.getTeam().equals(homeObject.getTeam()) && player.hasItem()) {

                homeObject.incrementScore(1);

                for (ItemModel item_obj : player.getItems()) {
                    item_obj.startRespawn();
                }
                player.clearInventory();
                player.resetTexture();

                // win condition
                checkWinCondition(homeObject);
            }
        } else if (object instanceof OilModel) {
            // Player slides only when oil is completely spilled
            if (((OilModel) object).isSpilled()) {
                player.setSlide();
                ((OilModel) object).markRemoved(true);
            }
        }
    }

    public void handleEnemyToObjectContact(HumanoidModel enemy, Object object) {
        if (object instanceof HoleModel) {
            // Enemy-Hole collision
            enemy.setDead();
            if (enemy.hasItem()) {
                for (ItemModel item_obj : enemy.getItems()) {
                    item_obj.startRespawn();
                }
                enemy.clearInventory();
            }
            SoundController.getInstance().play(FX_FALL_FILE, FX_FALL_FILE, false, Assets.VOLUME);
        }
    }

    public void handleItemToObjectContact(ItemModel item, Object object) {
        if (object instanceof HoleModel) {
            HumanoidModel p = item.holdingPlayer;
            if (p == null && (item.getVX()!=0f || item.getVY()!=0)) {
                item.startRespawn();
                // SoundController.getInstance().play(FX_FALL_FILE, FX_FALL_FILE, false, Assets.VOLUME);
            }

        } else if (object instanceof HomeModel && item.lastTouch instanceof PlayerModel) {
                PlayerModel lastTouchedPlayer = (PlayerModel) item.lastTouch;
                PlayerModel p = (PlayerModel) item.holdingPlayer;

                if (p == null && lastTouchedPlayer.getTeam().equals(((HomeModel) object).getTeam())) {
                    item.startRespawn();
                    // add score
                    HomeModel homeObject = (HomeModel) object;
                    homeObject.incrementScore(1);

                    // check win condition
                    checkWinCondition(homeObject);
                }
        }
    }

    /**
     * Collision handler for firecrackers to objects
     */
    public void handleFirecrackerToObjectContact(FirecrackerModel firecracker, Object object) {
        if (object instanceof HoleModel) {
            firecracker.addContactHole((HoleModel) object);
        }
    }

    public void checkWinCondition(HomeModel homeObject) {
        if (homeObject.getScore() >= ITEMS_TO_WIN) {
            worldModel.completeLevel(true);
            if (homeObject.getTeam().equals("teamA")) {
                worldModel.winner = "PLAYER 1 ";
            } else if (homeObject.getTeam().equals("teamB")) {
                worldModel.winner = "PLAYER 2 ";
            }
        }
    }
}
