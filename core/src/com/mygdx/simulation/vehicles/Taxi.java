package com.mygdx.simulation.vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.mygdx.simulation.TransportSimulation;

/**
 * Created by Digilogue on 19/11/2016.
 * <p>
 * Taxi Class - represents the in-simulation sprite for the Aerial Taxi vehicle. Maintains state, update logic and
 * rendering.
 */
public class Taxi {

    private static final float DIVE_ACCEL = 0.075F;
    private static final float HORIZ_ACCEL = 0.15F;
    private static final float THRUST_UP_ACCEL = 0.2F;
    private static final float THRUST_DOWN_ACCEL = 0.05F;
    private static final float LANDING_PYLON_LEFT_X = 5;
    private static final float LANDING_PYLON_RIGHT_X = 80;
    private static final float TAXI_LENGTH = 85;
    private static final float TAXI_HEIGHT = 43;
    private static final float THRUSTER_FRAME_DURATION = 0.03F;
    private static final int THRUSTER_TILE_WIDTH = 16;
    private static final int THRUSTER_TILE_HEIGHT = 16;
    private static final float MAX_SPEED = 5;

    private Texture taxiLeft;
    private Texture taxiRight;
    private Texture taxiLeftGear;
    private Texture taxiRightGear;

    Animation<TextureRegion> thrusterUpAnimation;
    Animation<TextureRegion> thrusterDownAnimation;
    Animation<TextureRegion> thrusterLeftAnimation;
    Animation<TextureRegion> thrusterRightAnimation;

    TextureRegion thrusterToRender;
    TextureRegion[][] thrusterTextures;
    private Rectangle pickupPlatform;
    private float delta;

    public enum Direction {UP, DOWN, LEFT, RIGHT}

    // private Direction thrusterDirection = Direction.RIGHT;
    private Direction leftRightFlag = Direction.RIGHT;
    private Direction travellingLeftRightDirection = Direction.RIGHT; // The current direction of travel.
    private Direction travellingUpDownDirection = Direction.DOWN; // The current direction of travel.

    private boolean landingGear = true;
    private boolean automateTaxi = false;
    private boolean pickupTraveller = false;

    private float x = 906;
    private float y = 3600;

    private float previousX = 0;
    private float previousY = -7;

    private float ySpeed = 0;
    private float xSpeed = 0;

    private float maxDistanceBeforeApplyingUpThrustUpdates;
    private int upthrustUpdatesToVerticalEquilibrium = 0;

    private boolean landed = false;

    private float thrusterAnimationTimer = 0;

    private boolean thrusterDirectionUp = false;
    private boolean thrusterDirectionDown = false;
    private boolean thrusterDirectionLeft = false;
    private boolean thrusterDirectionRight = false;

    /**
     * Constructor fetching sprite assets (taxiLeft, taxiRight, taxiLeftGear, taxiRightGear) and setting up thruster
     * animation.
     *
     * @param transportSimulation
     */
    public Taxi(TransportSimulation transportSimulation) {
        getAssets(transportSimulation);
        setupThrusterAnimation(transportSimulation);
        maxDistanceBeforeApplyingUpThrustUpdates = calculateMaxDistanceToTravelWhileApplyingUpThrustUpdates(MAX_SPEED);
    }

    public Taxi() {
    }

    private void getAssets(TransportSimulation transportSimulation) {
        taxiLeft = transportSimulation.getAssetManager().get("Taxi-Left.png", Texture.class);
        taxiRight = transportSimulation.getAssetManager().get("Taxi-Right.png", Texture.class);
        taxiLeftGear = transportSimulation.getAssetManager().get("Taxi-Left-Gear.png", Texture.class);
        taxiRightGear = transportSimulation.getAssetManager().get("Taxi-Right-Gear.png", Texture.class);
    }

    private void setupThrusterAnimation(TransportSimulation transportSimulation) {
        setupUpThrusterAnimation(transportSimulation);
        setupDownThrusterAnimation(transportSimulation);
        setupLeftThrusterAnimation(transportSimulation);
        setupRightThrusterAnimation(transportSimulation);
    }

