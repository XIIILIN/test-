package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.Clock;
import simulation.IMailDelivery;

import java.util.ArrayList;
import java.util.List;

/**
 * The robot delivers mail!
 */
public abstract class Robot {

    protected static final int INDIVIDUAL_MAX_WEIGHT = 2000;

    protected IMailDelivery delivery;
    protected String id;
    protected int startWorkTime;

    /**
     * Possible states the robot can be in
     */
    public enum RobotState {DELIVERING, WAITING, RETURNING}

    protected RobotType robot_type;
    protected RobotState current_state;
    protected int current_floor;
    protected int destination_floor;
    protected MailPool mailPool;
    protected boolean receivedDispatch;
    protected ServiceTimeCalculator serviceTimeCalculator;
    protected ServiceFeeCalculator serviceFeeCalculator;

    protected MailItem deliveryItem = null;
    protected List<MailItem> tube = new ArrayList<>();

    protected int deliveryCounter;


    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     *
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public Robot(IMailDelivery delivery, MailPool mailPool, int number, ServiceTimeCalculator serviceTimeCalculator, ServiceFeeCalculator serviceFeeCalculator) {
        this.id = "R" + number;
        this.serviceTimeCalculator = serviceTimeCalculator;
        this.serviceFeeCalculator = serviceFeeCalculator;
        current_state = RobotState.RETURNING;
        current_floor = Building.getInstance().getMailroomLocationFloor();
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
    }

    /**
     * This is called when a robot is assigned the mail items and ready to dispatch for the delivery
     */
    public void dispatch() {
        receivedDispatch = true;
    }

    public abstract void doDelivery() throws ExcessiveDeliveryException;

    /**
     * This is called on every time step
     *
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void operate() throws ExcessiveDeliveryException {
        if(this.current_state!=RobotState.WAITING){
            serviceTimeCalculator.doService(Clock.Time(), robot_type);
        }

        switch (current_state) {
            /** This state is triggered when the robot is returning to the mailroom after a delivery */
            case RETURNING:
                /** If its current position is at the mailroom, then the robot should change state */
                if (current_floor == Building.getInstance().getMailroomLocationFloor()) {
                    /** Tell the sorter the robot is ready */
                    mailPool.registerWaiting(this);
                    changeState(RobotState.WAITING);
                } else {
                    /** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.getInstance().getMailroomLocationFloor());
                    break;
                }
            case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if (!isEmpty() && receivedDispatch) {
                    receivedDispatch = false;
                    deliveryCounter = 0; // reset delivery counter
                    setDestination();
                    changeState(RobotState.DELIVERING);
                }
                break;
            case DELIVERING:
                if (current_floor == destination_floor) { // If already here drop off either way
                    /** Delivery complete, report this to the simulator! */
                    doDelivery();
                } else {
                    /** The robot is not at the destination yet, move towards it! */
                    moveTowards(destination_floor);
                }
                break;
        }
    }

    /**
     * Sets the route for the robot
     */
    protected void setDestination() {
        /** Set the destination floor */
        destination_floor = deliveryItem.getDestFloor();
    }

    /**
     * Generic function that moves the robot towards the destination
     *
     * @param destination the floor towards which the robot is moving
     */
    protected void moveTowards(int destination) {
        if (current_floor < destination) {
            current_floor++;
        } else {
            current_floor--;
        }
    }

    public String getIdTube() {
        return String.format("%s(%1d)", this.id, (tube.size()));
    }

    /**
     * Prints out the change in state
     *
     * @param nextState the state to which the robot is transitioning
     */
    protected void changeState(RobotState nextState) {
        assert (!(deliveryItem == null && tube != null));
        if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
        }
        if (current_state == RobotState.WAITING && nextState == RobotState.DELIVERING) {
            startWorkTime = Clock.Time();
        }

        current_state = nextState;
        if (nextState == RobotState.DELIVERING) {
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
        }
    }

    public abstract double getMaintenanceRate();



    public List<MailItem> getTube() {
        return tube;
    }

    public boolean isEmpty() {
        return (deliveryItem == null && tube.size() == 0);
    }

    public void addToHand(MailItem mailItem) throws ItemTooHeavyException {
        assert (deliveryItem == null);
        deliveryItem = mailItem;
        if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
    }

    public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
        double weight = 0;
        for (MailItem item : tube) {
            weight += item.getWeight();
        }
        if (weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
        tube.add(mailItem);
    }

}
