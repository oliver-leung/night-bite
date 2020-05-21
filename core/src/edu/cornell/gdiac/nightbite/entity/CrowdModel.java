package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.util.PooledList;

import java.util.Random;

public class CrowdModel {

    public static int MAX_PEOPLE_IN_CROWD = 1;  // change back to 5 eventually... when less crowded
    private static final int SPAWN_TO_ROAM_RATE = 5;
    private static final int ROAM_RADIUS = 4;
    private static final int CHANGE_LEADER_TIME = 100;

    private String[] textureList = {"character/Filmstrip/NPC1_Walk_8.png", "character/Filmstrip/NPC2_Walk_8.png", "character/Filmstrip/NPC3_Walk_8.png", "character/Filmstrip/NPC4_Walk_8.png"};
    private enum State {
        IDLE,
        ROAM
    }
    public State state;
    public int leaderIndex;
    public int leaderIndexTime;
    private PooledList<CrowdUnitModel> crowdUnitList;
    public Vector2 targetPos;
    private float previousDistanceFromTarget;
    public WorldModel worldModel;

    public CrowdModel(float x, float y, float width, float height, WorldModel worldModel) {
        crowdUnitList = new PooledList<>();
        for (int i = 0; i < new Random().nextInt(MAX_PEOPLE_IN_CROWD) + 2; i++) {
            CrowdUnitModel crowdUnit = new CrowdUnitModel(x + new Random().nextFloat(), y + new Random().nextFloat(), width, height, textureList[new Random().nextInt(textureList.length)], worldModel);
            crowdUnit.setDrawScale(worldModel.getScale());
            crowdUnit.setActualScale(worldModel.getActualScale());
            crowdUnit.setFixedRotation(true);
            crowdUnitList.add(crowdUnit);
        }
        state = State.IDLE;
        targetPos = new Vector2(x, y);
        this.worldModel = worldModel;
        leaderIndex = 0;
        leaderIndexTime = CHANGE_LEADER_TIME;
    }

    public PooledList<CrowdUnitModel> getCrowdUnitList() {
        return crowdUnitList;
    }

    public void update(float dt) {
        switch (state) {
            case IDLE:
                if (new Random().nextInt(SPAWN_TO_ROAM_RATE) == 0) {
                    targetPos.x = new Random().nextInt(ROAM_RADIUS * 2) - ROAM_RADIUS + crowdUnitList.get(0).getHomePosition().x;
                    targetPos.y = new Random().nextInt(ROAM_RADIUS * 2) - ROAM_RADIUS + crowdUnitList.get(0).getHomePosition().y;

                    // bounding to screen lmfao
                    targetPos.x = Math.min(worldModel.getWidth(), targetPos.x);
                    targetPos.y = Math.min(worldModel.getHeight(), targetPos.y);
                    targetPos.x = Math.max(0, targetPos.x);
                    targetPos.y = Math.max(0, targetPos.y);

                    for (CrowdUnitModel crowdUnit: getCrowdUnitList()) {
                        crowdUnit.move(targetPos, crowdUnit.getDimension(), worldModel.getAILattice());
                    }
                    state = State.ROAM;
                }
                break;
            case ROAM:
//                for (CrowdUnitModel crowdUnit: getCrowdUnitList()) {
//                    crowdUnit.move(targetPos, crowdUnit.getDimension(), worldModel.getAILattice());
//                }
//                float distanceFromTarget = getCrowdUnitList().get(0).getPosition().dst(targetPos); // TODO does this actualy work
//                if (distanceFromTarget == previousDistanceFromTarget) {
//                    previousDistanceFromTarget = 0f;
//                    state = State.IDLE;
//                    return;
//                }
//                previousDistanceFromTarget = distanceFromTarget;
//                break;

                // leader ai

                updateChangeLeader();
                float distanceFromTarget;
                for (int i = 0; i < getCrowdUnitList().size(); i++) {
                    getCrowdUnitList().get(i).setWalkTexture(dt);
                    if (i == leaderIndex) { // leader
                        getCrowdUnitList().get(leaderIndex).move(targetPos, getCrowdUnitList().get(leaderIndex).getDimension(), worldModel.getAILattice());
                        distanceFromTarget = getCrowdUnitList().get(leaderIndex).getPosition().dst(targetPos);
                        if (distanceFromTarget == previousDistanceFromTarget) {
                            previousDistanceFromTarget = 0f;
                            state = State.IDLE;
                            leaderIndexTime = CHANGE_LEADER_TIME;
                            getCrowdUnitList().get(leaderIndex).setStaticTexture();
                            return;
                        }
                        previousDistanceFromTarget = distanceFromTarget;
                    } else {
                        Vector2 leaderPos = getCrowdUnitList().get(leaderIndex).getPosition();
                        distanceFromTarget = getCrowdUnitList().get(i).getPosition().dst(leaderPos);
                        if (distanceFromTarget > 0.6f) { // TODO
                            getCrowdUnitList().get(i).move(leaderPos, getCrowdUnitList().get(i).getDimension(), worldModel.getAILattice());
                        }
                    }
                }
                break;
        }
    }

    public void updateChangeLeader() {
        leaderIndexTime--;
        if (leaderIndexTime == 0) {
            leaderIndexTime = CHANGE_LEADER_TIME;
            leaderIndex = new Random().nextInt(getCrowdUnitList().size());
        }
    }
}
