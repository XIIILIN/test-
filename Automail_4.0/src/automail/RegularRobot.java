package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import simulation.IMailDelivery;
import util.Configuration;

public class RegularRobot extends Robot {
    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     *
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     * @param number
     */
    public RegularRobot(IMailDelivery delivery, MailPool mailPool, int number, ServiceTimeCalculator serviceTimeCalculator, ServiceFeeCalculator serviceFeeCalculator) {
        super(delivery, mailPool, number, serviceTimeCalculator, serviceFeeCalculator);
        robot_type = RobotType.REGULAR;
    }

    @Override
    public void doDelivery() throws ExcessiveDeliveryException {
        Configuration configuration = Configuration.getInstance();
        boolean feeCharge = Boolean.parseBoolean(configuration.getProperty(Configuration.FEE_CHARGING_KEY));
        String extra = "";

        if (feeCharge) {
            int num_regular_robots = Integer.parseInt(configuration.getProperty(Configuration.REGULAR_ROBOTS_KEY));

            double serviceFee = serviceFeeCalculator.calculateServiceFee(deliveryItem.getDestFloor());
            double avgOperatingTime = serviceTimeCalculator.getServiceTime(this.robot_type) * 1.0 / num_regular_robots;

            double maintenance = getMaintenanceRate() * avgOperatingTime;
            double totalCharge = serviceFee + maintenance;
            extra = String.format(" | Service Fee: %.2f | Maintenance: %.2f | Avg. Operating Time: %.2f | Total Charge: %.2f",
                    serviceFee, maintenance, avgOperatingTime, totalCharge);
        }
        delivery.deliver(this, deliveryItem, extra);
        deliveryItem = null;
        deliveryCounter++;
        if (deliveryCounter > 2) {  // Implies a simulation bug
            throw new ExcessiveDeliveryException();
        }
        /** Check if want to return, i.e. if there is no item in the tube*/
        if (tube.size() == 0) {
            changeState(RobotState.RETURNING);
        } else {
            /** If there is another item, set the robot's route to the location to deliver the item */
            deliveryItem = tube.remove(tube.size() - 1);
            setDestination();
            changeState(RobotState.DELIVERING);
        }

    }

    @Override
    public double getMaintenanceRate() {
        return 0.025;
    }


    @Override
    protected void moveTowards(int destination) {
        super.moveTowards(destination);
    }

    @Override
    public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
        if (getTube().size() == 0) {
            super.addToTube(mailItem);
        } else {
            throw new ItemTooHeavyException();
        }
    }
}
