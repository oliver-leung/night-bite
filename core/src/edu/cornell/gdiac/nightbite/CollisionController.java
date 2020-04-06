package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import edu.cornell.gdiac.nightbite.entity.HoleModel;
import edu.cornell.gdiac.nightbite.entity.HomeModel;
import edu.cornell.gdiac.nightbite.entity.ItemModel;
import edu.cornell.gdiac.nightbite.entity.PlayerModel;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;


public class CollisionController implements ContactListener {
    protected static final float PUSH_IMPULSE = 200f;

    public static final int ITEMS_TO_WIN = 3;

    private WorldModel worldModel;

    public CollisionController(WorldModel worldModel) {
        this.worldModel = worldModel;
    }

    public void beginContact(Contact contact) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();

        // Player-Object Contact
        if (a instanceof PlayerModel) {
            handlePlayerToObjectContact((PlayerModel) a, b);
        } else if (b instanceof PlayerModel) {
            handlePlayerToObjectContact((PlayerModel) b, a);
        }

        if (a instanceof ItemModel) {
            handleItemToObjectContact((ItemModel) a, b);
        } else if (b instanceof ItemModel) {
            handleItemToObjectContact((ItemModel) b, a);
        }

    }

    public void endContact(Contact contact) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();
        if (a instanceof PlayerModel && b instanceof BoxObstacle && ((BoxObstacle) b).getName().equals("item")) {
            int itemId = ((ItemModel) b).getId();
            ((PlayerModel) a).setOverlapItem(itemId, false);
        } else if (b instanceof PlayerModel && a instanceof BoxObstacle && ((BoxObstacle) a).getName().equals("item")) {
            int itemId = ((ItemModel) a).getId();
            ((PlayerModel) b).setOverlapItem(itemId, false);
        }
    }

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

    public void preSolve(Contact contact, Manifold oldManifold) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();

        if ((a instanceof PlayerModel && b instanceof ItemModel) || (b instanceof PlayerModel && a instanceof ItemModel)) {
            contact.setEnabled(false);
        }
    }


    public void handlePlayerToObjectContact(PlayerModel player, Object object) {

        if (object instanceof HoleModel) {

            // Player-Hole collision
            player.setDead();

            if (player.hasItem()) { // TODO fix jank implementation
                for (Integer id : player.getItemId()) {
                    ItemModel item = worldModel.getItem(id);
                    item.setUnheld();
                    item.startRespawn();
                }
            }

        } else if (object instanceof ItemModel) {

            // Player-Item
            int id = ((ItemModel) object).getId();
            player.setOverlapItem(id, true);

        } else if (object instanceof HomeModel) {

            // Player-Home
            HomeModel homeObject = (HomeModel) object;
            // If players went to their own home, drop off item and increment score
            if (player.getTeam().equals(homeObject.getTeam()) && player.hasItem()) {

                int numCarriedItems = player.getItemId().size();
                homeObject.incrementScore(numCarriedItems);

                for (Integer id : player.getItemId()) {
                    ItemModel item = worldModel.getItem(id);
                    item.setUnheld();
                    item.startRespawn();
                }

                // win condition
                checkWinCondition(homeObject);
            }
        }
    }

    public void handleItemToObjectContact(ItemModel item, Object object) {
        if (object instanceof HoleModel) {
            PlayerModel p = item.holdingPlayer;
            if (p == null) {
                item.startRespawn();
            }
        } else if (object instanceof HomeModel && item.lastTouch.getTeam().equals(((HomeModel) object).getTeam())) {
            item.setUnheld();
            item.startRespawn();

            // add score
            HomeModel homeObject = (HomeModel) object;
            homeObject.incrementScore(1);

            // check win condition
            checkWinCondition(homeObject);
        }
    }

    public void checkWinCondition(HomeModel homeObject) {
        if (homeObject.getScore() >= ITEMS_TO_WIN) {
            worldModel.completeLevel();
            if (homeObject.getTeam().equals("a")) {
                worldModel.winner = "PLAYER A ";
            } else if (homeObject.getTeam().equals("b")) {
                worldModel.winner = "PLAYER B ";
            }
        }
    }
}