    private void setupRightThrusterAnimation(TransportSimulation transportSimulation) {
        TextureRegion thrusterRight = new TextureRegion(transportSimulation.getAssetManager().get("thruster-right.png",
                Texture
                        .class));
        thrusterTextures = new TextureRegion(thrusterRight).split(THRUSTER_TILE_WIDTH,
                THRUSTER_TILE_HEIGHT);
        thrusterRightAnimation = new Animation(THRUSTER_FRAME_DURATION, thrusterTextures[0][0],
                thrusterTextures[0][1]);
        thrusterRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    private void setupLeftThrusterAnimation(TransportSimulation transportSimulation) {
        TextureRegion thrusterLeft = new TextureRegion(transportSimulation.getAssetManager().get("thruster-left.png",
                Texture
                        .class));
        thrusterTextures = new TextureRegion(thrusterLeft).split(THRUSTER_TILE_WIDTH,
                THRUSTER_TILE_HEIGHT);
        thrusterLeftAnimation = new Animation(THRUSTER_FRAME_DURATION, thrusterTextures[0][0],
                thrusterTextures[0][1]);
        thrusterLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    private void setupDownThrusterAnimation(TransportSimulation transportSimulation) {
        TextureRegion thrusterDown = new TextureRegion(transportSimulation.getAssetManager().get("thruster-down.png",
                Texture
                        .class));
        thrusterTextures = new TextureRegion(thrusterDown).split(THRUSTER_TILE_WIDTH,
                THRUSTER_TILE_HEIGHT);
        thrusterDownAnimation = new Animation(THRUSTER_FRAME_DURATION, thrusterTextures[0][0],
                thrusterTextures[0][1]);
        thrusterDownAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    private void setupUpThrusterAnimation(TransportSimulation transportSimulation) {
        TextureRegion thrusterUp = new TextureRegion(transportSimulation.getAssetManager().get("thruster-up.png", Texture
                .class));
        thrusterTextures = new TextureRegion(thrusterUp).split(THRUSTER_TILE_WIDTH,
                THRUSTER_TILE_HEIGHT);
        thrusterUpAnimation = new Animation(THRUSTER_FRAME_DURATION, thrusterTextures[0][0],
                thrusterTextures[0][1]);
        thrusterUpAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    /**
     * Taxi's draw method for rendering appropriate sprite textures (taxiLeft, taxiRight, taxiLeftGear, taxiRightGear)
     * at current position.
     *
     * @param batch
     */
    public void draw(Batch batch) {
        drawTaxi(batch);
        drawThrusters(batch);
    }

    private void drawTaxi(Batch batch) {
        if (leftRightFlag == Direction.LEFT) {
            if (landingGear)
                batch.draw(taxiLeftGear, Math.round(x), Math.round(y));
            else
                batch.draw(taxiLeft, Math.round(x), Math.round(y));
        }
        if (leftRightFlag == Direction.RIGHT) {
            if (landingGear)
                batch.draw(taxiRightGear, Math.round(x), Math.round(y));
            else
                batch.draw(taxiRight, Math.round(x), Math.round(y));
        }
    }

    private void drawThrusters(Batch batch) {
        if (thrusterDirectionUp) {
            thrusterToRender = thrusterUpAnimation.getKeyFrame(thrusterAnimationTimer);
            batch.draw(thrusterToRender, Math.round(x) + (TAXI_LENGTH / 2) - (THRUSTER_TILE_WIDTH / 2), Math.round(y) -
                    5);
        }

        if (thrusterDirectionDown) {
            thrusterToRender = thrusterDownAnimation.getKeyFrame(thrusterAnimationTimer);
            batch.draw(thrusterToRender, Math.round(x) + (TAXI_LENGTH / 2) - (THRUSTER_TILE_WIDTH / 2), Math.round(y) +
                    43);
        }

        if (thrusterDirectionLeft) {
            thrusterToRender = thrusterLeftAnimation.getKeyFrame(thrusterAnimationTimer);
            batch.draw(thrusterToRender, Math.round(x) + TAXI_LENGTH, Math.round(y) + 20);
        }

        if (thrusterDirectionRight) {
            thrusterToRender = thrusterRightAnimation.getKeyFrame(thrusterAnimationTimer);
            batch.draw(thrusterToRender, Math.round(x) - 15, Math.round(y) + 20);
        }
    }

    /**
     * Main update logic for the taxi. Processes user input, processes collisions with platforms, works out travel
     * direction, processes collisions with world boundaries, processes dive logic and finally sets the taxi's
     * position.
     *
     * @param delta
     * @param platforms
     * @param worldWidth
     * @param worldHeight
     */
    public void update(float delta, Array<Rectangle> platforms, float worldWidth, float worldHeight) {

        this.delta = delta;
        updateAnimationTimers(delta);
        processInput();
        processCollisionWithPlatforms(platforms);
        workOutTravellingDirection();
        processCollisionWithWorldBoundaries(worldWidth, worldHeight);
        processDive();

        setPosition(x + xSpeed, y + ySpeed);
    }

    private void updateAnimationTimers(float delta) {
        thrusterAnimationTimer += delta;
    }

    /**
     * If taxi is not landed then proceed with dive.
     */
    private void processDive() {
        if (!landed) {
            ySpeed -= DIVE_ACCEL;
//            if (ySpeed < -MAX_SPEED) {
//                thrusterDirectionUp = true;
//                ySpeed += THRUST_UP_ACCEL;
//                landed = false;
//            } else {
//                ySpeed -= DIVE_ACCEL;
//            }
        }
    }

    /**
     * Detects if taxi collides with any of the world boundaries then prevents that direction from advancing beyond
     * the boundary.
     *
     * @param worldWidth
     * @param worldHeight
     */
    private void processCollisionWithWorldBoundaries(float worldWidth, float worldHeight) {
        if (x < 0) {
            x = 0;
            xSpeed = 0;
        }

        if (x > worldWidth - TAXI_LENGTH) {
            x = worldWidth - TAXI_LENGTH;
            xSpeed = 0;
        }

        if (y < 0) {
            y = 0;
            ySpeed = 0;
            landed = true;
        }

        if (y > worldHeight - TAXI_HEIGHT) {
            y = worldHeight - TAXI_HEIGHT;
            ySpeed = 0;
        }
    }

    /**
     * Processes user input and updates taxi's x, y, speed and direction states accordingly.
     */
    private void processInput() {

        thrusterDirectionUp = false;
        thrusterDirectionDown = false;
        thrusterDirectionLeft = false;
        thrusterDirectionRight = false;

        controlTaxi();
    }

    private void controlTaxi() {
        if (automateTaxi) {
            automaticControlTaxi();
        } else {
            manualControlTaxi();
        }
    }

    private void automaticControlTaxi() {
        if (pickupTraveller) {
            disableLandingGear();

            // Always calculate the (Max Distance Before Applying Up Thrust Updates) during each iteration
            // of the game loop as this can always change depending on the taxis speed.
            maxDistanceBeforeApplyingUpThrustUpdates = calculateMaxDistanceToTravelWhileApplyingUpThrustUpdates
                    (Math.abs(ySpeed));

            // Do we need to go up?
            // If there are upthrust updates, then apply those updates by inc the ySpeed.
            if (upthrustUpdatesToVerticalEquilibrium > 0) {
                upThrust();
                return;
            }

            // If there are no upthrust updates, then check to see if the platforms y pos is
            // within the (Max Distance Before Applying Up Thrust Updates) of the taxi.
            // If it is, then calculate the no of (Upthrust Updates To Vertical Equilibrium) to
            // be performed so we we may apply them next time round the loop.
            if (pickupPlatform.getY() > y - maxDistanceBeforeApplyingUpThrustUpdates) {
                upthrustUpdatesToVerticalEquilibrium =
                        (int) Math.ceil(calculateUpthrustUpdatesToVerticalEquilibrium(Math.abs(ySpeed)));

                landed = false;
                return;
            }

            // Determine if we require more upThrust bursts.
//            if (travellingUpDownDirection == Direction.UP) {
//                // Calculate how many additional upbursts are needed
//                upthrustUpdatesToVerticalEquilibrium = (int) ((getY() - pickupPlatform.getY()) / (THRUST_UP_ACCEL -
//                        DIVE_ACCEL));
//                System.out.println("*** UPBURST !");
//            }

            // Do we need to go down?
//            if (pickupPlatform.getY() < y) {
//                thrusterDirectionDown = true;
//                if (ySpeed > -MAX_SPEED)
//                    ySpeed -= THRUST_DOWN_ACCEL;
//                landed = false;
//            }
            // Do we need to go left?
            // System.out.println("* taxi needs to go left");
            // Do we need to go right?
            // System.out.println("* taxi needs to go right");
        }
    }

    /**
     * Assigns a single upThrust burst to the taxi.
     */
    private void upThrust() {
        thrusterDirectionUp = true;
        ySpeed += THRUST_UP_ACCEL;
        upthrustUpdatesToVerticalEquilibrium--;
    }

    /**
     * If landing gear is enabled, disable.
     */
    private void disableLandingGear() {
        if (landingGear) {
            landingGear = !landingGear;
        }
    }


    public static void main(String[] args) {
        Taxi taxi = new Taxi();
        System.out.println("No of updates: " + taxi.calculateUpthrustUpdatesToVerticalEquilibrium(4));
        System.out.println("Max distance: " + taxi.calculateMaxDistanceToTravelWhileApplyingUpThrustUpdates(4));
        System.out.println();

        float speed = 5;
        float totalDistance = 0;
        for (int i = 0; i < 40; i++) {
            totalDistance += speed;
            speed = speed - 0.125f;
        }
        System.out.println("Speed is: " + speed);
        System.out.println("Total Distance is: " + totalDistance);
    }

    /**
     * Calculates the total no of upthrust updates that need to be applied to the taxi before it reaches 0 or
     * equilibrium
     *
     * @param velocityInPixels
     * @return
     */
    private float calculateUpthrustUpdatesToVerticalEquilibrium(float velocityInPixels) {
        if (calculateUpThrustDuringClimb())
            return upthrustUpdatesToVerticalEquilibrium;

        return calculateUpThrustDuringFreeFall(velocityInPixels);
    }

    /**
     * Determine how many upThrust bursts are required if the taxi is already climbing.
     *
     * @return
     */
    private boolean calculateUpThrustDuringClimb() {
        if (travellingUpDownDirection == Direction.UP && upthrustUpdatesToVerticalEquilibrium < 1) {
            if (pickupPlatform.getY() > getY()) {
                // We assume that vertical velocity is 0.
                float distanceToClimb = pickupPlatform.getY() - getY();
                float tempDistance = 0;
                int noOfUpdates = 0;

                while (tempDistance < distanceToClimb) {
                    tempDistance += tempDistance + (THRUST_UP_ACCEL - DIVE_ACCEL);
                    noOfUpdates++;
                }

                upthrustUpdatesToVerticalEquilibrium = noOfUpdates;
            }
            System.out.println("*** plaform y - " + pickupPlatform.getY());
            System.out.println("*** taxi y - " + getY());
            System.out.println("*** Do we get called for upthrust. " + upthrustUpdatesToVerticalEquilibrium + " - " + travellingUpDownDirection);
            return true;
        }
        return false;
    }

    /**
     * Determine how many upThrust bursts are required if taxi is going down or free falling.
     *
     * @param velocityInPixels
     * @return
     */
    private float calculateUpThrustDuringFreeFall(float velocityInPixels) {
        float upThrustUpdates = 0;
        if (travellingUpDownDirection == Direction.DOWN) {
            upThrustUpdates = (velocityInPixels / (THRUST_UP_ACCEL - DIVE_ACCEL)) + 1;
            System.out.println("* upthrust FREE FALL " + travellingUpDownDirection + " - " + upThrustUpdates);
        }
        return upThrustUpdates;
    }

    /**
     * Essentially calculates the perimeter edge or the maximum distance that the taxi has to travel
     * while constantly applying Up Thrusts before the taxi reaches a speed of 0 or equilibrium.
     *
     * @param velocityInPixels
     * @return
     */
    private float calculateMaxDistanceToTravelWhileApplyingUpThrustUpdates(float velocityInPixels) {

        float noOfUpdates = (float) Math.ceil(calculateUpThrustDuringFreeFall(velocityInPixels));
        float distanceLostOverUpdatesAtConstantVelocity = 0;

        float tempVelocityInPixels = velocityInPixels;

        for (int i = 0; i < noOfUpdates; i++) {
            distanceLostOverUpdatesAtConstantVelocity += tempVelocityInPixels;
            tempVelocityInPixels -= (THRUST_UP_ACCEL - DIVE_ACCEL);
        }

        return distanceLostOverUpdatesAtConstantVelocity;
    }

    private void manualControlTaxi() {
        Input input = Gdx.input;
        if (input.isKeyPressed(Input.Keys.RIGHT)) {
            thrusterDirectionRight = true;
            leftRightFlag = Direction.RIGHT;
            xSpeed += HORIZ_ACCEL;
            landed = false;
        }
        if (input.isKeyPressed(Input.Keys.LEFT)) {
            thrusterDirectionLeft = true;
            leftRightFlag = Direction.LEFT;
            xSpeed -= HORIZ_ACCEL;
            landed = false;
        }
        if (input.isKeyPressed(Input.Keys.UP)) {
            thrusterDirectionUp = true;
            ySpeed += THRUST_UP_ACCEL;
            landed = false;
        }
        if (input.isKeyPressed(Input.Keys.DOWN)) {
            thrusterDirectionDown = true;
            ySpeed -= THRUST_DOWN_ACCEL;
            landed = false;
        }
        if (input.isKeyJustPressed(Input.Keys.SPACE)) {
            landingGear = !landingGear;
            if (!landingGear)
                landed = false;
        }
    }

    /**
     * Works out the travelling direction of taxi and sets appropriate state.
     */
    private void workOutTravellingDirection() {
        if (previousX > x)
            travellingLeftRightDirection = Direction.LEFT;
        if (previousX < x)
            travellingLeftRightDirection = Direction.RIGHT;

        if (previousY > y)
            travellingUpDownDirection = Direction.DOWN;
        if (previousY < y)
            travellingUpDownDirection = Direction.UP;

    }

    /**
     * Detects whether taxi has landed on a platform. If does this if both landing pylons land within the platform
     * boundaries. If so, stop taxi and set to landed.
     *
     * @param platforms
     */
    private void processCollisionWithPlatforms(Array<Rectangle> platforms) {

        if (landingGear) {
            for (Rectangle rectangle : platforms) {
                if (((rectangle.getY() + rectangle.getHeight() <= previousY &&
                        rectangle.getY() + rectangle.getHeight() >= y))) {
                    float landingPylonOffsetLeftX = LANDING_PYLON_LEFT_X + x;
                    float landingPylonOffsetRightX = LANDING_PYLON_RIGHT_X + x;
                    float platformRightEdge = rectangle.getX() + rectangle.getWidth();

                    if ((rectangle.getX() <= landingPylonOffsetLeftX) &&
                            (platformRightEdge >= landingPylonOffsetRightX)) {
                        xSpeed = 0;
                        ySpeed = 0;
                        landed = true;
                        System.out.println("taxi y is: " + y);
                        System.out.println("current platform y is: " + rectangle.getY());
                    }
                }
            }
        }
    }

    public void pickupTraveller(Rectangle pickupPlatform) {
        pickupTraveller = true;
        this.pickupPlatform = pickupPlatform;
        System.out.println("pickup platform y is: " + pickupPlatform.getY());
    }

    /**
     * Sets the position of the taxi and takes a note of it's previous position.
     *
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        this.previousX = this.x;
        this.previousY = this.y;

        this.x = x;
        this.y = y;
    }

    /**
     * Get x
     *
     * @return x
     */
    public float getX() {
        return x;
    }

    /**
     * Get y
     *
     * @return y
     */
    public float getY() {
        return y;
    }

    /**
     * Get travellingLeftRightDirection
     *
     * @return travellingLeftRightDirection
     */
    public Direction getTravellingLeftRightDirection() {
        return travellingLeftRightDirection;
    }

    /**
     * Get travellingUpDownDirection
     *
     * @return travellingUpDownDirection
     */
    public Direction getTravellingUpDownDirection() {
        return travellingUpDownDirection;
    }

    public boolean isAutomateTaxi() {
        return automateTaxi;
    }

    public void setAutomateTaxi(boolean automateTaxi) {
        this.automateTaxi = automateTaxi;
    }

    public float getySpeed() {
        return ySpeed;
    }

    public void setySpeed(float ySpeed) {
        this.ySpeed = ySpeed;
    }

    public float getxSpeed() {
        return xSpeed;
    }

    public void setxSpeed(float xSpeed) {
        this.xSpeed = xSpeed;
    }
}